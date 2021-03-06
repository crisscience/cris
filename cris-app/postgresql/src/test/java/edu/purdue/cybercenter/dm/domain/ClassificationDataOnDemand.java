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
public class ClassificationDataOnDemand {
    /*
    public Classification getNewTransientClassification(int index) {
        edu.purdue.cybercenter.dm.domain.Classification obj = new edu.purdue.cybercenter.dm.domain.Classification();
        obj.setCode(new Integer(index).toString());
        java.lang.String name = "name_" + index;
        if (name.length() > 100) {
            name  = name.substring(0, 100);
        }
        obj.setId(index);
        obj.setName(name);
        obj.setDescription("description_" + index);
        obj.setTimeCreated(new java.util.GregorianCalendar(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY), java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE), java.util.Calendar.getInstance().get(java.util.Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime());
        obj.setTimeUpdated(new java.util.GregorianCalendar(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), java.util.Calendar.getInstance().get(java.util.Calendar.MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH), java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY), java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE), java.util.Calendar.getInstance().get(java.util.Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime());
        return obj;
    }
    *
    */

    private Random rnd = new SecureRandom();

    private List<Classification> data;

    @Autowired
    private UserDataOnDemand userDataOnDemand;

    @Autowired
    private TenantDataOnDemand tenantDataOnDemand;

    public Classification getNewTransientClassification(int index) {
        Classification obj = new Classification();
        setCode(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setName(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

    public void setCode(Classification obj, int index) {
        String code = "code_" + index;
        if (code.length() > 100) {
            code = new Random().nextInt(10) + code.substring(1, 100);
        }
        obj.setCode(code);
    }

    public void setCreatorId(Classification obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

    public void setDescription(Classification obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

    public void setName(Classification obj, int index) {
        String name = "name_" + index;
        if (name.length() > 100) {
            name = new Random().nextInt(10) + name.substring(1, 100);
        }
        obj.setName(name);
    }

    public void setTenantId(Classification obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

    public void setTimeCreated(Classification obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

    public void setTimeUpdated(Classification obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

    public void setUpdaterId(Classification obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

    public Classification getSpecificClassification(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        Classification obj = data.get(index);
        return Classification.findClassification(obj.getId());
    }

    public Classification getRandomClassification() {
        init();
        Classification obj = data.get(rnd.nextInt(data.size()));
        return Classification.findClassification(obj.getId());
    }

    public boolean modifyClassification(Classification obj) {
        return false;
    }

    public void init() {
        data = Classification.findClassificationEntries(0, 10);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'Classification' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Classification>();
        for (int i = 0; i < 10; i++) {
            Classification obj = getNewTransientClassification(i);
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
