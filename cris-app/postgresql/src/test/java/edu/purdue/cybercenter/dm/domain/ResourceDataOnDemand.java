package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.util.EnumAssetStatus;
import edu.purdue.cybercenter.dm.util.EnumAssetType;
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
public class ResourceDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Resource> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	@Autowired
    private SmallObjectDataOnDemand smallObjectDataOnDemand;

	public Resource getNewTransientResource(int index) {
        Resource obj = new Resource();
        setAssetTypeId(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setEmail(obj, index);
        setImageId(obj, index);
        setName(obj, index);
        setOwnerId(obj, index);
        setPhysicalLocation(obj, index);
        setPhysicalLocationLabel(obj, index);
        setStatusId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setAssetTypeId(Resource obj, int index) {
        obj.setAssetTypeId(EnumAssetType.Resource.getIndex());
    }

	public void setCreatorId(Resource obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Resource obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setEmail(Resource obj, int index) {
        String email = "email_" + index;
        if (email.length() > 250) {
            email = email.substring(0, 250);
        }
        obj.setEmail(email);
    }

	public void setImageId(Resource obj, int index) {
        SmallObject imageId = smallObjectDataOnDemand.getRandomSmallObject();
        obj.setImageId(imageId);
    }

	public void setName(Resource obj, int index) {
        String name = "name_" + index;
        if (name.length() > 250) {
            name = name.substring(0, 250);
        }
        obj.setName(name);
    }

	public void setOwnerId(Resource obj, int index) {
        Group ownerId = groupDataOnDemand.getRandomGroup();
        obj.setOwnerId(ownerId);
    }

	public void setPhysicalLocation(Resource obj, int index) {
        String physicalLocation = "physicalLocation_" + index;
        if (physicalLocation.length() > 250) {
            physicalLocation = physicalLocation.substring(0, 250);
        }
        obj.setPhysicalLocation(physicalLocation);
    }

	public void setPhysicalLocationLabel(Resource obj, int index) {
        String physicalLocationLabel = "physicalLocationLabel_" + index;
        if (physicalLocationLabel.length() > 250) {
            physicalLocationLabel = physicalLocationLabel.substring(0, 250);
        }
        obj.setPhysicalLocationLabel(physicalLocationLabel);
    }

	public void setStatusId(Resource obj, int index) {
        obj.setStatusId(EnumAssetStatus.Operational.getIndex());
    }

	public void setTenantId(Resource obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Resource obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Resource obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Resource obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public Resource getSpecificResource(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Resource obj = data.get(index);
        return Resource.findResource(obj.getId());
    }

	public Resource getRandomResource() {
        init();
        Resource obj = data.get(rnd.nextInt(data.size()));
        return Resource.findResource(obj.getId());
    }

	public boolean modifyResource(Resource obj) {
        return false;
    }

	public void init() {
        data = Resource.findResourceEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Resource' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Resource>();
        for (int i = 0; i < 10; i++) {
            Resource obj = getNewTransientResource(i);
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
