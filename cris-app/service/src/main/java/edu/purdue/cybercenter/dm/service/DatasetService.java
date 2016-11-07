/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import com.mongodb.util.JSON;
import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.JobContext;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.repository.ExperimentRepository;
import edu.purdue.cybercenter.dm.repository.JobContextRepository;
import edu.purdue.cybercenter.dm.repository.JobRepository;
import edu.purdue.cybercenter.dm.repository.ProjectRepository;
import edu.purdue.cybercenter.dm.util.ActivitiHelper;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.ServiceUtils;
import edu.purdue.cybercenter.dm.util.TermName;
import edu.purdue.cybercenter.dm.vocabulary.util.VocabularyUtils;
import edu.purdue.cybercenter.dm.vocabulary.validators.BooleanValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.CompositeValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.NumericValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.VocabularyValidator;
import edu.purdue.cybercenter.dm.vocabulary.validators.VocabularyValidatorImpl;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Query;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class DatasetService {

    static final public String PRE_AUTHORIZE_SAVE = "isAdmin() or isDatasetOwner(#value) or hasDatasetPermission(#value, 'update')";
    static final public String PRE_AUTHORIZE_DELETE = "isAdmin() or isDatasetOwner(#value) or hasDatasetPermission(#value, 'delete')";

    @Autowired
    private VocabularyValidator termValueValidator;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private TermService termService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private JobContextRepository jobContextRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SecurityService securityService;

    // values must be in json format
    public List<Map> putValues(UUID termUuid, UUID termVersion, List<Map<String, Object>> value, Map<String, Object> context) {
        List<Map> savedValues = new ArrayList<>();

        if (value == null) {
            return savedValues;
        }

        for (Map<String, Object> v : value) {
            v.putAll(context);
            Map savedValue = putValue(termUuid, termVersion, v);
            savedValues.add(savedValue);
        }

        return savedValues;
    }

    // value must be in jason format
    @PreAuthorize(PRE_AUTHORIZE_SAVE)
    public Map putValue(UUID termUuid, UUID termVersion, Map<String, Object> value) {
        Map savedValue = null;

        if (termUuid != null) {
            Integer jobId = null;
            if (value != null) {
                jobId = (Integer) value.get(MetaField.JobId);
            }

            UUID versionFromDataset = (UUID) value.get(MetaField.TemplateVersion);
            if (termVersion == null && versionFromDataset == null) {
                // use the most recent version of the template
                Term template = termService.getTerm(termUuid, null, false);
                termVersion = UUID.fromString(template.getVersion());
            }

            // added owner policy
            Integer groupId = edu.purdue.cybercenter.dm.threadlocal.GroupId.get();
            Group group = Group.findGroup(groupId);
            value.put(MetaField.IsGroupOwner, group == null ? null : group.getIsGroupOwner());

            savedValue = documentService.save(termUuid, termVersion, value);

            // remember the uuid of the template
            if (jobId != null) {
                ProcessInstance processInstance = ActivitiHelper.jobIdToProcessInstance(jobId);
                if (processInstance != null) {
                    // the job is still running
                    workflowService.updateJobTemplateUuids(termUuid.toString(), processInstance.getId());
                }
            }
        }

        return savedValue;
    }

    public List find(UUID termUuid, Map<String, Object> aggregators) {
        addSecurity(aggregators);
        List<Map<String, Object>> results = documentService.find(termUuid, null, aggregators);
        return results;
    }

    public List find(UUID termUuid, Map<String, Object> aggregators, File file) {
        addSecurity(aggregators);
        List<Map<String, Object>> results = documentService.find(termUuid, null, aggregators, file);
        return results;
    }

    public long count(UUID termUuid, Map<String, Object> aggregators) {
        addSecurity(aggregators);
        return documentService.count(termUuid, null, aggregators);
    }

    @PreAuthorize(PRE_AUTHORIZE_DELETE)
    public Object delete(UUID termUuid, Map<String, Object> value) {
        if (value != null) {
            documentService.delete(termUuid, null, value);
        }
        return value;
    }

    public String[] mergeValueToObjectus(Map<String, Object> objectuses, String key, Object value) {
        return mergeValueToObjectus(objectuses, key, value, false);
    }

    private void addSecurity(Map<String, Object> aggregators) {
        Boolean isAdmin = isAdmin();
        List<Integer> projectIds = securityService.getPermittedProjectIds();
        List<Integer> experimentIds = securityService.getPermittedExperimentIds();
        List<Integer> ownerProjectIds = securityService.getOwnerProjectIds();
        List<Integer> ownerExperimentIds = securityService.getOwnerExperimentIds();
        aggregators.put(DocumentService.IS_ADMIN, isAdmin);
        aggregators.put(DocumentService.PROJECT_IDS, projectIds);
        aggregators.put(DocumentService.EXPERIMENT_IDS, experimentIds);
        aggregators.put(DocumentService.OWNER_PROJECT_IDS, ownerProjectIds);
        aggregators.put(DocumentService.OWNER_EXPERIMENT_IDS, ownerExperimentIds);
    }

    private Boolean isAdmin() {
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        User user = User.findUser(userId);
        return user.isAdmin();
    }

    private Object getValue(String alias, Object parent) {
        Map<String, Object> result = ServiceUtils.processArrayNotation(alias);
        String base = (String) result.get("base");
        Integer index = (Integer) result.get("index");

        Object termValue = ((Map) parent).get(base);
        if (index == null) {
            // map
            if (termValue == null) {
                termValue = new HashMap();
                ((Map) parent).put(base, termValue);
            }
        } else {
            // list
            List list = (List) termValue;
            if (list == null) {
                list = new ArrayList();
                ((Map) parent).put(base, list);
            }
            if (list.size() < (index + 1)) {
                // fill the list up to index + 1 elements with null
                for (int i = list.size(); i < (index + 1); i++) {
                    list.add(null);
                }
            }
            termValue = list.get(index);
        }

        return termValue;
    }

    public String[] mergeValueToObjectus(Map<String, Object> objectuses, String key, Object value, boolean mergeArray) {
        TermName termName = new TermName(key);
        String templateName;
        String termPath;
        Term term;
        if (termName.getUuid() != null) {
            Term template = termService.getTerm(termName.getUuid(), termName.getVersion(), true);
            templateName = new TermName(template).getName();
            termPath = termName.getAlias();
            term = termService.getSubTerm(template, termPath);
        } else {
            templateName = termName.getAlias();
            termPath = null;
            term = null;
        }

        Object convertedValue = convertValue(term, value, null);
        String[] aliases = null;
        if (StringUtils.isEmpty(termPath)) {
            objectuses.put(templateName, convertedValue);
        } else {
            Map<String, Object> objectus;
            objectus = (Map<String, Object>) objectuses.get(templateName);
            if (objectus == null) {
                objectus = new HashMap<>();
                objectuses.put(templateName, objectus);
            }

            // find the value to be merged
            aliases = termPath.split("\\.");
            Object parent = objectus;
            for (int i = 0; i < aliases.length - 1; i++) {
                String alias = aliases[i];
                parent = getValue(alias, parent);
            }

            String termAlias = aliases[aliases.length - 1];
            Object termValue = getValue(termAlias, parent);

            Map<String, Object> result = ServiceUtils.processArrayNotation(termAlias);
            String base = (String) result.get("base");
            Integer index = (Integer) result.get("index");

            // merge values
            if (mergeArray && termValue != null && (termValue instanceof List || termValue instanceof Object[])) {
                // array
                if (convertedValue != null) {
                    List newList;
                    if (termValue instanceof List) {
                        newList = (List) termValue;
                    } else {
                        newList = new ArrayList(Arrays.asList((Object[]) termValue));
                    }
                    ((Map) parent).put(base, newList);

                    if (index != null) {
                        newList.set(index, convertedValue);
                    } else if (convertedValue instanceof List) {
                        newList.addAll((List) convertedValue);
                    } else {
                        newList.addAll(Arrays.asList((Object[]) convertedValue));
                    }
                }
            } else {
                // map
                ((Map) parent).put(base, convertedValue);
            }
        }

        return aliases;
    }

    public Map buildObjectusFromMap(Map<String, Object> object) {
        Map<String, Object> objectus = new HashMap<>();
        for (String key : object.keySet()) {
            mergeValueToObjectus(objectus, key, object.get(key));
        }
        return objectus;
    }

    public Map buildObjectus(String json) {
        Object object = JSON.parse(json);
        return buildObjectus(object);
    }

    public Map buildObjectus(Object object) {
        Map<String, Object> objectuses = new HashMap<>();
        if (object instanceof Map) {
            Map<String, Object> objectus = buildObjectusFromMap((Map) object);
            objectuses = objectus;
        } else if (object instanceof List) {
            for (Map item : (List<Map>) object) {
                Map<String, Object> objectus = buildObjectusFromMap(item);
                for (Map.Entry<String, Object> entry : objectus.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    List values;
                    if (!objectuses.containsKey(key)) {
                        values = new ArrayList<>();
                        objectuses.put(key, values);
                    } else {
                        values = (List) objectuses.get(key);
                    }
                    values.add(value);
                }
            }
        } else {
            String message = String.format("object must be either a Map or List: %s", object.toString());
            throw new RuntimeException(message);
        }

        return objectuses;
    }

    /*
     * attempt a convertion of value from string to the type defined by the term
     * The original value is returned if it fails for any reason
     */
    private Object convertValue(Term term, Object value, Object error) {
        if (term == null || value == null) {
            return value;
        }

        Object convertedValue;

        if (value instanceof List || value instanceof Object[]) {
            List listValue = new ArrayList();
            if (value instanceof List) {
                for (Object v : (List) value) {
                    Object newValue = convertValue(term, v, error);
                    listValue.add(newValue);
                }
            } else {
                for (Object v : (Object[]) value) {
                    Object newValue = convertValue(term, v, error);
                    listValue.add(newValue);
                }
            }

            boolean multipleFiles = VocabularyUtils.isMultipleFiles(term);
            if ((term.isList() == null || !term.isList()) && !multipleFiles && listValue.size() <= 1) {
                if (listValue.size() == 1) {
                    convertedValue = listValue.get(0);
                } else {
                    convertedValue = null;
                }
            } else {
                convertedValue = listValue;
            }
        } else if (value instanceof Map) {
            Map mapValue = new HashMap();
            for (Object key : ((Map) value).keySet()) {
                Term subTerm = termService.getSubTerm(term, (String) key);
                Object newValue = convertValue(subTerm, ((Map) value).get(key), error);
                mapValue.put(key, newValue != null ? newValue : ((Map) value).get(key));
            }
            convertedValue = mapValue;
        } else if (value instanceof org.bson.types.ObjectId) {
            convertedValue = value;
        } else {
            String stringValue = (String) value.toString();
            String type = termService.getType(term);
            if (StringUtils.isBlank(type) || CompositeValidator.validatorName.equals(type)) {
                // it's an object: deserialize the string
                convertedValue = DatasetUtils.deserialize(stringValue);
            } else if (BooleanValidator.validatorName.equals(type)) {
                convertedValue = stringValue != null && (stringValue.equalsIgnoreCase("true") || stringValue.equalsIgnoreCase("yes") || stringValue.equalsIgnoreCase("on"));
            } else if (NumericValidator.validatorName.equals(type)) {
                try {
                    convertedValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException ex0) {
                    try {
                        convertedValue = Double.parseDouble(stringValue);
                    } catch (NumberFormatException ex1) {
                        convertedValue = stringValue;
                    }
                }
            } else {
                convertedValue = stringValue;
            }
        }

        return convertedValue;
    }

    public boolean isValid(Object errorObject) {
        boolean isValid = VocabularyValidatorImpl.result(errorObject);
        return isValid;
    }

    public Object validateValue(Term term, Object value) {
        Object errorObject = termValueValidator.validate(term, value);
        return errorObject;
    }

    public boolean validateValue(Map<String, Object> objectus, Map<String, Object> status) {
        Boolean isValid = true;
        for (String name : objectus.keySet()) {
            TermName termName = new TermName(name);
            UUID uuid = termName.getUuid();
            UUID version = termName.getVersion();
            if (uuid != null && version != null) {
                Term term = termService.getTerm(uuid, version, true);
                Object value = objectus.get(name);
                Object errorObject = termValueValidator.validate(term, value);
                isValid = VocabularyValidatorImpl.result(errorObject) & isValid;
                status.put(name, errorObject);
            }
        }
        return isValid;
    }

    // THIS METHOD IS UNSAFE AND SHOULD ONLY BE USER INTERNALLY
    public Map<String, Object> findByIdUnsecured(UUID termUuid, ObjectId id) {
        Map<String, Object> doc = documentService.findById(termUuid, id);
        return doc;
    }

    // THIS METHOD IS UNSAFE AND SHOULD ONLY BE USER INTERNALLY
    public void updateStateUnsecured(Integer stateId, Map<String, Object> query, Set<String> templateUuids) {
        Set<String> collectionNames;
        if (templateUuids == null) {
            collectionNames = new HashSet<>();
        } else {
            collectionNames = DatasetUtils.uuidsToCollectionNames(templateUuids);
        }
        Map<String, Object> data = new HashMap<>();
        data.put(MetaField.State, stateId);
        collectionNames.stream().forEach((collectionName) -> {
            documentService.update(collectionName, query, data);
        });
    }

    private int makeContextId(String name, String value) {
        TermName termName = new TermName(name);
        UUID termUuid = termName.getUuid();
        UUID termVersion = termName.getVersion();
        String alias = termName.getAlias();
        if (alias == null || alias.isEmpty()) {
            Term template = termService.getTerm(termUuid, termVersion, false);
            alias = template.getName();
        }
        Query query = DomainObjectHelper.createNamedQuery("JobContext.countByUuidVersionName").setParameter("uuid", termUuid).setParameter("version", termVersion).setParameter("name", alias);
        Long count = (Long) query.getSingleResult();
        int contextId = 0;
        if (count == 0) {
            JobContext jobCtx = new JobContext();
            jobCtx.setTermUuid(termUuid);
            jobCtx.setTermVersion(termVersion);
            jobCtx.setName(alias);
            jobCtx.setValue(value);
            jobCtx = jobContextRepository.save(jobCtx);
            contextId = jobCtx.getId();
        }
        return contextId;
    }

    private int makeContextId(Project project, String name, String value) {
        TermName termName = new TermName(name);
        UUID termUuid = termName.getUuid();
        UUID termVersion = termName.getVersion();
        String alias = termName.getAlias();
        if (alias == null || alias.isEmpty()) {
            Term template = termService.getTerm(termUuid, termVersion, false);
            alias = template.getName();
        }
        Query query = DomainObjectHelper.createNamedQuery("JobContext.countByProjectUuidVersionName").setParameter("projectId", project).setParameter("uuid", termUuid).setParameter("version", termVersion).setParameter("name", alias);
        Long count = (Long) query.getSingleResult();
        int contextId = 0;
        if (count == 0) {
            JobContext jobCtx = new JobContext();
            jobCtx.setProjectId(project);
            jobCtx.setTermUuid(termUuid);
            jobCtx.setTermVersion(termVersion);
            jobCtx.setName(alias);
            jobCtx.setValue(value);
            jobCtx = jobContextRepository.save(jobCtx);
            contextId = jobCtx.getId();
        }
        return contextId;
    }

    private int makeContextId(Experiment experiment, String name, String value) {
        TermName termName = new TermName(name);
        UUID termUuid = termName.getUuid();
        UUID termVersion = termName.getVersion();
        String alias = termName.getAlias();
        if (alias == null || alias.isEmpty()) {
            Term template = termService.getTerm(termUuid, termVersion, false);
            alias = template.getName();
        }
        Project project = experiment.getProjectId();
        Query query = DomainObjectHelper.createNamedQuery("JobContext.countByProjectExperimentUuidVersionName").setParameter("projectId", project).setParameter("experimentId", experiment).setParameter("uuid", termUuid).setParameter("version", termVersion).setParameter("name", alias);
        Long count = (Long) query.getSingleResult();
        int contextId = 0;
        if (count == 0) {
            JobContext jobCtx = new JobContext();
            jobCtx.setProjectId(project);
            jobCtx.setExperimentId(experiment);
            jobCtx.setTermUuid(termUuid);
            jobCtx.setTermVersion(termVersion);
            jobCtx.setName(alias);
            jobCtx.setValue(value);
            jobCtx = jobContextRepository.save(jobCtx);
            contextId = jobCtx.getId();
        }
        return contextId;
    }

    private int makeContextId(Job job, String task, String name, String value) {
        TermName termName = new TermName(name);
        UUID termUuid = termName.getUuid();
        UUID termVersion = termName.getVersion();
        String alias = termName.getAlias();
        if (alias == null || alias.isEmpty()) {
            Term template = termService.getTerm(termUuid, termVersion, false);
            alias = template.getName();
        }
        Query query = DomainObjectHelper.createNamedQuery("JobContext.countByJobTaskUuidVersionName").setParameter("jobId", job).setParameter("task", task).setParameter("uuid", termUuid).setParameter("version", termVersion).setParameter("name", alias);
        Long count = (Long) query.getSingleResult();
        int contextId = 0;
        if (count == 0) {
            JobContext jobCtx = new JobContext();
            jobCtx.setProjectId(job.getProjectId());
            jobCtx.setExperimentId(job.getExperimentId());
            jobCtx.setJobId(job);
            jobCtx.setTask(task);
            jobCtx.setTermUuid(termUuid);
            jobCtx.setTermVersion(termVersion);
            jobCtx.setName(alias);
            jobCtx.setValue(value);
            jobCtx = jobContextRepository.save(jobCtx);
            contextId = jobCtx.getId();
        }
        return contextId;
    }

    public int makeContextId(Integer projectId, Integer experimentId, Integer jobId, String taskId, String name, String value) {
        int contextId;

        if (jobId != null) {
            Job job = jobRepository.findOne(jobId);
            contextId = makeContextId(job, taskId, name, value);
        } else if (experimentId != null) {
            Experiment experiment = experimentRepository.findOne(experimentId);
            contextId = makeContextId(experiment, name, value);
        } else if (projectId != null) {
            Project project = projectRepository.findOne(projectId);
            contextId = makeContextId(project, name, value);
        } else {
            contextId = makeContextId(name, value);
        }

        return contextId;
    }

}
