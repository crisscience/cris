package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Report;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.ReportService;
import edu.purdue.cybercenter.dm.util.AppConfigConst;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RequestMapping("/reports")
@Controller
public class ReportController {

    static final private Logger logger = LoggerFactory.getLogger(ReportController.class.getName());

    @Autowired
    private ReportService reportService;
    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "reports/index";
    }

    @RequestMapping(value = "/import", method = {RequestMethod.PUT, RequestMethod.POST}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Object importReport(MultipartHttpServletRequest request, HttpServletResponse response) {
        String error = null;
        List<MultipartFile> mpFiles = WebHelper.multipartFileMapToList(request.getMultiFileMap());

        if (mpFiles.size() == 1) {
            MultipartFile mpFile = mpFiles.get(0);
            if (!mpFile.isEmpty()) {
                try {
                    Report report = new Report();
                    report.setUuid(UUID.randomUUID());
                    report.setVersionNumber(1);
                    report.setName(mpFile.getOriginalFilename());
                    report.setContent(DatatypeConverter.printBase64Binary(mpFile.getBytes()));
                    domainObjectService.persist(report, Report.class);
                } catch (IOException ex) {
                    error = "Failed to update database: " + mpFile.getOriginalFilename();
                    logger.error(error, ex);
                }
            }
        }

        if (error != null && !error.isEmpty()) {
            return WebHelper.buildIframeResponse("{\"error\":\"" + error + "\"}");
        } else {
            return WebHelper.buildIframeResponse("");
        }
    }

    @RequestMapping(value = "/export/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void export(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Report report = Report.findReport(id);
        byte[] content = DatatypeConverter.parseBase64Binary(report.getContent());
        response.setContentLength(content.length);
        ServletOutputStream sos = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment; filename=" + report.getName());
        response.setContentType("application/octet-stream");
        sos.write(content);
        sos.flush();
        sos.close();
    }

    @RequestMapping(value = "/run/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void run(@PathVariable("id") Integer id, @RequestParam(value = "outputType", required = true) String outputType, @RequestParam(value = "uuid", required = false) String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, MalformedURLException, ResourceKeyCreationException, ResourceException, ResourceCreationException, ReportProcessingException {
        Report report = Report.findReport(id);
        String filename = reportService.getReportTemplateFromId(report.getUuid());
        ServletContext servletContext = request.getSession().getServletContext();

        Map<String, Object> input = new HashMap<>();
        String outputFolder = (String) request.getSession().getAttribute(AppConfigConst.SESSION_TMP_PATH);
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        input.put("uuid", uuid);
        input.put("rootPath", outputFolder);
        input.put("type", outputType);
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            if (paramName.equals("uuid") || paramName.equals("outputType")) {
                continue;
            }
            input.put(paramName, request.getParameter(paramName));
        }

        Map<String, String> output = reportService.generateReport(filename, input);
        response.setContentType(output.get("content_type"));
        response.addHeader("Content-Disposition", "attachment; filename=" + output.get("filename"));
        FileInputStream fis = new FileInputStream(outputFolder + "/" + output.get("filename"));
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] bytes = new byte[bis.available()];
        OutputStream os = response.getOutputStream();
        bis.read(bytes);
        os.write(bytes);
        bis.close();
        fis.close();
    }

    @RequestMapping(value = "/parameters/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void parameters(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, ResourceKeyCreationException, ResourceException, ServletException, IOException {
        ServletContext servletContext = request.getSession().getServletContext();
        Report report = Report.findReport(id);
        String filename = reportService.getReportTemplateFromId(report.getUuid());

        // Get parameters.
        Map<String, String> output = reportService.getParameters(filename);

        // Construct json string.
        StringBuilder json = new StringBuilder();
        json.append("{ ");
        for (String key : output.keySet()) {
            json.append(key).append(": '").append(output.get(key)).append("', ");
        }
        json.setLength(json.length() - 2);
        json.append(" }");

        if (output.isEmpty()) {
            json = new StringBuilder().append("{ }");
        }

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Report.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.list(request, response, Report.class);
    }
}
