package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rangars
 */
public class ProfileControllerTest extends BaseControllerTest {

    static final private String AdminUsername = "administrator";

    @BeforeClass
    public static void setupClass() throws Exception {
    }

    @Ignore
    @Test
    public void testUserShouldBeAbleToChangePasswordOnceEnabled() throws Exception {
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
        mockHttpServletRequestBuilder = get("/profile").session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        mockHttpServletRequestBuilder = post("/profile/password")
                .param("firstName", "Test")
                .param("middleName", "Space")
                .param("lastName", "Person")
                .param("username", "test@person.com")
                .param("email", "test@person.com")
                .param("password0", "password")
                .param("password1", "p@ssw0rd")
                .param("password2", "p@ssw0rd")
                .session(httpSession);
        mockMvc.perform(mockHttpServletRequestBuilder);

        logout();

        mockHttpServletRequestBuilder = get("/testws").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));

        mockHttpServletRequestBuilder = post("/auth/verify").param("username", "test@person.com").param("password", "password").session(httpSession);
        resultActions = this.mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(redirectedUrl("/auth/signin?authFailed=true"));

        login("test@person.com", "p@ssw0rd");
    }

    @Ignore
    @Test
    public void testUserShouldBeAbleToChangeUsernameOnceEnabled() throws Exception {
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
        mockHttpServletRequestBuilder = get("/profile").session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        mockHttpServletRequestBuilder = post("/profile/password")
                .param("firstName", "Test")
                .param("middleName", "Space")
                .param("lastName", "Person")
                .param("username", "special_user")
                .param("email", "test@person.com")
                .param("password0", "")
                .param("password1", "")
                .param("password2", "")
                .session(httpSession);
        mockMvc.perform(mockHttpServletRequestBuilder);

        logout();

        mockHttpServletRequestBuilder = get("/testws").session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isFound());
        assertNotNull(httpSession.getAttribute("tenantId"));

        mockHttpServletRequestBuilder = post("/auth/verify").param("username", "test@person.com").param("password", "password").session(httpSession);
        resultActions = this.mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(redirectedUrl("/auth/signin?authFailed=true"));

        login("special_user", "password");
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
        System.out.println("PCT: ******************************************" + content + "******************************************");
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
