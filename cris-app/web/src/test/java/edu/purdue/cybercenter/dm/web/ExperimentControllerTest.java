package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 *
 * @author xu222, jain117
 */
public class ExperimentControllerTest extends BaseWithAdminUserControllerTest {

    public static final String jsonNewExperiment = "{description: \"\", name: \"DemoExperiment\", projectId: {$ref: \"/projects/5001\"}}";//{$ref: \"/cris/projects/5001\"}
    public static final String jsonOperationalExperiment = "{\"assetTypeId\":5,\"creatorId\":null,\"description\":\"Purdue Center for Cancer Research\",\"email\":null,\"id\":7001,\"imageId\":null,\"name\":\"NSF Fund ABCD1234 Experiment 1\",\"ownerId\":null,\"projectId\":{\"$ref\":\"/projects/5001\",\"assetTypeId\":7,\"creatorId\":null,\"description\":\"Purdue Center for Cancer Research\",\"email\":null,\"imageId\":null,\"name\":\"NSF Fund 12345678\",\"ownerId\":null,\"statusId\":1,\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null},\"statusId\":1,\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null}";
    public static final String jsonDeprecatedExperiment = "{\"assetTypeId\":5,\"creatorId\":null,\"description\":\"Cyber Center at Purdue\",\"email\":null,\"id\":7007,\"imageId\":null,\"name\":\"Deprecated Project\",\"ownerId\":null,\"projectId\":{\"$ref\":\"/projects/5001\",\"assetTypeId\":7,\"creatorId\":null,\"description\":\"Purdue Center for Cancer Research\",\"email\":null,\"imageId\":null,\"name\":\"NSF Fund 12345678\",\"ownerId\":null,\"statusId\":1,\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null},\"statusId\":0,\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null}";

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    public void validateExperimentInfo(Map<String, Object> experimentMap, Map<String, Object> expectedMap){
        if (experimentMap == null || experimentMap.isEmpty())
            throw new RuntimeException("Improper inputs");

        assertEquals(16, experimentMap.size());
        assertEquals("experimentId", expectedMap.get("id"), experimentMap.get("id"));
        assertEquals("assetTypeId", expectedMap.get("assetTypeId"), experimentMap.get("assetTypeId"));
        assertEquals("projectId", ((Map<String, Object>) expectedMap.get("projectId")).get("$ref"), ((Map<String, Object>) experimentMap.get("projectId")).get("$ref"));
        assertEquals("description", expectedMap.get("description"), experimentMap.get("description"));
        assertEquals("name", expectedMap.get("name"), experimentMap.get("name"));
        assertEquals("tenantId", 1, experimentMap.get("tenantId"));
        assertEquals("statusId", expectedMap.get("statusId"), experimentMap.get("statusId"));
    }

    @Test
    public void testCreateExperiment() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/experiments").content(jsonNewExperiment).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());
    }

    @Ignore
    @Test
    public void testCreateExperimentNonExistProject() throws Exception {
        Map<String, Object> map = Helper.deserialize(jsonNewExperiment, Map.class);
        ((Map<String, Object>) map.get("projectId")).put("$ref", "/projects/10000");
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/experiments").content(Helper.deepSerialize(map)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println(response);

        resultActions.andExpect(status().isNotFound());
        assertEquals("Unable to create the experiment", Helper.deserialize(response, Map.class).get("status"));
    }

    @Ignore
    @Test
    public void testCreateExperimentNoProjectId() throws Exception {
        Map<String, Object> map = Helper.deserialize(jsonNewExperiment, Map.class);
        map.remove("projectId");
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/experiments").content(Helper.deepSerialize(map)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println(response);

        resultActions.andExpect(status().isNotFound());
        assertEquals("Unable to create the experiment", Helper.deserialize(response, Map.class).get("status"));
    }

    @Ignore
    @Test
    public void testCreateExperimentDeprecatedProject() throws Exception {
        Map<String, Object> map = Helper.deserialize(jsonNewExperiment, Map.class);
        ((Map<String, Object>) map.get("projectId")).put("$ref", "/projects/5005"); // deprecated Project
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/experiments").content(Helper.deepSerialize(map)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println(response);

        resultActions.andExpect(status().isNotFound());
        assertEquals("Unable to create the experiment", Helper.deserialize(response, Map.class).get("status"));
    }

    @Test
    public void testRetrieveExperiment() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/experiments/7001").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        System.out.println(resultActions.andReturn().getResponse().getContentAsString());

        Map<String, Object> experimentMap = Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonOperationalExperiment, Map.class);

        validateExperimentInfo(experimentMap, expectedMap);
    }

    @Ignore
    @Test
    public void testRetrieveExperimentNonExistProject() throws Exception{
        // Deprecating the Project first
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/projects/5001").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession); //.accept(MediaType.APPLICATION_JSON_UTF8)
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        Map<String, Object> projectMap = Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class);

        projectMap.put("statusId", 0); // to deprecate the project

        mockHttpServletRequestBuilder = put("/projects/5001").content(Helper.deepSerialize(projectMap)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        //Test retrieval of experiment whose project has been deprecated
        mockHttpServletRequestBuilder = get("/experiments/7001").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateExperiment() throws Exception{
        String experimentUpdate = "{id : 7001, name : \"NSF Modified Fund 12345678 Experiment 1\", description : \"This is a test project. This is an update\", \"assetTypeId\":5, \"statusId\":1, \"tenantId\":1, projectId: {$ref: \"/projects/5001\"}}";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/experiments/7001").content(experimentUpdate).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();

        Map<String, Object> experimentMap = Helper.deserialize(contents, Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonOperationalExperiment, Map.class);

        expectedMap.put("name", "NSF Modified Fund 12345678 Experiment 1");
        expectedMap.put("description", "This is a test project. This is an update");

        validateExperimentInfo(experimentMap, expectedMap);
    }

    @Ignore
    @Test
    public void testUpdateExperimentNonExistProject() throws Exception{
        String experimentUpdate = "{id : 7001, name : \"NSF Modified Fund 12345678 Experiment 1\", description : \"This is a test project. This is an update\", \"assetTypeId\":5, \"statusId\":1, \"tenantId\":{\"$ref\":\"/tenants/1\"}, projectId: {$ref: \"/projects/10000\"}}";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/experiments/7001").content(experimentUpdate).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String contents = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isNotFound());
        assertEquals("Unable to Update the experiment", Helper.deserialize(contents, Map.class).get("status"));
        //resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        //System.out.println(contents);
    }

    @Ignore
    @Test
    public void testUpdateExperimentNoProjectId() throws Exception{
        String experimentUpdate = "{id : 7001, name : \"NSF Modified Fund 12345678 Experiment 1\", description : \"This is a test project. This is an update\", \"assetTypeId\":5, \"statusId\":1, \"tenantId\":{\"$ref\":\"/tenants/1\"}}";//, projectId: {$ref: \"/projects/10000\"}

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/experiments/7001").content(experimentUpdate).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String contents = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isNotFound());
        assertEquals("Unable to Update the experiment", Helper.deserialize(contents, Map.class).get("status"));
        //resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        //System.out.println(contents);
    }

    @Ignore
    @Test
    public void testUpdateExperimentDeprecatedProject() throws Exception{
        String experimentUpdate = "{id : 7001, name : \"NSF Modified Fund 12345678 Experiment 1\", description : \"This is a test project. This is an update\", \"assetTypeId\":5, \"statusId\":1, \"tenantId\":{\"$ref\":\"/tenants/1\"}, projectId: {$ref: \"/projects/5005\"}}";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/experiments/7001").content(experimentUpdate).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String contents = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isNotFound());
        //resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        System.out.println(contents);
    }

    @Test
    public void testDeprecateExperiment() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/experiments/7001").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        Map<String, Object> experimentMap = Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonOperationalExperiment, Map.class);

        experimentMap.put("statusId", 0); // to deprecate the project
        expectedMap.put("statusId", 0); // to validate deprecation

        mockHttpServletRequestBuilder = put("/experiments/7001").content(Helper.deepSerialize(experimentMap)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        experimentMap = Helper.deserialize(contents, Map.class);

        validateExperimentInfo(experimentMap, expectedMap);
    }

    @Test
    public void testRestoreExperiment() throws Exception{
        Map<String, Object> experimentMap = Helper.deserialize(jsonDeprecatedExperiment, Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonDeprecatedExperiment, Map.class);

        experimentMap.put("statusId", 1); // to deprecate the project
        expectedMap.put("statusId", 1); // to validate deprecation

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/experiments/7007").content(Helper.deepSerialize(experimentMap)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        experimentMap = Helper.deserialize(contents, Map.class);

        validateExperimentInfo(experimentMap, expectedMap);
    }

    @Test
    public void testRetrieveNonExperiment() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/experiments/7008").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    @Ignore
    @Test
    public void testUpdateNonExperiment() throws Exception{
        String experimentUpdate = "{id : 7008, name : \"NSF Modified Fund 12345678 Experiment 1\", description : \"This is a test project. This is an update\", \"assetTypeId\":5, \"statusId\":1, \"tenantId\":{\"$ref\":\"/tenants/1\"}, projectId: {$ref: \"/projects/5001\"}}";

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/experiments/7008").content(experimentUpdate).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());
    }
    @Ignore
    @Test
    public void testDeprecateNonExperiment() throws Exception{
        String experimentUpdate = "{id : 7008, name : \"NSF Modified Fund 12345678 Experiment 1\", description : \"This is a test project. This is an update\", \"assetTypeId\":5, \"statusId\":1, \"tenantId\":{\"$ref\":\"/tenants/1\"}, projectId: {$ref: \"/projects/5001\"}}";
        Map<String, Object> experimentMap = Helper.deserialize(experimentUpdate, Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonOperationalExperiment, Map.class);

        experimentMap.put("statusId", 0); // to deprecate the project
        expectedMap.put("statusId", 0); // to validate deprecation

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/experiments/7008").content(Helper.deepSerialize(experimentMap)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());
    }
    @Ignore
    @Test
    public void testRestoreNonExperiment() throws Exception{
        String experimentUpdate = "{id : 7008, name : \"NSF Modified Fund 12345678 Experiment 1\", description : \"This is a test project. This is an update\", \"assetTypeId\":5, \"statusId\":0, \"tenantId\":{\"$ref\":\"/tenants/1\"}, projectId: {$ref: \"/projects/5001\"}}";
        Map<String, Object> experimentMap = Helper.deserialize(experimentUpdate, Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonOperationalExperiment, Map.class);

        experimentMap.put("statusId", 0); // to deprecate the project
        expectedMap.put("statusId", 0); // to validate deprecation

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/experiments/7008").content(Helper.deepSerialize(experimentMap)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());
    }
}
