package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
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
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String list(Model model) {
        return "experiments/index";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Experiment.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        ResponseEntity<String> result = WebJsonHelper.create(json, request, response, Experiment.class);
        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object updateFromJson(@PathVariable("id") Integer id, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        ResponseEntity<String> result = WebJsonHelper.update(json, request, response, Experiment.class);
        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        ResponseEntity<String> result = WebJsonHelper.delete(id, request, response, Experiment.class);
        return result;
    }
}
