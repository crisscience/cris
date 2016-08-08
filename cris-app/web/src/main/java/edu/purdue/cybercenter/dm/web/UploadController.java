package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class UploadController {

    @RequestMapping(value = "/upload", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object putStorageFilesForm() {
        return "/upload/form";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    public Object putStorageFiles(MultipartHttpServletRequest request, HttpServletResponse response, Model model) {
        Map<String, Object> files = WebHelper.saveFiles(request);

        model.addAttribute("files", files);

        return "/upload/result";
    }

    @RequestMapping(value = "/ifupload", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Object putStorageFilesIFrame(MultipartHttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = putFiles(request);

        return WebHelper.buildIframeResponse(Helper.serialize(result));
    }

    private Map<String, Object> putFiles(MultipartHttpServletRequest request) {
        Map<String, Object> files = WebHelper.saveFiles(request);

        Map<String, Object> result = new HashMap<>();
        result.put("files", files);

        // echo all request parameters back
        Map params = request.getParameterMap();
        for (Object param : params.keySet()) {
            String name = (String) param;
            Object value = params.get(param);
            result.put(name, value);
        }

        return result;
    }

}
