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
public class RoleOperationDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<RoleOperation> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private EnumOperationDataOnDemand enumOperationDataOnDemand;

	@Autowired
    private RoleDataOnDemand roleDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public RoleOperation getNewTransientRoleOperation(int index) {
        RoleOperation obj = new RoleOperation();
        setCreatorId(obj, index);
        setOperationId(obj, index);
        setRoleId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setCreatorId(RoleOperation obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setOperationId(RoleOperation obj, int index) {
        EnumOperation operationId = enumOperationDataOnDemand.getRandomEnumOperation();
        obj.setOperationId(operationId);
    }

	public void setRoleId(RoleOperation obj, int index) {
        Role roleId = roleDataOnDemand.getRandomRole();
        obj.setRoleId(roleId);
    }

	public void setTenantId(RoleOperation obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(RoleOperation obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(RoleOperation obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(RoleOperation obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public RoleOperation getSpecificRoleOperation(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        RoleOperation obj = data.get(index);
        return RoleOperation.findRoleOperation(obj.getId());
    }

	public RoleOperation getRandomRoleOperation() {
        init();
        RoleOperation obj = data.get(rnd.nextInt(data.size()));
        return RoleOperation.findRoleOperation(obj.getId());
    }

	public boolean modifyRoleOperation(RoleOperation obj) {
        return false;
    }

	public void init() {
        data = RoleOperation.findRoleOperationEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'RoleOperation' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.RoleOperation>();
        for (int i = 0; i < 10; i++) {
            RoleOperation obj = getNewTransientRoleOperation(i);
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
