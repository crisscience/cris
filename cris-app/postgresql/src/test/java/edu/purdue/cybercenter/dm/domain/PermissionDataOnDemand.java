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
public class PermissionDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Permission> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	@Autowired
    private EnumOperationDataOnDemand enumOperationDataOnDemand;

	@Autowired
    private RoleDataOnDemand roleDataOnDemand;

	@Autowired
    private EnumRoleTypeDataOnDemand enumRoleTypeDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public Permission getNewTransientPermission(int index) {
        Permission obj = new Permission();
        setCreatorId(obj, index);
        setGroupId(obj, index);
        setOperationId(obj, index);
        setRoleId(obj, index);
        setRoleTypeId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        setUserId(obj, index);
        return obj;
    }

	public void setCreatorId(Permission obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setGroupId(Permission obj, int index) {
        Group groupId = groupDataOnDemand.getRandomGroup();
        obj.setGroupId(groupId);
    }

	public void setOperationId(Permission obj, int index) {
        EnumOperation operationId = enumOperationDataOnDemand.getRandomEnumOperation();
        obj.setOperationId(operationId);
    }

	public void setRoleId(Permission obj, int index) {
        Role roleId = roleDataOnDemand.getRandomRole();
        obj.setRoleId(roleId);
    }

	public void setRoleTypeId(Permission obj, int index) {
        EnumRoleType roleTypeId = enumRoleTypeDataOnDemand.getRandomEnumRoleType();
        obj.setRoleTypeId(roleTypeId);
    }

	public void setTenantId(Permission obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Permission obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Permission obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Permission obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public void setUserId(Permission obj, int index) {
        User userId = userDataOnDemand.getRandomUser();
        obj.setUserId(userId);
    }

	public Permission getSpecificPermission(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Permission obj = data.get(index);
        return Permission.findPermission(obj.getId());
    }

	public Permission getRandomPermission() {
        init();
        Permission obj = data.get(rnd.nextInt(data.size()));
        return Permission.findPermission(obj.getId());
    }

	public boolean modifyPermission(Permission obj) {
        return false;
    }

	public void init() {
        data = Permission.findPermissionEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Permission' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Permission>();
        for (int i = 0; i < 10; i++) {
            Permission obj = getNewTransientPermission(i);
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
