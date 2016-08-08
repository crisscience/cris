package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.MultiPartFileContentBuilder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rangars
 */
public class WorkflowControllerTest extends BaseWithAdminUserControllerTest {

    private final String WORKFLOW_FILES_DIR = "src/test/files/workflow_tests/";
    private final String WORKFLOW_DATABASE_TEST_FILES_DIR = "src/test/files/workflow_tests/database_tests/";
    private final String WORKFLOW_JSON_IN_TEST_FILES_DIR = "src/test/files/workflow_tests/json_in_tests/";
    private final String WORKFLOW_JSON_OUT_TEST_FILES_DIR = "src/test/files/workflow_tests/json_out_tests/";

    @Test
    public void shouldBeAbleToUploadWorkflow() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("hplc_vocabulary.xml", new FileInputStream(WORKFLOW_FILES_DIR + "hplc_vocabulary.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_template.xml", new FileInputStream(WORKFLOW_FILES_DIR + "hplc_template.xml"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/templates/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_workflow.zip", new FileInputStream(WORKFLOW_FILES_DIR + "hplc_workflow.zip"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/workflows/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isCreated());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/workflows").accept(MediaType.APPLICATION_JSON).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Integer workflowId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                workflowId = (Integer) item.get("id");
            }
        }

        mockHttpServletRequestBuilder = get("/workflows/export/" + workflowId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        byte[] workflow = resultActions.andReturn().getResponse().getContentAsByteArray();
        InputStream is = new ByteArrayInputStream(workflow);
        try (FileOutputStream out = new FileOutputStream("download.zip")) {
            IOUtils.copy(is, out);
        }
        unzip("download.zip");
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(WORKFLOW_FILES_DIR, "hplc_workflow"), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        int fileCount = 0;
        while (fileIterator.hasNext()) {
            File downloadedFile;
            fileCount++;
            downloadedFile = fileIterator.next();
            String downloadedString = FileUtils.readFileToString(downloadedFile);
            String uploadedString = FileUtils.readFileToString(new File(WORKFLOW_FILES_DIR + downloadedFile.getName()));
                //TODO: to be fixed
            // assertTrue(downloadedString.equals(uploadedString));
        }
        assertEquals(5, fileCount);

        FileUtils.deleteDirectory(new File(WORKFLOW_FILES_DIR + "hplc_workflow"));
        File file = new File("download.zip");
        file.delete();
    }

    @Test
    public void shouldBeAbleToRunAWorkflow() throws Exception {
        /* upload the vocabulary and templates required for the workflow */
        MockMultipartFile mockMultipartFile = new MockMultipartFile("hplc_vocabulary.xml", new FileInputStream(WORKFLOW_FILES_DIR + "hplc_vocabulary.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_template.xml", new FileInputStream(WORKFLOW_FILES_DIR + "hplc_template.xml"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/templates/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_workflow.zip", new FileInputStream(WORKFLOW_FILES_DIR + "hplc_workflow.zip"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/workflows/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isCreated());

        /*
         * create a project and an experiment to associate the job for the workflow with
         * while doing that, make sure we save all the IDs associated to post it with the job
         */
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects").content("{\"description\":\"This is a project\",\"name\":\"Project 1\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = Helper.deserialize(content, Map.class);
        Integer projectId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = post("/experiments").content("{\"projectId\":{\"$ref\":\"/projects/" + projectId + "\"},\"name\":\"Experiment 1\",\"description\":\"This is an experiment\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        content = resultActions.andReturn().getResponse().getContentAsString();
        map = Helper.deserialize(content, Map.class);
        Integer experimentId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = get("/workflows").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        content = resultActions.andReturn().getResponse().getContentAsString();
        Integer workflowId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                workflowId = (Integer) item.get("id");
            }
        }

        /* create a job associated with the project, workflow and experiment we just created */
        mockHttpServletRequestBuilder = post("/jobs").param("projectId", projectId.toString()).param("experimentId", experimentId.toString()).param("workflowId", workflowId.toString()).param("name", "Just a job").accept(MediaType.TEXT_HTML).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        /*forwarded to job/submit/jobId*/
        String forwardedUrl = resultActions.andReturn().getResponse().getForwardedUrl();

        System.out.println("Creating job, forwarded to: " + forwardedUrl);

        mockHttpServletRequestBuilder = post(forwardedUrl).accept(MediaType.TEXT_HTML_VALUE).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /* redirected to jobs/task/jobId */
        String redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        System.out.println("Redirected to: " + redirectedUrl);

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        /* we're at UT1 in the workflow */
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /* we now get the task ID from it being set in the Task object on the model. we need this for posting the actual age and temp values */
        TaskEntity task = (TaskEntity) resultActions.andReturn().getModelAndView().getModel().get("task");
        String taskId = task.getId();

        /* match out the jobId from the redirected URL, it's the last number after the "/" */
        Pattern p = Pattern.compile("-?\\d+");
        Matcher matcher = p.matcher(redirectedUrl);
        String jobId = null;
        while (matcher.find()) {
            jobId = matcher.group();
        }

        //TODO: parse these from the template file
            /*
         * we build the data to post in the multi part request. sadly this request
         * will not get fielded by a commons resolver as it's disabled in our context.
         * it's disabled because when enabled, there is a problem with parsing attached
         * files. we suspect it might be an encoding error on our end (unlike the browser)
         */
        String templateId = "ae8bd3c0-73cf-11e2-bcfd-0800200c9a66";
        String templateVersion = "228fc1dacf-d278-4fd8-9357-46e837f00167";

        String multipartBoundary = "------WebKitFormBoundaryBXhFKdSKJOMAY6zw";
        MultiPartFileContentBuilder multiPartFileContentBuilder = new MultiPartFileContentBuilder(multipartBoundary);
        multiPartFileContentBuilder.addField(templateId + ".age({%22_template_version:%22" + templateVersion + "%22})", String.valueOf(20));
        multiPartFileContentBuilder.addField(templateId + ".temp({%22_template_version:%22" + templateVersion + "%22})", String.valueOf(20));
        multiPartFileContentBuilder.addField("id", jobId);
        multiPartFileContentBuilder.addField("taskId", taskId);
        multiPartFileContentBuilder.addField("jsonFromServer", "{}");
        multiPartFileContentBuilder.addField("jsonToServer", "{\"id\":\"" + jobId + "\",\"taskId\":\"" + taskId + "\"}");
        String taskContent = multiPartFileContentBuilder.build();

        /*
         * note that even though for correctness we built the "multipartrequest",
         * we're still attaching all the values are params to that spring can map
         * it to the controller without the commons resolver
         */
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/jobs/task").param("jobId", jobId).param("taskId", taskId).param(templateId + ".age", String.valueOf(20)).param(templateId + ".temp", String.valueOf(20)).accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.content(taskContent);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        /* the javascript probably failed and redirected us to the data import failure page */
        redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        /* going to the redirected failure page */
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /*
         * we posted a value which will cause the javascript to fail and hence cause
         * the exclusive gateway to go to the failed import page
         */
        assertTrue(resultActions.andReturn().getModelAndView().getModel().get("generatedHtml").toString().contains("<h3>Import failed</h3>"));
        task = (TaskEntity) resultActions.andReturn().getModelAndView().getModel().get("task");
        taskId = task.getId();

        /* starting to go back to UT1 */
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/jobs/task").param("jobId", jobId).param("taskId", taskId).accept(MediaType.ALL).session(httpSession);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        /* of course, we have to redirect ourselves there */
        redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        /* back at UT1 */
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        task = (TaskEntity) resultActions.andReturn().getModelAndView().getModel().get("task");
        taskId = task.getId();

        multiPartFileContentBuilder = new MultiPartFileContentBuilder(multipartBoundary);
        multiPartFileContentBuilder.addField(templateId + ".age({%22_template_version:%22" + templateVersion + "%22})", String.valueOf(30));
        multiPartFileContentBuilder.addField(templateId + ".temp({%22_template_version:%22" + templateVersion + "%22})", String.valueOf(20));
        multiPartFileContentBuilder.addField("id", jobId);
        multiPartFileContentBuilder.addField("taskId", taskId);
        multiPartFileContentBuilder.addField("jsonFromServer", "{}");
        multiPartFileContentBuilder.addField("jsonToServer", "{\"id\":\"" + jobId + "\",\"taskId\":\"" + taskId + "\"}");
        taskContent = multiPartFileContentBuilder.build();

        /* posting data to make the script pass and move to the final page */
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/jobs/task").param("jobId", jobId).param("taskId", taskId).param(templateId + ".age", String.valueOf(30)).param(templateId + ".temp", String.valueOf(20)).accept(MediaType.ALL).session(httpSession);

        mockMultipartHttpServletRequestBuilder.content(taskContent);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        /* the javascript hopefully succeeded and redirected us to the data import success page */
        redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        /* going to the redirected success page */
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /* success! */
        assertEquals("<h3>Import succeeded</h3>", resultActions.andReturn().getModelAndView().getModel().get("generatedHtml"));
        task = (TaskEntity) resultActions.andReturn().getModelAndView().getModel().get("task");
        taskId = task.getId();

        multiPartFileContentBuilder = new MultiPartFileContentBuilder(multipartBoundary);
        multiPartFileContentBuilder.addField("id", jobId);
        multiPartFileContentBuilder.addField("taskId", taskId);
        multiPartFileContentBuilder.addField("jsonFromServer", "{}");
        multiPartFileContentBuilder.addField("jsonToServer", "{\"id\":\"" + jobId + "\",\"taskId\":\"" + taskId + "\"}");
        taskContent = multiPartFileContentBuilder.build();

        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/jobs/task").param("jobId", jobId).param("taskId", taskId).accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.content(taskContent);

        /* wrapping up, moving on to the end task */
        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /* should get redirected back to the job summary */
        redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /* asserting that we go back to the index page after finishing */
        assertEquals("jobs/index", resultActions.andReturn().getModelAndView().getViewName());
    }

    @Test
    public void workflowShouldSaveInformationToDatabaseFromServiceTask() throws Exception {
        /* upload the vocabulary and templates required for the workflow */
        MockMultipartFile mockMultipartFile = new MockMultipartFile("hplc_vocabulary.xml", new FileInputStream(WORKFLOW_DATABASE_TEST_FILES_DIR + "hplc_vocabulary.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_template.xml", new FileInputStream(WORKFLOW_DATABASE_TEST_FILES_DIR + "hplc_template.xml"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/templates/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_workflow.zip", new FileInputStream(WORKFLOW_DATABASE_TEST_FILES_DIR + "hplc_workflow.zip"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/workflows/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isCreated());

        /*
         * create a project and an experiment to associate the job for the workflow with
         * while doing that, make sure we save all the IDs associated to post it with the job
         */
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects").content("{\"description\":\"This is a project\",\"name\":\"Project 1\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = Helper.deserialize(content, Map.class);
        Integer projectId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = post("/experiments").content("{\"projectId\":{\"$ref\":\"/projects/" + projectId + "\"},\"name\":\"Experiment 1\",\"description\":\"This is an experiment\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        content = resultActions.andReturn().getResponse().getContentAsString();
        map = Helper.deserialize(content, Map.class);
        Integer experimentId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = get("/workflows").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        content = resultActions.andReturn().getResponse().getContentAsString();
        Integer workflowId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                workflowId = (Integer) item.get("id");
            }
        }

        /* create a job associated with the project, workflow and experiment we just created */
        mockHttpServletRequestBuilder = post("/jobs").param("projectId", projectId.toString()).param("experimentId", experimentId.toString()).param("workflowId", workflowId.toString()).param("name", "Just a job").accept(MediaType.TEXT_HTML).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        /*forwarded to job/submit/jobId*/
        String forwardedUrl = resultActions.andReturn().getResponse().getForwardedUrl();

        System.out.println("Creating job, forwarded to: " + forwardedUrl);

        mockHttpServletRequestBuilder = post(forwardedUrl).accept(MediaType.TEXT_HTML_VALUE).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /* redirected to jobs/task/jobId */
        String redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        System.out.println("Redirected to: " + redirectedUrl);

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        /* we've run through all Service Tasks by now */
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        assertEquals("<h3>Import succeeded</h3>", resultActions.andReturn().getModelAndView().getModel().get("generatedHtml"));
    }

    @Test
    public void workflowJsonInTests() throws Exception {
        /* upload the vocabulary and templates required for the workflow */
        MockMultipartFile mockMultipartFile = new MockMultipartFile("hplc_vocabulary.xml", new FileInputStream(WORKFLOW_JSON_IN_TEST_FILES_DIR + "hplc_vocabulary.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_template.xml", new FileInputStream(WORKFLOW_JSON_IN_TEST_FILES_DIR + "hplc_template.xml"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/templates/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_workflow.zip", new FileInputStream(WORKFLOW_JSON_IN_TEST_FILES_DIR + "hplc_workflow.zip"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/workflows/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isCreated());

        /*
         * create a project and an experiment to associate the job for the workflow with
         * while doing that, make sure we save all the IDs associated to post it with the job
         */
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects").content("{\"description\":\"This is a project\",\"name\":\"Project 1\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = Helper.deserialize(content, Map.class);
        Integer projectId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = post("/experiments").content("{\"projectId\":{\"$ref\":\"/projects/" + projectId + "\"},\"name\":\"Experiment 1\",\"description\":\"This is an experiment\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        content = resultActions.andReturn().getResponse().getContentAsString();
        map = Helper.deserialize(content, Map.class);
        Integer experimentId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = get("/workflows").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        content = resultActions.andReturn().getResponse().getContentAsString();
        Integer workflowId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                workflowId = (Integer) item.get("id");
            }
        }

        /* create a job associated with the project, workflow and experiment we just created */
        mockHttpServletRequestBuilder = post("/jobs").param("projectId", projectId.toString()).param("experimentId", experimentId.toString()).param("workflowId", workflowId.toString()).param("name", "Just a job").accept(MediaType.TEXT_HTML).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        /*forwarded to job/submit/jobId*/
        String forwardedUrl = resultActions.andReturn().getResponse().getForwardedUrl();

        System.out.println("Creating job, forwarded to: " + forwardedUrl);

        mockHttpServletRequestBuilder = post(forwardedUrl).accept(MediaType.TEXT_HTML_VALUE).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /* redirected to jobs/task/jobId */
        String redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        System.out.println("Redirected to: " + redirectedUrl);

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        /* we've run through all service tasks now */
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        assertEquals("<h3>Import succeeded</h3>", resultActions.andReturn().getModelAndView().getModel().get("generatedHtml"));
    }

    //TODO: Need to fix TODOs in workflow associated with this test
    @Ignore
    @Test
    public void workflowJsonOutTests() throws Exception {
        /* upload the vocabulary and templates required for the workflow */
        MockMultipartFile mockMultipartFile = new MockMultipartFile("hplc_vocabulary.xml", new FileInputStream(WORKFLOW_JSON_OUT_TEST_FILES_DIR + "hplc_vocabulary.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_template.xml", new FileInputStream(WORKFLOW_JSON_OUT_TEST_FILES_DIR + "hplc_template.xml"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/templates/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("hplc_workflow.zip", new FileInputStream(WORKFLOW_JSON_OUT_TEST_FILES_DIR + "hplc_workflow.zip"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/workflows/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        /*
         * create a project and an experiment to associate the job for the workflow with
         * while doing that, make sure we save all the IDs associated to post it with the job
         */
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects").content("{\"description\":\"This is a project\",\"name\":\"Project 1\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = Helper.deserialize(content, Map.class);
        Integer projectId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = post("/experiments").content("{\"projectId\":{\"$ref\":\"/projects/" + projectId + "\"},\"name\":\"Experiment 1\",\"description\":\"This is an experiment\"}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());

        content = resultActions.andReturn().getResponse().getContentAsString();
        map = Helper.deserialize(content, Map.class);
        Integer experimentId = (Integer) map.get("id");

        mockHttpServletRequestBuilder = get("/workflows").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        content = resultActions.andReturn().getResponse().getContentAsString();
        Integer workflowId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                workflowId = (Integer) item.get("id");
            }
        }

        /* create a job associated with the project, workflow and experiment we just created */
        mockHttpServletRequestBuilder = post("/jobs").param("projectId", projectId.toString()).param("experimentId", experimentId.toString()).param("workflowId", workflowId.toString()).param("name", "Just a job").accept(MediaType.TEXT_HTML).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        /*forwarded to job/submit/jobId*/
        String forwardedUrl = resultActions.andReturn().getResponse().getForwardedUrl();

        System.out.println("Creating job, forwarded to: " + forwardedUrl);

        mockHttpServletRequestBuilder = post(forwardedUrl).accept(MediaType.TEXT_HTML_VALUE).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        /* redirected to jobs/task/jobId */
        String redirectedUrl = resultActions.andReturn().getResponse().getRedirectedUrl();

        System.out.println("Redirected to: " + redirectedUrl);

        mockHttpServletRequestBuilder = get(redirectedUrl).session(httpSession);

        /* we've run through all service tasks now */
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        assertEquals("<h3>Import succeeded</h3>", resultActions.andReturn().getModelAndView().getModel().get("generatedHtml"));
    }

    private void unzip(String zipFile) throws IOException {
        File folder = new File(WORKFLOW_FILES_DIR, "hplc_workflow");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(folder, fileName);
                new File(newFile.getParent()).mkdirs();
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    IOUtils.copy(zis, fos);
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }
}
