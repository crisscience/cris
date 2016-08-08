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
public class UserSessionDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<UserSession> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private SessionDataOnDemand sessionDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public UserSession getNewTransientUserSession(int index) {
        UserSession obj = new UserSession();
        setCreatorId(obj, index);
        setSessionId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        setUserId(obj, index);
        return obj;
    }

	public void setCreatorId(UserSession obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setSessionId(UserSession obj, int index) {
        Session sessionId = sessionDataOnDemand.getRandomSession();
        obj.setSessionId(sessionId);
    }

	public void setTenantId(UserSession obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(UserSession obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(UserSession obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(UserSession obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public void setUserId(UserSession obj, int index) {
        User userId = userDataOnDemand.getRandomUser();
        obj.setUserId(userId);
    }

	public UserSession getSpecificUserSession(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        UserSession obj = data.get(index);
        return UserSession.findUserSession(obj.getId());
    }

	public UserSession getRandomUserSession() {
        init();
        UserSession obj = data.get(rnd.nextInt(data.size()));
        return UserSession.findUserSession(obj.getId());
    }

	public boolean modifyUserSession(UserSession obj) {
        return false;
    }

	public void init() {
        data = UserSession.findUserSessionEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'UserSession' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.UserSession>();
        for (int i = 0; i < 10; i++) {
            UserSession obj = getNewTransientUserSession(i);
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
