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
public class ConfigurationDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Configuration> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public Configuration getNewTransientConfiguration(int index) {
        Configuration obj = new Configuration();
        setCreatorId(obj, index);
        setDescription(obj, index);
        setName(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setType(obj, index);
        setUpdaterId(obj, index);
        setValueBinary(obj, index);
        setValueBool(obj, index);
        setValueInteger(obj, index);
        setValueText(obj, index);
        return obj;
    }

	public void setCreatorId(Configuration obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Configuration obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setName(Configuration obj, int index) {
        String name = "name_" + index;
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        obj.setName(name);
    }

	public void setTenantId(Configuration obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Configuration obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Configuration obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setType(Configuration obj, int index) {
        String type = "type_" + index;
        if (type.length() > 100) {
            type = type.substring(0, 100);
        }
        obj.setType(type);
    }

	public void setUpdaterId(Configuration obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public void setValueBinary(Configuration obj, int index) {
        byte[] valueBinary = String.valueOf(index).getBytes();
        obj.setValueBinary(valueBinary);
    }

	public void setValueBool(Configuration obj, int index) {
        Boolean valueBool = Boolean.TRUE;
        obj.setValueBool(valueBool);
    }

	public void setValueInteger(Configuration obj, int index) {
        Integer valueInteger = new Integer(index);
        obj.setValueInteger(valueInteger);
    }

	public void setValueText(Configuration obj, int index) {
        String valueText = "valueText_" + index;
        obj.setValueText(valueText);
    }

	public Configuration getSpecificConfiguration(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Configuration obj = data.get(index);
        return Configuration.findConfiguration(obj.getId());
    }

	public Configuration getRandomConfiguration() {
        init();
        Configuration obj = data.get(rnd.nextInt(data.size()));
        return Configuration.findConfiguration(obj.getId());
    }

	public boolean modifyConfiguration(Configuration obj) {
        return false;
    }

	public void init() {
        data = Configuration.findConfigurationEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Configuration' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Configuration>();
        for (int i = 0; i < 10; i++) {
            Configuration obj = getNewTransientConfiguration(i);
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
