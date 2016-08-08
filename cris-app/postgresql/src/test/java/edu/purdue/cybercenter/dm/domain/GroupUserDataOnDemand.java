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

@Component
@Configurable
public class GroupUserDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<GroupUser> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public GroupUser getNewTransientGroupUser(int index) {
        GroupUser obj = new GroupUser();
        setCreatorId(obj, index);
        setGroupId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        setUserId(obj, index);
        return obj;
    }

	public void setCreatorId(GroupUser obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setGroupId(GroupUser obj, int index) {
        Group groupId = groupDataOnDemand.getRandomGroup();
        obj.setGroupId(groupId);
    }

	public void setTenantId(GroupUser obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(GroupUser obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(GroupUser obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(GroupUser obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public void setUserId(GroupUser obj, int index) {
        User userId = userDataOnDemand.getRandomUser();
        obj.setUserId(userId);
    }

	public GroupUser getSpecificGroupUser(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        GroupUser obj = data.get(index);
        return GroupUser.findGroupUser(obj.getId());
    }

	public GroupUser getRandomGroupUser() {
        init();
        GroupUser obj = data.get(rnd.nextInt(data.size()));
        return GroupUser.findGroupUser(obj.getId());
    }

	public boolean modifyGroupUser(GroupUser obj) {
        return false;
    }

	public void init() {
        data = GroupUser.findGroupUserEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'GroupUser' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.GroupUser>();
        for (int i = 0; i < 10; i++) {
            GroupUser obj = getNewTransientGroupUser(i);
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
