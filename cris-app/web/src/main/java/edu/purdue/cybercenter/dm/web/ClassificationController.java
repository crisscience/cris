package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Classification;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
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

@RequestMapping("/classifications")
@Controller
public class ClassificationController {

    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String list(Model model) {
        return "classifications/index";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Classification.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.list(request, response, Classification.class);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.create(json, request, response, Classification.class);
    }

    @RequestMapping(value = "/jsonArray", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.createArray(json, request, response, Classification.class);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object updateFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.update(json, request, response, Classification.class);
    }

    @RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.updateArray(json, request, response, Classification.class);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.delete(id, request, response, Classification.class);
    }

}
