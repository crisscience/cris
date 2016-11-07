package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.util.EnumAssetStatus;
import edu.purdue.cybercenter.dm.util.EnumAssetType;
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

@Configurable
@Component
public class ProjectDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Project> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private SmallObjectDataOnDemand smallObjectDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public Project getNewTransientProject(int index) {
        Project obj = new Project();
        setAssetTypeId(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setEmail(obj, index);
        setImageId(obj, index);
        setName(obj, index);
        setOwnerId(obj, index);
        setStatusId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setAssetTypeId(Project obj, int index) {
        obj.setAssetTypeId(EnumAssetType.Project.getIndex());
    }

	public void setCreatorId(Project obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Project obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setEmail(Project obj, int index) {
        String email = "email_" + index;
        if (email.length() > 250) {
            email = email.substring(0, 250);
        }
        obj.setEmail(email);
    }

	public void setImageId(Project obj, int index) {
        Integer imageId = smallObjectDataOnDemand.getRandomSmallObject().getId();
        obj.setImageId(imageId);
    }

	public void setName(Project obj, int index) {
        String name = "name_" + index;
        if (name.length() > 250) {
            name = name.substring(0, 250);
        }
        obj.setName(name);
    }

	public void setOwnerId(Project obj, int index) {
        Integer ownerId = groupDataOnDemand.getRandomGroup().getId();
        obj.setOwnerId(ownerId);
    }

	public void setStatusId(Project obj, int index) {
        obj.setStatusId(EnumAssetStatus.Operational.getIndex());
    }

	public void setTenantId(Project obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Project obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Project obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Project obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public Project getSpecificProject(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Project obj = data.get(index);
        return Project.findProject(obj.getId());
    }

	public Project getRandomProject() {
        init();
        Project obj = data.get(rnd.nextInt(data.size()));
        return Project.findProject(obj.getId());
    }

	public boolean modifyProject(Project obj) {
        return false;
    }

	public void init() {
        data = Project.findProjectEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Project' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Project>();
        for (int i = 0; i < 10; i++) {
            Project obj = getNewTransientProject(i);
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
