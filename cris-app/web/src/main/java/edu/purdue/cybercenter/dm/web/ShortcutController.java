package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Shortcut;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.util.Map;
import java.util.UUID;
import javax.persistence.TypedQuery;
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

@RequestMapping("/shortcuts")
@Controller
public class ShortcutController {

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        return "shortcuts/index";
    }

    @RequestMapping(value = "/run/{uuid}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String run(@PathVariable("uuid") String sUuid, Model model, HttpServletRequest request, HttpServletResponse response) {
        UUID uuid = UUID.fromString(sUuid);

        User user = (User) request.getAttribute("user");
        TypedQuery<Shortcut> query = DomainObjectHelper.createNamedQuery("Shortcut.findByUuid", Shortcut.class);
        query.setParameter("uuid", uuid);
        Shortcut shortcut = domainObjectService.executeTypedQueryWithSingleResult(query);

        String url = shortcut.getUrl();
        String parameters = shortcut.getParameters();
        Map<String, String> mapParameters = Helper.deserialize(parameters, Map.class);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (mapParameters != null) {
            for (Map.Entry<String, String> entry : mapParameters.entrySet()) {
                if (first) {
                    first = false;
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("shortcut", shortcut);
        model.addAttribute("url", url);
        model.addAttribute("parameters", parameters);
        return "forward:" + url + sb.toString();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Shortcut.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.list(request, response, Shortcut.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.create(json, request, response, Shortcut.class);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object updateFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.update(json, request, response, Shortcut.class);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.delete(id, request, response, Shortcut.class);
    }
}
