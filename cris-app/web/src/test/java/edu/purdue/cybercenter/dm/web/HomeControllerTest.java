package edu.purdue.cybercenter.dm.web;

import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author xu222
 */
public class HomeControllerTest extends BaseControllerTest {

    @Test
    public void testWorkspace() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/").accept(MediaType.TEXT_HTML).session(httpSession);

        // perform action
        ResultActions resultActions = this.mockMvc.perform(mockHttpServletRequestBuilder);
        ModelAndView mav = resultActions.andReturn().getModelAndView();
        Map<String, Object> model = mav.getModel();
        String contents = resultActions.andReturn().getResponse().getContentAsString();

        assertEquals("index", mav.getViewName());
        assertNotNull("tenant list should not be null", model.get("tenants"));
        assertEquals(contents, "");
    }

    @Test
    public void testLoginLogout() throws Exception {

        /*
         * admin user home
         */
        loginAdminUser();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/").accept(MediaType.TEXT_HTML).session(httpSession);

        ResultActions resultActions = this.mockMvc.perform(mockHttpServletRequestBuilder);
        ModelAndView mav = resultActions.andReturn().getModelAndView();
        Map<String, Object> model = mav.getModel();
        String content = resultActions.andReturn().getResponse().getContentAsString();

        assertEquals("home", mav.getViewName());
        assertNull("tenant list should be null after login", model.get("tenants"));
        assertEquals(content, "");

        logout();

        testWorkspace();

        /*
         * normal user home
         */
        loginNormalUser();

        mockHttpServletRequestBuilder = get("/").accept(MediaType.TEXT_HTML).session(httpSession);

        resultActions = this.mockMvc.perform(mockHttpServletRequestBuilder);
        mav = resultActions.andReturn().getModelAndView();
        model = mav.getModel();
        content = resultActions.andReturn().getResponse().getContentAsString();

        assertEquals("home", mav.getViewName());
        assertNull("tenant list should be null after login", model.get("tenants"));
        assertEquals(content, "");

        logout();

        testWorkspace();
    }
}
