package edu.purdue.cybercenter.dm.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/assets")
@Controller
public class AssetController {

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces=MediaType.TEXT_HTML_VALUE)
    public String list(Model model) {
        return "assets/index";
    }

}
