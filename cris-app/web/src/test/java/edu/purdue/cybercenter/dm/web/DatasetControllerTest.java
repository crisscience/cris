package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.threadlocal.TenantId;
import edu.purdue.cybercenter.dm.threadlocal.UserId;
import edu.purdue.cybercenter.dm.util.Helper;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
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
public class DatasetControllerTest extends BaseWithAdminUserControllerTest {

    @Test
    public void objectusIndex() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/objectus/index").accept(MediaType.TEXT_HTML).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
    }

    /* These two tests are no longer valid. To be removed in the future
    @Test
    public void objectusHtml1() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/objectus/html").accept(MediaType.TEXT_HTML).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        assertEquals(contents, "Invalid template");
    }

    @Test
    public void objectusHtml2() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/objectus/html/?uuid=85833b40-73d3-11e2-bcfd-0800200c9a66&version=167822c0-c85a-11e2-8b8b-0800200c9a66").accept(MediaType.TEXT_HTML).session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        assertFalse(((String) contents).equals(""));
        assertTrue(((String) contents).startsWith("<div id="));
    }
    */

    @Test
    public void save() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post("/rest/objectus/").accept(MediaType.APPLICATION_JSON).session(httpSession);
        mockHttpServletRequestBuilder.contentType(MediaType.APPLICATION_JSON);
        Map<String, Object> data = new HashMap<>();
        data.put("85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_id({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})", "1");
        data.put("85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_name({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})", "2");
        data.put("85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_owner({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})", "3");
        data.put("85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_make({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})", "4");
        data.put("85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_model({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})", "5");
        data.put("85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_serialnum({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})", "6");
        data.put("85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_description({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})", "7");
        Map<String, Object> content = new HashMap<>();
        content.put("projectId", "5001");
        content.put("experimentId", "7001");
        content.put("data", data);
        mockHttpServletRequestBuilder.content(Helper.serialize(content));

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        String responseContent = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> responseJson = Helper.deserialize(responseContent, Map.class);
        assertNotNull(responseJson);
        Map<String, Object> responseData = (Map<String, Object>) responseJson.get("data");
        assertNotNull(responseData);
        Map<String, Object> responseValue = (Map<String, Object>) responseData.get("85833b40-73d3-11e2-bcfd-0800200c9a66({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})");
        assertNotNull(responseValue);
        assertNotNull(MetaField.Id, responseValue.get(MetaField.Id));
        assertNotNull(MetaField.ContextId, responseValue.get(MetaField.ContextId));
        assertEquals(MetaField.TenantId, TenantId.get(), responseValue.get(MetaField.TenantId));
        assertEquals(MetaField.CreatorId, UserId.get(), responseValue.get(MetaField.CreatorId));
        assertEquals(MetaField.UpdaterId, UserId.get(), responseValue.get(MetaField.UpdaterId));
        assertNotNull(MetaField.TimeCreated, responseValue.get(MetaField.TimeCreated));
        assertNotNull(MetaField.TimeUpdated, responseValue.get(MetaField.TimeUpdated));
        assertEquals(MetaField.TemplateVersion, "167822c0-c85a-11e2-8b8b-0800200c9a66", ((Map) responseValue.get(MetaField.TemplateVersion)).get("$uuid"));
        assertEquals(MetaField.ProjectId, 5001, responseValue.get(MetaField.ProjectId));
        assertEquals(MetaField.ExperimentId, 7001, responseValue.get(MetaField.ExperimentId));
        assertNull(MetaField.JobId, responseValue.get(MetaField.JobId));
        assertNull(MetaField.TaskId, responseValue.get(MetaField.TaskId));
        assertEquals("hplc_id", "1", responseValue.get("hplc_id"));
        assertEquals("hplc_name", "2", responseValue.get("hplc_name"));
        assertEquals("hplc_owner", "3", responseValue.get("hplc_owner"));
        assertEquals("hplc_make", "4", responseValue.get("hplc_make"));
        assertEquals("hplc_model", "5", responseValue.get("hplc_model"));
        assertEquals("hplc_serialnum", "6", responseValue.get("hplc_serialnum"));
        assertEquals("hplc_description", "7", responseValue.get("hplc_description"));
    }
}
