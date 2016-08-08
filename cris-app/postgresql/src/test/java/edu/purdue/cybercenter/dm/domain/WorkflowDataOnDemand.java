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

@Component
@Configurable
public class WorkflowDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Workflow> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	@Autowired
    private ResourceDataOnDemand resourceDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	@Autowired
    private SmallObjectDataOnDemand smallObjectDataOnDemand;

	public Workflow getNewTransientWorkflow(int index) {
        Workflow obj = new Workflow();
        setAssetTypeId(obj, index);
        setContent(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setEmail(obj, index);
        setImageId(obj, index);
        setKey(obj, index);
        setVersionNumber(obj, index);
        setName(obj, index);
        setOwnerId(obj, index);
        setResourceId(obj, index);
        setStatusId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setAssetTypeId(Workflow obj, int index) {
        obj.setAssetTypeId(EnumAssetType.Workflow.getIndex());
    }

	public void setContent(Workflow obj, int index) {
        String content = "content_" + index;
        obj.setContent(content);
    }

	public void setCreatorId(Workflow obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Workflow obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setEmail(Workflow obj, int index) {
        String email = "email_" + index;
        if (email.length() > 250) {
            email = email.substring(0, 250);
        }
        obj.setEmail(email);
    }

	public void setImageId(Workflow obj, int index) {
        SmallObject imageId = smallObjectDataOnDemand.getRandomSmallObject();
        obj.setImageId(imageId);
    }

	public void setKey(Workflow obj, int index) {
        String key = "key_" + index;
        if (key.length() > 250) {
            key = new Random().nextInt(10) + key.substring(1, 250);
        }
        obj.setKey(key);
    }

	public void setVersionNumber(Workflow obj, int index) {
        Integer versionNumber = new Integer(index);
        obj.setVersionNumber(versionNumber);
    }

	public void setName(Workflow obj, int index) {
        String name = "name_" + index;
        if (name.length() > 250) {
            name = name.substring(0, 250);
        }
        obj.setName(name);
    }

	public void setOwnerId(Workflow obj, int index) {
        Group ownerId = groupDataOnDemand.getRandomGroup();
        obj.setOwnerId(ownerId);
    }

	public void setResourceId(Workflow obj, int index) {
        Resource resourceId = resourceDataOnDemand.getRandomResource();
        obj.setResourceId(resourceId);
    }

	public void setStatusId(Workflow obj, int index) {
        obj.setStatusId(EnumAssetStatus.Operational.getIndex());
    }

	public void setTenantId(Workflow obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Workflow obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Workflow obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Workflow obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public Workflow getSpecificWorkflow(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Workflow obj = data.get(index);
        return Workflow.findWorkflow(obj.getId());
    }

	public Workflow getRandomWorkflow() {
        init();
        Workflow obj = data.get(rnd.nextInt(data.size()));
        return Workflow.findWorkflow(obj.getId());
    }

	public boolean modifyWorkflow(Workflow obj) {
        return false;
    }

	public void init() {
        data = Workflow.findWorkflowEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Workflow' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Workflow>();
        for (int i = 0; i < 10; i++) {
            Workflow obj = getNewTransientWorkflow(i);
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
