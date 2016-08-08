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

function SearchController($scope) {
    // related to document
    $scope.term = {};
    $scope.readOnly = true;

    // current selections
    $scope.currentProject = null;
    $scope.currentExperiment = null;
    $scope.currentJob = null;
    $scope.currentTemplate = null;
    $scope.currentTemplateVersion = null;
    $scope.experiment = null;
    $scope.project = null;
    $scope.job = null;

    $scope.showDetailedView = false;

    // open an existing document
    $scope.openDocument = function(doc) {
        this.showDetailedView = true;
        this.currentTemplate = doc["_type"].replace("dataset_", "").replace(/_/g, "-");
        this.currentProject = doc["_source"]["_project_id"];
        this.currentTemplateVersion = doc["_source"]["_template_version"];
        this.currentExperiment = doc["_source"]["_experiment_id"];
        this.currentJob = doc["_source"]["_job_id"];
        this.term = getTerm(this.currentTemplate, this.currentTemplateVersion, true);
        this.data = doc["_source"];
        this.experiment = getDetails(this.currentExperiment, "experiments");
        this.project = getDetails(this.currentProject, "projects");
        this.job = getDetails(this.currentJob, "jobs");
        this.$apply();
    };

    $scope.hideDetailedView = function() {
        $scope.showDetailedView = false;
        this.$apply();
    };

    function getDetails(id, type) {
        var details;
        if (id) {
            var url = cris.baseUrl + type + "/" + id;
            var data = dojo.xhr.get({
                url: url,
                handleAs: "json",
                headers: {
                    Accept: 'application/json'
                },
                sync: true
            }).results;
            details = data[0];
        } else {
            details = {};
        }

        return details;
    }

    $scope.reSearchProject = function() {
        var value = this.currentProject;
        linkedSearch("_project_id", value.toString());
    };
    
    $scope.reSearchExperiment = function() {
        var value = this.currentExperiment;
        linkedSearch("_experiment_id", value.toString());
    };
    
    $scope.reSearchJob = function() {
        var value = this.currentJob;
        linkedSearch("_job_id", value.toString());
    };
}
