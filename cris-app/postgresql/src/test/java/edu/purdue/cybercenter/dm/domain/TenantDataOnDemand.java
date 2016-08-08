package edu.purdue.cybercenter.dm.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class TenantDataOnDemand {

    public void setUuid(Tenant obj, int index) {
        obj.setUuid(UUID.randomUUID());
    }

	private Random rnd = new SecureRandom();

	private List<Tenant> data;

	public Tenant getNewTransientTenant(int index) {
        Tenant obj = new Tenant();
        setCreatorId(obj, index);
        setDescription(obj, index);
        setEmail(obj, index);
        setEnabled(obj, index);
        setImage(obj, index);
        setName(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        setUrlIdentifier(obj, index);
        setUuid(obj, index);
        return obj;
    }

	public void setCreatorId(Tenant obj, int index) {
        Integer creatorId = new Integer(index);
        obj.setCreatorId(creatorId);
    }

	public void setDescription(Tenant obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setEmail(Tenant obj, int index) {
        String email = "email_" + index;
        if (email.length() > 250) {
            email = email.substring(0, 250);
        }
        obj.setEmail(email);
    }

	public void setEnabled(Tenant obj, int index) {
        Boolean enabled = Boolean.TRUE;
        obj.setEnabled(enabled);
    }

	public void setImage(Tenant obj, int index) {
        byte[] image = String.valueOf(index).getBytes();
        obj.setImage(image);
    }

	public void setName(Tenant obj, int index) {
        String name = "name_" + index;
        if (name.length() > 250) {
            name = name.substring(0, 250);
        }
        obj.setName(name);
    }

	public void setTimeCreated(Tenant obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Tenant obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Tenant obj, int index) {
        Integer updaterId = new Integer(index);
        obj.setUpdaterId(updaterId);
    }

	public void setUrlIdentifier(Tenant obj, int index) {
        String urlIdentifier = "urlIdentifier_" + index;
        if (urlIdentifier.length() > 250) {
            urlIdentifier = urlIdentifier.substring(0, 250);
        }
        obj.setUrlIdentifier(urlIdentifier);
    }

	public Tenant getSpecificTenant(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Tenant obj = data.get(index);
        return Tenant.findTenant(obj.getId());
    }

	public Tenant getRandomTenant() {
        init();
        Tenant obj = data.get(rnd.nextInt(data.size()));
        return Tenant.findTenant(obj.getId());
    }

	public boolean modifyTenant(Tenant obj) {
        return false;
    }

	public void init() {
        data = Tenant.findTenantEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Tenant' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Tenant>();
        for (int i = 0; i < 10; i++) {
            Tenant obj = getNewTransientTenant(i);
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
