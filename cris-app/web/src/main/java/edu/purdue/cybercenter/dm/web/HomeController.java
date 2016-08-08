package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Tenant;
import edu.purdue.cybercenter.dm.domain.Tile;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.MediaType;

@Controller
public class HomeController {

    @Autowired
    DomainObjectService domainObjectService;

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(HttpServletRequest request, HttpServletResponse response, Model model) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        User user = User.findUser(userId);

        String url = "index";

        if (user == null) {
            model.addAttribute("tenants", Tenant.findAllTenants());
        } else {
            String homeUri = (String) request.getSession().getAttribute("wsHomeUri");
            if (homeUri != null && !homeUri.isEmpty()) {
                return "forward:" + homeUri;
            } else {
                // TODO: populate the model according to the role of the user
                if (user.isAdmin()) {
                    String view = (String) request.getSession().getAttribute("currentView");
                    if (view != null && view.equalsIgnoreCase("admin")) {
                        url = "home";
                    } else {
                        url = "home";
                    }
                } else {
                    url = "home";
                }
            }
        }

        return url;
    }

    @RequestMapping(value = "home", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String home(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("tiles", domainObjectService.findAll(Tile.class));
        return "home";
    }

    @RequestMapping(value = "home/help", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String help(Model model, HttpServletRequest request, HttpServletResponse response) {
        return "home/help";
    }

}
