package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.StorageService;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.util.Helper;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

@RequestMapping("/globus")
@Controller
public class GlobusController {
    //TODO: need a place to manage this
    private final String clientName = "CRIS Development (local)";
    private final String clientId = "747d66f2-fdb6-11e5-b59b-8c705ad34f60";
    private final String clientSecret = "UO/GwR3Ls9979i5v7a+otbPmUCjnitRpnbv8yVclk5MqJXV0sKGvmIawInfVhaET6Cz+ZU3Q6Q7JutxbmQdmOg==";

    private StorageFileManager storageFileManager;
    @Autowired
    public void setStorageService(StorageService storageService) throws IOException {
        storageFileManager = storageService.getStorageFileManager(AccessMethodType.GLOBUS);
    }

    @Autowired
    private DomainObjectService domainObjectService;

    @RequestMapping(value = "/browseFile", method = RequestMethod.GET)
    public void browseFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseUrl = getBaseUrl(request);
        String key = request.getParameter("key");
        String alias = request.getParameter("alias");
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

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://auth.globus.org/v2/oauth2/authorize");
        uriBuilder.queryParam("response_type", "code");
        uriBuilder.queryParam("client_id", clientId);
        //uriBuilder.queryParam("redirect_uri", URLEncoder.encode(baseUrl + "/globus/loginCallback?key=" + key + "&alias=" + alias + "&fileId=" + fileId + "&multiple=" + multiple, "UTF-8"));
        uriBuilder.queryParam("redirect_uri", URLEncoder.encode(baseUrl + "/globus/loginCallback", "UTF-8"));

        String redirectUrl = uriBuilder.build(true).toUriString();
        response.setContentType("text/plain");
        response.getWriter().println(redirectUrl);
    }

    @RequestMapping(value = "/loginCallback", method = RequestMethod.GET)
    public void loginCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // login and get access token
        String code = request.getParameter("code");
        String requestUrl = request.getRequestURL().toString();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://auth.globus.org/v2/oauth2/token");
        uriBuilder.queryParam("grant_type", "authorization_code");
        uriBuilder.queryParam("code", URLEncoder.encode(code, "UTF-8"));
        uriBuilder.queryParam("redirect_url", URLEncoder.encode(requestUrl, "UTF-8"));
        System.out.println(requestUrl);
        uriBuilder.queryParam("client_id", clientId);
        URL url = new URL(uriBuilder.build(true).toUriString());

        String userpass = clientId + ":" + clientSecret;
        String basicAuth = "Basic " + new String(Base64.encodeBase64(userpass.getBytes()));
        System.out.println(basicAuth);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(HttpMethod.POST.name());
        connection.setRequestProperty(HttpHeaders.AUTHORIZATION, basicAuth);
        connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        connection.setRequestProperty(HttpHeaders.ACCEPT, "application/json");
        System.out.println("****************");
        System.out.println(connection.getURL());
        System.out.println(connection.getRequestMethod());
        System.out.println(connection.getRequestProperties());
        try {
            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());
            System.out.println(IOUtils.toString(connection.getInputStream()));
        } catch (Exception ex) {
            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());
            System.out.println(IOUtils.toString(connection.getErrorStream()));
            throw ex;
        }
        Map<String, Object> responseMap = Helper.deserialize(IOUtils.toString(connection.getInputStream()), Map.class);
        String accessToken = (String) responseMap.get("access_token");
        System.out.println(accessToken);

        // redirect to file selection
        String baseUrl = getBaseUrl(request);
        String key = request.getParameter("key");
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
        uriBuilder.queryParam("key", key);
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
            String key = params.get("key");
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

                response.getWriter().println("File transfer initiated: file: " + storageFile.getFileName() + " will be transferred into folder: " + transferToFolder);
            } else {
                // put files into cris
                inputObj.put("sourceEndpoint", endpoint);
                inputObj.put("filePaths", filesToTransfer);
                inputObj.put("alias", alias);

                // put in the session
                Map<String, Object> globusFiles = (Map) request.getSession().getAttribute(key);
                if (globusFiles == null) {
                    globusFiles = new HashMap<>();
                    request.getSession().setAttribute(key, globusFiles);
                }
                globusFiles.put(alias, inputObj);

                response.getWriter().println("Recorded and the transfer will begin after you hit the save button");
                response.getWriter().println(Helper.deepSerialize(filesToTransfer));
            }
        } else {
            response.getWriter().println("No files selected to Transfer");
        }
    }

    @RequestMapping(value = "/getStorageFiles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object getStorageFiles(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> params) {
        String key = params.get("key");
        String alias = params.get("alias");
        Map<String, Object> templateFiles = (Map) request.getSession().getAttribute(key);
        Map<String, Object> termFiles;
        if (templateFiles != null) {
            termFiles = (Map) templateFiles.get(alias);
        } else {
            termFiles = null;
        }
        return termFiles != null ? termFiles.get("filePaths") : null;
    }

    private String getBaseUrl(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort() + request.getContextPath();
        return baseUrl;
    }

}
