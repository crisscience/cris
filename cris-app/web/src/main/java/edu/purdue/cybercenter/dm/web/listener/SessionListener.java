/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web.listener;

import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.Session;
import edu.purdue.cybercenter.dm.util.ActivitiHelper;
import edu.purdue.cybercenter.dm.util.AppConfigConst;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web application lifecycle listener.
 * @author xu222
 */
public class SessionListener implements HttpSessionListener, HttpSessionAttributeListener, HttpSessionActivationListener, HttpSessionBindingListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class.getName());

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        Session session = new Session();
        session.setJsessionid(sessionId);
        session.persist();

        // create a temp directory for the session
        File sessionPath = makeSessionPath(se);
        if (!sessionPath.exists() && !sessionPath.mkdir()) {
            throw new RuntimeException("Unable to create a working directory for session: " + sessionPath.getAbsolutePath());
        }
        se.getSession().setAttribute(AppConfigConst.SESSION_TMP_PATH, sessionPath.getAbsolutePath());
        logger.info("session directory created: {}", sessionPath.getAbsolutePath());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // remove the temp directory for the session
        String sessionPath = (String) se.getSession().getAttribute(AppConfigConst.SESSION_TMP_PATH);
        File file = new File(sessionPath);
        try {
            FileUtils.deleteDirectory(file);
            logger.info("session directory removed: {}", sessionPath);
        } catch (IOException ex) {
            logger.error("fail to remove session directory: {}", sessionPath);
        }

        Session session = Session.findBySessionId(se.getSession().getId());
        if (session != null) {
            session.setTimeUpdated(new Date(se.getSession().getLastAccessedTime()));
        } else {
            // this should never happen
            logger.error("Destroyed a session without database record: {}", se.getSession().getId());
        }

        // for workflow as session, clean up remaining jobs
        Boolean workflowAsSession = (Boolean) se.getSession().getAttribute("workflowAsSession");
        if (workflowAsSession != null && workflowAsSession) {
            List<Integer> jobIds = (List<Integer>) se.getSession().getAttribute("jobs");
            if (jobIds != null) {
                ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                RuntimeService runtimeService = processEngine.getRuntimeService();
                for (Integer jobId : jobIds) {
                    Job job = Job.findJob(jobId);
                    ProcessInstance processInstance = ActivitiHelper.jobToProcessInstance(job);
                    runtimeService.deleteProcessInstance(processInstance.getId(), "Workflow as session: user session closed. Clean up pending workflow");
                    logger.warn("Workflow as session: job deleted as session closed: {}", jobId);
                }
            }
        }
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent se) {
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent se) {
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent se) {
    }

    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent se) {
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
    }

    private File makeSessionPath(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        String sessionTmpPath = (String) se.getSession().getServletContext().getAttribute(AppConfigConst.SESSION_TMP_PATH);
        File sessionPath = new File(sessionTmpPath, sessionId);
        return sessionPath;
    }
}
