/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.activiti;

import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.service.CqlService;
import edu.purdue.cybercenter.dm.util.AppConfigConst;
import edu.purdue.cybercenter.dm.util.ConstDatasetState;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.JsonTransformer;
import edu.purdue.cybercenter.dm.service.WorkflowService;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author xu222
 */
@Configurable
public class ServiceTaskDelegate implements JavaDelegate {

    static final private String IsValid = "isValid";
    static final private String ErrorMessage = "errorMessage";

    private Expression filesToPlace;
    private Expression jsonIn;
    private Expression preFilter;
    private Expression commandLine;
    private Expression postFilter;
    private Expression jsonOut;
    private Expression filesToCollect;

    // These fields are used by workflow editor
    // This class does not have any use of them but need to keep them here to satisfy the field injector.
    private Expression files;
    private Expression uuid;
    private Expression crisWorkflowImplementationVersion;
    private Expression uiLocation;
    private Expression orientation;

    // the command should be the wrapper
    // it takes input from job context table.
    // it executes the command according to the input
    // it writes the result into job context table

    @Autowired
    private CqlService cqlService;

    @Autowired
    private WorkflowService workflowService;

    @Override
    public void execute(DelegateExecution de) throws Exception {
        Map<String, Object> context = buildContext(de);
        placeFiles(context, de);

        Map<String, Object> mergedJsonOut = execute(context);
        collectFiles(context);

        saveResult(mergedJsonOut, context, de, null);
    }

    protected Map<String, Object> buildContext(DelegateExecution de) {
        String activityId = de.getCurrentActivityId();
        Integer jobId = Integer.parseInt(de.getProcessBusinessKey());
        Job job = Job.findJob(jobId);
        Integer projectId = job.getProjectId().getId();
        Integer experimentId = job.getExperimentId().getId();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        User user = User.findUser(userId);
        // the state set for the current step
        Integer state = (Integer) de.getVariable(ConstDatasetState.DatasetState);
        if (state == null) {
            // otherwise use the initial state
            state = (Integer) de.getVariable(ConstDatasetState.InitialDatasetState);
        }

        Map<String, Object> localVariables = de.getVariables();

        Map<String, Object> context = new HashMap<>();
        context.put(MetaField.User, user);
        context.put(MetaField.Project, job.getProjectId());
        context.put(MetaField.Experiment, job.getExperimentId());
        context.put(MetaField.Job, job);
        context.put(MetaField.ProjectId, projectId);
        context.put(MetaField.ExperimentId, experimentId);
        context.put(MetaField.JobId, jobId);
        context.put(MetaField.TaskId, activityId);
        context.put(MetaField.UserId, userId);
        context.put(MetaField.State, state);
        context.put(MetaField.LocalVariables, localVariables);

        return context;
    }

    protected void placeFiles(Map<String, Object> context, DelegateExecution de) throws Exception {
        Job job = (Job) context.get(MetaField.Job);
        Integer jobId = (Integer) context.get(MetaField.JobId);
        String sFilesToPlace = filesToPlace != null ? filesToPlace.getExpressionText() : null;
        String dirPath = AppConfigConst.getJobTmpPath() + AppConfigConst.FILE_SEPARATOR + jobId + "/";

        if (sFilesToPlace != null && !sFilesToPlace.isEmpty()) {
            ExecutionEntity ee = (ExecutionEntity) de;
            workflowService.placeFiles(sFilesToPlace, dirPath, job, (ProcessDefinition) ee.getProcessDefinition(), context);
        }
    }

    protected Map<String, Object> execute(Map<String, Object> context) throws Exception {
        String sJsonIn = cqlService.eval(jsonIn != null ? jsonIn.getExpressionText() : null, context);
        String sPreFilter = cqlService.eval(preFilter != null ? preFilter.getExpressionText() : null, context);
        String sCommandLine = cqlService.eval(commandLine != null ? commandLine.getExpressionText() : null, context);
        String sPostFilter = cqlService.eval(postFilter != null ? postFilter.getExpressionText() : null, context);
        String sJsonOut = cqlService.eval(jsonOut != null ? jsonOut.getExpressionText() : null, context);

        Integer jobId = (Integer) context.get(MetaField.JobId);
        String dirPath = AppConfigConst.getJobTmpPath() + AppConfigConst.FILE_SEPARATOR + jobId + "/";
        File workingDir = new File(dirPath);
        if (!workingDir.exists()) {
            throw new Exception("This shouldn't happen but no working directory exist for job " + jobId + ": " + dirPath);
        }

        Map<String, Object> mergedJsonOut = execute(workingDir, JsonTransformer.transformJson(sJsonIn), sPreFilter, sCommandLine, sPostFilter, JsonTransformer.transformJson(sJsonOut));

        return mergedJsonOut;
    }

    protected void collectFiles(Map<String, Object> context) {
        Job job = (Job) context.get(MetaField.Job);
        Integer jobId = (Integer) context.get(MetaField.JobId);
        String sFilesToCollect = cqlService.eval(filesToCollect != null ? filesToCollect.getExpressionText() : null, context);
        String dirPath = AppConfigConst.getJobTmpPath() + AppConfigConst.FILE_SEPARATOR + jobId + "/";
        String activityId = (String) context.get(MetaField.TaskId);

        if (sFilesToCollect != null && !sFilesToCollect.isEmpty()) {
            workflowService.collectFiles(sFilesToCollect, dirPath, job, activityId, context);
        }
    }

    protected void saveResult(Map<String, Object> mergedJsonOut, Map<String, Object> context, DelegateExecution de, String processInstanceId) {
        // remove project/experiment/job: we don't want to save them into database
        context.remove(MetaField.User);
        context.remove(MetaField.Project);
        context.remove(MetaField.Experiment);
        context.remove(MetaField.Job);

        if (!mergedJsonOut.isEmpty()) {
            if (de != null) {
                workflowService.validateAndSaveData(mergedJsonOut, de, context);
            } else {
                workflowService.validateAndSaveData(mergedJsonOut, processInstanceId, context);
            }
        }
    }

    private Map<String, Object> execute(File workingDir, String sJsonIn, String sPreFilter, String sCommandLine, String sPostFilter, String sJsonOut) {
        String stdin = sJsonIn;
        String stdout;
        String stderr = null;
        Integer exitCode = 0;

        String isValid = "true";
        String errorMessage = null;
        if (sPreFilter != null && !sPreFilter.isEmpty()) {
            Map<String, Object> result = workflowService.execute(sPreFilter, null, workingDir, stdin);
            isValid = (String) result.get("isValid");
            errorMessage = (String) result.get("errorMessage");
            exitCode = (Integer) result.get("exitCode");
            stderr = (String) result.get("stderr");
            // prepare for next step
            stdin = (String) result.get("stdout");
        }

        if ((isValid != null && isValid.equals("true")) && sCommandLine != null && !sCommandLine.isEmpty()) {
            Map<String, Object> result = workflowService.execute(sCommandLine, null, workingDir, stdin);
            isValid = (String) result.get("isValid");
            errorMessage = (String) result.get("errorMessage");
            exitCode = (Integer) result.get("exitCode");
            stderr = (String) result.get("stderr");
            // prepare for next step
            stdin = (String) result.get("stdout");
        }

        if ((isValid != null && isValid.equals("true")) && sPostFilter != null && !sPostFilter.isEmpty()) {
            Map<String, Object> result = workflowService.execute(sPostFilter, null, workingDir, stdin);
            isValid = (String) result.get("isValid");
            errorMessage = (String) result.get("errorMessage");
            exitCode = (Integer) result.get("exitCode");
            stderr = (String) result.get("stderr");
            // prepare for next step
            stdin = (String) result.get("stdout");
        }

        // at this point "stdin" holds the stdout
        stdout = stdin;

        Map<String, Object> mapStdout;
        if (stdout == null || stdout.isEmpty()) {
            mapStdout = (Map<String, Object>) DatasetUtils.deserialize("{}");
        } else if (stdout.startsWith("{")) {
            mapStdout = (Map<String, Object>) DatasetUtils.deserialize(stdout);
        } else {
            mapStdout = (Map<String, Object>) DatasetUtils.deserialize("{\"invalid_jsonout_by_service_task\":\"" + stdout + "\"}");
        }

        Map<String, Object> mapJsonOut;
        if (sJsonOut == null || sJsonOut.isEmpty()) {
            mapJsonOut = (Map<String, Object>) DatasetUtils.deserialize("{}");
        } else if (sJsonOut.startsWith("{")) {
            mapJsonOut = (Map<String, Object>) DatasetUtils.deserialize(sJsonOut);
        } else {
            mapJsonOut = (Map<String, Object>) DatasetUtils.deserialize("{\"invalid_jsonout_in_workflow_definition\":\"" + sJsonOut + "\"}");
        }

        // Merge stdout into jsonOut
        if (mapJsonOut == null) {
            mapJsonOut = mapStdout;
        } else {
            mapJsonOut.putAll(mapStdout);
        }

        if (mapJsonOut.get(IsValid) == null) {
            // at this point, we know the tool did not populate the isValid flag
            // put in CRIS's isValid and message
            mapJsonOut.put(IsValid, isValid);
            if (errorMessage != null && !errorMessage.isEmpty() && mapJsonOut.get(ErrorMessage) == null) {
                mapJsonOut.put(ErrorMessage, errorMessage);
            } else if (mapJsonOut.get(ErrorMessage) == null) {
                mapJsonOut.put(ErrorMessage, "<p>THIS IS GENERATED BY CRIS. UPDATE YOUR TOOL TO POPULATE \"isValid\" AND \"errorMessage\" FIELDS.</p> ");
            }
        }

        // convert isValid to boolean type
        Object oIsValid = mapJsonOut.get(IsValid);
        boolean bIsValid;
        if (oIsValid instanceof Boolean) {
            bIsValid = (boolean) oIsValid;
        } else {
            bIsValid = oIsValid.toString().equals("true");
        }
        mapJsonOut.put(IsValid, bIsValid);

        mapJsonOut.put("exitCode", exitCode);
        mapJsonOut.put("stderr", stderr);

        return mapJsonOut;
    }

}
