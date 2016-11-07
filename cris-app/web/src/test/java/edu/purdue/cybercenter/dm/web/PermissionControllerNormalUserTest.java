/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import java.io.UnsupportedEncodingException;
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
 * @author Ihsan
 */
public class PermissionControllerNormalUserTest extends BaseWithNormalUserControllerTest {

    @BeforeClass
    public static void setupClass() throws Exception {
    }

    private void mainMethodGet(String url, Map<String, Object> expectedResult) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/permissions/?" + url).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        Map<String, Object> list = checkResultContent(mockHttpServletRequestBuilder);

        assertEquals(8, list.size());

        checkPermission(list, expectedResult);
    }

    private void mainMethodPost(String json) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
    }

    private Map<String, Object> checkResultContent(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> list = Helper.deserialize(contents, Map.class);

        assertEquals(1, list.size());

        Map<String, Object> list1 = (Map<String, Object>) list.values().iterator().next();

        return list1;
    }

    private Map<String, Object> getExpectedResult(int id, Boolean read, Boolean update, Boolean create, Boolean delete, Boolean execute, Boolean owner){
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("id", id);
        expectedResult.put("read", read);
        expectedResult.put("update", update);
        expectedResult.put("create", create);
        expectedResult.put("delete", delete);
        expectedResult.put("execute", execute);
        expectedResult.put("owner", owner);

        return expectedResult;
    }

    private void checkPermission(Map<String, Object> list, Map<String, Object> expectedResult) {
        assertEquals(expectedResult.get("id"), list.get("id"));
        assertEquals(expectedResult.get("read"), list.get("read"));
        assertEquals(expectedResult.get("update"), list.get("update"));
        assertEquals(expectedResult.get("create"), list.get("create"));
        assertEquals(expectedResult.get("delete"), list.get("delete"));
        assertEquals(expectedResult.get("execute"), list.get("execute"));
        assertEquals(expectedResult.get("owner"), list.get("owner"));
    }

    /*******************************************************
      * Read
      ******************************************************/
    @Test
    public void testUserNormalPermissions() throws Exception {
        String url = "format=old&sId=3&objectIds=7002&objectClass=Experiment&group=false";
        Map<String, Object> expectedResult = getExpectedResult(7002, true, true, false, true, false, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testAdminGroupNormalPermissions() throws Exception {
        String url = "format=old&sId=1000&objectIds=5002&objectClass=Project&group=true";
        Map<String, Object> expectedResult = getExpectedResult(5002, true, true, true, true, true, true);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testGroupNormalPermissions() throws Exception {
        String url = "format=old&sId=1001&objectIds=7002&objectClass=Experiment&group=true";
        Map<String, Object> expectedResult = getExpectedResult(7002, true, true, false, true, false, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testUserNormalPermissionsNotExist() throws Exception {
        String url = "format=old&sId=3&objectIds=5001&objectClass=Project&group=false";
        Map<String, Object> expectedResult = getExpectedResult(5001, null, null, null, null, null, null);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testGroupNormalPermissionsNotExist() throws Exception {
        String url = "format=old&sId=1001&objectIds=5001&objectClass=Project&group=true";
        Map<String, Object> expectedResult = getExpectedResult(5001, null, null, null, null, null, null);
        mainMethodGet(url, expectedResult);
    }

    /*******************************************************
      * Create
      ******************************************************/
    @Test
    public void testCreatePermissionsForUser() throws Exception {
        String json = "{sId : 4, objectId : 7005, objectClass : \"Experiment\", read : true, update : true, create : false, delete : false, execute : false, owner : false, group : false}";
        mainMethodPost(json);

        String url = "format=old&sId=4&objectIds=7005&objectClass=Experiment&group=false";
        Map<String, Object> expectedResult = getExpectedResult(7005, true, true, false, false, false, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testCreatePermissionsForGroup() throws Exception {
        String json = "{sId : 1002, objectId : 754, objectClass : \"Job\", read : true, update : true, create : false, delete : false, execute : false, owner : false, group : true}";
        mainMethodPost(json);

        String url = "format=old&sId=1002&objectIds=754&objectClass=Job&group=true";
        Map<String, Object> expectedResult = getExpectedResult(754, true, true, false, false, false, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testCreatePermissionsForAdminGroup() throws Exception {
        String json = "{sId : 1000, objectId : 5001, objectClass : \"Project\", read : true, update : true, create : false, delete : false, execute : false, owner : false, group : true}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isPreconditionFailed());
    }

    @Test
    public void testCreateUserPermissionsForInheritance() throws Exception {
        String json = "{sId : 4, objectId : 0, objectClass : \"Project\", read : true, update : true, create : false, delete : false, execute : false, owner : false, group : false}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testCreateGroupPermissionsForInheritance() throws Exception {
        String json = "{sId : 1002, objectId : 0, objectClass : \"Project\", read : true, update : true, create : false, delete : false, execute : false, owner : false, group : true}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isForbidden());
    }

    /*******************************************************
      * Update
      ******************************************************/
    @Test
    public void testUpdatePermissionsForUser() throws Exception {
        String json = "{sId : 4, objectId : 7005, objectClass : \"Experiment\", read : true, update : true, create : true, delete : true, execute : false, owner : false, group : false}";
        mainMethodPost(json);

        String url = "format=old&sId=4&objectIds=7005&objectClass=Experiment&group=false";
        Map<String, Object> expectedResult = getExpectedResult(7005, true, true, true, true, false, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testUpdatePermissionsForGroup() throws Exception {
        String json = "{sId : 1002, objectId : 754, objectClass : \"Job\", read : true, update : true, create : true, delete : true, execute : false, owner : false, group : true}";
        mainMethodPost(json);

        String url = "format=old&sId=1002&objectIds=754&objectClass=Job&group=true";
        Map<String, Object> expectedResult = getExpectedResult(754, true, true, true, true, false, false);
        mainMethodGet(url, expectedResult);
    }

    @Test
    public void testUpdateUserPermissionsForInheritance() throws Exception {
        String json = "{sId : 4, objectId : 0, objectClass : \"Project\", read : true, update : true, create : true, delete : true, execute : false, owner : false, group : false}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateGroupPermissionsForInheritance() throws Exception {
        String json = "{sId : 1002, objectId : 0, objectClass : \"Project\", read : true, update : true, create : true, delete : true, execute : false, owner : false, group : true}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/permissions/").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isForbidden());
    }

    /*******************************************************
      * Testing Projects
      ******************************************************/

    @Test
    public void testGetProjects() throws Exception {
        List<Map<String, Object>> projects = listProjects();
        assertEquals(0, projects.size());

        setPermission("{sId : 2, objectId : 0, objectClass : \"Project\", read : true, update : false, create : false, delete : false, execute : false, owner : false, group : false}");

        projects = listProjects();
        assertEquals(4, projects.size());
    }

    @Test
    public void testCreateProjectWithoutPermission() throws Exception {
        setPermission("{sId : 2, objectId : 0, objectClass : \"Project\", read : false, update : false, create : false, delete : false, execute : false, owner : false, group : false}");

        String json = "{name : \"Stanley Morgan Fund ABCD1234\", description : \"This is a test project from Stanely Morgan\"}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects/").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> error = Helper.deserialize(contents, Map.class);

        assertEquals(3, error.size());
        assertTrue(error.containsKey("hasError"));
        assertTrue(error.containsKey("status"));
        assertTrue(error.containsKey("message"));

        assertEquals("Message", "Access is denied", error.get("message"));
        assertEquals("Status", "Unable to create the project", error.get("status"));
    }

    @Test
    public void testCreateProject() throws Exception {
        setPermission("{sId : 2, objectId : 0, objectClass : \"Project\", read : true, update : false, create : true, delete : false, execute : false, owner : false, group : false}");

        String json2 = "{name : \"Stanley Morgan Fund ABCD1234\", description : \"This is a test project from Stanely Morgan\"}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects/").content(json2).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> project = Helper.deserialize(contents, Map.class);

        assertEquals(15, project.size());
        assertEquals("Name", "Stanley Morgan Fund ABCD1234", project.get("name"));
        assertEquals("Description", "This is a test project from Stanely Morgan", project.get("description"));

        List<Map<String, Object>> projects = listProjects();
        assertEquals(4 + 1, projects.size());

        logout();
        loginAdminUser();

        projects = listProjects();
        assertEquals(4 + 1, projects.size());
    }

    @Test
    public void testUpdateProjectWithoutPermission() throws Exception {
        String json = "{id : 5003, name : \"NIH Fund ABCD1234\", description : \"This is a test project\"}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/projects/5003").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> error = Helper.deserialize(contents, Map.class);

        assertEquals(3, error.size());
        assertTrue(error.containsKey("hasError"));
        assertTrue(error.containsKey("status"));
        assertTrue(error.containsKey("message"));

        assertEquals("Message", "Access is denied", error.get("message"));
        assertEquals("Status", "Unable to update the project", error.get("status"));
    }

    @Test
    public void testUpdateProject() throws Exception {
        setPermission("{sId : 2, objectId : 0, objectClass : \"Project\", read : false, update : true, create : false, delete : false, execute : false, owner : false, group : false}");

        String json = "{id : 5002, name : \"NIH Modified Fund ABCD1234\", description : \"This is a test project\", \"assetTypeId\":5, \"statusId\":1, \"tenantId\":1}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/projects/5002").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> project = Helper.deserialize(contents, Map.class);

        assertEquals(15, project.size());
        assertEquals("Id", 5002, project.get("id"));
        assertEquals("Name", "NIH Modified Fund ABCD1234", project.get("name"));
        assertEquals("Description", "This is a test project", project.get("description"));
        assertEquals("AssetTypeId", 5, project.get("assetTypeId"));
        assertEquals("StatusId", 1, project.get("statusId"));
        assertEquals("TenantId", 1, project.get("tenantId"));
    }

    @Test
    public void testDeleteProjectWithoutPermission() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = delete("/projects/5004").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> error = Helper.deserialize(contents, Map.class);

        assertEquals(1, error.size());
        assertEquals("Message", "Access is denied: Unable to delete the project", error.get("message"));
    }

    @Test
    public void testDeleteProject() throws Exception {
        setPermission("{sId : 2, objectId : 0, objectClass : \"Project\", read : true, update : false, create : false, delete : true, execute : false, owner : false, group : false}");

        List<Map<String, Object>> projects = listProjects();
        assertEquals(4, projects.size());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = delete("/projects/5003").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        projects = listProjects();

        assertEquals(3, projects.size());
    }

    private List<Map<String, Object>> listProjects() throws UnsupportedEncodingException, Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/projects/").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> projects = Helper.deserialize(contents, List.class);
        return projects;
    }

    private void setPermission(String permission) throws Exception {
        logout();
        loginAdminUser();

        mainMethodPost(permission);

        logout();
        loginNormalUser();
    }

}
