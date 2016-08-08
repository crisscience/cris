// Meta fields


function TemplateController($scope) {
    var theApp = cris.template.index.app;
    var urlVocabTerm = cris.baseUrl + "vocabularys/fetchVocabTerms/";

    $scope.term = {};
    $scope.actions = instantiateTerm($scope.term, {});
    $scope.data = instantiateTerm($scope.term, {});
    $scope.message = instantiateTerm($scope.term, {});
    $scope.readOnly = false;

    $scope.showDetailedView = false;

    $scope.loadTemplate = function(templateUuid, templateName, templateVersion) {

        var term = getTerm(templateUuid, templateVersion, true);
        if (term === undefined) {
            term = [];
        }
        var sortedTerms = theApp.checkOrderTerm(term);
     
        this.showDetailedView = true;
        this.term = sortedTerms;
        this.data = instantiateTerm(this.term, {});
        this.message = instantiateTerm(term, {}, true);
        this.actions = [{
                icon: "famfamfam_silk_icons_v013/icons/pencil_add.png",
                text: "Edit",
                onClick: function(id, term, dataset, message, readOnly) {
                    theApp.editTempTerm(term);
                },
                forField: true
            },
            {
                icon: "delete.png",
                text: "Delete",
                onClick: function(id, term, dataset, message, readOnly) {
                    theApp.deleteTerm(term);
                },
                forField: true
            }];
        this.readOnly = true;
        this.$apply();

        theApp.hideLoadTemplateDialog();
        if (term.length === 0) {
            showMessage("There are no terms in the template. Please add terms ...");
        }
        theApp.editButtonDisplay();
        theApp.setTitle(templateName);
    };

    $scope.newTemplate = function(templateName, templateDesc, templateUuid, templateVersion) {

        this.showDetailedView = true;
        this.term = [];
        this.data = {};
        this.message = {};
        this.actions = [{
                icon: "famfamfam_silk_icons_v013/icons/pencil_add.png",
                text: "Edit",
                //onHover: "aaa",
                onClick: function(id, term, dataset, message, readOnly) {
                    theApp.editTempTerm(term);
                },
                forField: true
            },
            {
                icon: "delete.png",
                text: "Delete",
                onClick: function(id, term, dataset, message, readOnly) {
                    theApp.deleteTerm(term);
                },
                forField: true
            }];
        this.readOnly = true;
        this.$apply();

        theApp.editButtonDisplay();
        theApp.setNewTitle(templateName, templateDesc, templateUuid, templateVersion);
    };
}
