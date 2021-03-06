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

@Component
@Configurable
public class ExperimentDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Experiment> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private SmallObjectDataOnDemand smallObjectDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	@Autowired
    private ProjectDataOnDemand projectDataOnDemand;

	@Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	public Experiment getNewTransientExperiment(int index) {
        Experiment obj = new Experiment();
        setAssetTypeId(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setEmail(obj, index);
        setImageId(obj, index);
        setName(obj, index);
        setOwnerId(obj, index);
        setProjectId(obj, index);
        setStatusId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        return obj;
    }

	public void setAssetTypeId(Experiment obj, int index) {
        obj.setAssetTypeId(EnumAssetType.Experiment.getIndex());
    }

	public void setCreatorId(Experiment obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Experiment obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setEmail(Experiment obj, int index) {
        String email = "email_" + index;
        if (email.length() > 250) {
            email = email.substring(0, 250);
        }
        obj.setEmail(email);
    }

	public void setImageId(Experiment obj, int index) {
        Integer imageId = smallObjectDataOnDemand.getRandomSmallObject().getId();
        obj.setImageId(imageId);
    }

	public void setName(Experiment obj, int index) {
        String name = "name_" + index;
        if (name.length() > 250) {
            name = name.substring(0, 250);
        }
        obj.setName(name);
    }

	public void setOwnerId(Experiment obj, int index) {
        Integer ownerId = groupDataOnDemand.getRandomGroup().getId();
        obj.setOwnerId(ownerId);
    }

	public void setProjectId(Experiment obj, int index) {
        Project projectId = projectDataOnDemand.getRandomProject();
        obj.setProjectId(projectId);
    }

	public void setStatusId(Experiment obj, int index) {
        obj.setStatusId(EnumAssetStatus.Operational.getIndex());
    }

	public void setTenantId(Experiment obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Experiment obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Experiment obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Experiment obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public Experiment getSpecificExperiment(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Experiment obj = data.get(index);
        return Experiment.findExperiment(obj.getId());
    }

	public Experiment getRandomExperiment() {
        init();
        Experiment obj = data.get(rnd.nextInt(data.size()));
        return Experiment.findExperiment(obj.getId());
    }

	public boolean modifyExperiment(Experiment obj) {
        return false;
    }

	public void init() {
        data = Experiment.findExperimentEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Experiment' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Experiment>();
        for (int i = 0; i < 10; i++) {
            Experiment obj = getNewTransientExperiment(i);
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
