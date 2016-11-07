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

angular.module("crisWorkflow").controller("WorkflowEditorController", ["$scope",  "$http", "$rootScope", "$compile", "$timeout", "$uibModal", "workflowBuilderService", function ($scope, $http, $rootScope, $compile, $timeout, $uibModal, workflowBuilderService) {
    console.log("++++++++ editor init called");
    
    this.errorMessage = null;
    this.isDirty = false;
    this.newTaskPosition = {top: 100, left: 100};

    this.workflow = null;

    this.uploaders = {};
    this.uploaders.tasks = {};

    this.currentEditUT = {};
    this.currentEditST = {};
    this.currentEditRT = {};
    this.currentEditCF = {};
    
    this.filesToUpload = {}; // holds files to "POST" to server
    this.currentZoom = 1;

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
            $uibModal.open({
                template: '<div ng-include="\'view_open_workflow_modal\'"></div>',
                size: 'md',
                scope: $scope,
                controller: function ($scope, $uibModalInstance) {
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss();
                    };
                }
            });
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
            $uibModal.open({
                template: '<div ng-include="\'view_import_workflow_modal\'"></div>',
                size: 'md',
                scope: $scope,
                controller: function ($scope, $uibModalInstance) {
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss();
                    };
                }
            });
        }
    };
    
    // Event emitted by file uploader. Get file-to-upload here
    $scope.$on('FilesToUpload', function (event, data) {
        if (data.uploaderName === 'workflowToUpload') {
            $scope.workflowToUpload = data.fileList[0];
        } else {
            if (data.isMultiple) {
                _this.filesToUpload[data.uploaderName] = data.fileList;
            } else {
                _this.filesToUpload[data.uploaderName] = data.fileList[0];
            }
        }
    });
    
    // Event emitted by file uploader when file(s) to upload are delete
    $scope.$on('FilesRemoved', function (event, data) {
        if (data.uploaderName === 'workflowToUpload') {
            $scope.workflowToUpload = null;
        } else {
            delete _this.filesToUpload[data.uploaderName];
        }
    });

    this.fetchAndInitWorkflow = function(id) {
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
       // TODO: This will replace above cleanup code after we've removed all dojo/dijit references from page
       //$('#container').empty();

        // ... and uploaders
        if (_this.uploaders.theEndPage) {
            angular.forEach(dijit.findWidgets(_this.uploaders.theEndPage), function(w) {
                w.destroyRecursive();
            });
            dojo.destroy(_this.uploaders.theEndPage);
            
            // TODO: This will replace above cleanup code after we've removed all dojo/dijit references from page
            //angular.element(_this.uploaders.theEndPage).remove();
            
            _this.uploaders.theEndPage = null;
        }
        if (_this.uploaders.tasks) {
            angular.forEach(_this.uploaders.tasks, function(task) {
                angular.forEach(dijit.findWidgets(task), function(w) {
                    w.destroyRecursive();
                });
                dojo.destroy(task);
                
                // TODO: This will replace above cleanup code after we've removed all dojo/dijit references from page
                //angular.element(task).remove();
            });
            _this.uploaders.tasks = {};
        }
        
        _this.filesToUpload = {}; // Reset file uploader container
        _this.resetZoom(); // reset zoom

        _this.workflow = new WorkFlow(workflow.id, workflow.name, workflow.key, workflow.uuid, workflow.documentation, workflow.tasks, workflow.flows, workflow.startTaskId, workflow.endTaskIds, 1, workflow.initialDatasetState, workflow.finalDatasetStates, workflow.theEndFile);
        console.dir(_this.workflow);

        // draw start and end nodes
        drawStartAndEndNodes();

        // TheEnd page file
        _this.uploaders.theEndPage = $compile('<cris-file-uploader ng-model="dummyFile" path="theEndFile" is-multiple="false"></cris-file-uploader>')($scope);

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
        this.repaintWorkflow();
    };
    
    this.repaintWorkflow = function () {
        $timeout(function () {
            jsPlumb.repaintEverything();
        });
    };

    this.saveAsWorkflow = function () {
        console.log("======== save as workflow");
        $uibModal.open({
            template: '<div ng-include="\'view_save-as_modal\'"></div>',
            size: 'md',
            scope: $scope,
            controller: function ($scope, $uibModalInstance) {
                $scope.cancel = function () {
                    $uibModalInstance.dismiss();
                };
            }
        });
    };

    this.saveWorkflow = function () {
        console.log("======== save workflow");
        updateWorkflow(_this.workflow);
        console.dir(_this.workflow);
        
        $http({
            method: 'POST',
            url: cris.baseUrl + "workflows/save",
            headers: {'Content-Type': undefined}, // This is needed because we are manually appending file data to the request payload
            transformRequest: function (data) {
                // We need to manually append file data to request data since server handling of file is expecting submission of a form containing the file.
                var formData = new FormData();
                formData.append('workflow', data.workflow);
                for (var name in data.filesToUpload) {
                    if (data.filesToUpload[name].length) {
                        for (var i = 0; i < data.filesToUpload[name].length; i++) {
                            formData.append(name, data.filesToUpload[name][i]);
                        }
                    } else {
                        formData.append(name, data.filesToUpload[name]);
                    }
                }
                return formData;
            },
            // json repsonse is wrapped in <html><body><textarea> tags (I think to support use of the dojo iframe uploader).
            // We need to extract the json from the html tags.
            transformResponse: function (data) {
                if (typeof data === 'string' && data.startsWith('<html><body><textarea>')) {
                    data = JSON.parse(data.replace('<html><body><textarea>', '').replace('</textarea></body></html>', ''));
                }
                return data;
            },
            data: {
                filesToUpload: _this.filesToUpload,
                workflow: angular.toJson(_this.workflow)
            }
        }).then(function(success){
            var response = success.data;
            if (response.error) {
                _this.isDirty = true;
                _this.errorMessage = "Error: " + response.error;
            } else {
                _this.errorMessage = "Workflow: " + response.name + ": saved successfully";
                console.log("Data received");
                console.log(response);
                _this.fetchAndInitWorkflow(response.id);
                _this.isDirty = false;
                
                //reset files-to-upload container
                _this.filesToUpload = {};
            }
        }, function(error){
            console.log("Failed to save changes: " + error.data.message);
            console.dir(error)
             _this.isDirty = true;
            _this.errorMessage = "Failed to save changes: " + error.data.message;
        });
    };

    this.saveWorkflowArchive = function () {
        // send item + file to the backend using PUT/POST
        $http({
            method: 'POST',
            url: cris.baseUrl + "workflows/import",
            headers: {'Content-Type': undefined}, // This is needed because we are manually appending file data to the request payload
            transformRequest: function (data) {
                // We need to manually append file data to request data since server handling of file is expecting submission of a form containing the file.
                var formData = new FormData();
                formData.append('file', data.file);
                return formData;
            },
            // json repsonse is wrapped in <html><body><textarea> tags (I think to support use of the dojo iframe uploader).
            // We need to extract the json from the html tags.
            transformResponse: function (data) {
                if (typeof data === 'string' && data.startsWith('<html><body><textarea>')) {
                    data = JSON.parse(data.replace('<html><body><textarea>', '').replace('</textarea></body></html>', ''));
                }
                return data;
            },
            data: {
                file: $scope.workflowToUpload
            }
        }).then(function(success){
            var response = success.data;
            if (response.error) {
                _this.errorMessage = "Error: " + response.error;
            } else {
                console.log("Data received");
                console.log(response);
                _this.fetchAndInitWorkflow(response.id);
                _this.isDirty = false;
                
                // reset workflow-to-upload reference
                _this.workflowToUpload = null;
            }
        }, function(error){
            _this.errorMessage = "Error: " + error.data.message;
        });
    };

    this.renameAndSaveWorkflow = function (name, description) {
        console.log("-------- renameAndSaveWorkflow");
        console.log("name: " + name);
        console.log("description: " + description);
        console.dir(_this.workflow);

        _this.workflow.id = null;
        _this.workflow.uuid = null;
        _this.workflow.key = null;
        _this.workflow.name = name;
        _this.workflow.description = description;
        _this.saveWorkflow();
    };
    
    this.initEndFileUploader = function () { // load task end-file
        angular.element('#idTheEndPageFile').html(_this.uploaders.theEndPage);
    };

    this.editWorkflowMetadata = function () {
        console.log("******** entering editWorkflowMetadata...");
        
        $uibModal.open({
            template: '<div ng-include="\'view_edit_metadata_modal\'"></div>',
            size: 'md',
            scope: $scope,
            controller: function ($scope, $uibModalInstance) {
                $scope.cancel = function () {
                    $uibModalInstance.dismiss();
                };
            }
        });
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

        var task = new ServiceTask("System Task", "Please provide a name", "Please provide documentation", true, "", "", "", "", "", false, "", "", "0", [], "normal", 70 + cris.workflow.app.topIncrement, 70 + cris.workflow.app.topIncrement);
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

    };

    this.editServiceTask = function (task) {

    };

    this.editConditionalBranch = function (task) {

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
            _this.uploaders.tasks[key + "_html"] = $compile('<cris-file-uploader ng-model="dummyFile" path="' + key + '_html" is-multiple="false"></cris-file-uploader>')($scope)[0];
        } else if (task.taskType === "exclusiveGateway") {
            if (!task.conditionExpression) {
                task.conditionExpression = "isValid == true";
            }
        }

        if (!task.isSubCondition) { // skip sub-condition tasks. They are drawn when their parent (multi-condition task) is drawn.
            drawTask(key, task);
        }

        // file(s) used by the task
        this.uploaders.tasks[key] = $compile('<cris-file-uploader ng-model="dummyFile" path="' + key + '" is-multiple="true"></cris-file-uploader>')($scope)[0];
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

        // TODO: This will replace above cleanup code after we've removed all dojo/dijit references from page
        // var domNode = document.getElementById(task.id);
        // angular.element(domNode).remove();

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
            
        } else if (taskType === "exclusiveGateway") {
            if (!task.isMultiCondition) {
                
            } else {
                
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
                $http({
                    url: cris.baseUrl + "templates/" + +templateId,
                    method: "GET"
                }).then(function (success) {
                   var response = success.data;
                   task.ui_page = response.uuid.$uuid;
                }, function (error) {
                    _this.errorMessage = error;
                });
            }
        } else {
            // custome page: get from upload file name
            var customFile = _this.filesToUpload[task.id + "_html"];
            if (customFile) {
                task.ui_page = customFile.name;
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
    };

    this.closeConditionBranchDialog = function (task) {
        console.dir(task);

        // refresh UI
        drawTask(task.id, task);
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
    };
    
    this.resetZoom = function () {
        _this.currentZoom = 1;
        $("#container").css({
                            "-webkit-transform":"scale(1) translateY(0)",
                            "-moz-transform":"scale(1) translateY(0)",
                            "-ms-transform":"scale(1) translateY(0)",
                            "-o-transform":"scale(1) translateY(0)",
                            "transform":"scale(1) translateY(0)"
                        });
        jsPlumb.setZoom(_this.currentZoom);
    }
    
    this.zoomIn = function () {
        if (_this.currentZoom < 1) {
            _this.currentZoom += 0.05;
            
            // We need traslate to maintain the zoomed-out container's top position
            var translate = Math.round((1 - _this.currentZoom) * 100) / 2;
            
            $("#container").css({
                            "-webkit-transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)",
                            "-moz-transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)",
                            "-ms-transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)",
                            "-o-transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)",
                            "transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)"
                        });
            jsPlumb.setZoom(_this.currentZoom);
        }
    };
    
    this.zoomOut = function () {
        if (_this.currentZoom > 0.7) {
            _this.currentZoom -= 0.05;
            
            // We need traslate to maintain the zoomed-out container's top position
            var translate = Math.round((1 - _this.currentZoom) * 100) / 2;
            
            $("#container").css({
                                "-webkit-transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)",
                                "-moz-transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)",
                                "-ms-transform":"scale(" + _this.currentZoom + ") translateY(-" + translate+ "%)",
                                "-o-transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)",
                                "transform":"scale(" + _this.currentZoom + ") translateY(-" + translate + "%)"
                            });
        }
        jsPlumb.setZoom(_this.currentZoom);
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
                _task.jsonIn = workflowBuilderService.setJsonOptions(_task.jsonIn, _task, 'jsonIn');
            }
            if (_task.jsonOut) {
                _task.jsonOut = _task.jsonOut.replace(/\n/g, '');
                _task.jsonOut = workflowBuilderService.removeUnsafeSpaceCharacters(_task.jsonOut);
                _task.jsonOut = workflowBuilderService.setJsonOptions(_task.jsonOut, _task, 'jsonOut');
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
        dojo.place(node, dojo.byId("container"));
        jsPlumb.addEndpoint(node, sourceEndpoint, {anchor: "RightMiddle", uuid: "Start"});

        // end event
        node = dojo.create("div", {
            "id": "end",
            "className": "shape",
            "data-shape": "CircleEnd"
        }, "container");
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

                            $uibModal.open({
                                template: '<div ng-include="\'view_report_edit_modal\'"></div>',
                                size: 'md',
                                scope: $scope,
                                controller: function ($scope, $uibModalInstance) {
                                    $scope.modalTitle = "Edit Report Task: " + task.name;
                                    $scope.cancel = function () {
                                        $uibModalInstance.dismiss();
                                    };
                                }
                            });
                            break;
                        case "exclusiveGateway":
                            _this.currentEditCF = task;
                            $scope.$apply();
                            
                            var modalTitle = "";
                            var modalTtemplate = "";
                            if (task.isMultiCondition) {
                                modalTitle = "Edit Switch Branch: " + task.name;
                                modalTtemplate = '<div ng-include="\'view_multi-condition_branch_modal\'"></div>';
                            }else if (task.isSubCondition) {
                                return;
                            } else {
                                modalTitle = "Edit Condition Branch: " + task.name;
                                modalTtemplate = '<div ng-include="\'view_condition_branch_modal\'"></div>';
                            }
                            
                            $uibModal.open({
                                template: modalTtemplate,
                                size: 'md',
                                scope: $scope,
                                controller: function ($scope, $uibModalInstance) {
                                    $scope.modalTitle = modalTitle;
                                    $scope.cancel = function () {
                                        $uibModalInstance.dismiss();
                                    };
                                }
                            });
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
        
        require(["dojo/on"], function(on){
            on(window, 'resize', function() {
                jsPlumb.repaintEverything();
            });
        });
        
    });
}]);

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
            scope.jsonViewType = {};
            scope.$watch("task.id", function (newValue, oldValue) {
                if (newValue && newValue !== oldValue) {
                    scope.deserializeJson();
                    if (scope.item && !scope.error) {
                        scope.serializeToJson();
                    }
                }
            });
            
            $rootScope.$on('onTaskDetailsOpened', function (event, taskId) {
                $timeout(function () {
                    if (scope.task.id === taskId && !scope.error) {
                        console.log('************* BROADCAST HANDLING - onTaskDetailsOpened *************');
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
                var jsonStr = scope.task[scope.type];
                if (!(/\n/.test(jsonStr))) { // If no \n character, prettify the json
                    scope.serializeToJson();
                }
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
                var val = workflowBuilderService.deserializeJson(json, $scope.queries, $scope.task, $scope.type);
                if (val.error) {
                    $scope.error = val.error;
                } else {
                    $scope.error = ""; // Clear previous error
                }
                $scope.item = val.result;
                
                // Get the view type (advanced or builder) from the $options object
                // Default = builder. If no $options object but json exists, default = advanced
                if ((val.$options && val.$options._advanced) || ((!val.$options || typeof val.$options._advanced === 'undefined') && $scope.task[$scope.type])) {
                    $scope.jsonViewType[$scope.type] = 'advanced';
                } else {
                    $scope.jsonViewType[$scope.type] = 'builder';
                }
                
                // Let task remember $options like view type
                if (!$scope.task[$scope.type + '_$options']) {
                    $scope.task[$scope.type + '_$options'] = {};
                }
                $scope.task[$scope.type + '_$options']._advanced = ($scope.jsonViewType[$scope.type] === 'advanced');

                $scope.queryAliases = [];
                dojo.forEach($scope.queries, function (q) {
                    $scope.queryAliases.push({id: q.key, name: q.key, query: q});
                });
            };
            
            $scope.viewTypeChange = function (viewType) {
                $scope.jsonViewType[$scope.type] = viewType;
                $scope.task[$scope.type + '_$options']._advanced = (viewType === 'advanced');
            };
            
            $scope.getTermNames = function(templateUUID, parentTerm) {
                var termNames = [];
                if ($scope.termsByTemplate && $scope.termsByTemplate[templateUUID]) {
                    termNames = $scope.termsByTemplate[templateUUID];
                } else {
                    for (var i = 0; i < $scope.templates.length; i++) {
                        if ($scope.templates[i].id === templateUUID) {
                            termNames = termNames.concat($scope.templates[i].termNames);
                            for (var j = termNames.length - 1; j > -1; j--) {
                                if (['_job_id', '_experiment_id', '_project_id'].indexOf(termNames[j].id) !== -1) {
                                    termNames.splice(j, 1);
                                }
                            }
                            termNames.unshift({id: '_id', name: '_id'});
                            
                            if (!$scope.termsByTemplate) {
                                $scope.termsByTemplate = {};
                            }
                            $scope.termsByTemplate[templateUUID] = termNames
                            
                            break;
                        }
                    }
                }
                
                if (parentTerm) { // Get sub terms of composite term
                    var subTerms = [];
                    for (var h = 0; h < termNames.length; h++) {
                        
                        if (termNames[h].id === parentTerm && termNames[h].dataType !== 'Composite') { // data-type must be "Composite" for term to have sub-terms
                            return null;
                            break;
                        }
                        
                        if (termNames[h].id.startsWith(parentTerm + '.')) {
                            var termName = termNames[h].id.substring(parentTerm.length + 1, termNames[h].id.length); // +1 accounts for '.'
                            var o = {id: termName, name: termName};
                            workflowBuilderService.setTermProperties(o, termNames[h]);
                            subTerms.push(o);
                        }
                    }
                    return subTerms;
                }
                
                return termNames;
            };
            
            $scope.getAttachToData = function (templateUUID, term) {
                var data = {};
                if ($scope.attachToDataByTemplate && $scope.attachToDataByTemplate[templateUUID]) {
                    data = $scope.attachToDataByTemplate[templateUUID][term];
                } else {
                    for (var i = 0; i < $scope.templates.length; i++) {
                        if ($scope.templates[i].id === templateUUID && $scope.templates[i].attachToData) {
                            data = $scope.templates[i].attachToData;
                            break;
                        }
                    }
                }
                return data;
            };
            
            $scope.termIsValid = function (term, templateUUID) {
                var isInvalidTerm = true;
                if (term.itemType === "Term" && $scope.termsByTemplate && $scope.termsByTemplate[templateUUID]) {
                    var termNames = $scope.termsByTemplate[templateUUID];
                    for (var j = 0; j < termNames.length; j++) {
                        if (termNames[j].id === term.variable) {
                            isInvalidTerm = false;
                        }
                    }
                }
                return isInvalidTerm;
            };
            
            $scope.templateIsValid = function (templateUUID) {
                var result = false;
                if (templateUUID && $scope.templates && $scope.templates.length) {
                    for (var i = 0; i < $scope.templates.length; i++) {
                        if ($scope.templates[i].id === templateUUID) {
                            result = true;
                            break;
                        }
                    }
                } else {
                    result = true;
                }
                return result;
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
                // If deserialization error exists, do not serialize again. This prevents json string from being automatically.
                // (Any existing json strings, with errors or not, should not be deleted)
                //.....
                // Only serialize if view is not advanced. In advanced view it is up to user to manage json
                if ($scope.error === "" && !$scope.task[$scope.type + '_$options']._advanced) {
                    workflowBuilderService.fromBuilderToJson($scope.item, $scope.task, $scope.type, $scope.queries);
                }
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
            $scope.onJsonBuilderEdit = function (index, termItem, templateUUID) {
                $timeout(function () { // timeout needed because after onBlur event, updated value is not yet available
                    if (typeof index === 'number') {
                        var value = $scope.item[index].value;
                        var termsToMerge = $scope.item[index].termsToMerge;
                        if ((value !== null && typeof value !== 'undefined') || $scope.item[index].staticDataType === "Null" || ($scope.item[index].variableType === 'template' && termsToMerge && termsToMerge.length)) {
                            $scope.$emit('onJsonBuilderEdit');
                        }
                    } else {
                        
                        // Set edited record's term properties
                        if (termItem && templateUUID && termItem.variable) {
                            var terms = $scope.termsByTemplate[templateUUID];
                            if (terms) {
                                for (var t = 0; t < terms.length; t++) {
                                    if (terms[t].id === termItem.variable) {
                                        workflowBuilderService.setTermProperties(termItem, terms[t]);
                                        break;
                                    }
                                }
                            }
                        }
                        
                        $scope.$emit('onJsonBuilderEdit');
                    }
                }, 300);
            };

            $scope.fetchTemplates = function () {
                var t = workflowBuilderService.getTemplateData();
                t.promise.then(function(result){
                    t.thenCallback(result, $scope); // Templates array will be added to $scope in the callback
                }, function (error) {
                    $scope.templates = [];
                });
            };
        }
    }
});

angular.module("crisWorkflow").directive("crisWorkflowVariableTypes", ["$uibModal", function ($uibModal) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            item: '=',
            queries: '@',
            subTerms: '@', // child terms of a composite term
            checkValidity: '&',
            attachToData: '@'
        },
        template: '<div ng-include="\'view_builder_variable_types\'"></div>',
        link: function (scope, element, attrs) {
            scope.fetchSystemVariables();
            
            scope.$watch('item', function(newValue, oldValue){
                scope.previousValue = oldValue.value;
                scope.previousValueType = oldValue.valueType;
                scope.previousStaticDataType = oldValue.staticDataType;
                
                if (scope.objectToPreview && (typeof scope.item.value === 'object')) { // If value is object or Array, generate json to preview
                    scope.convertObjectValueToJson();
                }
            }, true);
            
            // update staticDataType when switching terms (within same template). Each term in template has an index.
            scope.$watchCollection('[item.index, item.template]', function (newValue, oldValue) {
                if (newValue[0] !== oldValue[0] && newValue[1] === oldValue[1]) {
                    //console.log(newValue[0] + '-' + oldValue[0] + ' >>>> ' + newValue[1] + '-' + oldValue[1])

                    if (['_experiment_id', '_job_id', '_project_id'].indexOf(scope.item.term) !== -1) {
                        scope.item.valueType = "systemVariable";
                    } else {
                        scope.item.valueType = "static";
                    }
                    
                    var staticDataType = scope.getStaticDataType();
                    
                    if (staticDataType !== scope.item.staticDataType) {
                        scope.item.value = null;
                        scope.item.subValue = null;
                    }
                    scope.item.staticDataType = staticDataType;
                }
            });
        },
        controller: function($scope, $timeout, workflowBuilderService) {
            $scope.fetchSystemVariables = function () {
                $scope.systemVariables = workflowBuilderService.getSystemVariables();
            };
            
            $scope.onJsonBuilderEdit = function (type) {
                $timeout(function () {
                    if (type === 'valueType' && $scope.item.valueType !== $scope.previousValueType) {
                        $scope.item.value = null;
                        $scope.item.staticDataType = $scope.item.dataType ? $scope.getStaticDataType() : null;
                        $scope.item.subValue = null;
                    } else if (type === 'value' && $scope.item.value !== $scope.previousValue) {
                        $scope.item.subValue = null;
                    } else if (type === 'staticDataType' && $scope.item.staticDataType !== $scope.previousStaticDataType) {
                        $scope.item.value = null;
                    }
                    
                    $scope.$emit('onJsonBuilderEdit');
                }, 300);
            };
            
            $scope.getStaticDataType = function () {
                // A List term (dropdown) could be a Multi-Select...therefore staticDataType should be an Array of List values
                // Likewise, a File term could be a Multi-File, and therefore staticDataType should be an Array of File values
                // And...any term could be a list term. In that case staticDataType will be an Array. Array will contain instances of term.
                var staticDataType = null;
                if (($scope.item.dataType === 'List' && $scope.item.listItemProps && $scope.item.listItemProps.isMultiSelect) || ($scope.item.dataType === 'File' && $scope.item.isMultiFile) || $scope.item.isListTerm) {
                    staticDataType = 'Array';
                } else {
                    staticDataType = $scope.item.dataType;
                }
                return staticDataType;
            };
            
            $scope.termNames = function () {
                var termNames = [];
                if ($scope.queries) {
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
                }
                return termNames;
            };
            
            // Edit/View object or array value
            $scope.editValue = function (objectType) {
                if (!$scope.item.value || !($scope.item.value instanceof Array)) {
                    $scope.item.value = [];
                }
                
                $scope.item.objectType = objectType; // Array or Object
                $scope.convertObjectValueToJson();
                $uibModal.open({
                    template: '<div ng-include="\'view_builder_edit_value\'"></div>',
                    size: 'lg',
                    scope: $scope,
                    controller: function ($scope, $uibModalInstance) {
                        $scope.close = function () {
                            $uibModalInstance.close();
                        };
                    }
                });
            };
            
            $scope.addObjectItem = function () {
                $scope.item.value.push({variable: null, value: {}});
            };
            
            $scope.addArrayItem = function () {
                if (!$scope.item.value) {
                    $scope.item.value = [];
                }
                
                var o = {};
                if ($scope.item.dataType === 'List' || $scope.item.dataType === 'File' || $scope.item.isListTerm) {
                    if ($scope.item.dataType === 'List') {
                        o.listItemProps = {items: $scope.item.listItemProps.items};
                    }
                    o.dataType = $scope.item.dataType;
                    o.staticDataType = $scope.item.dataType;
                    o.valueType = 'static';
                    o.isReadOnly = true;
                    o.term = ($scope.item.term || $scope.item.variable);
                }
                
                $scope.item.value.push(o);
            };
            
            $scope.removeObjectItem = function ($index) {
                $scope.item.value.splice($index, 1);
            };
            
            $scope.convertObjectValueToJson = function () { // Convert object/array to json for preview
                $scope.objectToPreview = workflowBuilderService.objectToJson($scope.item.value, $scope.item.objectType);
            };
            
            // Get child terms of composite term (parentTerm)
            $scope.getSubTerms = function (parentTerm) {
                if ($scope.subTerms) {
                    var subTerms = [];
                    var terms = eval($scope.subTerms);
                    angular.forEach(terms, function (t) {
                        if (t.id.startsWith(parentTerm + '.')) {
                            var termName = t.id.substring(parentTerm.length + 1, t.id.length); // +1 accounts for '.'
                            var o = {id: termName, name: termName};
                            workflowBuilderService.setTermProperties(o, t);
                            subTerms.push(o);
                        }
                    });
                    return subTerms;
                } else {
                    return null;
                }
            };
            
            // On Object Edit, set the value properties based on properties of selected term
            $scope.onObjectEdit = function (record) {
                $timeout(function () { // timeout necessary since onBlur values haven't yet updated
                    if ($scope.subTerms) {
                        var terms = eval($scope.subTerms);
                        for (var y = 0; y < terms.length; y++) {
                            if (terms[y].id === record.variable) {
                                workflowBuilderService.setTermProperties(record.value, terms[y]);
                                break;
                            }
                        }
                    }
                    $scope.$emit('onJsonBuilderEdit');
                }, 500);
            }
            
            $scope.termIsValid = function (termName) {
                var isValid = false;
                if (termName) {
                    var terms = eval($scope.subTerms);
                    for (var y = 0; y < terms.length; y++) {
                        if (terms[y].id === termName) {
                            isValid = true;
                            break;
                        }
                    }
                }
                return isValid;
            }
            
            // For term object values each record needs a reference to the selected record term
            $scope.objectTermChanged = function (recordValue, variable) {
                //recordValue.term = ($scope.item.variable ? $scope.item.variable + '.' + variable : variable);
                recordValue.term = ($scope.item.term ? $scope.item.term + '.' + variable : variable);
            };
            
            $scope.getAttachToData = function (itemId) {
                var attachToData = [];
                if ($scope.attachToData) {
                    var data = JSON.parse($scope.attachToData);
                    if (data[itemId]) {
                        attachToData = data[itemId];
                    }
                }
                return attachToData;
            };
            
            $scope.getStaticDataTypes = function () {
                var types = [{id: 'Boolean', name: 'Boolean'},
                            {id: 'Date', name: 'Date'},
                            {id: 'Numeric', name: 'Numeric'},
                            {id: 'Text', name: 'Text'},
                            {id: 'Array', name: 'Array'},
                            {id: 'Object', name: 'Object'},
                            {id: 'Time', name: 'Time'},
                            {id: 'Null', name: 'Null'}];
                
                // Term specific types
                if ($scope.item.dataType === 'List') {
                    types.push({id: 'List', name: 'List'});
                } else if ($scope.item.dataType === 'Composite') {
                    types.push({id: 'Composite', name: 'Composite'});
                } else if ($scope.item.dataType === 'AttachTo') {
                    types.push({id: 'AttachTo', name: 'AttachTo'});
                } else if ($scope.item.dataType === 'File') {
                    types.push({id: 'File', name: 'File'});
                }
                
                return types;
            };
            
            // Check if value has wrong data type that doesn't match term definition data type
            $scope.valueDataTypeIsValid = function (value, termStaticDataType) {
                var isValid = false;
                if (typeof value !== 'undefined' && value !== null && termStaticDataType) {
                    var dataType = "";
                    if (typeof value === 'number' && !isNaN(value)) {
                        dataType = 'Numeric|AttachTo';
                    } else if (typeof value !== 'object' && isNaN(value) && !isNaN(Date.parse(value))) {
                        dataType = 'Date|Time';
                    } else if (typeof value === 'boolean') {
                        dataType = 'Boolean';
                    } else if (value === null) {
                        dataType = "Null";
                    } else if (typeof value === 'string') {
                        dataType = "Text|List|File|AttachTo";
                    } else if (value instanceof Array) {
                        dataType = 'Array|List|File|Object|Composite';
                    }
                    isValid = (dataType.indexOf(termStaticDataType) !== -1);
                    
                    if (!isValid) {
                        //$scope.item.value = null;
                    }
                }
                return isValid;
            };
            
            // Check variable (term, system variables, etc.) against list of valid values
            $scope.valueIsValid = function (value, list) {
                var isValid = false;
                if (value && list && list instanceof Array) {
                    angular.forEach(list, function(item) {
                        if (item.id === value) {
                            isValid = true;
                        }
                    });
                } else {
                    isValid = true;
                }
                // update parent's isValid flag
                $scope.item.isValid = isValid;
                
                // Tell parent to check if all children are valid
                if ($scope.checkValidity) {
                    $scope.checkValidity();
                }
                
                return isValid;
            };
        }
    }
}]);

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
            scope.queryValidator = {}; // Holds validation flags for the query
            
            scope.$watchCollection('queries', function (val) {
                if (val) {
                    scope.queryAliases = [];
                    dojo.forEach(scope.queries, function (q) {
                        scope.queryAliases.push({id: q.key, name: q.key, query: q});
                    });
                    if (val.length === 0 || !scope.queryAdded) { // only hide query details section when switching between tasks
                        scope.isQuerySelected = false;
                    }
                    scope.queryAdded = false;
                    
                    // Reset selected query color when switching between tasks
                    if (scope.previousSelection) {
                        scope.previousSelection.style.backgroundColor = '#FFF';
                    }
                    scope.validateQueries();
                }
            });
            
            scope.$watch('selectedQuery.templateUUID', function (newValue, oldValue) {
                if (scope.selectedQuery && newValue) {
                    var templateIsValid = false;
                   
                    for (var i = 0; i < scope.templates.length; i++) {
                        if (scope.templates[i].id === scope.selectedQuery.templateUUID) {
                            scope.selectedQuery.termNames = scope.templates[i].termNames;
                            scope.selectedQuery.attachToData = scope.templates[i].attachToData;
                            templateIsValid = true;
                            break;
                        }
                    }
                   
                    if (!templateIsValid) {
                        scope.invalidTemplateError = "*The referenced template is invalid";
                    } else {
                        scope.invalidTemplateError = "";
                    }
                }
            });

            scope.$watch('selectedQuery.addCurrentJobFilter', function (newValue, oldValue) { // current job toggle
                if (scope.selectedQuery && newValue) {
                    if (!scope.hasCurrentJobTerm()) { // Add current job term if non exists
                        scope.addQueryWhere({term: '_job_id', value: 'current_job.id', valueType: 'systemVariable', queryOperator: '$eq'});
                    }
                } else if (scope.selectedQuery && !newValue && oldValue) { // toggle off. Remove current job term
                    for (var i = scope.selectedQuery.where.length - 1; i >= 0; i--) {
                        var orGroup = scope.selectedQuery.where[i].orGroup;
                        for (var j = orGroup.length - 1; j >= 0; j--) {
                            if (orGroup[j].term === '_job_id' && orGroup[j].value === 'current_job.id') {
                                orGroup.splice(j, 1);
                            }
                        }
                        if (orGroup.length === 0) { // If group is empty, also remove where item (remember, actual where records are contained in the group array)
                            scope.selectedQuery.where.splice(i, 1);
                        }
                    }
                    scope.onQueryEdit(); // Notify json builder of update
                }
            });

            scope.$on('refreshQueries', function (event, data) {
                console.log('********** BROADCAST HANDLING - refreshQueries ***************');
                $timeout(function () {
                    if (scope.isQuerySelected && scope.queries[scope.selectedQueryIndex]) {
                        scope.selectedQuery = scope.queries[scope.selectedQueryIndex];
                    }
                    scope.validateQueries();
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
                if (!$scope.hasCurrentJobTerm()) {
                    $scope.selectedQuery.addCurrentJobFilter = false;
                }
                $scope.selectedQuery.where.splice(index, 1);
                $scope.onQueryEdit();
            };

            $scope.addQueryWhere = function (item) {
                $scope.selectedQuery.where.push({orGroup: [item ? item : {term: null, value: null}]});
                if (item) {
                    $scope.onQueryEdit(); // Notify json builder of update
                }
            };

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
                $scope.queryAdded = true;
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
            
            $scope.validateQueries = function () {
                angular.forEach($scope.queries, function(query) {
                    $scope.queryValidator[query.key] = {isValid: true};

                    // Initialize validator objects for all query sections: sort, distinct, operators (e.g. $gt, $eq, etc.) termnames, etc.
                    $scope.queryValidator[query.key].queryOperatorValidator = {};
                    $scope.queryValidator[query.key].termNameValidator = {};
                    $scope.queryValidator[query.key].projectionTermNameValidator = {};
                    $scope.queryValidator[query.key].projectionValueValidator = {};
                    $scope.queryValidator[query.key].distinctTermNameValidator = {};
                    $scope.queryValidator[query.key].distinctValueValidator = {};
                    $scope.queryValidator[query.key].sortTermNameValidator = {};
                    $scope.queryValidator[query.key].sortValueValidator = {};
                    $scope.queryValidator[query.key].groupItemTermNameValidator = {};
                    $scope.queryValidator[query.key].groupItemValueValidator = {};

                    // validate all sections of query: operators, termnames, etc.
                    var allTermsValid = true;
                    var allWhereItemsValid = true;
                    var allQueryOperatorsValid = true;
                    var allProjectionItemsValid = true;
                    var allDistinctItemsValid = true;
                    var allSortItemsValid = true;
                    var allGroupItemsValid = true;

                    // validate term names and query operators in the where section
                    angular.forEach(query.where, function (item, parentIndex) {
                        angular.forEach(item.orGroup, function (groupItem, index) {
                            var valid = $scope.termNameIsValid(query.termNames, groupItem.term);

                            if (!valid) {
                                allTermsValid = false;
                            }

                            // isValid is set in  crisWorkflowVariableTypes for each whereItem value
                            if (groupItem.isValid === false) {
                                allWhereItemsValid = false;
                            }

                            // validate term names
                            $scope.queryValidator[query.key].termNameValidator[parentIndex + '-' + index] = {};
                            $scope.queryValidator[query.key].termNameValidator[parentIndex + '-' + index].isValid = valid;

                            // validate query operators
                            //var operatorIsValid = (['$eq','$gt','$gte','$lt','$lte','$ne','$in','$nin'].indexOf(groupItem.queryOperator) !== -1);
                            var operatorList = $scope.getQueryOperators(groupItem.dataType);
                            var validOperators = [];
                            for (var k in operatorList) {
                                validOperators.push(operatorList[k].id)
                            }
                            var operatorIsValid = (validOperators.indexOf(groupItem.queryOperator) !== -1);
                            
                            if (!operatorIsValid) {
                                allQueryOperatorsValid = false;
                            }
                            $scope.queryValidator[query.key].queryOperatorValidator[parentIndex + '-' + index] = {};
                            $scope.queryValidator[query.key].queryOperatorValidator[parentIndex + '-' + index].isValid = operatorIsValid;

                            if (groupItem.term === '_job_id' && groupItem.value === 'current_job.id') {
                                query.addCurrentJobFilter = true;
                            }
                        });
                    });

                    // validate projection items
                    dojo.forEach(query.projectionItems, function (item, $index) {
                        var valid = $scope.termNameIsValid(query.termNames, item.term);

                        // validate term names
                        $scope.queryValidator[query.key].projectionTermNameValidator[$index] = {};
                        $scope.queryValidator[query.key].projectionTermNameValidator[$index].isValid = valid;

                        // validate values
                        var valueIsValid = ([true,false].indexOf(item.project) !== -1);
                        $scope.queryValidator[query.key].projectionValueValidator[$index] = {};
                        $scope.queryValidator[query.key].projectionValueValidator[$index].isValid = valueIsValid;

                        allProjectionItemsValid = (allProjectionItemsValid && valid && valueIsValid);
                    });

                    // validate distinct items
                    dojo.forEach(query.distinctItems, function (item, $index) {
                        var valid = $scope.termNameIsValid(query.termNames, item.term);

                        // validate term names
                        $scope.queryValidator[query.key].distinctTermNameValidator[$index] = {};
                        $scope.queryValidator[query.key].distinctTermNameValidator[$index].isValid = valid;

                        // validate values
                        var valueIsValid = ([true,false].indexOf(item.isDistinct) !== -1);
                        $scope.queryValidator[query.key].distinctValueValidator[$index] = {};
                        $scope.queryValidator[query.key].distinctValueValidator[$index].isValid = valueIsValid;

                        allDistinctItemsValid = (allDistinctItemsValid && valid && valueIsValid);
                    });

                    // validate sort items
                    dojo.forEach(query.sortItems, function (item, $index) {
                        var valid = $scope.termNameIsValid(query.termNames, item.term);

                        // validate term names
                        $scope.queryValidator[query.key].sortTermNameValidator[$index] = {};
                        $scope.queryValidator[query.key].sortTermNameValidator[$index].isValid = valid;

                        // validate values
                        var valueIsValid = ([1,-1].indexOf(item.order) !== -1);
                        $scope.queryValidator[query.key].sortValueValidator[$index] = {};
                        $scope.queryValidator[query.key].sortValueValidator[$index].isValid = valueIsValid;

                        allSortItemsValid = (allSortItemsValid && valid && valueIsValid);
                    });

                    // validate group items
                    dojo.forEach(query.groupItems, function (item, $index) {
                        var valid = $scope.termNameIsValid(query.termNames, item.term);

                        // validate term names
                        $scope.queryValidator[query.key].groupItemTermNameValidator[$index] = {};
                        $scope.queryValidator[query.key].groupItemTermNameValidator[$index].isValid = valid;

                        // validate values
                        var valueIsValid = (['$sum','$avg','$first','$last','$max','$min','$stdDevPop','$stdDevSamp'].indexOf(item.operator) !== -1);
                        $scope.queryValidator[query.key].groupItemValueValidator[$index] = {};
                        $scope.queryValidator[query.key].groupItemValueValidator[$index].isValid = valueIsValid;

                        allGroupItemsValid = (allGroupItemsValid && valid && valueIsValid);
                    });

                    // Set the global isValid flag for the query. Any invalid section marks the entire query as invalid
                    $scope.queryValidator[query.key].isValid = (allTermsValid && allWhereItemsValid && allQueryOperatorsValid && allProjectionItemsValid && allDistinctItemsValid && allSortItemsValid && allGroupItemsValid);
                });
            };

            // Notify json builder of changes.....
            $scope.onQueryEdit = function (groupItem) {
                $timeout(function () {
                    if ((groupItem && groupItem.value) || !groupItem) {
                        $scope.$emit('onQueryEdit');
                        
                        // Toggle current job checkbox based on whether a current job term (_job_id) exists
                        if ($scope.hasCurrentJobTerm()) {
                            $scope.selectedQuery.addCurrentJobFilter = true;
                        } else {
                            $scope.selectedQuery.addCurrentJobFilter = false;
                        }
                    }
                    $scope.validateQueries();
                }, 300);
            };
            
            $scope.getQueryOperators = function (dataType) {
                if (dataType === "Numeric") {
                    return [{id: '$eq', name: 'Equal To'},
                            {id: '$gt', name: 'Greater Than'},
                            {id: '$gte', name: 'Greater Than or Equal To'},
                            {id: '$lt', name: 'Less Than'},
                            {id: '$lte', name: 'Less Than or Equal To'},
                            {id: '$ne', name: 'Not Equal To'}];
                } else if (["Text", "Boolean", "Date", "Time", "Object", "Composite", "AttachTo", "File", "List"].indexOf(dataType) !== -1) {
                    return [{id: '$eq', name: 'Equal To'},{id: '$ne', name: 'Not Equal To'}];
                } else if (dataType === "Array") {
                    return [{id: '$eq', name: 'Equal To'},
                            {id: '$ne', name: 'Not Equal To'},
                            {id: '$in', name: 'In'},
                            {id: '$nin', name: 'Not In'}];
                } else {
                    return [{id: '$eq', name: 'Equal To'},
                            {id: '$gt', name: 'Greater Than'},
                            {id: '$gte', name: 'Greater Than or Equal To'},
                            {id: '$lt', name: 'Less Than'},
                            {id: '$lte', name: 'Less Than or Equal To'},
                            {id: '$ne', name: 'Not Equal To'},
                            {id: '$in', name: 'In'},
                            {id: '$nin', name: 'Not In'}];
                }
            };

            $scope.previewQuery = function () {
                workflowBuilderService.jsonPreview($scope, null, $scope.queries, $scope.selectedQuery.key);
            };

            $scope.fetchTemplates = function () {
                var t = workflowBuilderService.getTemplateData();
                t.promise.then(function(result){
                    t.thenCallback(result, $scope);
                }, function (error) {
                    $scope.templates = [];
                });
            };
            
            $scope.addSortItem = function () {
                if (!$scope.selectedQuery.sortItems) {
                    $scope.selectedQuery.sortItems = [];
                }
                $scope.selectedQuery.sortItems.push({});
            };
            
            $scope.removeSortItem = function (index) {
                $scope.selectedQuery.sortItems.splice(index, 1);
                $scope.onQueryEdit();
            };
            
            $scope.addDistinctItem = function () {
                if (!$scope.selectedQuery.distinctItems) {
                    $scope.selectedQuery.distinctItems = [];
                }
                $scope.selectedQuery.distinctItems.push({});
            };
            
            $scope.removeDistinctItem = function (index) {
                $scope.selectedQuery.distinctItems.splice(index, 1);
                $scope.onQueryEdit();
            };
            
            $scope.addProjectionItem = function () {
                if (!$scope.selectedQuery.projectionItems) {
                    $scope.selectedQuery.projectionItems = [];
                }
                $scope.selectedQuery.projectionItems.push({});
            };
            
            $scope.removeProjectionItem = function (index) {
                $scope.selectedQuery.projectionItems.splice(index, 1);
                $scope.onQueryEdit();
            };
            
            $scope.addGroupItem = function (index) {
                if (!$scope.selectedQuery.groupItems) {
                    $scope.selectedQuery.groupItems = [];
                }
                $scope.selectedQuery.groupItems.push({});
            }
            $scope.removeGroupItem = function (index) {
                $scope.selectedQuery.groupItems.splice(index, 1);
                $scope.onQueryEdit();
            };
            
            $scope.termNameIsValid = function (termList, termName) {
                var valid = false;
                if (termList) {
                    for (var i = 0; i < termList.length; i++) {
                        if (termList[i].id === termName) {
                            valid = true;
                            break;
                        }
                    }
                }
                return valid;
            };
            
            $scope.getTermNames = function (groupItem) {
                var terms = [];
                dojo.forEach($scope.selectedQuery.termNames, function (term) {
                    if (groupItem && (groupItem.term === term.id)) {
                        workflowBuilderService.setTermProperties(groupItem, term);
                    }
                    terms.push(term);
                });
                return terms;
            };
            
            // Get child terms of composite term
            $scope.getSubTerms = function (parentTerm) {
                var subTerms = []; // child terms of composite term (parentTerm)
                if ($scope.selectedQuery.termNames) {
                    for (var g = 0; g < $scope.selectedQuery.termNames.length; g++) {
                        var term = $scope.selectedQuery.termNames[g];

                        if (term.id === parentTerm && term.dataType !== 'Composite') { // data-type must be "Composite"
                            return null;
                            break;
                        }

                        if (parentTerm && term.id.startsWith(parentTerm + '.')) {
                            var _termName = term.id.substring(parentTerm.length + 1, term.id.length); // +1 accounts for '.'
                            var o = {id: _termName, name: _termName};
                            workflowBuilderService.setTermProperties(o, term);
                            subTerms.push(o);
                        }
                    }
                }
                return subTerms;
            };
            
            $scope.hasCurrentJobTerm = function () {
                var hasCurrentJobFilter = false;
                angular.forEach($scope.selectedQuery.where, function (item) {
                    angular.forEach(item.orGroup, function (groupItem) {
                        if (groupItem.term === '_job_id' && groupItem.value === 'current_job.id') {
                            hasCurrentJobFilter = true;
                        }
                    })
                });
                return hasCurrentJobFilter;
            };
            
            $scope.addToGroup = function (group) {
                group.push({value: null, term: null});
            };
            
            $scope.deleteGroupItem = function (group, index) {
                group.splice(index, 1);
                $scope.onQueryEdit();
            };
        }
    }
});

angular.module("crisWorkflow").factory('workflowBuilderService', ["$uibModal", "$http", function($uibModal, $http) {
    var templatesHttpPromise; // promise when fetching template data
    var cachedTemplates;

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
                    var q1 = queryItems[i].rawQuery.replace(/\s/g, '');
                    var q2 = val.replace(/\s/g, '');
                    if (q1 === q2) {
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
                    var queryFrom = queryStr.substring(0, queryStr.indexOf('('));
                    var REGEX_UUID = /([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12})/i;
                    queryItem.templateUUID = REGEX_UUID.exec(queryFrom)[0];
                    
                    var queryParams = queryStr.substring(queryStr.indexOf('(') + 1, queryStr.length - 1);
                    var deserializedParams = angular.fromJson(queryParams);
                    for (var key in deserializedParams) {
                        var paramValue = deserializedParams[key];
                        if (key === '$skip' || key === '$limit') {
                            if (typeof paramValue !== 'number') {
                                throw 'invalid value for $limit or $skip in the query';
                            }
                            queryItem[key] = paramValue;
                        } else if (key === '$sort' || key === '$orderby') {
                            if (typeof paramValue === 'object') {
                                queryItem.sortItems = [];
                                for (var k in paramValue) {
                                    var obj = {term: k, order: paramValue[k]};
                                    queryItem.sortItems.push(obj);
                                }
                            }
                        } else if (key === '$distinct') {
                            if (typeof paramValue === 'object') {
                                queryItem.distinctItems = [];
                                for (var k in paramValue) {
                                    var obj = {term: k, isDistinct: paramValue[k]};
                                    queryItem.distinctItems.push(obj);
                                }
                            }
                        } else if (key === '$project') {
                            if (typeof paramValue === 'object') {
                                queryItem.projectionItems = [];
                                for (var k in paramValue) {
                                    var obj = {term: k, project: paramValue[k]};
                                    queryItem.projectionItems.push(obj);
                                }
                            }
                        } else if (key === '$group') {
                            if (typeof paramValue === 'object') {
                                queryItem.groupItems = [];
                                for (var k in paramValue) {
                                    if (paramValue[k] === null) {
                                        continue;
                                    }
                                    if ((paramValue[k] !== null && typeof paramValue[k] !== 'object') || (paramValue[k] instanceof Array)) {
                                        throw 'invalid group sytax';
                                    }
                                    var operator = Object.keys(paramValue[k])[0];
                                    var term = paramValue[k][operator];
                                    term = (typeof term === 'string' && term.startsWith('$')) ? term.substring(1, term.length) : term;
                                    var obj = {groupAlias: k, term: term, operator: operator};
                                    queryItem.groupItems.push(obj);
                                }
                            }
                        } else {
                            var whereItems = [];
                            if (key === '$query' && typeof paramValue === 'object' && !(paramValue instanceof Array)) {
                                for (var k in paramValue) {
                                    deserializeValue(paramValue[k], k, whereItems);
                                }
                            } else {
                                deserializeValue(paramValue, key, whereItems);
                            }
                            
                            queryItem.where = queryItem.where.concat(whereItems);
                        }
                    }
                    
                    (function initValues (items) {
                        for (var i = 0; i < items.length; i++) {
                            var whereItem = items[i];
                            if (whereItem.orGroup) {
                                initValues(whereItem.orGroup);
                            } else {
                                var valueProperties = setValueProperties(whereItem.value, queryItem.templateUUID, whereItem.term);
                                dojo.mixin(whereItem, valueProperties);
                            }
                        }
                    })(queryItem.where);
                    
                    // All where items must be in "or" group array for easy management in view. Group those without a group
                    var newGroups = [];
                    var len = queryItem.where.length;
                    while (len--) {
                        if (!queryItem.where[len].orGroup) {
                            var group = {orGroup: []};
                            group.orGroup.push(queryItem.where[len]);
                            newGroups.push(group);
                            queryItem.where.splice(len, 1);
                        }
                    }
                    
                    if (newGroups.length) {
                        queryItem.where = queryItem.where.concat(newGroups);
                    }
                    
                    for (var r = 0; r < cachedTemplates.length; r++) {
                        var template = cachedTemplates[r];
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
        
        function deserializeValue (value, _key, whereItems) {
            if (_key === '$and' || _key === '$or') {
                var orItems = []
                dojo.forEach(value, function (item, $index) {
                    var term = Object.keys(item)[0];
                    var _whereItem = {};
                    
                    if (term === '$or') {
                        var res = [];
                        deserializeValue(item[term], term, res);
                        _whereItem = res.length ? res[0] : {};
                    } else {
                        _whereItem = setWhereItemValue(term, item[term]);
                    }
                    
                    if (_key === '$or') {
                        orItems.push(_whereItem);
                    } else {
                        whereItems.push(_whereItem);
                    }
                });
                
                if (orItems.length) {
                    var _whereItem = {};
                    _whereItem.orGroup = orItems;
                    whereItems.push(_whereItem);
                }
            } else { // all other properties are implicitly 'AND' conditions
                var whereItem = setWhereItemValue(_key, value);
                whereItems.push(whereItem);
            }
            
            function setWhereItemValue (term, value) {
                var whereItem = {term: term};
                if (typeof value === 'object' && value !== null && !(value instanceof Array)) {
                    var operator = Object.keys(value)[0]; // Mongo operator. E.g. $lt, $gt, $gte, etc.
                    if (operator && operator.trim().startsWith('$')) {
                        whereItem.queryOperator = operator;
                        whereItem.value = value[operator];
                    }
                } else {
                    whereItem.queryOperator = '$eq';
                    whereItem.value = value;
                }
                if (!whereItem.queryOperator && typeof value === 'object') { // object or array value
                    whereItem.queryOperator = '$eq';
                    whereItem.value = value;
                }
                return whereItem;
            }
        }
    };

    var serializeToJson_ = function (builderRecords, queries, queryId) {
        console.log('******************* Serialize-to-Json *****************************');
        var newJsonStr = '';
        updateRawQueries(queries); // deserialze all queries to their raw versions
        if (builderRecords) {
            var records = angular.copy(builderRecords);
            if (records.length) {
                
                var termsToMerge = {};
                var newJsonObj = sanitizeJsonRecords(records, termsToMerge);
                
                // Add $merge directive if at least one item requires merging
                var doMerge = false;
                var multipleMergePaths;
                for (var r in termsToMerge) {
                    for (var j in termsToMerge[r]) {
                        var value = termsToMerge[r][j];
                        if (typeof value === 'string' && value.startsWith('QUERY_') && Object.keys(termsToMerge[r]).length > 1) {
                            doMerge = true;
                        }
                    }
                }
                if (doMerge && Object.keys(newJsonObj).length > 1) {
                    multipleMergePaths = true;
                } else if (doMerge && Object.keys(newJsonObj).length === 1) {
                    multipleMergePaths = false;
                }
                
                var finalJsonObj = {};
                if (typeof multipleMergePaths !== 'undefined') {
                    if (multipleMergePaths) {
                        finalJsonObj['$directive'] = [];
                        finalJsonObj['$data'] = {};
                    } else {
                        finalJsonObj['$directive'] = {$merge:{}};
                        finalJsonObj['$data'] = [];
                    }
                }
                
                for (var key in newJsonObj) {
                    var terms = termsToMerge[key];
                    if (terms && Object.keys(terms).length) {
                        for (var k in terms) {
                            var o;
                            if (k.startsWith('QUERY__') && terms[k].startsWith('QUERY__')) {
                                o = terms[k];
                            } else {
                                o = {};
                                o[k] = terms[k];
                            }
                            var mergeItem = {};
                            mergeItem[key] = o;
                            if (finalJsonObj['$data']) {
                                if (finalJsonObj['$data'] instanceof Array) {  // single merge.....
                                    finalJsonObj['$data'].push(mergeItem);
                                } else { // for multiple merge paths.....
                                    if (!finalJsonObj['$data'][key]) {
                                        finalJsonObj['$data'][key] = [];
                                    }
                                    finalJsonObj['$data'][key].push(o);
                                }
                            } else {
                                if (!finalJsonObj[key]) {
                                    finalJsonObj[key] = {};
                                }
                                if (k.startsWith('QUERY_')) {
                                    finalJsonObj[key] = terms[k];
                                } else {
                                    finalJsonObj[key][k] = terms[k];
                                }
                            }
                        }
                        // Remember to add $path to $directive array when dealing with multiple merge paths.....
                        if (finalJsonObj['$directive'] instanceof Array) {
                            finalJsonObj['$directive'].push({$merge: {$path: key}});
                        }
                    } else {
                        var value = newJsonObj[key];
                        if (finalJsonObj['$data']) {
                            finalJsonObj['$data'][key] = value;
                        } else {
                            finalJsonObj[key] = value;
                        }
                    }
                }
                newJsonStr = JSON.stringify(finalJsonObj, null, 4);
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
    
    function setValueProperties (_value, templateUUID, termName) {
        // Get term definition in order to refer to term properties. E.g. list flag, multi-list flag, multi-file flag
        var termDefinition = null;
        if (templateUUID && termName && cachedTemplates) {
            for (var t = 0; t < cachedTemplates.length; t++) {
                if (cachedTemplates[t].id === templateUUID) { // locate template data
                    var terms = cachedTemplates[t].termNames;
                    for (var i = 0; i < terms.length; i++) {
                        if (terms[i].id === termName) {
                            termDefinition = terms[i];
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        // if term is list, staticDataType will be an array of term instances
        if (termDefinition && termDefinition.isListTerm) {
            // value must be array. Otherwise do not process it...user will be notified of invalid value in view
            if (_value instanceof Array) {
                var a = [];
                for (var t in _value) {
                    var o = processValue(_value[t], templateUUID, termName);
                    o.term = termName; // Reference to term necessary to be able to extract term attributes, e.g. attach-to data
                    a.push(o);
                }
                return {valueType: 'static', value: a, staticDataType: 'Array', isReadOnly: true, dataType: termDefinition.dataType, isListTerm: termDefinition.isListTerm};
            } else {
                return {valueType: 'static', value: _value, staticDataType: 'Array', isReadOnly: true};
            }
        } else {
            return processValue(_value, templateUUID, termName);
        }
        
        function processValue (_value, templateUUID, termName) {
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
            } else if (typeof value !== 'object' && isNaN(value) && !isNaN(Date.parse(value))) {
                staticDataType = 'Date';
            } else if (typeof value === 'boolean') {
                staticDataType = 'Boolean';
            } else if (value === null) {
                staticDataType = "Null";
            } else {
                if (typeof value === 'string') {
                    staticDataType = 'Text';
                } else {  // value is object or array
                    valueType = 'static';
                    if (value instanceof Array) {
                        staticDataType = 'Array';
                        var v = [];
                        for (var b = 0; b < value.length; b++) {
                            v.push(setValueProperties(value[b], templateUUID, termName));
                        }
                        value = v;
                    } else {
                        staticDataType = 'Object';
                        var v = [];
                        for (var key in value) {
                            var term = (termName ? termName + '.' + key : key);
                            var oB = setValueProperties(value[key], templateUUID, term);
                            oB.term = term; // Reference to term necessary to be able to extract term attributes in the view, e.g. attach-to data
                            v.push({variable: key, value: oB});
                        }
                        value = v;
                    }
                }
            }

            var isReadOnly = false;
            var dataType = null;
            var listItemProps = null;
            var isMultiFile = null;
            if (termDefinition) {
                var previousStaticDataType = staticDataType;
                staticDataType = termDefinition.dataType;
                dataType = termDefinition.dataType;
                isReadOnly = true;
                
                if (termDefinition.dataType === 'List') {
                    listItemProps = termDefinition.listItemProps; // dropdown list items from term definition

                    // For multiSelect term, set each array item's staticDataType to match term data type
                    if (value instanceof Array) {
                        staticDataType = 'Array';
                        for (var e = 0; e < value.length; e++) {
                            value[e].staticDataType = dataType;
                        }
                    }
                }
                if (termDefinition.dataType === 'File') {
                    isMultiFile = termDefinition.isMultiFile;

                    // For multiFile term, set each array item's staticDataType to match term data type
                    if (value instanceof Array) {
                        staticDataType = 'Array';
                        for (var e = 0; e < value.length; e++) {
                            value[e].staticDataType = dataType;
                        }
                    }
                }

                // If term value doesn't match term data type, rollback processed value to original. User will be notified of invalid value.
                if (previousStaticDataType === 'Array' && staticDataType !== 'Array') {
                    value = _value;
                }
                if (previousStaticDataType === 'Object' && !(staticDataType === 'Object' || staticDataType === 'Composite')) {
                    value = _value;
                }
            }

            // dataType applies to template terms. Non term-variables rely on staticDataType.
            return {valueType: valueType, value: value, staticDataType: staticDataType, subValue: subValue, dataType: dataType, listItemProps: listItemProps, isMultiFile: isMultiFile, isReadOnly: isReadOnly};
        }
    }
    
    function prettifyQuery (queryStr) { // format the json part of a query
        var REGEX_JSONPART = /[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}(?:\.\w+)?(?:\[\])?\((\{.*\})\)/i; // matches the json part of query
        var queryParts = REGEX_JSONPART.exec(queryStr);
        if (queryParts && queryParts[1]) {
            var queryPart = queryParts[1];
            var objectified = angular.fromJson(queryPart);
            var prettified = JSON.stringify(objectified, null, 4); // angular's toJson skips property names starting with '$'
            queryStr = queryStr.replace(queryPart, prettified);
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
                    var orGroup = item.orGroup;
                    var valueList = processOrGroup(orGroup);
                    
                    if (valueList.length) {
                        if (typeof obj['$query'] === 'undefined') {
                            obj['$query'] = {};
                        }
                        
                        if (valueList.length === 1) {
                            for (var alias in valueList[0]) {
                                if (obj['$query'][alias]) { // If duplicate exists, group them in $and
                                    if (!obj['$query']['$and']) {
                                        obj['$query']['$and'] = [];
                                    }
                                    obj['$query']['$and'].push(valueList[0]);
                                } else {
                                    if (obj['$query']['$and']) {
                                        obj['$query']['$and'].push(valueList[0]);
                                    } else {
                                        angular.extend(obj['$query'], valueList[0]);
                                    }
                                }
                            }
                            
                            // If $and array exists, move all items outside it into it
                            if (obj['$query']['$and']) {
                                for (var alias in obj['$query']) {
                                    if (alias !== '$and') {
                                        var o = {};
                                        o[alias] = obj['$query'][alias];
                                        obj['$query']['$and'].push(o);
                                        delete obj['$query'][alias];
                                    }
                                }
                            }
                        } else {
                            if (!obj['$query']['$and']) {
                                var props = [];
                                for (var y in obj['$query']) {
                                    var o = {};
                                    o[y] = obj['$query'][y]
                                    props.push(o);
                                    delete obj['$query'][y];
                                }
                                obj['$query']['$and'] = props;
                            }
                            
                            var orGrpValue = {};
                            orGrpValue['$or'] = valueList;
                            obj['$query']['$and'].push(orGrpValue);
                        }
                    }
                }
                
                // if only one item in $and array of query, move it to root of $query object and delete the $and array
                if (obj['$query'] && obj['$query']['$and'] && obj['$query']['$and'].length === 1 && Object.keys(obj['$query']).length === 1) {
                    var v = obj['$query']['$and'][0];
                    delete obj['$query']['$and'];
                    angular.extend(obj['$query'], v);
                }
                
                if (query.projectionItems && query.projectionItems.length) {
                    var projectionObj = {};
                    for (var i = 0; i < query.projectionItems.length; i++) {
                        var projectionItem = query.projectionItems[i];
                        if (projectionItem.term && projectionItem.project !== null && typeof projectionItem.project !== 'undefined') {
                            projectionObj[projectionItem.term] = projectionItem.project;
                        }
                    }
                    obj['$project'] = projectionObj;
                }
                if (query.distinctItems && query.distinctItems.length) {
                    var distinctObj = {};
                    for (var i = 0; i < query.distinctItems.length; i++) {
                        var distinctItem = query.distinctItems[i];
                        if (distinctItem.term && distinctItem.isDistinct !== null && typeof distinctItem.isDistinct !== 'undefined') {
                            distinctObj[distinctItem.term] = distinctItem.isDistinct;
                        }
                    }
                    obj['$distinct'] = distinctObj;
                }
                if (query.sortItems && query.sortItems.length) {
                    var sortObj = {};
                    for (var i = 0; i < query.sortItems.length; i++) {
                        var sortItem = query.sortItems[i];
                        if (sortItem.term && sortItem.order) {
                            sortObj[sortItem.term] = sortItem.order;
                        }
                    }
                    obj['$sort'] = sortObj;
                }
                if (query.groupItems && query.groupItems.length) {
                    var groupObj = {};
                    for (var i = 0; i < query.groupItems.length; i++) {
                        var groupItem = query.groupItems[i];
                        if (groupItem.term && groupItem.groupAlias && groupItem.operator) {
                            var o = {};
                            o[groupItem.operator] = (typeof groupItem.term === 'string') ? '$' + groupItem.term : groupItem.term;
                            groupObj[groupItem.groupAlias] = o;
                        }
                    }
                    if (Object.keys(groupObj).length && !groupObj._id) {
                        groupObj._id = null;
                    }
                    obj['$group'] = groupObj;
                }
                if (query['$limit']) {
                    obj['$limit'] = parseInt(query['$limit'], 10);
                }
                if (query['$skip']) {
                    obj['$skip'] = parseInt(query['$skip'], 10);
                }
                query.rawQuery = rawQuery + dojo.toJson(obj) + ')';
            }
        }
        
        function processOrGroup (orGroup) {
            var valueList = [];
            for (var k = 0; k < orGroup.length; k++) {
                var orItem = orGroup[k];
                var valueObj = {};
                if (orItem.term && ((typeof orItem.value !== 'undefined' && orItem.value !== null) || orItem.staticDataType === "Null") && orItem.queryOperator) {
                    if (orItem.queryOperator === '$eq') {
                        valueObj[orItem.term] = restoreValue(orItem);
                    } else {
                        var nestedValue = {};
                        nestedValue[orItem.queryOperator] = restoreValue(orItem);
                        valueObj[orItem.term] = nestedValue;
                    }
                }
                
                // In case of nested "or" group. Note that UI doesn't supported nested OR groups. Possibly mongo doesn't too.
                // We still process them to maintain a user-typed query's original form
                if (orItem.orGroup && orItem.orGroup.length)  { 
                    var subValues = processOrGroup(orItem.orGroup);
                    if (subValues.length) {
                        valueObj['$or'] = subValues;
                    }
                }

                if (Object.keys(valueObj).length) {
                    valueList.push(valueObj);
                }
            }
            return valueList;
        }
    }

    function sanitizeJsonRecords(builderRecords, termsToMerge) {
        var newJsonObj = {};
        for (var i = 0; i < builderRecords.length; i++) {
            var record = builderRecords[i];
            
            if (record.variable && record.variableType === 'template') {
                newJsonObj[record.variable] = {};
                if (record.termsToMerge && record.termsToMerge.length) {
                    termsToMerge[record.variable] = sanitizeJsonRecords(record.termsToMerge);
                }
                continue;
            }
            
            if (!record.variable || (!record.valueType && record.itemType !== 'Query') || ((record.value === null || typeof record.value === 'undefined' || (typeof record.value === 'string' && record.value.trim().length === 0)) && record.staticDataType !== "Null" && record.itemType !== 'Query')) {
                continue; // ignore incomplete records
            }
            record.value = restoreValue(record);
            
            newJsonObj[record.variable] = record.value;
        }
        return newJsonObj;
    }
    
    function restoreValue (record) {
        var value = record.value;
        var subValue = record.subValue ? '.' + record.subValue : '';
        if (record.valueType === 'systemVariable') {
            value = '#{' + value + '}';
        }
        if (record.valueType === 'localVariable') {
            value = '${' + value + '}';
        }
        if (record.valueType === 'static' && record.staticDataType === 'Numeric') {
            value = parseInt(value);  // Ensures Numerics aren't quoted after jsonifying
        }
        if (record.valueType === 'query' && subValue) {
            value = value + subValue;
        }
        if (record.itemType === 'Query') {
            value = record.variable;
        }
        if (record.valueType === 'static' && (record.staticDataType === 'Object' || record.staticDataType === 'Composite')) {
            value = restoreObjectOrArrayValue(value, 'Object');
        }
        if (record.valueType === 'static' && record.staticDataType === 'Array') {
            value = restoreObjectOrArrayValue(value, 'Array');
        }
        return value;
    }
    
    function restoreObjectOrArrayValue (value, type) {
        if (type === 'Object' || type === 'Composite') {
            return processObjectValue(value);
        }
        
        if (type === 'Array') {
            return processArrayValue(value);
        }
        
        function processArrayValue (theArray) {
            var originalArray = [];
            if (theArray) {
                for (var t = 0; t < theArray.length; t++) {
                    var _val = theArray[t].value;
                    if (theArray[t].staticDataType === "Object" || theArray[t].staticDataType === "Composite") {
                        originalArray.push(processObjectValue(_val));
                    } else if (theArray[t].staticDataType === "Array") {
                        originalArray.push(processArrayValue(_val));
                    } else {
                        originalArray.push(decorateValue(_val, theArray[t].valueType));
                    }
                }
            }
            return originalArray;
        }
        
        function processObjectValue (theObject) {
            var originalObj = {};
            if (theObject) {
                for (var t = 0; t < theObject.length; t++) {
                    if (theObject[t].value.staticDataType === "Object" || theObject[t].value.staticDataType === "Composite") {
                        originalObj[theObject[t].variable] = processObjectValue(theObject[t].value.value);
                    } else if (theObject[t].value.staticDataType === "Array") {
                        originalObj[theObject[t].variable] = processArrayValue(theObject[t].value.value);
                    } else {
                        originalObj[theObject[t].variable] = decorateValue(theObject[t].value.value, theObject[t].value.valueType);
                    }
                }
            }
            return originalObj;
        }
        
        function decorateValue (value, valueType) {
            var _val = value;
            if (valueType === 'systemVariable') {
                _val = '#{' + _val + '}';
            }
            if (valueType === 'localVariable') {
                _val = '${' + _val + '}';
            }
            return _val;
        }
    }
    
    function extractOptions (task, taskType) {
        var json = task[taskType].trim();
        (function findOptionsObj(jsonStr) {
            var matcher = balancedMatcher(jsonStr, ['{'], ['}']);
            while (matcher.find()) {
                var startIndex = matcher.start();
                var endIndex = matcher.end();
                var str = jsonStr.substring(startIndex + 1, endIndex - 1);

                var regEx = new RegExp('\\s*,?\\s*("\\$options"|\\$options)\\s*:\\s*\\{\\s*' + str.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&") + '\\s*\\}', 'g');
                if (regEx.test(json)) {
                    var $options = JSON.parse('{' + str + '}');

                    json = json.replace(regEx, ''); // Remove $options from json. User shouldn't see it
                    if (task && taskType) {
                        task[taskType] = json;
                        task[taskType + '_$options'] = $options; // Remember options object after it's removed from json
                    }
                    // break or continue and ensure only one $options options is appended to json?
                    //break;
                }
                findOptionsObj(str);
            }
        }(json));
    }
    
    return {
        removeUnsafeSpaceCharacters: function (jsonString) {
            return _removeUnsafeSpaceCharacters(jsonString);
        },
        setJsonOptions: function (jsonStr, task, taskType) {
            var result = jsonStr;
            if (task && jsonStr) {
                try {
                    if (!task[taskType + '_$options']) {
                        extractOptions(task, taskType);
                    }
                    if (!task[taskType + '_$options']) { // Still no options? set default.....
                        // if json exists set advanced to true. For empty json, set advanced to true.
                        task[taskType + '_$options'] = {_advanced: ((task[taskType] && task[taskType].length) > 0 ? true : false)};
                    }
                    jsonStr = task[taskType];
                    var $options = task[taskType + '_$options'];
                    result = jsonStr.trim().replace(/^\{(.+)\}$/, '{' + '$1' + ',"$options":' + JSON.stringify($options)  + '}');
                } catch (e) {
                    result = jsonStr;
                }
            }
            return result;
        },
        getTemplateData: function () {
            if (!templatesHttpPromise) {
                var requestUrl = cris.baseUrl + 'templates/?showAllStatus=true&filter={"op":"equal","data":[{"op":"number","data":"statusId","isCol":true},{"op":"number","data":1,"isCol":false}]}&sort(+name)';
                templatesHttpPromise =  $http({
                                            method: 'GET',
                                            url: requestUrl
                                        });
            }
            
            return ({
                        promise: templatesHttpPromise, 
                        thenCallback: function (result, templateStore) {
                            
                            if (cachedTemplates) { // return cached templates if true
                                templateStore.templates = cachedTemplates;
                                return;
                            }
                            
                            var results = result.data;
                            var templates_ = [];
                            
                            // For each template fetch its latest version of terms and/or any associated attach-to data
                            angular.forEach(results, function (item) {
                                var obj = {name: item.name, id: item['uuid']['$uuid']};
                                var templateUUID = item.uuid.$uuid;
                                var templateVersion = item.versionNumber.$uuid;
                                $http({
                                    method: 'GET',
                                    url: cris.baseUrl + "templates/load/" + templateUUID + "/" + templateVersion
                                }).then(function (resultData) {
                                    
                                    var termNames = [];
                                    var termDataTypes = {};
                                    var attachToRecords = {};
                                    var isMultiFileFlags = {};
                                    var listItemProps = {};
                                    var isListTermFlags = {};
                                    
                                    (function extractTermNames(term, isTopLevel) {
                                        var compositeTermName = arguments[2];
                                        angular.forEach(term.term, function (term_) {
                                            var _alias = term_.alias || term_.useAlias || term_.name;
                                            var path = compositeTermName ? compositeTermName + '.' + _alias : _alias;
                                            if (path) {
                                                termDataTypes[path] = getType(term_.validation.validator[0].type, term_.validation.validator[0].property, path);
                                                termNames.push(path);
                                                isListTermFlags[path] = term_.list;
                                                
                                                for (var v = 0; v < term_.attachTo.length; v++) {
                                                    var attachToAlias = (path + '.' + term_.attachTo[v].useAlias);
                                                    termDataTypes[attachToAlias]  = "AttachTo";
                                                    termNames.push(attachToAlias);
                                                    isListTermFlags[attachToAlias] = term_.attachTo[v].list;
                                                    getAttachToData(term_.attachTo[v], attachToAlias);
                                                }
                                            }
                                            if (term_.term && term_.term instanceof Array && term_.term.length) {
                                                extractTermNames(term_, false, path);
                                            }
                                        });
                                        
                                        if (isTopLevel) { // get term names for attach-tos in template top-level
                                            for (var v = 0; v < term.attachTo.length; v++) {
                                                var attachToAlias = term.attachTo[v].useAlias;
                                                termDataTypes[attachToAlias]  = "AttachTo";
                                                termNames.push(attachToAlias);
                                                isListTermFlags[attachToAlias] = term.attachTo[v].list;
                                                getAttachToData(term.attachTo[v], attachToAlias);
                                            }
                                        }
                                    })(resultData.data, true);
                                    
                                    termNames = termNames.sort().concat(['_experiment_id', '_job_id', '_project_id']);
                                    var termsCollection = [];
                                    dojo.forEach(termNames, function (termName, index) {
                                        var termDataType = termDataTypes[termName];
                                        if (['_experiment_id', '_job_id', '_project_id'].indexOf(termName) !== -1) {
                                            termDataType = 'Numeric';
                                        }
                                        termsCollection.push({id: termName, name: termName, dataType: termDataType, listItemProps: listItemProps[termName], isMultiFile: isMultiFileFlags[termName], index: index, isListTerm: isListTermFlags[termName], template: templateUUID});
                                    });
                                    obj.termNames = termsCollection;
                                    obj.attachToData = attachToRecords;
                                    
                                    function getAttachToData(term, termAlias) {
                                        if (attachToRecords[termAlias]) { // attach-to data for term was previously extracted and cached
                                            return;
                                        }
                                            
                                        var uuid = term.uuid;
                                        var idField = term.idField;
                                        var nameField = term.nameField;

                                        if (uuid && nameField && idField) {
                                            var url = cris.baseUrl + "rest/objectus/" + uuid;
                                            $http({
                                                method: 'GET',
                                                url: url
                                            }).then(function (result){
                                                var data = [];
                                                for (var i = 0; i < result.data.length; i++) {
                                                    if (result.data[i][nameField] && result.data[i][idField]) {
                                                        data.push({id: result.data[i][idField], name: result.data[i][nameField]});
                                                    }
                                                }
                                                attachToRecords[termAlias] = data;
                                            }, function (error) {
                                                
                                            });
                                        }
                                    }
                                    
                                    function getType (validationType, properties, termAlias) {
                                        var dataType = null;
                                        if (validationType === "text" || validationType === "advanced") {
                                            dataType = "Text";
                                        } else if (validationType === "numeric") {
                                            dataType = "Numeric";
                                        } else if (validationType === "date-time") {
                                            dataType = "Date";
                                            if ((properties instanceof Array) && properties.length && properties[0].name === 'format' && properties[0].value === 'time') {
                                                dataType = "Time";
                                            }
                                        } else if (validationType === "boolean") {
                                            dataType = "Boolean";
                                        } else if (validationType === "composite") {
                                            dataType = "Composite";
                                        } else if (validationType === "list") {
                                            dataType = "List"
                                            if (properties instanceof Array) {
                                                var items = [];
                                                for (var i = 0; i < properties.length; i++) {
                                                    if (properties[i].name === 'item' && properties[i].value) {
                                                        var id = properties[i].id ? properties[i].id : properties[i].value;
                                                        items.push({id: id, name: properties[i].value});
                                                    }
                                                }
                                                
                                                listItemProps[termAlias] = {items: items};
                                                if (properties.length && properties[0].name === 'isMultiSelect' && properties[0].value === 'true') {
                                                    listItemProps[termAlias].isMultiSelect = true;
                                                }
                                            }
                                        } else if (validationType === "file") {
                                            dataType = 'File';
                                            if (properties.length && properties[0].name === 'multiple' && properties[0].value === 'true') {
                                                isMultiFileFlags[termAlias] = true;
                                            }
                                        }
                                        return dataType;
                                    }
                                }, function (errorData) {
                                    
                                });
                                templates_.push(obj);
                            });
                            templateStore.templates = templates_;
                            cachedTemplates = templates_; // Cache templates for next retrieval
                            
                            console.log('************************* Templates Fetched ****************************');
                            console.dir(templates_);
                        }
                    });
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
                {id: 'current_user.fullName', name: 'current_user.fullName'},
                {id: 'current_user.email', name: 'current_user.email'},
                {id: 'current_user.externalId', name: 'current_user.externalId'},
                {id: 'current_user.enabled', name: 'current_user.enabled'},
                {id: 'current_user.timeCreated', name: 'current_user.timeCreated'},
                {id: 'current_user.timeUpdated', name: 'current_user.timeUpdated'},
                {id: 'current_date', name: 'current_date'}];
        },
        deserializeJson: function (json, queryItems, task, taskType) {
            console.log('************Deserialize Json**********************');
            var result = [];
            var error = "";
            var $options = null;
            
            if (json) {
                var _json = json.trim();
                if (isJsonLike(_json)) {
                    try {
                        // Extract the $options object from the json
                        extractOptions(task, taskType);
                        _json = task[taskType];
                        $options = task[taskType + '_$options'];
                        
                        /*
                        * Disable query or directive functionality: If json contains queries or directives, cancel parsing
                        */
                        /*
                        var REGEXP_UUID = /\b([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12})/i;
                        if (REGEXP_UUID.test(_json) || _json.indexOf('$directive') !== -1) {
                           throw 'Unsuported features';
                        }
                        */
                        //************************************************

                        var sanitizedJson = eval(_json, queryItems);
                        deserializeQueries(queryItems);
                        var jsonObj = dojo.fromJson(sanitizedJson);
                        
                        var REGEX_TEMPLATE_VARIABLE = /^[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}/i;

                        var initializedValues = [];
                        if (jsonObj['$directive']) {
                            
                            /**************************************************************/
                            // Currently Merge only supported for template data.....
                            // Throw exception if Merge has a non-template variable 
                            var allKeys = [];
                            if (jsonObj['$data'] instanceof Array) {
                                for (var k in jsonObj['$data']) {
                                    var itemKey = Object.keys(jsonObj['$data'][k])[0];
                                    allKeys.push(itemKey);
                                }
                            } else {
                                // For multiple merges get templateUUIDs to merge using the $path directive
                                for (var k in jsonObj['$directive']) {
                                    var itemKey = jsonObj['$directive'][k]['$merge']['$path'];
                                    allKeys.push(itemKey);
                                }
                            }
                            
                            for (var t = 0; t < allKeys.length; t++) {
                                // Test fails if variable is not templateUUID or if in case of [$data instancof Array] all Keys aren't the same
                                if (!REGEX_TEMPLATE_VARIABLE.test(allKeys[t]) || (jsonObj['$data'] instanceof Array && allKeys[t] !== allKeys[0])) {
                                    throw 'Invalid Merge directive';
                                }
                            }
                            /********************************************************************/
                            
                            if (jsonObj['$directive'] instanceof Array) {
                                for (var key in jsonObj['$data']) {
                                    var value = jsonObj['$data'][key];
                                    if (value instanceof Array) {
                                        
                                        var valProps = {};
                                        valProps.variable = key;
                                        valProps.variableType = 'template';
                                        valProps.termsToMerge = [];
                                        for (var y = 0; y < value.length; y++) {
                                            var itemToMerge = value[y];
                                            if (itemToMerge && typeof itemToMerge === 'object' && !(itemToMerge instanceof Array)) {
                                                for (var termKey in itemToMerge) {
                                                    var o = setValueProperties(itemToMerge[termKey], key, termKey);
                                                    o.itemType = 'Term';
                                                    o.variable = termKey;
                                                    valProps.termsToMerge.push(o);
                                                }
                                            } else if (itemToMerge && itemToMerge.startsWith('QUERY_')) {
                                                valProps.termsToMerge.push({itemType: 'Query', variable: itemToMerge})
                                            }
                                        }
                                        initializedValues.push(valProps);
                                        
                                    } else {
                                        var valueProperties = initVariableValue(value, key);
                                        initializedValues.push(valueProperties);
                                    }
                                }
                            } else {
                                
                                if (jsonObj['$data'].length) {
                                    
                                    var variableId = Object.keys(jsonObj['$data'][0])[0];
                                    var valProps = {};
                                    valProps.termsToMerge = [];
                                    valProps.variableType = 'template';
                                    valProps.variable = variableId;
                                    for (var h = 0; h < jsonObj['$data'].length; h++) {
                                        var itemToMerge = jsonObj['$data'][h][variableId];
                                        if (itemToMerge && typeof itemToMerge === 'object' && !(itemToMerge instanceof Array)) {
                                            for (var termKey in itemToMerge) {
                                                var o = setValueProperties(itemToMerge[termKey], variableId, termKey);
                                                o.itemType = 'Term';
                                                o.variable = termKey;
                                                valProps.termsToMerge.push(o);
                                            }
                                        } else if (itemToMerge && itemToMerge.startsWith('QUERY_')) {
                                            valProps.termsToMerge.push({itemType: 'Query', variable: itemToMerge})
                                        }
                                    }
                                    initializedValues.push(valProps);
                                }
                            }
                        } else {
                            for (var key in jsonObj) {
                                var valueProperties = initVariableValue(jsonObj[key], key);
                                initializedValues.push(valueProperties); 
                            }
                        }
                        result = result.concat(initializedValues);
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
                console.dir(result);
                console.dir(queryItems);
            }
            
            function initVariableValue(value, variableId) {
                var REGEX_TEMPLATE_VARIABLE = /^[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}/i;
                var variableType = 'variable';
                if (REGEX_TEMPLATE_VARIABLE.test(variableId)) {
                    variableType = 'template';
                }
                var valueProperties = setValueProperties(value);
                if (variableType === 'template' && value !== null) {
                    valueProperties.termsToMerge = [];
                    if (typeof value === 'object') {
                        for (var e in value) {
                            var props = setValueProperties(value[e], variableId, e);
                            props.variable = e;
                            if (e.startsWith('QUERY_')) {
                                props.itemType = 'Query';
                            } else {
                                props.itemType = 'Term';
                            }
                            valueProperties.termsToMerge.push(props);
                        }
                    } else if (value.startsWith('QUERY_')) {
                        valueProperties.termsToMerge.push({itemType: 'Query', variable: value});
                    }
                }
                dojo.mixin(valueProperties, {variableType: variableType, variable: variableId});
                return valueProperties;
            }
            
            return {result: result, error: error, $options: $options};
        },
        fromBuilderToJson: function (builderRecords, task, jsonType, queries) {
            console.log('************From builder to Json');
            try {
                var newJsonStr = serializeToJson_(builderRecords, queries);

                if (jsonType === 'jsonIn') {
                    task.jsonIn = newJsonStr;
                } else if (jsonType === 'jsonOut') {
                    task.jsonOut = newJsonStr;
                }
            } catch (e) {
                
            }
        },
        objectToJson: function (obj, objectType) {
            var formattedStr = JSON.stringify(restoreObjectOrArrayValue(obj, objectType), null, 4);
            return formattedStr;
        },
        setTermProperties: function (obj, term) {
            obj.dataType = term.dataType;
            obj.isMultiFile = term.isMultiFile;
            obj.listItemProps = term.listItemProps;
            obj.index = term.index;
            obj.isReadOnly = true;
            obj.isListTerm = term.isListTerm;
            obj.template = term.template;
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
            
            $uibModal.open({
                template: '<div ng-include="\'view_json_preview\'"></div>',
                size: 'lg',
                scope: _scope,
                controller: function ($scope, $uibModalInstance) {
                    $scope.close = function () {
                        $uibModalInstance.close();
                    };
                }
            });
        }
    }
}]);
