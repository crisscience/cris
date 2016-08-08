/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.util.Helper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author xu222
 * @author jain117
 */
@Ignore
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class StorageServiceIntegrationTest {
    private static final String NexusApi = "https://nexus.api.globusonline.org/goauth/token?grant_type=client_credentials";
    private static final String UserEndpoint = "jain117#test";
    private static final String fileStorageRoot = "Y:\\";

    private static final String REMOTE_ENDPOINT = "remote_endpoint/";

    @Autowired
    private StorageService storageService;

    private StorageFileManager globusStorageFileManager;
    @Autowired
    public void setGlobusStorageService(StorageService storageService) throws IOException {
        globusStorageFileManager = storageService.getStorageFileManager(AccessMethodType.GLOBUS);
    }

    private StorageFileManager fileStorageFileManager;
    @Autowired
    public void setFileStorageService(StorageService storageService) throws IOException {
        fileStorageFileManager = storageService.getStorageFileManager(AccessMethodType.FILE);
    }

    private String getAuthToken() throws IOException{
        URL url = new URL(NexusApi);
        URLConnection connection = url.openConnection();
        String basicAuth = "Basic amFpbjExNzpOaXRqYWkxOTkx";//"Basic " + new String(new Base64().encodeBase64(userpass.getBytes()));
        connection.setRequestProperty ("Authorization", basicAuth);

        HashMap<String,Object> tokenMap = Helper.deserialize(IOUtils.toString(connection.getInputStream()),HashMap.class);
        return (String)tokenMap.get("access_token");
    }

    /*
    private boolean doesAllFilesExistAtDestination(String authToken, List<String> fileList,String relDirectoryPath) throws IOException{
        HashMap inputObj = new HashMap();
        inputObj.put("authToken", authToken);
        inputObj.put("relDirectoryPath", relDirectoryPath);

        List<StorageFile> fileListing = storageFileManager.listFile(Helper.deepSerialize(inputObj), null);
        List<String> fileNames = new ArrayList<String>();
        for (StorageFile fileListing1 : fileListing) {
            fileNames.add(fileListing1.getFileName());
        }
        boolean doesFilesExistAtDestination = true;
        for (String fileList1 : fileList) {
            if (!fileNames.contains(fileList1.substring(fileList1.lastIndexOf('/')+1))) {
                doesFilesExistAtDestination = false;
                break;
            }
        }
        return doesFilesExistAtDestination;
     }

     private boolean doesTransferredFileSizeMatch(String authToken, List<StorageFile> storageFileList, String endpoint, List<String> fileList){
        List<String> directoriesToBeListed = new ArrayList<String>();

        for(StorageFile storageFile:storageFileList){
            String crisDirectory = storageFile.getLocation().substring(storageFile.getLocation().getLastIndexOf('/'));
            if(!directoriesToBeListed.contains(crisDirectory)){
                directoriesToBeListed.add(crisDirectory);
            }

        }
        StorageFile storageFile = storageFileList.get(0);
        String crisDirectoryToBeListed = storageFile.getLocation().substring(storageFile.getLocation().getLastIndexOf('/'));
        HashMap inputObj = new HashMap();
        inputObj.put("authToken", authToken);
        inputObj.put("relDirectoryPath", crisDirectoryToBeListed);

        List<StorageFile> crisStorageFileList = storageFileManager.listFile(Helper.deepSerialize(inputObj), null);

        //inputObj = new HashMap();
        //inputObj.put("authToken", authToken);
        inputObj.put("endpoint", UserEndpoint);
        inputObj.put("relDirectoryPath", fileList.get(0).substring(0,fileList.get(0).getLastIndexOf('/')));

        List<StorageFile> userStorageFileList = storageFileManager.listFile(Helper.deepSerialize(inputObj), null);

     }*/

    private boolean doesTransferredFileSizeMatch(String authToken, List<StorageFile> transferredStorageList, List<String> filesToTransfer ) throws IOException{

        // Assuming all files are transferred from same directory
        //TODO: Get all the file directories to be listed and proceed with file list calls for each unique directory

        String relDirectoryPath = filesToTransfer.get(0).substring(0,filesToTransfer.get(0).lastIndexOf('/'));

        HashMap inputObj = new HashMap();
        inputObj.put("authToken", authToken);
        inputObj.put("relDirectoryPath", relDirectoryPath);
        inputObj.put("endpoint", UserEndpoint);

        List<StorageFile> fileListing = globusStorageFileManager.listFile(Helper.deepSerialize(inputObj), null);

        List<String> fileNamesToTransfer = new ArrayList<>();
        for (String file : filesToTransfer){
            fileNamesToTransfer.add(file.substring(file.lastIndexOf('/') + 1));
        }

        for(StorageFile file: fileListing){
            if (fileNamesToTransfer.contains(file.getFileName())){
                //fileListing.remove(file);
                for (StorageFile trasferredFile : transferredStorageList){
                    if(trasferredFile.getFileName().equals(file.getFileName())){
                        String fileLocation = trasferredFile.getLocation().substring(0,trasferredFile.getLocation().lastIndexOf('/')+1) + trasferredFile.getFileName();
                        File f = new File(fileStorageRoot + fileLocation);
                        long fileSize = 0;
                        if(f.exists())
                            fileSize = (long) f.length();
                        if (fileSize != Integer.parseInt(file.getDescription())) return false;
                        break;
                    }
                }
            }
        }
        return true;
    }

    private boolean fileTransferCheck(List<String> filePaths, List<String> transferredFilePaths){

        int i = 0;
        if (filePaths.size() != transferredFilePaths.size()){
            return false;
        }
        for (String filePath : filePaths){
            File originalFile = new File(filePath);
            File transferredFile = new File(transferredFilePaths.get(i));
            i++;

            if (originalFile.exists() && transferredFile.exists()){
                if (originalFile.length() == transferredFile.length()){
                    return true;
                }
            }
            else{
                break;
            }
        }
        return false;
    }
    //@Ignore
    @Test
    public void testPutMethodOneFile() throws IOException,AssertionError, InterruptedException {
        final String FILE_TO_TRANSFER = "globus_connect_install_latest(1).exe";

        // copy a file to the test endpoint
        String remoteEndpointDir = storageService.getStorageRoot() + REMOTE_ENDPOINT;
        File source = new File("./src/test/resources/Globus/globus_connect_install_latest(1).exe");
        File destDir = new File(remoteEndpointDir);
        FileUtils.copyFileToDirectory(source, destDir, true);

        List<String> filesToTransfer = new ArrayList<>();
        filesToTransfer.add( REMOTE_ENDPOINT + source.getName());

        String authToken = getAuthToken();
        Assert.assertNotNull("Auth token is Null", authToken);

        Map inputObj = new HashMap();
        inputObj.put("accessToken", authToken);
        inputObj.put("sourceEndpoint", UserEndpoint);
        inputObj.put("filePaths", filesToTransfer);

        List<StorageFile> transferredStorageList = globusStorageFileManager.putFile(Helper.deepSerialize(inputObj), null, true);

        Assert.assertEquals("Number of files returned doesn't match files to be transferred", filesToTransfer.size(), transferredStorageList.size());
        Assert.assertEquals("Different File transferred",filesToTransfer.get(0).substring(filesToTransfer.get(0).lastIndexOf('/')+1),transferredStorageList.get(0).getFileName());

        List<String> globusSourceFileList = new ArrayList<>();
        List<String> globusDestFileList = new ArrayList<>();
        globusSourceFileList.add(remoteEndpointDir + FILE_TO_TRANSFER);
        globusDestFileList.add(storageService.getStorageRoot() + transferredStorageList.get(0).getLocation());

        Assert.assertTrue("Transfer Invalid", fileTransferCheck(globusSourceFileList, globusDestFileList));
        //Assert.assertTrue("Transferred File sizes don't match",doesTransferredFileSizeMatch(authToken,transferredStorageList,filesToTransfer));

        for (StorageFile storageFile : transferredStorageList){
            storageFile.setName(authToken);
            globusStorageFileManager.deleteFile(storageFile);
        }

        // Deleting the transferred file on remote endpoint.
        Files.delete(new File(remoteEndpointDir + FILE_TO_TRANSFER).toPath());
    }
    //@Ignore
    @Test
    public void testPutMethodTwoFile() throws IOException,AssertionError {
        final String FILE_TO_TRANSFER_1 = "globus_connect_install_latest(1).exe";
        final String FILE_TO_TRANSFER_2 = "putty.exe";

        // copy a file to the test endpoint
        String remoteEndpointDir = storageService.getStorageRoot() + REMOTE_ENDPOINT;
        File source1 = new File("./src/test/resources/Globus/globus_connect_install_latest(1).exe");
        File source2 = new File("./src/test/resources/Globus/putty.exe");
        File destDir = new File(remoteEndpointDir);
        FileUtils.copyFileToDirectory(source1, destDir, true);
        FileUtils.copyFileToDirectory(source2, destDir, true);

        List<String> filesToTransfer = new ArrayList<String>();
        filesToTransfer.add( REMOTE_ENDPOINT + source1.getName());
        filesToTransfer.add( REMOTE_ENDPOINT + source2.getName());

        String authToken = getAuthToken();
        Assert.assertNotNull("Auth token is Null", authToken);

        Map inputObj = new HashMap();
        inputObj.put("accessToken", authToken);
        inputObj.put("sourceEndpoint", UserEndpoint);
        inputObj.put("filePaths", filesToTransfer);

        List<StorageFile> transferredStorageList = globusStorageFileManager.putFile(Helper.deepSerialize(inputObj), null, true);

        Assert.assertEquals("Number of files returned doesn't match files to be transferred", filesToTransfer.size(), transferredStorageList.size());
        Assert.assertEquals("Different File transferred",filesToTransfer.get(0).substring(filesToTransfer.get(0).lastIndexOf('/')+1),transferredStorageList.get(0).getFileName());

        //Assert.assertTrue("Transfer Invalid", fileTransferCheck(remoteEndpointDir + source.getName(), storageService.getStorageRoot() + transferredStorageList.get(0).getLocation()));
        //Assert.assertTrue("Transferred File sizes don't match",doesTransferredFileSizeMatch(authToken,transferredStorageList,filesToTransfer));

        List<String> globusSourceFileList = new ArrayList<>();
        List<String> globusDestFileList = new ArrayList<>();
        globusSourceFileList.add(remoteEndpointDir + FILE_TO_TRANSFER_1);
        globusSourceFileList.add(remoteEndpointDir + FILE_TO_TRANSFER_1);
        globusDestFileList.add(storageService.getStorageRoot() + transferredStorageList.get(0).getLocation());
        globusDestFileList.add(storageService.getStorageRoot() + transferredStorageList.get(1).getLocation());

        Assert.assertTrue("Transferred File sizes don't match", fileTransferCheck(globusSourceFileList, globusDestFileList));


        for (StorageFile storageFile : transferredStorageList){
            storageFile.setName(authToken);
            globusStorageFileManager.deleteFile(storageFile);
        }

        // Deleting the transferred file on remote endpoint.
        Files.delete(new File(remoteEndpointDir + FILE_TO_TRANSFER_1).toPath());
        Files.delete(new File(remoteEndpointDir + FILE_TO_TRANSFER_2).toPath());
    }
    @Ignore
    @Test
    public void testPutMethodDirectory() throws IOException,AssertionError {

        List<String> directoriesToTransfer = new ArrayList<String>();

        File file = new File("./src/test/resources/Globus/data/test.txt");
        String absolutePath = file.getAbsoluteFile().getParentFile().getAbsolutePath();
        absolutePath = "/"+absolutePath.replace("\\", "/");
        absolutePath = absolutePath.replace(":","");
        directoriesToTransfer.add(absolutePath);
        String authToken = getAuthToken();

        Assert.assertNotNull("Auth token is Null", authToken);
        //Assert.assertFalse("All files already present", doesAllFilesExistAtDestination(authToken,directoriesToTransfer,null));

        Map inputObj = new HashMap();
        inputObj.put("accessToken", getAuthToken());
        inputObj.put("sourceEndpoint", UserEndpoint);
        inputObj.put("directoryPaths", directoriesToTransfer);

        List<StorageFile> transferredStorageList =  globusStorageFileManager.putFile(Helper.deepSerialize(inputObj), null, true);

        Assert.assertEquals("Number of directories returned doesn't match files to be transferred", directoriesToTransfer.size(), transferredStorageList.size());
        //for (int i = 0; i< directoriesToTransfer.size();i++)
            //Assert.assertEquals("Different File transferred",directoriesToTransfer.get(i).substring(directoriesToTransfer.get(i).lastIndexOf('/')),transferredStorageList.get(i).getFileName().substring(transferredStorageList.get(i).getFileName().lastIndexOf('/')));

        //Assert.assertTrue("All files not transferred",doesAllFilesExistAtDestination(authToken,directoriesToTransfer,null));

        try {
            Thread.sleep(30000);
        } catch (InterruptedException ex) {
            Logger.getLogger(StorageServiceIntegrationTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (StorageFile storageFile:transferredStorageList){
            storageFile.setName(authToken);
            globusStorageFileManager.deleteFile(storageFile);
        }
    }
    //@Ignore
    @Test
    public void testGetMethod() throws IOException {
        //First transfer to cris endpoint
        final String FILE_TO_TRANSFER = "globus_connect_install_latest(1).exe";

        // copy a file to the test endpoint
        String remoteEndpointDir = storageService.getStorageRoot() + REMOTE_ENDPOINT;
        File source = new File("./src/test/resources/Globus/globus_connect_install_latest(1).exe");
        File destDir = new File(remoteEndpointDir);
        FileUtils.copyFileToDirectory(source, destDir, true);

        List<String> filesToTransfer = new ArrayList<>();
        filesToTransfer.add( REMOTE_ENDPOINT + source.getName());

        String authToken = getAuthToken();
        Assert.assertNotNull("Auth token is Null", authToken);

        Map inputObj = new HashMap();
        inputObj.put("accessToken", authToken);
        inputObj.put("sourceEndpoint", UserEndpoint);
        inputObj.put("filePaths", filesToTransfer);

        List<StorageFile> transferredStorageList = globusStorageFileManager.putFile(Helper.deepSerialize(inputObj), null, true);

        Assert.assertEquals("Number of files returned doesn't match files to be transferred", filesToTransfer.size(), transferredStorageList.size());
        //Assert.assertEquals("Different File transferred",filesToTransfer.get(0).substring(filesToTransfer.get(0).lastIndexOf('/')+1),transferredStorageList.get(0).getFileName());
        Assert.assertEquals("Different File transferred", FILE_TO_TRANSFER,transferredStorageList.get(0).getFileName());

        List<String> globusSourceFileList = new ArrayList<>();
        List<String> globusDestFileList = new ArrayList<>();
        globusSourceFileList.add(remoteEndpointDir + FILE_TO_TRANSFER);
        globusDestFileList.add(storageService.getStorageRoot() + transferredStorageList.get(0).getLocation());

        Assert.assertTrue("Transfer Invalid", fileTransferCheck(globusSourceFileList, globusDestFileList));

        Files.delete(new File(remoteEndpointDir + FILE_TO_TRANSFER).toPath());

        //Map inputObj = new HashMap();
        //inputObj.put("authToken", getAuthToken());
        inputObj.put("destinationEndpoint", UserEndpoint);
        inputObj.put("destinationFolder", REMOTE_ENDPOINT);

        globusStorageFileManager.getFile(transferredStorageList.get(0),Helper.deepSerialize(inputObj),true);

        Assert.assertTrue("Transfer Invalid", fileTransferCheck(globusDestFileList, globusSourceFileList));

        //Deletin temporary and transferred files
        for (StorageFile storageFile : transferredStorageList){
            storageFile.setName(authToken);
            globusStorageFileManager.deleteFile(storageFile);
        }

        Files.delete(new File(remoteEndpointDir + FILE_TO_TRANSFER).toPath());
        /*
        if (! "SUCCEEDED".equals())
            throw new RuntimeException("GetFile failed");
        */
    }
}
