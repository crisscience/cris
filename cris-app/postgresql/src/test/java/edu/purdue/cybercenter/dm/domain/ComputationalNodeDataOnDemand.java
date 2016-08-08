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
public class ComputationalNodeDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<ComputationalNode> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public ComputationalNode getNewTransientComputationalNode(int index) {
        ComputationalNode obj = new ComputationalNode();
        setAssetTypeId(obj, index);
        setCapacity(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setIpAddress(obj, index);
        setLocation(obj, index);
        setName(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setType(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setAssetTypeId(ComputationalNode obj, int index) {
        Integer assetTypeId = new Integer(index);
        obj.setAssetTypeId(assetTypeId);
    }

	public void setCapacity(ComputationalNode obj, int index) {
        String capacity = "capacity_" + index;
        if (capacity.length() > 256) {
            capacity = capacity.substring(0, 256);
        }
        obj.setCapacity(capacity);
    }

	public void setCreatorId(ComputationalNode obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(ComputationalNode obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setIpAddress(ComputationalNode obj, int index) {
        String ipAddress = "ipAddress_" + index;
        if (ipAddress.length() > 256) {
            ipAddress = ipAddress.substring(0, 256);
        }
        obj.setIpAddress(ipAddress);
    }

	public void setLocation(ComputationalNode obj, int index) {
        String location = "location_" + index;
        if (location.length() > 256) {
            location = location.substring(0, 256);
        }
        obj.setLocation(location);
    }

	public void setName(ComputationalNode obj, int index) {
        String name = "name_" + index;
        if (name.length() > 256) {
            name = name.substring(0, 256);
        }
        obj.setName(name);
    }

	public void setTenantId(ComputationalNode obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(ComputationalNode obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(ComputationalNode obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setType(ComputationalNode obj, int index) {
        String type = "type_" + index;
        if (type.length() > 256) {
            type = type.substring(0, 256);
        }
        obj.setType(type);
    }

	public void setUpdaterId(ComputationalNode obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public ComputationalNode getSpecificComputationalNode(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        ComputationalNode obj = data.get(index);
        return ComputationalNode.findComputationalNode(obj.getId());
    }

	public ComputationalNode getRandomComputationalNode() {
        init();
        ComputationalNode obj = data.get(rnd.nextInt(data.size()));
        return ComputationalNode.findComputationalNode(obj.getId());
    }

	public boolean modifyComputationalNode(ComputationalNode obj) {
        return false;
    }

	public void init() {
        data = ComputationalNode.findComputationalNodeEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'ComputationalNode' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.ComputationalNode>();
        for (int i = 0; i < 10; i++) {
            ComputationalNode obj = getNewTransientComputationalNode(i);
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
