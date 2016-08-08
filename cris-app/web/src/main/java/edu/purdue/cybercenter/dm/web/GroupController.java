package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Classification;
import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/groups")
@Controller
public class GroupController {

    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        model.addAttribute("classifications", Classification.findAllClassifications());
        return "groups/index";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Group.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        Session session = DomainObjectHelper.getHbmSession();

        if (request.getParameter("organizationUnitId") != null) {
            Integer organizationUnit = Integer.parseInt(request.getParameter("organizationUnitId"));

            if (!request.getParameter("organizationUnitId").startsWith("-")) {
                session.enableFilter("groupInUnitFilter").setParameter("organizationUnitId", organizationUnit);
            } else {
                session.enableFilter("groupNotInUnitFilter").setParameter("organizationUnitId", -organizationUnit);
            }
        }

        if (request.getParameter("enabled") != null) {
            Boolean enabled = Boolean.parseBoolean(request.getParameter("enabled"));
            org.hibernate.Filter enabledFilter = session.enableFilter("enabledFilter");
            enabledFilter.setParameter("enabled", enabled);
        }

        return WebJsonHelper.list(request, response, Group.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Object result;
        try {
            result = WebJsonHelper.create(json, request, response, Group.class);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            error.put("status", "Unable to create the group");

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", error);

            result = errorResult;
        }

        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object updateFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Group group = DomainObjectUtils.fromJson(json, request.getContextPath(), Group.class);
        group = Group.findGroup(group.getId());
        if (Constant.AdminGroupName.equals(group.getName())) {
            throw new RuntimeException("you cannot make changes to group: " + Constant.AdminGroupName);
        }

        Object result;
        try {
            result = WebJsonHelper.update(json, request, response, Group.class);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            error.put("status", "Unable to update the group");

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", error);

            result = errorResult;
        }

        return result;
    }

}
