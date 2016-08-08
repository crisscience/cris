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
public class SmallObjectDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<SmallObject> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public SmallObject getNewTransientSmallObject(int index) {
        SmallObject obj = new SmallObject();
        setContent(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setFilename(obj, index);
        setMimeType(obj, index);
        setName(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setContent(SmallObject obj, int index) {
        byte[] content = String.valueOf(index).getBytes();
        obj.setContent(content);
    }

	public void setCreatorId(SmallObject obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(SmallObject obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setFilename(SmallObject obj, int index) {
        String filename = "filename_" + index;
        if (filename.length() > 250) {
            filename = filename.substring(0, 250);
        }
        obj.setFilename(filename);
    }

	public void setMimeType(SmallObject obj, int index) {
        String mimeType = "mimeType_" + index;
        if (mimeType.length() > 100) {
            mimeType = mimeType.substring(0, 100);
        }
        obj.setMimeType(mimeType);
    }

	public void setName(SmallObject obj, int index) {
        String name = "name_" + index;
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        obj.setName(name);
    }

	public void setTenantId(SmallObject obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(SmallObject obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(SmallObject obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(SmallObject obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public SmallObject getSpecificSmallObject(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        SmallObject obj = data.get(index);
        return SmallObject.findSmallObject(obj.getId());
    }

	public SmallObject getRandomSmallObject() {
        init();
        SmallObject obj = data.get(rnd.nextInt(data.size()));
        return SmallObject.findSmallObject(obj.getId());
    }

	public boolean modifySmallObject(SmallObject obj) {
        return false;
    }

	public void init() {
        data = SmallObject.findSmallObjectEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'SmallObject' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.SmallObject>();
        for (int i = 0; i < 10; i++) {
            SmallObject obj = getNewTransientSmallObject(i);
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
