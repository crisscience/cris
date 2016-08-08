/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import edu.purdue.cybercenter.dm.domain.Tenant;
import edu.purdue.cybercenter.dm.repository.TenantRepository;
import edu.purdue.cybercenter.dm.service.CrisScriptEngine;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.util.ServiceUtils;
import edu.purdue.cybercenter.dm.web.filter.RequestFilter;
import edu.purdue.cybercenter.dm.web.filter.SessionFilter;
import edu.purdue.cybercenter.dm.web.filter.WorkflowAsSessionFilter;
import edu.purdue.cybercenter.dm.web.listener.ContextListener;
import edu.purdue.cybercenter.dm.web.listener.SessionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSessionEvent;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import org.springframework.test.web.servlet.result.CookieResultMatchers;
import org.springframework.test.web.servlet.result.HeaderResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author xu222
 */
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(locations = {
        "file:src/main/resources/META-INF/spring/applicationContext.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-security.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-database.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-activiti.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-cache.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-jms.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-mail.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-task.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-mongo.xml"}),
    @ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/spring/webmvc-config.xml",
        "file:src/test/webapp/WEB-INF/spring/applicationContext-filters.xml"
    })
})
@Transactional
public abstract class BaseControllerTestNg extends AbstractTransactionalTestNGSpringContextTests {

    private static final String TEST_FILE_DIR = "src/test/files/";
    private static final String TEST_DATA_DIR = TEST_FILE_DIR + "test_data/";
    private static final String TEST_CASE_EXCEL_SHEET = "testData";
    private static final String TEST_BASE_NAME = "base";
    private static final String TEST_DATA_POSTGRESQL_SUFFIX = "Data.xml";
    private static final String TEST_DATA_MONGODB_SUFFIX = "Data.json";
    private static final String TEST_SUITE_FILE_SUFFIX = "Tests.xls";

    private static final String TOP_CONTEXT_INIT = "init";
    private static final String TOP_CONTEXT_REQUEST = "request";
    private static final String TOP_CONTEXT_RESPONSE = "response";
    private static final String TOP_CONTEXT_RESULT = "result"; // the same as response.content for easy access
    private static final String TOP_CONTEXT_EXPECTED = "expected";

    private static final String KEY_STATUS_CODE = "statusCode";
    private static final String KEY_REASON_PHRASE = "reasonPhrase";
    private static final String KEY_REDIRECTED_URL = "redirectedUrl";
    private static final String KEY_HEADERS = "headers";
    private static final String KEY_COOKIES = "cookies";
    private static final String KEY_CHARACTER_ENCODING = "characterEncoding";
    private static final String KEY_LOCALE = "locale";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final String KEY_CONTENT_LENGTH = "contentLength";
    private static final String KEY_CONTENT_AS_STRING = "contentAsString";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_FILENAME = "filename";

    private static final String EXPRESSION_PREFIX = "${";
    private static final String EXPRESSION_SUFFIX = "}";

    private static final String ASSERT_MESSAGE_TEMPLATE = "Row: %s, Test Case: %s, Step: %d: %s";

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private MockHttpSession httpSession;
    @Autowired
    private MockServletContext servletContext;
    @Autowired
    private OpenEntityManagerInViewFilter openEntityManagerInViewFilter;
    @Autowired
    private HiddenHttpMethodFilter hiddenHttpMethodFilter;
    @Autowired
    private CharacterEncodingFilter characterEncodingFilter;
    @Autowired
    private SessionFilter sessionFilter;
    @Autowired
    private WorkflowAsSessionFilter workflowAsSessionFilter;
    @Autowired
    private FilterChainProxy springSecurityFilterChain;
    @Autowired
    private RequestFilter requestFilter;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private CrisScriptEngine crisScriptEngine;

    private static DataSource dataSource;
    private static MongoTemplate mongoTemplate;
    private String moduleName;
    private MockMvc mockMvc;

    protected final void setModuleName() {
        setModuleName(null);
    }

    protected final void setModuleName(String moduleName) {
        if (StringUtils.isNotBlank(moduleName)) {
            this.moduleName = moduleName;
        } else {
            String className = this.getClass().getSimpleName();
            int idx = className.indexOf("ControllerTest");
            this.moduleName = StringUtils.uncapitalize(className.substring(0, idx));
        }
    }

    @BeforeSuite
    public void setUpSuite() throws Exception {
        // postgresql data source
        DataSource ds = buildDataSource();
        dataSource = ds;

        // mongodb data source
        MongoClient mongo = new MongoClient("localhost", 27017);
        mongoTemplate = new MongoTemplate(mongo, "test");

        // load base test data
        loadData(TEST_BASE_NAME);
    }

    @AfterSuite
    public void tearDownSuite() throws Exception {
        // clean up base test data
        clearData(TEST_BASE_NAME);
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        // load per module test data
        loadData(moduleName);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(openEntityManagerInViewFilter, "/*")
                .addFilter(hiddenHttpMethodFilter, "/*")
                .addFilter(characterEncodingFilter, "/*")
                .addFilter(sessionFilter, "/*")
                .addFilter(workflowAsSessionFilter, "/*")
                .addFilter(springSecurityFilterChain, "/*")
                .addFilter(requestFilter, "/*")
                .build();

        this.servletContext.setContextPath("/");

        ServletContextEvent sce = new ServletContextEvent(this.servletContext);
        ContextListener contextListener = new ContextListener();
        contextListener.contextInitialized(sce);
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        ServletContextEvent sce = new ServletContextEvent(this.servletContext);
        ContextListener contextListener = new ContextListener();
        contextListener.contextDestroyed(sce);

        // clean up per module test data
        clearData(moduleName);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        //TODO: perform per test case actions
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        //TODO: remove per test case actions
    }

    @DataProvider(name = "CRIS_TESTDATA")
    public Object[][] createTestSuite() throws Exception {
        return getTestSuite(moduleName + TEST_SUITE_FILE_SUFFIX);
    }

    @Test(dataProvider = "CRIS_TESTDATA")
    public void runTestSuite(String testCaseName, Map testCase) throws Exception {
        testSuite(testCaseName, testCase);
    }

    /*
     * utility methods
     */
    private Integer getTenantId() {
        return (Integer) httpSession.getAttribute("tenantId");
    }

    private void setTenantId(int tenantId) {
        httpSession = new MockHttpSession(servletContext, UUID.randomUUID().toString());

        HttpSessionEvent hse = new HttpSessionEvent(httpSession);
        SessionListener sessionListener = new SessionListener();
        sessionListener.sessionCreated(hse);
        httpSession.setAttribute("tenantId", tenantId);
    }

    private void setupWorkspace(String tenantUrlIdentifier) {
        Tenant tenant = tenantRepository.findByUrlIdentifier(tenantUrlIdentifier);
        setTenantId(tenant.getId());
    }

    private void login(String username, String password) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post("/auth/verify").param("username", username).param("password", password).session(httpSession);
        ResultActions auth = this.mockMvc.perform(mockHttpServletRequestBuilder);
        auth.andExpect(redirectedUrl("/"));
    }

    private void logout() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get("/auth/signout").session(httpSession);
        ResultActions auth = this.mockMvc.perform(mockHttpServletRequestBuilder);
        auth.andExpect(redirectedUrl("/"));
    }

    /*
     * Convenience methods
     */
    private void setupDefaultWorkspace() {
        setTenantId(1);
    }

    private void loginDefaultAdminUser() throws Exception {
        if (getTenantId() == null) {
            setTenantId(1);
        }
        login("george.washington", "1234");
    }

    private void loginDefaultNormalUser() throws Exception {
        if (getTenantId() == null) {
            setTenantId(1);
        }
        login("john.adams", "1234");
    }

    private void loginDefaultDenyAllUser() throws Exception {
        if (getTenantId() == null) {
            setTenantId(1);
        }
        login("abraham.lincoln", "1234");
    }

    private void loginPublicUser() throws Exception {
        if (getTenantId() == null) {
            setTenantId(1);
        }
        login("public", "d41d62b0-3cbc-11e2-a25f-0800200c9a66");
    }

    private void loadData(String moduleName) {
        String filePostgreSqlData = TEST_DATA_DIR + moduleName + "/" + moduleName + TEST_DATA_POSTGRESQL_SUFFIX;
        String fileMongoDbData = TEST_DATA_DIR + moduleName + "/" + moduleName + TEST_DATA_MONGODB_SUFFIX;
        Path pathPostgreSqlData = FileSystems.getDefault().getPath(".", filePostgreSqlData);
        Path pathMongoDbData = FileSystems.getDefault().getPath(".", fileMongoDbData);
        if (Files.exists(pathPostgreSqlData)) {
            loadPostgreSqlData(moduleName);
        }
        if (Files.exists(pathMongoDbData)) {
            loadMongoDbData(moduleName);
        }
    }

    private void clearData(String moduleName) {
        String filePostgreSqlData = TEST_DATA_DIR + moduleName + "/" + moduleName + TEST_DATA_POSTGRESQL_SUFFIX;
        String fileMongoDbData = TEST_DATA_DIR + moduleName + "/" + moduleName + TEST_DATA_MONGODB_SUFFIX;
        Path pathPostgreSqlData = FileSystems.getDefault().getPath(".", filePostgreSqlData);
        Path pathMongoDbData = FileSystems.getDefault().getPath(".", fileMongoDbData);
        if (Files.exists(pathMongoDbData)) {
            clearMongoDbData(moduleName);
        }
        if (Files.exists(pathPostgreSqlData)) {
            clearPostgreSqlData(moduleName);
        }
    }

    private DefaultDataSet toDefaultDataSet(IDataSet dataset) throws AmbiguousTableNameException, DataSetException {
        ITable[] tables = new ITable[dataset.getTableNames().length];
        ITableIterator it = dataset.iterator();
        int idx = 0;
        while (it.next()) {
            tables[idx++] = it.getTable();
        }
        DefaultDataSet defaultDataset = new DefaultDataSet(tables);
        return defaultDataset;
    }

    private void loadPostgreSqlData(String moduleName) {
        String filePostgreSqlData = TEST_DATA_DIR + moduleName + "/" + moduleName + TEST_DATA_POSTGRESQL_SUFFIX;
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // load PostgreSql data
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            IDatabaseConnection dbUnitConnection;
            IDataSet dataset;
            dbUnitConnection = new DatabaseConnection(connection);
            dataset = new FlatXmlDataSet(new FileInputStream(filePostgreSqlData));

            DefaultDataSet defaultDataset = toDefaultDataSet(dataset);

            try {
                // place and update storage files
                String sourceStorageLocation = TEST_DATA_DIR + moduleName + "/";
                String targetStorageLocation = jdbcTemplate.queryForObject("SELECT location FROM storage", String.class);

                DefaultTable storageTable = (DefaultTable) defaultDataset.getTable("storage_file");
                int rows = storageTable.getRowCount();
                for (int i = 0; i < rows; i++) {
                    int id = Integer.parseInt((String) storageTable.getValue(i, "id"));
                    String tenantId = (String) storageTable.getValue(i, "tenant_id");
                    UUID tenantUuid = jdbcTemplate.queryForObject("SELECT uuid FROM tenant WHERE id = " + tenantId, UUID.class);

                    String source = (String) storageTable.getValue(i, "source");
                    Path sourcePath = FileSystems.getDefault().getPath(sourceStorageLocation + source);
                    String target = tenantUuid.toString() + "/" + ServiceUtils.makeFilePath(id, sourcePath.getFileName().toString());
                    Path targetPath = FileSystems.getDefault().getPath(targetStorageLocation + target);
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    storageTable.setValue(i, "file_name", sourcePath.getFileName().toString());
                    storageTable.setValue(i, "location", target);
                }
            } catch (NoSuchTableException ex) {
                // no storage_file table is fine
            }

            DatabaseOperation.REFRESH.execute(dbUnitConnection, defaultDataset);
        } catch (DatabaseUnitException | SQLException | IOException ex) {
            Assert.fail("failed to load data: " + filePostgreSqlData + ": " + ex.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void clearPostgreSqlData(String moduleName) {
        String filePostgreSqlData = TEST_DATA_DIR + moduleName + "/" + moduleName + TEST_DATA_POSTGRESQL_SUFFIX;

        // remove PostgreSql data
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            IDatabaseConnection dbUnitConnection;
            IDataSet dataset;
            dbUnitConnection = new DatabaseConnection(connection);
            dataset = new FlatXmlDataSet(new FileInputStream(filePostgreSqlData));

            DefaultDataSet defaultDataset = toDefaultDataSet(dataset);

            // remove storage files
            try {
                // place and update storage files
                String sourceStorageLocation = TEST_DATA_DIR + moduleName + "/";
                String targetStorageLocation = jdbcTemplate.queryForObject("SELECT location FROM storage", String.class);

                DefaultTable storageTable = (DefaultTable) defaultDataset.getTable("storage_file");
                int rows = storageTable.getRowCount();
                for (int i = 0; i < rows; i++) {
                    int id = Integer.parseInt((String) storageTable.getValue(i, "id"));
                    String tenantId = (String) storageTable.getValue(i, "tenant_id");
                    UUID tenantUuid = jdbcTemplate.queryForObject("SELECT uuid FROM tenant WHERE id = " + tenantId, UUID.class);

                    String source = (String) storageTable.getValue(i, "source");
                    Path sourcePath = FileSystems.getDefault().getPath(sourceStorageLocation + source);
                    String target = tenantUuid.toString() + "/" + ServiceUtils.makeFilePath(id, sourcePath.getFileName().toString());
                    Path targetPath = FileSystems.getDefault().getPath(targetStorageLocation + target);
                    Files.delete(targetPath);
                }
            } catch (NoSuchTableException ex) {
                // no storage_file table is fine
            }

            DatabaseOperation.DELETE.execute(dbUnitConnection, dataset);
        } catch (DatabaseUnitException | SQLException | IOException ex) {
            Assert.fail("failed to load data: " + filePostgreSqlData + ": " + ex.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void upsertDocument(Map<String, Object> document, String templateUuid) {
        String collectionName = DatasetUtils.makeCollectionName(UUID.fromString(templateUuid), false);
        mongoTemplate.save(document, collectionName);
    }

    private void removeDocument(ObjectId objectId, String templateUuid) {
        String collectionName = DatasetUtils.makeCollectionName(UUID.fromString(templateUuid), false);
        QueryBuilder qb = new QueryBuilder();
        qb.put("_id").is(objectId);
        mongoTemplate.remove(qb.get(), collectionName);
    }

    private void loadMongoDbData(String moduleName) {
        String fileMongoDbData = TEST_DATA_DIR + moduleName + "/" + moduleName + TEST_DATA_MONGODB_SUFFIX;
        Path file = FileSystems.getDefault().getPath(fileMongoDbData);
        try {
            String json = IOUtils.toString(new FileInputStream(file.toFile()));
            Map<String, Object> mongoObject = (Map<String, Object>) DatasetUtils.deserialize(json);
            if (mongoObject != null) {
                for (Map.Entry<String, Object> entry : mongoObject.entrySet()) {
                    String sUuid = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        upsertDocument((Map) value, sUuid);
                    } else if (value instanceof List) {
                        for (Map<String, Object> v : (List<Map<String, Object>>) value) {
                            upsertDocument(v, sUuid);
                        }
                    }
                }
            } else {
                Assert.fail("module MongoDB file is empty: " + fileMongoDbData);
            }
        } catch (FileNotFoundException ex) {
            Assert.fail("module MongoDB file not found: " + fileMongoDbData);
        } catch (IOException ex) {
            Assert.fail("module MongoDB file unable to read: " + fileMongoDbData);
        }
    }

    private void clearMongoDbData(String moduleName) {
        String fileMongoDbData = TEST_DATA_DIR + moduleName + "/" + moduleName + TEST_DATA_MONGODB_SUFFIX;
        Path file = FileSystems.getDefault().getPath(fileMongoDbData);
        try {
            String json = IOUtils.toString(new FileInputStream(file.toFile()));
            Map<String, Object> mongoObject = (Map<String, Object>) DatasetUtils.deserialize(json);
            if (mongoObject != null) {
                for (Map.Entry<String, Object> entry : mongoObject.entrySet()) {
                    String sUuid = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        removeDocument((ObjectId) ((Map) value).get("_id"), sUuid);
                    } else if (value instanceof List) {
                        for (Map<String, Object> v : (List<Map<String, Object>>) value) {
                            removeDocument((ObjectId) ((Map) v).get("_id"), sUuid);
                        }
                    }
                }
            } else {
                Assert.fail("module MongoDB file is empty: " + fileMongoDbData);
            }
        } catch (FileNotFoundException ex) {
            Assert.fail("module MongoDB file not found: " + fileMongoDbData);
        } catch (IOException ex) {
            Assert.fail("module MongoDB file unable to read: " + fileMongoDbData);
        }
    }

    private DataSource buildDataSource() throws Exception, NamingException {
        // postgresql data source
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

        InitialContext ic = new InitialContext();
        ic.createSubcontext("java:");
        ic.createSubcontext("java:comp");
        ic.createSubcontext("java:comp/env");
        ic.createSubcontext("java:comp/env/jdbc");

        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:postgresql://localhost:5432/test");
        properties.setProperty("maxActive", "10");
        properties.setProperty("maxIdle", "8");
        properties.setProperty("minIdle", "10");
        properties.setProperty("maxWait", "10");
        properties.setProperty("testOnBorrow", "true");
        properties.setProperty("username", "test");
        properties.setProperty("password", "c1234c");
        properties.setProperty("validationQuery", "SELECT 1");
        properties.setProperty("removeAbandoned", "true");
        properties.setProperty("removeAbandonedTimeout", "1");
        properties.setProperty("logAbandoned", "true");

        DataSource ds = BasicDataSourceFactory.createDataSource(properties);
        ic.bind("java:comp/env/jdbc/cris", ds);

        return ds;
    }

    private String eval(String text, StandardEvaluationContext context) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        ExpressionParser parser = new SpelExpressionParser();
        TemplateParserContext templateParserContext = new TemplateParserContext(EXPRESSION_PREFIX, EXPRESSION_SUFFIX);
        Expression expression = parser.parseExpression(text, templateParserContext);
        String result = expression.getValue(context, String.class);
        return result;
    }

    private Map<String, String> evalStep(Map<String, String> step, StandardEvaluationContext context) {
        Map<String, String> evaledStep = new HashMap<>();
        for (Map.Entry<String, String> entry : step.entrySet()) {
            switch (entry.getKey()) {
                // request
                case "Headers":
                case "Cookies":
                case "Url":
                case "Parameters":
                case "Content":
                case "Files":
                // expected
                case "ExpectedHeaders":
                case "ExpectedCookies":
                case "ExpectedContent":
                case "ExpectedFiles":
                // expression
                case "Asserts":
                    evaledStep.put(entry.getKey(), eval(entry.getValue(), context));
                    break;
                default:
                    evaledStep.put(entry.getKey(), entry.getValue());
                    break;
            }
        }
        return evaledStep;
    }

    // load test suite from excel file
    private Map<String, Map<Integer, Map<String, String>>> loadTestSuite(String fileName, String sheetName) {
        Map<String, Map<Integer, Map<String, String>>> testSuite = new HashMap<>();

        Workbook workbook;
        try {
            workbook = Workbook.getWorkbook(new File(fileName));
        } catch (IOException ex) {
            Assert.fail("unable to read test suite file: " + fileName + ": " + ex.getMessage());
            return null;
        } catch (BiffException ex) {
            Assert.fail("unable to parse test suite file: " + fileName + ": " + ex.getMessage());
            return null;
        }

        Sheet sheet = workbook.getSheet(sheetName);
        Cell multistepCounter = sheet.findCell(EnumTestCaseFileHeader.Name.toString());
        int multistepCounterPosition = multistepCounter.getColumn();
        int multistepStartRowPosition = multistepCounter.getRow();
        Cell cTestStep = sheet.findCell(EnumTestCaseFileHeader.Step.toString());
        int nTestStep = cTestStep.getColumn();
        Cell cDescription = sheet.findCell(EnumTestCaseFileHeader.Description.toString());
        int nDescription = cDescription.getColumn();
        Cell cWorkspace = sheet.findCell(EnumTestCaseFileHeader.Workspace.toString());
        int nWorkspace = cWorkspace.getColumn();
        Cell cUserName = sheet.findCell(EnumTestCaseFileHeader.Username.toString());
        int nUserName = cUserName.getColumn();
        Cell cPassword = sheet.findCell(EnumTestCaseFileHeader.Password.toString());
        int nPassword = cPassword.getColumn();
        Cell cMethod = sheet.findCell(EnumTestCaseFileHeader.Method.toString());
        int nMethod = cMethod.getColumn();
        Cell cUrl = sheet.findCell(EnumTestCaseFileHeader.Url.toString());
        int nUrl = cUrl.getColumn();
        Cell cContent = sheet.findCell(EnumTestCaseFileHeader.Content.toString());
        int nContent = cContent.getColumn();
        Cell cHeaders = sheet.findCell(EnumTestCaseFileHeader.Headers.toString());
        int nHeaders = cHeaders.getColumn();
        Cell cParameters = sheet.findCell(EnumTestCaseFileHeader.Parameters.toString());
        int nParameters = cParameters.getColumn();
        Cell cFiles = sheet.findCell(EnumTestCaseFileHeader.Files.toString());
        int nFiles = cFiles.getColumn();
        Cell cCookies = sheet.findCell(EnumTestCaseFileHeader.Cookies.toString());
        int nCookies = cCookies.getColumn();
        Cell cexpectedStatusCode = sheet.findCell(EnumTestCaseFileHeader.ExpectedStatusCode.toString());
        int nexpectedStatusCode = cexpectedStatusCode.getColumn();
        Cell cExpectedContent = sheet.findCell(EnumTestCaseFileHeader.ExpectedContent.toString());
        int nExpectedContent = cExpectedContent.getColumn();
        Cell cExpectedReasonPhrase = sheet.findCell(EnumTestCaseFileHeader.ExpectedReasonPhrase.toString());
        int nExpectedReasonPhrase = cExpectedReasonPhrase.getColumn();
        Cell cExpectedHeaders = sheet.findCell(EnumTestCaseFileHeader.ExpectedHeaders.toString());
        int nExpectedHeaders = cExpectedHeaders.getColumn();
        Cell cExpectedCookies = sheet.findCell(EnumTestCaseFileHeader.ExpectedCookies.toString());
        int nExpectedCookies = cExpectedCookies.getColumn();
        Cell cExpectedRedirectedUrl = sheet.findCell(EnumTestCaseFileHeader.ExpectedRedirectedUrl.toString());
        int nExpectedRedirectedUrl = cExpectedRedirectedUrl.getColumn();
        Cell cExpectedCharacterEncoding = sheet.findCell(EnumTestCaseFileHeader.ExpectedCharacterEncoding.toString());
        int nExpectedCharacterEncoding = cExpectedCharacterEncoding.getColumn();
        Cell cExpectedLocale = sheet.findCell(EnumTestCaseFileHeader.ExpectedLocale.toString());
        int nExpectedLocale = cExpectedLocale.getColumn();
        Cell cExpectedContentType = sheet.findCell(EnumTestCaseFileHeader.ExpectedContentType.toString());
        int nExpectedContentType = cExpectedContentType.getColumn();
        Cell cExpectedContentLength = sheet.findCell(EnumTestCaseFileHeader.ExpectedContentLength.toString());
        int nExpectedContentLength = cExpectedContentLength.getColumn();
        Cell cExpectedFilename = sheet.findCell(EnumTestCaseFileHeader.ExpectedFilename.toString());
        int nExpectedFilename = cExpectedFilename.getColumn();
        Cell cAsserts = sheet.findCell(EnumTestCaseFileHeader.Asserts.toString());
        int nAsserts = cAsserts.getColumn();
        Cell cInit = sheet.findCell(EnumTestCaseFileHeader.Init.toString());
        int nInit = cInit.getColumn();
        Cell cignoreColumn = sheet.findCell(EnumTestCaseFileHeader.Ignore.toString());
        int nignoreColumnPosition = cignoreColumn.getColumn();

        // Iterate through the rows and get all the column headers for execution
        for (int startRow = multistepStartRowPosition + 1; startRow < sheet.getRows(); startRow++) {
            String testCaseName = sheet.getCell(multistepCounterPosition, startRow).getContents();
            String stepId = sheet.getCell(nTestStep, startRow).getContents();
            if (StringUtils.isBlank(testCaseName) && StringUtils.isBlank(stepId)) {
                // break execution if test case name and step columns are both empty
                break;
            }

            Map<String, String> testStep = new HashMap<>();
            testStep.put(EnumTestCaseFileHeader.Description.toString(), sheet.getCell(nDescription, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Workspace.toString(), sheet.getCell(nWorkspace, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Username.toString(), sheet.getCell(nUserName, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Password.toString(), sheet.getCell(nPassword, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Method.toString(), sheet.getCell(nMethod, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Url.toString(), sheet.getCell(nUrl, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Content.toString(), sheet.getCell(nContent, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Headers.toString(), sheet.getCell(nHeaders, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Parameters.toString(), sheet.getCell(nParameters, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Files.toString(), sheet.getCell(nFiles, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Cookies.toString(), sheet.getCell(nCookies, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedStatusCode.toString(), sheet.getCell(nexpectedStatusCode, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedReasonPhrase.toString(), sheet.getCell(nExpectedReasonPhrase, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedHeaders.toString(), sheet.getCell(nExpectedHeaders, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedCookies.toString(), sheet.getCell(nExpectedCookies, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedRedirectedUrl.toString(), sheet.getCell(nExpectedRedirectedUrl, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedCharacterEncoding.toString(), sheet.getCell(nExpectedCharacterEncoding, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedContent.toString(), sheet.getCell(nExpectedContent, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedLocale.toString(), sheet.getCell(nExpectedLocale, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedContentType.toString(), sheet.getCell(nExpectedContentType, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedContentLength.toString(), sheet.getCell(nExpectedContentLength, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.ExpectedFilename.toString(), sheet.getCell(nExpectedFilename, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Asserts.toString(), sheet.getCell(nAsserts, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Init.toString(), sheet.getCell(nInit, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Ignore.toString(), sheet.getCell(nignoreColumnPosition, startRow).getContents());
            testStep.put(EnumTestCaseFileHeader.Row.toString(), String.valueOf(startRow + 1));

            Map<Integer, Map<String, String>> testCase = testSuite.get(testCaseName);
            if (testCase == null) {
                testCase = new TreeMap<>();
                testSuite.put(testCaseName, testCase);
            }
            testCase.put(Integer.valueOf(stepId), testStep);
        }

        return testSuite;
    }

    private MockHttpServletRequestBuilder buildRequestBuilder(Map<String, String> step) throws IOException {
        MockHttpServletRequestBuilder mockBuilder;
        String method = step.get(EnumTestCaseFileHeader.Method.toString()).toLowerCase();
        String url = step.get(EnumTestCaseFileHeader.Url.toString());
        String content = step.get(EnumTestCaseFileHeader.Content.toString());
        switch (method) {
            case "post":
                if ((step.get(EnumTestCaseFileHeader.Files.toString()).isEmpty())) {
                    // normal post
                    mockBuilder = post(url);
                    mockBuilder = mockBuilder.content(content).accept(MediaType.APPLICATION_JSON);
                } else {
                    // file upload
                    String pathExpression = TEST_DATA_DIR + moduleName + step.get(EnumTestCaseFileHeader.Files.toString());
                    File file = new File(pathExpression);
                    File parent = file.getParentFile();
                    String wildcardExpression = file.getName();
                    FileFilter fileFilter = new WildcardFileFilter(wildcardExpression);
                    File[] files = parent.listFiles(fileFilter);

                    MockMultipartHttpServletRequestBuilder mockHttpServletRequestBuilder = fileUpload(url);
                    for (File f : files) {
                        MockMultipartFile multiPartFile = new MockMultipartFile(f.getName(), new FileInputStream(f.getAbsolutePath()));
                        mockHttpServletRequestBuilder = mockHttpServletRequestBuilder.file(multiPartFile);
                    }
                    mockBuilder = mockHttpServletRequestBuilder.accept(MediaType.ALL);
                }
                break;
            case "get":
                mockBuilder = get(url);
                mockBuilder = mockBuilder.accept(MediaType.APPLICATION_JSON);
                break;
            case "put":
                mockBuilder = put(url);
                mockBuilder = mockBuilder.content(content).accept(MediaType.APPLICATION_JSON);
                break;
            case "delete":
                mockBuilder = delete(url);
                mockBuilder = mockBuilder.content(content).accept(MediaType.APPLICATION_JSON);
                break;
            default:
                mockBuilder = null;
        }

        if (mockBuilder != null) {
            mockBuilder.session(httpSession);
        }

        return mockBuilder;
    }

    private Map<String, Object> buildRequestContext(MockHttpServletRequest request, String content) {
        Map<String, Object> context = new HashMap<>();

        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        context.put(KEY_HEADERS, headers);

        Map<String, Cookie> cookies = new HashMap<>();
        for (Cookie cookie : request.getCookies()) {
            cookies.put(cookie.getName(), cookie);
        }
        context.put(KEY_COOKIES, cookies);

        context.put(KEY_LOCALE, request.getLocale().toLanguageTag());
        context.put(KEY_CHARACTER_ENCODING, request.getCharacterEncoding());
        context.put(KEY_CONTENT_TYPE, request.getContentType());
        context.put(KEY_CONTENT_LENGTH, request.getContentLength());
        if (StringUtils.isNotBlank(content)) {
            context.put(KEY_CONTENT_AS_STRING, content);
        }

        String contentType = request.getContentType();
        if (contentType != null && contentType.equals("application/json")) {
            if (content.startsWith("{")) {
                context.put(KEY_CONTENT, Helper.deserialize(content, Map.class));
            } else if (content.startsWith("[")) {
                context.put(KEY_CONTENT, Helper.deserialize(content, List.class));
            } else {
                context.put(KEY_CONTENT, null);
            }
        } else {
            context.put(KEY_CONTENT, null);
        }

        return context;
    }

    private Map<String, Object> buildResponseContext(MockHttpServletResponse response) throws UnsupportedEncodingException {
        Map<String, Object> context = new HashMap<>();

        context.put(KEY_STATUS_CODE, response.getStatus());
        context.put(KEY_REASON_PHRASE, response.getErrorMessage());
        context.put(KEY_REDIRECTED_URL, response.getRedirectedUrl());

        Map<String, String> headers = new HashMap<>();
        response.getHeaderNames().stream().forEach((header) -> {
            headers.put(header, response.getHeader(header));
        });
        context.put(KEY_HEADERS, headers);

        Map<String, Cookie> cookies = new HashMap<>();
        for (Cookie cookie : response.getCookies()) {
            cookies.put(cookie.getName(), cookie);
        }
        context.put(KEY_COOKIES, cookies);

        context.put(KEY_LOCALE, response.getLocale().toLanguageTag());
        context.put(KEY_CHARACTER_ENCODING, response.getCharacterEncoding());
        String contentType = response.getContentType();
        context.put(KEY_CONTENT_TYPE, contentType);
        context.put(KEY_CONTENT_LENGTH, response.getContentLength());
        String contentAsString = response.getContentAsString().trim();
        context.put(KEY_CONTENT_AS_STRING, contentAsString);

        if (contentType != null && contentType.equals("application/json")) {
            if (contentAsString.startsWith("{")) {
                context.put(KEY_CONTENT, Helper.deserialize(contentAsString, Map.class));
            } else if (contentAsString.startsWith("[")) {
                context.put(KEY_CONTENT, Helper.deserialize(contentAsString, List.class));
            } else {
                context.put(KEY_CONTENT, null);
            }
        } else {
            context.put(KEY_CONTENT, null);
        }

        String contentDisposition = response.getHeader("Content-Disposition");
        if (contentDisposition != null && contentDisposition.contains("filename")) {
            String filename = contentDisposition.substring(contentDisposition.indexOf("\""), contentDisposition.lastIndexOf("\""));
            context.put(KEY_FILENAME, filename);
        } else {
            context.put(KEY_FILENAME, null);
        }

        return context;
    }

    private Object buildExpectedContext(Map<String, String> evaledStep) {
        String expectedContent = evaledStep.get(EnumTestCaseFileHeader.ExpectedContent.name()).trim();
        Object context;
        if (expectedContent.startsWith("[")) {
            context = Helper.deserialize(expectedContent, List.class);
        } else if (expectedContent.startsWith("{")) {
            context = Helper.deserialize(expectedContent, Map.class);
        } else {
            context = null;
        }

        return context;
    }

    private Object[][] getTestSuite(String testSuiteFile) throws Exception {
        Map<String, Map<Integer, Map<String, String>>> testSuite = loadTestSuite(TEST_DATA_DIR + moduleName + "/" + testSuiteFile, TEST_CASE_EXCEL_SHEET);
        Object[][] testCases = new Object[testSuite.size()][];
        int i = 0;
        for (Map.Entry<String, Map<Integer, Map<String, String>>> entry : testSuite.entrySet()) {
            testCases[i] = new Object[]{entry.getKey(), entry.getValue()};
            i++;
        }
        return testCases;
    }

    private void testSuite(String testCaseName, Map<Integer, Map<String, String>> testCase) throws Exception {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

        String currentWorkspace = null;
        String currentUsername = null;
        boolean loggedIn = false;
        for (Integer testStepId : testCase.keySet()) {
            // for each test step
            // if any step fails, it should stop running the remaining steps

            Map<String, String> testStep = (Map<String, String>) testCase.get(testStepId);
            String evaledInit = eval(testStep.get(EnumTestCaseFileHeader.Init.toString()), evaluationContext);

            evaluationContext.setVariable(TOP_CONTEXT_INIT, evaledInit);
            Map<String, String> evaledStep = evalStep(testStep, evaluationContext);

            String row = evaledStep.get(EnumTestCaseFileHeader.Row.toString());
            String ignore = evaledStep.get(EnumTestCaseFileHeader.Ignore.name());
            System.out.println("Executing test case: " + testCaseName + ", step: " + testStepId + ", row: " + row + ", ignore: " + ignore);
            if (ignore.equalsIgnoreCase("y")) {
                // if there's one step ignored, the whole test case is skipped
                throw new SkipException(String.format(ASSERT_MESSAGE_TEMPLATE, row, testCaseName, testStepId, "the whole test case is ignored"));
            }

            String workspace = evaledStep.get(EnumTestCaseFileHeader.Workspace.name());
            String username = evaledStep.get(EnumTestCaseFileHeader.Username.name());
            String password = evaledStep.get(EnumTestCaseFileHeader.Password.name());

            // workspace
            if (StringUtils.isNotBlank(workspace)) {
                if (!workspace.equals(currentWorkspace)) {
                    setupWorkspace(workspace);
                    currentWorkspace = workspace;
                }
            } else {
                if (StringUtils.isBlank(currentWorkspace)) {
                    setupDefaultWorkspace();
                    currentWorkspace = "default";
                }
            }

            // login
            if (StringUtils.isNotBlank(username) && !username.equals(currentUsername)) {
                login(username, password);
                currentUsername = username;
                loggedIn = true;
            }

            // build request builder
            MockHttpServletRequestBuilder requestBuilder = buildRequestBuilder(evaledStep);
            if (requestBuilder == null) {
                Assert.fail(String.format(ASSERT_MESSAGE_TEMPLATE, row, testCaseName, testStepId, "unable to build a request"));
                break;
            }

            ResultActions resultActions = mockMvc.perform(requestBuilder);
            MockHttpServletResponse response = resultActions.andReturn().getResponse();
            System.out.println(response.getContentAsString());

            // validate various information

            // status code
            String expectedStatusCode = evaledStep.get(EnumTestCaseFileHeader.ExpectedStatusCode.name());
            if (StringUtils.isNotBlank(expectedStatusCode)) {
                ResultMatcher resultMatcher;
                switch (expectedStatusCode) {
                    case "1xx":
                        resultMatcher = status().is1xxInformational();
                        break;
                    case "2xx":
                        resultMatcher = status().is2xxSuccessful();
                        break;
                    case "3xx":
                        resultMatcher = status().is3xxRedirection();
                        break;
                    case "4xx":
                        resultMatcher = status().is4xxClientError();
                        break;
                    case "5xx":
                        resultMatcher = status().is5xxServerError();
                        break;
                    default:
                        resultMatcher = status().is(Integer.parseInt(expectedStatusCode));
                }
                resultActions.andExpect(resultMatcher);
            }

            // reason phrase
            String expectedReasonPhrase = evaledStep.get(EnumTestCaseFileHeader.ExpectedReasonPhrase.name());
            if (StringUtils.isNotBlank(expectedReasonPhrase)) {
                Assert.assertTrue(true, String.format(ASSERT_MESSAGE_TEMPLATE, row, testCaseName, testStepId, "reason phrase: " + expectedReasonPhrase + ": not implemented"));
            }

            // headers
            String expectedHeaders = evaledStep.get(EnumTestCaseFileHeader.ExpectedHeaders.name());
            if (StringUtils.isNotBlank(expectedHeaders)) {
                String[] headers = expectedHeaders.split("\n");
                HeaderResultMatchers matcher = header();
                boolean headerExists = false;
                for (String header : headers) {
                    if (StringUtils.isNotBlank(header)) {
                        String[] nameValue = header.split(":");
                        if (nameValue.length == 2) {
                            headerExists = true;
                            matcher.string(nameValue[0], nameValue[1]);
                        } else if (nameValue.length == 1) {
                            headerExists = true;
                            matcher.string(nameValue[0], "*");
                        }
                    }
                }
                if (headerExists) {
                    resultActions.andExpect((ResultMatcher) matcher);
                }
            }

            // cookies
            String expectedCookies = evaledStep.get(EnumTestCaseFileHeader.ExpectedCookies.name());
            if (StringUtils.isNotBlank(expectedCookies)) {
                String[] cookies = expectedCookies.split("\n");
                CookieResultMatchers matcher = cookie();
                boolean cookieExists = false;
                for (String cookie : cookies) {
                    if (StringUtils.isNotBlank(cookie)) {
                        String[] nameValue = cookie.split("");
                        if (nameValue.length == 2) {
                            cookieExists = true;
                            matcher.value(nameValue[0], nameValue[1]);
                        }
                    }
                }
                if (cookieExists) {
                    resultActions.andExpect((ResultMatcher) matcher);
                }
            }

            // redirected url
            String expectedRedirectedUrl = evaledStep.get(EnumTestCaseFileHeader.ExpectedRedirectedUrl.name());
            if (StringUtils.isNotBlank(expectedRedirectedUrl)) {
                resultActions.andExpect(redirectedUrl(expectedRedirectedUrl));
            }

            // locale
            String expectedLocale = evaledStep.get(EnumTestCaseFileHeader.ExpectedLocale.name());
            if (StringUtils.isNotBlank(expectedLocale)) {
                Assert.assertEquals(response.getLocale().getDisplayName(), expectedLocale, String.format(ASSERT_MESSAGE_TEMPLATE, row, testCaseName, testStepId, "locale"));
            }

            // content type
            // character encoding
            String expectedCharacterEncoding = evaledStep.get(EnumTestCaseFileHeader.ExpectedCharacterEncoding.name());
            String expectedContentType = evaledStep.get(EnumTestCaseFileHeader.ExpectedContentType.name());
            if (StringUtils.isNotBlank(expectedCharacterEncoding) || StringUtils.isNotBlank(expectedContentType)) {
                if (StringUtils.isNotBlank(expectedCharacterEncoding)) {
                    content().encoding(expectedCharacterEncoding);
                }
                if (StringUtils.isNotBlank(expectedContentType)) {
                    content().contentType(expectedContentType);
                }
                resultActions.andExpect((ResultMatcher) content());
            }

            // content length
            String expectedContentLength = evaledStep.get(EnumTestCaseFileHeader.ExpectedContentLength.name());
            if (StringUtils.isNotBlank(expectedContentLength)) {
                Assert.assertEquals(response.getContentLength(), Integer.parseInt(expectedContentLength), String.format(ASSERT_MESSAGE_TEMPLATE, row, testCaseName, testStepId, "content length"));
            }

            // Create engine scope
            Map<String, Object> initContext = Helper.deserialize(evaledInit, Map.class);
            Map<String, Object> requestContext = buildRequestContext(requestBuilder.buildRequest(servletContext), evaledStep.get(EnumTestCaseFileHeader.Content.name()));
            Map<String, Object> responseContext = buildResponseContext(response);
            Object expectedContext = buildExpectedContext(evaledStep);

            Map<String, Object> engineScope = new HashMap<>();
            engineScope.put(TOP_CONTEXT_INIT, initContext);
            engineScope.put(TOP_CONTEXT_REQUEST, requestContext);
            engineScope.put(TOP_CONTEXT_EXPECTED, expectedContext);
            engineScope.put(TOP_CONTEXT_RESPONSE, responseContext);
            engineScope.put(TOP_CONTEXT_RESULT, responseContext.get(KEY_CONTENT));
            crisScriptEngine.createEngineScope(engineScope);
            System.out.println("**** engine scope");
            System.out.println(Helper.deepSerialize(engineScope));

            // asserts
            String expressions = evaledStep.get(EnumTestCaseFileHeader.Asserts.toString());
            String[] expressionArray = expressions.split("\n");
            for (String expression : expressionArray) {
                if (StringUtils.isNotBlank(expression)) {
                    try {
                        boolean isValid = crisScriptEngine.evaluateBooleanExpression(expression);
                        Assert.assertTrue(isValid, String.format(ASSERT_MESSAGE_TEMPLATE, row, testCaseName, testStepId, expression));
                    } catch (Exception ex) {
                        Assert.fail(String.format(ASSERT_MESSAGE_TEMPLATE, row, testCaseName, testStepId, "unable to evaluate: " + expression));
                    }
                }
            }

            // update context for the next step
            evaluationContext.setVariable(TOP_CONTEXT_RESPONSE, responseContext);
        }

        // Logout after each test case
        if (loggedIn) {
            logout();
        }
    }

}
