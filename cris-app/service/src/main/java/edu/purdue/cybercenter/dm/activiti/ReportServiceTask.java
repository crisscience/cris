package edu.purdue.cybercenter.dm.activiti;

import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Report;
import edu.purdue.cybercenter.dm.domain.Term;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.service.CqlService;
import edu.purdue.cybercenter.dm.service.ReportService;
import edu.purdue.cybercenter.dm.util.AppConfigConst;
import edu.purdue.cybercenter.dm.util.ConstDatasetState;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.service.WorkflowService;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author xu222
 */
@Configurable
public class ReportServiceTask implements JavaDelegate {
    static final private String IsValid = "isValid";
    static final private String Cmd_GetParameters = "GetParameters";
    static final private String Cmd_GenerateReport = "GenerateReport";

    private Expression command;
    private Expression reportId;
    private Expression templateId;
    private Expression parameters;
    private Expression outputType;
    private Expression filesToCollect;

    // These fields are used by workflow editor
    // This class does not have any use of them but need to keep them here to satisfy the field injector.
    private Expression files;
    private Expression uuid;
    private Expression crisWorkflowImplementationVersion;
    private Expression uiLocation;
    private Expression orientation;

    @Autowired
    private ReportService reportService;

    @Autowired
    private CqlService cqlService;

    @Autowired
    private WorkflowService workflowService;

    @Override
    public void execute(DelegateExecution de) throws MalformedURLException, ResourceKeyCreationException, ResourceException, ResourceCreationException, IOException, ReportProcessingException, Exception {
        ExecutionEntity ee = (ExecutionEntity) de;

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

        String dirPath = AppConfigConst.getJobTmpPath() + AppConfigConst.FILE_SEPARATOR + jobId + "/";
        File workingDir = new File(dirPath);
        if (!workingDir.exists()) {
            workingDir.mkdir();
            throw new Exception("This shouldn't happen but no working directory exist for job " + jobId + ": " + dirPath);
        }

        String sReportCommand = command != null ? command.getExpressionText() : "";
        String sReportId = reportId != null ? reportId.getExpressionText() : null;
        Report report = Report.findReport(Integer.parseInt(sReportId));
        String reportTemplate = reportService.getReportTemplateFromId(report.getUuid());

        boolean isValid = true;
        String message = null;
        Map<String, Object> result = new HashMap<>();

        if (null != sReportCommand) switch (sReportCommand) {
            case Cmd_GetParameters:
                Map<String, String> parameterMap = reportService.getParameters(reportTemplate);
                result.put("reportParameters", parameterMap);
                break;
            case Cmd_GenerateReport:
                String sTemplateId = templateId != null ? templateId.getExpressionText() : null;
                Term template = Term.findTerm(Integer.parseInt(sTemplateId));
                String type = outputType != null ? outputType.getExpressionText() : "pdf";
                String sReportParameters = cqlService.eval(parameters != null ? parameters.getExpressionText() : null, context);
                Map<String, Object> reportParameters;
                if (sReportParameters != null && !sReportParameters.isEmpty()) {
                    reportParameters = Helper.deserialize(sReportParameters, Map.class);
                } else {
                    reportParameters = new HashMap<>();
                }
                reportParameters.putAll(context);
                //reportParameters.put("uuid", template.getUuid().toString());
                reportParameters.put("uuid", null);
                reportParameters.put("rootPath", dirPath);
                reportParameters.put("type", type);

                Map<String, String> reporta = reportService.generateReport(reportTemplate, reportParameters);

                result.put("report", reporta);
                result.put("fileUrl", "/download/JobFile:" + jobId + "/" + reporta.get("filename"));

                collectFiles(context);
                break;
            default:
                throw new RuntimeException("Unknown report command: " + sReportCommand == null ? "" : sReportCommand);
        }

        result.put(IsValid, isValid);
        result.put("error_message", message);

        for (String key : result.keySet()) {
            Object value = result.get(key);
            de.setVariable(key, value);
        }
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
}