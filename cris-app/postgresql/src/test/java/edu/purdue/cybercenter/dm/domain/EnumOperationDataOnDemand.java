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

@Component
@Configurable
public class EnumOperationDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<EnumOperation> data;

	public EnumOperation getNewTransientEnumOperation(int index) {
        EnumOperation obj = new EnumOperation();
        setCode(obj, index);
        setDescription(obj, index);
        setName(obj, index);
        return obj;
    }

	public void setCode(EnumOperation obj, int index) {
        Integer code = new Integer(index);
        obj.setCode(code);
    }

	public void setDescription(EnumOperation obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setName(EnumOperation obj, int index) {
        String name = "name_" + index;
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        obj.setName(name);
    }

	public EnumOperation getSpecificEnumOperation(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        EnumOperation obj = data.get(index);
        return EnumOperation.findEnumOperation(obj.getId());
    }

	public EnumOperation getRandomEnumOperation() {
        init();
        EnumOperation obj = data.get(rnd.nextInt(data.size()));
        return EnumOperation.findEnumOperation(obj.getId());
    }

	public boolean modifyEnumOperation(EnumOperation obj) {
        return false;
    }

	public void init() {
        data = EnumOperation.findEnumOperationEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'EnumOperation' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<edu.purdue.cybercenter.dm.domain.EnumOperation>();
        for (int i = 0; i < 10; i++) {
            EnumOperation obj = getNewTransientEnumOperation(i);
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
