package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
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
 * @author xu222
 */
public class JobControllerTest extends BaseWithAdminUserControllerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Test
    public void jobsJsonNames1() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/jobs/json/names").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.valueOf("application/json")));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);

        // Note that if there is no parameters, it works same as "projectId=0&experimentId=0".
        assertEquals(((Map) list.get(0)).get("name"), "--ALL--");
        assertEquals(((Map) list.get(0)).get("id"), "0");
        assertEquals(list.size(), 4);
    }

    @Test
    public void jobsJsonNames2() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/jobs/json/names/?projectId=0&experimentId=0").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.valueOf("application/json")));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);

        assertEquals(((Map) list.get(0)).get("name"), "--ALL--");
        assertEquals(((Map) list.get(0)).get("id"), "0");
        assertEquals(list.size(), 4);
    }

    @Test
    public void jobsJsonNames3() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/jobs/json/names/?projectId=5001&experimentId=7001").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.valueOf("application/json")));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);

        assertEquals(((Map) list.get(0)).get("name"), "--ALL--");
        assertEquals(((Map) list.get(0)).get("id"), "0");
        assertEquals(list.size(), 3);
    }

    @Test
    public void jobsJsonNames4() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/jobs/json/names/?projectId=9999&experimentId=9999").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.valueOf("application/json")));

        // Note that this gets all jobs as same as testJobsJsonNames2.
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);

        assertEquals(((Map) list.get(0)).get("name"), "--ALL--");
        assertEquals(((Map) list.get(0)).get("id"), "0");
        assertEquals(list.size(), 4);
    }

    @Test
    public void jobsJsonNames5() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/jobs/json/names/?projectId=5002&experimentId=0").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.valueOf("application/json")));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);

        assertEquals(((Map) list.get(0)).get("name"), "--ALL--");
        assertEquals(((Map) list.get(0)).get("id"), "0");
        assertEquals(list.size(), 2);
    }
}
