/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

angular.module("crisWorkflow", ['angular-dojo', 'dataset']);

angular.module("crisWorkflow").directive("crisWorkflowMetadata", function ($compile) {
    return {
        restrict: "E",
        replace: true,
        template: "",
        scope: {
            type: "@",
            isNew: "@",
            emptyMessage: "@",
            ngModel: "="
        },
        templateUrl: cris.baseUrl + "vocabularys/partials/view_term",
        link: function (scope, element, attrs) {
            console.log("==== crisWorkflowMetadata link: begin ====");

            scope.$watch('ngModel', function (value) {
                console.log("==== crisWorkflowMetadata link: $watch ====");
                console.dir(value);
            });

            console.log("==== crisWorkflowMetadata link: end ====");
        },
        controller: function ($scope) {
            console.log("==== crisWorkflowMetadata controller: begin ====");
            console.log("==== crisWorkflowMetadata controller: end ====");
        }
    };
});

angular.module("crisWorkflow").directive("crisWorkflowTask", function ($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: false,
        scope: {
            isNew: "@",
            ngModel: "="
        },
        templateUrl: cris.baseUrl + "vocabularys/partials/view_term_validator",
        link: function (scope, element, attrs) {
            console.log("==== validator link: begin ====");

            /****************
             * model -> view
             ****************/
            scope.$watch('ngModel.type', function (value) {
                console.log("==== validator $watch ====");
                if (scope.ngModel) {
                    if (!scope.ngModel.property) {
                        scope.ngModel.property = [];
                    }
                    var store = new dojo.store.Memory({
                        data: scope.ngModel.property
                    });
                    scope.type = scope.ngModel.type;
                    if (scope.ngModel.type === "boolean") {
                        scope.viewType = "view_term_validator_boolean";
                    } else if (scope.ngModel.type === "numeric") {
                        scope.viewType = "view_term_validator_numeric";
                        var props = store.query({name: "range"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "range", value: null});
                        }
                    } else if (scope.ngModel.type === "text") {
                        scope.viewType = "view_term_validator_text";
                        var props = store.query({name: "type"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "type", value: null});
                        }
                    } else if (scope.ngModel.type === "date" || scope.ngModel.type === "time" || scope.ngModel.type === "date-time") {
                        scope.viewType = "view_term_validator_date";
                        scope.type = "date";
                        var props = store.query({name: "format"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "format", value: null});
                        }
                    } else if (scope.ngModel.type === "list") {
                        scope.viewType = "view_term_validator_list";
                        var props = store.query({name: "isMultiSelect"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "isMultiSelect", value: null});
                        }
                    } else if (scope.ngModel.type === "file") {
                        scope.viewType = "view_term_validator_file";
                        var props = store.query({name: "multiple"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "multiple", value: null});
                        }
                    } else if (scope.ngModel.type === "advanced") {
                        scope.viewType = "view_term_validator_advanced";
                        var props = store.query({name: "regexp"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "regexp", value: null});
                        }
                    } else {
                        //TODO: what to do
                        scope.viewType = "view_term_validator_text";
                    }
                } else {

                }
            });
            console.log("==== validator link: end ====");
        },
        controller: function ($scope) {
            console.log("==== validator controller: begin ====");
            console.log("==== validator controller: end ====");
        }
    };
});

angular.module("crisWorkflow").controller("WorkflowEditorController", function ($scope, $rootScope, workflowBuilderService) {
    console.log("++++++++ editor init called");

    this.errorMessage = null;
    this.isDirty = false;
    this.newTaskPosition = {top: 100, left: 100};

    this.workflow = null;
    this.storeWorkflows = createJsonRestStore(cris.baseUrl + "workflows");

    this.storeTemplates = createJsonRestStore(cris.baseUrl + "templates");

    this.uploaders = {};
    this.uploaders.tasks = {};

    this.currentEditUT = {};
    this.currentEditST = {};
    this.currentEditRT = {};
    this.currentEditCF = {};

    var _this = this;

    $scope.$watch(function(scope) { return _this.errorMessage; }, function(value) {
        console.log("errorMessage changed: " + value);
        if ($scope.timer) {
            clearTimeout($scope.timer);
        }

        if (!value) {
            return;
        }

        $scope.timer = setTimeout(function() {
            console.log("errorMessage cleared: ");
            _this.errorMessage = "";
            $scope.$apply();
        }, 4000);
    });

    this.newWorkflow = function () {
        console.log("******** new workflow");
        var yes;
        if (this.isDirty) {
            yes = showConfirm("Any changes to the current workflow will be lost. Continue?");
        } else {
            yes = true;
        }

        if (yes) {
            this.errorMessage = "";
            var workflow = {id: 0, name: "", key: "", uuid: "", documentation: "", tasks: {}, flows: {}, startTaskId: null, endTaskIds: [], UUID: 0, initialDatasetState: "0", finalDatasetStates: "[1]"};
            this.initWorkflow(workflow);
        }
    };

    this.openWorkflow = function () {
        console.log("******** open workflow");
        var yes;
        if (this.isDirty) {
            yes = showConfirm("Any changes to the current workflow will be lost. Continue?");
        } else {
            yes = true;
        }

        if (yes) {
            openWorkflowDialog.show();
        }
    };

    this.importWorkflow = function () {
        console.log("******** import workflow");
        var yes;
        if (this.isDirty) {
            yes = showConfirm("Any changes to the current workflow will be lost. Continue?");
        } else {
            yes = true;
        }

        if (yes) {
            importWorkflowDialog.show();
        }
    };

    this.fetchAndInitWorkflow = function(id) {
        openWorkflowDialog.hide();
        require(["dojo/request/xhr"], function(xhr){
            xhr(cris.baseUrl + "workflows/load/" + id, {
                handleAs: "json",
                preventCache: true
            }).then(function (data) {
                if (data.error) {
                    _this.errorMessage = "Error: " + data.error;
                    _this.isDirty = false;
                } else {
                    console.log("Data received");
                    console.log(data);
                    var workflow = JSON.parse(data.workflow);
                    _this.initWorkflow(workflow);
                }
                $scope.$apply();
            }, function (error) {
                if (error.response && error.response.text) {
                    _this.errorMessage = "Unable to load workflow: " + error.response.text;
                } else {
                    _this.errorMessage = "Unable to load workflow: " + error;
                }
                $scope.$apply();
            });
        });
    };

    this.initWorkflow = function (workflow) {
        console.log("-------- init workflow");

        // clear the drawing area
        jsPlumb.deleteEveryEndpoint();
        angular.forEach(dijit.findWidgets(dojo.byId('container')), function(w) {
            w.destroyRecursive();
        });
        dojo.empty("container");

        // ... and uploaders
        if (_this.uploaders.theEndPage) {
            angular.forEach(dijit.findWidgets(_this.uploaders.theEndPage), function(w) {
                w.destroyRecursive();
            });
            dojo.destroy(_this.uploaders.theEndPage);
            _this.uploaders.theEndPage = null;
        }
        if (_this.uploaders.tasks) {
            angular.forEach(_this.uploaders.tasks, function(task) {
                angular.forEach(dijit.findWidgets(task), function(w) {
                    w.destroyRecursive();
                });
                dojo.destroy(task);
            });
            _this.uploaders.tasks = {};
        }

        _this.workflow = new WorkFlow(workflow.id, workflow.name, workflow.key, workflow.uuid, workflow.documentation, workflow.tasks, workflow.flows, workflow.startTaskId, workflow.endTaskIds, 1, workflow.initialDatasetState, workflow.finalDatasetStates, workflow.theEndFile);
        console.dir(_this.workflow);

        // draw start and end nodes
        drawStartAndEndNodes();

        // TheEnd page file
        var uploader = new dojox.form.Uploader({name: "theEndFile", label: "Browse ...", multiple: false, force: "html"});
        uploader.startup();
        var fileList = new dojox.form.uploader.FileList({uploader: uploader});
        fileList.startup();

        var node = dojo.create("div");
        node.appendChild(uploader.domNode);
        node.appendChild(fileList.domNode);
        _this.uploaders.theEndPage = node;

        var key;

        // Process Multi-condition (switch condition) tasks
        var tasksCopy = angular.copy(_this.workflow.tasks);
        for (key in tasksCopy) {
            var task = tasksCopy[key];

            if (task.taskType !== "exclusiveGateway") {
                continue;
            }

            // Find all tasks that share the same location coordinates with the current task
            var taskIds = [];
            for (var _key in tasksCopy) {
                var _task = tasksCopy[_key];
                if (_task.taskType === "exclusiveGateway" && _task.top === task.top && _task.left === task.left) {
                    taskIds.push(_task.id);
                    delete tasksCopy[_key];
                }
            }

            // Identify the parent multi-condition task from the list of tasks with the same location coordinates
            var multiConditionTask = null;
            for (var _key in taskIds) {
                var _task = _this.workflow.tasks[taskIds[_key]];
                // A multi-condition exclusive-gateway targets the same sub-task for both TRUE and FALSE conditions
                if (_task.targetRef['False'] && _task.targetRef['False'] === _task.targetRef['True'] && taskIds.indexOf(_task.targetRef['False']) !== -1) {
                    multiConditionTask = _task;
                    taskIds.splice(_key, 1); // taskIds should now only contain sub-tasks of the multi-condition
                    break;
                }
            }

            if (multiConditionTask && taskIds.length > 0) { // At least one sub-condition task exists
                multiConditionTask.isMultiCondition = true;
                multiConditionTask.subConditionTaskIds = [];
                multiConditionTask.conditionExpressions = [];
                multiConditionTask.expressionCount = taskIds.length;
                multiConditionTask.numExpressions = new Array(taskIds.length);

                // Process sub-conditions in multi-task in the order they were created
                var t = _this.workflow.tasks[multiConditionTask.targetRef['False']];
                while (t && taskIds.indexOf(t.id) !== -1) {
                    t.isSubCondition = true;
                    multiConditionTask.subConditionTaskIds.push(t.id);
                    multiConditionTask.conditionExpressions.push({
                        expression: t.conditionExpression,
                        label: t.name
                    });
                    t = _this.workflow.tasks[t.targetRef['False']];
                }
            }
        }

        // draw tasks
        for (key in _this.workflow.tasks) {
            var task = _this.workflow.tasks[key];
            _this.initTask(task);
        }

        // draw flows
        for (key in _this.workflow.flows) {
            var flow = _this.workflow.flows[key];
            drawFlow(flow);
        }

        // draw conditional flow
        for (key in _this.workflow.tasks) {
            var task = _this.workflow.tasks[key];
            if (task.taskType === "exclusiveGateway") {
                drawFlow(task);
            }
        }

        this.isDirty = true;
    };

    this.saveAsWorkflow = function () {
        console.log("======== save as workflow");
        saveAsWorkflowDialog.show();
    };

    this.saveWorkflow = function () {
        console.log("======== save workflow");
        fixUploadFiles();
        updateWorkflow(_this.workflow);
        console.dir(_this.workflow);

        require(["dojo/request/iframe"], function(iframe){
            iframe(cris.baseUrl + "workflows/save", {
                method: "POST",
                handleAs: "json",
                form: "idFormEditor",
                data: {
                    workflow: dojox.json.ref.toJson(_this.workflow)
                }
            }).then(function(data) {
                if (data.error) {
                    _this.isDirty = true;
                    _this.errorMessage = "Error: " + data.error;
                } else {
                    _this.errorMessage = "Workflow: " + data.name + ": saved successfully";
                    console.log("Data received");
                    console.log(data);
                    _this.fetchAndInitWorkflow(data.id);
                    _this.isDirty = false;
                }
                $scope.$apply();
            }, function(error) {
                console.log("Failed to save changes: " + error);
                _this.isDirty = true;
                _this.errorMessage = "Failed to save changes: " + error;
                $scope.$apply();
            });
        });
    };

    this.saveWorkflowArchive = function () {
        importWorkflowDialog.hide();

        // send item + file to the backend using PUT/POST
        require(["dojo/request/iframe"], function(iframe){
            iframe(cris.baseUrl + "workflows/import", {
                method: "POST",
                handleAs: "json",
                form: "idFormImportWorkflow"
            }).then(function(data) {
                if (data.error) {
                    _this.errorMessage = "Error: " + data.error;
                } else {
                    console.log("Data received");
                    console.log(data);
                    _this.fetchAndInitWorkflow(data.id);
                    _this.isDirty = false;
                }
                $scope.$apply();
            }, function(error) {
                _this.errorMessage = "Error: " + error;
                $scope.$apply();
            });
        });
    };

    this.renameAndSaveWorkflow = function (name, description) {
        console.log("-------- renameAndSaveWorkflow");
        console.log("name: " + name);
        console.log("description: " + description);
        console.dir(_this.workflow);

        saveAsWorkflowDialog.hide();

        _this.workflow.id = null;
        _this.workflow.uuid = null;
        _this.workflow.key = null;
        _this.workflow.name = name;
        _this.workflow.description = description;
        _this.saveWorkflow();
    };

    this.editWorkflowMetadata = function () {
        console.log("******** entering editWorkflowMetadata...");

        var node = dojo.byId("idTheEndPageFile");
        dojo.empty(node);
        node.appendChild(_this.uploaders.theEndPage);

        workflowMetadataDialog.show();
    };

    this.newUserTask = function () {
        console.log("******** new user task");

        cris.workflow.app.topIncrement = cris.workflow.app.topIncrement + 15;

        var task = new UserTask("User Task", "Please provide a name", "Please provide documentation", "", "", [], [], "0", [], "normal", 70 + cris.workflow.app.topIncrement, 70 + cris.workflow.app.topIncrement);
        _this.workflow.addTask(task);
        this.initTask(task);

    };

    this.newServiceTask = function () {
        console.log("******** new service task");

        cris.workflow.app.topIncrement = cris.workflow.app.topIncrement + 15;

        var task = new ServiceTask("System Task", "Please provide a name", "Please provide documentation", true, "", "", "", "", "", "", "", "0", [], "normal", 70 + cris.workflow.app.topIncrement, 70 + cris.workflow.app.topIncrement);
        _this.workflow.addTask(task);
        this.initTask(task);

    };

    this.newReportTask = function () {
        console.log("******** new report task");

        cris.workflow.app.topIncrement = cris.workflow.app.topIncrement + 15;

        var task = new ReportTask("Report Task", "Please provide a name", "Please provide documentation", true, "", "", "", "", "pdf", "0", [], "normal", 70 + cris.workflow.app.topIncrement, 70 + cris.workflow.app.topIncrement);
        _this.workflow.addTask(task);
        this.initTask(task);
    };

    this.newConditionBranch = function () {
        console.log("******** new condition branch");

        cris.workflow.app.topIncrement = cris.workflow.app.topIncrement + 15;

        var task = new ExclusiveGateway("exclusiveGateway", "Please provide a name", "Please provide documentation", "", {}, "normal", 70 + cris.workflow.app.topIncrement, 70 + cris.workflow.app.topIncrement);
        _this.workflow.addConditionBranch(task);
        drawTask(task.id, task);

    };

    this.newMultiConditionBranch = function () {
        console.log("******** new switch branch");

        cris.workflow.app.topIncrement = cris.workflow.app.topIncrement + 15;

        var task = new ExclusiveGateway("exclusiveGateway", "Please provide a name", "", "1==1", {}, "normal", 70 + cris.workflow.app.topIncrement, 70 + cris.workflow.app.topIncrement);
        task.conditionExpressions = [];
        task.subConditionTaskIds = [];
        task.isMultiCondition = true;
        _this.workflow.addConditionBranch(task);
        drawTask(task.id, task);
    };

    this.editUserTask = function (task) {
        console.log("******** edit user task...");

        var node = dojo.byId("idUserTaskCustomHtmlFile");
        dojo.empty(node);
        node.appendChild(_this.uploaders.tasks[key]);

        userTaskDialog.set("title", "New User Task");
        userTaskDialog.show();
    };

    this.editServiceTask = function (task) {
        console.log("******** edit service task...");

        var node = dojo.byId("idServiceTaskFiles");
        dojo.empty(node);
        node.appendChild(_this.uploaders.tasks[key]);

        serviceTaskDialog.set("title", "New System Task");
        serviceTaskDialog.show();
    };

    this.editConditionalBranch = function (task) {
        console.log("******** edit condition branch...");
        conditionBranchDialog.set("title", "New Condition Branch");
        conditionBranchDialog.show();
    };

    this.editCaseBranch = function (task) {
        console.log("******** edit case branch...");
    };

    this.createTask = function() {
        console.log("-------- createTask");
    };

    this.initTask = function (task) {
        if (!task.orientation) {
            // default to "normal"
            task.orientation = "normal";
        }
        var key = task.id;

        if (task.taskType === "User Task") {
            if (!task.ui_page || isUuid(task.ui_page)) {
                task.pageType = "template";
            } else {
                task.pageType = "custom";
            }

            // for custom html file
            var uploader = new dojox.form.Uploader({id: key + "_html", name: key, label: "Browse ...", multiple: false, force: "html"});
            uploader.startup();
            var fileList = new dojox.form.uploader.FileList({uploader: uploader});
            fileList.startup();

            var node = dojo.create("div");
            node.appendChild(uploader.domNode);
            node.appendChild(fileList.domNode);
            _this.uploaders.tasks[key + "_html"] = node;
        } else if (task.taskType === "exclusiveGateway") {
            if (!task.conditionExpression) {
                task.conditionExpression = "isValid == true";
            }
        }

        if (!task.isSubCondition) { // skip sub-condition tasks. They are drawn when their parent (multi-condition task) is drawn.
            drawTask(key, task);
        }

        // file(s) used by the task
        var uploader = new dojox.form.Uploader({id: key, name: key, label: "Browse ...", multiple: true, force: "html"});
        uploader.startup();
        var fileList = new dojox.form.uploader.FileList({uploader: uploader});
        fileList.startup();

        var node = dojo.create("div");
        node.appendChild(uploader.domNode);
        node.appendChild(fileList.domNode);
        _this.uploaders.tasks[key] = node;
    };

    this.deleteTask = function (task) {
        console.log("-------- deleteTask");
        console.dir(task);

        var taskType;
        if (task.taskType) {
            taskType = task.taskType;
        } else {
            taskType = task.flowType;
        }

        var domNode = dojo.byId(task.id);
        jsPlumb.detachAllConnections(domNode);
        jsPlumb.removeAllEndpoints(domNode);
        angular.forEach(dijit.findWidgets(domNode), function(w) {
            w.destroyRecursive();
        });
        dojo.destroy(domNode);

        if (taskType !== "exclusiveGateway") {
            // user task and service task
            _this.workflow.removeTask(task.id);

            // Handle Start node
            if (_this.workflow.startTaskId === task.id) {
                _this.workflow.startTaskId = null;
            }

            // Handle End Node
            for (var endTaskId in _this.workflow.endTaskIds) {
                if (_this.workflow.endTaskIds[endTaskId] === task.id) {
                    _this.workflow.endTaskIds.splice(endTaskId, 1);
                }
            }

            // Handle Flows: sequential
            for (flowId in _this.workflow.flows) {
                if (_this.workflow.flows[flowId].sourceRef === task.id || _this.workflow.flows[flowId].targetRef === task.id) {
                    _this.workflow.removeFlow(flowId);
                }
            }
            // Handle Flows: conditional
            for (flowId in _this.workflow.tasks) {
                if (_this.workflow.tasks[flowId].taskType === "exclusiveGateway") {
                    if (_this.workflow.tasks[flowId].targetRef["False"] === task.id) {
                        _this.workflow.removeConditionalFlow(flowId, "False");
                    } else if (_this.workflow.tasks[flowId].targetRef["True"] === task.id) {
                        _this.workflow.removeConditionalFlow(flowId, "True");
                    }
                }
            }
        } else {
            // condition branch
            _this.workflow.removeConditionBranch(task.id);

            // Handle Start node
            if (_this.workflow.startTaskId === task.id) {
                _this.workflow.startTaskId = null;
            }

            // Handle End Node
            for (var endTaskId in _this.workflow.endTaskIds) {
                if (_this.workflow.endTaskIds[endTaskId] === task.id) {
                    _this.workflow.endTaskIds.splice(endTaskId, 1);
                }
            }

            // Handle Flows
            for (flowId in _this.workflow.flows) {
                if (_this.workflow.flows[flowId].sourceRef === task.id || _this.workflow.flows[flowId].targetRef === task.id) {
                    _this.workflow.removeFlow(flowId);
                }
            }
        }

        if (taskType === "User Task") {
            dojo.setStyle(dojo.byId('userTaskDetails'), 'display', 'none');
        } else if (taskType === "System Task") {
            dojo.setStyle(dojo.byId('serviceTaskDetails'), 'display', 'none');
        } else if (taskType === "Report Task") {
            reportTaskDialog.hide();
        } else if (taskType === "exclusiveGateway") {
            if (!task.isMultiCondition) {
                conditionBranchDialog.hide();
            } else {
                multiConditionBranchDialog.hide();
            }
        }
    };

    this.deleteMultiConditionTask= function (task) {
        dojo.forEach(task.subConditionTaskIds, function (taskId) {
            _this.deleteTask(_this.workflow.tasks[taskId]);
        });
        _this.deleteTask(task);
    };

    this.newflow = function (flow) {

    };

    this.updateflow = function (flow) {

    };

    this.deleteflow = function (flow) {

    };

    this.keyDown = function($event) {
        var keyCode = $event.which || $event.keyCode;
        if (keyCode === 27) { // Esc
            dojo.setStyle(dojo.byId('userTaskDetails'), 'display', 'none');
            dojo.setStyle(dojo.byId('serviceTaskDetails'), 'display', 'none');
        }
    };

    this.cancelTaskDetails = function(divId) {
        dojo.setStyle(dojo.byId(divId), 'display', 'none');
    };

    this.closeUserTaskDetails = function (task) {
        console.dir(task);
        var pageType = task.pageType;
        if (pageType === "template") {
            // template page: get from json store
            var templateId = task.templateId;
            if (templateId) {
                _this.storeTemplates.fetchItemByIdentity({identity: +templateId, onItem: function(item) { task.ui_page = item.uuid.$uuid; }, onError: function(error) { _this.errorMessage = error; }});
            }
        } else {
            // custome page: get from upload file name
            var uploader = dijit.byId(task.id + "_html");
            var files = uploader.getFileList();
            if (files.length === 1) {
                task.ui_page = files[0].name;
            }
        }

        task.users = csvToList(task.csvUsers);
        task.groups = csvToList(task.csvGroups);

        // refresh UI
        drawTask(task.id, task);

        dojo.setStyle(dojo.byId('userTaskDetails'), 'display', 'none');
    };

    this.closeServiceTaskDetails = function (task) {
        console.dir(task);

        // refresh UI
        drawTask(task.id, task);

        dojo.setStyle(dojo.byId('serviceTaskDetails'), 'display', 'none');
    };

    this.closeReportTaskDialog = function (task) {
        console.dir(task);

        // refresh UI
        drawTask(task.id, task);

        reportTaskDialog.hide();
    };

    this.closeConditionBranchDialog = function (task) {
        console.dir(task);

        // refresh UI
        drawTask(task.id, task);

        conditionBranchDialog.hide();
    };

    this.closeMultiConditionBranchDialog = function (task) {
        console.dir(task);

        task.conditionExpressions.splice(task.expressionCount);
        var numExpressions = task.conditionExpressions.length;

        for (var i = 0; i < numExpressions; i++) {
            var expression = task.conditionExpressions[i].expression;
            if (!expression) {
                task.conditionExpressions[i].expression = "isValid == true";
            }
            var label = task.conditionExpressions[i].label;
            if (!label) {
                task.conditionExpressions[i].label = 'condition' + i;
            }
        }

        if (task.subConditionTaskIds.length > numExpressions) {
            for (var i = 0; i < task.subConditionTaskIds.length; i++) {
                if (i >= numExpressions) {
                    _this.deleteTask(_this.workflow.tasks[task.subConditionTaskIds[i]]);
                    task.subConditionTaskIds.splice(i--, 1);
                }
            }
        } else if (task.subConditionTaskIds.length < numExpressions) {
            for (var i = 0; i < numExpressions; i++) {
                if (i >= task.subConditionTaskIds.length) {
                    var expression = task.conditionExpressions[i].expression;
                    var label = task.conditionExpressions[i].label;
                    var _task = new ExclusiveGateway("exclusiveGateway", label, "", expression, {}, "normal", 70 + cris.workflow.app.topIncrement, 70 + cris.workflow.app.topIncrement);
                    _task.isSubCondition = true;
                    _this.workflow.addConditionBranch(_task);
                    task.subConditionTaskIds.push(_task.id);
                }
            }
        }

        // refresh UI
        drawTask(task.id, task);

        dojo.query('._jsPlumb_endpoint_anchor_', dojo.byId(task.id)).forEach(function(ele){
            jsPlumb.repaint(ele);
        });

        multiConditionBranchDialog.hide();
    };

    $scope.$watch('weCtrl.currentEditCF.expressionCount', function(value){
        if (value) {
            _this.currentEditCF.numExpressions = new Array(value);
        }
    });

    function isUuid(text) {
      if (text && text.match(/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}/)) {
          return true;
      } else {
          return false;
      }
    };

    function listToCsv(list) {
        var csv = "";
        angular.forEach(list, function(item) {
            if (csv) {
                csv += ",";
            }
            csv += item;
        });
        return csv;
    }

    function csvToList(csv) {
        var list;
        if (csv) {
            list = csv.split(",");
        } else {
            list = [];
        }
        return list;
    }

    function trimName(fullName) {
        var tName = null;
        var isLine2 = false;
        var line1 = "";
        var line2 = "";
        var a = 0;
        var len = fullName.length;
        var tokens = fullName.split(" ");
        if (tokens.length === 1) {
            if (len < 20) {
                tName = fullName;
            } else {
                tName = fullName.substring(0, 16) + " ...";
            }
        } else {
            for (var i = 0; i < tokens.length; i++) {
                if (line1.length + tokens[i].length < 16 && !isLine2) {
                    if (i === 0) {
                        line1 = line1 + tokens[i];
                    } else {
                        line1 = line1 + " " + tokens[i];
                    }
                }
                else {
                    if (line2.length + tokens[i].length < 16) {
                        isLine2 = true;
                        if (a === 0) {
                            line2 = line2 + tokens[i];
                            a++;
                        } else {
                            line2 = line2 + " " + tokens[i];
                        }
                    } else {
                        if (isLine2) {
                            line2 = line2 + " ...";
                        } else {
                            line2 = tokens[i].substring(0, 12) + " ...";
                        }
                        break;
                    }
                }
            }
            tName = line1 + "</br>" + line2;
        }
        return "</br>" + tName;
    }

    function getEndpointOrientation(endpoint) {
        if ((endpoint.anchor.type === "LeftMiddle" && endpoint.isTarget) || (endpoint.anchor.type === "RightMiddle" && endpoint.isSource)) {
            return "normal";
        } else {
            return "flipped";
        }
    }

    function getEndpointOrientationCB(endpoints) {
        var orientation = "normal";
        angular.forEach(endpoints, function(endpoint) {
            if (endpoint.anchor.type === "LeftMiddle") {
                orientation = "normal";
            } else if (endpoint.anchor.type === "RightMiddle") {
                orientation = "flipped";
            }
        });

        return orientation;
    }

    function fixUploadFiles() {
        var files = dojo.byId("idUploaderFiles");
        dojo.empty(files);

        files.appendChild(_this.uploaders.theEndPage);

        for (var key in _this.uploaders.tasks) {
            files.appendChild(_this.uploaders.tasks[key]);
        }
    }

    function updateWorkflow(workflow) {
        console.dir(workflow);

        for (var taskId in workflow.tasks) {
            var _task = workflow.tasks[taskId];

            if (!_task.isSubCondition) {
                var myTop = dojo.style(taskId, "top");
                var myLeft = dojo.style(taskId, "left");
                _task.top = myTop;
                _task.left = myLeft;
            }

            // Location for Multi-condition and its sub-tasks should be the same. Useful when re-loading workflow.
            if (_task.isMultiCondition) {
                for (var _key in _task.subConditionTaskIds) {
                    var id = _task.subConditionTaskIds[_key];
                    workflow.tasks[id].top = _task.top;
                    workflow.tasks[id].left = _task.left;
                }
            }

            if (_task.jsonIn) {
                _task.jsonIn = _task.jsonIn.replace(/\n/g, '');
                _task.jsonIn = workflowBuilderService.removeUnsafeSpaceCharacters(_task.jsonIn);
            }
            if (_task.jsonOut) {
                _task.jsonOut = _task.jsonOut.replace(/\n/g, '');
                _task.jsonOut = workflowBuilderService.removeUnsafeSpaceCharacters(_task.jsonOut);
            }
        }
        for (flowID in workflow.flows) {
            if (workflow.flows[flowID].sourceRef === "start") {
                workflow.startTaskId = workflow.flows[flowID].targetRef;
                workflow.removeFlow(flowID);
            }
        }
        for (flowID in workflow.flows) {
            if (workflow.flows[flowID].targetRef === "end") {
                var isDuplicate = false;
                for (var endID in workflow.endTaskIds) {
                    if (workflow.endTaskIds[endID] === workflow.flows[flowID].sourceRef) {
                        isDuplicate = true;
                    }
                }
                if (!isDuplicate) {
                    workflow.endTaskIds.push(workflow.flows[flowID].sourceRef);
                }
                workflow.removeFlow(flowID);
            }
        }
    }

    function drawStartAndEndNodes() {
        // start
        var node = dojo.create("div", {
            "id": "start",
            "className": "shape",
            "data-shape": "Circle"
        }, "container");
        dojo.style(node, {
            'top': "10px",
            'left': "20px"
        });
        dojo.place(node, dojo.byId("container"));
        jsPlumb.addEndpoint(node, sourceEndpoint, {anchor: "RightMiddle", uuid: "Start"});

        // end event
        node = dojo.create("div", {
            "id": "end",
            "className": "shape",
            "data-shape": "CircleEnd"
        }, "container");
        dojo.style(node, {
            'top': "10px",
            'left': "870px"
        });
        dojo.place(node, dojo.byId("container"));
        jsPlumb.addEndpoint(node, targetEndpoint, {anchor: "LeftMiddle", uuid: "End"});
    }

    function updateOrientation(node, task) {
        // get endpoints
        var endpoints = jsPlumb.getEndpoints(node);

        var oldOrientation = getEndpointOrientation(endpoints[0]);
        var newOrientation = task.orientation;
        if (oldOrientation !== newOrientation) {
            // switch aource and target endpoints
            var sourceEP, targetEP;
            if (endpoints[0].isTarget) {
                sourceEP = endpoints[1];
                targetEP = endpoints[0];
            } else {
                sourceEP = endpoints[0];
                targetEP = endpoints[1];
            }
            if (newOrientation === "normal") {
                sourceEP.setAnchor("RightMiddle");
                targetEP.setAnchor("LeftMiddle");
            } else {
                sourceEP.setAnchor("LeftMiddle");
                targetEP.setAnchor("RightMiddle");
            }
        }
    }

    function updateOrientationCB(node, task) {
        // get endpoints
        var endpoints = jsPlumb.getEndpoints(node);

        var oldOrientation = getEndpointOrientationCB(endpoints);
        var newOrientation = task.orientation;
        if (oldOrientation !== newOrientation) {
            var sourceEP;
            angular.forEach(endpoints, function(endpoint) {
                if (endpoint.isSource && (endpoint.anchor.type === "LeftMiddle" || endpoint.anchor.type === "RightMiddle")) {
                    sourceEP = endpoint;
                }
            });

            if (sourceEP) {
                // reset anchor
                if (newOrientation === "normal") {
                    sourceEP.setAnchor("LeftMiddle");
                } else {
                    sourceEP.setAnchor("RightMiddle");
                }
            }
        }
    }

    function updateOrientationSCB(node, task) {
        // get endpoints
        var endpoints = jsPlumb.getEndpoints(node);

        var oldOrientation = "normal";
        angular.forEach(endpoints, function(endpoint) {
            if (endpoint.anchor.type === "RightMiddle") {
                oldOrientation = "normal";
            } else if (endpoint.anchor.type === "LeftMiddle") {
                oldOrientation = "flipped";
            }
        });

        var newOrientation = task.orientation;
        if (oldOrientation !== newOrientation) {
            var sourceEP;
            angular.forEach(endpoints, function(endpoint) {
                if (endpoint.isSource && (endpoint.anchor.type === "LeftMiddle" || endpoint.anchor.type === "RightMiddle")) {
                    sourceEP = endpoint;
                }
            });

            if (sourceEP) {
                // reset anchor
                if (newOrientation === "normal") {
                    sourceEP.setAnchor("RightMiddle");
                } else {
                    sourceEP.setAnchor("LeftMiddle");
                }
            }
        }
    }

    function drawTask(id, task) {
        var node = dojo.byId(id);
        var type;
        if (task.flowType) {
            type = task.flowType;
        } else {
            type = task.taskType;
        }

        var dataShape;
        switch (type) {
        case "User Task":
            dataShape = "UTask";
            break;
        case "System Task":
            dataShape = "STask";
            break;
        case "Report Task":
            dataShape = "RTask";
            break;
        case "exclusiveGateway":
            dataShape = "CTask";
            if (task.isMultiCondition) {
                dataShape = "MCTask";
            } else if (task.isSubCondition) {
                dataShape = "SCTask";
            }
            break;
        default:
            dataShape = "Unknown";
        }

        if (!node) {
            node = dojo.create("div", {
                "id": id,
                "className": !task.isSubCondition ? "shape" : "",
                "data-shape": dataShape
            }, "container");
            dojo.style(node, {
                'top': task.top + "px",
                'left': task.left + "px"
            });
            dojo.place(node, dojo.byId("container"));

            jsPlumb.draggable(node, {
                containment: 'parent',
                drag: function(e) { // Needed for multi-condition tasks, otherwise endpoint anchors won't move when dragging
                    dojo.query('._jsPlumb_endpoint_anchor_', e.target).forEach(function(ele){
                        jsPlumb.repaint(ele);
                    });
                }
            });

            if (task.isMultiCondition) {
                dojo.setStyle(node, 'border-style', 'solid');
                dojo.setStyle(node, 'border-width', 'thin');
            }
            if (task.isSubCondition) {
                jsPlumb.setDraggable(node, false);
                dojo.setStyle(node, 'border-top-style', 'solid');
                dojo.setStyle(node, 'border-top-width', 'medium');
            }

            // draw end points
            var sourcesPos;
            var targetsPos;

            if (type !== "exclusiveGateway") {
                if (task.orientation === "normal") {
                    sourcesPos = ["RightMiddle"];
                    targetsPos = ["LeftMiddle"];
                } else {
                    sourcesPos = ["LeftMiddle"];
                    targetsPos = ["RightMiddle"];
                }
            } else {
                if (task.isMultiCondition) {
                    targetsPos = ["TopCenter"];
                    sourcesPos = [];
                } else if (task.isSubCondition) {
                    if (task.orientation === "normal") {
                        sourcesPos = ["RightMiddle", "BottomCenter"];
                    } else {
                        sourcesPos = ["LeftMiddle", "BottomCenter"];
                    }
                    targetsPos = [];
                } else {
                    if (task.orientation === "normal") {
                        targetsPos = ["TopCenter"];
                        sourcesPos = ["LeftMiddle", "BottomCenter"];
                    } else {
                        targetsPos = ["TopCenter"];
                        sourcesPos = ["RightMiddle", "BottomCenter"];
                    }
                }
            }

            drawEndPoints(node, sourcesPos, targetsPos, task.taskType);

            if (task.isMultiCondition) { // Draw sub-exclusiveGateways
                drawTask(task.id, task);
            }
        } else {
            if (type === 'exclusiveGateway' && task.isMultiCondition) {
                var conditionExpressions = task.conditionExpressions;
                dojo.forEach(task.subConditionTaskIds, function(taskId, idx) {
                    var task_ = _this.workflow.tasks[taskId];

                    // Update expression and label
                    task_.orientation = task.orientation;
                    task_.conditionExpression = conditionExpressions[idx].expression;
                    task_.name = conditionExpressions[idx].label;
                    drawTask(taskId, task_);

                    dojo.place(dojo.byId(taskId), dojo.byId(id), "last");

                    // False condition connects to next sub-condition task
                    if (task.subConditionTaskIds[idx + 1]) {
                        _this.workflow.addConditionFlow(taskId, "False", task.subConditionTaskIds[idx + 1]);
                    }

                    // Only last sub-task should have FALSE endpoint set to Visible
                    var endPoints = jsPlumb.selectEndpoints({source: dojo.byId(task_.id)});
                    if (idx < task.subConditionTaskIds.length - 1) {
                        if (endPoints.get(1).connections.length) {
                            jsPlumb.detach(endPoints.get(1).connections[0]);
                        }
                        endPoints.get(1).setVisible(false);
                    } else {
                        endPoints.get(1).setVisible(true);
                    }
                });

                // Multi-condition task always connects to first sub-condition task for both TRUE and FALSE conditions
                if (task.subConditionTaskIds.length) {
                    _this.workflow.addConditionFlow(task.id, "True", task.subConditionTaskIds[0]);
                    _this.workflow.addConditionFlow(task.id, "False", task.subConditionTaskIds[0]);
                } else {
                    delete task.targetRef.True;
                    delete task.targetRef.False;
                }
            }

            if (type !== "exclusiveGateway") {
                updateOrientation(node, task);
            } else {
                if (!task.isMultiCondition && !task.isSubCondition) {
                    updateOrientationCB(node, task);
                } else if (task.isSubCondition) {
                    updateOrientationSCB(node, task);
                }
            }
        }

        drawLabel(node, task);
    }

    function drawLabel(node, task) {
        var name = task.name;
        var labelName;
        if (task.taskType && task.taskType === "exclusiveGateway" && !task.isMultiCondition && !task.isSubCondition) {
            labelName = "<br/><br/>" + (name.length < 13 ? name : name.substring(0, 10) + "...");
        } else {
            labelName = "<br/>" + (name.length < 15 ? name : name.substring(0, 12) + "...");
        }

        var labelId = node.id + "_title";
        var label = dijit.byId(labelId);
        if (!label) {
            label = new dijit.form.Button({
                id: labelId,
                label: labelName,
                baseClass: "taskStyle",
                onDblClick: function(evt) {
                    console.log("******** edit task");
                    console.dir(task);

                    var type;
                    if (task.flowType) {
                        type = task.flowType;
                    } else {
                        type = task.taskType;
                    }
                    switch (type) {
                        case "User Task":
                            task.csvUsers = listToCsv(task.users);
                            task.csvGroups = listToCsv(task.groups);
                            if (isUuid(task.ui_page)) {
                                // template
                                require(["dojo/request/xhr"], function(xhr){
                                    xhr(cris.baseUrl + "templates/metadata/" + task.ui_page, {
                                        handleAs: "json",
                                        preventCache: true
                                    }).then(function (data) {
                                        _this.currentEditUT = task;
                                        
                                        if (data.error) {
                                            _this.errorMessage = "Error: " + data.error;
                                        } else {
                                            _this.currentEditUT.templateId = data.id;
                                        }
                                        $scope.$apply();

                                        // custom html file
                                        var node = dojo.byId("idUserTaskCustomHtmlFile");
                                        dojo.empty(node);
                                        node.appendChild(_this.uploaders.tasks[task.id + "_html"]);

                                        // other files
                                        var node = dojo.byId("idUserTaskFiles");
                                        dojo.empty(node);
                                        node.appendChild(_this.uploaders.tasks[task.id]);

                                        dojo.setStyle(dojo.byId('userTaskDetails'), 'display', 'block');
                                        dojo.byId('userTaskLabel').innerHTML = "Edit User Task: " + task.name;
                                        $rootScope.$broadcast('onTaskDetailsOpened', task.id);
                                    }, function (error) {
                                        _this.errorMessage = "Unable to load template: \"" + task.ui_page + "\"";
                                        _this.currentEditUT = task;
                                        $scope.$apply();

                                        // custom html file
                                        var node = dojo.byId("idUserTaskCustomHtmlFile");
                                        dojo.empty(node);
                                        node.appendChild(_this.uploaders.tasks[task.id + "_html"]);

                                        // other files
                                        var node = dojo.byId("idUserTaskFiles");
                                        dojo.empty(node);
                                        node.appendChild(_this.uploaders.tasks[task.id]);

                                        dojo.setStyle(dojo.byId('userTaskDetails'), 'display', 'block');
                                        dojo.byId('userTaskLabel').innerHTML = "Edit User Task: " + task.name;
                                        $rootScope.$broadcast('onTaskDetailsOpened', task.id);
                                    });
                                });
                            } else {
                                // custom HTML
                                _this.currentEditUT = task;
                                $scope.$apply();

                                // custom html file
                                var node = dojo.byId("idUserTaskCustomHtmlFile");
                                dojo.empty(node);
                                node.appendChild(_this.uploaders.tasks[task.id + "_html"]);

                                // other files
                                var node = dojo.byId("idUserTaskFiles");
                                dojo.empty(node);
                                node.appendChild(_this.uploaders.tasks[task.id]);

                                dojo.setStyle(dojo.byId('userTaskDetails'), 'display', 'block');
                                dojo.byId('userTaskLabel').innerHTML = "Edit User Task: " + task.name;
                                $rootScope.$broadcast('onTaskDetailsOpened', task.id);
                            }
                            break;
                        case "System Task":
                            _this.currentEditST = task;
                            $scope.$apply();

                            var node = dojo.byId("idServiceTaskFiles");
                            dojo.empty(node);
                            node.appendChild(_this.uploaders.tasks[task.id]);

                            dojo.setStyle(dojo.byId('serviceTaskDetails'), 'display', 'block');
                            dojo.byId('serviceTaskLabel').innerHTML = "Edit System Task: " + task.name;
                            $rootScope.$broadcast('onTaskDetailsOpened', task.id);
                            break;
                        case "Report Task":
                            _this.currentEditRT = task;
                            $scope.$apply();

                            reportTaskDialog.set("title", "Edit Report Task: " + task.name);
                            reportTaskDialog.show();
                            break;
                        case "exclusiveGateway":
                            _this.currentEditCF = task;
                            $scope.$apply();

                            if (task.isMultiCondition) {
                                multiConditionBranchDialog.set("title", "Edit Switch Branch: " + task.name);
                                multiConditionBranchDialog.show();
                            }else if (task.isSubCondition) {
                                return;
                            } else {
                                conditionBranchDialog.set("title", "Edit Condition Branch: " + task.name);
                                conditionBranchDialog.show();
                            }
                            break;
                        default:
                            console.log("Unknown task/flow type: \"" + type + "\";");
                    }
                }
            });
            dojo.place(label.domNode, node, "first");

            dojo.style(labelId, "fontSize", "8pt");
            dojo.style(labelId, "width", "70px");
            dojo.style(labelId, "height", "30px");
        } else {
            label.set("label", labelName);
        }

        new dijit.Tooltip({
            connectId: [labelId],
            label: name
        });

        return label;
    }

    function drawFlow(flow) {
        var source;
        var target;
        var sourceAnchor;
        var targetAnchor;

        var key = flow.id;
        if (flow.flowType === "exclusiveGateway" || flow.taskType === "exclusiveGateway") {
            // condition flows
            sourceOrientation = flow.orientation;
            for (var targetRef in flow.targetRef) {
                if (targetRef === "False") {
                    if (sourceOrientation === "normal") {
                        source = key + "LeftMiddle";
                    } else {
                        source = key + "RightMiddle";
                    }
                    if (flow.isSubCondition) {
                        source = key + "BottomCenter";
                    }
                } else if (targetRef === "True") {
                    source = key + "BottomCenter";
                    if (flow.isSubCondition) {
                        if (sourceOrientation === "normal") {
                            source = key + "RightMiddle";
                        } else {
                            source = key + "LeftMiddle";
                        }
                    }
                }
                var targetID = flow.targetRef[targetRef];
                if (!targetID) {
                    // there's no target ID when the target is theEnd
                    // so do nothing
                    continue;
                } else if (targetID.indexOf("task") !== -1 || targetID.indexOf("step") !== -1) {
                    targetOrientation = _this.workflow.tasks[targetID].orientation;
                    if (targetOrientation === "normal") {
                        target = targetID + "LeftMiddle";
                    } else {
                        target = targetID + "RightMiddle";
                    }
                } else if (targetID.indexOf("exgw") !== -1) {
                    target = targetID + "TopCenter";
                } else if (targetID.indexOf("end") !== -1) {
                    target = "End";
                }

                var t = _this.workflow.tasks[targetID];
                if (t && t.isSubCondition) { // Skip making jsPlumb connection if target is sub-condition task
                    continue;
                }

                jsPlumb.connect({uuids: [source, target], editable: true});
            }
        } else if (flow.flowType === "sequenceFlow") {
            // sequential flows
            var source = flow.sourceRef;
            var target = flow.targetRef;
            var sourceTask = _this.workflow.tasks[source];
            var targetTask = _this.workflow.tasks[target];

            if ((source || sourceTask) && (target || targetTask)) {
                conditionExpression = flow.conditionExpression;
                if (source === "start") {
                    // start
                    sourceAnchor = "Start";
                } else if (source.indexOf("step") !== -1 || source.indexOf("task") !== -1) {
                    // source is a task
                    sourceOrientation = _this.workflow.tasks[source].orientation;
                    if (sourceOrientation === "normal") {
                        sourceAnchor = source + "RightMiddle";
                    } else {
                        sourceAnchor = source + "LeftMiddle";
                    }
                } else if (source.indexOf("exgw") !== -1) {
                    // source is a condition branch
                    sourceOrientation = _this.workflow.tasks[source].orientation;
                    if (conditionExpression) {
                        // true branch
                        sourceAnchor = source + "BottomCenter";
                    } else {
                        // false branch
                        if (sourceOrientation === "normal") {
                            sourceAnchor = source + "LeftMiddle";
                        } else {
                            sourceAnchor = source + "RightMiddle";
                        }
                    }
                }

                if (target === "end") {
                    // end
                    targetAnchor = "End";
                } else if (target.indexOf("step") !== -1 || target.indexOf("task") !== -1) {
                    // target is a task
                    targetOrientation = _this.workflow.tasks[target].orientation;
                    if (targetOrientation === "normal") {
                        targetAnchor = target + "LeftMiddle";
                    } else {
                        targetAnchor = target + "RightMiddle";
                    }
                } else if (target.indexOf("exgw") !== -1) {
                    // target is a condition branch
                    targetAnchor = target + "TopCenter";
                }

                jsPlumb.connect({uuids: [sourceAnchor, targetAnchor], editable: true});
            }
        }
    };

    var connectorPaintStyle = {
        lineWidth: 1,
        strokeStyle: "black",
        joinstyle: "round",
        outlineColor: "#EAEDEF",
        outlineWidth: 1
    },
    connectorHoverStyle = {
        lineWidth: 1,
        strokeStyle: "red"
    },
    endpointHoverStyle = {fillStyle: "grey"};
    var sourceEndpoint = {
        endpoint: "Dot",
        paintStyle: {
            strokeStyle: "#225588",
            fillStyle: "transparent",
            radius: 7,
            lineWidth: 1,
            outlineWidth: 4
        },
        isSource: true,
        isTarget: false,
        connector: ["Flowchart", {stub: [30, 30], gap: 0, cornerRadius: 5, alwaysRespectStubs: false}],
        connectorStyle: connectorPaintStyle,
        hoverPaintStyle: endpointHoverStyle,
        connectorHoverStyle: connectorHoverStyle,
        dragOptions: {},
        dropOptions: {},
        overlays: [
            ["Label", {
                    location: [0.5, 1.5],
                    label: "",
                    cssClass: "endpointSourceLabel"
                }]
        ]
    },
    trueSourceEndpoint = {
        endpoint: "Dot",
        paintStyle: {
            outlineColor: "green",
            strokeStyle: "#225588",
            fillStyle: "transparent",
            radius: 7,
            lineWidth: 1,
            outlineWidth: 4
        },
        isSource: true,
        connector: ["Flowchart", {stub: [30, 30], gap: 0, cornerRadius: 5, alwaysRespectStubs: false}],
        connectorStyle: connectorPaintStyle,
        hoverPaintStyle: endpointHoverStyle,
        connectorHoverStyle: connectorHoverStyle,
        dragOptions: {},
        dropOptions: {},
        overlays: [
            ["Label", {
                    location: [0.5, 1.5],
                    label: "True",
                    cssClass: "endpointSourceLabel"
                }]
        ]
    },
    falseSourceEndpoint = {
        endpoint: "Dot",
        paintStyle: {
            outlineColor: "red",
            strokeStyle: "#225588",
            fillStyle: "transparent",
            radius: 7,
            lineWidth: 1,
            outlineWidth: 4
        },
        isSource: true,
        connector: ["Flowchart", {stub: [30, 30], gap: 0, cornerRadius: 5, alwaysRespectStubs: false}],
        connectorStyle: connectorPaintStyle,
        hoverPaintStyle: endpointHoverStyle,
        connectorHoverStyle: connectorHoverStyle,
        dragOptions: {},
        dropOptions: {},
        overlays: [
            ["Label", {
                    location: [0.5, 1.5],
                    label: "False",
                    cssClass: "endpointSourceLabel"
                }]
        ]
    },
    targetEndpoint = {
        endpoint: "Dot",
        paintStyle: {fillStyle: "#558822", radius: 7, outlineWidth: 4},
        hoverPaintStyle: endpointHoverStyle,
        maxConnections: -1,
        dragOptions: {},
        dropOptions: {hoverClass: "hover", activeClass: "active"},
        isSource: false,
        isTarget: true,
        overlays: [
            ["Label", {location: [0.5, -0.5], label: "", cssClass: "endpointTargetLabel"}]
        ]
    };
    function drawEndPoints(node, sourceAnchors, targetAnchors, type) {
        var nodeId = node.id;
        var endpoints = [];
        var endpoint;
        switch (type) {
            case "exclusiveGateway":
                var _task = _this.workflow.tasks[node.id];
                if (_task.isMultiCondition) {
                    var targetAnchor = targetAnchors[0];
                    endpoint = jsPlumb.addEndpoint(nodeId, targetEndpoint, {anchor: targetAnchor, uuid: nodeId + targetAnchor});
                    endpoints.push(endpoint);
                } else if (_task.isSubCondition) {
                    var sourceTrueAnchor = sourceAnchors[0];
                    var sourceFalseAnchor = sourceAnchors[1];
                    endpoint = jsPlumb.addEndpoint(nodeId, trueSourceEndpoint, {anchor: sourceTrueAnchor, uuid: nodeId + sourceTrueAnchor});
                    endpoints.push(endpoint);
                    endpoint = jsPlumb.addEndpoint(nodeId, falseSourceEndpoint, {anchor: sourceFalseAnchor, uuid: nodeId + sourceFalseAnchor});
                    endpoints.push(endpoint);

                    // Adjust overlay for False endpoints
                    endpoint.getOverlays()[0].setLabel('Default');
                    endpoint.getOverlays()[0].setLocation([-1.2,1.2]);
                } else {
                    // condition branches
                    var targetAnchor, sourceFalseAnchor, sourceTrueAnchor;
                    if (targetAnchors.length === 1) {
                        targetAnchor = targetAnchors[0];
                    } else {
                        // default
                        targetAnchor = "TopCenter";
                    }
                    if (sourceAnchors.length === 2) {
                        sourceFalseAnchor = sourceAnchors[0];
                        sourceTrueAnchor = sourceAnchors[1];
                    } else {
                        // default
                        sourceFalseAnchor = "LeftMiddle";
                        sourceTrueAnchor = "RightMiddle";
                    }
                    endpoint = jsPlumb.addEndpoint(nodeId, targetEndpoint, {anchor: targetAnchor, uuid: nodeId + targetAnchor});
                    endpoints.push(endpoint);
                    endpoint = jsPlumb.addEndpoint(nodeId, trueSourceEndpoint, {anchor: sourceTrueAnchor, uuid: nodeId + sourceTrueAnchor});
                    endpoints.push(endpoint);
                    endpoint = jsPlumb.addEndpoint(nodeId, falseSourceEndpoint, {anchor: sourceFalseAnchor, uuid: nodeId + sourceFalseAnchor});
                    endpoints.push(endpoint);
                }
                break;
            case "start":
                // start event
                endpoint = jsPlumb.addEndpoint("start", sourceEndpoint, {anchor: "RightMiddle", uuid: "Start"});
                endpoints.push(endpoint);
                break;
            case "end":
                // end event
                endpoint = jsPlumb.addEndpoint("end", targetEndpoint, {anchor: "LeftMiddle", uuid: "End"});
                endpoints.push(endpoint);
                break;
            default:
                // tasks
                for (var i = 0; i < sourceAnchors.length; i++) {
                    var sourceUUID = nodeId + sourceAnchors[i];
                    endpoint = jsPlumb.addEndpoint(nodeId, sourceEndpoint, {anchor: sourceAnchors[i], uuid: sourceUUID});
                    endpoints.push(endpoint);
                }
                for (var j = 0; j < targetAnchors.length; j++) {
                    var targetUUID = nodeId + targetAnchors[j];
                    endpoint = jsPlumb.addEndpoint(nodeId, targetEndpoint, {anchor: targetAnchors[j], uuid: targetUUID});
                    endpoints.push(endpoint);
                }
        }
        return endpoints;
    };

    function addFlow(sourceId, sourceAnchorType, targetId) {
        var flowId = sourceId + "_"+ targetId;
        var task = _this.workflow.tasks[sourceId];
        if (sourceId === "start" || (task && task.taskType !== "exclusiveGateway")) {
            // task
            var flow = new Flow("sequenceFlow", sourceId, targetId);
            _this.workflow.addFlow(flowId, flow);
        } else {
            if (task.isSubCondition) {
                if (sourceAnchorType === "BottomCenter") {
                    _this.workflow.addConditionFlow(sourceId, "False", targetId);
                } else {
                    _this.workflow.addConditionFlow(sourceId, "True", targetId);
                }
            } else {
                // condition branch
                if (sourceAnchorType === "BottomCenter") {
                    _this.workflow.addConditionFlow(sourceId, "True", targetId);
                } else {
                    _this.workflow.addConditionFlow(sourceId, "False", targetId);
                }
            }
        }
    };

    function removeFlow(sourceId, targetId) {
        var key = sourceId + "_" + targetId;
        if (targetId === "end") {
            _this.workflow.removeEndTask(sourceId);
            _this.workflow.removeFlow(key);
        } else {
            if (_this.workflow.flows[sourceId]) {
                // condition branch
                _this.workflow.removeConditionalFlow(sourceId, targetId);
            } else if (_this.workflow.tasks[sourceId]) {
                // task
                _this.workflow.removeFlow(key);
            } else {
                // start
                _this.workflow.removeStartTask();
                _this.workflow.removeFlow(key);
            }
        }
    };

    // initialize jsPlumb
    jsPlumb.ready(function () {
        jsPlumb.importDefaults({
            Container: "container",
            DragOptions: {cursor: 'pointer', zIndex: 2000},
            EndpointStyles: [
                {fillStyle: '#225588'},
                {fillStyle: '#558822'}
            ],
            Endpoints: [
                ["Dot", {radius: 7}],
                ["Dot", {radius: 7}]
            ],
            ConnectionOverlays: [
                ["Arrow", {location: -7, width: 10, length: 10, foldback: 0}],
                ["Label", {
                        location: 0.1,
                        id: "label",
                        cssClass: "aLabel"
                    }]
            ]
        });

        jsPlumb.bind("connection", function (info, originalEvent) {
            // add a flow
            //console.log("******** a connection is made");
            //console.dir(info);

            var sourceId = info.connection.sourceId;
            var sourceAnchorType = info.sourceEndpoint.anchor.type;
            var targetId = info.connection.targetId;
            addFlow(sourceId, sourceAnchorType, targetId);
        });

        jsPlumb.bind("connectionDetached", function (info, originalEvent) {
            // remove a flow
            //console.log("******** a connection was detached");
            //console.dir(info);

            var sourceId = info.connection.sourceId;
            var targetId = info.connection.targetId;
            removeFlow(sourceId, targetId);
        });

        jsPlumb.bind("connectionMoved", function (info, originalEvent) {
            // move a flow
            //console.log("******** a connection was moved");
            //console.dir(info);

            // should just do the same as connectionDetached
            // since the add new connection part is done by the connection event
            var sourceId = info.originalSourceId;
            var targetId = info.originalTargetId;
            removeFlow(sourceId, targetId);
        });

        jsPlumb.bind("click", function (connection, originalEvent) {
            // remove a flow dialog
            // console.log("******** a connection clicked");
            // console.dir(connection);

            var yes = confirm("Delete connection from " + connection.sourceId + " to " + connection.targetId + "?");
            if (yes) {
                jsPlumb.detach(connection);
            }
        });

        jsPlumb.bind("dblclick", function (connection, originalEvent) {
            //console.log("******** dblclick");
            //console.dir(connection);
        });

        jsPlumb.bind("contextmenu", function (connection, originalEvent) {
            //console.log("******** contextmenu");
            //console.dir(connection);
        });

        jsPlumb.bind("connectionDrag", function (connection) {
            //console.log("connection " + connection.id + " is being dragged. suspendedElementId is ", connection.suspendedElementId, " of type ", connection.suspendedElementType);
        });

        jsPlumb.bind("connectionDragStop", function (connection) {
            //console.log("connection " + connection.id + " was dropped");
        });

        jsPlumb.draggable(jsPlumb.getSelector(".window"), {grid: [20, 20]});
    });
});

angular.module("crisWorkflow").directive("crisWorkflowJsonBuilder", function ($compile, $timeout, $rootScope) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            type: '@', // jsonIn or jsonOut
            task: '=',
            queries: '='
        },
        template: '<div ng-include="\'view_builder_json\'"></div>',
        link: function (scope, element, attrs) {
            scope.$watch("task.id", function (newValue, oldValue) {
                if (newValue && newValue !== oldValue) {
                    scope.deserializeJson();
                    if (scope.item && !scope.error) {
                        scope.serializeToJson();
                    }
                }
            });
            
            $rootScope.$on('onTaskDetailsOpened', function (event, taskId) {
                console.log('************* BROADCAST HANDLING - onTaskDetailsOpened *************')
                $timeout(function () {
                    var json = scope.task[scope.type];
                    // If no newline character in json string (due to a recent save), deserialize and reserialize json to create readable well-formatted json.
                    if (scope.task.id === taskId && !scope.error && !(/\n/.test(json))) {
                        scope.deserializeJson();
                        scope.serializeToJson();
                    }
                }, 600);
            });

            // Broadcast events for updating views
            // *******
            // Textarea json edit: parse json to object
            scope.$on('onJsonTextEdit', function (event, data) {
                console.log('********** EMIT HANDLING - onJsonTextEdit ***************');
                scope.deserializeJson();
                scope.$broadcast('refreshQueries');
            });

            // json builder edit: serialize result to json
            scope.$on('onJsonBuilderEdit', function (event, args) {
                console.log('*********** EMIT HANDLING - onJsonBuilderEdit **********');
                scope.serializeToJson();
            });

            // query builder edit: serialize result to json
            scope.$on('onQueryEdit', function (event, args) {
                console.log('************* EMIT HANDLING - onQueryUpdated ***********');
                scope.serializeToJson();
            });

            scope.$watch('queries.length', function (val) {
                if (val) {
                    scope.queryAliases = [];
                    dojo.forEach(scope.queries, function (q) {
                        scope.queryAliases.push({id: q.key, name: q.key, query: q});
                    });
                }
            });

            scope.fetchTemplates();
        },
        controller: function ($scope, $compile, workflowBuilderService) {
            $scope.addJsonVariable = function (variableType) { // variableType = 'template' or 'variable'
                var valueType = (variableType === 'template') ? 'query' : null;
                $scope.item.push({variableType: variableType, variable: null, valueType: valueType, staticDataType: null, value: null, subValue: null});
            };

            $scope.removeItem = function (index) {
                $scope.item.splice(index, 1);
                $scope.onJsonBuilderEdit();
            };

            $scope.previewJson = function () {
                workflowBuilderService.jsonPreview($scope, $scope.item, $scope.queries)
            };

            $scope.deserializeJson = function () {
                $scope.queries = [];
                var json = $scope.task[$scope.type]; // type = jsonIn or jsonOut
                var val = workflowBuilderService.deserializeJson(json, $scope.queries);
                if (val.error) {
                    $scope.error = val.error;
                } else {
                    $scope.error = ""; // Clear previous error
                }
                $scope.item = val.result;

                $scope.queryAliases = [];
                dojo.forEach($scope.queries, function (q) {
                    $scope.queryAliases.push({id: q.key, name: q.key, query: q});
                });
            };
            
            $scope.getTermNames = function(templateUUID) {
                var termNames = [];
                for (var i = 0; i < $scope.templates.length; i++) {
                    if ($scope.templates[i].id === templateUUID) {
                        termNames = termNames.concat($scope.templates[i].termNames);
                        for (var j = termNames.length - 1; j > -1; j--) {
                            if (['_job_id', '_experiment_id', '_project_id'].indexOf(termNames[j].id) !== -1) {
                                termNames.splice(j, 1);
                            }
                        }
                        termNames.unshift({id: '_id', name: '_id'});
                        break;
                    }
                }
                return termNames;
            };
            
            $scope.getTemplates = function(index) {
                var templates = [];
                var usedTemplates = [];
                for (var i = 0; i < $scope.item.length; i++) {
                    if ($scope.item[i].variableType === 'template' && $scope.item[i].variable && i !== index) {
                        usedTemplates.push($scope.item[i].variable);
                    }
                }
                dojo.forEach($scope.templates, function(t) {
                    if (usedTemplates.indexOf(t.id) === -1) {
                        templates.push(t);
                    }
                });
                return templates;
            }
            
            $scope.addTermToMerge = function(index) {
                var termsToMerge = $scope.item[index].termsToMerge;
                if (!termsToMerge) {
                    termsToMerge = $scope.item[index].termsToMerge = [];
                }
                termsToMerge.push({});
            };
            
            $scope.removeTermToMerge = function (parentIndex, index) {
                $scope.item[parentIndex].termsToMerge.splice(index, 1);
                $scope.onJsonBuilderEdit();
            };
            
            $scope.serializeToJson = function () {
                workflowBuilderService.fromBuilderToJson($scope.item, $scope.task, $scope.type, $scope.queries);
            };

            // notify json builder after json text edit (onBlur)
            $scope.onJsonTextEdit = function (data) {
                $timeout(function () { // timeout needed because after onBlur event updated value is not yet available
                    if (data !== $scope.task[$scope.type]) { // Check for change in data...sometimes onblur event fires when clicking outside window boundary
                        $scope.$emit('onJsonTextEdit');
                    }
                }, 300);
            };

            // notify to refresh json textarea after builder edit (queries or json builder)
            $scope.onJsonBuilderEdit = function (index) {
                $timeout(function () { // timeout needed because after onBlur event, updated value is not yet available
                    if (typeof index === 'number') {
                        if ($scope.item[index].value || $scope.item[index].staticDataType === "Null") {
                            $scope.$emit('onJsonBuilderEdit');
                        }
                    } else {
                        $scope.$emit('onJsonBuilderEdit');
                    }
                }, 300);
            };

            $scope.fetchTemplates = function () {
                workflowBuilderService.getTemplateData().then(function (res) {
                    $scope.templates = res;
                }, function (err) {
                    $scope.templates = [];
                });
            };
        }
    }
});

angular.module("crisWorkflow").directive("crisWorkflowVariableTypes", function ($compile) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            item: '=',
            queries: '@'
        },
        template: '<div ng-include="\'view_builder_variable_types\'"></div>',
        link: function (scope, element, attrs) {
            scope.fetchSystemVariables();
        },
        controller: function($scope, $timeout, workflowBuilderService) {
            $scope.fetchSystemVariables = function () {
                $scope.systemVariables = workflowBuilderService.getSystemVariables();
            };
            $scope.onJsonBuilderEdit = function (type) {
                $timeout(function () {
                    if ($scope.item.value || ($scope.item.termsToMerge && $scope.item.termsToMerge.length) || $scope.item.staticDataType === "Null") {
                        if (type === 'valueType') {
                            $scope.item.value = null;
                            $scope.item.staticDataType = null;
                            $scope.item.subValue = null;
                        } else if (type === 'value') {
                            $scope.item.subValue = null;
                        } else if (type === 'staticDataType') {
                            $scope.item.value = null;
                        }
                        $scope.$emit('onJsonBuilderEdit');
                    }
                }, 300);
            };
            $scope.termNames = function () {
                var termNames = [];
                var queries = eval($scope.queries);
                for (var i = 0; i < queries.length; i++) {
                    if (queries[i].id === $scope.item.value) {
                        dojo.forEach(queries[i].query.termNames, function (term) {
                            if (['_job_id', '_experiment_id', '_project_id'].indexOf(term.id) === -1) { // Only template terms
                                termNames.push(term);
                            }
                        });
                        // termNames.unshift({id: '_id', name: '_id'});
                        break;
                    }
                }
                return termNames;
            };
        }
    }
});

angular.module("crisWorkflow").directive("crisWorkflowQuery", function ($compile, $timeout) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            queries: '=',
            taskId: '@'
        },
        template: '<div ng-include="\'view_builder_query\'"></div>',
        link: function (scope, element, attrs) {
            scope.selectedQuery = {};
            scope.$watch('queries.length', function (val) {
                if (val) {
                    scope.queryAliases = [];
                    dojo.forEach(scope.queries, function (q) {
                        scope.queryAliases.push({id: q.key, name: q.key, query: q});
                    });
                }
            });

            scope.$watch('taskId', function (newValue, oldValue) {
                if (newValue && newValue !== oldValue) {
                    scope.isQuerySelected = false;
                    if (scope.previousSelection) {
                        scope.previousSelection.style.backgroundColor = '#FFF';
                    }
                }
            });

            scope.$watch('selectedQuery.templateUUID', function (newValue, oldValue) {
                if (newValue) {
                    dojo.forEach(scope.templates, function (template) {
                        if (template.id === scope.selectedQuery.templateUUID) {
                            scope.selectedQuery.termNames = template.termNames;
                        }
                    });
                    scope.validateSelectedQueryWhere();
                }
            });

            scope.$watch('selectedQuery.addCurrentJobFilter', function (newValue, oldValue) {
                if (scope.selectedQuery && newValue) {
                    var hasCurrentJobFilter = false;
                    dojo.forEach(scope.selectedQuery.where, function (where) {
                        if (where.term === '_job_id') {
                            hasCurrentJobFilter = true;
                        }
                    });
                    if (!hasCurrentJobFilter) {
                        scope.selectedQuery.where.push({term: '_job_id' ,
                                                        value: 'current_job.id' ,
                                                        valueType: 'systemVariable' ,
                            queryOperator: '$eq'});
                        scope.onQueryEdit(); // Notify json builder of update
                    }
                } else if (scope.selectedQuery && !newValue && oldValue) {
                    for (var i = 0; i < scope.selectedQuery.where.length; i++) {
                        if (scope.selectedQuery.where[i].term === '_job_id') {
                            scope.selectedQuery.where.splice(i, 1);
                            scope.onQueryEdit(); // Notify json builder of update
                            break;
                        }
                    }
                }
            });

            scope.$on('refreshQueries', function (event, data) {
                console.log('********** BROADCAST HANDLING - refreshQueries ***************');
                $timeout(function () {
                    if (scope.isQuerySelected && scope.queries[scope.selectedQueryIndex]) {
                        scope.selectedQuery = scope.queries[scope.selectedQueryIndex];
                        scope.validateSelectedQueryWhere();
                    }
                }, 300);
            });
            scope.fetchTemplates();
        },
        controller: function($scope, $timeout, workflowBuilderService) {
            $scope.querySelected = function (index, event) {
                console.log('************* Query Selected ************************');
                $scope.isQuerySelected = true;
                $scope.selectedQueryIndex = index;
                $scope.selectedQuery = $scope.queries[index];

                if ($scope.previousSelection) {
                    $scope.previousSelection.style.backgroundColor = $scope.previousBackgroundColor;
                }

                $scope.previousSelection = event.currentTarget;
                $scope.previousBackgroundColor = $scope.previousSelection.style.backgroundColor;
                event.currentTarget.style.backgroundColor = '#FFCC99';
            };

            $scope.removeWhereItem = function (index) {
                if ($scope.selectedQuery.where[index].term === '_job_id') {
                    $scope.selectedQuery.addCurrentJobFilter = false;
                }
                $scope.selectedQuery.where.splice(index, 1);
                $scope.onQueryEdit();
            };

            $scope.addQueryWhere = function () {
                $scope.selectedQuery.where.push({});
            };

            ///******
            $scope.getQueryAliases = function () {
                var item = [];
                dojo.forEach($scope.queryAliases, function (val) {
                    if (val.id !== $scope.selectedQuery.key) {
                        item.push(val);
                    }
                });
                return item;
            };

            $scope.addQuery = function () {
                if (!$scope.queries) {
                    $scope.queries = [];
                }
                var queryKey = 'QUERY__' + ($scope.queries.length + 1);
                $scope.queries.push({key: queryKey, templateUUID: '', where: []});
            };

            $scope.deleteQuery = function (query) {
                for (var i = 0; i < $scope.queries.length; i++) {
                    if (query.key === $scope.queries[i].key) {
                        $scope.queries.splice(i, 1);
                        $scope.isQuerySelected = false;
                        $scope.previousSelection.style.backgroundColor = '#FFF';
                        break;
                    }
                }
            };

            // Make sure where condition has valid terms
            $scope.validateSelectedQueryWhere = function () {
                // Make sure where condition has valid terms
                dojo.forEach($scope.selectedQuery.where, function (item) {
                    var valid = false;
                    dojo.forEach($scope.selectedQuery.termNames, function (term) {
                        if (item.term === term.id) {
                            valid = true;
                        }
                    });
                    if (!valid) {
                        item.term = "";
                    }
                    if (item.term === '_job_id') {
                        $scope.selectedQuery.addCurrentJobFilter = true;
                    }
                });
            };

            // Notify json builder of changes.....
            $scope.onQueryEdit = function (index) {
                $timeout(function () {
                    if (((typeof index === 'number') && $scope.selectedQuery.where[index] && $scope.selectedQuery.where[index].value) || !index) {
                        $scope.$emit('onQueryEdit');

                        // Uncheck current-job checkbox if _job_id term is switched to another term
                        var hasCurrentJobFilter = false;
                        dojo.forEach($scope.selectedQuery.where, function (item) {
                            if (item.term === '_job_id') {
                                hasCurrentJobFilter = true;
                            }
                        });
                        if (!hasCurrentJobFilter && $scope.selectedQuery.addCurrentJobFilter) {
                            $scope.selectedQuery.addCurrentJobFilter = false;
                        }
                    }
                }, 300);
            };

            $scope.previewQuery = function () {
                workflowBuilderService.jsonPreview($scope, null, $scope.queries, $scope.selectedQuery.key);
            };

            $scope.fetchTemplates = function () {
                workflowBuilderService.getTemplateData().then(function (res) {
                    $scope.templates = res;
                }, function (err) {
                    $scope.templates = [];
                });
            };

            $scope.getTermNames = function (index) {
                // A term can only be used once in the where
                var usedTermNames = [];
                dojo.forEach($scope.selectedQuery.where, function (whereItem, idx) {
                    //if (index  !== idx && ['_job_id', '_experiment_id', '_project_id'].indexOf(whereItem.term) !== -1) {
                    if (index !== idx) {
                        usedTermNames.push(whereItem.term);
                    }
                });

                var item = [];
                dojo.forEach($scope.selectedQuery.termNames, function (termName) {
                    if (usedTermNames.indexOf(termName.id) === -1) {
                        item.push(termName);
                    }
                });
                return item;
            };
        }
    }
});

angular.module("crisWorkflow").factory('workflowBuilderService', ["$q", "$compile", function($q, $compile) {
    var templateData;
    var deferred = $q.defer();

    var balancedMatcher = function (source, patternStart, patternEnd) {
        var patternBegin = patternStart;
        var patternEnd = patternEnd;
        var source = source;
        var start = 0;
        var nextPosition = 0;
        var end = null;

        return {
            find: function () {
                var count = 0;
                var found = false;
                var i = nextPosition;
                while (i < source.length) {
                    var skip = 1;

                    var s = false;
                    for (var j = 0; j < patternBegin.length; j++) {
                        s = source.startsWith(patternBegin[j], i);
                        if (s) {
                            if (!found) {
                                // reset count after finding first match
                                count = 0;
                            }
                            found = true;
                            skip = patternBegin[j].length;
                            break;
                        }
                    }

                    if (found && !s) {
                        s = source.startsWith('{', i);
                    }

                    var e = false;
                    for (var j = 0; j < patternEnd.length; j++) {
                        e = source.startsWith(patternEnd[j], i);
                        if (e) {
                            break;
                        }
                    }

                    if (s) {
                        if (count === 0) {
                            start = i;
                        }
                        count++;
                    } else if (e) {
                        count--;
                        if (count === 0) {
                            end = i + 1;
                            nextPosition = end;
                            return true;
                        }
                    }
                    i += skip;
                }
                return false;
            },
            start: function () {
                return start;
            },
            end: function () {
                return end;
            }
        }
    };

    var eval = function (jsonVal, queryItems) {
        jsonVal = jsonVal.replace(/\n/g, '');
        jsonVal = _removeUnsafeSpaceCharacters(jsonVal);
        var matcher = balancedMatcher(jsonVal, ['${', '#{'], ['}']);
        var startIndex = 0;
        var str = "";
        while (matcher.find()) {
            str += (jsonVal.substring(startIndex, matcher.start()));
            var contents = jsonVal.substring(matcher.start() + 2, matcher.end() - 1); // Contents wrapped in ${} or #{}
            var val = eval(contents, queryItems);

            var REGEX_QUERY = /^\s*([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12})/i;
            if (REGEX_QUERY.test(val)) {
                // Find specific term to query if it exists. E.g. templateUUID.TermA
                var termToQuery = '';
                var QUERY_SPECIFIC_TERM = /^\s*[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}\.((\w\.?)+)/i;

                var results = QUERY_SPECIFIC_TERM.exec(val);
                if (results) {
                    termToQuery = '.' + results[1];
                }
                
                // Remove specific term to query...
                var reg = /^\s*([0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12})(?:\.\w+)+\s*(?=\()/i;
                val = val.replace(reg, '$1');
                
                val = val.replace(/\s*(\$[a-zA-Z]+)\s*(?=\:)/ig, '"' + '$1' + '"'); // Place quotes around mongo query operators. e.g. "$and"
                
                var returnsMultipleRecords = /^\s*[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}\[\]/i.test(val);
                if (returnsMultipleRecords) {
                    val = val.replace(/^\s*([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12})\[\]/i, '$1'); // remove array symbol for multiple records from query
                }

                var queryKey = null;
                for (var i = 0; i < queryItems.length; i++) {
                    if (queryItems[i].rawQuery === val) {
                        queryKey = queryItems[i].key;
                        break;
                    }
                }
                if (!queryKey) {
                    var _key = 'QUERY__' + (queryItems.length + 1);
                    queryItems.push({rawQuery: val, key: _key, multipleRecords: returnsMultipleRecords});
                    val = _key + termToQuery;
                } else {
                    val = queryKey + termToQuery;
                }
            }

            var origVal = jsonVal.substring(matcher.start(), matcher.end());
            if (origVal.startsWith('${')) { // local variable
                if (val.startsWith('QUERY__')) {
                    str += '"' + val + '"';
                } else {
                    str += '"' + val + ':$"';
                }
            } else if (origVal.startsWith('#{')) { // system variable
                str += '"' + val + ':#"';
            }
            startIndex = matcher.end();
        }
        str += jsonVal.substring(startIndex);
        return str;
    };
    
    var _removeUnsafeSpaceCharacters = function _evalJson(jsonString) {
        var matcher = balancedMatcher(jsonString, ['${', '#{'], ['}']);
        var startIndex = 0;
        var str = "";
        while (matcher.find()) {
            str += (jsonString.substring(startIndex, matcher.start()));
            var contents = jsonString.substring(matcher.start() + 2, matcher.end() - 1); // Contents wrapped in ${} or #{}
            var val = _evalJson(contents.trim());

            var origVal = jsonString.substring(matcher.start(), matcher.end());
            if (origVal.startsWith('${')) {
                str += '${' + val + '}';
            } else if (origVal.startsWith('#{')) {
                str += '#{' + val + '}';
            }
            startIndex = matcher.end();
        }
        str += jsonString.substring(startIndex);
        return str;
    };
    
    var deserializeQueries = function (queryItems) {
        for (var j = 0; j < queryItems.length; j++) {
            var queryItem = queryItems[j];

            if (queryItem.isDeserialized) { // current query already deserialized
                continue;
            } else {
                queryItem.where = [];
                var queryStr = queryItem.rawQuery;

                if (queryStr.indexOf('(') !== -1 && queryStr.endsWith(')')) {
                    var queryParams = queryStr.substring(queryStr.indexOf('(') + 1, queryStr.length - 1);
                    var deserializedParams = angular.fromJson(queryParams);
                    for (var key in deserializedParams) {
                        var paramValue = deserializedParams[key];
                        if (key === '$skip' || key === '$limit' || key === '$orderby') {
                            if (key === '$orderby' && typeof paramValue === 'object') {
                                queryItem[key] = Object.keys(paramValue)[0];
                                queryItem.orderbyOrder = paramValue[queryItem[key]];
                            } else {
                                queryItem[key] = paramValue;
                            }
                        } else {
                            var whereItem = {};
                            var whereItems = [];
                            if (key === '$and') {
                                dojo.forEach(paramValue, function (item) {
                                    var term = Object.keys(item)[0];
                                    var _whereItem = {};
                                    _whereItem.term = term;
                                    if (typeof item[term] === 'object' && !(item[term] instanceof Array)) {
                                        var operator = Object.keys(item[term])[0]; // Mongo operator. E.g. $lt, $gt, $gte, etc.
                                        if (operator && operator.trim().startsWith('$')) {
                                            _whereItem.queryOperator = operator;
                                            _whereItem.value = item[term][operator];
                                        }
                                    } else {
                                        _whereItem.queryOperator = '$eq';
                                        _whereItem.value = item[term];
                                    }
                                    if (!_whereItem.queryOperator && typeof item[term] === 'object') { // object or array value
                                        _whereItem.queryOperator = '$eq';
                                        _whereItem.value = item[term];
                                    }
                                    whereItems.push(_whereItem);
                                });
                            } else { // all other properties are implicitly 'AND' conditions
                                whereItem.term = key;
                                if (typeof paramValue === 'object' && paramValue !== null && !(paramValue instanceof Array)) {
                                    var operator = Object.keys(paramValue)[0]; // Mongo operator. E.g. $lt, $gt, $gte, etc.
                                    if (operator && operator.trim().startsWith('$')) {
                                        whereItem.queryOperator = operator;
                                        whereItem.value = paramValue[operator];
                                    }
                                } else {
                                    whereItem.queryOperator = '$eq';
                                    whereItem.value = paramValue;
                                }
                                if (!whereItem.queryOperator && typeof paramValue === 'object') { // object or array value
                                    whereItem.queryOperator = '$eq';
                                    whereItem.value = paramValue;
                                }
                            }

                            if (whereItems.length) {
                                whereItem = whereItems.pop();
                            }
                            while (whereItem) {
                                var valueProperties = setValueProperties(whereItem.value);
                                dojo.mixin(whereItem, valueProperties);
                                queryItem.where.push(whereItem);
                                whereItem = whereItems.pop();
                            }
                        }
                    }

                    var queryFrom = queryStr.substring(0, queryStr.indexOf('('));

                    var REGEX_UUID = /([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12})/i;
                    queryItem.templateUUID = REGEX_UUID.exec(queryFrom)[0];

                    for (var r = 0; r < templateData.length; r++) {
                        var template = templateData[r];
                        if (template.id === queryItem.templateUUID) {
                            queryItem.termNames = template.termNames;
                            break;
                        }
                    }
                } else {
                    throw 'Invalid query in json!';
                }
                queryItem.isDeserialized = true;
            }
        }
    };

    var serializeToJson_ = function (builderRecords, queries, queryId) {
        var newJsonStr = '';
        updateRawQueries(queries); // deserialze all queries to their raw versions
        if (builderRecords) {
            var records = angular.copy(builderRecords);
            if (records.length) {
                
                var termsToMerge = {};
                var newJsonObj = sanitizeJsonRecords(records, termsToMerge);
                for (var key in newJsonObj) {
                    var value = newJsonObj[key];
                    var terms = termsToMerge[key];
                    if (terms && Object.keys(terms).length) {
                        var existingDirective = newJsonObj['$directive'];
                        if (existingDirective) {
                            if (!(existingDirective instanceof Array)) {
                                var mergePath = Object.keys(newJsonObj['$data'][0])[0];
                                newJsonObj['$directive'] = [{$merge: {$path: mergePath}}];
                                var n = [];
                                for (var k in newJsonObj['$data']) {
                                    n.push(newJsonObj['$data'][k][mergePath]);
                                }
                                var m = {};
                                m[mergePath] = n;
                                newJsonObj['$data'] = m;
                            }
                            newJsonObj['$directive'].push({$merge: {$path: key}});
                            newJsonObj['$data'][key] = [value, terms];
                        } else {
                            var mergeObj = {};
                            mergeObj['$directive'] = {$merge:{}};
                            mergeObj['$data'] = [];

                            var v1 = {};
                            v1[key] = value;
                            mergeObj['$data'].push(v1);

                            var v2 = {};
                            v2[key] = terms;
                            mergeObj['$data'].push(v2);

                            dojo.mixin(newJsonObj, mergeObj);
                        }
                        delete newJsonObj[key];
                    }
                    
                    /*
                     * TODO: Add support for non-template merge objects
                     * 
                     * This if makes sure non-template merge objects in the advanced editor aren't lost 
                     */
                    if (typeof value === 'object' && value.mergeValues) {
                        var existingDirective = newJsonObj['$directive'];
                        if (existingDirective) {
                            if (!(existingDirective instanceof Array)) {
                                var mergePath = Object.keys(newJsonObj['$data'][0])[0];
                                newJsonObj['$directive'] = [{$merge: {$path: mergePath}}];
                                var n = [];
                                for (var k in newJsonObj['$data']) {
                                    n.push(newJsonObj['$data'][k][mergePath]);
                                }
                                var m = {};
                                m[mergePath] = n;
                                newJsonObj['$data'] = m;
                            }
                            newJsonObj['$directive'].push({$merge: {$path: key}});
                            newJsonObj['$data'][key] = value.mergeValues;
                        } else {
                            var mergeObj = {};
                            mergeObj['$directive'] = {$merge:{}};
                            mergeObj['$data'] = [];
                            
                            dojo.forEach(value.mergeValues, function(val){
                                var v = {};
                                v[key] = val;
                                mergeObj['$data'].push(v);
                            });
                            dojo.mixin(newJsonObj, mergeObj);
                        }
                        delete newJsonObj[key];
                    }
                }
                
                // if $directive property exists, move all other properties into the "$data" array property. The top level of the json will only have $directive and $data)
                if (newJsonObj['$directive']) {
                    for (var key in newJsonObj) {
                        if (key === '$directive' || key === '$data') {
                            continue;
                        }
                        if (!(newJsonObj['$directive'] instanceof Array)) {
                            var mergePath = Object.keys(newJsonObj['$data'][0])[0];
                            newJsonObj['$directive'] = [{$merge: {$path: mergePath}}];
                            var n = [];
                            for (var k in newJsonObj['$data']) {
                                n.push(newJsonObj['$data'][k][mergePath]);
                            }
                            var m = {};
                            m[mergePath] = n;
                            newJsonObj['$data'] = m;
                        }
                        newJsonObj['$data'][key] = newJsonObj[key];
                        delete newJsonObj[key];
                    }
                }
                
                newJsonStr = JSON.stringify(newJsonObj, null, 4);
            }
        } else if (queryId) {
            for (var j = 0; j < queries.length; j++) {
                if (queries[j].key === queryId) {
                    newJsonStr = prettifyQuery(queries[j].rawQuery);
                    break;
                }
            }
        }

        while (newJsonStr.indexOf('QUERY__') !== -1) {
            for (var j = 0; j < queries.length; j++) {
                if (newJsonStr.indexOf(queries[j].key) !== -1) {
                    var prettifiedQuery = prettifyQuery(queries[j].rawQuery);

                    // replace query aliases (e.g. QUERY__2) with actual query. Take into account specific term to query, i.e. templateUUID.TermToQuery({...});
                    // Query alias with dot notation has specific term to query. E.g QUERY__3.TermToQuery
                    var reg = new RegExp(queries[j].key + "((?:\\.\\w+)*)(?=\\b)", "g");
                    newJsonStr = newJsonStr.replace(reg, function (a, termToQuery, offset) {
                        var replacement = prettifiedQuery;
                        if (termToQuery) { // termToQuery is matched group in regex for the specific term to query
                            replacement = replacement.replace(/([0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12})/i, '$1' + termToQuery);
                        }

                        // Calculate distance (in char count) from left edge of textarea to index of query in string.
                        // Newline character is the indicator for textarea edge
                        var lastNewLineIndex = offset;
                        var char = newJsonStr.charAt(lastNewLineIndex)
                        while (char) {
                            if (char === '\n') {
                                break;
                            } else {
                                lastNewLineIndex--;
                                char = newJsonStr.charAt(lastNewLineIndex);
                            }
                        }
                        // Add padding to each newline character in the query we are placing in the string
                        // This maintains the json formatting
                        if (lastNewLineIndex >= 0) {
                            var charCountFromTextAreaEdge = offset - lastNewLineIndex;
                            var r = replacement.replace(/\n[ ]*/g, function (match) {
                                var padding = "";
                                var idx = charCountFromTextAreaEdge;
                                while (idx !== 0) {
                                    padding = padding + " ";
                                    idx--;
                                }
                                return match + padding
                            });
                            replacement = r;
                        }

                        return replacement;
                    });
                }
            }
        }
        newJsonStr = newJsonStr.replace(/"(\$\{|\#\{)/g, '$1').replace(/\}"/g, '}'); // No quotes on local/system variables and queries
        newJsonStr = newJsonStr.replace(/"(true|false)"/g, '$1'); // No quotes around booleans
        newJsonStr = newJsonStr.replace(/"(\$directive|\$merge|\$data|\$path)"/g, '$1'); // No quotes around $directives
        return newJsonStr;
    };
    
    function setValueProperties (_value) {
        var value = _value;
        var valueType = 'static';
        var staticDataType = null;
        var subValue = null;
        if (typeof value === 'string') {
            if (value.endsWith(':$')) {
                valueType = 'localVariable';
                value = value.substring(0, value.length - 2);
            } else if (value.endsWith(':#')) {
                valueType = 'systemVariable';
                value = value.substring(0, value.length - 2);
            } else if (value.startsWith('QUERY__')) {
                valueType = 'query';
                // extract specific term to query (It's appended to query key using dot notation)
                var idx = value.indexOf('.');
                if (idx > 0 && idx !== value.length - 1) {
                    subValue = value.substring(idx + 1, value.length);
                    value = value.substring(0, idx);
                }
            }
        }
        if (typeof value === 'number' && !isNaN(value)) {
            staticDataType = 'Numeric';
        } else if (isNaN(value) && !isNaN(Date.parse(value))) {
            staticDataType = 'Date';
        } else if (typeof value === 'boolean') {
            value = value.toString(); // make sure booleans are stringified (for boolean dropdown to work)
            staticDataType = 'Boolean';
        } else if (value === null) {
            staticDataType = "Null";
        } else {
            if (typeof value === 'string') {
                staticDataType = 'Text';
            } else { // value is object or array. Item won't be editable in the builder, only in the Advanced section
                valueType = 'object';
            }
        }
        return {valueType: valueType, value: value, staticDataType: staticDataType, subValue: subValue};                   
    }
    
    function prettifyQuery (queryStr) { // format the json part of a query
        var REGEX_JSONPART = /[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}(?:\.\w+)?(?:\[\])?\((\{.*\})\)/i; // matches a the json part of query
        var queryParts = REGEX_JSONPART.exec(queryStr);
        if (queryParts && queryParts[1]) {
            var queryPart = queryParts[1];
            var objectified = angular.fromJson(queryPart);
            var prettified = JSON.stringify(objectified, null, 4); // angular's toJson skips property names starting with '$'
            queryStr = queryStr.replace(queryPart, prettified);
            queryStr = queryStr.replace(/"((?:(?:\w+\.)*)?\w+):\$"/g, '${' + '$1' + '}'); // standardize local variables with ${...}. No quotes
            queryStr = queryStr.replace(/"((?:\w+\.)?\w+):#"/g, '#{' + '$1' + '}'); // standardize system variables with #{...}. No quotes
            queryStr = queryStr.replace(/"(\$\w+)"(?=\s*\:)/g, '$1'); // No quotes on query operators (e.g. $gt)
            queryStr = queryStr.replace(/"(true|false)"/g, '$1'); // No quotes on booleans
        }
        return '${' + queryStr + '}';
    }

    function updateRawQueries (queryItems) { // convert query objects in the query builder to raw mongo queries
        if (!queryItems) {
            return;
        }

        for (var j = 0; j < queryItems.length; j++) {
            var query = queryItems[j];
            var rawQuery = '';
            if (query.templateUUID) {
                if (query.multipleRecords === true) {
                    rawQuery = query.templateUUID + '[](';
                } else {
                    rawQuery = query.templateUUID + '(';
                }

                var obj = {};
                for (var i = 0; i < query.where.length; i++) {
                    var item = query.where[i];
                    if (item.term && (item.value || item.staticDataType === "Null") && item.queryOperator) {
                        var value = item.value;

                        // Add identifiers to system and localVariables
                        if (item.valueType === 'systemVariable') {
                            value += ':#';
                        } else if (item.valueType === 'localVariable') {
                            value += ':$';
                        } else if (item.valueType === 'static' && item.staticDataType === 'Numeric') {
                            value = parseInt(value, 10);
                        } else if (item.valueType === 'query' && item.subValue) {
                            value = value + (item.subValue ? '.' + item.subValue : '');
                        }

                        if (item.queryOperator === '$eq') {
                            obj[item.term] = value;
                        } else {
                            var nestedValue = {};
                            nestedValue[item.queryOperator] = value;
                            obj[item.term] = nestedValue;
                        }
                    }
                }

                if (query['$limit']) {
                    obj['$limit'] = parseInt(query['$limit'], 10);
                }
                if (query['$orderby'] && query['orderbyOrder']) {
                    var orderByObj = {};
                    orderByObj[query['$orderby']] = query['orderbyOrder'];
                    obj['$orderby'] = orderByObj;
                }
                if (query['$skip']) {
                    obj['$skip'] = parseInt(query['$skip'], 10);
                }
                query.rawQuery = rawQuery + dojo.toJson(obj) + ')';
            }
        }
    }

    function sanitizeJsonRecords(builderRecords, termsToMerge) {
        var newJsonObj = {};
        for (var i = 0; i < builderRecords.length; i++) {
            var record = builderRecords[i];
            if (!record.variable || !record.valueType || (!record.value && record.staticDataType !== "Null")) {
                if (record.variable && record.variableType === 'template' && record.termsToMerge && record.termsToMerge.length) {
                    newJsonObj[record.variable] = sanitizeJsonRecords(record.termsToMerge);
                }
                continue; // ignore incomplete records
            }
            var subValue = record.subValue ? '.' + record.subValue : '';
            if (record.valueType === 'systemVariable') {
                record.value = '#{' + record.value + '}';
            }
            if (record.valueType === 'localVariable') {
                record.value = '${' + record.value + '}';
            }
            if (record.valueType === 'static' && record.staticDataType === 'Numeric') {
                record.value = parseInt(record.value);  // Ensures Numerics aren't quoted after jsonifying
            }
            if (record.valueType === 'query' && subValue) {
                record.value = record.value + subValue;
            }
            if (record.termsToMerge && record.termsToMerge.length) {
                termsToMerge[record.variable] = sanitizeJsonRecords(record.termsToMerge);
            }
            newJsonObj[record.variable] = record.value;
        }
        return newJsonObj;
    }

    return {
        removeUnsafeSpaceCharacters: function (jsonString) {
            return _removeUnsafeSpaceCharacters(jsonString);
        },
        getTemplateData: function () {
            if (templateData) {
                deferred.resolve(templateData);
            } else {
                var requestUrl = cris.baseUrl + 'templates/?showAllStatus=true&filter={"op":"equal","data":[{"op":"number","data":"statusId","isCol":true},{"op":"number","data":1,"isCol":false}]}&sort( name)';
                dojo.xhrGet({
                    url: requestUrl,
                    handleAs: "text",
                    load: function (data) {
                        var results = dojo.fromJson(data);
                        var templates_ = [];
                        dojo.forEach(results, function (item) {
                            var obj = {name: item.name, id: item['uuid']['$uuid']};
                            var termsXML = dojox.xml.parser.parse(item.content);
                            
                            var docNode = termsXML.documentElement;
                            var termNames = [];
                            templateTerms(docNode, termNames);
                            function templateTerms(node, termNames) {
                                var name = arguments[2];
                                dojo.forEach(node.getElementsByTagName('term'), function (element) {
                                    if (element.parentNode === node) { // restrict to top level elements only. (getElementsByTagName is recursive)
                                        var aliasAttr = element.getAttribute('alias');
                                        if (aliasAttr) { // Reference or Composite Reference term
                                            var termName = (name ? name + '.' + aliasAttr : aliasAttr);
                                            if (element.getElementsByTagName('term').length) { // Composite Reference term
                                                templateTerms(element, termNames, termName);
                                            } else { // Reference term
                                                termNames.push(termName);
                                            }
                                        } else { // Simple or Composite term
                                            var n = element.getElementsByTagName('name');
                                            if (n.length && n[0].parentNode === element) {
                                                var termName = dojox.xml.parser.textContent(n[0]);
                                                if (termName) {
                                                    if (element.getElementsByTagName('term').length) { // Composite Term
                                                        templateTerms(element, termNames, (name ? name + '.' + termName : termName));
                                                    } else { // Simple Term
                                                        termNames.push(name ? name + '.' + termName : termName);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                            termNames = termNames.concat(['_job_id', '_experiment_id', '_project_id']).sort();
                            var termsCollection = [];
                            dojo.forEach(termNames, function (termName) {
                                termsCollection.push({id: termName, name: termName});
                            });
                            obj.termNames = termsCollection;
                            templates_.push(obj);
                        });
                        templateData = templates_;
                        deferred.resolve(templates_);
                    },
                    error: function (error) {
                        deferred.resolve('Error loading templates');
                    }
                });
            }
            return deferred.promise;
        },
        getSystemVariables: function () {
            return [{id: 'current_project.id', name: 'current_project.id'},
                {id: 'current_project.name', name: 'current_project.name'},
                {id: 'current_project.description', name: 'current_project.description'},
                {id: 'current_project.statusId', name: 'current_project.statusId'},
                {id: 'current_project.timeCreated', name: 'current_project.timeCreated'},
                {id: 'current_project.timeUpdated', name: 'current_project.timeUpdated'},
                {id: 'current_experiment.id', name: 'current_experiment.id'},
                {id: 'current_experiment.name', name: 'current_experiment.name'},
                {id: 'current_experiment.description', name: 'current_experiment.description'},
                {id: 'current_experiment.statusId', name: 'current_experiment.statusId'},
                {id: 'current_experiment.timeCreated', name: 'current_experiment.timeCreated'},
                {id: 'current_experiment.timeUpdated', name: 'current_experiment.timeUpdated'},
                {id: 'current_job.id', name: 'current_job.id'},
                {id: 'current_job.name', name: 'current_job.name'},
                {id: 'current_job.description', name: 'current_job.description'},
                {id: 'current_job.statusId', name: 'current_job.statusId'},
                {id: 'current_job.timeCreated', name: 'current_job.timeCreated'},
                {id: 'current_job.timeUpdated', name: 'current_job.timeUpdated'},
                {id: 'current_task.id', name: 'current_task.id'},
                {id: 'current_user.id', name: 'current_user.id'},
                {id: 'current_user.username', name: 'current_user.username'},
                {id: 'current_user.firstName', name: 'current_user.firstName'},
                {id: 'current_user.middleName', name: 'current_user.middleName'},
                {id: 'current_user.lastName', name: 'current_user.lastName'},
                {id: 'current_user.email', name: 'current_user.email'},
                {id: 'current_user.externalId', name: 'current_user.externalId'},
                {id: 'current_user.enabled', name: 'current_user.enabled'},
                {id: 'current_user.timeCreated', name: 'current_user.timeCreated'},
                {id: 'current_user.timeUpdated', name: 'current_user.timeUpdated'},
                {id: 'current_date', name: 'current_date'}, ];
        },
        deserializeJson: function (json, queryItems) {
            console.log('************Deserialize Json');
            var result = [];
            var error = "";

            if (json) {
                var _json = json.trim();
                if (isJsonLike(_json)) {
                    try {
                        var sanitizedJson = eval(_json, queryItems);
                        deserializeQueries(queryItems);
                        var jsonObj = dojo.fromJson(sanitizedJson);
                        
                        var REGEX_TEMPLATE_VARIABLE = /^[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}/i
                        var initializedValues = [];
                        if (jsonObj['$directive']) {
                            if (jsonObj['$directive'] instanceof Array) {
                                for (var key in jsonObj['$data']) {
                                    var value = jsonObj['$data'][key];
                                    if (value instanceof Array) {
                                        initObjectVariables(value, key);
                                    } else {
                                        var valueProperties = initVariableValue(value, key);
                                        initializedValues.push(valueProperties);
                                    }
                                }
                            } else {
                                initObjectVariables(jsonObj['$data']);
                            }
                        } else {
                            for (var key in jsonObj) {
                                var valueProperties = initVariableValue(jsonObj[key], key);
                                initializedValues.push(valueProperties);
                            }
                        }
                        result = result.concat(initializedValues);
                        
                        function initObjectVariables(variableObj, variableId) {
                            var terms = {};
                            var mergeObj= {mergeValues: []};
                            var valProps = {};
                            var noKey = typeof variableId === 'undefined';
                            for (var h in variableObj) {
                                var value = variableObj[h];
                                if (noKey) {
                                    variableId = Object.keys(value)[0];
                                    value = variableObj[h][variableId];
                                }
                                if (REGEX_TEMPLATE_VARIABLE.test(variableId)) {
                                    if (typeof value !== 'object') {
                                        valProps = setValueProperties(value);
                                        valProps.variable = variableId;
                                        valProps.variableType = 'template';
                                        valProps.termsToMerge = [];
                                        initializedValues.push(valProps);
                                    } else {
                                        dojo.mixin(terms, value);
                                    }
                                } else {
                                    /*
                                     * TODO: Add support for non-template merge objects. Also see deserializeJson function
                                     */
                                    mergeObj.mergeValues.push(value);
                                }
                            }
                            if (Object.keys(terms).length) {
                                for (var y in terms) {
                                    var props = setValueProperties(terms[y]);
                                    props.variable = y;
                                    valProps.termsToMerge.push(props);
                                }
                            }
                            if (mergeObj.mergeValues.length) {
                                var props = setValueProperties(mergeObj);
                                props.variable = variableId;
                                props.variableType = 'variable';
                                initializedValues.push(props);
                            }
                        }
                        
                        function initVariableValue(value, variableId) {
                            var variableType = 'variable';
                            if (REGEX_TEMPLATE_VARIABLE.test(variableId)) {
                                variableType = 'template';
                            }
                            var valueProperties = setValueProperties(value);
                            if (variableType === 'template' && typeof value === 'object' && value !== null) {
                                valueProperties.termsToMerge = [];
                                for (var e in value) {
                                    var props= setValueProperties(value[e]);
                                    props.variable = e;
                                    valueProperties.termsToMerge.push(props);
                                }
                                valueProperties.value = null;
                                valueProperties.valueType = null;
                            }
                            dojo.mixin(valueProperties, {variableType: variableType, variable: variableId});
                            return valueProperties;
                        }
                    } catch (e) {
                        console.error('************************ JSON DESERIALIZE ERROR **********************');
                        console.error(e);
                        //error = "Failed to parse json";
                        error = "Unable to extract the Json contents due to a syntax error or features not yet supported by the builder";
                        result = [];
                    }
                } else {
                    error = "Error: Invalid json format";
                }
            }
            return {result: result, error: error};
        },
        fromBuilderToJson: function (builderRecords, task, jsonType, queries) {
            console.log('************From builder to Json');
            var newJsonStr = serializeToJson_(builderRecords, queries);
            ;
            if (jsonType === 'jsonIn') {
                task.jsonIn = newJsonStr;
            } else if (jsonType === 'jsonOut') {
                task.jsonOut = newJsonStr;
            }
        },
        jsonPreview: function (_scope, jsonBuilderKeyValues, queries, queryId) {
            _scope.jsonToPreview = serializeToJson_(jsonBuilderKeyValues, queries, queryId);
            _scope.currentJsonPreview = _scope.jsonToPreview;

            if (!_scope.globalVariableStore) {
                _scope.globalVariableStore = {};
            }
            var variables = [];

            var REGEX_VARIABLES = /\$\{(?![0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}).+\}|\#\{.+\}/g; // Match system or local variables i.e. #{...} or ${...}
            var match;
            while ((match = REGEX_VARIABLES.exec(_scope.jsonToPreview)) !== null) {
                var variable = match[0];
                if (!_scope.globalVariableStore[variable]) {
                    _scope.globalVariableStore[variable] = {variable: variable, value: null};
                }
                var exists = false;
                dojo.forEach(variables, function (v) {
                    if (v.variable === variable) {
                        exists = true;
                    }
                });
                if (!exists) {
                    variables.push(_scope.globalVariableStore[variable]);
                }
            }
            _scope.previewVariables = variables;

            var watcher = _scope.$watch('previewVariables', function (value) {
                _scope.jsonToPreview = _scope.currentJsonPreview;
                for (var i = 0; i < _scope.previewVariables.length; i++) {
                    var variable = _scope.previewVariables[i].variable;
                    var value = _scope.globalVariableStore[variable].value ? '"' + _scope.globalVariableStore[variable].value + '"' : variable;
                    var variablePart = variable.substring(2, variable.length - 1); // remove '${' or '#{' and '}' to extract the actual variable

                    if (variable.startsWith('${')) {
                        var reg = new RegExp('\\$\\{' + variablePart + '\\}', 'g');
                        _scope.jsonToPreview = _scope.jsonToPreview.replace(reg, value);
                    } else if (variable.startsWith('#{')) {
                        var reg = new RegExp('\\#\\{' + variablePart + '\\}', 'g');
                        _scope.jsonToPreview = _scope.jsonToPreview.replace(reg, value);
                    }
                }
            }, true);

            var dialog = new dijit.Dialog({
                title: "Json Preview",
                content: '<div ng-include="\'view_json_preview\'"></div>',
                style: "width: 750px;height:700px;padding-bottom:5px;",
                onCancel: function () {
                    watcher();
                    _scope.$apply();
                }
            });
            $compile(dialog.domNode)(_scope);
            dialog.show();
        }
    }
}]);
