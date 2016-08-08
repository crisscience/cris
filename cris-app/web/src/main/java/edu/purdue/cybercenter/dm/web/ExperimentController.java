package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.PermissionService;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/experiments")
@Controller
public class ExperimentController {

    @Autowired
    DomainObjectService domainObjectService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String list(Model model) {
        return "experiments/index";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Experiment.class);
    }

    @RequestMapping(value = "/json/names", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object listNames(HttpServletRequest request, HttpServletResponse response) {
        String sProjectId = request.getParameter("projectId");
        Integer projectId;
        if (sProjectId != null && !sProjectId.isEmpty()) {
            try {
                projectId = Integer.parseInt(sProjectId);
            } catch (Exception ex) {
                ResponseEntity<String> result = new ResponseEntity<>("Invalid project ID: " + sProjectId, HttpStatus.BAD_REQUEST);
                return result;
            }
        } else {
            projectId = 0;
        }

        Project project = null;
        if (projectId != 0) {
            project = Project.findProject(projectId);
        }

        List<Map<String, Object>> experimentList = new ArrayList<>();
        HashMap<String, Object> e = new HashMap<>();
        e.put("id", 0);
        e.put("name", "--ALL--");
        experimentList.add(0, e);

        List<Experiment> experiments;
        Map.Entry<String, String> orderBy = new HashMap.SimpleEntry<>("name", "+");
        if (project == null) {
            experiments = domainObjectService.findAll(orderBy, Experiment.class);
        } else {
            Map<String, Object> where = WebHelper.getWhere(project, null);
            experiments = domainObjectService.findBy(orderBy, where, Experiment.class);
        }

        for (Experiment experiment : experiments) {
            e = new HashMap<>();
            e.put("id", experiment.getId());
            e.put("name", experiment.getName());
            experimentList.add(e);
        }

        return Helper.deepSerialize(experimentList);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter("projectId") != null) {
            Session session = DomainObjectHelper.getHbmSession();
            Integer projectId = Integer.parseInt(request.getParameter("projectId"));
            if (!request.getParameter("projectId").startsWith("-")) {
                session.enableFilter("experimentInProjectFilter").setParameter("projectId", projectId);
            } else {
                session.enableFilter("experimentNotInProjectFilter").setParameter("projectId", -projectId);
            }
        }
        return WebJsonHelper.list(request, response, Experiment.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        ResponseEntity<String> result;
        try {
            result = WebJsonHelper.create(json, request, response, Experiment.class);
            ResponseEntity<String> responseEntity = (ResponseEntity<String>) result;
            Experiment experiment = DomainObjectUtils.fromJson(responseEntity.getBody(), request.getContextPath(), Experiment.class);
            permissionService.createAcl(experiment, experiment.getProjectId());
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("hasError", Boolean.TRUE);
            error.put("message", ex.getMessage());
            error.put("status", "Unable to create the experiment");
            result = new ResponseEntity<>(Helper.serialize(error), HttpStatus.BAD_REQUEST);
        }

        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object updateFromJson(@PathVariable("id") Integer id, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Object result;
        try {
            result = WebJsonHelper.update(json, request, response, Experiment.class);
            ResponseEntity<String> responseEntity = (ResponseEntity<String>) result;
            Experiment experiment = DomainObjectUtils.fromJson(responseEntity.getBody(), request.getContextPath(), Experiment.class);
            permissionService.createAcl(experiment, experiment.getProjectId());
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("hasError", Boolean.TRUE);
            error.put("message", ex.getMessage());
            error.put("status", "Unable to update the experiment");
            result = Helper.serialize(error);
        }

        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        ResponseEntity<String> result;
        try {
            result = WebJsonHelper.delete(id, request, response, Experiment.class);
        } catch (Exception ex) {
            result = new ResponseEntity<>("{\"message\": \"" + ex.getMessage() + ": Unable to delete the experiment" + "\"}", HttpStatus.OK);
        }
        return result;
    }
}
