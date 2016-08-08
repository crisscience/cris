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
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class SessionDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Session> data;

	public Session getNewTransientSession(int index) {
        Session obj = new Session();
        setHost(obj, index);
        setJsessionid(obj, index);
        setReferer(obj, index);
        setRequestUrl(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUserAgent(obj, index);
        return obj;
    }

	public void setHost(Session obj, int index) {
        String host = "host_" + index;
        obj.setHost(host);
    }

	public void setJsessionid(Session obj, int index) {
        String jsessionid = "jsessionid_" + index;
        if (jsessionid.length() > 500) {
            jsessionid = jsessionid.substring(0, 500);
        }
        obj.setJsessionid(jsessionid);
    }

	public void setReferer(Session obj, int index) {
        String referer = "referer_" + index;
        obj.setReferer(referer);
    }

	public void setRequestUrl(Session obj, int index) {
        String requestUrl = "requestUrl_" + index;
        obj.setRequestUrl(requestUrl);
    }

	public void setTimeCreated(Session obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Session obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUserAgent(Session obj, int index) {
        String userAgent = "userAgent_" + index;
        obj.setUserAgent(userAgent);
    }

	public Session getSpecificSession(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Session obj = data.get(index);
        return Session.findSession(obj.getId());
    }

	public Session getRandomSession() {
        init();
        Session obj = data.get(rnd.nextInt(data.size()));
        return Session.findSession(obj.getId());
    }

	public boolean modifySession(Session obj) {
        return false;
    }

	public void init() {
        data = Session.findSessionEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Session' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Session>();
        for (int i = 0; i < 10; i++) {
            Session obj = getNewTransientSession(i);
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
