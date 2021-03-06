package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Tool;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.service.CqlService;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

@RequestMapping("/tools")
@Controller
public class ToolController {

    @Autowired
    private CqlService cqlService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        return "tools/index";
    }

    @RequestMapping(value = "/run/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String run(@PathVariable("id") Integer id, Model model, HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> context = new HashMap<>();
        User user = (User) request.getAttribute("user");
        context.put(MetaField.User, user);
        context.put(MetaField.UserId, user.getId());

        Tool tool = Tool.findTool(id);
        String html = tool.getContent();
        String generatedHtml = cqlService.eval(html, context);

        model.addAttribute("user", user);
        model.addAttribute("tool", tool);
        model.addAttribute("generatedHtml", generatedHtml);

        return "tools/run";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Tool.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.list(request, response, Tool.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.create(json, request, response, Tool.class);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object updateFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.update(json, request, response, Tool.class);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.delete(id, request, response, Tool.class);
    }
}
