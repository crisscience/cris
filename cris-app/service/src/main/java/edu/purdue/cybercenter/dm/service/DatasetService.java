/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import com.mongodb.util.JSON;
import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.JobContext;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.repository.ExperimentRepository;
import edu.purdue.cybercenter.dm.repository.JobContextRepository;
import edu.purdue.cybercenter.dm.repository.JobRepository;
import edu.purdue.cybercenter.dm.repository.ProjectRepository;
import edu.purdue.cybercenter.dm.util.ActivitiHelper;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class DatasetService {

    static final private String PostFilterRead = "hasGroup('Admin Group') or hasDatasetPermission(filterObject, 'read')";
    static final private String PreAuthorizeSave = "hasGroup('Admin Group') or hasDatasetPermission(#value, 'update')";
    static final private String PreAuthorizeCreate = "hasGroup('Admin Group') or hasDatasetPermission(#value, 'create')";
    static final private String PreAuthorizeRead = "hasGroup('Admin Group') or hasDatasetPermission(#value, 'read')";
    static final private String PreAuthorizeUpdate = "hasGroup('Admin Group') or hasDatasetPermission(#value, 'update')";
    static final private String PreAuthorizeDelete = "hasGroup('Admin Group') or hasDatasetPermission(#value, 'delete')";

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
    @PreAuthorize(PreAuthorizeSave)
    public Map putValue(UUID termUuid, UUID termVersion, Map<String, Object> value) {
        Map savedValue = null;

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

        if (termUuid != null) {
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

    @PostFilter(PostFilterRead)
    public List find(UUID termUuid, Map<String, Object> aggregators) {
        List<Map<String, Object>> results = documentService.find(termUuid, null, aggregators);
        return results;
    }

    @PostFilter(PostFilterRead)
    public List find(UUID termUuid, Map<String, Object> aggregators, File file) {
        List<Map<String, Object>> results = documentService.find(termUuid, null, aggregators, file);
        return results;
    }

    public long count(UUID termUuid, Map<String, Object> query) {
        return documentService.count(termUuid, null, query);
    }

    @PreAuthorize(PreAuthorizeDelete)
    public Object delete(UUID termUuid, Map<String, Object> value) {
        if (value != null) {
            documentService.delete(termUuid, null, value);
        }
        return value;
    }

    public String[] mergeValueToObjectus(Map<String, Object> objectuses, String key, Object value) {
        return mergeValueToObjectus(objectuses, key, value, false);
    }

    public String[] mergeValueToObjectus(Map<String, Object> objectuses, String key, Object value, boolean mergeArray) {
        TermName termName = new TermName(key);
        String templateName;
        String termAlias;
        Term term;
        if (termName.getUuid() != null) {
            Term template = termService.getTerm(termName.getUuid(), termName.getVersion(), true);
            templateName = new TermName(template).getName();
            termAlias = termName.getAlias();
            term = termService.getSubTerm(template, termAlias);
        } else {
            templateName = termName.getAlias();
            termAlias = "";
            term = null;
        }
        Object convertedValue = convertValue(term, value, null);
        String[] aliases = null;
        if (termAlias == null || termAlias.isEmpty()) {
            objectuses.put(templateName, convertedValue);
        } else {
            Map<String, Object> objectus;
            objectus = (Map<String, Object>) objectuses.get(templateName);
            if (objectus == null) {
                objectus = new HashMap<>();
                objectuses.put(templateName, objectus);
            }
            aliases = termAlias.split("\\.");
            if (aliases.length > 1) {
                for (int i = 0; i < aliases.length - 1; i++) {
                    if (objectus.get(aliases[i]) == null) {
                        objectus.put(aliases[i], new HashMap<>());
                    }
                    objectus = (Map<String, Object>) objectus.get(aliases[i]);
                }
            }
            String alias = aliases[aliases.length - 1];
            Object oldValue = objectus.get(alias);
            if (mergeArray && oldValue != null && (oldValue instanceof List || oldValue instanceof Object[])) {
                if (convertedValue != null) {
                    if (oldValue instanceof List) {
                        if (convertedValue instanceof List) {
                            ((List) oldValue).addAll((List) convertedValue);
                        } else {
                            ((List) oldValue).addAll(Arrays.asList((Object[]) convertedValue));
                        }
                    } else {
                        List newList = new ArrayList(Arrays.asList((Object[]) oldValue));
                        if (convertedValue instanceof List) {
                            newList.addAll((List) convertedValue);
                        } else {
                            newList.addAll(Arrays.asList((Object[]) convertedValue));
                        }
                        objectus.put(alias, newList);
                    }
                }
            } else {
                objectus.put(alias, convertedValue);
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

    //@PreAuthorize(PreAuthorizeUpdate)
    public void updateStateByJob(Integer stateId, Integer jobId, Set<String> templateUuids) {
        Set<String> collectionNames;
        if (templateUuids == null) {
            collectionNames = documentService.getCollectionNames();
        } else {
            collectionNames = DatasetUtils.uuidsToCollectionNames(templateUuids);
        }
        Map<String, Object> query = new HashMap<>();
        query.put(MetaField.JobId, jobId);
        Map<String, Object> data = new HashMap<>();
        data.put(MetaField.State, stateId);
        collectionNames.stream().forEach((collectionName) -> {
            documentService.update(collectionName, query, data);
        });
    }

    //@PreAuthorize(PreAuthorizeUpdate)
    public void updateState(Integer stateId, Map<String, Object> query, Set<String> templateUuids) {
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
