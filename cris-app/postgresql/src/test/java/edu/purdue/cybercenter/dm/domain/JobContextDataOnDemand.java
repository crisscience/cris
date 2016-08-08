package edu.purdue.cybercenter.dm.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Configurable
@Component
public class JobContextDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<JobContext> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private JobDataOnDemand jobDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public JobContext getNewTransientJobContext(int index) {
        JobContext obj = new JobContext();
        setCreatorId(obj, index);
        setValue(obj, index);
        setJobId(obj, index);
        setName(obj, index);
        setStatus(obj, index);
        setTask(obj, index);
        setTenantId(obj, index);
        setTermUuid(obj, index);
        setTermVersion(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setCreatorId(JobContext obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setJobId(JobContext obj, int index) {
        Job jobId = jobDataOnDemand.getRandomJob();
        obj.setJobId(jobId);
    }

	public void setName(JobContext obj, int index) {
        String name = "name_" + index;
        if (name.length() > 250) {
            name = name.substring(0, 250);
        }
        obj.setName(name);
    }

	public void setStatus(JobContext obj, int index) {
        String status = "status_" + index;
        if (status.length() > 50) {
            status = status.substring(0, 50);
        }
        obj.setStatus(status);
    }

	public void setTask(JobContext obj, int index) {
        String task = "task_" + index;
        if (task.length() > 250) {
            task = task.substring(0, 250);
        }
        obj.setTask(task);
    }

	public void setTenantId(JobContext obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTermUuid(JobContext obj, int index) {
        UUID termUuid = null;
        obj.setTermUuid(termUuid);
    }

	public void setTermVersion(JobContext obj, int index) {
        UUID termVersion = null;
        obj.setTermVersion(termVersion);
    }

	public void setTimeCreated(JobContext obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(JobContext obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(JobContext obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public void setValue(JobContext obj, int index) {
        String value = "value_" + index;
        obj.setValue(value);
    }

	public JobContext getSpecificJobContext(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        JobContext obj = data.get(index);
        return JobContext.findJobContext(obj.getId());
    }

	public JobContext getRandomJobContext() {
        init();
        JobContext obj = data.get(rnd.nextInt(data.size()));
        return JobContext.findJobContext(obj.getId());
    }

	public boolean modifyJobContext(JobContext obj) {
        return false;
    }

	public void init() {
        data = JobContext.findJobContextEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'JobContext' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.JobContext>();
        for (int i = 0; i < 10; i++) {
            JobContext obj = getNewTransientJobContext(i);
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
