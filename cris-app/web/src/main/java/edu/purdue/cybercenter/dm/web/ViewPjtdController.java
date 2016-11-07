package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.ViewPjtd;
import edu.purdue.cybercenter.dm.service.DatasetService;
import edu.purdue.cybercenter.dm.service.DocumentService;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/viewpjtds")
@Controller
public class ViewPjtdController {

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    /*********************************************************
     * tree
     *********************************************************/
    @RequestMapping(value={"/root"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getProjectsRoot(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"root\",");
        sb.append("\"name\":\"Project\",");
        sb.append("\"type\":\"project\",");
        sb.append("\"children\": ");
        sb.append("true");
        sb.append("}");
        return sb.toString();
    }

    @RequestMapping(value={"/project/root"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getProjects(HttpServletRequest request, HttpServletResponse response) {
        //Query query = Helper.createNamedQuery("Project.findAll");
        List<Project> items = domainObjectService.findAll(Project.class);
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (Project item : items) {
            Map<String, Object> mapItem = new HashMap<>();
            mapItem.put("id", item.getId());
            mapItem.put("name", item.getName());
            mapItem.put("type", "project");
            mapItem.put("children", (Experiment.countByProjectId(item) > 0));
            listItems.add(mapItem);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"root\",");
        sb.append("\"name\":\"Project\",");
        sb.append("\"type\":\"project\",");
        sb.append("\"children\": ");
        sb.append(Helper.serialize(listItems));
        sb.append("}");
        return sb.toString();
    }

    @RequestMapping(value="/project/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getExperiments(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        Project project = domainObjectService.findById(id, Project.class);
        TypedQuery query = DomainObjectHelper.createNamedQuery("Experiment.findByProject", Experiment.class).setParameter("projectId", project);
        List<Experiment> items = domainObjectService.executeTypedQueryWithResultList(query);
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (Experiment item : items) {
            Map<String, Object> mapItem = new HashMap<>();
            mapItem.put("id", item.getId());
            mapItem.put("name", item.getName());
            mapItem.put("type", "experiment");
            mapItem.put("children", (Job.countByExperimentId(item) > 0));
            listItems.add(mapItem);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\"").append(":").append(id).append(",");
        sb.append("\"name\"").append(":").append("\"").append(project.getName()).append("\"").append(",");
        sb.append("\"type\":\"experiment\",");
        sb.append("\"children\": ");
        sb.append(Helper.serialize(listItems));
        sb.append("}");
        return sb.toString();
    }

    @RequestMapping(value="/experiment/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getJobs(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        Session session = DomainObjectHelper.getHbmSession();
        session.enableFilter("jobStatusFilter").setParameterList("statusIds", Arrays.asList(1, 2, 3, 4, 6));

        Experiment experiment = domainObjectService.findById(id, Experiment.class);
        Query query = DomainObjectHelper.createNamedQuery("Job.findByExperiment").setParameter("experimentId", experiment);
        List<Job> items = query.getResultList();
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (Job item : items) {
            Map<String, Object> mapItem = new HashMap<>();
            mapItem.put("id", item.getId());
            mapItem.put("name", item.getName());
            mapItem.put("type", "job");
            mapItem.put("children", ViewPjtd.countByJobId(item.getId()) > 0 ? true : false);
            listItems.add(mapItem);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\"").append(":").append(id).append(",");
        sb.append("\"name\"").append(":").append("\"").append(experiment.getName()).append("\"").append(",");
        sb.append("\"type\":\"job\",");
        sb.append("\"children\": ");
        sb.append(Helper.serialize(listItems));
        sb.append("}");

        session.disableFilter("jobStatusFilter");

        return sb.toString();
    }

    @RequestMapping(value="/job/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getTasks(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        Job job = Job.findJob(id);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\"").append(":").append(id).append(",");
        sb.append("\"name\"").append(":").append("\"").append(job.getName()).append("\"").append(",");
        sb.append("\"type\":\"task\",");
        sb.append("\"children\": []");
        sb.append("}");

        return sb.toString();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, ViewPjtd.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        Integer[] ia = WebHelper.getDojoGridPaginationInfo(request);
        Integer firstResult = ia[0];
        Integer lastResult = ia[1];

        Map.Entry<String, String> orderBy = WebHelper.getDojoJsonRestStoreOrderBy(request.getParameterNames());
        Map<String, Object> where = WebHelper.FromJsonToFilterClass(request.getParameter("filter"));

        List<ViewPjtd> items;
        Integer totalCount;

        Project project = null;
        Experiment experiment = null;
        Job job = null;

        // verify that the user is permitted to access project, experiment, job or task
        if (where != null) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) where.get("data");
            Map<String, Object> data0 = data.get(0);
            Map<String, Object> data1 = data.get(1);
            boolean data0IsCol = (boolean) data0.get("isCol");
            String objectIdName;
            Integer objectIdValue;
            if (data0IsCol) {
                objectIdName = (String) data0.get("data");
                objectIdValue = (int) data1.get("data");
            } else {
                objectIdName = (String) data1.get("data");
                objectIdValue = (int) data0.get("data");
            }
            switch (objectIdName) {
                case "projectId":
                    project = domainObjectService.findById(objectIdValue, Project.class);
                    break;
                case "experimentId":
                    experiment = domainObjectService.findById(objectIdValue, Experiment.class);
                    break;
                case "jobId":
                    job = Job.findJob(objectIdValue);
                    break;
                case "taskId":
                    break;
                default:
            }

            // add a criteria that the task id cannot be null

            items = domainObjectService.findEntriesNoPermissionCheck(firstResult, lastResult - firstResult + 1, orderBy, where, ViewPjtd.class);
            totalCount = domainObjectService.countEntries(where, ViewPjtd.class).intValue();
        } else {
            // TODO: should be all permitted projects
            items = new ArrayList<>();
            totalCount = 0;
        }

        for (ViewPjtd item : items) {
            //TODO: get the values from mongodb
            //List<Map<String, Object>> result = datasetService.findByTemplateAndJobIdAndTaskId(item.getDataTermUuid(), item.getJobId(), item.getTaskId());
            UUID termUuid = item.getDataTermUuid();
            UUID termVersion = item.getDataTermVersion();
            Map<String, Object> query = new HashMap<>();
            if (job != null) {
                query.put(MetaField.JobId, item.getJobId());
            } else if (experiment != null) {
                query.put(MetaField.ExperimentId, item.getExperimentId());
            } else if (project != null) {
                query.put(MetaField.ProjectId, item.getProjectId());
            }

            query.put(MetaField.TemplateVersion, termVersion);
            /*
            if (item.getTaskId() != null) {
                query.put(MetaField.TaskId, item.getTaskId());
            }
            */

            if (termUuid != null) {
                Map<String, Object> aggregators = new HashMap<>();
                aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
                List object = datasetService.find(termUuid, aggregators);
                String result = DatasetUtils.serialize(object);
                item.setDataValue(result);
            }
        }

        WebHelper.setDojoGridPaginationInfo(firstResult, lastResult, totalCount, response);

        String result = DomainObjectUtils.toJsonArray(items, request.getContextPath());

        return result;
    }

}
