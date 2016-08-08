package edu.purdue.cybercenter.dm.domain;

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
public class GroupDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Group> data;

	@Autowired
    private ClassificationDataOnDemand classificationDataOnDemand;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public Group getNewTransientGroup(int index) {
        Group obj = new Group();
        setClassificationId(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setEmail(obj, index);
        setImageId(obj, index);
        setName(obj, index);
        setOwnerId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setClassificationId(Group obj, int index) {
        Classification classificationId = classificationDataOnDemand.getRandomClassification();
        obj.setClassificationId(classificationId);
    }

	public void setCreatorId(Group obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Group obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setEmail(Group obj, int index) {
        String email = "email_" + index;
        if (email.length() > 100) {
            email = email.substring(0, 100);
        }
        obj.setEmail(email);
    }

	public void setImageId(Group obj, int index) {
        SmallObject imageId = null;
        obj.setImageId(imageId);
    }

	public void setName(Group obj, int index) {
        String name = "name_" + index;
        if (name.length() > 100) {
            name = new Random().nextInt(10) + name.substring(1, 100);
        }
        obj.setName(name);
    }

	public void setOwnerId(Group obj, int index) {
        User ownerId = userDataOnDemand.getRandomUser();
        obj.setOwnerId(ownerId);
    }

	public void setTenantId(Group obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Group obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Group obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Group obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public Group getSpecificGroup(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Group obj = data.get(index);
        return Group.findGroup(obj.getId());
    }

	public Group getRandomGroup() {
        init();
        Group obj = data.get(rnd.nextInt(data.size()));
        return Group.findGroup(obj.getId());
    }

	public boolean modifyGroup(Group obj) {
        return false;
    }

	public void init() {
        data = Group.findGroupEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Group' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Group>();
        for (int i = 0; i < 10; i++) {
            Group obj = getNewTransientGroup(i);
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
