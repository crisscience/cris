/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.storage;

import edu.purdue.cybercenter.dm.domain.StorageAccessMethod;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.service.StorageService;
import edu.purdue.cybercenter.dm.util.Helper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.task.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author xu222
 * @author jain117
 */
@Configurable
public class GlobusStorageFileManager extends AbstractStorageFileManager {
    private static final String GLOBUS = "globus/";

    private final String globusBaseUrl; // = "https://transfer.api.globusonline.org/v0.10";
    private final String globusEndpoint; // = "purdue#rcac";
    private final String globusRoot; // = "/~/";

    @Autowired
    private StorageService storageService;

    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private TaskExecutor syncTaskExecutor;

    static final private Logger LOGGER = LoggerFactory.getLogger(GlobusStorageFileManager.class.getName());

    private class CheckAndUpdateTransferStatusTask implements Runnable {
        private final List<String> fileLinks;
        private final List<StorageFile> storageFiles;
        private final String taskId;
        private final String token;

        public CheckAndUpdateTransferStatusTask(List<String> fileLinksToRemove, List<StorageFile> newStorageFiles, String taskId, String token) {
            this.fileLinks = fileLinksToRemove;
            this.storageFiles = newStorageFiles;
            this.taskId = taskId;
            this.token = token;
        }

        @Override
        public void run() {
            // Check the status of the transfer
            try {
                HttpURLConnection connection = makeStatusCheckConnection(taskId, token);
                connection.connect();
                String status = getStatus(connection);
                connection.disconnect();
                while ("ACTIVE".equals(status) || "INACTIVE".equals(status)) {
                    try {
                        System.out.println("Waiting: " + status);
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        LOGGER.error("wait for transfer is interrupted", ex);
                    }

                    connection = makeStatusCheckConnection(taskId, token);
                    connection.connect();
                    status = getStatus(connection);
                    connection.disconnect();
                }

                // remove symbolic links
                if (fileLinks != null) {
                    removeSymbolicLinks(fileLinks);
                }

                if ("SUCCEEDED".equals(status)) {
                    System.out.println("Succeeded");
                } else {
                    System.out.println("Failed");
                    if (storageFiles != null) {
                        for (StorageFile storageFile : storageFiles) {
                            storageFile.setName(token);
                            GlobusStorageFileManager.super.deleteFile(storageFile);
                        }
                        storageFiles.clear();
                    }
                    throw new RuntimeException("Transfer Failed");
                }
            } catch (IOException ex) {
                LOGGER.error("", ex);
            }

        }
    }

    public GlobusStorageFileManager(StorageAccessMethod sam) throws IOException {
        this.globusBaseUrl = sam.getUri();
        String root = sam.getRoot();
        String[] parts = root.split(":");
        if (parts.length == 2) {
            this.globusEndpoint = parts[0];
            this.globusRoot = parts[1];
        } else if (parts.length == 1) {
            this.globusEndpoint = parts[0];
            this.globusRoot = "/";
        } else {
            throw new RuntimeException("Invalid globus root: " + root);
        }
    }

    @Override
    public List<StorageFile> putFile(String source, StorageFile targetStorageFile, boolean sync) throws IOException {
        /* Notes:
         * source endpoint info are passed in from source
         * target endpoint is managed by cris and the root should be: storage.getLocation() + Tenant.findTenant(TenantId.get()).getUuid()
         * if the source contains more than one file, it should always create new storageFile, i.e. targetStorageFile will be ignored
         * otherwise update targetStorageFile if it is not null or create a new storageFile if it is null
         * if sync is true, this method should wait for the transfer to finish.
         */

        // 1: un-wrap input
        Map inputMap = Helper.deserialize(source, Map.class);
        String token = (String) inputMap.get("accessToken");
        String sourceEndpoint = (String) inputMap.get("sourceEndpoint");
        List<String> sourceFilePaths = (List<String>) inputMap.get("filePaths");

        // 2: Get submission_id
        String submissionId = getSubmissionId(token);
        System.out.println(submissionId);

        // 3. Auto-activate endpoint
        int responseCode = activateEndpoint(sourceEndpoint, token);
        System.out.println("Response code: " + responseCode);
        responseCode = activateEndpoint(globusEndpoint, token);
        System.out.println("Response code: " + responseCode);

        // 4. prepare for transfer
        List<StorageFile> destinationStorageFiles = makeStorageFiles(sourceFilePaths);
        List<String> destinationFileLinks = createSymbolicLinks(destinationStorageFiles);
        List<String> destinationGlobusPaths = convertToGlobusPaths(destinationFileLinks);

        // 5. initiate transfer
        String taskId = initiateFileTransfer(sourceEndpoint, sourceFilePaths, globusEndpoint, destinationGlobusPaths, submissionId, token);
        System.out.println("task ID: " + taskId);

        // 6. start a thread to check the status of the transfer
        if (sync) {
            // mainly for test
            syncTaskExecutor.execute(new CheckAndUpdateTransferStatusTask(destinationFileLinks, destinationStorageFiles, taskId, token));
        } else {
            taskExecutor.execute(new CheckAndUpdateTransferStatusTask(destinationFileLinks, destinationStorageFiles, taskId, token));
        }

        return destinationStorageFiles;
    }

    @Override
    public String getFile(StorageFile sourceStorageFile, String target, boolean sync) throws IOException {
        /* Notes:
         * source endpoint info should be constructed from sourceStorageFile
         * target endpoint info is passed in from target
         * if sync is true, this method should wait for the transfer to finish.
         */

        // 1: unwrap input
        Map inputMap = Helper.deserialize(target, Map.class);
        String accessToken = (String) inputMap.get("accessToken");
        String destinationEndpoint = (String) inputMap.get("destinationEndpoint");
        String destinationFolder = (String) inputMap.get("destinationFolder");

        // 2: Get submission_id
        String submissionId = getSubmissionId(accessToken);
        System.out.println(submissionId);

        // 4. prepare for transfer
        List<StorageFile> sourceStorageFiles = new ArrayList<>();
        sourceStorageFiles.add(sourceStorageFile);
        List<String> sourceFileLinks = createSymbolicLinks(sourceStorageFiles);
        List<String> sourceGlobusPaths = convertToGlobusPaths(sourceFileLinks);

        List<String> destinationFilePaths = new ArrayList<>();
        String destinationFilePath;
        if (destinationFolder != null && !destinationFolder.isEmpty()) {
            destinationFilePath = destinationFolder + (destinationFolder.endsWith("/") ? "" : "/") + sourceStorageFile.getFileName();
        } else {
            destinationFilePath = sourceStorageFile.getFileName();
        }
        destinationFilePaths.add(destinationFilePath);
        System.out.println("destination file path: " + destinationFilePath);

        // 5. initiate transfer
        String taskId = initiateFileTransfer(globusEndpoint, sourceGlobusPaths, destinationEndpoint, destinationFilePaths, submissionId, accessToken);
        System.out.println("task ID: " + taskId);

        // 6. start a thread to check the status of the transfer
        if (sync) {
            // mainly for test
            syncTaskExecutor.execute(new CheckAndUpdateTransferStatusTask(sourceFileLinks, null, taskId, accessToken));
        } else {
            taskExecutor.execute(new CheckAndUpdateTransferStatusTask(sourceFileLinks, null, taskId, accessToken));
        }

        return null;
    }

    @Override
    public void deleteFile(StorageFile storageFile) throws IOException {
        String token = storageFile.getName();

        URL task = new URL(globusBaseUrl + "/submission_id");
        HttpURLConnection connection = (HttpURLConnection) task.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);

        Map<String, Object> submissionIdMap = getResponse(connection);
        String submissionId = (String) submissionIdMap.get("value");

        // Preparing JSON data object
        HashMap transferFileObj = new HashMap();
        transferFileObj.put("DATA_TYPE", "delete");
        transferFileObj.put("submission_id", submissionId);
        transferFileObj.put("endpoint", globusEndpoint);
        transferFileObj.put("recursive", true);
        transferFileObj.put("label", "example delete label");

        List<HashMap> fileDeleteList = new ArrayList<HashMap>();
        HashMap delFileData = new HashMap();
        delFileData.put("path", storageFile.getLocation());//.substring(0, storageFile.getLocation().lastIndexOf('/') + 1) + storageFile.getFileName());
        delFileData.put("DATA_TYPE", "delete_item");
        fileDeleteList.add(delFileData);

        transferFileObj.put("DATA", fileDeleteList);

        //Json data ready for transfer
        String delete = "/delete";
        task = new URL(globusBaseUrl + delete);
        connection = (HttpURLConnection) task.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        System.out.println(Helper.deepSerialize(transferFileObj));
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        wr.write(Helper.deepSerialize(transferFileObj));
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();
        System.out.println("Response code" + responseCode);
        Map<String, Object> taskMap = getResponse(connection);//new ObjectMapper().readValue(connection.getInputStream(), HashMap.class);
        String taskId = (String) taskMap.get("task_id");//getVal(task_json,"task_id");
        connection.disconnect();

        //Section 3: Check the status of the transfer (not present in asynchronous mode)
        String getTaskStatusUrl = "/task/" + taskId;
        task = new URL(globusBaseUrl + getTaskStatusUrl);
        connection = (HttpURLConnection) task.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);
        connection.setRequestMethod("GET");

        String status = (String) getResponse(connection).get("status");

        while ("ACTIVE".equals(status)) {
            connection.disconnect();
            try {
                Thread.sleep(10000);
                System.out.println("Active Waiting");
            } catch (InterruptedException ex) {
                LOGGER.error("", ex);
            }
            connection = (HttpURLConnection) task.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);
            connection.setRequestMethod("GET");
            status = (String) getResponse(connection).get("status");
        }
        while ("INACTIVE".equals(status)) {
            connection.disconnect();
            try {
                Thread.sleep(10000);
                System.out.println("Inactive Waiting");
            } catch (InterruptedException ex) {
                LOGGER.error("", ex);
            }
            connection = (HttpURLConnection) task.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);
            connection.setRequestMethod("GET");
            status = (String) getResponse(connection).get("status");
        }
        if ("SUCCEEDED".equals(status)) {
            System.out.println("SUCCEEDED");
            storageFile.remove();
        } else {
            System.out.println("Failed");
        }

    }

    @Override
    public StorageFile transferFile(StorageFile sourceStorageFile, StorageFile targetStorageFile, boolean sync) throws IOException {
        // DO NOT IMPLEMENT
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StorageFile moveFile(StorageFile sourceStorageFile, StorageFile targetStorageFile, boolean sync) throws IOException {
        // DO NOT IMPLEMENT
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<StorageFile> listFile(String path, FileFilter filter) throws IOException {
        Map inputMap = Helper.deserialize(path, Map.class);
        String token = (String) inputMap.get("accessToken");
        String relativeDirectoryPath = (String) inputMap.get("relDirectoryPath");

        if (token == null) {
            throw new UnsupportedOperationException("Null Auth token");
        }

        String directoryListingUrl;

        if (inputMap.containsKey("endpoint")) {
            String pathEndpoint = (String) inputMap.get("endpoint");
            directoryListingUrl = "/endpoint/" + pathEndpoint.substring(0, globusEndpoint.indexOf('#')) + "%23" + pathEndpoint.substring(globusEndpoint.indexOf('#') + 1) + "/ls?path=";
        } else {
            directoryListingUrl = "/endpoint/" + globusEndpoint.substring(0, globusEndpoint.indexOf('#')) + "%23" + globusEndpoint.substring(globusEndpoint.indexOf('#') + 1) + "/ls?path=";
        }

        if (relativeDirectoryPath == null) {
            directoryListingUrl = directoryListingUrl + globusRoot;
        } else if (inputMap.containsKey("endpoint")) {
            directoryListingUrl = directoryListingUrl + relativeDirectoryPath;
        } else {
            directoryListingUrl = directoryListingUrl + globusRoot + relativeDirectoryPath;
        }

        URL url = new URL(globusBaseUrl + directoryListingUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);
        connection.setRequestMethod("GET");

        Map dirListing = getResponse(connection);
        System.out.println(Helper.deepSerialize(dirListing));
        String basePath = (String) dirListing.get("path");
        System.out.println(basePath);
        List<Map> files = (List<Map>) dirListing.get("DATA");
        List<StorageFile> storageFiles = new ArrayList<>();

        for (Map file : files) {
            StorageFile storageFile = new StorageFile();
            System.out.println((String) file.get("name"));
            storageFile.setLocation(basePath + (String) file.get("name"));
            String fileName = (String) file.get("name");
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            storageFile.setFileName(fileName);
            //int size = fileListData1.get("size");
            storageFile.setDescription(Integer.toString((Integer) file.get("size")));
            storageFiles.add(storageFile);
        }

        return storageFiles;
    }

    private String getSubmissionId(String token) throws MalformedURLException, IOException {
        URL url = new URL(globusBaseUrl + "/submission_id");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);

        Map<String, Object> responseMap = getResponse(connection);
        connection.disconnect();
        String submissionId = (String) responseMap.get("value");

        return submissionId;
    }

    private int activateEndpoint(String endPoint, String token) throws MalformedURLException, IOException {
        String activate = "/endpoint/" + endPoint.replaceAll("#", "%23") + "/autoactivate";
        System.out.println(activate);
        URL url = new URL(globusBaseUrl + activate);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);
        connection.setDoOutput(false);
        connection.connect();
        int responseCode = connection.getResponseCode();
        connection.disconnect();

        return responseCode;
    }

    private HttpURLConnection makeStatusCheckConnection(String taskId, String token) throws MalformedURLException, IOException {
        String taskStatusUrl = "/task/" + taskId;
        URL url = new URL(globusBaseUrl + taskStatusUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);
        connection.setRequestMethod("GET");

        return connection;
    }

    private String getStatus(HttpURLConnection connection) throws IOException {
        String status = (String) getResponse(connection).get("status");
        return status;
    }

    private String initiateFileTransfer(String sourceEndpoint, List<String> sourceFilePaths, String destinationEndpoint, List<String> destinationFilePaths, String submissionId, String token) throws MalformedURLException, IOException {
        if (sourceFilePaths.size() != destinationFilePaths.size()) {
            throw new RuntimeException("Globus file transfer: source and destination have different number of files: " + sourceFilePaths.size() + ", " + destinationFilePaths.size());
        }

        List<Map> fileTransferObjects = new ArrayList<>();
        for (int i = 0; i < sourceFilePaths.size(); i++) {
            Map<String, Object> fileTransferObject = new HashMap<>();
            fileTransferObject.put("recursive", false);
            fileTransferObject.put("DATA_TYPE", "transfer_item");
            fileTransferObject.put("source_path", sourceFilePaths.get(i));
            fileTransferObject.put("destination_path", destinationFilePaths.get(i));

            fileTransferObjects.add(fileTransferObject);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("DATA_TYPE", "transfer");
        request.put("submission_id", submissionId);
        request.put("source_endpoint", sourceEndpoint);
        request.put("destination_endpoint", destinationEndpoint);
        request.put("sync_level", null);
        request.put("label", "example transfer label");
        request.put("DATA", fileTransferObjects);

        URL url = new URL(globusBaseUrl + "/transfer");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Globus-Goauthtoken " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        System.out.println(Helper.deepSerialize(request));
        try (BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            wr.write(Helper.deepSerialize(request));
            wr.flush();
        }

        int responseCode = connection.getResponseCode();
        Map<String, Object> responseMap = getResponse(connection);
        connection.disconnect();

        System.out.println("Response code: " + responseCode);
        String taskId = (String) responseMap.get("task_id");

        return taskId;
    }

    private Map<String, Object> getResponse(HttpURLConnection connection) throws IOException {
        Map<String, Object> response;

        int responseCode = connection.getResponseCode();
        System.out.println("response code: " + responseCode);

        if (responseCode >= 200 && responseCode < 300) {
            InputStream inputStream = connection.getInputStream();
            String input = IOUtils.toString(inputStream);
            response = Helper.deserialize(input, Map.class);
        } else {
            InputStream errorStream = connection.getErrorStream();
            String error = IOUtils.toString(errorStream);
            response = Helper.deserialize(error, Map.class);
        }

        return response;
    }

    private List<String> createSymbolicLinks(List<StorageFile> storageFiles) throws IOException {
        List<String> fileLinks = new ArrayList<>();
        for (StorageFile storageFile: storageFiles) {
            String dest = getAbsolutePath(storageFile);
            Path destPath = FileSystems.getDefault().getPath(dest);

            File destFile = destPath.toFile();
            if (!destFile.exists()) {
                destFile.getParentFile().mkdirs();
                destFile.createNewFile();
            }

            String location = storageFile.getLocation();
            Path destLink = FileSystems.getDefault().getPath("../../" + location);
            String link = getWorkareaDirectory() + GLOBUS + destLink.toFile().getName();
            Path linkPath = FileSystems.getDefault().getPath(link);

            Files.createSymbolicLink(linkPath, destLink);
            fileLinks.add(link);
        }

        return fileLinks;
    }

    private void removeSymbolicLinks(List<String> fileLinks) throws IOException {
        for (String fileLink : fileLinks) {
            Path linkPath = FileSystems.getDefault().getPath(fileLink);
            //if (Files.isSymbolicLink(linkPath)) {
                Files.delete(linkPath);
            //}
        }
    }

    private List<String> convertToGlobusPaths(List<String> filePaths) throws IOException {
        String fileRoot = storageService.getStorageRoot();
        int fileRootLength = fileRoot.length();

        List<String> globusPaths = new ArrayList<>();
        for (String filePath : filePaths) {
            String globusPath = this.globusRoot + filePath.substring(fileRootLength);
            globusPaths.add(globusPath);
        }
        return globusPaths;
    }
}
