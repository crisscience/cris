/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.jms;

import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Tenant;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.security.UserDetailsAdapter;
import edu.purdue.cybercenter.dm.util.ActivitiHelper;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.service.WorkflowService;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author xu222
 */
@Service
public class UserJobMessageDelegate {

    private static final Logger logger = LoggerFactory.getLogger(UserJobMessageDelegate.class.getName());

    private static final String RootDir = "/tmp/";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private WorkflowService workflowService;

    @Transactional
    void handleMessage(Serializable sMessage) {
        Map<String, Object> message = (Map<String, Object>) sMessage;

        Integer tenantId = (Integer) message.get("tenantId");
        Integer userId = (Integer) message.get("userId");
        Tenant tenant = Tenant.findTenant(tenantId);
        edu.purdue.cybercenter.dm.threadlocal.TenantId.set(tenantId);
        edu.purdue.cybercenter.dm.threadlocal.UserId.set(userId);

        // create the credentials used by spring security
        User user = User.findUser(userId);
        UserDetails userDetails = new UserDetailsAdapter(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        org.hibernate.Session hSession = DomainObjectHelper.getHbmSession();
        org.hibernate.Filter tenantFilter = hSession.enableFilter("tenantFilter");
        tenantFilter.setParameter("tenantId", tenantId);

        logger.info("UserJob message: {}", sMessage);

        String messageType = (String) message.get("messageType");

        switch (messageType) {
            case "Finish":
                handleFinishMessage(message);
                break;
            case "Invalid":
                handleInvalidMessage(message);
                break;
            default:
                // unknown message
                handleUnknownMessage(message);
                break;
        }

        hSession.disableFilter("tenantFilter");
    }

    private void handleFinishMessage(Map<String, Object> message) {
        Integer projectId = (Integer) message.get("projectId");
        Integer experimentId = (Integer) message.get("experimentId");
        Integer jobId = (Integer) message.get("jobId");
        String activityId = (String) message.get("taskId");
        Integer state = (Integer) message.get("state");

        String mountPointLocal = (String) message.get("mountPointLocal");
        String mountPointRemote = (String) message.get("mountPointRemote");
        String tmpDir = (String) message.get("tmpDir");
        Boolean setupWorkingDir = (Boolean) message.get("setupWorkingDir");
        Boolean cleanupWorkingDir = (Boolean) message.get("cleanupWorkingDir");

        Job job = Job.findJob(jobId);

        String sJsonOut = (String) message.get("jsonOut");
        String sFilesToCollect = (String) message.get("filesToCollect");

        Map<String, Object> data;
        if (sJsonOut == null || sJsonOut.isEmpty()) {
            data = (Map<String, Object>) DatasetUtils.deserialize("{}");
        } else if (sJsonOut.startsWith("{")) {
            data = (Map<String, Object>) DatasetUtils.deserialize(sJsonOut);
        } else {
            data = (Map<String, Object>) DatasetUtils.deserialize("{\"unknown\":\"" + sJsonOut + "\"}");
        }

        String rootDir;
        if (mountPointLocal != null) {
            rootDir = mountPointLocal + ((mountPointLocal.endsWith("/") || tmpDir.startsWith("/")) ? "" : "/") + tmpDir + (tmpDir.endsWith("/") ? "" : "/");
        } else {
            rootDir = RootDir;
        }
        String dirPath = rootDir + jobId + "/";
        File workingDir = new File(dirPath);
        if (!workingDir.exists()) {
            logger.error("Working directory: {} does not exist for job: {}", new Object[]{dirPath, jobId});
        }

        Map<String, Object> context = new HashMap<>();
        context.put(MetaField.ProjectId, projectId);
        context.put(MetaField.ExperimentId, experimentId);
        context.put(MetaField.JobId, jobId);
        context.put(MetaField.TaskId, activityId);
        context.put(MetaField.State, state);

        String processInstanceId = ActivitiHelper.jobToProcessInstance(job).getId();

        if (sFilesToCollect != null && !sFilesToCollect.isEmpty()) {
            workflowService.collectFiles(sFilesToCollect, dirPath, job, activityId, context);
        }

        if (data != null && !data.isEmpty()) {
            workflowService.validateAndSaveData(data, processInstanceId, context);
        }

        if (cleanupWorkingDir) {
            File file = new File(dirPath);
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException ex) {
                logger.error("Unable to clean up working directory for job " + jobId, ex);
            }
        }

        // Signal the completion of the task
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
        runtimeService.signal(execution.getId());
    }

    private void handleInvalidMessage(Map<String, Object> message) {

    }

    private void handleUnknownMessage(Map<String, Object> message) {

    }
}
