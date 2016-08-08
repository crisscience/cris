/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web.listener;

import edu.purdue.cybercenter.dm.domain.Configuration;
import edu.purdue.cybercenter.dm.util.AppConfigConst;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.ProductBuildInfo;
import java.io.File;
import java.util.List;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web application lifecycle listener.
 * @author jiaxu
 */
public class ContextListener implements ServletContextListener, ServletContextAttributeListener {

    private static final Logger logger = LoggerFactory.getLogger(ContextListener.class.getName());

    // application-wide default
    private static final String DefaultSearchEngineUrl = "http://localhost:9200/";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // context
        ServletContext servletContext = sce.getServletContext();
        String contextConfigLocation = (String) servletContext.getInitParameter("contextConfigLocation");
        logger.info("Context configuration: {}", contextConfigLocation);

        // put product build info to context
        ProductBuildInfo pbi = new ProductBuildInfo();
        servletContext.setAttribute("productBuildInfo", pbi);
        logger.info("productBuildInfo: Revision: {}", pbi.getRevision());
        logger.info("productBuildInfo: Major: {}", pbi.getMajor());
        logger.info("productBuildInfo: Minor: {}", pbi.getMinor());
        logger.info("productBuildInfo: Patch: {}", pbi.getPatch());
        logger.info("productBuildInfo: Build ID: {}", pbi.getBuildId());
        logger.info("productBuildInfo: Build Number: {}", pbi.getBuildNumber());
        logger.info("productBuildInfo: Build Tag: {}", pbi.getBuildTag());
        logger.info("productBuildInfo: Build Timestamp: {}", pbi.getBuildTimestamp());
        logger.info("productBuildInfo: Build Type: {}", pbi.getBuildType());
        logger.info("productBuildInfo: Java Home: {}", pbi.getJavaHome());
        logger.info("productBuildInfo: Job Name: {}", pbi.getJobName());
        logger.info("productBuildInfo: Workspace: {}", pbi.getWorkspace());
        logger.info("productBuildInfo: Executor Number: {}", pbi.getExecutorNumber());

        // log application-wide configurations
        TypedQuery<Configuration> query = DomainObjectHelper.createNamedQuery("Configuration.findGlobals", Configuration.class);
        List<Configuration> configurations = query.getResultList();
        for (Configuration configuration : configurations) {
            String name = configuration.getName();
            String value = configuration.getValueText();
            logger.info(String.format("Global configuration: %s: %s", name, value));
        }

        // path separator
        String pathSeparator = System.getProperty("file.separator");
        // tmp directory
        String tmpDir = AppConfigConst.getTmpPath();
        logger.info("Path separator: {}", pathSeparator);

        File appTmpPath = new File(AppConfigConst.getAppTmpPath());
        if (!appTmpPath.exists() && !appTmpPath.mkdir()) {
            throw new RuntimeException(String.format("Unable to create a temporary directory for app: %s", appTmpPath.getAbsolutePath()));
        }

        File sessionTmpPath = new File(AppConfigConst.getSessionTmpPath());
        if (!sessionTmpPath.exists() && !sessionTmpPath.mkdir()) {
            throw new RuntimeException(String.format("Unable to create a temporary directory for sessions: %s", sessionTmpPath.getAbsolutePath()));
        }

        File jobTmpPath = new File(AppConfigConst.getJobTmpPath());
        if (!jobTmpPath.exists() && !jobTmpPath.mkdir()) {
            throw new RuntimeException(String.format("Unable to create a temporary directory for jobs: %s", jobTmpPath.getAbsolutePath()));
        }

        // and put both path into the context for access later
        servletContext.setAttribute(AppConfigConst.TMP_PATH, tmpDir);
        servletContext.setAttribute(AppConfigConst.APP_TMP_PATH, appTmpPath.getAbsolutePath());
        servletContext.setAttribute(AppConfigConst.SESSION_TMP_PATH, sessionTmpPath.getAbsolutePath());
        servletContext.setAttribute(AppConfigConst.JOB_TMP_PATH, jobTmpPath.getAbsolutePath());
        logger.info("tmp directory: {}", tmpDir);
        logger.info("app tmp directory: {}", appTmpPath.getAbsolutePath());
        logger.info("session tmp directory: {}", sessionTmpPath.getAbsolutePath());
        logger.info("job tmp directory: {}", jobTmpPath.getAbsolutePath());

        logger.info("Initializing Activiti...");
        ProcessEngines.init();
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        if (processEngine == null) {
            logger.error("Unable to get an process engine");
        } else {
            logger.info("Process engine: {}", processEngine.getName());
        }

        // get search engine url
        String searchEngineUrl;
        try {
            TypedQuery<Configuration> q = DomainObjectHelper.createNamedQuery("Configuration.findGlobalPropertyByName", Configuration.class).setParameter("name", "searchEngineUrl");
            Configuration configuration = q.getSingleResult();
            searchEngineUrl = configuration.getValueText();
        } catch (Exception ex) {
            // searchEngineUrl is not configured
            searchEngineUrl = null;
        }

        if (searchEngineUrl == null || searchEngineUrl.isEmpty()) {
            searchEngineUrl = DefaultSearchEngineUrl;
        } else if (!searchEngineUrl.endsWith("/")) {
            searchEngineUrl = searchEngineUrl + "/";
        }
        servletContext.setAttribute("searchEngineUrl", searchEngineUrl);
        logger.info("searchEngineUrl: {}", searchEngineUrl);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ProcessEngines.destroy();
        logger.info("Process engine destroyed");
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent scab) {
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent scab) {
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent scab) {
    }
}
