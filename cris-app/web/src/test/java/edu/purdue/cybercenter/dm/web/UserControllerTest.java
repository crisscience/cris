package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rangars
 */
public class UserControllerTest extends BaseControllerTest {

    static final private String AdminUsername = "administrator";

    @BeforeClass
    public static void setupClass() throws Exception {
    }

    @Test
    public void shouldBeAbleToSignUp() throws Exception {
        createTestWorkspace();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/auth/signup").session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void newUserShouldNotBeAbleToLoginAfterSignUp() throws Exception {
        createTestWorkspace();
        createTestUser();
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        ResultActions resultActions;

        mockHttpServletRequestBuilder = post("/auth/verify").param("username", "test@person.com").param("password", "password").session(httpSession);
        resultActions = this.mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(redirectedUrl("/auth/signin?authFailed=true"));
    }

    @Test
    public void shouldCreateADisabledUserOnSignUp() throws Exception {
        createTestWorkspace();
        createTestUser();

        login(AdminUsername, "password");

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/users?sort=+lastName").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String content = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> map = Helper.deserialize(content, List.class);
        Map<String, Boolean> userEnabledStatus = new HashMap<String, Boolean>();
        for (Map<String, Object> stringObjectMap : map) {
            userEnabledStatus.put((String) stringObjectMap.get("username"), (Boolean) stringObjectMap.get("enabled"));
        }
        assertTrue(userEnabledStatus.containsKey("test@person.com"));
        assertFalse(userEnabledStatus.get("test@person.com"));
    }

    @Test
    public void userShouldBeAbleToLoginAndLogoutOnceEnabled() throws Exception {
        createTestWorkspace();
        createTestUser();
        enableTestUser();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
        ResultActions resultActions;

        mockHttpServletRequestBuilder = get("/testws").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));

        login("test@person.com", "password");
        logout();
        mockHttpServletRequestBuilder = get("/").session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        assertNull(httpSession.getAttribute("tenantId"));
    }

    private void enableTestUser() throws Exception {
        login(AdminUsername, "password");
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/users?sort=+lastName").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String content = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> map = Helper.deserialize(content, List.class);
        Integer userId = null;
        for (Map<String, Object> stringObjectMap : map) {
            if (stringObjectMap.get("username").equals("test@person.com")) {
                userId = (Integer) stringObjectMap.get("id");
            }
        }
        assertNotNull(userId);
        mockHttpServletRequestBuilder = get("/users/" + userId).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        content = content.replace("\"enabled\":false", "\"enabled\":true");
        mockHttpServletRequestBuilder = put("/users/" + userId).content(content).accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        assertEquals(-1, content.indexOf("error"));

        logout();
    }

    private void createTestUser() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/auth/signup")
                .param("firstName", "Test")
                .param("middleName", "Space")
                .param("lastName", "Person")
                .param("email", "test@person.com")
                .param("username", "test@person.com")
                .param("password", "password")
                .param("password2", "password")
                .session(httpSession);


        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
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
