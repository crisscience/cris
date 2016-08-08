package edu.purdue.cybercenter.dm.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Configurable
@Component
public class EnumRoleTypeDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<EnumRoleType> data;

	public EnumRoleType getNewTransientEnumRoleType(int index) {
        EnumRoleType obj = new EnumRoleType();
        setCode(obj, index);
        setDescription(obj, index);
        setName(obj, index);
        return obj;
    }

	public void setCode(EnumRoleType obj, int index) {
        Integer code = new Integer(index);
        obj.setCode(code);
    }

	public void setDescription(EnumRoleType obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setName(EnumRoleType obj, int index) {
        String name = "name_" + index;
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        obj.setName(name);
    }

	public EnumRoleType getSpecificEnumRoleType(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        EnumRoleType obj = data.get(index);
        return EnumRoleType.findEnumRoleType(obj.getId());
    }

	public EnumRoleType getRandomEnumRoleType() {
        init();
        EnumRoleType obj = data.get(rnd.nextInt(data.size()));
        return EnumRoleType.findEnumRoleType(obj.getId());
    }

	public boolean modifyEnumRoleType(EnumRoleType obj) {
        return false;
    }

	public void init() {
        data = EnumRoleType.findEnumRoleTypeEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'EnumRoleType' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<edu.purdue.cybercenter.dm.domain.EnumRoleType>();
        for (int i = 0; i < 10; i++) {
            EnumRoleType obj = getNewTransientEnumRoleType(i);
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
