// Meta fields
var MetaField_Id = "_id";
var MetaField_Parent = "__parent";
var MetaField_ProjectId = "_project_id";
var MetaField_ExperimentId = "_experiment_id";
var MetaField_JobId = "_job_id";
var MetaField_TemplateId = "_template_id";
var MetaField_TemplateUuid = "_template_uuid";
var MetaField_TemplateVersion = "_template_version";
var MetaField_TaskId = "_task_id";
var MetaField_TimeCreated = "_time_created";
var MetaField_TimeUpdated = "_time_updated";

angular.module("crisDataset", ["dataset","ui.bootstrap"]);

angular.module("crisDataset").controller("DatasetController", ["$scope","$uibModal","$http", function ($scope,$uibModal,$http) {
    var theApp = cris.objectus.index.app;
    var urlRestObjectus = cris.baseUrl + "rest/objectus/";

    // related to document
    $scope.term = {};
    $scope.data = instantiateTerm($scope.term, {});
    $scope.message = instantiateTerm($scope.term, {});
    $scope.readOnly = false;
    $scope.context = {};

    // current selections
    $scope.currentProject = null;
    $scope.currentExperiment = null;
    $scope.currentJob = null;
    $scope.currentTemplate = null;
    
    // current document being edited
    $scope.isNew = null;
    $scope.showDetailedView = false;
    
    $scope.overrideHidden = false;
    $scope.overrideReadOnly = false;
    
    // Set query string for templates dropdown
    $scope.$watchCollection('[selectedProject,selectedExperiment,selectedJob,showAllTemplates]', function (value) {
        var queryStr = "/?sort(+name)&";
        queryStr += ("projectId=" + (value[0] ? value[0] : "0") + "&"); // projectId
        queryStr += ("experimentId=" + (value[1] ? value[1] : "0") + "&"); // experimentId
        queryStr += ("jobId=" + (value[2] ? value[2] : "0") + "&"); // jobId
        queryStr += ("showAll=" + (value[3] ? value[3] : "false")); // show all templates?
        
        if (queryStr !== $scope.templatesQueryStr) {
            $scope.selectedTemplate = null;
            $scope.templatesQueryStr = queryStr;
        }
    });
    
    // Set query string for jobs dropdown
    $scope.$watchCollection('[selectedProject,selectedExperiment]', function (value) {
        var projectId = (value[0] ? value[0] : "0");
        var experimentId = (value[1] ? value[1] : "0");
        var operator = (projectId !== "0" && experimentId !== "0") ? "and" : "any";
        var queryStr = '/?filter={"op":"' + operator + '","data":[{"op":"equal","data":[{"op":"number","data":"projectId.id","isCol":true},{"op":"number","data":' + projectId + ',"isCol":false}]},{"op":"equal","data":[{"op":"number","data":"experimentId.id","isCol":true},{"op":"number","data":' + experimentId+ ',"isCol":false}]}]}&sort(+name)';

        if (queryStr !== $scope.jobsQueryStr) {
            $scope.selectedJob = null;
            $scope.jobsQueryStr = queryStr;
        }
    });
    
    // Set query string for experiments dropdown
    $scope.$watch('selectedProject', function (value) {
        var queryStr = "/?sort(+name)" + (value ? ("&projectId=" + value) : "");
        $scope.selectedExperiment = null;
        $scope.experimentQueryStr = queryStr;
    });
    
    $scope.$watch('currentTemplate.id', function (value) {
        $scope.showDetailedView = false;
    });

    // create a new document based on the current selected template
    $scope.newDocument = function() {
        // pre-condition: a template is selected
        if (this.currentTemplate === null) {
            showMessage("Please select a template");
            return;
        }

        resetFileUploaders("idDatasetForm");

        var metaFields = {};
        if (this.currentProject) {
            metaFields[MetaField_ProjectId] = +this.currentProject.id;
        }
        if (this.currentExperiment) {
            metaFields[MetaField_ExperimentId] = +this.currentExperiment.id;
        }
        if (this.currentJob) {
            metaFields[MetaField_JobId] = +this.currentJob.id;
        }

        //TODO: provide a callback
        // clear the selection

        this.showDetailedView = true;
        this.isNew = true;

        this.term = getTerm(this.currentTemplate.id, this.currentTemplate.version, true);
        this.data = instantiateTerm(this.term, {});
        this.data = dojo.mixin(this.data, metaFields);
        this.context = this.data;

        var project = $scope.selectedProject;
        var experiment = $scope.selectedExperiment;
        this.currentProject = (project && project !== "0") ? project: null;
        this.currentExperiment = (experiment && experiment !== "0") ? experiment : null;
        this.currentJob = null;
        this.currentTask = null;
        this.timeCreated = null;
        this.timeUpdated = null;

        this.message = {};
        this.errors = "";
        this.readOnly = false;

        //this.$apply();
    };

    // open an existing document
    $scope.openDocument = function(doc) {
        // pre-condition: a template is selected
        if (this.currentTemplate === null) {
            showMessage("Please select a template");
            return;
        }

        resetFileUploaders("idDatasetForm");

        var templateUuid = this.currentTemplate.id;
        var templateVersion = doc[MetaField_TemplateVersion]["$uuid"];
        this.currentTemplate.version = templateVersion;
        var term = getTerm(templateUuid, templateVersion, true);
        var data = duplicateItem(doc, [MetaField_Parent]);
        var data = instantiateTerm(term, data);

        this.currentProject = doc[MetaField_ProjectId];
        this.currentExperiment = doc[MetaField_ExperimentId];
        this.currentJob = doc[MetaField_JobId];
        this.currentTask = doc[MetaField_TaskId];
        this.timeCreated = doc[MetaField_TimeCreated] ? doc[MetaField_TimeCreated].$date : null;
        this.timeUpdated = doc[MetaField_TimeUpdated] ? doc[MetaField_TimeUpdated].$date : null;

        this.showDetailedView = true;
        this.isNew = false;
        this.term = term;
        this.data = data; //dojo.mixin(data, termData);
        this.context = data;
        this.errors = "";
        this.message = instantiateTerm(term, {}, true);
        this.readOnly = false;

        console.log("****************");
        console.dir(this.term);
        console.dir(this.data);

        this.$apply();
    };

    // save the current document
    $scope.saveDocument = function() {
        
        $scope.DatasetForm.submitted = true; // We need this flag in order to color the border of invalid elements red; i.e. ng-form.submitted .ng-incalid {border:....}
        
        if ($scope.DatasetForm.$valid === false) {
            $scope.errors = "Please correct any errors highlighted in red.";
            return;
        }
        $scope.errors = "";
        
        var templateTermName = getTermName(this.currentTemplate);
        
        $scope.documentCleanup(this.data);

        var payload = {};
        payload.isIframe = true;
        payload.projectId = this.currentProject;
        payload.experimentId = this.currentExperiment;
        payload.jobId = this.currentJob;
        payload.taskId = this.currentTask;
        payload[templateTermName] = angular.toJson(this.data);
        
        var _this = this;
        dojo.io.iframe.send({
            url: urlRestObjectus,
            method: "post",
            form: "idDatasetForm",
            content: payload,
            handleAs: "json",
            load: function(data, ioArgs) {
                if (data["isValid"] === true) {
                    //TODO: provide a callback
                    // Update the grid.
                    theApp.updateDatasetGrid();

                    _this.isNew = null;
                    _this.showDetailedView = false;
                    _this.data = {};
                    _this.message = {};
                    $scope.DatasetForm.submitted = false; // flag ng-form as not in submit mode
                } else {
                    if (data["status"]) {
                        var termName = getTermName(_this.currentTemplate);
                        var status = data["status"];
                        var message = status[termName][_this.currentTemplate.name];

                        // update with error message
                        _this.message = message;
                        _this.errors = "Please correct any errors highlighted in red.";
                    } else if (data["message"]) {
                        _this.errors = "Save: " + data["message"];
                    }
                }
                _this.readOnly = false;
                _this.$apply();
            },
            error: function(error, ioArgs) {
                if (error.message) {
                    $scope.errors = error.message;
                    $scope.$apply();
                }
            }
        });
    };

    // delete the current document
    $scope.deleteDocument = function() {
        if (this.isNew) {
            alert("You should select a record first to delete.");
            return;
        }
        
        var _this = this;
        var modalTemplate = '<div> \
                                <div class="modal-body"> \
                                    <p>Are you sure you want to delete the record?</p> \
                                </div> \
                                <div class="modal-footer"> \
                                    <span class="pull-left"><input type="button" value="Yes" class="btn btn-primary" ng-click="ok()" /></span> \
                                    <span class="pull-right"><input type="button" value="No" class="btn btn-warning" ng-click="cancel()" /></span> \
                                </div> \
                            </div>';
        $uibModal.open({
            animation: true,
            template: modalTemplate,
            size: 'sm',
            controller: function ($scope, $uibModalInstance) {
                $scope.ok = function () {
                    var query = _this.currentTemplate.id + "/" + _this.data[MetaField_Id].$oid;
                    //TODO: use a separate store
                    $http({
                        method: 'DELETE',
                        url: urlRestObjectus + query
                    }).then(function (successResponse) {
                        var data = successResponse.data;
                        if (data["hasError"] && data["hasError"] === true) {
                            _this.errors = data["messages"];
                        } else {
                            //TODO: provide a callback
                            // Update the grid.
                            theApp.updateDatasetGrid();

                            _this.isNew = null;
                            _this.showDetailedView = false;
                            _this.data = {};
                            _this.message = {};
                            _this.readOnly = false;
                        }
                        //_this.$apply();
                    }, function (errorResponse) {
                        var error = errorResponse.data;
                        if (error) {
                            _this.errors = "Delete: " + error.message;
                            //_this.$apply();
                        }
                    });
                    $uibModalInstance.close();
                };
                $scope.cancel = function () {
                    $uibModalInstance.dismiss();
                };
            }
        });
    };
    
    $scope.documentCleanup = function (data) {
        removeTempProperties(data);
        
        function removeTempProperties(obj) {
            var propertyNames = Object.getOwnPropertyNames(obj);
            dojo.forEach(propertyNames, function(name) {
                var currentObj = obj[name];
                
                // Remove references to individual items of list composite terms. Items are referenced using underscore notation E.g Comp[0] = _Comp.
                if (!name.startsWith('_') && typeof currentObj === 'object' && currentObj !== null) {
                    if (currentObj instanceof Array) {
                        delete obj['_' + name]; // delete list item reference here
                        if (currentObj.length) {
                            dojo.forEach(currentObj, function(arrayObj) {
                                if (typeof arrayObj === 'object' && arrayObj !== null) {
                                    removeTempProperties(arrayObj);
                                }
                            });
                        }
                    } else {
                        removeTempProperties(currentObj);
                    }
                }
                
                // Remove _message_ property used by gridx to display error messages
                if (name === '_message_') { 
                    delete obj[name];
                }
            });
        }
        // Additional future cleanup goes here.....
    };
}]);
