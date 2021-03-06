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
public class UserDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<User> data;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public User getNewTransientUser(int index) {
        User obj = new User();
        setAccountNonExpired(obj, index);
        setAccountNonLocked(obj, index);
        setCreatorId(obj, index);
        setCredentialsNonExpired(obj, index);
        setEmail(obj, index);
        setEnabled(obj, index);
        setExternalId(obj, index);
        setExternalSource(obj, index);
        setFirstName(obj, index);
        setImageId(obj, index);
        setLastName(obj, index);
        setMiddleName(obj, index);
        setPassword(obj, index);
        setSalt(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        setUsername(obj, index);
        return obj;
    }

	public void setAccountNonExpired(User obj, int index) {
        Boolean accountNonExpired = Boolean.TRUE;
        obj.setAccountNonExpired(accountNonExpired);
    }

	public void setAccountNonLocked(User obj, int index) {
        Boolean accountNonLocked = Boolean.TRUE;
        obj.setAccountNonLocked(accountNonLocked);
    }

	public void setCreatorId(User obj, int index) {
        User creatorId = obj;
        obj.setCreatorId(creatorId.getId());
    }

	public void setCredentialsNonExpired(User obj, int index) {
        Boolean credentialsNonExpired = Boolean.TRUE;
        obj.setCredentialsNonExpired(credentialsNonExpired);
    }

	public void setEmail(User obj, int index) {
        String email = "email_" + index;
        if (email.length() > 100) {
            email = email.substring(0, 100);
        }
        obj.setEmail(email);
    }

	public void setEnabled(User obj, int index) {
        Boolean enabled = Boolean.TRUE;
        obj.setEnabled(enabled);
    }

	public void setExternalId(User obj, int index) {
        String externalId = "externalId_" + index;
        if (externalId.length() > 100) {
            externalId = new Random().nextInt(10) + externalId.substring(1, 100);
        }
        obj.setExternalId(externalId);
    }

	public void setExternalSource(User obj, int index) {
        String externalSource = "externalSource_" + index;
        if (externalSource.length() > 100) {
            externalSource = new Random().nextInt(10) + externalSource.substring(1, 100);
        }
        obj.setExternalSource(externalSource);
    }

	public void setFirstName(User obj, int index) {
        String firstName = "firstName_" + index;
        if (firstName.length() > 100) {
            firstName = firstName.substring(0, 100);
        }
        obj.setFirstName(firstName);
    }

	public void setImageId(User obj, int index) {
        SmallObject imageId = null;
        obj.setImageId(imageId);
    }

	public void setLastName(User obj, int index) {
        String lastName = "lastName_" + index;
        if (lastName.length() > 100) {
            lastName = lastName.substring(0, 100);
        }
        obj.setLastName(lastName);
    }

	public void setMiddleName(User obj, int index) {
        String middleName = "middleName_" + index;
        if (middleName.length() > 100) {
            middleName = middleName.substring(0, 100);
        }
        obj.setMiddleName(middleName);
    }

	public void setPassword(User obj, int index) {
        String password = "password_" + index;
        if (password.length() > 100) {
            password = password.substring(0, 100);
        }
        obj.setPassword(password);
    }

	public void setSalt(User obj, int index) {
        String salt = "salt_" + index;
        if (salt.length() > 100) {
            salt = salt.substring(0, 100);
        }
        obj.setSalt(salt);
    }

	public void setTenantId(User obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(User obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(User obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(User obj, int index) {
        User updaterId = obj;
        obj.setUpdaterId(updaterId.getId());
    }

	public void setUsername(User obj, int index) {
        String username = "username_" + index;
        if (username.length() > 100) {
            username = new Random().nextInt(10) + username.substring(1, 100);
        }
        obj.setUsername(username);
    }

	public User getSpecificUser(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        User obj = data.get(index);
        return User.findUser(obj.getId());
    }

	public User getRandomUser() {
        init();
        User obj = data.get(rnd.nextInt(data.size()));
        return User.findUser(obj.getId());
    }

	public boolean modifyUser(User obj) {
        return false;
    }

	public void init() {
        data = User.findUserEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'User' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.User>();
        for (int i = 0; i < 10; i++) {
            User obj = getNewTransientUser(i);
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
