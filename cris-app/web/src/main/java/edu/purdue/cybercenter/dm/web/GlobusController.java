package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.StorageService;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.util.Helper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@RequestMapping("/globus")
@Controller
public class GlobusController {
    private final String clientId = "jain117";
    private final String password = "Nitjai1991";

    private StorageFileManager storageFileManager;
    @Autowired
    public void setStorageService(StorageService storageService) throws IOException {
        storageFileManager = storageService.getStorageFileManager(AccessMethodType.GLOBUS);
    }

    @Autowired
    private DomainObjectService domainObjectService;

    @Autowired
    private RuntimeService runtimeService;

    @RequestMapping(value = "/browseFile", method = RequestMethod.GET)
    public void browseFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseUrl = getBaseUrl(request);
        String jobId = request.getParameter("jobId");
        String alias = request.getParameter("alias");
        if (jobId == null || jobId.isEmpty()) {
            jobId = "1000";
        }
        String storageFile = request.getParameter("storageFile");
        String fileId = "";
        if (storageFile != null && !storageFile.isEmpty()) {
            String[] parts = storageFile.split(":");
            if (parts.length != 2) {
                throw new RuntimeException("Invalid storage file format: " + storageFile);
            } else {
                fileId = parts[1];
            }
        }
        String multiple = request.getParameter("multiple");
        if (!"false".equals(multiple)) {
            multiple = "true";
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://www.globus.org/OAuth");
        uriBuilder.queryParam("response_type", "code");
        uriBuilder.queryParam("client_id", clientId);
        uriBuilder.queryParam("redirect_uri", URLEncoder.encode(baseUrl + "/globus/loginCallback?jobId=" + jobId + "&alias=" + alias + "&fileId=" + fileId + "&multiple=" + multiple, "UTF-8"));

        String redirectUrl = uriBuilder.build(true).toUriString();
        response.setContentType("text/plain");
        response.getWriter().println(redirectUrl);
    }

    @RequestMapping(value = "/loginCallback", method = RequestMethod.GET)
    public void loginCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // login and get access token
        String code = request.getParameter("code");
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://nexus.api.globusonline.org/goauth/token");
        uriBuilder.queryParam("grant_type", "authorization_code");
        //URLEncoder.encode(code, "UTF-8")
        uriBuilder.queryParam("code", URLEncoder.encode(code, "UTF-8"));
        URL url = new URL(uriBuilder.build(true).toUriString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String userpass = clientId + ":" + password; //Actual password
        String basicAuth = "Basic " + new String(Base64.encodeBase64(userpass.getBytes()));
        connection.setRequestProperty("Authorization", basicAuth);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        Map<String, Object> responseMap = Helper.deserialize(IOUtils.toString(connection.getInputStream()), Map.class);
        String accessToken = (String) responseMap.get("access_token");
        System.out.println(accessToken);

        // redirect to file selection
        String baseUrl = getBaseUrl(request);
        String jobId = request.getParameter("jobId");
        String alias = request.getParameter("alias");
        String fileId = request.getParameter("fileId");
        String multiple = request.getParameter("multiple");
        uriBuilder = UriComponentsBuilder.fromUriString("https://www.globus.org/xfer/BrowseEndpoint");
        if (fileId != null && !fileId.isEmpty()) {
            uriBuilder.queryParam("fileId", fileId);
            uriBuilder.queryParam("filelimit", "0");
            uriBuilder.queryParam("folderlimit", "1");
        } else {
            if ("false".equals(multiple)) {
                uriBuilder.queryParam("filelimit", "1");
            }
            uriBuilder.queryParam("folderlimit", "0");
        }
        uriBuilder.queryParam("action", URLEncoder.encode(baseUrl + "/globus/fileSelectCallback", "UTF-8"));
        uriBuilder.queryParam("accessToken", URLEncoder.encode(accessToken, "UTF-8"));
        uriBuilder.queryParam("jobId", jobId);
        uriBuilder.queryParam("alias", alias);
        response.sendRedirect(uriBuilder.build(true).toUriString());
    }

    @RequestMapping(value = "/fileSelectCallback", method = RequestMethod.GET)
    public void fileSelectCallback(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> params) throws IOException {
        System.out.println(Helper.deepSerialize(params));

        List<String> filesToTransfer = new ArrayList<>();
        for (int fileIdx = 0; params.containsKey("file[" + fileIdx + "]"); fileIdx++) {
            filesToTransfer.add(params.get("path") + params.get("file[" + fileIdx + "]"));
        }
        String transferToFolder = "";
        for (int index = 0; params.containsKey("folder[" + index + "]"); index++) {
            transferToFolder = params.get("path") + params.get("folder[" + index + "]");
        }

        response.setContentType("text/plain");
        if (!filesToTransfer.isEmpty() || !transferToFolder.isEmpty()) {
            String accessToken = params.get("accessToken");
            String jobId = params.get("jobId");
            String alias = params.get("alias");
            String endpoint = URLDecoder.decode(params.get("endpoint"));
            Map inputObj = new HashMap();
            inputObj.put("accessToken", accessToken);

            String fileId = params.get("fileId");
            if (fileId != null && !fileId.isEmpty()) {
                // get file out of cris
                inputObj.put("destinationEndpoint", endpoint);
                inputObj.put("destinationFolder", transferToFolder);

                StorageFile storageFile = domainObjectService.findById(Integer.parseInt(fileId), StorageFile.class);
                storageFileManager.getFile(storageFile, Helper.deepSerialize(inputObj), false);
            } else {
                // put file into cris
                inputObj.put("sourceEndpoint", endpoint);
                inputObj.put("filePaths", filesToTransfer);
                inputObj.put("alias", alias);

                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(jobId).singleResult();
                if (processInstance == null) {
                    // outside of a job
                    storageFileManager.putFile(Helper.deepSerialize(inputObj), null, false);
                } else {
                    // inside of a job
                    // Creating runtime variable to store the HashMap obj
                    // Transfer initiated once user moves from the current user task
                    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).singleResult();
                    runtimeService.setVariableLocal(execution.getId(), "inputMap" + alias, inputObj);

                    if (runtimeService.hasVariable(execution.getId(), "mapNames")) {
                        List<String> mapNames = (List<String>) runtimeService.getVariable(execution.getId(), "mapNames");
                        if (!mapNames.contains("inputMap" + alias)) {
                            mapNames.add("inputMap" + alias);
                            runtimeService.setVariableLocal(execution.getId(), "mapNames" , mapNames);
                        }
                    } else {
                        List<String> mapNames = new ArrayList<>();
                        mapNames.add("inputMap" + alias);
                        runtimeService.setVariableLocal(execution.getId(), "mapNames" , mapNames);
                    }
                }
            }
            response.getWriter().println("Recorded");
            response.getWriter().println(Helper.deepSerialize(filesToTransfer));
        } else {
            response.getWriter().println("No files selected to Transfer");
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort() + request.getContextPath();
        return baseUrl;
    }

}
