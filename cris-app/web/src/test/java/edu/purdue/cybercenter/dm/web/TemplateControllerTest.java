package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.util.Helper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TemplateControllerTest extends BaseWithAdminUserControllerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Test
    public void templatesJsonNames1() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/names").accept(MediaType.APPLICATION_JSON).session(httpSession);

        List<Map> expectedContents = Helper.deserialize("[{\"id\":\"85833b40-73d3-11e2-bcfd-0800200c9a66\",\"name\":\"HPLC Instrument Collection 2\",\"version\":\"bf7b1cd0-cfab-11e2-8b8b-0800200c9a66\"}]", List.class);//{\"id\":\"305b0f27-e829-424e-84eb-7a8a9ed93e28\",\"name\":\"GLB\",\"version\":\"db719406-f665-45cb-a8fb-985b6082b654\"}
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        List<Map> contents = Helper.deserialize(resultActions.andReturn().getResponse().getContentAsString(), List.class);
        assertTrue(contents.equals(expectedContents));
    }

    @Test
    public void templatesJsonNames2() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = get("/templates/json/names/?projectId=5001&experimentId=0&jobId=0&showAll=false").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions1 = mockMvc.perform(mockHttpServletRequestBuilder1);
        resultActions1.andExpect(status().isOk());
        resultActions1.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents1 = resultActions1.andReturn().getResponse().getContentAsString();

        List list1 = Helper.deserialize(contents1, List.class);
        assertEquals("number of templates", 1, list1.size());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder2 = get("/templates/json/names/?projectId=5001&experimentId=0&jobId=0&showAll=true").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions2 = mockMvc.perform(mockHttpServletRequestBuilder2);
        resultActions2.andExpect(status().isOk());
        resultActions2.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents2 = resultActions2.andReturn().getResponse().getContentAsString();
        List list2 = Helper.deserialize(contents2, List.class);
        assertEquals(2, list2.size());
    }

    @Test
    public void templatesJsonNames3() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = get("/templates/json/names/?projectId=0&experimentId=0&jobId=0&name=*&showAll=false").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder1);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        String contents1 = resultActions.andReturn().getResponse().getContentAsString();
        List list2 = Helper.deserialize(contents1, List.class);
        assertEquals(1, list2.size());
    }

    @Test
    public void templatesJsonNames4() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/names/?projectId=25&experimentId=0&jobId=0&name=*&showAll=false").accept(MediaType.APPLICATION_JSON).session(httpSession);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        assertEquals("[]", contents);
    }

    @Test
    public void templatesJsonDataset1() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/dataset").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List listContents = Helper.deserialize(contents, List.class);
        assertFalse("Dataset is empty", listContents.isEmpty());
    }

    @Test
    public void templatesJsonDataset2() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/dataset/?experimentId=0&jobId=0&projectId=0&templateUuid=85833b40-73d3-11e2-bcfd-0800200c9a66").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);

        for (int i = 0; i < list.size(); ++i) {
            int size = ((Map) list.get(i)).size();
            assertTrue(size >= 16 && size <= 22);
        }
    }

    @Test
    public void templatesJsonLayout1() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/layout").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List listContents = Helper.deserialize(contents, List.class);
        assertEquals(1, listContents.size());
    }

    @Test
    public void templatesJsonLayout2() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/layout/?experimentId=0&jobId=0&projectId=0&templateUuid=85833b40-73d3-11e2-bcfd-0800200c9a66").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);
        List listSub = ((Map<String, List>) list.get(0)).get("cells");

        assertEquals(MetaField.ProjectId, ((Map<String, String>) listSub.get(0)).get("field"));
        assertEquals(MetaField.ExperimentId, ((Map<String, String>) listSub.get(1)).get("field"));
        assertEquals(MetaField.JobId, ((Map<String, String>) listSub.get(2)).get("field"));
        Set<String> fields = new HashSet<>();
        for (int i = 3; i < 3 + 7; ++i) {
            fields.add(((Map<String, String>) listSub.get(i)).get("field"));
        }
        for (String field : new String[]{"hplc_id", "hplc_name", "hplc_owner", "hplc_make", "hplc_model", "hplc_serialnum", "hplc_description"}) {
            assertTrue(fields.contains(field));
        }
        assertEquals(13 + 3, listSub.size());
    }

    @Test
    public void templatesJsonLayout3() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/layout/?experimentId=0&jobId=0&projectId=999&templateUuid=85833b40-73d3-11e2-bcfd-0800200c9a66").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);
        List listSub = ((Map<String, List>) list.get(0)).get("cells");

        assertEquals(MetaField.ExperimentId, ((Map<String, String>) listSub.get(0)).get("field"));
        assertEquals(MetaField.JobId, ((Map<String, String>) listSub.get(1)).get("field"));
        Set<String> fields = new HashSet<>();
        for (int i = 2; i < 2 + 7; ++i) {
            fields.add(((Map<String, String>) listSub.get(i)).get("field"));
        }
        for (String field : new String[]{"hplc_id", "hplc_name", "hplc_owner", "hplc_make", "hplc_model", "hplc_serialnum", "hplc_description"}) {
            assertTrue(fields.contains(field));
        }
        assertEquals(13 + 2, listSub.size());
    }

    @Test
    public void templatesJsonLayout4() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/layout/?experimentId=999&jobId=0&projectId=0&templateUuid=85833b40-73d3-11e2-bcfd-0800200c9a66").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);
        List listSub = ((Map<String, List>) list.get(0)).get("cells");

        assertEquals(MetaField.ProjectId, ((Map<String, String>) listSub.get(0)).get("field"));
        assertEquals(MetaField.JobId, ((Map<String, String>) listSub.get(1)).get("field"));
        Set<String> fields = new HashSet<>();
        for (int i = 2; i < 2 + 7; ++i) {
            fields.add(((Map<String, String>) listSub.get(i)).get("field"));
        }
        for (String field : new String[]{"hplc_id", "hplc_name", "hplc_owner", "hplc_make", "hplc_model", "hplc_serialnum", "hplc_description"}) {
            assertTrue(fields.contains(field));
        }
        assertEquals(13 + 2, listSub.size());
    }

    @Test
    public void templatesJsonLayout5() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates/json/layout/?experimentId=999&jobId=999&projectId=999&templateUuid=85833b40-73d3-11e2-bcfd-0800200c9a66").accept(MediaType.APPLICATION_JSON).session(httpSession);
        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String contents = resultActions.andReturn().getResponse().getContentAsString();
        List list = Helper.deserialize(contents, List.class);
        List listSub = ((Map<String, List>) list.get(0)).get("cells");

        assertFalse(((Map<String, String>) listSub.get(0)).get("field").equals("project_id"));
        assertFalse(((Map<String, String>) listSub.get(0)).get("field").equals("experiment_id"));
        assertFalse(((Map<String, String>) listSub.get(0)).get("field").equals("job_id"));
        Set<String> fields = new HashSet<>();
        for (int i = 0; i < 0 + 7; ++i) {
            fields.add(((Map<String, String>) listSub.get(i)).get("field"));
        }
        for (String field : new String[]{"hplc_id", "hplc_name", "hplc_owner", "hplc_make", "hplc_model", "hplc_serialnum", "hplc_description"}) {
            assertTrue(fields.contains(field));
        }
        assertEquals(13 + 0, listSub.size());
    }
}
