/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import edu.purdue.cybercenter.dm.domain.Job;
import java.util.List;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author xu222
 */
@Component
public class ActivitiHelper {

    private static RuntimeService runtimeService;
    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        ActivitiHelper.runtimeService = runtimeService;
    }

    private static TaskService taskService;
    @Autowired
    public void setTaskService(TaskService taskService) {
        ActivitiHelper.taskService = taskService;
    }

    private static HistoryService historyService;
    @Autowired
    public void setHistoryService(HistoryService historyService) {
        ActivitiHelper.historyService = historyService;
    }

    public static String taskIdToActivityId(String taskId) {
        Task task = taskIdToTask(taskId);
        HistoricActivityInstance activity = taskToActivity(task);
        String activityId = activity == null ? null : activity.getId();

        return activityId;
    }

    public static HistoricActivityInstance taskToActivity(Task task) {
        if (task == null) {
            return null;
        }

        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().executionId(task.getExecutionId()).activityId(task.getTaskDefinitionKey()).orderByHistoricActivityInstanceId().desc().list();

        if (!activities.isEmpty()) {
            return activities.get(0);
        }

        // this shouldn't happen
        throw new RuntimeException("There's no activity for task " + task.getId() + ": " + task.getName());
    }

    public static Task taskIdToTask(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return null;
        }

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        return task;
    }

    public static HistoricActivityInstance activityIdToActivity(String activityId) {
        if (activityId == null || activityId.isEmpty()) {
            return null;
        }

        HistoricActivityInstance activity = historyService.createHistoricActivityInstanceQuery().activityInstanceId(activityId).singleResult();

        return activity;
    }

    // Note: there are act_id_ and id_ in act_hi_actinst table. act_id_ is actually the task id of a process definition
    public static HistoricActivityInstance actIdToActivity(String actId, String processInstanceId) {
        if (actId == null || actId.isEmpty()) {
            return null;
        }

        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).activityId(actId).orderByHistoricActivityInstanceStartTime().desc().list();

        if (activities.size() > 0) {
            return activities.get(0);
        } else {
            return null;
        }
    }

    public static ProcessInstance jobToProcessInstance(Job job) {
        ProcessInstance processInstance = null;
        if (job != null) {
            processInstance = jobIdToProcessInstance(job.getId());
        }
        return processInstance;
    }

    public static ProcessInstance jobIdToProcessInstance(Integer jobId) {
        ProcessInstance processInstance = null;
        if (jobId != null) {
            processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(jobId.toString()).singleResult();
        }
        return processInstance;
    }

    public static ProcessInstance taskToProcessInstance(Task task) {
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return processInstance;
    }

    public static boolean isJobRunning(Job job) {
        boolean isJobRunning = false;
        if (job != null) {
            ProcessInstance processInstance = jobIdToProcessInstance(job.getId());
            isJobRunning = (processInstance != null);
        }
        return isJobRunning;
    }

}
