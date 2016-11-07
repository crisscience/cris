package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.service.CrisScriptEngine;
import edu.purdue.cybercenter.dm.util.Helper;
import java.util.Map;
import javax.script.ScriptException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 *
 * @author xu222
 */
public class ProjectControllerTest extends BaseWithAdminUserControllerTest {

    @Autowired
    private CrisScriptEngine crisScriptEngine;

    private static final String jsonNewProject = "{\"description\":\"This is a project\",\"name\":\"Project Test\"}";
    private static final String jsonOperationalProjectId = "5001";
    private static final String jsonDeprecatedProjectId = "5005";
    private static final String jsonOperationalProject = "{\"assetTypeId\":7,\"creatorId\":null,\"description\":\"Purdue Center for Cancer Research\",\"email\":null,\"id\":" + jsonOperationalProjectId + ",\"imageId\":null,\"name\":\"NSF Fund 12345678\",\"ownerId\":null,\"statusId\":1,\"tenantId\":{\"$ref\":\"/tenants/1\",\"creatorId\":null,\"description\":null,\"email\":null,\"enabled\":true,\"name\":\"Sylvie Brouder's Workspace\",\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null,\"urlIdentifier\":\"brouder_sylvie\",\"uuid\":{\"$uuid\":\"f47ac10b-58cc-4372-a567-0e02b2c3d479\"}},\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null}";
    private static final String jsonDeprecatedProject = "{\"assetTypeId\":7,\"creatorId\":null,\"description\":\"Cyber Center at Purdue\",\"email\":null,\"id\":" + jsonDeprecatedProjectId + ",\"imageId\":null,\"name\":\"ProjectControllerDeprecatedTest Project\",\"ownerId\":null,\"statusId\":0,\"tenantId\":1,\"timeCreated\":null,\"timeUpdated\":null,\"updaterId\":null}";

    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    private void validateProjectInfo(Map<String, Object> projectMap, Map<String, Object> expectedMap){
        if (projectMap == null || projectMap.isEmpty())
            throw new RuntimeException("Improper inputs");

        assertEquals(15, projectMap.size());
        if (expectedMap.get("id") != null) {
            assertEquals("Id", (int) expectedMap.get("id"), projectMap.get("id"));
            assertEquals("StatusId", (int) expectedMap.get("statusId"), projectMap.get("statusId"));
        }
        assertEquals("Name", (String) expectedMap.get("name"), projectMap.get("name"));
        assertEquals("Description", (String) expectedMap.get("description"), projectMap.get("description"));
        assertEquals("AssetTypeId", 7, projectMap.get("assetTypeId"));
        assertEquals("TenantId", 1, projectMap.get("tenantId"));
    }

    @Test
    public void testBooleanExpression() throws ScriptException {
        //TODO: put a data type flag: json, xml and etc.
        //TODO: define a set of utility functions that are used frequently
        Map<String, Object> init = Helper.deserialize("{a: 1}", Map.class);
        Map<String, Object> expected = Helper.deserialize("{a: 1, b: 2, c: [1, 2]}", Map.class);
        Map<String, Object> result = Helper.deserialize("{a: 1, b: 2, c: [1, 2]}", Map.class);

        String expressionName = "Test boolean assertion";
        String expression1 = "init.a != null";
        String expression2 = "result.a == expected.a";
        String expression3 = "result.b == expected.b";
        String expression4 = "result.c[0] == expected.c[0]";

        crisScriptEngine.createEngineScope(init, expected, result);

        crisScriptEngine.evaluateScript("print(\"aaa\");");
        String value = crisScriptEngine.evaluateScript("hello();", String.class);
        assertEquals("expect world", "world", value);

        assertTrue(expressionName, crisScriptEngine.evaluateBooleanExpression(expression1));
        assertTrue(expressionName, crisScriptEngine.evaluateBooleanExpression(expression2));
        assertTrue(expressionName, crisScriptEngine.evaluateBooleanExpression(expression3));
        assertTrue(expressionName, crisScriptEngine.evaluateBooleanExpression(expression4));
    }

    /*
     * 1. create a project
     *    * expected status code: status().isCreated()
     *    * expected response content: newly created project with id assigned
     */
    @Test
    public void testProjectCreation() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/projects").content(jsonNewProject).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isCreated()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));

        Map<String, Object> jsonProjectMap = Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonNewProject, Map.class);

        validateProjectInfo(jsonProjectMap, expectedMap);
    }

    /*
     * 2. retrieve an exiting project
     *    * expected status code: status().isOk()
     *    * expected response content: project content
    */
    @Test
    public void testProjectRetrieval() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/projects/5001").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession); //.accept(MediaType.APPLICATION_JSON_UTF8)
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        Map<String, Object> jsonProjectMap = Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonOperationalProject, Map.class);

        validateProjectInfo(jsonProjectMap, expectedMap);
    }

    /*
     * 3. update an existing project
     *    * expected status code: status().isOk()
     *    * expected response content: updated project content
    */
    @Test
    public void testProjectUpdate() throws Exception{
        String json = "{id : 5001, name : \"NSF Modified Fund 12345678\", description : \"This is a test project\", \"assetTypeId\":7, \"statusId\":1, \"tenantId\":1}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/projects/5001").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();

        Map<String, Object> projectMap = Helper.deserialize(contents, Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonOperationalProject, Map.class);

        expectedMap.put("name", "NSF Modified Fund 12345678");
        expectedMap.put("description", "This is a test project");

        validateProjectInfo(projectMap, expectedMap);
    }

    /*
     * 4. deprecate an existing operational project
     *    * expected status code: status().isOk()
     *    * expected response content: updated project content
    */
    @Test
    public void testProjectDeprecation() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/projects/5001").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession); //.accept(MediaType.APPLICATION_JSON_UTF8)
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        Map<String, Object> projectMap = Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonOperationalProject, Map.class);

        projectMap.put("statusId", 0); // to deprecate the project
        expectedMap.put("statusId", 0); // to validate deprecation

        mockHttpServletRequestBuilder = put("/projects/5001").content(Helper.deepSerialize(projectMap)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        projectMap = Helper.deserialize(contents, Map.class);

        validateProjectInfo(projectMap, expectedMap);
    }

    /*
     * 5. restore an existing deprecated project
     *    * expected status code: status().isOk()
     *    * expected response content: updated project content
     */
    @Test
    public void testProjectRestoration() throws Exception{
        Map<String, Object> projectMap = Helper.deserialize(jsonDeprecatedProject, Map.class);
        Map<String, Object> expectedMap = Helper.deserialize(jsonDeprecatedProject, Map.class);

        projectMap.put("statusId", 1);// to restore deprecated project
        expectedMap.put("statusId", 1);// to validate restoration

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/projects/5005").content(Helper.deepSerialize(projectMap)).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        projectMap = Helper.deserialize(contents, Map.class);

        validateProjectInfo(projectMap, expectedMap);
    }

    /*
     * 6. retrieve a non-exist project
     *    * expected status code: status().isNotFound()
     *    * expected response content: no content
    */
    @Test
    public void testNonProjectRetrieval() throws Exception{
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/projects/5006").accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession); //.accept(MediaType.APPLICATION_JSON_UTF8)
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());

    }

    /*
     * 7. update a non-exist project
     *    * expected status code: status().isNotFound()
     *    * expected response content: no content
    */
    @Ignore
    @Test
    public void testNonProjectUpdation() throws Exception{
        String json = "{id : 5006, name : \"NIH Modified Modified Fund ABCD1234\", description : \"This is a test project\", \"assetTypeId\":7, \"statusId\":1, \"tenantId\":{\"$ref\":\"/tenants/1\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/projects/5006").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    /*
     * 8. deprecate a non-exist project
     *    * expected status code: status().isNotFound()
     *    * expected response content: no content
    */
    @Ignore
    @Test
    public void testNonProjectDeprecation() throws Exception{
        String json = "{id : 5006, name : \"NIH Modified Modified Fund ABCD1234\", description : \"This is a test project\", \"assetTypeId\":7, \"statusId\":0, \"tenantId\":{\"$ref\":\"/tenants/1\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/projects/5006").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());
    }

    /*
     * 9. restore a non-exist project
     *    * expected status code: status().isNotFound()
     *    * expected response content: no content
     */
    @Ignore
    @Test
    public void testNonProjectRestoration() throws Exception {
        String json = "{id : 5006, name : \"NIH Modified Modified Fund ABCD1234\", description : \"This is a test project\", \"assetTypeId\":7, \"statusId\":1, \"tenantId\":{\"$ref\":\"/tenants/1\"}}";
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = put("/projects/5006").content(json).accept(MediaType.APPLICATION_JSON_UTF8).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isNotFound());
    }
}
