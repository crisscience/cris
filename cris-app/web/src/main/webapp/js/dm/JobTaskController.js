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

angular.module("crisTask", ["dataset"]);

angular.module("crisTask").controller("JobTaskController", ["$scope", function ($scope) {
    var urlRestObjectus = cris.baseUrl + "rest/objectus/";

    // related to document
    $scope.term = {};
    $scope.data = instantiateTerm($scope.term, {});
    $scope.message = instantiateTerm($scope.term, {});
    $scope.readOnly = false;

    // current job and task
    $scope.currentProjectId = null;
    $scope.currentExperimentId = null;
    $scope.currentJobId = null;
    $scope.currentTaskId = null;
    $scope.currentTemplate = null;

    // whether the current document has been saved.
    $scope.isSaved = null;

    // open an existing or empty document
    $scope.openDocument = function(term, data, message, context) {
        resetFileUploaders("idForm");

        this.term = term;
        this.data = instantiateTerm(term, (data ? data : {}));
        this.message = instantiateTerm(term, (message ? message : {}), true);
        this.context = this.data;
        this.context._init = context;
        this.readOnly = false;
        this.$apply();
    };

    // save the current document
    $scope.saveDocument = function(validCallback, invalidCallback, errorCallback) {
        // check if all input are valid
        var formWidget = dijit.byId("idForm");
        var valid = formWidget.validate();
        
        $scope.mainForm.submitted = true;
        
        if (!valid || $scope.mainForm.$valid === false) {
            $scope.errors = "Please correct any errors highlighted in red.";
            errorCallback();
            return;
        }
        $scope.errors = "";

        var _initTemp = this.data._init;
        delete this.data._init;

        var templateTermName = getTermName(this.currentTemplate);

        var payload = {};
        payload.isIframe = true;
        payload.projectId = this.currentProjectId;
        payload.experimentId = this.currentExperimentId;
        payload.jobId = this.currentJobId;
        payload.taskId = this.currentTaskId;
        
        var _data = angular.copy(this.data);
        removeIllegalProperties(_data);
        payload[templateTermName] = angular.toJson(_data);

        var _this = this;
        dojo.io.iframe.send({
            url: urlRestObjectus,
            method: "post",
            form: "idForm",
            content: payload,
            handleAs: "json",
            load: function(data, ioArgs) {
                if (data["isValid"] === true) {
                    if (validCallback) {
                        validCallback(data, ioArgs);
                    }
                } else {
                    if (invalidCallback) {
                        invalidCallback(data, ioArgs);
                    }
                    _this.data._init = _initTemp; // re-add the workflow jsonIn

                    // update with error message
                    var termName = getTermName(_this.currentTemplate);
                    var status = data["status"];
                    var message = status[termName][_this.currentTemplate.name];
                    _this.message = message;
                    _this.$apply();
                }
            },
            error: function(error, ioArgs) {
                if (errorCallback) {
                    errorCallback(error, ioArgs);
                }
            }
        });
    };

    function removeIllegalProperties(obj) {
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
                                removeIllegalProperties(arrayObj);
                            }
                        });
                    }
                } else {
                    removeIllegalProperties(currentObj);
                }
            }
            
            // Add anymore items to remove in the future.....
            
        });
    }
}]);
