package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.PermissionService;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

@RequestMapping("/projects")
@Controller
public class ProjectController {

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        return "projects/index";
    }

    @RequestMapping(value = "/json/names", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object listNames(HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> projectList = new ArrayList<>();
        Map<String, Object> p = new HashMap<>();
        p.put("id", 0);
        p.put("name", "--ALL--");
        projectList.add(0, p);

        Map.Entry<String, String> orderBy = new HashMap.SimpleEntry<>("name", "+");
        List<Project> projects = domainObjectService.findAll(orderBy, Project.class);
        for (Project project : projects) {
            p = new HashMap<>();
            p.put("id", project.getId());
            p.put("name", project.getName());
            projectList.add(p);
        }

        return Helper.deepSerialize(projectList);
    }

    @RequestMapping(value = "/json/names/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getName(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> object = new HashMap<>();

        if (id == 0) {
            object.put("id", 0);
            object.put("name", "--ALL--");
        } else {
            Project project = domainObjectService.findById(id, Project.class);
            object.put("id", project.getId());
            object.put("name", project.getName());
        }

        return object;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Project.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.list(request, response, Project.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Object result;
        try {
            result = WebJsonHelper.create(json, request, response, Project.class);
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            User user = User.findUser(userId);
            if (!user.isAdmin()) {
                ResponseEntity<String> responseEntity = (ResponseEntity<String>) result;
                Project project = Helper.deserialize(responseEntity.getBody(), Project.class);
                permissionService.setPermission(userId, true, Project.class, project.getId(), "read", false);
            }
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("hasError", Boolean.TRUE);
            error.put("message", ex.getMessage());
            error.put("status", "Unable to create the project");
            result = Helper.serialize(error);
        }

        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object updateFromJson(@PathVariable("id") Integer id, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Object result;
        try {
            result = WebJsonHelper.update(json, request, response, Project.class);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("hasError", Boolean.TRUE);
            error.put("message", ex.getMessage());
            error.put("status", "Unable to update the project");
            result = Helper.serialize(error);
        }

        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        ResponseEntity<String> result;
        try {
            result = WebJsonHelper.delete(id, request, response, Project.class);
        } catch (Exception ex) {
            return new ResponseEntity<>("{\"message\": \"" + ex.getMessage() + ": Unable to delete the project" + "\"}", HttpStatus.OK);
        }
        return result;
    }
}
