package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/dataset")
@Controller
public class DatasetController {

    @Autowired
    DomainObjectService domainObjectService;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("projects", domainObjectService.findAll(Project.class));
        return "dataset/index";
    }

}
