/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Session;
import edu.purdue.cybercenter.dm.repository.SessionRepository;
import edu.purdue.cybercenter.dm.util.SecurityHelper;
import edu.purdue.cybercenter.dm.web.filter.RequestFilter;
import edu.purdue.cybercenter.dm.web.filter.SessionFilter;
import edu.purdue.cybercenter.dm.web.filter.WorkflowAsSessionFilter;
import edu.purdue.cybercenter.dm.web.listener.ContextListener;
import edu.purdue.cybercenter.dm.web.listener.SessionListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSessionEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 *
 * @author xu222
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(locations = {
        "file:src/test/resources/META-INF/spring/applicationContext.xml",
        "file:src/test/resources/META-INF/spring/applicationContext-security.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-database.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-activiti.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-cache.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-jms.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-mail.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-task.xml",
        "file:src/main/resources/META-INF/spring/applicationContext-mongo.xml"}),
    @ContextConfiguration(locations = {
        "file:src/test/webapp/WEB-INF/spring/webmvc-config.xml",
        "file:src/test/webapp/WEB-INF/spring/applicationContext-filters.xml"})
})
@Transactional
public class BaseControllerTest extends BaseTest {

    @Autowired
    WebApplicationContext wac;
    @Autowired
    MockHttpSession httpSession;
    @Autowired
    MockServletContext servletContext;
    @Autowired
    OpenEntityManagerInViewFilter openEntityManagerInViewFilter;
    @Autowired
    HiddenHttpMethodFilter hiddenHttpMethodFilter;
    @Autowired
    CharacterEncodingFilter characterEncodingFilter;
    @Autowired
    SessionFilter sessionFilter;
    @Autowired
    WorkflowAsSessionFilter workflowAsSessionFilter;
    @Autowired
    FilterChainProxy springSecurityFilterChain;
    @Autowired
    RequestFilter requestFilter;
    MockMvc mockMvc;

    @Autowired
    private SessionRepository sessionRepository;

    protected static final String TestFileDir = "src/test/files/";

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(openEntityManagerInViewFilter, "/*")
                .addFilter(hiddenHttpMethodFilter, "/*")
                .addFilter(characterEncodingFilter, "/*")
                .addFilter(sessionFilter, "/*")
                .addFilter(workflowAsSessionFilter, "/*")
                .addFilter(springSecurityFilterChain, "/*")
                .addFilter(requestFilter, "/*")
                .build();

        servletContext.setContextPath("/");

        ServletContextEvent sce = new ServletContextEvent(servletContext);
        ContextListener cl = new ContextListener();
        cl.contextInitialized(sce);

        HttpSessionEvent hse = new HttpSessionEvent(httpSession);
        SessionListener sl = new SessionListener();
        sl.sessionCreated(hse);

        Session session = sessionRepository.findByJsessionid(httpSession.getId());
        if (session == null) {
            session = new Session();
            session.setJsessionid(httpSession.getId());
            sessionRepository.save(session);
        }
    }

    @After
    public void tearDown() throws Exception {
        /*TODO: to be fixed
        HttpSessionEvent hse = new HttpSessionEvent(httpSession);
        SessionListener sl = new SessionListener();
        sl.sessionDestroyed(hse);

        ServletContextEvent sce = new ServletContextEvent(servletContext);
        ContextListener cl = new ContextListener();
        cl.contextDestroyed(sce);
        */
    }

    @Test
    public void markerMethod() throws Exception {
    }

    /*
     * utility methods
     */
    protected Integer getTenantId() {
        return (Integer) httpSession.getAttribute("tenantId");
    }

    protected void setTenantId(int tenantId) {
        httpSession.setAttribute("tenantId", 1);
    }

    protected void login(String username, String password) throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post("/auth/verify").param("username", username).param("password", password).session(httpSession);
        ResultActions auth = this.mockMvc.perform(mockHttpServletRequestBuilder);
        auth.andExpect(redirectedUrl("/"));

        httpSession = (MockHttpSession) auth.andReturn().getRequest().getSession();
        Session session = sessionRepository.findByJsessionid(httpSession.getId());
        if (session == null) {
            session = new Session();
            session.setJsessionid(httpSession.getId());
            sessionRepository.save(session);
        }
    }

    protected void logout() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get("/auth/signout").session(httpSession);
        ResultActions auth = this.mockMvc.perform(mockHttpServletRequestBuilder);
        auth.andExpect(redirectedUrl("/"));

        httpSession = (MockHttpSession) auth.andReturn().getRequest().getSession();
        Session session = sessionRepository.findByJsessionid(httpSession.getId());
        if (session == null) {
            session = new Session();
            session.setJsessionid(httpSession.getId());
            sessionRepository.save(session);
        }
    }

    /*
     * Convenience methods
     */
    protected void setupWorkspace() {
        setTenantId(1);
    }

    protected void loginAdminUser() throws Exception {
        if (getTenantId() == null) {
            setTenantId(1);
        }
        login("george.washington", "1234");
        SecurityHelper.setAuthentication(1);
    }

    protected void loginNormalUser() throws Exception {
        if (getTenantId() == null) {
            setTenantId(1);
        }
        login("john.adams", "1234");
    }
}
