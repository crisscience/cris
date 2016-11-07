package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.StorageFile;
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

@RequestMapping("/storagefiles")
@Controller
public class StorageFileController {

    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        return "storagefiles/index";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, StorageFile.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        Session session = DomainObjectHelper.getHbmSession();
        if (request.getParameter("storageId") != null) {
            Integer storageId = Integer.parseInt(request.getParameter("storageId"));
            session.enableFilter("storageFileFilter").setParameter("storageId", storageId);
        }
        return WebJsonHelper.list(request, response, StorageFile.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.create(json, request, response, StorageFile.class);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object updateFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.update(json, request, response, StorageFile.class);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.delete(id, request, response, StorageFile.class);
    }

}
