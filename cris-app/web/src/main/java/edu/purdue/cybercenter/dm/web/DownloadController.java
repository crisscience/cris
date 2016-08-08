package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.SmallObject;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.service.StorageService;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.util.AppConfigConst;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DownloadController {

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    HistoryService historyService;

    @Autowired
    DomainObjectService domainObjectService;

    private StorageFileManager storageFileManager;
    @Autowired
    public void setStorageService(StorageService storageService) throws IOException {
        storageFileManager = storageService.getStorageFileManager(AccessMethodType.FILE);
    }

    @RequestMapping(value="/download/{File:.*}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getFile(@PathVariable("File") String filename, HttpServletRequest request, HttpServletResponse response) {
        String downloadPath = (String) request.getSession().getAttribute(AppConfigConst.SESSION_TMP_PATH);
        String location = downloadPath + AppConfigConst.FILE_SEPARATOR + filename;
        download(location, response);
    }

    @RequestMapping(value = "/download/{JobFile:JobFile:[0-9]+}/{filename:.*}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getJobFile(@PathVariable("JobFile") String jobId, @PathVariable("filename") String filename, HttpServletRequest request, HttpServletResponse response) {
        String fullPath = AppConfigConst.getJobTmpPath() + AppConfigConst.FILE_SEPARATOR + jobId.substring(8) + "/" + filename;
        download(fullPath, response);
    }

    @RequestMapping(value="/download/{SmallFile:SmallFile:[0-9]+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getSmallFile(@PathVariable("SmallFile") String fileId, HttpServletRequest request, HttpServletResponse response) {
        // Small files are stored in small_object table in postgreSQL
        Integer id = Integer.parseInt(fileId.split(":")[1]);
        SmallObject smallObject = SmallObject.findSmallObject(id);
        String filename = smallObject.getFilename();
        byte[] content = smallObject.getContent();
        String mimeType = smallObject.getMimeType();

        try {
            download(filename, mimeType, content, response);
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Unable to download: %s: %s", fileId, ex.getMessage()), ex);
        }
    }

    @RequestMapping(value="/download/{StorageFile:StorageFile:[0-9]+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getStorageFile(@PathVariable("StorageFile") String filename, HttpServletRequest request, HttpServletResponse response) {
        String downloadPath = (String) request.getSession().getAttribute(AppConfigConst.SESSION_TMP_PATH) + AppConfigConst.FILE_SEPARATOR;
        File dir = new File(downloadPath);
        dir.mkdir();

        String file;
        try {
            file = storageFileManager.getFile(StorageFile.toStorageFile(filename), downloadPath, true);
        } catch (Exception ex) {
            throw new RuntimeException("StorageFile does not exist: " + filename + ": " + ex.getMessage(), ex);
        }

        download(file, response);
    }

    @RequestMapping(value="/download/{EmbeddedFile:EmbeddedFile:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getEmbeddedFile(@PathVariable("EmbeddedFile") String filespec, HttpServletRequest request, HttpServletResponse response) {
        String[] fields = filespec.split(":");
        if (fields.length != 2) {
            throw new RuntimeException("Invalid EmbeddedFile: \"" + filespec + "\"");
        }

        String sJobId = request.getParameter("jobId");
        String filename = fields[1];

        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(sJobId).singleResult();
        String processDefinitionId = processInstance.getProcessDefinitionId();
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
        String deploymentId = processDefinition.getDeploymentId();
        InputStream is = repositoryService.getResourceAsStream(deploymentId, filename);
        BufferedInputStream bis = new BufferedInputStream(is);

        try {
            download(filename, null, bis, response);
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Unable to download: %s: %s", filespec, ex.getMessage()), ex);
        }
    }

    /*@RequestMapping("/attachment/{id}")
    public void getAttachment(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        Attachment attachment = Attachment.findAttachment(id);
        String location = attachment.getLocation();
        download(location, response);
    }
    @RequestMapping("/jobContext/{id}")
    public void getJobAttachment(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        JobContext attachment = JobContext.findJobContext(id);
        String location = attachment.getValue();
        download(location, response);
    }*/

    private void download(String location, HttpServletResponse response) {
        File file = new File(location);

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            response.setContentType("application/octet-stream");
            response.setContentLength((int) file.length());

            try (ServletOutputStream sos = response.getOutputStream()) {
                IOUtils.copy(dis, sos);
                sos.flush();
            } catch (IOException ex) {
                throw new RuntimeException("Unable to download file: " + file.getName(), ex);
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("file not found: " + location, ex);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to open file: " + location, ex);
        }
    }

    private void download(String filename, String mimeType, byte[] content, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        if (mimeType != null && !mimeType.isEmpty()) {
            response.setContentType(mimeType);
        }
        response.setContentLength(content.length);

        ServletOutputStream sos = response.getOutputStream();
        sos.write(content);
        sos.flush();
    }

    private void download(String filename, String mimeType, InputStream content, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        if (mimeType != null && !mimeType.isEmpty()) {
            response.setContentType(mimeType);
        }

        ServletOutputStream sos = response.getOutputStream();
        IOUtils.copy(content, sos);
        sos.flush();
    }
}
