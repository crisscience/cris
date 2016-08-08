/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.activiti;

import edu.purdue.cybercenter.dm.domain.Job;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author xu222
 */
@Configurable
public class CrisTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask dt) {
        String jobId = dt.getExecution().getProcessBusinessKey();
        Job job = Job.findJob(Integer.parseInt(jobId));
        if (job != null) {
            if (dt.getEventName().equals(TaskListener.EVENTNAME_CREATE)) {
                // Things to do when a task is created
            } else if (dt.getEventName().equals(TaskListener.EVENTNAME_COMPLETE)) {
                // Things to do when a task is completed
            } else if (dt.getEventName().equals(TaskListener.EVENTNAME_ASSIGNMENT)) {
                // Things to do when a task is assigned
            } else {
                // this shouldn't happen: activiti problem
                throw new RuntimeException("Unknown task event: " + dt.getEventName());
            }
        } else {
            // this shouldn't happen: cris problem
            throw new RuntimeException("Unknown job with job ID: " + jobId);
        }
    }
}
