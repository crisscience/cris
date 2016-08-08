package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.service.DatasetService;
import edu.purdue.cybercenter.dm.service.DocumentService;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.util.SecurityHelper;
import edu.purdue.cybercenter.dm.util.TermName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
/**
 *
 * @author xu222
 */
public class RestControllerTest extends BaseWithAdminUserControllerTest {

    private static final String VALID_TEMPLATE_UUID = "85833b40-73d3-11e2-bcfd-0800200c9a66";
    private static final String INVALID_TEMPLATE_UUID = "85833b40-73d3-11e2-bcfd-0800200c9a99";
    private static final String VALID_OBJECT_ID = "512660295d032b861cb64b06";
    private static final String INVALID_OBJECT_ID = "999";
    private static final String REST_OBJECTUS_URL = "/rest/objectus";
    private static final String VALID_OBJECT_URL = REST_OBJECTUS_URL + "/" + VALID_TEMPLATE_UUID + "/" + VALID_OBJECT_ID;
    private static final String INVALID_OBJECT_URL = REST_OBJECTUS_URL + "/" + VALID_TEMPLATE_UUID + "/" + INVALID_OBJECT_ID;

    @Autowired
    private DatasetService datasetService;

    // List without template specified.
    @Test
    public void testFindWithoutTemplate() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(REST_OBJECTUS_URL).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isBadRequest());
        String sContent = resultActions.andReturn().getResponse().getContentAsString();
        assertEquals("Missing template name", sContent);
    }

    // Create or update without parameters.
    @Test
    public void testUpdateWithoutData() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.put(REST_OBJECTUS_URL).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isBadRequest());
        String sContent = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> content = Helper.deserialize(sContent, Map.class);
        String errorMessage = (String) content.get("message");
        assertTrue("No InputStream specified", errorMessage.startsWith("Required request body content is missing"));
    }

    // Update the existing record.
    @Test
    public void testUpdate() throws Exception {
        java.util.Random r = new java.util.Random();
        String val = "rand_update_" + r.nextInt(1000);
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.put(REST_OBJECTUS_URL).content("{'data':{'85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_id({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_name({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_owner({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_make({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_model({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_serialnum({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_description({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66._id({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':{$oid: \"512660295d032b861cb64b06\"}}}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = (Map<String, Object>) DatasetUtils.deserialize(contents);

        assertTrue((Boolean) map.get("isValid"));
        assertTrue(((ArrayList) map.get("messages")).isEmpty());
        String template = "85833b40-73d3-11e2-bcfd-0800200c9a66({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})";
        assertTrue(((HashMap) map.get("status")).containsKey(template));
        Map<String, Object> mapData = (HashMap) ((HashMap) map.get("data")).get(template);
        // TODO: figure out why it sometimes 1 and sometimes 1.0
        // assertEquals("tenant id", 1, mapData.get(MetaField.TenantId));
        assertEquals(mapData.get("hplc_name"), val);

        /* TODO: fix this later
        TermName termName = new TermName("85833b40-73d3-11e2-bcfd-0800200c9a66.HPLC Instrument Collection");
        UUID uuid = termName.getUuid();
        Map<String, Object> objects = (Map) ((List<Object>) ObjectusService.find(uuid, "{_id:{$oid:\"" + sObjectId + "\"}}", Boolean.TRUE)).get(0);
        String[] keys = new String[]{"hplc_name", "hplc_description", "hplc_owner", "hplc_serialnum", "hplc_make", "hplc_model", "hplc_id"};
        for (String key : keys) {
            assertEquals(objects.get(key), val);
        }
        */
    }

    // Creat new record.
    @Test
    public void testCreate() throws Exception {
        java.util.Random r = new java.util.Random();
        String val = "rand_new_" + r.nextInt(1000);

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(REST_OBJECTUS_URL).content("{'data':{'85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_id({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_name({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_owner({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_make({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_model({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_serialnum({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_description({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "'}}").accept(MediaType.APPLICATION_JSON).session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = (Map<String, Object>) DatasetUtils.deserialize(contents);

        assertTrue((Boolean) map.get("isValid"));
        assertTrue(((ArrayList) map.get("messages")).isEmpty());
        String template = "85833b40-73d3-11e2-bcfd-0800200c9a66({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})";
        assertTrue(((HashMap) map.get("status")).containsKey(template));
        Map<String, Object> mapData = (HashMap) ((HashMap) map.get("data")).get(template);
        assertEquals(mapData.get(MetaField.TenantId), 1);
        assertEquals(mapData.get("hplc_name"), val);

        TermName termName = new TermName("85833b40-73d3-11e2-bcfd-0800200c9a66.HPLC Instrument Collection");
        UUID uuid = termName.getUuid();
        Map query = (Map) DatasetUtils.deserialize("{hplc_id:\"" + val + "\"}");
        SecurityHelper.setAuthentication(1); // admin user
        Map<String, Object> aggregators = new HashMap<>();
        aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
        Map<String, Object> object = (Map) (datasetService.find(uuid, aggregators)).get(0);
        String[] keys = new String[]{"hplc_name", "hplc_description", "hplc_owner", "hplc_serialnum", "hplc_make", "hplc_model", "hplc_id"};
        for (String key : keys) {
            assertEquals(object.get(key), val);
        }
    }

    // Delete without parameters.
    @Test
    public void testDeleteWithoutTemplate() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(REST_OBJECTUS_URL).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isBadRequest());
        String sContent = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> content = Helper.deserialize(sContent, Map.class);
        String errorMessage = (String) content.get("message");
        assertEquals("Request method 'DELETE' not supported", errorMessage);
    }

    // Try to delete a record that does not exist.
    @Test
    public void testDeleteWithInvalidObjectId() throws Exception {
        // 999 should not a record id.
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(INVALID_OBJECT_URL).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isBadRequest());
        String sContent = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> content = Helper.deserialize(sContent, Map.class);
        String errorMessage = (String) content.get("message");
        assertEquals("invalid ObjectId [999]", errorMessage);
    }

    // Delete and create a record.
    // TODO: fix this later
    //@Test
    public void restObjectus7() throws Exception {
        java.util.Random r = new java.util.Random();

        // Assure that the record exists.
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(VALID_OBJECT_URL).accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, Object> map = (Map<String, Object>) DatasetUtils.deserialize(contents);
        assertTrue("Number of fields must not be zero", map.size() > 0);

        // Delete the record.
        mockHttpServletRequestBuilder = MockMvcRequestBuilders.delete(VALID_OBJECT_URL).accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        contents = resultActions.andReturn().getResponse().getContentAsString();

        // Check the received data.
        map = (Map<String, Object>) DatasetUtils.deserialize(contents);
        assertTrue((Boolean) map.get("isValid"));
        assertEquals(map.get("messages"), "");
        assertNull(map.get("status"));

        // Assure that the record is not presented on the database.
        mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(VALID_OBJECT_URL).accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        contents = resultActions.andReturn().getResponse().getContentAsString();
        map = (Map<String, Object>) DatasetUtils.deserialize(contents);
        assertEquals("Object ID", null, map.get("_id"));

        // Insert the record again.
        String val = "rand_update_" + r.nextInt(1000);
        mockHttpServletRequestBuilder = MockMvcRequestBuilders.put(VALID_OBJECT_URL).content("{'data':{'85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_id({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_name({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_owner({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_make({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_model({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_serialnum({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66.hplc_description({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':'" + val + "','85833b40-73d3-11e2-bcfd-0800200c9a66._id({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})':{'class':'org.bson.types.ObjectId','inc':481708806,'machine':1560488838,'new':false,'time':1361469481000,'timeSecond':1361469481}}}").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        contents = resultActions.andReturn().getResponse().getContentAsString();

        // Check the received data.
        map = (Map<String, Object>) DatasetUtils.deserialize(contents);
        assertTrue((Boolean) map.get("isValid"));
        assertTrue(((ArrayList) map.get("messages")).isEmpty());
        String template = "85833b40-73d3-11e2-bcfd-0800200c9a66({\"_template_version\":\"167822c0-c85a-11e2-8b8b-0800200c9a66\"})";
        assertTrue(((HashMap) ((ArrayList) map.get("status")).get(0)).containsKey(template));
        Map<String, Object> mapData = (HashMap) ((HashMap) ((ArrayList) map.get("data")).get(0)).get(template);
        assertEquals(mapData.get(MetaField.TenantId), 1);
        assertEquals(mapData.get("hplc_name"), val);

        TermName termName = new TermName(VALID_TEMPLATE_UUID + ".HPLC Instrument Collection");
        UUID uuid = termName.getUuid();
        Map query = (Map) DatasetUtils.deserialize("{_id:{$oid:\"" + VALID_OBJECT_ID + "\"}}");
        Map<String, Object> aggregators = new HashMap<>();
        aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
        Map<String, Object> objects = (Map) (datasetService.find(uuid, aggregators)).get(0);
        String[] keys = new String[]{"hplc_name", "hplc_description", "hplc_owner", "hplc_serialnum", "hplc_make", "hplc_model", "hplc_id"};
        for (String key : keys) {
            assertEquals(objects.get(key), val);
        }
    }
}
