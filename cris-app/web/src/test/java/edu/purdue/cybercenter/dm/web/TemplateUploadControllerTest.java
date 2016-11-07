package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.service.TermService;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TemplateUploadControllerTest extends BaseControllerTest {

    static final private String ADMIN_USERNAME = "administrator";

    private final String TEMPLATE_FILES_DIR = "src/test/files/template_tests/";

    @Autowired
    private TermService termService;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Test
    public void emptyFileShouldThrowError() throws Exception {
        createTestWorkspace();
        login(ADMIN_USERNAME, "password");

        MockMultipartFile file = new MockMultipartFile("empty_template.xml", new FileInputStream(TEMPLATE_FILES_DIR + "empty_template.xml"));
        MockMultipartHttpServletRequestBuilder mockHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/templates/import").accept(MediaType.ALL).session(httpSession);
        mockHttpServletRequestBuilder.file(file);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        assertTrue(resultActions.andReturn().getResponse().getContentAsString().contains("No file name found in the request"));
    }

    @Test
    public void fileWithSameTemplateDefinitionMustNotBeSavedButTimestampIsUpdated() throws Exception {
        createTestWorkspace();
        login(ADMIN_USERNAME, "password");

        uploadVocabulary("seed_vocab.xml");

        ResultActions resultActions = uploadTemplate("seed_template.xml");
        resultActions.andExpect(status().isOk());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates").accept(MediaType.APPLICATION_JSON).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String sContent = resultActions.andReturn().getResponse().getContentAsString();
        Integer templateId = null;
        String timeUpdated = null;
        String timeCreated = null, version = null, uuid = null, description = null;
        List<Map<String, Object>> list1 = Helper.deserialize(sContent, List.class);
        for (Map<String, Object> item : list1) {
            if (item.containsKey("id")) {
                templateId = (Integer) item.get("id");
            }
            if (item.containsKey("timeUpdated")) {
                timeUpdated = item.get("timeUpdated").toString();
            }
            if (item.containsKey("timeCreated")) {
                timeCreated = item.get("timeCreated").toString();
            }
            if (item.containsKey("versionNumber")) {
                version = item.get("versionNumber").toString();
            }
            if (item.containsKey("uuid")) {
                uuid = item.get("uuid").toString();
            }
            if (item.containsKey("description")) {
                description = item.get("description").toString();
            }
        }

        mockHttpServletRequestBuilder = get("/templates/export/" + templateId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        String oldContent = resultActions.andReturn().getResponse().getContentAsString();

        resultActions = uploadTemplate("seed_template_new.xml");
        resultActions.andExpect(status().isOk());

        mockHttpServletRequestBuilder = get("/templates").accept(MediaType.APPLICATION_JSON).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        sContent = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> list2 = Helper.deserialize(sContent, List.class);
        assertEquals(1, list2.size());

        resultActions = uploadTemplate("seed_template.xml");
        resultActions.andExpect(status().isOk());

        mockHttpServletRequestBuilder = get("/templates/" + templateId).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        sContent = resultActions.andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> list3 = Helper.deserialize(sContent, List.class);
        assertEquals(1, list3.size());
        String timeUpdated2 = null;
        String timeCreated2 = null;
        String version2 = null;
        String uuid2 = null;
        String description2 = null;
        assertEquals(1, list3.size());
        for (Map<String, Object> item : list3) {
            if (item.containsKey("timeUpdated")) {
                timeUpdated2 = item.get("timeUpdated").toString();
            }
            if (item.containsKey("timeCreated")) {
                timeCreated2 = item.get("timeCreated").toString();
            }
            if (item.containsKey("versionNumber")) {
                version2 = item.get("versionNumber").toString();
            }
            if (item.containsKey("uuid")) {
                uuid2 = item.get("uuid").toString();
            }
            if (item.containsKey("description")) {
                description2 = item.get("description").toString();
            }
        }

        assertFalse(timeUpdated2.equals(timeUpdated));
        assertTrue(timeCreated2.equals(timeCreated));
        assertTrue(version2.equals(version));
        assertTrue(uuid2.equals(uuid));
        assertTrue(description2.equals(description));
    }

    @Test
    public void fileWithDifferentTemplateDefinitionMustBeSavedWithUpdatedVersionNumber() throws Exception {
        createTestWorkspace();
        login(ADMIN_USERNAME, "password");

        uploadVocabulary("seed_vocab.xml");

        ResultActions resultActions = uploadTemplate("seed_template.xml");
        resultActions.andExpect(status().isOk());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/templates").accept(MediaType.APPLICATION_JSON).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String sContent = resultActions.andReturn().getResponse().getContentAsString();
        Integer templateId = null;
        List<Map<String, Object>> list1 = Helper.deserialize(sContent, List.class);
        for (Map<String, Object> item : list1) {
            if (item.containsKey("id")) {
                templateId = (Integer) item.get("id");
            }
        }

        mockHttpServletRequestBuilder = get("/templates/export/" + templateId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        String oldContent = resultActions.andReturn().getResponse().getContentAsString();
        Term importedTerm = termService.convertXmlToTerm(oldContent);

        resultActions = uploadTemplate("seed_template_new.xml");
        resultActions.andExpect(status().isOk());

        mockHttpServletRequestBuilder = get("/templates").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        sContent = resultActions.andReturn().getResponse().getContentAsString();
        Integer newTemplateId = null;
        List<Map<String, Object>> list2 = Helper.deserialize(sContent, List.class);
        for (Map<String, Object> item : list2) {
            if (item.containsKey("id")) {
                newTemplateId = (Integer) item.get("id");
            }
        }

        assertFalse(templateId.equals(newTemplateId));

        mockHttpServletRequestBuilder = get("/templates/export/" + newTemplateId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        sContent = resultActions.andReturn().getResponse().getContentAsString();
        Term updatedTerm = termService.convertXmlToTerm(sContent);
        assertFalse(updatedTerm.getVersion().equals(importedTerm.getVersion()));
        assertTrue(updatedTerm.getUuid().equals(importedTerm.getUuid()));
        assertFalse(termService.isTermInTerms(importedTerm, Arrays.asList(updatedTerm)));
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

    private void uploadVocabulary(String fileName) throws Exception{
        MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, new FileInputStream(TEMPLATE_FILES_DIR + fileName));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);
        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder1 = get("/vocabularys").accept(MediaType.APPLICATION_JSON).session(httpSession);
        resultActions = mockMvc.perform(mockHttpServletRequestBuilder1);
        String content1 = resultActions.andReturn().getResponse().getContentAsString();
        Integer vocabId = null;
        List<Map<String, Object>> list1 = Helper.deserialize(content1, List.class);
        for (Map<String, Object> item : list1) {
            if (item.containsKey("id")) {
                vocabId = (Integer) item.get("id");
            }
        }
        mockHttpServletRequestBuilder1 = get("/vocabularys/export/" + vocabId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder1);
    }

    private ResultActions uploadTemplate(String fileName) throws Exception{
        MockMultipartFile file = new MockMultipartFile(fileName, new FileInputStream(TEMPLATE_FILES_DIR + fileName));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/templates/import?force=true").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(file);
        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);
        return resultActions;
    }
}
