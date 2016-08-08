/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.MultiPartFileContentBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jain117
 */
public class GlobusControllerTest extends BaseControllerTest{

    private final String WORKFLOW_FILES_DIR = "src/test/files/workflow_tests/";
    public static final String WORKFLOW_ZIP_FILE = "Globus_File_Transfer_Test.zip";
    private final String username = "jain117";
    private final String password = "Nitjai1991";
    private final String endpoint = "jain117#test";
    private static final String REMOTE_ENDPOINT = "remote_endpoint/";
    private static final String STORAGE_ROOT = "Y:/";
    private static final String FILE_TO_UPLOAD_1  = "barney.txt";
    private static final String FILE_TO_UPLOAD_2  = "fontpage.txt";
    private static final String FILE_TO_DOWNLOAD_1  = "0001_barney1.txt";
    private static final String GLOBUS_STORAGE_DIRECTORY  = "f47ac10b-58cc-4372-a567-0e02b2c3d479/0000/0000/0000/0000";
    private static final String GLOBUS_STORAGE_WORKEAERA_DIRECTORY = "workarea/globus";

    //@BeforeClass
    public static void setUpClass() throws Exception {
        File source1 = new File("./src/test/files/" + FILE_TO_UPLOAD_1);
        File source2 = new File("./src/test/files/" + FILE_TO_UPLOAD_2);
        File source3 = new File("./src/test/files/" + FILE_TO_DOWNLOAD_1);

        String remoteEndpointDir = STORAGE_ROOT + REMOTE_ENDPOINT;
        File destDir = new File(remoteEndpointDir);

        FileUtils.copyFileToDirectory(source1, destDir, true);
        FileUtils.copyFileToDirectory(source2, destDir, true);

        String storageDir = STORAGE_ROOT + GLOBUS_STORAGE_DIRECTORY;
        destDir = new File(storageDir);

        if (destDir.exists() && destDir.isDirectory()){
            FileUtils.copyFileToDirectory(source3, destDir, true);
        }

        FileUtils.cleanDirectory(new File(STORAGE_ROOT + GLOBUS_STORAGE_WORKEAERA_DIRECTORY));
    }

    @Test
    @Ignore
    public void shouldBeAbleToRunWorkflowWithGlobusTransfer() throws Exception{

        useTestWorkspace("brouder_sylvie");
        login("george.washington", "1234");

        /*
         * upload the test workflow
         */
        MockMultipartFile mockMultipartFile = new MockMultipartFile(WORKFLOW_ZIP_FILE, new FileInputStream(WORKFLOW_FILES_DIR + WORKFLOW_ZIP_FILE));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/workflows/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isCreated());

        String content = extractTextarea(resultActions.andReturn().getResponse().getContentAsString());
        Map<String, Object> workflow = Helper.deserialize(content, Map.class);
        assertNotNull("workflow is null", workflow);
        Integer workflowId = (Integer) workflow.get("id");

        /*
         * create a project and an experiment to associate the job for the workflow with
         * while doing that, make sure we save all the IDs associated to post it with the job
         */
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects").content("{\"description\":\"This is a project\",\"name\":\"Project 1\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        content = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = Helper.deserialize(content, Map.class);
        Integer projectId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = post("/experiments").content("{\"projectId\":{\"$ref\":\"/projects/" + projectId + "\"},\"name\":\"Experiment 1\",\"description\":\"This is an experiment\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        content = resultActions.andReturn().getResponse().getContentAsString();
        map = Helper.deserialize(content, Map.class);
        Integer experimentId = (Integer) map.get("id");

        /*
         * create a job associated with the project, experiment and workflow we just created
         */
        mockHttpServletRequestBuilder = post("/jobs").param("projectId", projectId.toString()).param("experimentId", experimentId.toString()).param("workflowId", workflowId.toString()).param("name", "Just a job").accept(MediaType.TEXT_HTML).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        /*
         * forwarded to job/submit/jobId
         */
        String forwardedUrl = resultActions.andReturn().getResponse().getForwardedUrl();
        mockHttpServletRequestBuilder = post(forwardedUrl).accept(MediaType.TEXT_HTML_VALUE).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /*
         * redirected to jobs/task/jobId
         */
        String redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();
        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /*
         * we're at UT1 in the workflow
         */
        String jobId = redirectedUrl.substring(redirectedUrl.lastIndexOf('/') + 1);
        TaskEntity task = (TaskEntity) resultActions.andReturn().getModelAndView().getModel().get("task");
        String taskId = task.getId();


        String templateId = "305b0f27-e829-424e-84eb-7a8a9ed93e28";
        String templateVersion = "db719406-f665-45cb-a8fb-985b6082b654";

        // For buttton 1
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/globus/browseFile");
        uriBuilder.queryParam("jobId", jobId);
        uriBuilder.queryParam("alias", templateId + ".browsefile1");
        uriBuilder.queryParam("multiple", false);

        System.out.println( uriBuilder.build(true).toUriString() );

        mockHttpServletRequestBuilder = get(uriBuilder.build(true).toUriString()).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        redirectedUrl = resultActions.andReturn().getResponse().getContentAsString();

        System.out.println("Redirected to: " + redirectedUrl);

        uriBuilder = UriComponentsBuilder.fromUriString("https://www.globus.org/service/graph/goauth/authorize");
        uriBuilder.queryParam("response_type", "code");
        //uriBuilder.queryParam("redirect_uri", "code");
        uriBuilder.queryParam("client_id", username);

        URL url = new URL(uriBuilder.build(true).toUriString());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        String userpass = username + ":" + password;
        String basicAuth = "Basic " + new String(Base64.encodeBase64(userpass.getBytes()));
        connection.setRequestProperty("Authorization", basicAuth);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        String res = IOUtils.toString(connection.getInputStream());
        Map<String, Object> responseMap = Helper.deserialize(res, Map.class);
        String code = (String) responseMap.get("code");

        uriBuilder = UriComponentsBuilder.fromUriString("/globus/loginCallback");
        uriBuilder.queryParam("jobId", Integer.parseInt(jobId));
        uriBuilder.queryParam("alias", templateId + ".browsefile1");
        uriBuilder.queryParam("multiple", false);

        String uri = uriBuilder.build(true).toUriString() + "&code=" + code;
        mockHttpServletRequestBuilder = get(uri).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().is3xxRedirection());

        redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        System.out.println("Redirected to: " + redirectedUrl);

        // For Button 2
        uriBuilder = UriComponentsBuilder.fromUriString("/globus/browseFile");
        uriBuilder.queryParam("jobId", jobId);
        uriBuilder.queryParam("alias", templateId + ".browsefile2");
        uriBuilder.queryParam("multiple", true);

        System.out.println( uriBuilder.build(true).toUriString() );

        mockHttpServletRequestBuilder = get(uriBuilder.build(true).toUriString()).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        redirectedUrl = resultActions.andReturn().getResponse().getContentAsString();

        System.out.println("Redirected to: " + redirectedUrl);

        uriBuilder = UriComponentsBuilder.fromUriString("https://www.globus.org/service/graph/goauth/authorize");
        uriBuilder.queryParam("response_type", "code");
        uriBuilder.queryParam("client_id", username);

        url = new URL(uriBuilder.build(true).toUriString());
        connection = (HttpsURLConnection) url.openConnection();
        userpass = username + ":" + password;
        basicAuth = "Basic " + new String(Base64.encodeBase64(userpass.getBytes()));
        connection.setRequestProperty("Authorization", basicAuth);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        res = IOUtils.toString(connection.getInputStream());
        responseMap = Helper.deserialize(res, Map.class);
        code = (String) responseMap.get("code");

        // For button 2
        uriBuilder = UriComponentsBuilder.fromUriString("/globus/loginCallback");
        uriBuilder.queryParam("jobId", Integer.parseInt(jobId));
        uriBuilder.queryParam("alias", templateId + ".browsefile2");
        uriBuilder.queryParam("multiple", true);

        uri = uriBuilder.build(true).toUriString() + "&code=" + code;
        mockHttpServletRequestBuilder = get(uri).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().is3xxRedirection());

        redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        System.out.println("Redirected to: " + redirectedUrl);

        // Getting accessToken only from one button
        String accessToken = "";
        String[] urlParts = redirectedUrl.split("&");
        for (String urlPart : urlParts){
            if (urlPart.contains("accessToken")){
                String[] accessTokenPair = urlPart.split("=");
                accessToken = accessTokenPair[1];
                break;
            }
        }

        //Button 1
        uriBuilder = UriComponentsBuilder.fromUriString("/globus/fileSelectCallback");
        uriBuilder.queryParam(URLEncoder.encode("file[0]", "UTF-8"), FILE_TO_UPLOAD_1);

        uriBuilder.queryParam("jobId", jobId);
        uriBuilder.queryParam("alias", templateId + ".browsefile1");
        uriBuilder.queryParam("accessToken", accessToken);//URLEncoder.encode(accessToken,"UTF-8")
        uriBuilder.queryParam("path", URLEncoder.encode("/~/remote_endpoint/", "UTF-8"));

        uri = uriBuilder.build(true).toUriString();
        uri = URLDecoder.decode(uri);
        uri = uri + "&endpoint=" + URLEncoder.encode(endpoint, "UTF-8");
        mockHttpServletRequestBuilder = get(uri).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        //Button 2
        uriBuilder = UriComponentsBuilder.fromUriString("/globus/fileSelectCallback");
        uriBuilder.queryParam(URLEncoder.encode("file[0]", "UTF-8"), FILE_TO_UPLOAD_1);
        uriBuilder.queryParam(URLEncoder.encode("file[1]", "UTF-8"), FILE_TO_UPLOAD_2);

        uriBuilder.queryParam("jobId", jobId);
        uriBuilder.queryParam("alias", templateId + ".browsefile2");
        uriBuilder.queryParam("accessToken", accessToken);//URLEncoder.encode(accessToken,"UTF-8")
        uriBuilder.queryParam("path", URLEncoder.encode("/~/remote_endpoint/", "UTF-8"));

        uri = uriBuilder.build(true).toUriString();
        uri = URLDecoder.decode(uri);
        uri = uri + "&endpoint=" + URLEncoder.encode(endpoint, "UTF-8");
        mockHttpServletRequestBuilder = get(uri).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        //For getting Storage Files (an abstract button called browsefile3)
        uriBuilder = UriComponentsBuilder.fromUriString("/globus/browseFile");
        uriBuilder.queryParam("jobId", jobId);
        uriBuilder.queryParam("alias", templateId + ".browsefile3");
        uriBuilder.queryParam("multiple", true);
        uriBuilder.queryParam("storageFile", "StorageFile:1");// This file has to be present in the storage file record and in memory

        System.out.println( uriBuilder.build(true).toUriString() );

        mockHttpServletRequestBuilder = get(uriBuilder.build(true).toUriString()).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        redirectedUrl = resultActions.andReturn().getResponse().getContentAsString();

        System.out.println("Redirected to: " + redirectedUrl);

        //FileSelect
        uriBuilder = UriComponentsBuilder.fromUriString("/globus/fileSelectCallback");
        uriBuilder.queryParam("fileId", 1);
        uriBuilder.queryParam(URLEncoder.encode("folder[0]", "UTF-8"), "remote_endpoint/");

        uriBuilder.queryParam("jobId", jobId);
        uriBuilder.queryParam("alias", templateId + ".browsefile3");
        uriBuilder.queryParam("accessToken", accessToken);//URLEncoder.encode(accessToken,"UTF-8")
        uriBuilder.queryParam("path", URLEncoder.encode("/~/", "UTF-8"));

        uri = uriBuilder.build(true).toUriString();
        uri = URLDecoder.decode(uri, "UTF-8");
        uri = uri + "&endpoint=" + URLEncoder.encode(endpoint, "UTF-8");
        mockHttpServletRequestBuilder = get(uri).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        String multipartBoundary = "------WebKitFormBoundary3xeGH8uP6GWtBfd1";

        MultiPartFileContentBuilder multiPartFileContentBuilder = new MultiPartFileContentBuilder(multipartBoundary);
        multiPartFileContentBuilder.addField("autoGenerated", "true");
        multiPartFileContentBuilder.addField("jobId", jobId);
        multiPartFileContentBuilder.addField("taskId", taskId);
        multiPartFileContentBuilder.addField("jsonToServer", "{}");
        multiPartFileContentBuilder.addField("isIframe", "true");
        multiPartFileContentBuilder.addField("experimentId", "");
        multiPartFileContentBuilder.addField("projectId", "");
        multiPartFileContentBuilder.addField(templateId + ".name({%22_template_version:%22" + templateVersion + "%22})", "");
        multiPartFileContentBuilder.addField(templateId + ".browsefile1({%22_template_version:%22" + templateVersion + "%22})", "");
        multiPartFileContentBuilder.addField(templateId + ".browsefile2({%22_template_version:%22" + templateVersion + "%22})", "");
        String taskContent = multiPartFileContentBuilder.build();

        // /rest/objectus post call
        mockHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/rest/objectus/").param("jobId", jobId).param("taskId", taskId).param(templateId + ".name", "").param(templateId + ".browsefile1", "").param(templateId + ".browsefile2", "").param("jsonToServer", "{}").accept(MediaType.ALL).session(httpSession);
        mockHttpServletRequestBuilder.content(taskContent);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());


        multipartBoundary = "------WebKitFormBoundarybiQtLhfKnPwaMgsR";
        multiPartFileContentBuilder = new MultiPartFileContentBuilder(multipartBoundary);
        multiPartFileContentBuilder.addField("jobId", jobId);
        multiPartFileContentBuilder.addField("taskId", taskId);
        multiPartFileContentBuilder.addField("jsonToServer", "{}");
        taskContent = multiPartFileContentBuilder.build();

        // /jobs/task post call
        mockHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/jobs/task").param("jobId", jobId).param("taskId", taskId).param("ignoreFormData", "true").param("jsonToServer", "{}").accept(MediaType.ALL).session(httpSession);
        mockHttpServletRequestBuilder.content(taskContent);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().is3xxRedirection());

        redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        System.out.println("Redirected to: " + redirectedUrl);

        deleteDatasetEntries(templateId);
    }

    // TODO: to make it better
    private void deleteDatasetEntries(String templateId) throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/rest/objectus/"+templateId).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String result1 = resultActions.andReturn().getResponse().getContentAsString();
        resultActions.andExpect(status().isOk());

        String[] parts = result1.split("\\s+");
        List<String> objectIds = new ArrayList<>();
        if(Arrays.asList(parts).contains("\"$oid\"")){
            for(int i = 0; i < parts.length; i++){
                if (parts[i].equals("\"$oid\"")){
                    objectIds.add(parts[i + 2].substring(1,parts[i + 2].indexOf('}') - 1));
                }
            }
        }

        for(String objectId : objectIds){
            mockHttpServletRequestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/rest/objectus/" + templateId + "/" +objectId).accept(MediaType.APPLICATION_JSON).session(httpSession);
            resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
            Map<String, Object> result = (Map<String, Object>) DatasetUtils.deserialize((String) resultActions.andReturn().getResponse().getContentAsString());
            if(!((boolean) result.get("isValid"))){
                throw new RuntimeException("Can't delete Dataset Entries");
            }
            resultActions.andExpect(status().isOk());
        }
    }

    private void useTestWorkspace(String workspace) throws Exception {
        /*Due to problems with @Transactional and Hibernate not being able to reference GroupUsers from dynamically created Workspaces, an existing workspace
        is used as a boiler plate to run the test. Since the test is transactional and the state is not committed, no changes are effected at the end of the test*/
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/" + workspace).session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));
    }

    private String extractTextarea(String responseText) {
        if (StringUtils.isEmpty(responseText)) {
            return "";
        }

        int startIndex = responseText.indexOf("<textarea>");
        int endIndex = responseText.lastIndexOf("</textarea>");

        if (startIndex == -1 || endIndex == -1) {
            return "";
        }

        String textarea = responseText.substring(startIndex + 10, endIndex);

        return textarea;
    }

}
