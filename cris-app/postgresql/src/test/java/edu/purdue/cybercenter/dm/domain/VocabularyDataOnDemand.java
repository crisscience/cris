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
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class VocabularyDataOnDemand {

    public void setUuid(Vocabulary obj, int index) {
        String uuid = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
        obj.setUuid(UUID.fromString(uuid));
    }

    public void setContent(Vocabulary obj, int index) {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><vocabulary>" + index + "</vocabulary>";
        obj.setContent(content);
    }


	private Random rnd = new SecureRandom();

	private List<Vocabulary> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	@Autowired
    private SmallObjectDataOnDemand smallObjectDataOnDemand;

	public Vocabulary getNewTransientVocabulary(int index) {
        Vocabulary obj = new Vocabulary();
        setAssetTypeId(obj, index);
        setContent(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setDomain(obj, index);
        setImageId(obj, index);
        setName(obj, index);
        setOwnerId(obj, index);
        setStatusId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        setUuid(obj, index);
        setVersionNumber(obj, index);
        return obj;
    }

	public void setAssetTypeId(Vocabulary obj, int index) {
        obj.setAssetTypeId(EnumAssetType.Vocabulary.getIndex());
    }

	public void setCreatorId(Vocabulary obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Vocabulary obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setDomain(Vocabulary obj, int index) {
        String domain = "domain_" + index;
        if (domain.length() > 250) {
            domain = domain.substring(0, 250);
        }
        obj.setDomain(domain);
    }

	public void setImageId(Vocabulary obj, int index) {
        SmallObject imageId = smallObjectDataOnDemand.getRandomSmallObject();
        obj.setImageId(imageId);
    }

	public void setName(Vocabulary obj, int index) {
        String name = "name_" + index;
        if (name.length() > 250) {
            name = name.substring(0, 250);
        }
        obj.setName(name);
    }

	public void setOwnerId(Vocabulary obj, int index) {
        Group ownerId = groupDataOnDemand.getRandomGroup();
        obj.setOwnerId(ownerId);
    }

	public void setStatusId(Vocabulary obj, int index) {
        obj.setStatusId(EnumAssetStatus.Operational.getIndex());
    }

	public void setTenantId(Vocabulary obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Vocabulary obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Vocabulary obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Vocabulary obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public void setVersionNumber(Vocabulary obj, int index) {
        UUID versionNumber = UUID.randomUUID();
        obj.setVersionNumber(versionNumber);
    }

	public Vocabulary getSpecificVocabulary(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Vocabulary obj = data.get(index);
        return Vocabulary.findVocabulary(obj.getId());
    }

	public Vocabulary getRandomVocabulary() {
        init();
        Vocabulary obj = data.get(rnd.nextInt(data.size()));
        return Vocabulary.findVocabulary(obj.getId());
    }

	public boolean modifyVocabulary(Vocabulary obj) {
        return false;
    }

	public void init() {
        data = Vocabulary.findVocabularyEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Vocabulary' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Vocabulary>();
        for (int i = 0; i < 10; i++) {
            Vocabulary obj = getNewTransientVocabulary(i);
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
