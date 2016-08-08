package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * @author msarfraz
 */
public class PermissionControllerAdminUserTest extends BaseWithAdminUserControllerTest {

    //private static TestUtils testUtils = new TestUtils();

    @BeforeClass
    public static void setupClass() throws Exception {
    }

    private void mainMethodGet(String url, Map<String, Object> expectedResult) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/permissions/?" + url).accept(MediaType.APPLICATION_JSON).session(httpSession);
        Map<String, Object> list = checkResultContent(mockHttpServletRequestBuilder);

        assertEquals(list.size(), 6);

        checkPermission(list, expectedResult);
    }

    private void mainMethodPost(String json) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
    }

    private Map<String, Object> checkResultContent(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> list = Helper.deserialize(contents, Map.class);

        assertEquals(list.size(), 1);

        Map<String, Object> list1 = (Map<String, Object>) list.get("0");
        return list1;
    }

    private Map<String, Object> getExpectedResult(int id, boolean read, boolean update, boolean create, boolean delete, boolean execute){

        Map<String, Object> expectedResult = new HashMap<String, Object>();
        expectedResult.put("id", id);
        expectedResult.put("read", read);
        expectedResult.put("update", update);
        expectedResult.put("create", create);
        expectedResult.put("delete", delete);
        expectedResult.put("execute", execute);

        return expectedResult;
    }

    private void checkPermission(Map<String, Object> list, Map<String, Object> expectedResult) {

        assertEquals(list.get("id"), expectedResult.get("id"));
        assertEquals(list.get("read"), expectedResult.get("read"));
        assertEquals(list.get("update"), expectedResult.get("update"));
        assertEquals(list.get("create"), expectedResult.get("create"));
        assertEquals(list.get("delete"), expectedResult.get("delete"));
        assertEquals(list.get("execute"), expectedResult.get("execute"));
    }
    @Test
    public void testMarkerMethod(){

    }
    /*******************************************************
      * Read
      ******************************************************/
    @Test
    public void testAdminPermissionsProject() throws Exception {

        String url = "format=old&sId=1&objectIds=5002&objectClass=Project&group=no";
        Map<String, Object> expectedResult = getExpectedResult(5002, true, true, true, true, true);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testUserPermissionsProject() throws Exception {

        String url = "format=old&sId=2&objectIds=5001&objectClass=Project&group=no";
        Map<String, Object> expectedResult = getExpectedResult(5001, true, false, false, false, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testUserPermissionsNotExist() throws Exception {

        String url = "format=old&sId=3&objectIds=5003&objectClass=Project&group=no";
        // read is inherited from its member group
        Map<String, Object> expectedResult = getExpectedResult(5003, true, false, false, false, false);
        mainMethodGet(url, expectedResult);
    }

    /*******************************************************
      * Create
      ******************************************************/
    @Test
    public void testCreatePermissionsForAdminUser() throws Exception {

        String json = "{sId : 1, objectId : 5001, objectClass : \"Project\", read : true, update : true, create : false, delete : false, execute : false, group : false}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isPreconditionFailed());
    }

    @Test
    public void testCreatePermissionsForUser() throws Exception {

        String json = "{sId : 3, objectId : 7004, objectClass : \"Experiment\", read : true, update : true, create : false, delete : false, execute : false, group : false}";
        mainMethodPost(json);

        String url = "format=old&sId=3&objectIds=7004&objectClass=Experiment&group=no";
        Map<String, Object> expectedResult = getExpectedResult(7004, true, true, false, false, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testCreatePermissionsForInheritance() throws Exception {

        String json = "{sId : 2, objectId : 0, objectClass : \"Project\", read : false, update : false, create : false, delete : false, execute : true, group : false}";
        mainMethodPost(json);
        logout();
        loginNormalUser();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/experiments/").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> experiments = Helper.deserialize(contents, List.class);

        assertEquals("number of experiments", 2, experiments.size());

    }

    /*******************************************************
      * Update
      ******************************************************/
    @Test
    public void testUpdatePermissionsForAdminUser() throws Exception {

        String json = "{sId : 1, objectId : 5001, objectClass : \"Project\", read : true, update : true, create : true, delete : true, execute : false, group : false}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isPreconditionFailed());
    }

    @Test
    public void testUpdatePermissionsForUser() throws Exception {

        String json = "{sId : 3, objectId : 7004, objectClass : \"Experiment\", read : true, update : true, create : true, delete : true, execute : false, group : false}";
        mainMethodPost(json);

        String url = "format=old&sId=3&objectIds=7004&objectClass=Experiment&group=no";
        Map<String, Object> expectedResult = getExpectedResult(7004, true, true, true, true, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testUpdatePermissionsForInheritance() throws Exception {

        String json = "{sId : 2, objectId : 0, objectClass : \"Project\", read : true, update : true, create : true, delete : true, execute : false, group : false}";
        mainMethodPost(json);

        logout();
        loginNormalUser();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/experiments/").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> experiments = Helper.deserialize(contents, List.class);

        assertEquals("number of experiments", 2, experiments.size());

        logout();
        loginAdminUser();

        String json1 = "{sId : 2, objectId : 0, objectClass : \"Project\", read : false, update : false, create : false, delete : false, execute : false, group : false}";
        mainMethodPost(json1);

    }

    /*******************************************************
      * Testing Projects
      ******************************************************/
    @Test
    public void testGetProjects() throws Exception {

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/projects/").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> projects = Helper.deserialize(contents, List.class);

        assertEquals(4, projects.size());
    }

    @Test
    public void testUpdateProject() throws Exception {

        String json = "{id : 5002, name : \"NIH Modified Fund ABCD1234\", description : \"This is a test project\", \"assetTypeId\":5, \"statusId\":1, \"tenantId\":1}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/projects/5002").content(json).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> project = Helper.deserialize(contents, Map.class);

        assertEquals(13, project.size());
        assertEquals("Id", 5002, project.get("id"));
        assertEquals("Name", "NIH Modified Fund ABCD1234", project.get("name"));
        assertEquals("Description", "This is a test project", project.get("description"));
        assertEquals("AssetTypeId", 5, project.get("assetTypeId"));
        assertEquals("StatusId", 1, project.get("statusId"));
        assertEquals("TenantId", 1, project.get("tenantId"));
    }

    @Test
        public void testCreateProject() throws Exception {

        String json = "{name : \"HP Fund ABCD1234\", description : \"This is a test project\"}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects/").content(json).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> project = Helper.deserialize(contents, Map.class);

        assertEquals(13, project.size());
        assertEquals("Name", "HP Fund ABCD1234", project.get("name"));
        assertEquals("Description", "This is a test project", project.get("description"));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = get("/projects/").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions1 = mockMvc.perform(mockHttpServletRequestBuilder1);
        String contents1 = resultActions1.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> projects = Helper.deserialize(contents1, List.class);

        assertEquals(5, projects.size());
    }

    @Test
    public void testDeleteProject() throws Exception {

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = delete("/projects/5004").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = get("/projects/").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions1 = mockMvc.perform(mockHttpServletRequestBuilder1);
        String contents = resultActions1.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> projects = Helper.deserialize(contents, List.class);

        assertEquals(3, projects.size());
    }
}
