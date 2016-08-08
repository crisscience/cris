/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.repository.ExperimentRepository;
import edu.purdue.cybercenter.dm.repository.ProjectRepository;
import edu.purdue.cybercenter.dm.repository.StorageFileRepository;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.util.BalancedMatcher;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.util.TermName;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class CqlService {

    private final static String CONST_SYS_VAR_PROJECTS = "projects";
    private final static String CONST_SYS_VAR_EXPERIMENTS = "experiments";

    @Autowired
    private DatasetService datasetService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ExperimentRepository experimentRepository;
    @Autowired
    private StorageFileRepository storageFileRepository;

    public String eval(String source, Map<String, Object> context) {
        return eval(source, context, false);
    }

    public String eval(String source, Map<String, Object> context, boolean systemVarOnly) {
        if (source == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String[] patternStart;
        if (systemVarOnly) {
            patternStart = new String[]{"#{"};
        } else {
            patternStart = new String[]{"${", "#{"};
        }
        String[] patternEnd = new String[]{"}"};
        BalancedMatcher matcher = new BalancedMatcher(patternStart, patternEnd);
        matcher.compile(source);
        int startIndex = 0;
        while (matcher.find()) {
            sb.append(source.substring(startIndex, matcher.start()));
            String rawKey = source.substring(matcher.start() + 2, matcher.end() - 1);
            String key = eval(rawKey, context, systemVarOnly);
            String name;
            Boolean isRef = false;
            Boolean isUri = false;
            if (key.endsWith(":ref")) {
                isRef = true;
                name = key.substring(0, key.length() - 4);
            } else if (key.endsWith(":uri")) {
                isUri = true;
                name = key.substring(0, key.length() - 4);
            } else {
                name = key;
            }
            String value;
            if (source.substring(matcher.start(), matcher.start() + 1).equals("$")) {
                TermName termName = new TermName(name);
                if (termName.getUuid() == null) {
                    value = Helper.deepSerialize(getLocalValue(name, context));
                } else {
                    String paramsString = termName.getQueryString();
                    Map<String, Object> query;
                    Object distinct;
                    Map<String, Object> group;
                    Map<String, Integer> sort;
                    Integer skip;
                    Integer limit;
                    Map<String, Boolean> project;
                    if (StringUtils.isBlank(paramsString)) {
                        query = new HashMap<>();
                        distinct = false;
                        group = null;
                        sort = null;
                        skip = null;
                        limit = null;
                        project = null;
                    } else {
                        Map<String, Object> params = Helper.deserialize(paramsString, Map.class);

                        distinct = (Object) params.get(DocumentService.AGGREGATOR_DISTINCT);
                        group = (Map<String, Object>) params.get("$group");
                        if (params.get(DocumentService.AGGREGATOR_SORT) != null) {
                            // this syantax should be used
                            sort = (Map) params.get(DocumentService.AGGREGATOR_SORT);
                        } else {
                            // to be compatible with older version
                            sort = (Map) params.get("$orderby");
                        }
                        skip = (Integer) params.get(DocumentService.AGGREGATOR_SKIP);
                        limit = (Integer) params.get(DocumentService.AGGREGATOR_LIMIT);
                        project = (Map<String, Boolean>) params.get(DocumentService.AGGREGATOR_PROJECT);

                        params.remove(DocumentService.AGGREGATOR_DISTINCT);
                        params.remove(DocumentService.AGGREGATOR_GROUP);
                        params.remove(DocumentService.AGGREGATOR_SORT);
                        params.remove("$orderby");
                        params.remove(DocumentService.AGGREGATOR_SKIP);
                        params.remove(DocumentService.AGGREGATOR_LIMIT);
                        params.remove(DocumentService.AGGREGATOR_PROJECT);

                        if (params.containsKey("$query")) {
                            query = (Map) params.get("$query");
                        } else {
                            query = params;
                        }
                    }
                    if (context != null && context.containsKey(MetaField.JobId)) {
                        query.put(MetaField.Current + MetaField.JobId, context.get(MetaField.JobId));
                    }
                    Map<String, Object> aggregators = new HashMap<>();
                    aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
                    aggregators.put(DocumentService.AGGREGATOR_DISTINCT, group);
                    aggregators.put(DocumentService.AGGREGATOR_GROUP, group);
                    aggregators.put(DocumentService.AGGREGATOR_SORT, sort);
                    aggregators.put(DocumentService.AGGREGATOR_SKIP, skip);
                    aggregators.put(DocumentService.AGGREGATOR_LIMIT, limit);
                    aggregators.put(DocumentService.AGGREGATOR_PROJECT, project);
                    List objects = datasetService.find(termName.getUuid(), aggregators);
                    Object values;
                    if (termName.getIsList()) {
                        if (distinct != null && distinct instanceof Boolean) {
                            values = DatasetUtils.extractField(termName.getAlias(), objects, (Boolean) distinct);
                        } else {
                            values = DatasetUtils.extractField(termName.getAlias(), objects);
                        }
                    } else {
                        Map object = (Map) DatasetUtils.getOneOrNothing(objects);
                        values = DatasetUtils.extractField(termName.getAlias(), object);
                    }
                    value = DatasetUtils.serialize(values);
                }
            } else {
                value = Helper.serialize(getSystemValue(name, context));
            }

            String processedValue;
            if (isRef) {
                processedValue = deRef(value);
            } else if (isUri) {
                processedValue = getUri(value);
            } else {
                processedValue = value;
            }

            if (!processedValue.isEmpty() && matcher.start() > 0 && source.substring(matcher.start() - 1).startsWith("'")) {
                processedValue = processedValue.replaceAll("'", "\\\\'");
            } else if (!processedValue.isEmpty() && matcher.start() > 0 && source.substring(matcher.start() - 1).startsWith("\"")) {
                processedValue = processedValue.replaceAll("\"", "\\\\\"");
            }
            sb.append(processedValue);
            startIndex = matcher.end();
        }
        sb.append(source.substring(startIndex));
        return sb.toString();
    }

    private String deRef(String reference) {
        String[] parts = reference.split(":");
        String protocol;
        String path;
        if (parts.length != 2) {
            protocol = "file";
            path = reference;
        } else {
            protocol = parts[0];
            path = parts[1];
        }
        String content;
        if (protocol.compareTo("file") == 0) {
            content = Helper.fileToString(path);
        } else if (protocol.compareTo("StorageFile") == 0) {
            StorageFile sf = storageFileRepository.findOne(Integer.parseInt(path));
            try {
                StorageFileManager storageFileManager = storageService.getStorageFileManager(AccessMethodType.FILE);
                String location = storageFileManager.getFile(sf, "/tmp/", true);
                content = Helper.fileToString(location);
            } catch (Exception ex) {
                content = protocol + ":" + path + ": " + ex.getMessage();
            }
        } else {
            content = protocol + ":" + path + ": not implemented";
        }
        return content;
    }

    private String getUri(String reference) {
        String[] parts = reference.split(":");
        String protocol;
        String path;
        if (parts.length != 2) {
            protocol = "file";
            path = reference;
        } else {
            protocol = parts[0];
            path = parts[1];
        }
        String uri;
        if (protocol.compareTo("file") == 0) {
            uri = path;
        } else if (protocol.compareTo("StorageFile") == 0) {
            StorageFile sf = storageFileRepository.findOne(Integer.parseInt(path));
            try {
                StorageFileManager storageFileManager = storageService.getStorageFileManager(AccessMethodType.FILE);
                String location = storageFileManager.getFile(sf, "/tmp/", true);
                uri = location;
            } catch (Exception ex) {
                uri = protocol + ":" + path + ": " + ex.getMessage();
            }
        } else {
            uri = protocol + ":" + path;
        }
        return uri;
    }

    // for workflow local variables
    private Object getLocalValue(String name, Map<String, Object> context) {
        if (context == null) {
            return null;
        }

        Map<String, Object> localVariables = (Map<String, Object>) context.get(MetaField.LocalVariables);
        if (localVariables == null || localVariables.isEmpty()) {
            return null;
        }

        String[] parts = name.split("\\.");
        Object value;
        if (parts.length == 0) {
            value = null;
        } else {
            value = localVariables.get(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                if (value != null) {
                    value = ((Map) value).get(parts[i]);
                } else {
                    break;
                }
            }
        }

        return value;
    }

    private Object getPropertyValue(Object object, String propertyName) throws IllegalArgumentException, IllegalAccessException {
        Object value = null;

        Class<?> objectClass = object.getClass();
        do {
            try {
                Field field = objectClass.getDeclaredField(propertyName);
                field.setAccessible(true);
                value = field.get(objectClass.cast(object));
                break;
            } catch (NoSuchFieldException ex) {
            }
        } while ((objectClass = objectClass.getSuperclass()) != null);

        return value;
    }

    private Object getSystemValue(String name, Map<String, Object> context) {
        Object value;
        TermName termName = new TermName(name);
        String alias = termName.getAlias();
        String queryString = termName.getQueryString();
        Integer id = null;
        if (queryString != null) {
            Map<String, Object> queryMap = (Map<String, Object>) Helper.deserialize(queryString, Map.class);
            id = (Integer) queryMap.get(MetaField.Id);
        }
        if (CONST_SYS_VAR_PROJECTS.equals(alias)) {
            if (id == null) {
                List<Project> items = Project.findAllProjects();
                value = items;
            } else {
                Project item = projectRepository.findOne(id);
                value = item;
            }
        } else if (CONST_SYS_VAR_EXPERIMENTS.equals(alias)) {
            if (id == null) {
                List<Experiment> items = experimentRepository.findAll();
                value = items;
            } else {
                Experiment item = experimentRepository.findOne(id);
                value = item;
            }
        } else if (alias.startsWith(MetaField.Current)) {
            String path = alias.substring(MetaField.Current.length());
            int dotIndex = path.indexOf(".");
            int dashIndex = path.indexOf("_", 1);
            String objectName;
            String fieldName;

            if (dashIndex != -1) {
                // e.g. _job_id for compatibility
                if (context != null && context.get(path) != null) {
                    value = context.get(path);
                } else {
                    value = null;
                }
            } else {
                if (dotIndex == -1 && dashIndex == -1) {
                    // e.g. _job
                    objectName = path;
                    fieldName = null;
                } else {
                    // e.g. _job.id
                    objectName = path.substring(0, dotIndex);
                    fieldName = path.substring(dotIndex + 1);
                }
                if (objectName != null) {
                    Object object = context.get(objectName);
                    if (MetaField.Date.equals(objectName)) {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                        value = formatter.format(OffsetDateTime.now());
                    } else if (fieldName != null) {
                        try {
                            value = getPropertyValue(object, fieldName);
                        } catch (SecurityException | IllegalArgumentException ex) {
                            throw new RuntimeException("Object: " + objectName + " does not have field: " + fieldName, ex);
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException("Object: " + objectName + " unable to access field: " + fieldName, ex);
                        }
                    } else {
                        value = object;
                    }
                } else {
                    value = null;
                }
            }
        } else {
            value = null;
        }
        return value;
    }

}
