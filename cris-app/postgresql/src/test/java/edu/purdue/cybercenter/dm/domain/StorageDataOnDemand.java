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
public class StorageDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Storage> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public Storage getNewTransientStorage(int index) {
        Storage obj = new Storage();
        setAssetTypeId(obj, index);
        setCapacity(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setLocation(obj, index);
        setName(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setType(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setAssetTypeId(Storage obj, int index) {
        Integer assetTypeId = new Integer(index);
        obj.setAssetTypeId(assetTypeId);
    }

	public void setCapacity(Storage obj, int index) {
        Integer capacity = new Integer(index);
        obj.setCapacity(capacity);
    }

	public void setCreatorId(Storage obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Storage obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setLocation(Storage obj, int index) {
        String location = "location_" + index;
        if (location.length() > 256) {
            location = location.substring(0, 256);
        }
        obj.setLocation(location);
    }

	public void setName(Storage obj, int index) {
        String name = "name_" + index;
        if (name.length() > 256) {
            name = name.substring(0, 256);
        }
        obj.setName(name);
    }

	public void setTenantId(Storage obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Storage obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Storage obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setType(Storage obj, int index) {
        String type = "type_" + index;
        if (type.length() > 256) {
            type = type.substring(0, 256);
        }
        obj.setType(type);
    }

	public void setUpdaterId(Storage obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public Storage getSpecificStorage(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Storage obj = data.get(index);
        return Storage.findStorage(obj.getId());
    }

	public Storage getRandomStorage() {
        init();
        Storage obj = data.get(rnd.nextInt(data.size()));
        return Storage.findStorage(obj.getId());
    }

	public boolean modifyStorage(Storage obj) {
        return false;
    }

	public void init() {
        data = Storage.findStorageEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Storage' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Storage>();
        for (int i = 0; i < 10; i++) {
            Storage obj = getNewTransientStorage(i);
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
