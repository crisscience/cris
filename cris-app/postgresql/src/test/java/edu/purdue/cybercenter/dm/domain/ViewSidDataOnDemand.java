package edu.purdue.cybercenter.dm.domain;

import java.security.SecureRandom;
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
public class ViewSidDataOnDemand {

    private Random rnd = new SecureRandom();

    private List<ViewSid> data;

    @Autowired
    private TenantDataOnDemand tenantDataOnDemand;

    @Autowired
    private UserDataOnDemand userDataOnDemand;

    @Autowired
    private GroupDataOnDemand groupDataOnDemand;

    public ViewSid getSpecificViewSid(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        ViewSid obj = data.get(index);
        return ViewSid.findViewSid(obj.getSid());
    }

    public ViewSid getRandomViewSid() {
        init();
        ViewSid obj = data.get(rnd.nextInt(data.size()));
        return ViewSid.findViewSid(obj.getSid());
    }

    public void init() {
        data = ViewSid.findViewSidEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'ViewSid' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        // add 5 users
        for (int i = 0; i < 5; i++) {
            User obj = userDataOnDemand.getNewTransientUser(i);
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
        }

        // add 5 groups
        for (int i = 0; i < 5; i++) {
            Group obj = groupDataOnDemand.getNewTransientGroup(i);
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
        }

        // now there should be 10 ViewSids
        data = ViewSid.findViewSidEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'ViewSid' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        throw new IllegalStateException("Find entries implementation for 'ViewSid' illegally returned empty resultset. should be 10");
    }
}
