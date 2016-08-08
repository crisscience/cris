/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.storage.AccessMethodType;
import edu.purdue.cybercenter.dm.storage.StorageFileManager;
import edu.purdue.cybercenter.dm.util.ActivitiHelper;
import edu.purdue.cybercenter.dm.util.ConstDatasetState;
import edu.purdue.cybercenter.dm.util.DatasetUtils;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.util.TermName;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 *
 * @author xu222
 */
@Service
public class WorkflowService {

    public static final String JobTemplateUuids = "_jobTemplateUuids";

    private static final String FileGroupSeparater = ";";
    private static final String FileSrcDstSeparater = ":";
    private static final String IsValid = "isValid";
    private static final String ErrorMessage = "errorMessage";
    private static final String DatasetErrorObject = "datasetErrorObject";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TermService termService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private CqlService cqlService;

    @Autowired
    private StorageService storageService;

    public Map<String, Object> makeContextFromJobAndTask(Job job, String sTaskId) {
        Map<String, Object> context = new HashMap<>();

        Integer projectId = null;
        Integer experimentId = null;
        String activityId = null;
        Integer state = null;
        if (job != null) {
            projectId = job.getProjectId().getId();
            experimentId = job.getExperimentId().getId();
            Task task = ActivitiHelper.taskIdToTask(sTaskId);
            if (task != null) {
                HistoricActivityInstance activity = ActivitiHelper.taskToActivity(task);
                activityId = activity.getActivityId();
            } else {
                activityId = sTaskId;
            }

            // figure out the state for the dataset
            ProcessInstance processInstance = ActivitiHelper.jobToProcessInstance(job);
            if (processInstance != null) {
                // the state set for the current step
                state = (Integer) runtimeService.getVariable(processInstance.getId(), ConstDatasetState.DatasetState);
                if (state == null) {
                    // otherwise use the initial state
                    state = (Integer) runtimeService.getVariable(processInstance.getId(), ConstDatasetState.InitialDatasetState);
                }
            }
        }

        context.put(MetaField.ProjectId, projectId);
        context.put(MetaField.ExperimentId, experimentId);
        context.put(MetaField.JobId, job.getId());
        context.put(MetaField.TaskId, activityId);
        context.put(MetaField.State, state);

        return context;
    }

    public void placeFiles(String filesToPlace, String dirPath, Job job, ProcessDefinition processDefinition, Map<String, Object> context) {
        String[] namedPaths = filesToPlace.split(FileGroupSeparater);
        for (String namedPath : namedPaths) {
            String evaledNamedPath;
            if (namedPath.startsWith("ToFile")) {
                // do not eval for ToFile type
                evaledNamedPath = namedPath;
            } else {
                evaledNamedPath = cqlService.eval(namedPath, context);
            }
            placeFile(evaledNamedPath, dirPath, job, processDefinition, context);
        }
    }

    private void placeFile(String namedPath, String dirPath, Job job, ProcessDefinition processDefinition, Map<String, Object> context) {
        int index = namedPath.lastIndexOf(FileSrcDstSeparater);
        if (index == -1) {
            throw new RuntimeException("Wrong format for embedded file in workflow definition.");
        }

        String name = namedPath.substring(0, index);
        String path = namedPath.substring(index + 1).startsWith("/") ? namedPath.substring(index + 1) : dirPath + namedPath.substring(index + 1);

        if (path.endsWith("/")) {
            File dir = new File(path);
            if (dir.mkdirs()) {
                //TODO: throw new RuntimeException("Unable to create directory for fileToPlace: " + path);
            }
        }

        if (name.startsWith("[") && name.endsWith("]")) {
            List<String> names = (List<String>) Helper.deserialize(name, List.class);
            for (String n : names) {
                placeFile(n, path, dirPath, job, processDefinition, context);
            }
        } else {
            placeFile(name, path, dirPath, job, processDefinition, context);
        }
    }

    private void placeFile(String name, String path, String dirPath, Job job, ProcessDefinition processDefinition, Map<String, Object> context) {
        boolean isDir = path.endsWith("/");

        String fixedName;
        if (name.startsWith("\"") && name.endsWith("\"")) {
            fixedName  = name.substring(1, name.length() - 1);
        } else {
            fixedName = name;
        }

        if (fixedName.startsWith("StorageFile:")) {
            try {
                StorageFileManager storageFileManager = storageService.getStorageFileManager(AccessMethodType.FILE);
                // Get file from Storage
                //StorageService.get(name, isDir ? path + name.substring(12) : path);
                StorageFile storageFile = StorageFile.toStorageFile(fixedName);
                storageFileManager.getFile(storageFile, path, true);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to place storage file: " + fixedName + " to: " + path, ex);
            }
        } else if (fixedName.startsWith("EmbeddedFile:")) {
            // Get file from workflow
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            RepositoryService repositoryService = processEngine.getRepositoryService();

            String deploymentId = processDefinition.getDeploymentId();
            InputStream is = repositoryService.getResourceAsStream(deploymentId, fixedName.substring(13));
            BufferedInputStream bis = new BufferedInputStream(is);
            try {
                Helper.streamToFile(bis, isDir ? path + fixedName.substring(13) : path);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("Unable to place embedded file: " + fixedName + " to: " + path, ex);
            } catch (IOException ex) {
                throw new RuntimeException("Unable to place embedded file: " + fixedName + " to: " + path, ex);
            }
        } else if (fixedName.startsWith("ToFile:")) {
            // write the query result into a file
            String query = fixedName.substring(7);
            if (query.startsWith("${") && query.endsWith("}")) {
                String sTerm = query.substring(2, query.length() - 1);
                String evaledTerm = cqlService.eval(sTerm, context);
                TermName termName = new TermName(evaledTerm);
                UUID uuid = termName.getUuid();
                String q = termName.getQueryString();
                Map subQuery = Helper.deserialize(q, Map.class);
                if (subQuery == null) {
                    subQuery = new HashMap();
                }
                subQuery.put(MetaField.Current + MetaField.JobId, context.get(MetaField.JobId));
                Map<String, Object> aggregators = new HashMap<>();
                aggregators.put(DocumentService.AGGREGATOR_MATCH, subQuery);
                datasetService.find(uuid, aggregators, new File(path));
            } else {
                throw new RuntimeException("wrong ToFile query: " + fixedName);
            }
        } else {
            // This could happen when a query returns nothing.
            // silently ignor it. The service task should handle the error cheching
        }
    }

    public void collectFiles(String filesToCollect, String dirPath, Job job, String activityId, Map<String, Object> context) {
        String[] namedPaths = filesToCollect.split(FileGroupSeparater);
        for (String namedPath : namedPaths) {
            int index = namedPath.indexOf(FileSrcDstSeparater);
            if (index != -1) {
                String name = namedPath.substring(0, index);
                String path = namedPath.substring(index + 1).startsWith("/") ? namedPath.substring(index + 2) : namedPath.substring(index + 1);

                // break the path into dir and file
                int lastSlash = path.lastIndexOf("/");
                String dir;
                String file;
                if (lastSlash != -1) {
                    dir = path.substring(0, lastSlash);
                    if (path.endsWith("/")) {
                        file = "*";
                    } else {
                        file = path.substring(lastSlash + 1);
                    }
                } else {
                    dir = "";
                    file = path;
                }
                File base = new File((dirPath.endsWith("/") ? dirPath : dirPath + "/") + dir);
                FileFilter fileFilter = new WildcardFileFilter(file);
                File[] files = base.listFiles(fileFilter);
                if (files == null) {
                    throw new RuntimeException("Unable to collect file(s): " + file);
                }

                List<StorageFile> storageFiles = new ArrayList<>();
                for (File f : files) {
                    List<StorageFile> sfs;
                    try {
                        StorageFileManager storageFileManager = storageService.getStorageFileManager(AccessMethodType.FILE);
                        sfs = storageFileManager.putFile(AccessMethodType.FILE + ":" + f.getAbsolutePath(), null, true);
                    } catch (Exception ex) {
                        throw new RuntimeException("Unable to collect file(s): " + f.getAbsolutePath(), ex);
                    }
                    storageFiles.addAll(sfs);
                }

                TermName termName = new TermName(name);
                UUID uuid = termName.getUuid();
                UUID version = termName.getVersion();
                if (version == null) {
                    // use the most recent version of the template
                    Term template = termService.getTerm(uuid, null, false);
                    version = UUID.fromString(template.getVersion());
                }
                boolean isTemplate = StringUtils.isEmpty(termName.getAlias());
                if (isTemplate) {
                    int contextId = datasetService.makeContextId(null, null, job.getId(), activityId, name, null);
                    context.put(MetaField.ContextId, contextId);
                    for (File f : files) {
                        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
                            String json;
                            while ((json = fileReader.readLine()) != null) {
                                json = json.trim();
                                if (json.startsWith("[")) {
                                    json = json.substring(1);
                                }
                                if (json.endsWith("]")) {
                                    json = json.substring(0, json.length() - 1);
                                }
                                if (json.startsWith(",")) {
                                    json = json.substring(1);
                                }
                                if (json.endsWith(",")) {
                                    json = json.substring(0, json.length() - 1);
                                }
                                if (StringUtils.isEmpty(json)) {
                                    continue;
                                }
                                Map<String, Object> dataset = (Map<String, Object>) DatasetUtils.deserialize(json);
                                dataset.putAll(context);
                                datasetService.putValue(uuid, version, dataset);
                            }
                        } catch (IOException ex) {
                            //Logger.getLogger(WorkflowHelper.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else {
                    for (StorageFile storageFile : storageFiles) {
                        int contextId = datasetService.makeContextId(null, null, job.getId(), activityId, name, null);
                        context.put(MetaField.ContextId, contextId);

                        Map<String, Object> dataset = new HashMap<>();
                        dataset.put(termName.getAlias(), "StorageFile:" + storageFile.getId());
                        dataset.putAll(context);
                        datasetService.putValue(uuid, version, dataset);
                    }
                }
            }
        }
    }

    public void updateJobTemplateUuids(String uuid, String processInstanceId) {
        Set<String> templateUuids = (Set<String>) runtimeService.getVariable(processInstanceId, JobTemplateUuids);
        if (templateUuids == null) {
            templateUuids = new HashSet<>();
        }
        if (!templateUuids.contains(uuid)) {
            templateUuids.add(uuid);
            runtimeService.setVariable(processInstanceId, JobTemplateUuids, templateUuids);
        }
    }

    public void updateJobTemplateUuids(String uuid, DelegateExecution de) {
        Set<String> templateUuids = (Set<String>) de.getVariable(JobTemplateUuids);
        if (templateUuids == null) {
            templateUuids = new HashSet<>();
        }
        if (!templateUuids.contains(uuid)) {
            templateUuids.add(uuid);
            de.setVariable(JobTemplateUuids, templateUuids);
        }
    }

    public Map<String, Object> execute(List<String> commands, List<String[]> envps, File workingDir, String stdin) {
        Map<String, Object> result = null;
        for (int i = 0; i < commands.size(); i++) {
            String command = commands.get(i);
            String[] envp;
            if (i < envps.size()) {
                envp = envps.get(i);
            } else {
                envp = null;
            }

            result = execute(command, envp, workingDir, stdin);

            String isValid = (String) result.get(IsValid);
            if (isValid != null && isValid.equals("true")) {
                stdin = (String) result.get("stdout");
            } else {
                break;
            }
        }

        return result;
    }

    public Map<String, Object> execute(String command, String[] envp, File workingDir, String stdin) {
        Map<String, Object> result = new HashMap<>();
        if (command != null && !command.isEmpty()) {
            try {
                CommandLine commandLine = CommandLine.parse(command);
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec(commandLine.toStrings(), envp, workingDir);
                try (final OutputStream os = process.getOutputStream()) {
                    if (stdin != null && !stdin.isEmpty()) {
                        os.write(stdin.getBytes());
                    }
                }
                BufferedInputStream bis = new BufferedInputStream(process.getInputStream());
                String stdout = IOUtils.toString(bis);
                result.put("stdout", stdout);
                bis.close();
                bis = new BufferedInputStream(process.getErrorStream());
                String stderr = IOUtils.toString(bis);
                result.put("stderr", stderr);
                bis.close();
                int exit = process.waitFor();
                result.put("exitCode", exit);
                if (exit != 0) {
                    result.put(IsValid, "false");
                    result.put(ErrorMessage, "command exited abnormally: " + command);
                } else {
                    result.put(IsValid, "true");
                }
            } catch (IOException ex) {
                result.put(IsValid, "false");
                String message = "failed to execute commandLine: \"" + command + "\". " + ex.getMessage();
                result.put(ErrorMessage, message);
            } catch (InterruptedException ex) {
                result.put(IsValid, "false");
                result.put(ErrorMessage, "command execution is interrupted: " + command + ". " + ex.getMessage());
            }
        } else {
            result.put(IsValid, "false");
            result.put(ErrorMessage, "the command line is empty");
        }
        return result;
    }

    public Map<String, Object> validateAndSaveData(Map<String, Object> data, String processInstanceId, Map<String, Object> context) {
        return validateAndSaveData(data, null, processInstanceId, context);
    }

    public Map<String, Object> validateAndSaveData(Map<String, Object> data, DelegateExecution delegateExecution, Map<String, Object> context) {
        return validateAndSaveData(data, delegateExecution, null, context);
    }

    private Map<String, Object> validateAndSaveData(Map<String, Object> data, DelegateExecution delegateExecution, String processInstanceId, Map<String, Object> context) {
        validateData(data);

        Map<String, Object>  savedData;
        if (delegateExecution != null) {
            savedData = saveData(data, delegateExecution, context);
        } else {
            savedData = saveData(data, processInstanceId, context);
        }

        return savedData;
    }

    public boolean validateData(Map<String, Object> data) {
        Map<String, Object> errorObjects = new HashMap<>();
        boolean isValid;
        Boolean sIsValid = (Boolean) data.get(IsValid);
        if (sIsValid != null && !sIsValid) {
            isValid = false;
        } else {
            isValid = datasetService.validateValue(data, errorObjects);
            data.put(IsValid, isValid);
            data.put(DatasetErrorObject, errorObjects);
        }
        return isValid;
    }

    public Map<String, Object> saveData(Map<String, Object> data, String processInstanceId, Map<String, Object> context) {
        return saveData(data, null, processInstanceId, context);
    }

    public Map<String, Object> saveData(Map<String, Object> data, DelegateExecution delegateExecution, Map<String, Object> context) {
        return saveData(data, delegateExecution, null, context);
    }

    private Map<String, Object> saveData(Map<String, Object> data, DelegateExecution delegateExecution, String processInstanceId, Map<String, Object> context) {
        Integer projectId = (Integer) context.get(MetaField.ProjectId);
        Integer experimentId = (Integer) context.get(MetaField.ExperimentId);
        Integer jobId = (Integer) context.get(MetaField.JobId);
        String activityId = (String) context.get(MetaField.TaskId);

        Boolean sIsValid = (Boolean) data.get(IsValid);
        boolean isValid = sIsValid == null || sIsValid;

        Map<String, Object>  savedData = new HashMap<>();
        for (String name : data.keySet()) {
            Object value = data.get(name);
            Object savedValue;
            TermName termName = new TermName(name);
            UUID uuid = termName.getUuid();
            UUID version = termName.getVersion();
            if (uuid != null) {
                if (isValid) {
                    // context id
                    int contextId = datasetService.makeContextId(projectId, experimentId, jobId, activityId, name, null);
                    context.put(MetaField.ContextId, contextId);

                    // datasets
                    if (value instanceof Map) {
                        UUID versionFromDataset = (UUID) ((Map) value).get(MetaField.TemplateVersion);
                        if (version == null && versionFromDataset == null) {
                            // use the most recent version of the template
                            Term template = termService.getTerm(uuid, null, false);
                            version = UUID.fromString(template.getVersion());
                        }
                        ((Map) value).putAll(context);
                        savedValue = datasetService.putValue(uuid, version, (Map) value);
                    } else if (value instanceof List && !((List) value).isEmpty()) {
                        UUID versionFromDataset = (UUID) ((Map) ((List) value).get(0)).get(MetaField.TemplateVersion);
                        if (version == null && versionFromDataset == null) {
                            // use the most recent version of the template
                            Term template = termService.getTerm(uuid, null, false);
                            version = UUID.fromString(template.getVersion());
                        }
                        savedValue = datasetService.putValues(uuid, version, (List) value, context);
                    } else {
                        throw new RuntimeException("Dataset value malformed: " + value.toString());
                    }
                    savedData.put(name, savedValue);
                } else {
                    savedData.put(name, value);
                }
            } else {
                // local variables
                Object fixedValue;
                if (value instanceof String && ((String) value).length() > 4000) {
                    fixedValue = ((String) value).substring(0, 3900) + "... [THIS LOCAL VARIABLE HAS BEEN TRUNCATED BECAUSE IT EXCEEDED THE 4000 LIMIT]";
                } else {
                    fixedValue = value;
                }

                if (delegateExecution != null) {
                    delegateExecution.setVariable(name, fixedValue);
                } else if (processInstanceId != null && !processInstanceId.isEmpty()) {
                    runtimeService.setVariable(processInstanceId, name, fixedValue);
                }
            }
        }

        return savedData;
    }

}
