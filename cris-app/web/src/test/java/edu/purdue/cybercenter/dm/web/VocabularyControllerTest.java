package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.service.TermService;
import edu.purdue.cybercenter.dm.service.VocabularyService;
import edu.purdue.cybercenter.dm.service.CrisScriptEngine;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.xml.vocabulary.Vocabulary;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import javax.script.ScriptException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rangars
 */
public class VocabularyControllerTest extends BaseControllerTest {

    static final private String AdminUsername = "administrator";

    private final String VOCAB_FILES_DIR = "src/test/files/vocabulary_tests/";

    @Autowired
    private TermService termService;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private CrisScriptEngine crisScriptEngine;

    @BeforeClass
    public static void setupClass() throws Exception {
    }

    @Test
    public void emptyFileUploadShouldThrowError() throws Exception {
        createTestWorkspace();
        login(AdminUsername, "password");

        MockMultipartFile file = new MockMultipartFile("seed_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "empty_vocab.xml"));
        MockMultipartHttpServletRequestBuilder mockHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockHttpServletRequestBuilder.file(file);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());
        assertTrue(resultActions.andReturn().getResponse().getContentAsString().contains("No file name found in the request"));
    }

    @Test
    public void fileWithNoNewVocabularyMustNotBeSaved() throws Exception {
        createTestWorkspace();
        login(AdminUsername, "password");

        MockMultipartFile file = new MockMultipartFile("seed_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "seed_vocab.xml"));
        MockMultipartHttpServletRequestBuilder mockHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockHttpServletRequestBuilder.file(file);

        ResultActions resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        resultActions.andExpect(status().isOk());
        assertTrue(resultActions.andReturn().getResponse().getContentAsString().contains("All terms in the Vocabulary are already in the system. Vocabulary is not imported"));
    }

    @Test
    public void fileWithNewTermsForNewVocabAndVersionNumberIsImported() throws Exception {
        createTestWorkspace();
        login(AdminUsername, "password");

        MockMultipartFile mockMultipartFile = new MockMultipartFile("seed_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "seed_vocab.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/vocabularys").accept(MediaType.APPLICATION_JSON).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Integer vocabId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                vocabId = (Integer) item.get("id");
            }
        }

        mockHttpServletRequestBuilder = get("/vocabularys/export/" + vocabId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        File file = new File(VOCAB_FILES_DIR + "seed_vocab.xml");
        Vocabulary importedVocabulary = vocabularyService.convertXmlToVocabulary(FileUtils.readFileToString(file));
        Vocabulary exportedVocabulary = vocabularyService.convertXmlToVocabulary(content);

        checkResults(null, importedVocabulary, exportedVocabulary, true, null, 1);
    }

    @Test
    public void fileWithNewTermsForExistingVocabAndNewVersionNumberIsImported() throws Exception {
        createTestWorkspace();
        login(AdminUsername, "password");

        MockMultipartFile mockMultipartFile = new MockMultipartFile("seed_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "seed_vocab.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("updated_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "updated_vocab.xml"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/vocabularys").accept(MediaType.APPLICATION_JSON).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Integer vocabId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                vocabId = (Integer) item.get("id");
            }
        }

        mockHttpServletRequestBuilder = get("/vocabularys/export/" + vocabId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        File file = new File(VOCAB_FILES_DIR + "updated_vocab.xml");
        Vocabulary importedVocabulary = vocabularyService.convertXmlToVocabulary(FileUtils.readFileToString(file));
        Vocabulary exportedVocabulary = vocabularyService.convertXmlToVocabulary(content);

        checkResults(null, importedVocabulary, exportedVocabulary, true, null, -2);
    }

    @Test
    public void fileWithNewTermsForExistingVocabAndNoVersionNumberIsImported() throws Exception {
        createTestWorkspace();
        login(AdminUsername, "password");

        MockMultipartFile mockMultipartFile = new MockMultipartFile("seed_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "seed_vocab.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("updated_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "updated_vocab_no_version.xml"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/vocabularys").accept(MediaType.APPLICATION_JSON).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Integer vocabId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                vocabId = (Integer) item.get("id");
            }
        }

        mockHttpServletRequestBuilder = get("/vocabularys/export/" + vocabId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        File file = new File(VOCAB_FILES_DIR + "seed_vocab.xml");
        Vocabulary importedVocabulary = vocabularyService.convertXmlToVocabulary(FileUtils.readFileToString(file));
        Vocabulary exportedVocabulary = vocabularyService.convertXmlToVocabulary(content);

        checkResults(null, importedVocabulary, exportedVocabulary, false, "numeric", -1);
    }

    @Test
    public void fileWithModifiedTermsForExistingVocabAndSameVersionNumberIsImportedWithModifiedVersionNumber() throws Exception {
        createTestWorkspace();
        login(AdminUsername, "password");

        MockMultipartFile mockMultipartFile = new MockMultipartFile("seed_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "seed_vocab.xml"));
        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        ResultActions resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        mockMultipartFile = new MockMultipartFile("updated_vocab.xml", new FileInputStream(VOCAB_FILES_DIR + "modified_vocab.xml"));
        mockMultipartHttpServletRequestBuilder = (MockMultipartHttpServletRequestBuilder) fileUpload("/vocabularys/import").accept(MediaType.ALL).session(httpSession);
        mockMultipartHttpServletRequestBuilder.file(mockMultipartFile);

        resultActions = mockMvc.perform(mockMultipartHttpServletRequestBuilder);

        resultActions.andExpect(status().isOk());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get("/vocabularys").accept(MediaType.APPLICATION_JSON).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);

        String content = resultActions.andReturn().getResponse().getContentAsString();
        Integer vocabId = null;
        List<Map<String, Object>> list = Helper.deserialize(content, List.class);
        for (Map<String, Object> item : list) {
            if (item.containsKey("id")) {
                vocabId = (Integer) item.get("id");
            }
        }

        mockHttpServletRequestBuilder = get("/vocabularys/export/" + vocabId).session(httpSession);

        resultActions = mockMvc.perform(mockHttpServletRequestBuilder);
        content = resultActions.andReturn().getResponse().getContentAsString();
        File file = new File(VOCAB_FILES_DIR + "modified_vocab.xml");
        Vocabulary importedVocabulary = vocabularyService.convertXmlToVocabulary(FileUtils.readFileToString(file));
        Vocabulary exportedVocabulary = vocabularyService.convertXmlToVocabulary(content);

        checkResults(null, importedVocabulary, exportedVocabulary, false, null, 1);
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

    private void checkResults(Map<String, Object> init, Vocabulary expected, Vocabulary result, boolean sameVersion, String type, int deltaProperty) throws ScriptException {
        crisScriptEngine.createEngineScope(init, Helper.toMap(expected), Helper.toMap(result));

        String expectedName = crisScriptEngine.evaluateStringExpression("expected.name");
        String resultName = crisScriptEngine.evaluateStringExpression("result.name");
        String expectedVersion = crisScriptEngine.evaluateStringExpression("expected.version");
        String resultVersion = crisScriptEngine.evaluateStringExpression("result.version");
        String expectedType = crisScriptEngine.evaluateStringExpression("expected.terms.term[0].validation.validator[0].type");
        String resultType = crisScriptEngine.evaluateStringExpression("result.terms.term[0].validation.validator[0].type");
        Number expectedNumberOfTerms = crisScriptEngine.evaluateNumberExpression("expected.terms.term.length");
        Number resultNumberOfTerms = crisScriptEngine.evaluateNumberExpression("result.terms.term.length");
        Number expectedNumberOfProperties = crisScriptEngine.evaluateNumberExpression("expected.terms.term[0].validation.validator[0].property.length");
        Number resultNumberOfProperties = crisScriptEngine.evaluateNumberExpression("result.terms.term[0].validation.validator[0].property.length");
        assertEquals("vocabulary name", expectedName, resultName);
        if (sameVersion) {
            assertEquals("vocabulary version", expectedVersion, resultVersion);
        } else {
            assertNotEquals("vocabulary version", expectedVersion, resultVersion);
        }
        assertEquals("terms", expectedNumberOfTerms.intValue(), resultNumberOfTerms.intValue());
        if (StringUtils.isBlank(type)) {
            assertEquals("type of 1st term", expectedType, resultType);
        } else {
            assertEquals("type of 1st term", type, resultType);
        }
        assertEquals("properties", expectedNumberOfProperties.intValue() + deltaProperty, resultNumberOfProperties.intValue());
    }

}
