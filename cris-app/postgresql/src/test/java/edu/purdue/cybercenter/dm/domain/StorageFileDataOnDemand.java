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
public class StorageFileDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<StorageFile> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private StorageDataOnDemand storageDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public StorageFile getNewTransientStorageFile(int index) {
        StorageFile obj = new StorageFile();
        setAssetTypeId(obj, index);
        setCreatorId(obj, index);
        setName(obj, index);
        setLocation(obj, index);
        setSource(obj, index);
        setStorageId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setAssetTypeId(StorageFile obj, int index) {
        Integer assetTypeId = new Integer(index);
        obj.setAssetTypeId(assetTypeId);
    }

	public void setCreatorId(StorageFile obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setName(StorageFile obj, int index) {
        String name = "name_" + index;
        if (name.length() > 256) {
            name = name.substring(0, 256);
        }
        obj.setName(name);
    }

	public void setLocation(StorageFile obj, int index) {
        String location = "location_" + index;
        if (location.length() > 256) {
            location = location.substring(0, 256);
        }
        obj.setLocation(location);
    }

	public void setSource(StorageFile obj, int index) {
        String source = "source_" + index;
        if (source.length() > 256) {
            source = source.substring(0, 256);
        }
        obj.setSource(source);
    }

	public void setStorageId(StorageFile obj, int index) {
        Storage storageId = storageDataOnDemand.getRandomStorage();
        obj.setStorageId(storageId);
    }

	public void setTenantId(StorageFile obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(StorageFile obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(StorageFile obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(StorageFile obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public StorageFile getSpecificStorageFile(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        StorageFile obj = data.get(index);
        return StorageFile.findStorageFile(obj.getId());
    }

	public StorageFile getRandomStorageFile() {
        init();
        StorageFile obj = data.get(rnd.nextInt(data.size()));
        return StorageFile.findStorageFile(obj.getId());
    }

	public boolean modifyStorageFile(StorageFile obj) {
        return false;
    }

	public void init() {
        data = StorageFile.findStorageFileEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'StorageFile' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.StorageFile>();
        for (int i = 0; i < 10; i++) {
            StorageFile obj = getNewTransientStorageFile(i);
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
