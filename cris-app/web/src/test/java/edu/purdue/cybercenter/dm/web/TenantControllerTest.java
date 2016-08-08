package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author rangars
 */
public class TenantControllerTest extends BaseControllerTest {

    static final private String AdminUsername = "administrator";
    static final private String PublicUsername = "public";
    static final private String PublicPassword = "d41d62b0-3cbc-11e2-a25f-0800200c9a66";

    @BeforeClass
    public static void setupClass() throws Exception {
    }

    @Test
    public void shouldBeAbleToAccessCreateWorkspace() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/tenants/createForm").session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void shouldCreateAWorkSpace() throws Exception {
        createTestWorkspace();
    }

    @Test
    public void adminAndPublicUserShouldBeAbleToLogin() throws Exception {
        createTestWorkspace();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        ResultActions resultActions;
        login(PublicUsername, PublicPassword);
        logout();

        mockHttpServletRequestBuilder = get("/testws").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));

        login(AdminUsername, "password");
        logout();
    }

    @Test
    public void adminShouldSeeAdminAndPublicUsers() throws Exception {
        createTestWorkspace();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        ResultActions resultActions;

        login(AdminUsername, "password");

        mockHttpServletRequestBuilder = get("/users?sort=+lastName").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String content = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> map = Helper.deserialize(content, List.class);
        List<String> users = new ArrayList<String>();
        for (Map<String, Object> stringObjectMap : map) {
            users.add((String) stringObjectMap.get("username"));
        }
        assertTrue(users.contains("administrator"));
        assertTrue(users.contains("public"));
    }

    @Test
    public void adminShouldSeeAdminGroup() throws Exception {
        createTestWorkspace();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        ResultActions resultActions;

        login(AdminUsername, "password");

        mockHttpServletRequestBuilder = get("/groups?sort=+name").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String content = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> map = Helper.deserialize(content, List.class);
        assertEquals(1, map.size());
        assertEquals("Admin Group", map.get(0).get("name"));
    }

    @Test
    public void onlyAdminShouldBelongToAdminGroup() throws Exception {
        createTestWorkspace();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        ResultActions resultActions;

        login(AdminUsername, "password");

        mockHttpServletRequestBuilder = get("/groups?sort=+name").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String content = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> map = Helper.deserialize(content, List.class);
        assertEquals(1, map.size());
        Integer groupId = (Integer) map.get(0).get("id");

        mockHttpServletRequestBuilder = get("/users?groupId=" + groupId + "&sort=+username").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        map = Helper.deserialize(content, List.class);
        assertEquals(1, map.size());
        String userInGroup = (String) map.get(0).get("username");
        assertEquals("administrator", userInGroup);
    }

    private void createTestWorkspace() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/tenants/create")
                .param("urlIdentifier", "testws")
                .param("name", "Test Workspace")
                .param("password1", "password")
                .param("password2", "password")
                .session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());

        mockHttpServletRequestBuilder = get("/tenants/summary?urlIdentifier=testws&name=Test+Workspace").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockHttpServletRequestBuilder = get("/testws").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));
    }

}
