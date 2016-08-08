/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.activiti;

import edu.purdue.cybercenter.dm.util.EnumJobStatus;
import edu.purdue.cybercenter.dm.domain.Job;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author xu222
 */
@Configurable
public class CrisTransitionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution de) throws Exception {
        String jobId = de.getProcessBusinessKey();
        Job job = Job.findJob(Integer.parseInt(jobId));
        if (job != null) {
            if (de.getEventName().equals(ExecutionListener.EVENTNAME_START)) {
                job.setStatusId(EnumJobStatus.STARTED.getIndex());
                job.merge();
            } else if (de.getEventName().equals(ExecutionListener.EVENTNAME_END)) {
                job.setStatusId(EnumJobStatus.FINISHED.getIndex());
                job.merge();
            } else if (de.getEventName().equals(ExecutionListener.EVENTNAME_TAKE)) {

            } else {
                throw new RuntimeException("Unknown transition event: " + de.getEventName());
            }
        } else {
            throw new RuntimeException("Unknown job with job ID: " + jobId);
        }
    }
}
