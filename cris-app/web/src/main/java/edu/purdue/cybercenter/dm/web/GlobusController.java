package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.StorageService;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.util.Helper;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final String redirectUrl = "/globus/loginCallback";
    private final String fileSelectCallbackUrl = "/globus/fileSelectCallback";
    private final String globusAuthUrl = "https://auth.globus.org/v2/oauth2/authorize";
    private final String globusTokenUrl = "https://auth.globus.org/v2/oauth2/token";
    private final String globusXferUrl = "https://www.globus.org/xfer/BrowseEndpoint";

    private StorageFileManager storageFileManager;
    @Autowired
    public void setStorageService(StorageService storageService) throws IOException {
        storageFileManager = storageService.getStorageFileManager(AccessMethodType.GLOBUS);
    }

    @Autowired
    private DomainObjectService domainObjectService;

    @RequestMapping(value = "/browseFile", method = RequestMethod.GET)
    public void browseFile(HttpServletRequest request, HttpServletResponse response) throws IOException, OAuthSystemException {
        String baseUrl = getBaseUrl(request);

        // store request parameters in session and they are needed later on
        String key = request.getParameter("key");
        String alias = request.getParameter("alias");
        String multiple = request.getParameter("multiple");
        String storageFile = request.getParameter("storageFile");
        Map<String, String> globus = new HashMap<>();
        globus.put("key", key);
        globus.put("alias", alias);
        globus.put("multiple", multiple);
        globus.put("fileId", StringUtils.isEmpty(storageFile) ? null : storageFile.split(":")[1]);
        request.getSession().setAttribute("globus", globus);

        OAuthClientRequest oauthRequest = OAuthClientRequest
                .authorizationLocation(globusAuthUrl)
                .setResponseType("code")
                .setClientId(clientId)
                .setRedirectURI(baseUrl + redirectUrl)
                .buildQueryMessage();
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().println(oauthRequest.getLocationUri());
    }

    @RequestMapping(value = "/loginCallback", method = RequestMethod.GET)
    public void loginCallback(HttpServletRequest request, HttpServletResponse response) throws IOException, OAuthSystemException, OAuthProblemException {
        // login and get access token
        String baseUrl = getBaseUrl(request);
        String code = request.getParameter("code");

        OAuthClientRequest oauthRequest = OAuthClientRequest
                .tokenLocation(globusTokenUrl)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectURI(baseUrl + redirectUrl)
                .setCode(code)
                .buildQueryMessage();

        OAuthClient oauthClient = new OAuthClient(new URLConnectionClient());
        OAuthJSONAccessTokenResponse oauthResponse = oauthClient.accessToken(oauthRequest);
        String accessToken = oauthResponse.getAccessToken();
        Long expiresIn = oauthResponse.getExpiresIn();
        System.out.println(accessToken + ": " + expiresIn);

        // put access token in session
        Map<String, String> globus = (Map) request.getSession().getAttribute("globus");
        globus.put("accessToken", accessToken);

        // redirect to file selection
        String key = globus.get("key");
        String alias = globus.get("alias");
        String multiple = globus.get("multiple");
        String fileId = globus.get("fileId");

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        if (StringUtils.isNotEmpty(fileId)) {
            uriBuilder.queryParam("fileId", fileId);
            uriBuilder.queryParam("filelimit", "0");
            uriBuilder.queryParam("folderlimit", "1");
        } else {
            if ("false".equals(multiple)) {
                uriBuilder.queryParam("filelimit", "1");
            }
            uriBuilder.queryParam("folderlimit", "0");
        }
        uriBuilder.queryParam("action", URLEncoder.encode(baseUrl + fileSelectCallbackUrl, "UTF-8"));
        uriBuilder.queryParam("accessToken", URLEncoder.encode(accessToken, "UTF-8"));
        uriBuilder.queryParam("key", key);
        uriBuilder.queryParam("alias", alias);

        OAuthClientRequest clientRequest = new OAuthBearerClientRequest(globusXferUrl + "?" + uriBuilder.build().getQuery())
                .setAccessToken(accessToken)
                .buildQueryMessage();

        response.sendRedirect(clientRequest.getLocationUri());
    }

    @RequestMapping(value = "/fileSelectCallback", method = RequestMethod.POST)
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

        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        if (!filesToTransfer.isEmpty() || !transferToFolder.isEmpty()) {
            Map<String, String> globus = (Map) request.getSession().getAttribute("globus");
            String accessToken = globus.get("accessToken");
            String key = globus.get("key");
            String alias = globus.get("alias");
            String fileId = globus.get("fileId");
            String endpoint = URLDecoder.decode(params.get("endpoint"), "UTF-8");

            Map inputObj = new HashMap();
            inputObj.put("accessToken", accessToken);
            if (StringUtils.isNotEmpty(fileId)) {
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
