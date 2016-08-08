package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Workflow;
import edu.purdue.cybercenter.dm.service.DomainObjectService;
import edu.purdue.cybercenter.dm.util.AppConfigConst;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xml.sax.SAXException;

@RequestMapping("/workflows")
@Controller
public class WorkflowController {

    private static final String KEY_WORKFLOW_DEFINITION_FILE = "workflowDefinitionFile";

    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class.getName());

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "workflows/index";
    }

    @RequestMapping(value = "/import", method = {RequestMethod.PUT, RequestMethod.POST}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Object importWorkflow(MultipartHttpServletRequest request, HttpServletResponse response) {
        Workflow workflow = null;
        String error = null;

        String sessionTmpPath = (String) request.getSession().getAttribute(AppConfigConst.SESSION_TMP_PATH);
        List<MultipartFile> mpFiles = WebHelper.multipartFileMapToList(request.getMultiFileMap());
        if (mpFiles.size() == 1) {
            MultipartFile mpFile = mpFiles.get(0);
            if (!mpFile.isEmpty()) {
                try {
                    File zipFile = WebHelper.generateNewKeyForImportedWorkflow(mpFile, sessionTmpPath);
                    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                    RepositoryService repositoryService = processEngine.getRepositoryService();

                    ZipInputStream is = new ZipInputStream(new FileInputStream(zipFile));
                    Deployment deployment = repositoryService.createDeployment().name(mpFile.getOriginalFilename()).addZipInputStream(is).deploy();

                    // get info about the deployed workflow
                    // add/update workflow
                    ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
                    ProcessDefinition pd = pdq.deploymentId(deployment.getId()).orderByProcessDefinitionVersion().desc().singleResult();
                    String key = pd.getKey();
                    Integer version = pd.getVersion();
                    String name = pd.getName();
                    String description = pd.getResourceName();
                    String processArchiveFileName = deployment.getName();

                    // a new workflow
                    workflow = new Workflow();

                    workflow.setKey(key);
                    workflow.setVersionNumber(version);
                    workflow.setName(name);
                    workflow.setDescription(description);
                    workflow.setContent(processArchiveFileName);
                    workflow = workflow.merge();

                } catch (Exception ex) {
                    error = "Failed to import workflow: " + mpFile.getOriginalFilename() + ": " + ex.getMessage();
                }
            } else {
                error = "The file is empty: " + mpFile.getOriginalFilename();
            }
        } else if (mpFiles.isEmpty()) {
            error = "No file found in the request";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("too many files:");
            boolean isFirst = true;
            for (MultipartFile file : mpFiles) {
                if (isFirst) {
                    sb.append(" ");
                    isFirst = false;
                } else {
                    sb.append(", ");
                }
                sb.append(file.getOriginalFilename());
            }

            error = sb.toString();
        }

        String body;
        if (workflow != null) {
            body = DomainObjectUtils.toJson(workflow, request.getContextPath());
        } else {
            body = "{\"error\":\"" + error + "\"}";
        }

        return new ResponseEntity<>(WebHelper.buildIframeResponse(body), HttpStatus.CREATED);
    }


    @RequestMapping(value = "/export/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportWorkflow(@PathVariable("id") Integer id, @RequestParam(value = "version", required = false) Integer version, HttpServletRequest request, HttpServletResponse response) {
        Workflow workflow = domainObjectService.findById(id, Workflow.class);

        if (workflow != null) {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            RepositoryService repositoryService = processEngine.getRepositoryService();

            ZipOutputStream zos = null;

            try {
                String key = workflow.getKey();
                if (version == null) {
                    version = workflow.getVersionNumber();
                }

                // get the deployment ID
                ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
                ProcessDefinition processDefinition = processDefinitionQuery.processDefinitionKey(key).processDefinitionVersion(version).singleResult();
                String deploymentId = processDefinition.getDeploymentId();

                // get the zip filename
                DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
                Deployment deployment = deploymentQuery.deploymentId(deploymentId).singleResult();
                String deploymentName = deployment.getName();

                // prepare for download
                ServletOutputStream sos = response.getOutputStream();
                response.setHeader("Content-Disposition", "attachment; filename=" + deploymentName);
                response.setContentType("application/octet-stream");

                // get all the files and put them in a zip output stream
                zos = new ZipOutputStream(sos);
                List<String> resources = repositoryService.getDeploymentResourceNames(deploymentId);
                for (String resource : resources) {
                    InputStream is = repositoryService.getResourceAsStream(deploymentId, resource);
                    byte[] ba = IOUtils.toByteArray(is);
                    ZipEntry ze = new ZipEntry(resource);
                    zos.putNextEntry(ze);
                    zos.write(ba);
                    zos.closeEntry();
                }
            } catch (FileNotFoundException ex) {
                logger.error(null, ex);
            } catch (IOException ex) {
                logger.error(null, ex);
            } finally {
                try {
                    zos.close();
                } catch (IOException ex) {
                    logger.error("Unable to close zip file", ex);
                }
            }
        }
    }

    @RequestMapping(value = "/save", method = {RequestMethod.PUT, RequestMethod.POST}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Object saveWorkflow(MultipartHttpServletRequest request, HttpServletResponse response) throws SAXException, IOException {
        // workflow
        String sWorkflow = request.getParameter("workflow");
        Map<String, Object> mapWorkflow = Helper.deserialize(sWorkflow, Map.class);

        // files from uploader
        MultiValueMap<String, MultipartFile> mpFilesByTask = request.getMultiFileMap();
        Map<String, Map<String, InputStream>> filesFromUploader = new HashMap<>();
        for (Map.Entry<String, List<MultipartFile>> entry : mpFilesByTask.entrySet()) {
            String taskId;
            if (entry.getKey().endsWith("s[]")) {
                taskId = entry.getKey().substring(0, entry.getKey().length() - 3);
            } else {
                taskId = entry.getKey();
            }
            List<MultipartFile> multipartFiles = entry.getValue();
            Map<String, InputStream> files = new HashMap<>();
            for (MultipartFile multipartFile : multipartFiles) {
                if (!multipartFile.isEmpty()) {
                    files.put(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
                }
            }
            if (filesFromUploader.containsKey(taskId)) {
                filesFromUploader.get(taskId).putAll(files);
            } else {
                filesFromUploader.put(taskId, files);
            }
        }

        // files from arachive
        Map<String, Map<String, InputStream>> filesFromArchive = WebHelper.getFilesFromArchive(mapWorkflow);

        // merge files from uploader into files from archive
        // files from uploader take precedence
        Map<String, Map<String, InputStream>> files = filesFromArchive;
        for (String key : filesFromUploader.keySet()) {
            if (files.containsKey(key)) {
                files.get(key).putAll(filesFromUploader.get(key));
            } else {
                files.put(key, filesFromUploader.get(key));
            }
        }

        String body;
        String workflowName = (String) mapWorkflow.get("name");
        try {
            Map.Entry<String, InputStream> xmlFile = WebHelper.convertjsonToXmlWorkflow(mapWorkflow, files);
            Map<String, InputStream> entry = new HashMap<>();
            entry.put(xmlFile.getKey(), xmlFile.getValue());
            files.put(KEY_WORKFLOW_DEFINITION_FILE, entry);

            String zipFilename = WebHelper.getFileName(workflowName, ".zip");
            String zipFilePath = (String) request.getSession().getAttribute(AppConfigConst.SESSION_TMP_PATH) + AppConfigConst.FILE_SEPARATOR + zipFilename;
            File zipFile = WebHelper.zipStreams(files, zipFilePath);

            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            RepositoryService repositoryService = processEngine.getRepositoryService();

            ZipInputStream is = new ZipInputStream(new FileInputStream(zipFile));
            Deployment deployment = repositoryService.createDeployment().name(zipFile.getName()).addZipInputStream(is).deploy();

            zipFile.delete();

            // get info about the deployed workflow
            // add/update workflow
            ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
            ProcessDefinition pd = pdq.deploymentId(deployment.getId()).orderByProcessDefinitionVersion().desc().singleResult();
            String key = pd.getKey();
            Integer version = pd.getVersion();
            String name = pd.getName();
            String description = pd.getDescription();
            String processArchiveFileName = deployment.getName();

            Workflow workflow;
            try {
                workflow = (Workflow) DomainObjectHelper.createNamedQuery("Workflow.findByKey").setParameter("key", key).getSingleResult();
            } catch (Exception ex) {
                // a new workflow
                workflow = new Workflow();
            }

            workflow.setKey(key);
            workflow.setVersionNumber(version);
            workflow.setName(name);
            workflow.setDescription(description);
            workflow.setContent(processArchiveFileName);
            workflow = workflow.merge();

            body = DomainObjectUtils.toJson(workflow, request.getContextPath());
        } catch (IOException | TransformerException | ParserConfigurationException ex) {
            String error = "Failed to save workflow: " + workflowName + ": " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            body = "{\"error\":\"" + error + "\"}";
        }

        return WebHelper.buildIframeResponse(body);
    }

    @RequestMapping(value = "/load/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object loadWorkflow(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        Workflow workflow = domainObjectService.findById(id, Workflow.class);
        String json = null;

        if (workflow != null) {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            RepositoryService repositoryService = processEngine.getRepositoryService();

            String key = workflow.getKey();
            Integer version = workflow.getVersionNumber();

            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
            ProcessDefinition processDefinition = processDefinitionQuery.processDefinitionKey(key).processDefinitionVersion(version).singleResult();
            String deploymentId = processDefinition.getDeploymentId();

            List<String> resources = repositoryService.getDeploymentResourceNames(deploymentId);

            String sessionTmpPath = (String) request.getSession().getAttribute(AppConfigConst.SESSION_TMP_PATH) + AppConfigConst.FILE_SEPARATOR;
            for (String resource : resources) {
                if (resource.endsWith(".bpmn20.xml")) {
                    try {
                        InputStream is = repositoryService.getResourceAsStream(deploymentId, resource);
                        String fileName = sessionTmpPath + resource;
                        IOUtils.copy(is, new FileOutputStream(fileName));
                        String workflowJson = WebHelper.convertXmlWorkflowToJson(fileName, id);
                        boolean theEndExists = WebHelper.isExistTheEndInArchive(resources);
                        Map<String, Object> result = new HashMap<>();
                        result.put("workflow", workflowJson);
                        result.put("theEnd", theEndExists);
                        json = Helper.serialize(result);
                        break;
                    } catch (IOException | ParserConfigurationException | SAXException | XMLStreamException ex) {
                        json = "Failed to load workflow: " + workflow.getName() + ": " + ex.getMessage();
                    }
                }
            }
        }

        return json;
    }

    @RequestMapping(value = "/versions/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getVersions(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> result = new ArrayList<>();

        Workflow workflow = domainObjectService.findById(id, Workflow.class);

        if (workflow != null) {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            RepositoryService repositoryService = processEngine.getRepositoryService();

            String key = workflow.getKey();
            Integer statusId = workflow.getStatusId();

            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
            List<ProcessDefinition> processDefinitions = processDefinitionQuery.processDefinitionKey(key).orderByProcessDefinitionVersion().desc().list();
            for (ProcessDefinition processDefinition : processDefinitions) {
                Map<String, Object> pd = new HashMap<>();

                pd.put("category", processDefinition.getCategory());
                pd.put("deploymentId", processDefinition.getDeploymentId());
                pd.put("diagramResourceName", processDefinition.getDiagramResourceName());
                pd.put("id", processDefinition.getId());
                pd.put("key", processDefinition.getKey());
                pd.put("name", processDefinition.getName());
                pd.put("resourceName", processDefinition.getResourceName());
                pd.put("version", processDefinition.getVersion());
                pd.put("statusId", statusId);

                // get the deployment time
                DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
                Deployment deployment = deploymentQuery.deploymentId(processDefinition.getDeploymentId()).singleResult();
                pd.put("content", deployment.getName());
                pd.put("timeCreated", deployment.getDeploymentTime());
                pd.put("timeUpdated", deployment.getDeploymentTime());

                result.add(pd);
            }
        }

        return Helper.serialize(result);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, Workflow.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter("resourceId") != null) {
            Session session = DomainObjectHelper.getHbmSession();
            Integer resourceId = Integer.parseInt(request.getParameter("resourceId"));
            if (!request.getParameter("resourceId").startsWith("-")) {
                session.enableFilter("workflowInResourceFilter").setParameter("resourceId", resourceId);
            } else {
                session.enableFilter("workflowNotInResourceFilter").setParameter("resourceId", -resourceId);
            }
        }
        return WebJsonHelper.list(request, response, Workflow.class);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object updateFromJson(@PathVariable("id") Integer id, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Object result;
        try {
            result = WebJsonHelper.update(json, request, response, Workflow.class);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            error.put("status", "Unable to update the experiment");

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", error);

            result = errorResult;
        }

        return result;
    }
}
