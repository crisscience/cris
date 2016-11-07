/**
 * Created with JetBrains WebStorm.
 * User: intuinno
 * Date: 7/5/13
 * Time: 9:06 PM
 * To change this template use File | Settings | File Templates.
 */
function WorkFlow(id, name, key, uuid, documentation, tasks, flows, startTaskId, endTaskIds, uniqueIdCount, initialDatasetState, finalDatasetStates, theEndFile) {
    this.id = id;
    this.uuid = uuid;

    this.key = key;
    this.name = name;
    this.documentation = documentation;

    this.theEndFileInArchive = {};
    this.theEndFileToInclude = [];
    if (theEndFile) {
        this.theEndFileInArchive[theEndFile] = theEndFile;
        this.theEndFileToInclude = [theEndFile];
    }

    this.tasks = {};
    var taskCount = 0;
    for (var taskId in tasks) {
        var t = tasks[taskId];
        var task;
        if (t.taskType === "User Task") {
            task = new UserTask(t.taskType, t.name, t.documentation, t.ui_page, t.jsonIn, t.users, t.groups, t.datasetState, t.files, t.orientation, t.top, t.left);
        } else if (t.taskType === "System Task") {
            task = new ServiceTask(t.taskType, t.name, t.documentation, t.syncTask, t.filesToPlace, t.jsonIn, t.prefilter, t.commandline, t.postfilter, t.clearWorkingDirectory, t.jsonOut, t.filesToCollect, t.datasetState, t.files, t.orientation, t.top, t.left);
        } else if (t.taskType === "Report Task") {
            task = new ReportTask(t.taskType, t.name, t.documentation, t.syncTask, t.command, t.reportId, t.templateId, t.parameters, t.outputType, t.datasetState, t.files, t.orientation, t.top, t.left);
        } else {
            task = new ExclusiveGateway(t.taskType, t.name, t.documentation, t.conditionExpression, t.targetRef, t.orientation, t.top, t.left);
        }
        task.id = taskId;
        this.tasks[taskId] = task;
        taskCount++;
    }

    this.flows = {};
    for (var flowKey in flows) {
        var flow = flows[flowKey];
        var flowId;
        if (flow.flowType !== "exclusiveGateway") {
            flowId = flow.sourceRef + "_" + flow.targetRef;
        } else {
            flowId = flowKey;
        }
        flow.id = flowId;
        this.flows[flowId] = flow;
    }

    this.startTaskId = startTaskId;
    if (startTaskId) {
        var flowId = "start_" + startTaskId;
        var flow = {};
        flow.flowType = "sequenceFlow";
        flow.sourceRef = "start";
        flow.targetRef = startTaskId;
        this.flows[flowId] = flow;
    }

    if (endTaskIds) {
        this.endTaskIds = endTaskIds;
    } else {
        this.endTaskIds = [];
    }
    for (var i in this.endTaskIds) {
        var endTaskId = this.endTaskIds[i];
        var flowId = endTaskId + "_end";
        var flow = {};
        flow.flowType = "sequenceFlow";
        flow.sourceRef = endTaskId;
        flow.targetRef = "end";
        this.flows[flowId] = flow;
    }

    //TODO: missing from server
    if (initialDatasetState) {
        this.initialDatasetState = dojo.fromJson(initialDatasetState);
    } else {
        this.initialDatasetState = 0;
    }
    //TODO: as dataset_state (String) from server
    if (finalDatasetStates) {
        this.finalDatasetStates = dojo.fromJson(finalDatasetStates);
    } else {
        this.finalDatasetStates = [1];
    }

    //TODO: missing from server
    this.useTheEndPage = false;

    this.uniqueIdCount = taskCount + 1;
}

WorkFlow.prototype = {
    constructor: WorkFlow,

    issueUniqueId: function() {
        this.uniqueIdCount++;
        return this.uniqueIdCount;
    },

    addTask: function(task) {
        var id = "task_" + this.issueUniqueId();
        task.id = id;
        this.tasks[id] = task;
        return task;
    },

    addConditionBranch: function(task) {
        var id = "task_" + this.issueUniqueId();
        task.id = id;
        this.tasks[id] = task;
        return task;
    },

    loadTask: function(task, id) {
        task.id = id;
        this.tasks[id] = task;
        return task;
    },

    loadConditionalBranch: function(task, id) {
        task.id = id;
        this.tasks[id] = task;
        return task;
    },

    removeTask: function(id) {
        delete this.tasks[id];
    },

    removeConditionBranch: function(id) {
        delete this.tasks[id];
    },

    addFlow: function(id, flow) {
        this.flows[id] = flow;
        return id;
    },

    addConditionFlow: function(theBranchID, evalValue, targetID) {
        this.tasks[theBranchID].targetRef[evalValue] = targetID;
    },

    addCaseFlow: function(theBranchID, evalValue, targetID) {
        this.tasks[theBranchID].targetRef[evalValue] = targetID;
    },

    removeFlow: function(id) {
        delete this.flows[id];
    },

    removeStartTask: function() {
        delete this.startTaskId;
    },

    removeEndTask: function(sourceId) {
        for (var index in this.endTaskIds) {
            if (this.endTaskIds[index] === sourceId) {
                this.endTaskIds.splice(index, 1);
                break;
            }
        }
    },

    removeConditionalFlow: function(branchID, targetId) {
        var targetRef = this.tasks[branchID].targetRef;
        for (var key in targetRef) {
            if (targetRef[key] === targetId) {
                delete this.tasks[branchID].targetRef[key];
                break;
            }
        }
    }
};

function Task(taskType, name, documentation, orientation, top, left) {
    this.taskType = taskType;

    this.name = name;
    this.documentation = documentation;

    this.datasetState = 0;

    this.users = [];
    this.groups = [];

    this.orientation = orientation;
    this.top = top;
    this.left = left;
}

Task.prototype = {
    constructor: Task,

    updateXY: function(top, left) {
        this.top = top;
        this.left = left;
    }
};

function UserTask(taskType, name, documentation, template, jsonIn, users, groups, datasetState, files, orientation, top, left) {
    this.taskType = taskType;

    this.name = name ? name : "";
    this.documentation = documentation ? documentation : "";

    if (datasetState === undefined || datasetState === null) {
        this.datasetState = 0;
    } else {
        this.datasetState = datasetState;
    }

    this.filesInArchive = {};
    this.filesToInclude = [];
    if (files) {
        for (var index in files) {
            var file = files[index];
            this.filesInArchive[file] = file;
        };
        this.filesToInclude = files;
    }

    if (orientation) {
        this.orientation = orientation;
    } else {
        this.orientation = "normal";
    }

    this.top = top;
    this.left = left;

    if (template) {
        this.ui_page = template;
    } else {
        this.ui_page = "";
    }

    if (jsonIn) {
        this.jsonIn = jsonIn;
    } else {
        this.jsonIn =  "";
    }

    if (users) {
        this.users = users;
    } else {
        this.users = [];
    }
    if (groups) {
        this.groups = groups;
    } else {
        this.groups = [];
    }
}

UserTask.prototype = {
    constructor: UserTask,

    updateXY: function(top, left) {
        this.top = top;
        this.left = left;
    }
};

function ReceiveTask(taskType, name, documentation, orientation, top, left) {
    this.taskType = taskType;

    this.name = name;
    this.documentation = documentation;

    this.datasetState = 0;

    this.users = [];
    this.groups = [];

    this.orientation = orientation;
    this.top = top;
    this.left = left;
}

ReceiveTask.prototype = {
    constructor: ReceiveTask,

    updateXY: function(top, left) {
        this.top = top;
        this.left = left;
    }
};

function ServiceTask(taskType, name, documentation, isSyncTask, filesToPlace, jsonIn, prefilter, commandline, postfilter, clearWorkingDirectory, jsonOut, filesToCollect, datasetState, files, orientation, top, left) {
    this.taskType = taskType;

    this.name = name ? name : "";
    this.documentation = documentation ? documentation : "";

    if (datasetState === undefined || datasetState === null) {
        this.datasetState = 0;
    } else {
        this.datasetState = datasetState;
    }

    this.filesInArchive = {};
    this.filesToInclude = [];
    if (files) {
        for (index in files) {
            var file = files[index];
            this.filesInArchive[file] = file;
        };
        this.filesToInclude = files;
    }

    if (orientation) {
        this.orientation = orientation;
    } else {
        this.orientation = "normal";
    }

    this.top = top;
    this.left = left;

    this.syncTask = (isSyncTask === false ? false : true);

    this.filesToPlace = filesToPlace ? filesToPlace : "";
    this.jsonIn = jsonIn ? jsonIn : "";
    this.prefilter = prefilter ? prefilter : "";
    this.commandline = commandline ? commandline : "";
    this.postfilter = postfilter ? postfilter : "";
    this.clearWorkingDirectory = ((clearWorkingDirectory === false || typeof clearWorkingDirectory === 'undefined' || clearWorkingDirectory === null)  ? false : true);
    this.jsonOut = jsonOut ? jsonOut : "";
    this.filesToCollect = filesToCollect ? filesToCollect : "";
}

ServiceTask.prototype = {
    constructor: ServiceTask,

    updateXY: function(top, left) {
        this.top = top;
        this.left = left;
    }
};

function ReportTask(taskType, name, documentation, isSyncTask, command, reportId, templateId, parameters, outputType, datasetState, files, orientation, top, left) {
    this.taskType = taskType;

    this.name = name ? name : "";
    this.documentation = documentation ? documentation : "";

    if (datasetState === undefined || datasetState === null) {
        this.datasetState = 0;
    } else {
        this.datasetState = datasetState;
    }

    if (orientation) {
        this.orientation = orientation;
    } else {
        this.orientation = "normal";
    }

    this.top = top;
    this.left = left;

    this.syncTask = (isSyncTask === false ? false : true);

    this.command = command ? command : "";
    this.reportId = reportId ? reportId : "";
    this.templateId = templateId ? templateId : "";
    this.parameters = parameters ? parameters : "";
    this.outputType = outputType ? outputType : "pdf";
}

ReportTask.prototype = {
    constructor: ReportTask,

    updateXY: function(top, left) {
        this.top = top;
        this.left = left;
    }
};

function ExclusiveGateway(type, name, documentation, conditionExpression, targetRef, orientation, top, left) {
    this.taskType = type,

    this.name = name;
    this.documentation = documentation;
    if (!conditionExpression) {
        this.conditionExpression = "isValid == true";
    } else {
        this.conditionExpression = conditionExpression;
    }

    if (targetRef) {
        this.targetRef = targetRef;
    } else {
        this.targetRef = {};
    }

    this.orientation = orientation,
    this.top = top;
    this.left = left;
}

ExclusiveGateway.prototype = {
    constructor: ExclusiveGateway,

    updateXY: function(top, left) {
        this.top = top;
        this.left = left;
    }
};

function Flow(flowType, source, target) {
    this.flowType = flowType;
    this.sourceRef = source;
    this.targetRef = target;
}

Flow.prototype = {
    constructor: Flow
};

function CaseBranch(caseExpression, orientation, top, left) {
    this.flowType = "conditionalFlow",
    this.conditionExpression = caseExpression,
    this.targetRef = {},

    this.orientation = orientation,
    this.top = top;
    this.left = left;
}

CaseBranch.prototype = {
    constructor: CaseBranch,

    updateXY: function(top, left) {
        this.top = top;
        this.left = left;
    }
};
