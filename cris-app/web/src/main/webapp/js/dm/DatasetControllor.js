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

angular.module("crisDataset", ["dataset"]);

angular.module("crisDataset").controller("DatasetController", ["$scope", function ($scope) {
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
        theApp.datasetGrid.selection.clear();

        this.showDetailedView = true;
        this.isNew = true;

        this.term = getTerm(this.currentTemplate.id, this.currentTemplate.version, true);
        this.data = instantiateTerm(this.term, {});
        this.data = dojo.mixin(this.data, metaFields);
        this.context = this.data;

        var project = theApp.projectList.get("value");
        var experiment = theApp.experimentList.get("value");
        this.currentProject = (project && project !== "0") ? project: null;
        this.currentExperiment = (experiment && experiment !== "0") ? experiment : null;
        this.currentJob = null;
        this.currentTask = null;
        this.timeCreated = null;
        this.timeUpdated = null;

        this.message = {};
        this.errors = "";
        this.readOnly = false;

        this.$apply();
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
        // check if all input are valid
        var formWidget = dijit.byId("idDatasetForm");
        var valid = formWidget.validate();
        if (!valid) {
            return;
        }

        var templateTermName = getTermName(this.currentTemplate);

        var payload = {};
        payload.isIframe = true;
        payload.projectId = this.currentProject;
        payload.experimentId = this.currentExperiment;
        payload.jobId = this.currentJob;
        payload.taskId = this.currentTask;
        payload[templateTermName] = dojo.toJson(this.data);

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
                } else {
                    if (data["status"]) {
                        var termName = getTermName(_this.currentTemplate);
                        var status = data["status"];
                        var message = status[termName][_this.currentTemplate.name];

                        // update with error message
                        _this.message = message;
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
        showConfirmYesNo({title: "Confirm", message: "Are you sure you want to delete the record?",
            buttons: [
                {label: "Yes", callBack: function() {
                        var query = _this.currentTemplate.id + "/" + _this.data[MetaField_Id].$oid;
                        //TODO: use a separate store
                        require(["dojo/request/xhr"], function(xhr){
                            xhr(urlRestObjectus + query, {
                                method: "DELETE",
                                handleAs: "json"
                            }).then(function(data) {
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
                                _this.$apply();
                            }, function(error){
                                if (error.response && error.response.text) {
                                    var errors = dojo.fromJson(error.response.text);
                                    _this.errors = "Delete: " + errors.message;
                                    _this.$apply();
                                }
                            });
                        });
                    }},
                {label: "No", callBack: function() {
                    }}
            ]
        });
    };
}]);
