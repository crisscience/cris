/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web.util;

import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.domain.Workflow;
import edu.purdue.cybercenter.dm.service.DatasetService;
import edu.purdue.cybercenter.dm.service.StorageService;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.EnumDojoOperator;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.util.TermName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.Import;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jiaxu
 */
@Component
public class WebHelper {

    private static final String KEY_THE_END_FILE = "theEndFile";
    private static final String KEY_THE_END_FILE_IN_ARCHIVE = "theEndFileInArchive";
    private static final String KEY_THE_END_FILE_TO_INCLUDE = "theEndFileToInclude";
    private static final String KEY_DATASET_STATE_SELECTION_TASK_FILE = "datasetStateSelectionTaskFile";

    private static final String THE_END_PAGE_FILE_NAME = "the_end.html";
    private static final String DATASET_STATE_SELECTION_TASK_FILE_NAME = "pick_final_dataset_state.html";

    private static final String DATASET_STATE_SELECTION_TASK_NAME = "Dataset State Task";

    private static final String XML_START_EVENT_ID = "theStart";
    private static final String XML_END_EVENT_ID = "theEnd";
    private static final String JSON_START_EVENT_KEY = "start";
    private static final String JSON_END_EVENT_KEY = "end";

    private static final String USER_TASK_NAME = "User Task";
    private static final String SERVICE_TASK_NAME = "System Task";
    private static final String REPORT_TASK_NAME = "Report Task";
    private static final String EXCLUSIVE_GATEWAY_NAME = "exclusiveGateway";
    private static final String CASE_BRANCH_NAME = "Case Branch";

    private static final String PREFIX_TASK = "task_";
    private static final String PREFIX_EXCLUSIVE_GATEWAY = "exgw_";
    private static final String PREFIX_FLOW = "flow_";

    private static DatasetService datasetService;
    @Autowired
    public void setDatasetService(DatasetService datasetService) {
        WebHelper.datasetService = datasetService;
    }

    private static StorageFileManager storageFileManager;
    @Autowired
    public void setStorageService(StorageService storageService) throws IOException {
        storageFileManager = storageService.getStorageFileManager(AccessMethodType.FILE);
    }

    private static StorageFileManager globusStorageFileManager;
    @Autowired
    public void setGlobusStorageService(StorageService storageService) throws IOException {
        globusStorageFileManager = storageService.getStorageFileManager(AccessMethodType.GLOBUS);
    }

    public static File generateNewKeyForImportedWorkflow(MultipartFile mpFile, String filePath) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, SAXException, IOException {
        File newZipFile = new File(filePath, mpFile.getName());
        FileOutputStream osNewZipFile = new FileOutputStream(newZipFile);
        try (ZipInputStream zis = new ZipInputStream(mpFile.getInputStream()); ZipOutputStream zos = new ZipOutputStream(osNewZipFile)) {
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                zos.putNextEntry(new ZipEntry(ze.getName()));

                if (ze.getName().endsWith(".bpmn20.xml")) {
                    // change the ID of workflow
                    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                    String doc = IOUtils.toString(zis);
                    Document document = docBuilder.parse(IOUtils.toInputStream(doc));

                    Node node = document.getDocumentElement();
                    NodeList nodeList = node.getChildNodes();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node currentNode = nodeList.item(i);
                        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) currentNode;
                            if (currentNode.getNodeName().equals("process")) {
                                eElement.setAttribute("id", "id_" + UUID.randomUUID().toString());
                            }
                        }
                    }

                    // write the content into xml file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    StreamResult result = new StreamResult(zos);
                    transformer.transform(source, result);
                } else {
                    // otherwise outout as is
                    /*
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = zis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    */
                    IOUtils.copy(zis, zos);
                }

                ze = zis.getNextEntry();
            }
            zos.closeEntry();
            zis.closeEntry();
        }

        return newZipFile;
    }

    public static File saveFile(MultipartFile mpfile, String dir, String newFilename) {
        File target;

        if (mpfile != null && !mpfile.isEmpty()) {
            System.out.println("file name: " + mpfile.getOriginalFilename());
            try {
                target = new File(mpfile.getOriginalFilename());
                mpfile.transferTo(target);
            } catch (IOException | IllegalStateException ex) {
                throw new RuntimeException("Unable to save file: " + mpfile.getOriginalFilename(), ex);
            }
        } else {
            target = null;
        }

        return target;
    }

    public static String getFileName(String wName, String type) {
        String name = (wName.replaceAll("\\s", "_")).replaceAll("\\W+", "");
        return name + type;

    }

    public static String getDatasetStatePage() throws IOException {
        String content = "<script type=\"text/javascript\">\n"
                + "\n"
                + "    // <![CDATA[\n"
                + "    cris.workflow = {};\n"
                + "\n"
                + "    cris.workflow.app = {\n"
                + "\n"
                + "        submit: function() {\n"
                + "            var finalDatasetState = dijit.byId(\"idFinalDatasetState\").getValue();\n"
                + "            cris.job.task.app.jsonToServer[\"finalDatasetState\"] = +finalDatasetState;\n"
                + "            return true;\n"
                + "        },\n"
                + "\n"
                + "        init: function() {\n"
                + "            require([\n"
                + "                \"dojo/store/Memory\", \"dijit/form/FilteringSelect\", \"dojo/domReady!\"\n"
                + "            ], function(Memory, FilteringSelect) {\n"
                + "                var IdToStateMap = {0: 'Sandboxed', 1: 'Operational', 2: 'Archived', 3: 'Deprecated'};\n"
                + "                var finalDatasetStates = dojo.fromJson('${finalDatasetStates}');\n"
                + "                var data = [];\n"
                + "                dojo.forEach(finalDatasetStates, function(item, i){\n"
                + "                    if (IdToStateMap[item]) {\n"
                + "                        data.push({\"id\": item, \"name\": IdToStateMap[item]});\n"
                + "                    }\n"
                + "                });\n"
                + "                var store = new Memory({\n"
                + "                    data: data\n"
                + "                });\n"
                + "\n"
                + "                new FilteringSelect({\n"
                + "                    id: \"idFinalDatasetState\",\n"
                + "                    name: \"finalDatasetState\",\n"
                + "                    store: store,\n"
                + "                    searchAttr: \"name\"\n"
                + "                }, \"idFinalDatasetState\");\n"
                + "            });\n"
                + "        }\n"
                + "    };\n"
                + "\n"
                + "    cris.ready(function() {\n"
                + "        cris.workflow.app.init();\n"
                + "    });\n"
                + "    // ]]>\n"
                + "</script>\n"
                + "<div>\n"
                + "    <h1>Select the final state for all datasets of this job</h1>\n"
                + "\n"
                + "    <div>\n"
                + "        Final Dataset State: <input id=\"idFinalDatasetState\" name=\"finalDatasetState\"/>\n"
                + "    </div>\n"
                + "    <p><!-- --></p>\n"
                + "</div>";

        return content;
    }

    public static Map<String, Map<String, InputStream>> getFilesFromArchive(Map<String, Object> workflow) throws IOException {
        Map<String, Map<String, InputStream>> filesFromArchive = new HashMap<>();
        Map<String, InputStream> stream;

        List<Integer> finalDatasetStates = (List) workflow.get("finalDatasetStates");
        if (finalDatasetStates != null && finalDatasetStates.size() > 1) {
            String datasetStatePage = WebHelper.getDatasetStatePage();
            InputStream datasetStatePageStream = new ByteArrayInputStream(datasetStatePage.getBytes());
            stream = new HashMap<>();
            stream.put(DATASET_STATE_SELECTION_TASK_FILE_NAME, datasetStatePageStream);
            filesFromArchive.put(KEY_DATASET_STATE_SELECTION_TASK_FILE, stream);
        }

        Integer id = (Integer) workflow.get("id");
        Workflow prevWorkflow;
        if (id != null && id != 0) {
            prevWorkflow = Workflow.findWorkflow(id);
        } else {
            prevWorkflow = null;
        }

        if (prevWorkflow != null) {
            String key = prevWorkflow.getKey();
            Integer version = prevWorkflow.getVersionNumber();

            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            RepositoryService repositoryService = processEngine.getRepositoryService();
            // get the deployment ID
            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
            ProcessDefinition processDefinition = processDefinitionQuery.processDefinitionKey(key).processDefinitionVersion(version).singleResult();
            String deploymentId = processDefinition.getDeploymentId();
            List<String> resources = repositoryService.getDeploymentResourceNames(deploymentId);

            List<String> theEndFileToInclude = (List<String>) workflow.get(KEY_THE_END_FILE_TO_INCLUDE);
            if (!CollectionUtils.isEmpty(theEndFileToInclude)) {
                InputStream is = repositoryService.getResourceAsStream(deploymentId, THE_END_PAGE_FILE_NAME);
                stream = new HashMap<>();
                stream.put(THE_END_PAGE_FILE_NAME, is);
                filesFromArchive.put(KEY_THE_END_FILE, stream);
            }

            // streams by task
            Map<String, Object> tasks = (Map<String, Object>) workflow.get("tasks");
            for (Map.Entry<String, Object> entry : tasks.entrySet()) {
                Map<String, Object> task = (Map<String, Object>) entry.getValue();
                String taskId = (String) task.get("id");
                String taskType = (String) task.get("taskType");
                if (!taskType.equals(EXCLUSIVE_GATEWAY_NAME)) {
                    Map<String, String> filesInArchive = (Map<String, String>) task.get("filesInArchive");
                    List<String> filesToInclude = (List<String>) task.get("filesToInclude");

                    Map<String, InputStream> streamsByTask = new HashMap<>();

                    // for UserTask only
                    if (taskType.equals(USER_TASK_NAME)) {
                        String uiPage = (String) task.get("ui_page");
                        if (resources.contains(uiPage)) {
                            InputStream is = repositoryService.getResourceAsStream(deploymentId, uiPage);
                            streamsByTask.put(uiPage, is);
                        }
                    }

                    // for both tasks
                    if (filesToInclude != null) {
                        for (String file : filesToInclude) {
                            if (resources.contains(file)) {
                                InputStream is = repositoryService.getResourceAsStream(deploymentId, file);
                                streamsByTask.put(file, is);
                            }
                        }
                    }

                    filesFromArchive.put(taskId, streamsByTask);
                }
            }

            // all streams in one place (for compatibility)
            /*
            Map<String, InputStream> streamsAll = new HashMap<>();
            for (String file : resources) {
                InputStream is = repositoryService.getResourceAsStream(deploymentId, file);
                streamsAll.put(file, is);
            }
            filesFromArchive.put("", streamsAll);
            */
        }

        return filesFromArchive;
    }

    public static File zipStreams(Map<String, Map<String, InputStream>> streamsByTask, String path) throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(path); ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (Map.Entry<String, Map<String, InputStream>> streamsEntry : streamsByTask.entrySet()) {
                Map<String, InputStream> streams = streamsEntry.getValue();
                for (Map.Entry<String, InputStream> fileEntry : streams.entrySet()) {
                    String filename = fileEntry.getKey();
                    InputStream fis = fileEntry.getValue();

                    // add a new Zip Entry to the ZipOutputStream
                    ZipEntry ze = new ZipEntry(filename);
                    zos.putNextEntry(ze);
                    // read the file and write to ZipOutputStream
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    // Close the zip entry to write to zip file
                    zos.closeEntry();
                }
            }
        }

        return new File(path);
    }

    public static File zipFiles(Map<String, List<File>> filesByTask, String zipFileName) throws FileNotFoundException, IOException {
        List<File> listFiles = new ArrayList<>();
        for (Map.Entry<String, List<File>> entry : filesByTask.entrySet()) {
            List<File> files = entry.getValue();
            for (File file : files) {
                listFiles.add(file);
            }
        }
        return zipFiles(listFiles, zipFileName);
    }

    private static File zipFiles(List<File> files, String zipFileName) throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFileName); ZipOutputStream zos = new ZipOutputStream(fos)) {
            // create ZipOutputStream to write to the zip file
            for (File file : files) {
                // add a new Zip Entry to the ZipOutputStream
                ZipEntry ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);
                try (FileInputStream fis = new FileInputStream(file)) {
                    // read the file and write to ZipOutputStream
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    // Close the zip entry to write to zip file
                    zos.closeEntry();
                }
            }
        }

        return new File(zipFileName);
    }

    public static boolean isExistTheEndInArchive(List<String> resources) {
        boolean isExist = false;
        for (String resource : resources) {
            if (resource.equals(THE_END_PAGE_FILE_NAME)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    private static StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(XML_START_EVENT_ID);
        startEvent.setInitiator("initiator");
        return startEvent;
    }

    private static EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(XML_END_EVENT_ID);
        return endEvent;
    }

    private static UserTask createUserTask(String xmlId, Map<String, Object> data, Map<String, InputStream> files) {
        UserTask userTask = new UserTask();

        userTask.setId(xmlId);
        userTask.setName((String) data.get("name"));
        userTask.setDocumentation((String) data.get("documentation"));
        userTask.setFormKey((String) data.get("ui_page"));

        String jsonIn = (String) data.get("jsonIn");

        FormProperty jsonInProperty = new FormProperty();
        jsonInProperty.setId("jsonIn");
        jsonInProperty.setType("string");
        jsonInProperty.setName(jsonIn == null ? "{}" : jsonIn);
        userTask.setFormProperties(Arrays.asList(jsonInProperty));

        userTask.setTaskListeners(createTaskListeners());
        String datasetState = (data.get("datasetState") != null ? data.get("datasetState").toString() : null);
        userTask.setExecutionListeners(createActivityListeners(datasetState));
        addUiElements(userTask, data);

        if (!CollectionUtils.isEmpty(files)) {
            StringBuilder sb = new StringBuilder();
            files.keySet().stream().forEach((key) -> {
                if (sb.length() != 0) {
                    sb.append(";");
                }
                sb.append(key);
            });
            userTask.addExtensionElement(createPropertyElement("cris:field", "files", sb.toString()));
        }

        // users/groups
        List<String> users = (List<String>) data.get("users");
        if (!CollectionUtils.isEmpty(users)) {
            userTask.setCandidateUsers(users);
        }
        List<String> groups = (List<String>) data.get("groups");
        if (!CollectionUtils.isEmpty(groups)) {
            userTask.setCandidateGroups(groups);
        }

        return userTask;
    }

    private static ServiceTask createServiceTask(String xmlId, Map<String, Object> data, Map<String, InputStream> files) {
        ServiceTask serviceTask = new ServiceTask();

        serviceTask.setId(xmlId);
        serviceTask.setName((String) data.get("name"));
        serviceTask.setDocumentation((String) data.get("documentation"));

        boolean isAsyncTask = isAsyncTask(data);
        serviceTask.setImplementationType("class");
        if (isAsyncTask) {
            serviceTask.setImplementation("edu.purdue.cybercenter.dm.activiti.ServiceTaskAsyncDelegate");
        } else {
            serviceTask.setImplementation("edu.purdue.cybercenter.dm.activiti.ServiceTaskDelegate");
        }

        List<FieldExtension> extensionFields = new ArrayList<>();

        if (StringUtils.isNotEmpty((String) data.get("filesToPlace"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("filesToPlace");
            extensionField.setStringValue((String) data.get("filesToPlace"));
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty((String) data.get("jsonIn"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("jsonIn");
            extensionField.setStringValue((String) data.get("jsonIn"));
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty((String) data.get("commandline"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("commandLine");
            extensionField.setStringValue((String) data.get("commandline"));
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty((String) data.get("jsonOut"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("jsonOut");
            extensionField.setStringValue((String) data.get("jsonOut"));
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty((String) data.get("prefilter"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("preFilter");
            extensionField.setStringValue((String) data.get("prefilter"));
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty((String) data.get("postfilter"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("postFilter");
            extensionField.setStringValue((String) data.get("postfilter"));
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty((String) data.get("filesToCollect"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("filesToCollect");
            extensionField.setStringValue((String) data.get("filesToCollect"));
            extensionFields.add(extensionField);
        }

        serviceTask.setFieldExtensions(extensionFields);

        String datasetState = (data.get("datasetState") != null ? data.get("datasetState").toString() : null);
        serviceTask.setExecutionListeners(createActivityListeners(datasetState));
        addUiElements(serviceTask, data);

        if (!CollectionUtils.isEmpty(files)) {
            StringBuilder sb = new StringBuilder();
            files.keySet().stream().forEach((key) -> {
                if (sb.length() != 0) {
                    sb.append(";");
                }
                sb.append(key);
            });
            serviceTask.addExtensionElement(createPropertyElement("cris:field", "files", sb.toString()));
        }

        return serviceTask;
    }

    private static ReceiveTask createReceiveTask(String xmlId, Map<String, Object> data) {
        ReceiveTask receiveTask = new ReceiveTask();

        receiveTask.setId(xmlId);
        receiveTask.setName("Wait for execution to finish");
        receiveTask.setDocumentation("Wait for execution to finish");

        String datasetState = (data.get("datasetState") != null ? data.get("datasetState").toString() : null);
        receiveTask.setExecutionListeners(createActivityListeners(datasetState));
        addUiElements(receiveTask, data);

        return receiveTask;
    }

    private static ServiceTask createReportTask(String xmlId, Map<String, Object> data) {
        ServiceTask serviceTask = new ServiceTask();

        serviceTask.setId(xmlId);
        serviceTask.setName((String) data.get("name"));
        serviceTask.setDocumentation((String) data.get("documentation"));

        boolean isAsyncTask = isAsyncTask(data);
        serviceTask.setImplementationType("class");
        if (isAsyncTask) {
            serviceTask.setImplementation("edu.purdue.cybercenter.dm.activiti.ReportServiceTask");
        } else {
            serviceTask.setImplementation("edu.purdue.cybercenter.dm.activiti.ReportServiceTask");
        }

        List<FieldExtension> extensionFields = new ArrayList<>();

        if (StringUtils.isNotEmpty((String) data.get("command"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("command");
            extensionField.setStringValue((String) data.get("command"));
            extensionFields.add(extensionField);
        }

        Integer reportId = (Integer) data.get("reportId");
        if (reportId != null) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("reportId");
            extensionField.setStringValue(reportId.toString());
            extensionFields.add(extensionField);
        }

        Integer templateId = (Integer) data.get("templateId");
        if (templateId != null) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("templateId");
            extensionField.setStringValue(templateId.toString());
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty((String) data.get("parameters"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("parameters");
            extensionField.setStringValue((String) data.get("parameters"));
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty((String) data.get("outputType"))) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("outputType");
            extensionField.setStringValue((String) data.get("outputType"));
            extensionFields.add(extensionField);
        }

        serviceTask.setFieldExtensions(extensionFields);

        String datasetState = (data.get("datasetState") != null ? data.get("datasetState").toString() : null);
        serviceTask.setExecutionListeners(createActivityListeners(datasetState));
        addUiElements(serviceTask, data);

        return serviceTask;
    }

    private static ExclusiveGateway createExclusiveGateway(String xmlId, Map<String, Object> data) {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();

        exclusiveGateway.setId(xmlId);
        exclusiveGateway.setName((String) data.get("name"));
        exclusiveGateway.setDocumentation((String) data.get("documentation"));

        addUiElements(exclusiveGateway, data);

        return exclusiveGateway;
    }

    private static SequenceFlow createSequenceFlow(String xmlId, String sourceRef, String targetRef, String conditionExpression) {
        SequenceFlow sequenceFlow = new SequenceFlow();

        sequenceFlow.setId(xmlId);
        sequenceFlow.setSourceRef(sourceRef);
        sequenceFlow.setTargetRef(targetRef);
        if (conditionExpression != null) {
            sequenceFlow.setConditionExpression("${" + conditionExpression + "}");
        }

        ActivitiListener listener = createListener(null, "edu.purdue.cybercenter.dm.activiti.CrisTransitionListener", null, null, null);
        sequenceFlow.setExecutionListeners(Arrays.asList(listener));

        return sequenceFlow;
    }

    private static ActivitiListener createListener(String event, String clazz, String datasetState, String initialDatasetState, String finalDatasetStates) {
        ActivitiListener activitiListener = new ActivitiListener();

        if (event != null) {
            activitiListener.setEvent(event);
        }

        activitiListener.setImplementationType("class");
        activitiListener.setImplementation(clazz);

        List<FieldExtension> extensionFields = new ArrayList<>();

        if (datasetState != null) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("datasetState");
            extensionField.setStringValue(datasetState);
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty(initialDatasetState)) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("initialDatasetState");
            extensionField.setStringValue(initialDatasetState);
            extensionFields.add(extensionField);
        }

        if (StringUtils.isNotEmpty(finalDatasetStates)) {
            FieldExtension extensionField = new FieldExtension();
            extensionField.setFieldName("finalDatasetStates");
            extensionField.setStringValue(finalDatasetStates);
            extensionFields.add(extensionField);
        }

        activitiListener.setFieldExtensions(extensionFields);

        return activitiListener;
    }

    private static ExtensionElement createPropertyElement(String elmName, String propName, String propValue) {
        ExtensionElement property = new ExtensionElement();
        property.setName(elmName);

        ExtensionAttribute attrName = new ExtensionAttribute("name");
        attrName.setValue(propName);
        ExtensionAttribute attrValue = new ExtensionAttribute("stringValue");
        attrValue.setValue(propValue);
        property.addAttribute(attrName);
        property.addAttribute(attrValue);

        return property;
    }

    private static void addUiElements(FlowNode task, Map<String, Object> data) {
        if (data.get("top") != null && data.get("left") != null) {
            int top = new Double(data.get("top").toString()).intValue();
            int left = new Double(data.get("left").toString()).intValue();

            ExtensionElement uiLocation = createPropertyElement("cris:field", "uiLocation", "" + top + "," + "" + left);
            task.addExtensionElement(uiLocation);

            ExtensionElement orientation = createPropertyElement("cris:field", "orientation", (String) data.get("orientation"));
            task.addExtensionElement(orientation);
        }
    }

    private static List<ActivitiListener> createTaskListeners() {
        List<ActivitiListener> listeners = new ArrayList<>();

        ActivitiListener listener;
        listener = createListener("assignment", "edu.purdue.cybercenter.dm.activiti.CrisTaskListener", null, null, null);
        listeners.add(listener);
        listener = createListener("create", "edu.purdue.cybercenter.dm.activiti.CrisTaskListener", null, null, null);
        listeners.add(listener);
        listener = createListener("complete", "edu.purdue.cybercenter.dm.activiti.CrisTaskListener", null, null, null);
        listeners.add(listener);

        return listeners;
    }

    private static List<ActivitiListener> createActivityListeners(String datasetState) {
        List<ActivitiListener> listeners = new ArrayList<>();

        ActivitiListener listener;
        listener = createListener("start", "edu.purdue.cybercenter.dm.activiti.CrisActivityListener", datasetState, null, null);
        listeners.add(listener);
        listener = createListener("end", "edu.purdue.cybercenter.dm.activiti.CrisActivityListener", datasetState, null, null);
        listeners.add(listener);

        return listeners;
    }

    private static List<ActivitiListener> createExecutionListeners(String initialDatasetState, String finalDatasetStates) {
        List<ActivitiListener> listeners = new ArrayList<>();

        ActivitiListener listener;
        listener = createListener("start", "edu.purdue.cybercenter.dm.activiti.CrisExecutionListener", null, initialDatasetState, finalDatasetStates);
        listeners.add(listener);
        listener = createListener("end", "edu.purdue.cybercenter.dm.activiti.CrisExecutionListener", null, initialDatasetState, finalDatasetStates);
        listeners.add(listener);

        return listeners;
    }

    private static boolean isAsyncTask(Map<String, Object> task) {
        boolean async = false;
        Boolean syncTask = (Boolean) task.get("syncTask");
        if (syncTask != null && !syncTask) {
            async = true;
        }
        return async;
    }

    public static Map.Entry<String, InputStream> convertjsonToXmlWorkflow(Map<String, Object> object, Map<String, Map<String, InputStream>> filesToInclude) throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException {
        Integer initialDatasetState = (Integer) object.get("initialDatasetState");
        if (initialDatasetState == null) {
            initialDatasetState = 0;
        }

        List<Integer> finalDatasetStates = (List<Integer>) object.get("finalDatasetStates");
        if (finalDatasetStates == null) {
            finalDatasetStates = new ArrayList<>();
        }
        if (finalDatasetStates.isEmpty()) {
            finalDatasetStates.add(1);
        }

        String uuid = (String) object.get("uuid");
        String key = (String) object.get("key");
        if (StringUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            key = "id_" + uuid;
        }
        String name = (String) object.get("name");
        String description = (String) object.get("documentation");

        BpmnModel model = new BpmnModel();
        model.addNamespace("cris", "edu.purdue.cybercenter.dm");
        model.setTargetNamespace("edu.purdue.cybercenter.dm");
        Import importa = new Import();
        importa.setNamespace("xsi:schemaLocation");
        importa.setLocation("http://www.omg.org/spec/BPMN/20100524/MODEL  http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd");
        model.setImports(Arrays.asList(importa));
        //model.addNamespace("xsi:schemaLocation", "http://www.omg.org/spec/BPMN/20100524/MODEL  http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd");

        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        model.addProcess(process);

        process.setId(key);
        process.setName(name);
        process.setDocumentation(description);
        process.setExecutable(true);

        process.setExecutionListeners(createExecutionListeners(initialDatasetState.toString(), Helper.deepSerialize(finalDatasetStates)));

        process.addExtensionElement(createPropertyElement("cris:field", "uuid", uuid));
        process.addExtensionElement(createPropertyElement("cris:field", "cris-workflow-implementation-version", "1.0.0"));

        Map<String, InputStream> theEndFile = filesToInclude.get(WebHelper.KEY_THE_END_FILE);
        if (!CollectionUtils.isEmpty(theEndFile)) {
            process.addExtensionElement(createPropertyElement("cris:field", WebHelper.KEY_THE_END_FILE, WebHelper.THE_END_PAGE_FILE_NAME));
        }

        process.addFlowElement(createStartEvent());
        process.addFlowElement(createEndEvent());

        Map<String, Object> tasks = (Map<String, Object>) object.get("tasks");
        Map<String, Object> flows = (Map<String, Object>) object.get("flows");
        // we need to keep the order of flows out of exclusive gateway
        // true first and then false
        List<Map<String, Object>> flowList = new ArrayList<>();

        // json start flow
        String jsonFromTheStartId = (String) object.get("startTaskId");
        Map<String, Object> startFlow = new HashMap<>();
        String startId = JSON_START_EVENT_KEY + "_" + jsonFromTheStartId;
        startFlow.put("flowType", "sequenceFlow");
        startFlow.put("id", startId);
        startFlow.put("sourceRef", JSON_START_EVENT_KEY);
        startFlow.put("targetRef", jsonFromTheStartId);
        flowList.add(startFlow);

        for (Map.Entry<String, Object> entry : flows.entrySet()) {
            Map<String, Object> flow = (Map<String, Object>) entry.getValue();
            flowList.add(flow);
        }
        flows.put(startId, startFlow);

        List<String> xmlAsyncTaskIds = new ArrayList<>();
        List<String> jsonExclusiveGatewayIds = new ArrayList<>();
        Map<String, String> jsonIdToXmlId = new HashMap<>();

        jsonIdToXmlId.put(JSON_START_EVENT_KEY, XML_START_EVENT_ID);
        jsonIdToXmlId.put(JSON_END_EVENT_KEY, XML_END_EVENT_ID);

        int taskNo = 1;
        int exgwNo = 1;
        int flowNo = 1;

        // Tasks (User Service and Report Tasks)
        for (Map.Entry<String, Object> entry : tasks.entrySet()) {
            Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
            String taskId = (String) valueMap.get("id");
            String taskType = (String) valueMap.get("taskType");

            String xmlId;
            if (taskType.equals(EXCLUSIVE_GATEWAY_NAME)) {
                xmlId = PREFIX_EXCLUSIVE_GATEWAY + exgwNo;
            } else {
                xmlId = PREFIX_TASK + taskNo;
            }
            jsonIdToXmlId.put(entry.getKey(), xmlId);

            Map<String, InputStream> files = filesToInclude.get(taskId);

            if (USER_TASK_NAME.equals(taskType) && !DATASET_STATE_SELECTION_TASK_NAME.equals((String) valueMap.get("name"))) {
                UserTask userTask = createUserTask(xmlId, valueMap, files);
                process.addFlowElement(userTask);
                taskNo++;
            } else if (SERVICE_TASK_NAME.equals(taskType)) {
                ServiceTask serviceTask = createServiceTask(xmlId, valueMap, files);
                process.addFlowElement(serviceTask);
                taskNo++;

                if (isAsyncTask(valueMap)) {
                    xmlAsyncTaskIds.add(xmlId);
                }
            } else if (REPORT_TASK_NAME.equals(taskType)) {
                ServiceTask reportTask = createReportTask(xmlId, valueMap);
                process.addFlowElement(reportTask);
                taskNo++;

                if (isAsyncTask(valueMap)) {
                    xmlAsyncTaskIds.add(xmlId);
                }
            } else if (taskType.equals(EXCLUSIVE_GATEWAY_NAME)) {
                ExclusiveGateway exclusiveGateway = createExclusiveGateway(xmlId, valueMap);
                process.addFlowElement(exclusiveGateway);
                exgwNo++;

                jsonExclusiveGatewayIds.add(taskId);

                // exclusive gateway manages the two outgoing flows
                // we extract the two flows
                Map<String, String> targetRefs = (Map) valueMap.get("targetRef");
                String targetRefTrue = (String) targetRefs.get("True");
                String targetRefFalse = (String) targetRefs.get("False");
                String conditionExpression = (String) valueMap.get("conditionExpression");

                Map<String, Object> trueFlow = new HashMap<>();
                String id = taskId + "_true" + "_" + targetRefTrue;
                trueFlow.put("flowType", "sequenceFlow");
                trueFlow.put("id", id);
                trueFlow.put("sourceRef", taskId);
                trueFlow.put("targetRef", targetRefTrue);
                trueFlow.put("conditionExpression", conditionExpression);
                flows.put(id, trueFlow);
                flowList.add(trueFlow);

                Map<String, Object> falseFlow = new HashMap<>();
                id = taskId + "_false" + "_" + targetRefFalse;
                falseFlow.put("flowType", "sequenceFlow");
                falseFlow.put("id", id);
                falseFlow.put("sourceRef", taskId);
                falseFlow.put("targetRef", targetRefFalse);
                flows.put(id, falseFlow);
                flowList.add(falseFlow);
            }
        }

        // final state selection page
        if (finalDatasetStates.size() > 1) {
            String xmlId = PREFIX_TASK + taskNo;
            jsonIdToXmlId.put(DATASET_STATE_SELECTION_TASK_NAME, xmlId);

            Map<String, Object> data = new HashMap<>();
            data.put("id", xmlId);
            data.put("name", DATASET_STATE_SELECTION_TASK_NAME);
            data.put("documentation", DATASET_STATE_SELECTION_TASK_NAME);
            data.put("ui_page", DATASET_STATE_SELECTION_TASK_FILE_NAME);

            UserTask userTask = createUserTask(xmlId, data, (Map<String, InputStream>) null);
            process.addFlowElement(userTask);
            taskNo++;
        }

        // json end flows
        List<String> jsonToTheEndIds = (List) object.get("endTaskIds");
        for (String jsonToTheEndId : jsonToTheEndIds) {
            if (!jsonExclusiveGatewayIds.contains(jsonToTheEndId)) {
                Map<String, Object> endFlow = new HashMap<>();
                String endId = jsonToTheEndId + "_" + JSON_END_EVENT_KEY;
                endFlow.put("flowType", "sequenceFlow");
                endFlow.put("id", endId);
                endFlow.put("sourceRef", jsonToTheEndId);
                endFlow.put("targetRef", JSON_END_EVENT_KEY);
                flows.put(endId, endFlow);
                flowList.add(endFlow);
            }
        }

        // Sequence Flows
        for (Map<String, Object> valueMap : flowList) {
            String flowType = (String) valueMap.get("flowType");

            if (flowType.equals("sequenceFlow")) {
                String xmlSourceRef = jsonIdToXmlId.get((String) valueMap.get("sourceRef"));
                String xmlTargetRef = jsonIdToXmlId.get((String) valueMap.get("targetRef"));
                String conditionExpression = (String) valueMap.get("conditionExpression");

                boolean isAsyncTask = xmlAsyncTaskIds.contains(xmlSourceRef);

                // for an asynchronous task as its source
                // 1. insert a receive task and make it the target
                // 2. inset a new flow with the receive task as its target
                // 3. change the source to the receive task
                if (isAsyncTask) {
                    ReceiveTask receiveTask = createReceiveTask(xmlSourceRef + "_a", valueMap);
                    process.addFlowElement(receiveTask);

                    SequenceFlow flow = createSequenceFlow(PREFIX_FLOW + flowNo, xmlSourceRef, xmlSourceRef + "_a", null);
                    process.addFlowElement(flow);
                    flowNo++;

                    xmlSourceRef = xmlSourceRef + "_a";
                }

                // for an end task as its target and there's a dataset state task
                // 1. make dataset state task the target
                if (XML_END_EVENT_ID.equals(xmlTargetRef) && finalDatasetStates.size() > 1) {
                    xmlTargetRef = jsonIdToXmlId.get(DATASET_STATE_SELECTION_TASK_NAME);
                }

                SequenceFlow flow = createSequenceFlow(PREFIX_FLOW + flowNo, xmlSourceRef, xmlTargetRef, conditionExpression);
                process.addFlowElement(flow);
                flowNo++;
            }
        }

        // flow for dataset state selection page
        if (finalDatasetStates.size() > 1) {
            SequenceFlow sequenceFlow = createSequenceFlow(PREFIX_FLOW + flowNo, jsonIdToXmlId.get(DATASET_STATE_SELECTION_TASK_NAME), XML_END_EVENT_ID, null);
            process.addFlowElement(sequenceFlow);
            flowNo++;
        }

        // write the content into xml file
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] byteArray = converter.convertToXML(model, "UTF-8");
        InputStream inputStream = new ByteArrayInputStream(byteArray);

        String fileName = getFileName(name, ".bpmn20.xml");
        Map.Entry<String, InputStream> entry = new HashMap.SimpleEntry<>(fileName, inputStream);

        return entry;
    }

    private static Map<String, Object> createUserTask(String jsonId, Map<String, Object> crisProperties, FlowElement flowElement) {
        UserTask userTask = (UserTask) flowElement;

        String jsonIn = "{}";
        List<FormProperty> formProperties = userTask.getFormProperties();
        for (FormProperty property : formProperties) {
            String id = property.getId();
            if ("jsonIn".equals(id)) {
                jsonIn = property.getName();
                break;
            }
        }

        String[] files = (String[]) crisProperties.get("files");

        Integer top = (Integer) crisProperties.get("top");
        Integer left = (Integer) crisProperties.get("left");
        if (top == null) {
            top = 100;
        }
        if (left == null) {
            left = 100;
        }
        String orientation = (String) crisProperties.get("orientation");
        if (StringUtils.isEmpty(orientation)) {
            orientation = "normal";
        }

        int datasetState = getDatasetState(userTask);

        Map<String, Object> task = new HashMap<>();
        task.put("taskType", USER_TASK_NAME);
        task.put("id", jsonId);
        task.put("name", userTask.getName());
        task.put("documentation", userTask.getDocumentation());

        task.put("ui_page", userTask.getFormKey());
        task.put("users", userTask.getCandidateUsers());
        task.put("groups", userTask.getCandidateGroups());

        task.put("jsonIn", jsonIn);

        // dataset state
        task.put("datasetState", datasetState);

        // files
        task.put("files", (files == null ? new String[0] : files));

        // UI location, orientation
        task.put("top", top);
        task.put("left", left);
        task.put("orientation", orientation);

        return task;
    }

    private static Map<String, Object> createServiceTask(String jsonId, Map<String, Object> crisProperties, FlowElement flowElement) {
        ServiceTask serviceTask = (ServiceTask) flowElement;

        Map<String, String> activitiProperties = getActivitiFields(serviceTask.getFieldExtensions(), Arrays.asList("filesToPlace", "jsonIn", "commandLine", "jsonOut", "preFilter", "postFilter", "filesToCollect", "files", "uiLocation", "orientation"));
        String filesToPlace = activitiProperties.get("filesToPlace");
        String jsonIn = activitiProperties.get("jsonIn");
        String commandLine = activitiProperties.get("commandLine");
        String jsonOut = activitiProperties.get("jsonOut");
        String preFilter = activitiProperties.get("preFilter");
        String postFilter = activitiProperties.get("postFilter");
        String filesToCollect = activitiProperties.get("filesToCollect");

        String[] files = (String[]) crisProperties.get("files");

        Integer top = (Integer) crisProperties.get("top");
        Integer left = (Integer) crisProperties.get("left");
        if (top == null) {
            top = 100;
        }
        if (left == null) {
            left = 100;
        }
        String orientation = (String) crisProperties.get("orientation");
        if (StringUtils.isEmpty(orientation)) {
            orientation = "normal";
        }

        int datasetState = getDatasetState(serviceTask);

        Map<String, Object> task = new HashMap<>();
        task.put("taskType", SERVICE_TASK_NAME);
        task.put("id", jsonId);
        task.put("name", serviceTask.getName());
        task.put("documentation", serviceTask.getDocumentation());
        task.put("syncTask", !isAsyncTask(serviceTask));

        task.put("filesToPlace", filesToPlace);
        task.put("jsonIn", jsonIn);
        task.put("commandline", commandLine);
        task.put("jsonOut", jsonOut);
        task.put("prefilter", preFilter);
        task.put("postfilter", postFilter);
        task.put("filesToCollect", filesToCollect);

        // dataset state
        task.put("datasetState", datasetState);

        // files
        task.put("files", (files == null ? new String[0] : files));

        // UI location, orientation
        task.put("top", top);
        task.put("left", left);
        task.put("orientation", orientation);

        return task;
    }

    private static Map<String, Object> createReportTask(String jsonId, Map<String, Object> crisProperties, FlowElement flowElement) {
        ServiceTask serviceTask = (ServiceTask) flowElement;

        Map<String, String> activitiProperties = getActivitiFields(serviceTask.getFieldExtensions(), Arrays.asList("command", "reportId", "templateId", "parameters", "outputType", "files", "uiLocation", "orientation"));
        String command = activitiProperties.get("command");
        String reportId = activitiProperties.get("reportId");
        String templateId = activitiProperties.get("templateId");
        String parameters = activitiProperties.get("parameters");
        String outputType = activitiProperties.get("outputType");

        String[] files = (String[]) crisProperties.get("files");

        Integer top = (Integer) crisProperties.get("top");
        Integer left = (Integer) crisProperties.get("left");
        if (top == null) {
            top = 100;
        }
        if (left == null) {
            left = 100;
        }
        String orientation = (String) crisProperties.get("orientation");
        if (StringUtils.isEmpty(orientation)) {
            orientation = "normal";
        }

        int datasetState = getDatasetState(serviceTask);

        Map<String, Object> task = new HashMap<>();
        task.put("taskType", REPORT_TASK_NAME);
        task.put("id", jsonId);
        task.put("name", serviceTask.getName());
        task.put("documentation", serviceTask.getDocumentation());
        task.put("syncTask", !isAsyncTask(serviceTask));

        task.put("command", command);
        task.put("reportId", reportId != null ? Integer.parseInt(reportId) : null);
        task.put("templateId", templateId != null ? Integer.parseInt(templateId) : null);
        task.put("parameters", parameters);
        task.put("outputType", outputType);

        // dataset state
        task.put("datasetState", datasetState);

        // files
        task.put("files", (files == null ? new String[0] : files));

        // UI location, orientation
        task.put("top", top);
        task.put("left", left);
        task.put("orientation", orientation);

        return task;
    }

    private static Map<String, Object> createExclusiveGateway(String jsonId, Map<String, Object> crisProperties, FlowElement flowElement) {
        ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;

        Integer top = (Integer) crisProperties.get("top");
        Integer left = (Integer) crisProperties.get("left");
        if (top == null) {
            top = 100;
        }
        if (left == null) {
            left = 100;
        }
        String orientation = (String) crisProperties.get("orientation");
        if (StringUtils.isEmpty(orientation)) {
            orientation = "normal";
        }

        Map<String, Object> task = new HashMap<>();
        task.put("taskType", EXCLUSIVE_GATEWAY_NAME);
        task.put("id", jsonId);
        task.put("name", exclusiveGateway.getName());
        task.put("documentation", exclusiveGateway.getDocumentation());

        task.put("targetRef", new HashMap<>());

        // UI location, orientation
        task.put("top", top);
        task.put("left", left);
        task.put("orientation", orientation);

        return task;
    }

    private static Map<String, Object> createSequenceFlow(String jsonId, FlowElement flowElement, Map<String, String> xmlIdToJsonId) {
        SequenceFlow sequenceFlow = (SequenceFlow) flowElement;

        String jsonSourceRef = xmlIdToJsonId.get(sequenceFlow.getSourceRef());
        String jsonTargetRef = xmlIdToJsonId.get(sequenceFlow.getTargetRef());

        Map<String, Object> flow = createSequenceFlow(jsonId, sequenceFlow.getName(), sequenceFlow.getDocumentation(), jsonSourceRef, jsonTargetRef, sequenceFlow.getConditionExpression());

        return flow;
    }

    private static Map<String, Object> createSequenceFlow(String jsonId, String name, String documentation, String jsonSourceRef, String jsonTargetRef, String conditionExpression) {
        String fixedConditionExpression = null;
        if (StringUtils.isNotEmpty(conditionExpression)) {
            if (conditionExpression.startsWith("${") && conditionExpression.endsWith("}")) {
                fixedConditionExpression = conditionExpression.substring(2, conditionExpression.length() - 1);
            } else {
                fixedConditionExpression = conditionExpression;
            }
        }

        Map<String, Object> jsonFlow = new HashMap<>();
        jsonFlow.put("flowType", "sequenceFlow");
        jsonFlow.put("id", jsonId);
        jsonFlow.put("name", name);
        jsonFlow.put("documentation", documentation);

        jsonFlow.put("sourceRef", jsonSourceRef);
        jsonFlow.put("targetRef", jsonTargetRef);
        jsonFlow.put("conditionExpression", fixedConditionExpression);

        return jsonFlow;
    }

    private static Map<String, String> getActivitiFields(List<FieldExtension> fieldExtensions, List<String> propertyNames) {
        Map<String, String> properties = new HashMap<>();

        if (fieldExtensions != null) {
            for (FieldExtension fieldExtension : fieldExtensions) {
                String name = fieldExtension.getFieldName();
                String value = fieldExtension.getStringValue();
                if (propertyNames.contains(name)) {
                    properties.put(name, value);
                }
            }
        }

        return properties;
    }

    private static Map<String, String> getCrisFields(Map<String, List<ExtensionElement>> extensionElementsMap, List<String> propertyNames) {
        Map<String, String> properties = new HashMap<>();

        List<ExtensionElement> extensionElements = extensionElementsMap.get("field");
        if (extensionElements != null) {
            for (ExtensionElement extensionElement : extensionElements) {
                Map<String, List<ExtensionAttribute>> attributes = extensionElement.getAttributes();
                List<ExtensionAttribute> nameAttributes = attributes.get("name");
                List<ExtensionAttribute> valueAttributes = attributes.get("stringValue");
                if (!CollectionUtils.isEmpty(nameAttributes) && !CollectionUtils.isEmpty(valueAttributes)) {
                    ExtensionAttribute nameAttribute = nameAttributes.get(0);
                    ExtensionAttribute valueAttribute = valueAttributes.get(0);
                    String name = nameAttribute.getValue();
                    String value = valueAttribute.getValue();
                    if (propertyNames.contains(name)) {
                        properties.put(name, value);
                    }
                }
            }
        }

        return properties;
    }

    private static int getDatasetState(FlowElement task) {
        String datasetState = null;
        List<ActivitiListener> listeners = task.getExecutionListeners();
        for (ActivitiListener listener : listeners) {
            List<FieldExtension> fieldExtensions = listener.getFieldExtensions();
            for (FieldExtension fieldExtension : fieldExtensions) {
                String name = fieldExtension.getFieldName();
                if ("datasetState".equals(name)) {
                    datasetState = fieldExtension.getStringValue();
                    break;
                }
            }
            if (datasetState != null) {
                break;
            }
        }

        int iDatasetState;
        if (StringUtils.isEmpty(datasetState)) {
            iDatasetState = 0;
        } else {
            iDatasetState = Integer.parseInt(datasetState);
        }

        return iDatasetState;
    }

    private static boolean isAsyncTask(ServiceTask task) {
        boolean async = false;
        String implementaion = task.getImplementation();
        if ("edu.purdue.cybercenter.dm.activiti.ServiceTaskAsyncDelegate".equals(implementaion)) {
            async = true;
        }
        return async;
    }

    private static Map<String, Object> getInfoForEditor(String xmlId, Document document) {
        Map<String, Object> properties = new HashMap<>();

        Element node = document.getDocumentElement();
        NodeList nodeList = node.getChildNodes();

        // process tasks and exclusive gateways
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("process")) {
                    NodeList nodeListProcess = currentNode.getChildNodes();
                    for (int j = 0; j < nodeListProcess.getLength(); j++) {
                        Node currentNodeProcess = nodeListProcess.item(j);
                        if (currentNodeProcess.getNodeType() == Node.ELEMENT_NODE) {

                            if (Arrays.asList("userTask", "serviceTask", EXCLUSIVE_GATEWAY_NAME).contains(currentNodeProcess.getNodeName())) {
                                String id = ((Element) currentNodeProcess).getAttribute("id");
                                if (xmlId.equals(id)) {
                                    NodeList nodeListCondTask = currentNodeProcess.getChildNodes();
                                    for (int k = 0; k < nodeListCondTask.getLength(); k++) {
                                        Node currentNodeCondTask = nodeListCondTask.item(k);
                                        if (currentNodeCondTask.getNodeName().equals("extensionElements")) {
                                            NodeList nodeListCondTaskExtElement = currentNodeCondTask.getChildNodes();
                                            for (int l = 0; l < nodeListCondTaskExtElement.getLength(); l++) {
                                                Node currentNodeCondTaskExtElement = nodeListCondTaskExtElement.item(l);
                                                if (currentNodeCondTaskExtElement.getNodeName().equals("cris:field")) {
                                                    Element eElementCondTaskExtElement = (Element) currentNodeCondTaskExtElement;
                                                    String name = eElementCondTaskExtElement.getAttribute("name");
                                                    String value = eElementCondTaskExtElement.getAttribute("stringValue");
                                                    if (name.equals("files") && value != null && !value.isEmpty()) {
                                                        properties.put("files", value.split(";"));
                                                    } else if (name.equals("uiLocation")) {
                                                        String[] words = value.split(",");
                                                        if (words.length == 2) {
                                                            properties.put("top", Integer.parseInt(words[0]));
                                                            properties.put("left", Integer.parseInt(words[1]));
                                                        } else {
                                                            properties.put("top", 100);
                                                            properties.put("left", 100);
                                                        }
                                                    } else if (name.equals("orientation")) {
                                                        properties.put("orientation", value);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return properties;
    }

    public static String convertXmlWorkflowToJson(String fileName, Integer id) throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
        /***************************************************************
         * Used to get info for workflow editor
         ***************************************************************/
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(new FileInputStream(fileName));
        /***************************************************************
         * Used to get info for workflow editor
         ***************************************************************/

        int taskNo = 1;
        int exgwNo = 1;

        Map<String, String> xmlIdToJsonId = new HashMap<>();
        xmlIdToJsonId.put(XML_START_EVENT_ID, JSON_START_EVENT_KEY);
        xmlIdToJsonId.put(XML_END_EVENT_ID, JSON_END_EVENT_KEY);

        Map<String, Map<String, Object>> jsonTasks = new HashMap<>();
        Map<String, Map<String, Object>> jsonFlows = new HashMap<>();

        String xmlFromTheStartId = null;
        String xmlDatasetStateTaskId = null;
        List<String> xmlToTheEndIds = new ArrayList<>();
        List<String> xmlReceiveTaskIds = new ArrayList<>();
        Map<String, String> xmlReceiveTasksSource = new HashMap<>();
        Map<String, String> xmlReceiveTasksTarget = new HashMap<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader xmlReader = factory.createXMLStreamReader(new FileInputStream(fileName));
        BpmnXMLConverter converter = new BpmnXMLConverter();
        BpmnModel model = converter.convertToBpmnModel(xmlReader);
        System.out.print(model);

        List<org.activiti.bpmn.model.Process> processes = model.getProcesses();
        if (CollectionUtils.isEmpty(processes)) {
            throw new RuntimeException("No processes defined");
        } else if (processes.size() > 1) {
            throw new RuntimeException("More than one process defined: " + processes.size());
        }
        org.activiti.bpmn.model.Process process = processes.get(0);

        String initialDatasetState = null;
        String finalDatasetStates = null;
        List<ActivitiListener> listeners = process.getExecutionListeners();
        for (ActivitiListener listener : listeners) {
            List<FieldExtension> fieldExtensions = listener.getFieldExtensions();
            for (FieldExtension fieldExtension : fieldExtensions) {
                String name = fieldExtension.getFieldName();
                switch (name) {
                    case "initialDatasetState":
                        initialDatasetState = fieldExtension.getStringValue();
                        break;
                    case "finalDatasetStates":
                        finalDatasetStates = fieldExtension.getStringValue();
                        break;
                }
            }
            if (initialDatasetState != null && finalDatasetStates != null) {
                break;
            }
        }

        Map<String, Object> workflow = new HashMap<>();

        // workflow itself
        workflow.put("id", id);
        workflow.put("key", process.getId());
        workflow.put("name", process.getName());
        workflow.put("documentation", process.getDocumentation());

        Map<String, String> crisProperties = getCrisFields(process.getExtensionElements(), Arrays.asList("uuid", "cris-workflow-implementation-version", WebHelper.KEY_THE_END_FILE));
        workflow.put("uuid", crisProperties.get("uuid"));
        workflow.put("cris-workflow-implementation-version", crisProperties.get("cris-workflow-implementation-version"));
        workflow.put(WebHelper.KEY_THE_END_FILE, crisProperties.get(WebHelper.KEY_THE_END_FILE));

        workflow.put("initialDatasetState", initialDatasetState);
        workflow.put("finalDatasetStates", finalDatasetStates);

        Collection<FlowElement> flowElements = process.getFlowElements();

        // process tasks and exclusive gateways
        for (FlowElement flowElement : flowElements) {
            String jsonId;
            Map<String, Object> task;

            String xmlId = flowElement.getId();
            String taskClass = flowElement.getClass().getSimpleName();
            String taskName = flowElement.getName();
            if (DATASET_STATE_SELECTION_TASK_NAME.equals(taskName)) {
                taskClass = "DatasetStateTask";
            }
            Map<String, Object> crisFields = getInfoForEditor(xmlId, document);
            switch (taskClass) {
                case "StartEvent":
                    // nothing to do
                    jsonId = JSON_START_EVENT_KEY;
                    task = null;
                    break;
                case "EndEvent":
                    // nothing to do
                    jsonId = JSON_END_EVENT_KEY;
                    task = null;
                    break;
                case "UserTask":
                    jsonId = PREFIX_TASK + taskNo;
                    task = createUserTask(jsonId, crisFields, flowElement);
                    taskNo++;
                    break;
                case "ServiceTask":
                    jsonId = PREFIX_TASK + taskNo;
                    ServiceTask serviceTask = (ServiceTask) flowElement;
                    String clazz = serviceTask.getImplementation();
                    if ("edu.purdue.cybercenter.dm.activiti.ServiceTaskDelegate".equals(clazz) ||
                        "edu.purdue.cybercenter.dm.activiti.ServiceTaskAsyncDelegate".equals(clazz)) {
                        task = createServiceTask(jsonId, crisFields, flowElement);
                    } else {
                        task = createReportTask(jsonId, crisFields, flowElement);
                    }
                    taskNo++;
                    break;
                case "ExclusiveGateway":
                    jsonId = PREFIX_EXCLUSIVE_GATEWAY + exgwNo;
                    task = createExclusiveGateway(jsonId, crisFields, flowElement);
                    exgwNo++;
                    break;
                case "ReceiveTask":
                    // no json task is created. used only to fix flows
                    jsonId = PREFIX_TASK + taskNo;
                    task = null;
                    xmlReceiveTaskIds.add(xmlId);

                    ReceiveTask receiveTask = (ReceiveTask) flowElement;
                    List<SequenceFlow> incomingFlows = receiveTask.getIncomingFlows();
                    List<SequenceFlow> outgoingFlows = receiveTask.getOutgoingFlows();
                    for (SequenceFlow outgoingFlow : outgoingFlows) {
                        xmlReceiveTasksTarget.put(xmlId, outgoingFlow.getSourceRef());
                    }
                    for (SequenceFlow incomingFlow : incomingFlows) {
                        xmlReceiveTasksSource.put(xmlId, incomingFlow.getTargetRef());
                    }

                    taskNo++;
                    break;
                case "DatasetStateTask":
                    // no json task is created. used only to fix flows
                    jsonId = PREFIX_TASK + taskNo;
                    task = null;
                    xmlDatasetStateTaskId = xmlId;
                    break;
                default:
                    jsonId = null;
                    task = null;
            }

            if (jsonId != null) {
                xmlIdToJsonId.put(xmlId, jsonId);
            }
            if (task != null) {
                jsonTasks.put(jsonId, task);
            }
        }

        // process flows
        for (FlowElement flowElement : flowElements) {
            String flowClass = flowElement.getClass().getSimpleName();
            if ("SequenceFlow".equals(flowClass)) {
                SequenceFlow xmlflow = (SequenceFlow) flowElement;
                String xmlSourceRef = xmlflow.getSourceRef();
                String xmlTargetRef = xmlflow.getTargetRef();

                boolean inReceiveTask = xmlReceiveTaskIds.contains(xmlTargetRef);
                boolean outDatasetStateTask = StringUtils.isNotEmpty(xmlDatasetStateTaskId) && xmlDatasetStateTaskId.equals(xmlSourceRef);
                if (inReceiveTask || outDatasetStateTask) {
                    // drop the flow
                    continue;
                }

                boolean outReceiveTask = xmlReceiveTaskIds.contains(xmlSourceRef);
                if (outReceiveTask) {
                    // change source to the source of the receive task
                    FlowElement sourceElement = process.getFlowElement(xmlSourceRef);
                    ReceiveTask receiveTask = (ReceiveTask) sourceElement;
                    List<SequenceFlow> incomingFlows = receiveTask.getIncomingFlows();
                    for (SequenceFlow incomingFlow : incomingFlows) {
                        // there should only be one
                        xmlSourceRef = incomingFlow.getSourceRef();
                    }
                }

                boolean inDatasetStateTask = StringUtils.isNotEmpty(xmlDatasetStateTaskId) && xmlDatasetStateTaskId.equals(xmlTargetRef);
                if (inDatasetStateTask) {
                    // change the target to the end task
                    xmlTargetRef = XML_END_EVENT_ID;
                }

                String jsonSourceRef = xmlIdToJsonId.get(xmlSourceRef);
                String jsonTargetRef = xmlIdToJsonId.get(xmlTargetRef);
                FlowElement sourceElement = process.getFlowElement(xmlSourceRef);
                String sourceClass = sourceElement.getClass().getSimpleName();

                if (XML_START_EVENT_ID.equals(xmlSourceRef)) {
                    xmlFromTheStartId = xmlTargetRef;
                    continue;
                }
                if (XML_END_EVENT_ID.equals(xmlTargetRef)) {
                    if (!"ExclusiveGateway".equals(sourceClass)) {
                        xmlToTheEndIds.add(xmlSourceRef);
                        continue;
                    }
                }

                if ("ExclusiveGateway".equals(sourceClass)) {
                    Map<String, Object> exclusiveGateway = jsonTasks.get(jsonSourceRef);
                    Map<String, Object> targetRefs = (Map) exclusiveGateway.get("targetRef");
                    String xmlConditionExpression = xmlflow.getConditionExpression();
                    if (StringUtils.isNotEmpty(xmlConditionExpression)) {
                        String conditionExpression;
                        if (xmlConditionExpression.startsWith("${") && xmlConditionExpression.endsWith("}")) {
                            conditionExpression = xmlConditionExpression.substring(2, xmlConditionExpression.length() - 1);
                        } else {
                            conditionExpression = xmlConditionExpression;
                        }

                        // put the conditionExpression into exclusiveGateway
                        exclusiveGateway.put("conditionExpression", conditionExpression);
                        targetRefs.put("True", jsonTargetRef);
                    } else {
                        targetRefs.put("False", jsonTargetRef);
                    }

                    // the exclusive gateway manages the flow so we don't create a flow here
                } else {
                    // create a flow
                    Map<String, Object> flow = createSequenceFlow(jsonSourceRef, xmlflow.getName(), xmlflow.getDocumentation(), jsonSourceRef, jsonTargetRef, null);
                    jsonFlows.put(jsonSourceRef, flow);
                }
            }
        }

        String jsonFromTheStartId = xmlIdToJsonId.get(xmlFromTheStartId);
        List<String> jsonToTheEndIds = new ArrayList<>();
        for (String xmlToTheEndId : xmlToTheEndIds) {
            jsonToTheEndIds.add(xmlIdToJsonId.get(xmlToTheEndId));
        }

        workflow.put("tasks", jsonTasks);
        workflow.put("flows", jsonFlows);
        workflow.put("startTaskId", jsonFromTheStartId);
        workflow.put("endTaskIds", jsonToTheEndIds);

        String json = Helper.deepSerialize(workflow);

        return json;
    }

    public static Boolean isResourceRequest(HttpServletRequest request) {
        Boolean yes = false;

        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(contextPath + "/static")) {
            yes = true;
        }

        return yes;
    }

    public static Map<String, Object> getWhere(Project project, Experiment experiment) {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> data1 = new HashMap<>();
        data1.put(DomainObjectHelper.DOJO_FILTER_OP, EnumDojoOperator.typeString.getName());
        if (project == null) {
            data1.put(DomainObjectHelper.DOJO_FILTER_DATA, "experimentId.id");
        } else {
            data1.put(DomainObjectHelper.DOJO_FILTER_DATA, "projectId.id");
        }
        data1.put(DomainObjectHelper.DOJO_FILTER_IS_COL, true);
        data.add(data1);

        Map<String, Object> data2 = new HashMap<>();
        data2.put(DomainObjectHelper.DOJO_FILTER_OP, EnumDojoOperator.typeString.getName());
        if (project == null) {
            data2.put(DomainObjectHelper.DOJO_FILTER_DATA, "" + experiment.getId());
        } else {
            data2.put(DomainObjectHelper.DOJO_FILTER_DATA, "" + project.getId());
        }
        data2.put(DomainObjectHelper.DOJO_FILTER_IS_COL, false);
        data.add(data2);

        Map<String, Object> where = new HashMap<>();
        where.put(DomainObjectHelper.DOJO_FILTER_OP, EnumDojoOperator.equal.getName());
        where.put(DomainObjectHelper.DOJO_FILTER_DATA, data);

        return where;
    }

    public static Entry<String, String> convertToOrderBy(String input) {
        String ad;
        if (input.charAt(0) == '+' || input.charAt(0) == ' ') {
            ad = "asc";
        } else {
            ad = "desc";
        }

        Entry<String, String> e = null;

        if (input.length() > 2) {
            e = new java.util.AbstractMap.SimpleEntry<>(input.substring(1), ad);
        }

        return e;
    }

    public static Entry<String, String> getDojoJsonRestStoreFilter(String filter) {

        Entry<String, String> where = null;

        if (filter != null) {
            String[] arr = filter.split(":");

            if (arr.length == 2) {
                where = new java.util.AbstractMap.SimpleEntry<>(arr[0], arr[1]);
            }
        }

        return where;
    }

    public static Map<String, Object> FromJsonToFilterClass(String filter) {
        Map<String, Object> where;

        if (!StringUtils.isBlank(filter)) {
            where = Helper.deserialize(filter, Map.class);
        } else {
            where = null;
        }

        return where;
    }

    public static Entry<String, String> getDojoJsonRestStoreOrderBy(Enumeration paramNames) {
        Entry<String, String> orderBy = null;
        while (paramNames.hasMoreElements()) {
            String name = (String) paramNames.nextElement();
            if (name.matches("^sort\\(.+\\)$")) {
                String orderByString = name.substring(5, name.length() - 1);
                orderBy = WebHelper.convertToOrderBy(orderByString);
            }
        }

        return orderBy;
    }

    public static Integer[] getDojoGridPaginationInfo(HttpServletRequest request) {
        String range = request.getHeader("Range");
        Integer firstResult, lastResult;
        Integer[] ia = new Integer[2];
        if (range != null) {
            String[] a = range.replaceFirst("items=", "").split("-");
            if (a.length == 2) {
                firstResult = Integer.parseInt(a[0]);
                lastResult = Integer.parseInt(a[1]);
            } else {
                firstResult = 0;
                lastResult = 99;
            }
        } else {
            firstResult = 0;
            lastResult = 99;
        }
        ia[0] = firstResult;
        ia[1] = lastResult;

        return ia;
    }

    public static void setDojoGridPaginationInfo(Integer firstResult, Integer lastResult, Integer total, HttpServletResponse response) {
        response.addHeader("Content-Range", "items " + firstResult + "-" + lastResult + "/" + total);
    }

    public static void setDojoLocationHeader(String ref, HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("Location", request.getContextPath() + "/" + ref);
    }

    /*
     private static final String s1 = "{\"op\":\"all\",\"data\":[{\"op\":\"largerEqual\",\"data\":[{\"op\":\"date\",\"data\":\"";
     //timeUpdated
     private static final String s2 = "\",\"isCol\":true},{\"op\":\"date\",\"data\":";
     //1309492800000
     private static final String s3 = ",\"isCol\":false}]},{\"op\":\"lessEqual\",\"data\":[{\"op\":\"date\",\"data\":\"";
     //timeUpdated
     private static final String s4 = "\",\"isCol\":true},{\"op\":\"date\",\"data\":";
     //1312084800000
     private static final String s5 = ",\"isCol\":false}]}]}";

     public static String buildJsonBetweenDateFilter(String field, Date start, Date end) {
     StringBuilder dateFilter = new StringBuilder();
     dateFilter.append(s1).append(field).append(s2).append(start.getTime()).append(s3).append(field).append(s4).append(end.getTime()).append(s5);
     return dateFilter.toString();
     }
     */
    public static String buildJsonBetweenDateFilter(Date start, Date end) {
        Map<String, Date> map = new HashMap<>();
        map.put("start", start);
        map.put("end", end);
        return Helper.serialize(map);
    }

    public static Map<String, Object> buildContext(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");

        Integer jobId = null;
        String sJobId = request.getParameter("jobId");
        if (sJobId != null && !sJobId.isEmpty()) {
            jobId = Integer.parseInt(sJobId);
        }

        String taskId = request.getParameter("taskId");
        String processInstanceId = request.getParameter("processInstanceId");

        Integer projectId = null;
        Integer experimentId = null;
        if (jobId != null) {
            Job job = Job.findJob(jobId);
            if (job != null) {
                projectId = job.getProjectId().getId();
                experimentId = job.getExperimentId().getId();
            }
        }

        if (projectId == null) {
            String sId = request.getParameter("projectId");
            try {
                projectId = Integer.parseInt(sId);
            } catch (Exception ex) {
            }
        }

        if (experimentId == null) {
            String sId = request.getParameter("experimentId");
            try {
                experimentId = Integer.parseInt(sId);
            } catch (Exception ex) {
            }
        }

        Map<String, Object> context = new HashMap<>();
        context.put("project_id", projectId);
        context.put("experiment_id", experimentId);
        context.put("job_id", jobId);
        context.put("task_id", taskId);
        context.put("process_instance_id", processInstanceId);
        context.put("user_id", user.getId());

        return context;
    }
    private final static String JsonToServer = "jsonToServer";

    public static Map<String, Object> buildObjectus(MultipartHttpServletRequest request) {

        Map<String, Object> objectuses = new HashMap<>();

        // 1. regular input fields
        Map parameters = request.getParameterMap();
        for (Object key : parameters.keySet()) {
            if (JsonToServer.equals(key)) {
                String jsonToServer = request.getParameter((String) key);
                if (jsonToServer != null && !jsonToServer.isEmpty()) {
                    Map<String, Object> map = Helper.deserialize(jsonToServer, Map.class);
                    for (String name : map.keySet()) {
                        datasetService.mergeValueToObjectus(objectuses, name, map.get(name));
                    }
                }
            } else {
                Object value = parameters.get(key);
                if (value != null && ((String[]) value).length == 1) {
                    value = ((String[]) value)[0];
                }
                datasetService.mergeValueToObjectus(objectuses, StringEscapeUtils.unescapeJava((String) key).replaceAll("%20", " ").replaceAll("%22", "\"").replaceAll("%27", "'"), value);
            }
        }

        // 2. file upload
        Integer groupId = edu.purdue.cybercenter.dm.threadlocal.GroupId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        Group group = Group.findGroup(groupId);
        Boolean isGroupOwner = group == null ? null : group.getIsGroupOwner();
        Integer ownerId;
        if (isGroupOwner != null && isGroupOwner) {
            ownerId = groupId;
        } else {
            ownerId = userId;
        }
        Map<String, Object> value = new HashMap<>();
        value.put(MetaField.Id, null);
        value.put(MetaField.IsGroupOwner, isGroupOwner);
        value.put(MetaField.OwnerId, ownerId);

        Map<String, Object> context = buildContext(request);
        value.put(MetaField.ProjectId, context.get("project_id"));
        value.put(MetaField.ExperimentId, context.get("experiment_id"));
        value.put(MetaField.JobId, context.get("job_id"));

        collectFiles(request, objectuses, value);

        return objectuses;
    }

    @PreAuthorize(DatasetService.PRE_AUTHORIZE_SAVE)
    private static void collectFiles(MultipartHttpServletRequest request, Map<String, Object> objectuses, Map<String, Object> value) {
        MultiValueMap<String, MultipartFile> fileMultiValueMap = request.getMultiFileMap();

        // 1. regular files
        for (String key : fileMultiValueMap.keySet()) {
            TermName termName = new TermName(key);
            if (termName.getUuid() != null) {
                List<MultipartFile> files = fileMultiValueMap.get(key);
                List<String> storageFiles = new ArrayList<>();
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        try {
                            List<StorageFile> sfs = storageFileManager.putFile(file.getOriginalFilename(), file.getInputStream(), null, true);
                            for (StorageFile storageFile : sfs) {
                                storageFile.setProjectId((Integer) value.get(MetaField.ProjectId));
                                storageFile.setExperimentId((Integer) value.get(MetaField.ExperimentId));
                                storageFile.setJobId((Integer) value.get(MetaField.JobId));
                                storageFile.merge();

                                storageFiles.add("StorageFile:" + storageFile.getId());
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException("Unable to put file in storage: " + file.getOriginalFilename() + ": " + ex.getMessage(), ex);
                        }
                    }
                }

                // if no file is selected. keep the current files
                // so only update when there is at least one file.
                if (!storageFiles.isEmpty()) {
                    datasetService.mergeValueToObjectus(objectuses, key, storageFiles.toArray(), true);
                }
            }
        }

        // 2. Globus Upload
        for (Map.Entry<String, Object> entry : objectuses.entrySet()) {
            String key = entry.getKey();
            if (key.length() >= 36) {
                String globusFileKey = key.substring(0, 36);
                Map<String, Object> globusFiles = (Map) request.getSession().getAttribute(globusFileKey);

                if (globusFiles != null) {
                    globusFiles.entrySet().stream().forEach((globusFile) -> {
                        String alias = globusFile.getKey();
                        Map<String, Object> inputObj = (Map) globusFile.getValue();

                        // initiate a trnsfer
                        List<StorageFile> storageFiles;
                        try {
                            storageFiles = globusStorageFileManager.putFile(Helper.deepSerialize(inputObj), null, false);
                            datasetService.mergeValueToObjectus(objectuses, globusFileKey + "." + alias, storageFilesToStorageFileIds(storageFiles));
                        } catch (IOException ex) {
                            Logger.getLogger(WebHelper.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });

                    // no longer needed
                    request.getSession().removeAttribute(globusFileKey);
                }
            }
        }
    }

    public static String buildIframeResponse(String response) {
        return String.format("<html><body><textarea>%s</textarea></body></html>", (response == null || response.isEmpty() ? "{}" : response));
    }

    public static Map<String, Object> saveFiles(MultipartHttpServletRequest request) {
        MultiValueMap<String, MultipartFile> fileMap = request.getMultiFileMap();
        Map<String, Object> result = saveFiles(fileMap);

        return result;
    }

    public static Map<String, Object> saveFiles(MultiValueMap<String, MultipartFile> mpfMap) {
        // TODO: should implement all or nothing policy?

        Map<String, Object> result = new HashMap<>();

        for (String key : mpfMap.keySet()) {
            List<MultipartFile> files = mpfMap.get(key);
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String filename = file.getOriginalFilename();
                    try {
                        File tempFile = new File("/tmp/" + filename);
                        file.transferTo(tempFile);
                        List<StorageFile> sfs = storageFileManager.putFile(tempFile.getPath(), null, true);
                        for (StorageFile storageFile : sfs) {
                            result.put(filename, "StorageFile:" + storageFile.getId());
                        }
                        tempFile.delete();
                    } catch (IOException | IllegalStateException ex) {
                        result.put(filename, "Failed to save file: " + ex.getMessage());
                    }
                }
            }
        }

        return result;
    }

    public static List<MultipartFile> multipartFileMapToList(MultiValueMap<String, MultipartFile> fileMultiValueMap) {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        if (fileMultiValueMap != null) {
            for (String key : fileMultiValueMap.keySet()) {
                List<MultipartFile> files = fileMultiValueMap.get(key);
                for (MultipartFile file : files) {
                    multipartFiles.add(file);
                }
            }
        }
        return multipartFiles;
    }

    public static Map<String, String> convertMultiValueMapToMap(Map<String, String[]> multiMap) {
        Map<String, String> map = new HashMap<>();
        for (String key : multiMap.keySet()) {
            String value = multiMap.get(key).length == 0 ? null : multiMap.get(key)[0];
            map.put(key, value);
        }

        return map;
    }

    public static String prettyPrint(String text, String delimit) {
        String[] parts = text.split(delimit);
        StringBuilder sb = new StringBuilder();

        for (String part : parts) {
            if (sb.length() != 0) {
                sb.append(" ");
            }
            sb.append(StringUtils.capitalize(part));
        }

        return sb.toString();
    }

    private static String storageFileToStorgaeFileId(StorageFile storageFile) {
        return storageFile != null ? "StorageFile:" + storageFile.getId() : null;
    }

    private static List<String> storageFilesToStorageFileIds(List<StorageFile> storageFiles) {
        List<String> storageFileIds = new ArrayList<>();
        for (StorageFile storageFile : storageFiles) {
            storageFileIds.add(storageFileToStorgaeFileId(storageFile));
        }
        return storageFileIds;
    }

}
