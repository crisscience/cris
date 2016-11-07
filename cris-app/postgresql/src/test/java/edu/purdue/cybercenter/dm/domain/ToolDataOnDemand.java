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

@Configurable
@Component
public class ToolDataOnDemand {

	private Random rnd = new SecureRandom();

	private List<Tool> data;

	@Autowired
    private UserDataOnDemand userDataOnDemand;

	@Autowired
    private GroupDataOnDemand groupDataOnDemand;

	//    @Autowired
//    private TemplateDataOnDemand ToolDataOnDemand.templateDataOnDemand;

    @Autowired
    private TenantDataOnDemand tenantDataOnDemand;

	@Autowired
    private SmallObjectDataOnDemand smallObjectDataOnDemand;

	public Tool getNewTransientTool(int index) {
        Tool obj = new Tool();
        setAssetTypeId(obj, index);
        setCommandLine(obj, index);
        setCreatorId(obj, index);
        setDescription(obj, index);
        setEmail(obj, index);
        setImageId(obj, index);
        setName(obj, index);
        setOwnerId(obj, index);
        setPostFilter(obj, index);
        setPreFilter(obj, index);
        setStatusId(obj, index);
//        setTemplateId(obj, index);
        setTenantId(obj, index);
        setTimeCreated(obj, index);
        setTimeUpdated(obj, index);
        setUpdaterId(obj, index);
        setUuid(obj, index);
        setVersionNumber(obj, index);
        setKey(obj, index);
        return obj;
    }

	public void setAssetTypeId(Tool obj, int index) {
        obj.setAssetTypeId(EnumAssetType.Tool.getIndex());
    }

	public void setCommandLine(Tool obj, int index) {
        String commandLine = "commandLine_" + index;
        obj.setCommandLine(commandLine);
    }

	public void setCreatorId(Tool obj, int index) {
        User creatorId = userDataOnDemand.getRandomUser();
        obj.setCreatorId(creatorId.getId());
    }

	public void setDescription(Tool obj, int index) {
        String description = "description_" + index;
        obj.setDescription(description);
    }

	public void setEmail(Tool obj, int index) {
        String email = "email_" + index;
        if (email.length() > 250) {
            email = email.substring(0, 250);
        }
        obj.setEmail(email);
    }

	public void setImageId(Tool obj, int index) {
        Integer imageId = smallObjectDataOnDemand.getRandomSmallObject().getId();
        obj.setImageId(imageId);
    }

	public void setName(Tool obj, int index) {
        String name = "name_" + index;
        if (name.length() > 250) {
            name = name.substring(0, 250);
        }
        obj.setName(name);
    }

	public void setOwnerId(Tool obj, int index) {
        Integer ownerId = groupDataOnDemand.getRandomGroup().getId();
        obj.setOwnerId(ownerId);
    }

	public void setPostFilter(Tool obj, int index) {
        String postFilter = "postFilter_" + index;
        obj.setPostFilter(postFilter);
    }

	public void setPreFilter(Tool obj, int index) {
        String preFilter = "preFilter_" + index;
        obj.setPreFilter(preFilter);
    }

	public void setStatusId(Tool obj, int index) {
        obj.setStatusId(EnumAssetStatus.Operational.getIndex());
    }

	//    public void ToolDataOnDemand.setTemplateId(Tool obj, int index) {
//        Template templateId = templateDataOnDemand.getRandomTemplate();
//        obj.setTemplateId(templateId);
//    }

    public void setTenantId(Tool obj, int index) {
        Tenant tenantId = tenantDataOnDemand.getRandomTenant();
        obj.setTenantId(tenantId.getId());
    }

	public void setTimeCreated(Tool obj, int index) {
        Date timeCreated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeCreated(timeCreated);
    }

	public void setTimeUpdated(Tool obj, int index) {
        Date timeUpdated = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setTimeUpdated(timeUpdated);
    }

	public void setUpdaterId(Tool obj, int index) {
        User updaterId = userDataOnDemand.getRandomUser();
        obj.setUpdaterId(updaterId.getId());
    }

	public void setUuid(Tool obj, int index) {
        obj.setUuid(UUID.randomUUID());
    }

	public void setVersionNumber(Tool obj, int index) {
        obj.setVersionNumber(UUID.randomUUID());
    }

	public void setKey(Tool obj, int index) {
        String key = "key_" + index;
        if (key.length() > 250) {
            key = key.substring(0, 250);
        }
        obj.setKey(key);
    }

	public Tool getSpecificTool(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Tool obj = data.get(index);
        return Tool.findTool(obj.getId());
    }

	public Tool getRandomTool() {
        init();
        Tool obj = data.get(rnd.nextInt(data.size()));
        return Tool.findTool(obj.getId());
    }

	public boolean modifyTool(Tool obj) {
        return false;
    }

	public void init() {
        data = Tool.findToolEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Tool' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<edu.purdue.cybercenter.dm.domain.Tool>();
        for (int i = 0; i < 10; i++) {
            Tool obj = getNewTransientTool(i);
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
