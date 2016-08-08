package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.util.EnumJobStatus;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class JobDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Job> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	@Autowired
    private ProjectDataOnDemand projectDataOnDemand;

	@Autowired
    private ResourceDataOnDemand resourceDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	@Autowired
    private WorkflowDataOnDemand workflowDataOnDemand;

	public Job getNewTransientJob(int index) {
        Job obj = new Job();
        setCreatorId(obj, index);
        setDescription(obj, index);
        setExperimentId(obj, index);
        setGroupId(obj, index);
        setName(obj, index);
        setParentId(obj, index);
        setProjectId(obj, index);
        setResourceId(obj, index);
        setStatusId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        setUserId(obj, index);
        setWorkflowId(obj, index);
        return obj;
    }

	public void setCreatorId(Job obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Job obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setExperimentId(Job obj, int index) {
        Experiment experimentId = null;
        obj.setExperimentId(experimentId);
    }

	public void setGroupId(Job obj, int index) {
        Group groupId = groupDataOnDemand.getRandomGroup();
        obj.setGroupId(groupId);
    }

	public void setName(Job obj, int index) {
        String name = "name_" + index;
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        obj.setName(name);
    }

	public void setParentId(Job obj, int index) {
        Job parentId = obj;
        obj.setParentId(parentId);
    }

	public void setProjectId(Job obj, int index) {
        Project projectId = projectDataOnDemand.getRandomProject();
        obj.setProjectId(projectId);
    }

	public void setResourceId(Job obj, int index) {
        Resource resourceId = resourceDataOnDemand.getRandomResource();
        obj.setResourceId(resourceId);
    }

	public void setStatusId(Job obj, int index) {
        obj.setStatusId(EnumJobStatus.CREATED.getIndex());
    }

	public void setTenantId(Job obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Job obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Job obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Job obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public void setUserId(Job obj, int index) {
        User userId = userDataOnDemand.getRandomUser();
        obj.setUserId(userId);
    }

	public void setWorkflowId(Job obj, int index) {
        Workflow workflowId = workflowDataOnDemand.getRandomWorkflow();
        obj.setWorkflowId(workflowId);
    }

	public Job getSpecificJob(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Job obj = data.get(index);
        return Job.findJob(obj.getId());
    }

	public Job getRandomJob() {
        init();
        Job obj = data.get(rnd.nextInt(data.size()));
        return Job.findJob(obj.getId());
    }

	public boolean modifyJob(Job obj) {
        return false;
    }

	public void init() {
        data = Job.findJobEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Job' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Job>();
        for (int i = 0; i < 10; i++) {
            Job obj = getNewTransientJob(i);
            try {
                obj.persist();
            } catch (ConstraintViolationException e) {
                StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> it = e.getConstraintViolations().iterator(); it.hasNext();) {
                    ConstraintViolation<?> cv = it.next();
                    msg.append("[").append(cv.getConstraintDescriptor()).append(":").append(cv.getMessage()).append("=").append(cv.getInvalidValue()).append("]");
                }
                throw new RuntimeException(msg.toString(), e);
            }
            obj.flush();
            data.add(obj);
        }
    }
}
