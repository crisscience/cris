/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.service.CqlService;
import edu.purdue.cybercenter.dm.service.DatasetService;
import edu.purdue.cybercenter.dm.service.DocumentService;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.TermService;
import edu.purdue.cybercenter.dm.util.ActivitiHelper;
import edu.purdue.cybercenter.dm.util.ConstDatasetState;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.util.TermName;
import edu.purdue.cybercenter.dm.service.WorkflowService;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 *
 * @author jiaxu
 */
@Controller
public class RestController {
    // a single flag to indicate whether a request has any error
    private static final String HasError = "hasError";
    // a single flag to indicate whether a request is processed successfully
    private static final String IsValid = "isValid";
    // general message about the request as a whole
    private static final String Messages = "messages";
    // processed request data
    private static final String Data = "data";
    // detailed error message for each data item. empty if there's no error
    private static final String Status = "status";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private TermService termService;
    @Autowired
    private CqlService cqlService;
    @Autowired
    private WorkflowService workflowService;

    /*********************************************************
     * Objectus
     *********************************************************/
    @RequestMapping(value = "/rest/objectus/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String validateJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        // build
        Map<String, Object> object = (Map<String, Object>) DatasetUtils.deserialize(json);
        Map<String, Object> objectuses = datasetService.buildObjectus(object.get(Data));

        // validate
        return validateJson(objectuses);
    }

    @RequestMapping(value = "/rest/objectus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object listJson(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        if (name == null || name.isEmpty()) {
            ResponseEntity<String> result = new ResponseEntity<>("Missing template name", HttpStatus.BAD_REQUEST);
            return result;
        }

        String query = request.getParameter("query");

        String result = listJson(name, query, request, response);

        return result;
    }

    @RequestMapping(value = "/rest/objectus", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String createFromMultipart(MultipartHttpServletRequest request, HttpServletResponse response) throws IOException {
        String sProjectId = request.getParameter("projectId");
        String sExperimentId = request.getParameter("experimentId");
        String sJobId = request.getParameter("jobId");
        String sTaskId = request.getParameter("taskId");
        Map<String, Object> objectuses = WebHelper.buildObjectus(request);

        Map<String, Object> result = saveFromJson(sProjectId, sExperimentId, sJobId, sTaskId, objectuses);

        String jsonResponse = DatasetUtils.serialize(result);

        return WebHelper.buildIframeResponse(jsonResponse);
    }

    @RequestMapping(value = "/rest/objectus", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Map object = (Map) DatasetUtils.deserialize(json);

        String sProjectId = (String) object.get("projectId");
        String sExperimentId = (String) object.get("experimentId");
        String sJobId = (String) object.get("jobId");
        String sTaskId = (String) object.get("taskId");
        Map<String, Object> objectus = datasetService.buildObjectus(object.get(Data));

        Map<String, Object> result = saveFromJson(sProjectId, sExperimentId, sJobId, sTaskId, objectus);

        return DatasetUtils.serialize(result);
    }

    @RequestMapping(value = "/rest/objectus", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String updateFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return createFromJson(json, request, response);
    }

    @RequestMapping(value = "/rest/objectus/validate/{datasetUuid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String validateDatasetJson(@PathVariable("datasetUuid") String datasetUuid, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        // build
        Object object = DatasetUtils.deserialize(json);
        Map<String, Object> objectuses = new HashMap<>();
        objectuses.put(datasetUuid, object);

        // validate
        return validateJson(objectuses);
    }

    @RequestMapping(value = "/rest/objectus/{datasetUuid}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String showDatasetJson(@PathVariable("datasetUuid") UUID datasetUuid, @PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        Object found;

        try {
            Map query = (Map) DatasetUtils.deserialize(String.format("{_id:{$oid:\"%s\"}}", id));
            Map<String, Object> aggregators = new HashMap<>();
            aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
            List object = datasetService.find(datasetUuid, aggregators);
            if (object == null || object.isEmpty()) {
                found = new HashMap<>();
            } else {
                found = object.get(0);
            }
        } catch (Exception ex) {
            found = new HashMap<>();
        }

        return DatasetUtils.serialize(found);
    }

    @RequestMapping(value = "/rest/objectus/{datasetUuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String listDatasetJson(@PathVariable("datasetUuid") String datasetUuid, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> params = request.getParameterMap();
        int size = request.getParameterMap().size();
        String query;
        if (size == 1 && !params.containsKey("query")) {
            String name = (String) request.getParameterNames().nextElement();
            String value = request.getParameter(name);
            if (value != null && !value.isEmpty()) {
                if (value.endsWith("*")) {
                    query = String.format("{%s : {$regex: '^%s', $options: 'i'}}", name, value.substring(0, value.length() - 1));
                } else {
                    query = String.format("{%s: %s}", name, value);
                }
            } else {
                query = value;
            }
        } else {
            query = request.getParameter("query");
        }

        String result = listJson(datasetUuid + "[]", query, request, response);

        return result;
    }

    @RequestMapping(value = "/rest/objectus/{datasetUuid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String createFromDatasetJson(@PathVariable("datasetUuid") String datasetUuid, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Map object = (Map) DatasetUtils.deserialize(json);

        Map<String, Object> wrappedObject = DatasetUtils.wrapWithTemplate(datasetUuid, object);

        Map<String, Object> result = saveFromJson(null, null, null, null, wrappedObject);

        return DatasetUtils.serialize(result);
    }

    @RequestMapping(value = "/rest/objectus/{datasetUuid}/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String updateFromDatasetJson(@PathVariable("datasetUuid") String datasetUuid, @PathVariable("id") String id, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        // for update the "id" is contained in the request body.
        return createFromDatasetJson(datasetUuid, json, request, response);
    }

    @RequestMapping(value = "/rest/objectus/{datasetUuid}/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String deleteFromDatasetJson(@PathVariable("datasetUuid") UUID datasetUuid, @PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        Map query = (Map) DatasetUtils.deserialize(String.format("{_id:{$oid:\"%s\"}}", id));
        List object = null;
        boolean isValid = false;
        String message = "";
        try {
            Map<String, Object> aggregators = new HashMap<>();
            aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
            object = datasetService.find(datasetUuid, aggregators);
            if (object == null || object.isEmpty()) {
                message = String.format("Unable to find record with id: %s", id);
            } else {
                Object deleted = datasetService.delete(datasetUuid, (Map) object.get(0));
                isValid = deleted != null;
                if (!isValid) {
                    message = String.format("Unable to delete record with id: %s", id);
                }
            }
        } catch (AccessDeniedException ex) {
            isValid = false;
            message = String.format("Unable to delete record with id: %s: %s", id, ex.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put(HasError, !isValid);
        result.put(IsValid, isValid);
        result.put(Messages, message);
        result.put(Data, object);
        result.put(Status, null);

        return DatasetUtils.serialize(result);
    }

    private String validateJson(Map<String, Object> objectuses) {
        // validate
        Map<String, Object> status = new HashMap<>();
        Boolean isValid = datasetService.validateValue(objectuses, status);

        Map<String, Object> result = new HashMap<>();
        List<String> messages = new ArrayList<>();
        result.put(HasError, !isValid);
        result.put(IsValid, isValid);
        result.put(Messages, messages);
        result.put(Data, objectuses);
        result.put(Status, status);

        return DatasetUtils.serialize(result);
    }

    private String listJson(String name, String paramsString, HttpServletRequest request, HttpServletResponse response) {
        if (paramsString == null || paramsString.trim().isEmpty()) {
            paramsString = "{}";
        }
        Map<String, Object> params = (Map<String, Object>) DatasetUtils.deserialize(paramsString);
        Map<String, Object> match;
        Object distinct;
        Map<String, Object> group;
        Map<String, Integer> sort;
        Integer skip;
        Integer limit;
        Map<String, Boolean> project;
        if (params.isEmpty()) {
            match = new HashMap<>();
            distinct = null;
            group = null;
            sort = null;
            skip = null;
            limit = null;
            project = null;
        } else {
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
                match = (Map) params.get("$query");
            } else {
                match = params;
            }
        }

        Integer jobId = (Integer) match.get(MetaField.Current + MetaField.JobId);
        if (jobId == null) {
            jobId = (Integer) match.get(MetaField.JobId);
        }
        if (jobId != null) {
            try {
                // TODO: check user permission
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(jobId.toString()).singleResult();
                if (processInstance != null) {
                    // job is still running
                    match.put(MetaField.Current + MetaField.JobId, jobId);
                } else {
                    // job is finished
                    match.remove(MetaField.Current + MetaField.JobId);
                }
            } catch (AccessDeniedException ex) {
                // user don't have the permission to access the current job
                match.remove(MetaField.Current + MetaField.JobId);
            }
        }

        String sVersion = request.getParameter("version");
        UUID version = null;
        try {
            version = UUID.fromString(sVersion);
        } catch (Exception ex) {
            // treat it as if there's no version specified
        }

        TermName termName = new TermName(name);
        if (termName.getVersion() == null && version != null) {
            termName.setVersion(version);
        }

        UUID templateUuid = termName.getUuid();
        UUID templateVersion = termName.getVersion();
        String alias = termName.getAlias();
        Boolean isList = termName.getIsList();

        if (templateVersion != null) {
            match.put(MetaField.TemplateVersion, templateVersion);
        }

        if (skip == null && limit == null) {
            Integer[] a = WebHelper.getDojoGridPaginationInfo(request);
            skip = a[0];
            limit = a[1] - skip + 1;
        }

        Map<String, Object> aggregators = new HashMap<>();
        aggregators.put(DocumentService.AGGREGATOR_MATCH, match);
        if (distinct != null && distinct instanceof Map) {
            aggregators.put(DocumentService.AGGREGATOR_DISTINCT, distinct);
        }
        aggregators.put(DocumentService.AGGREGATOR_GROUP, group);

        int count = (int) datasetService.count(templateUuid, aggregators);
        String result;

        String op = request.getParameter("op");
        if (op != null && op.equalsIgnoreCase("count")) {
            result = "" + count;
        } else {
            if (count > 0) {
                aggregators.put(DocumentService.AGGREGATOR_SORT, sort);
                aggregators.put(DocumentService.AGGREGATOR_SKIP, skip);
                aggregators.put(DocumentService.AGGREGATOR_LIMIT, limit);
                aggregators.put(DocumentService.AGGREGATOR_PROJECT, project);
                List object = datasetService.find(templateUuid, aggregators);
                int numOfRecord = object.size();
                if (distinct != null && distinct instanceof Boolean) {
                    object = (List) DatasetUtils.extractField(alias, object, (Boolean) distinct);
                } else {
                    object = (List) DatasetUtils.extractField(alias, object);
                }
                if (isList != null && isList) {
                    result = DatasetUtils.serialize(object);
                } else {
                    if (object.size() == 1) {
                        result = DatasetUtils.serialize(object.get(0));
                    } else if (object.isEmpty()) {
                        throw new RuntimeException(name + " has no records");
                    } else {
                        throw new RuntimeException(name + " has more than one records: " + numOfRecord);
                    }
                }
                if (skip != null) {
                    WebHelper.setDojoGridPaginationInfo(skip, skip + (numOfRecord - 1), count, response);
                }
            } else {
                if (skip != null) {
                    WebHelper.setDojoGridPaginationInfo(skip, skip, 0, response);
                }
                if (isList != null && isList) {
                    result = "[]";
                } else {
                    result = "null";
                }
            }
        }

        return result;
    }

    private Map<String, Object> saveFromJson(String sProjectId, String sExperimentId, String sJobId, String sTaskId, Map<String, Object> objectuses) {
        // validate
        Map<String, Object> mapErrorObjects = new HashMap<>();
        Boolean isValid = true;
        for (String name : objectuses.keySet()) {
            TermName termName = new TermName(name);
            UUID uuid = termName.getUuid();
            UUID version = termName.getVersion();
            Object value = objectuses.get(name);

            if (uuid != null) {
                Term term = termService.getTerm(uuid, version, true);
                Object errorObject = datasetService.validateValue(term, value);
                isValid = datasetService.isValid(errorObject) & isValid;
                mapErrorObjects.put(name, errorObject);
            }
        }

        Map<String, Object> resultObjectuses;

        // save if no error
        if (isValid) {
            Integer projectId;
            try {
                projectId = Integer.parseInt(sProjectId);
            } catch (NumberFormatException ex) {
                projectId = null;
            }

            Integer experimentId;
            try {
                experimentId = Integer.parseInt(sExperimentId);
            } catch (NumberFormatException ex) {
                experimentId = null;
            }

            Integer jobId;
            try {
                jobId = Integer.parseInt(sJobId);
            } catch (NumberFormatException ex) {
                jobId = null;
            }

            Task task = null;
            String activityId = null;
            Integer state = null;
            if (jobId != null) {
                Job job = Job.findJob(jobId);
                if (job != null) {
                    projectId = job.getProjectId().getId();
                    experimentId = job.getExperimentId().getId();
                }
                task = ActivitiHelper.taskIdToTask(sTaskId);
                if (task != null) {
                    HistoricActivityInstance activity = ActivitiHelper.taskToActivity(task);
                    activityId = activity.getActivityId();
                } else {
                    activityId = sTaskId;
                }

                // figure out the state for the dataset
                ProcessInstance processInstance = ActivitiHelper.jobToProcessInstance(job);
                if (processInstance != null) {
                    // the state set for the current step
                    state = (Integer) runtimeService.getVariable(processInstance.getId(), ConstDatasetState.DatasetState);
                    if (state == null) {
                        // otherwise use the initial state
                        state = (Integer) runtimeService.getVariable(processInstance.getId(), ConstDatasetState.InitialDatasetState);
                    }
                }
            }

            Map<String, Object> context = new HashMap<>();
            context.put(MetaField.ProjectId, projectId);
            context.put(MetaField.ExperimentId, experimentId);
            context.put(MetaField.JobId, jobId);
            context.put(MetaField.TaskId, activityId);

            if (state != null) {
                context.put(MetaField.State, state);
            }

            String processInstanceId = null;
            if (task != null) {
                processInstanceId = task.getProcessInstanceId();
            }

            resultObjectuses = workflowService.saveData(objectuses, processInstanceId, context);
        } else {
            resultObjectuses = objectuses;
        }

        List<String> messages = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        result.put(HasError, !isValid);
        result.put(IsValid, isValid);
        result.put(Messages, messages);
        result.put(Data, resultObjectuses);
        result.put(Status, DatasetUtils.deserialize(Helper.deepSerialize(mapErrorObjects)));

        return result;
    }
}
