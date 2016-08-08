/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.activiti;

import edu.purdue.cybercenter.dm.util.EnumJobStatus;
import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.util.AppConfigConst;
import edu.purdue.cybercenter.dm.util.ConstDatasetState;
import java.io.File;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author xu222
 */
@Configurable
public class CrisActivityListener implements ExecutionListener {
    private static final long serialVersionUID = 1L;

    private Expression datasetState;

    @Override
    public void notify(DelegateExecution de) throws Exception {
        String jobId = de.getProcessBusinessKey();
        Job job = Job.findJob(Integer.parseInt(jobId));
        if (job != null) {
            if (de.getEventName().equals(ExecutionListener.EVENTNAME_START)) {
                /*
                 * TODO: CRIS-597: this should be removed after we figure out what caused this problem
                 * make sure the working directory exists
                 */
                String workingDir = AppConfigConst.getJobTmpPath() + AppConfigConst.FILE_SEPARATOR + jobId;
                File file = new File(workingDir);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        throw new Exception("Unable to create working directory for job " + jobId + ": " + workingDir);
                    }
                }

                job.setStatusId(EnumJobStatus.STARTED.getIndex());
                job.merge();

                // set the dataset state for the current step
                String sDatasetState = datasetState != null ? datasetState.getExpressionText() : null;
                if (sDatasetState != null) {
                    Integer iDatasetState = Integer.parseInt(sDatasetState);
                    de.setVariable(ConstDatasetState.DatasetState, iDatasetState);
                }
            } else if (de.getEventName().equals(ExecutionListener.EVENTNAME_END)) {
                job.setStatusId(EnumJobStatus.FINISHED.getIndex());
                job.merge();

                // remove the dataset state for the current step
                String sDatasetState = datasetState != null ? datasetState.getExpressionText() : null;
                if (sDatasetState != null) {
                    de.removeVariable(ConstDatasetState.DatasetState);
                }
            } else if (de.getEventName().equals(ExecutionListener.EVENTNAME_TAKE)) {

            } else {
                throw new RuntimeException("Unknown activity event: " + de.getEventName());
            }
        } else {
            throw new RuntimeException("Unknown job with job ID: " + jobId);
        }
    }
}
