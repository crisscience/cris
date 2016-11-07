package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        return "groups/index";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Group.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.list(request, response, Group.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

    @RequestMapping(value = "/currentgroup", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<String> getCurrentGroup(HttpServletRequest request, HttpServletResponse response) {
        Integer groupId = (Integer) request.getSession().getAttribute("groupId");
        Group group = Group.findGroup(groupId);
        String responseBody;
        HttpStatus httpStatus;
        if (group == null) {
            responseBody = "";
            httpStatus = HttpStatus.NOT_FOUND;
        } else {
            responseBody = DomainObjectUtils.toJson(group, request.getContextPath());
            httpStatus = HttpStatus.OK;
        }
        ResponseEntity responseEntity = new ResponseEntity(responseBody, httpStatus);
        return responseEntity;
    }

    @RequestMapping(value = "/currentgroup", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity setCurrentGroup(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute("groupId");
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.NO_CONTENT);
        return responseEntity;
    }

    @RequestMapping(value = "/currentgroup/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity setCurrentGroup(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        Object responseBody;
        HttpStatus httpStatus;
        Group group = Group.findGroup(id);
        if (group != null) {
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            User user = User.findUser(userId);
            boolean isMemebr = group.isMember(user);
            if (isMemebr) {
                request.getSession().setAttribute("groupId", id);
                responseBody = DomainObjectUtils.toJson(group, request.getContextPath());
                httpStatus = HttpStatus.OK;
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "not a member of: " + id);
                error.put("status", "failed");
                Map<String, Object> result = new HashMap<>();
                result.put("error", error);
                responseBody = result;
                httpStatus = HttpStatus.NOT_FOUND;
            }
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "group does not exist: " + id);
            error.put("status", "failed");
            Map<String, Object> result = new HashMap<>();
            result.put("error", error);
            responseBody = result;
            httpStatus = HttpStatus.NOT_FOUND;
        }

        ResponseEntity responseEntity = new ResponseEntity(responseBody, httpStatus);

        return responseEntity;
    }
}
