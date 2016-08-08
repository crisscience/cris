
function TermController($scope) {

    $scope.term = {};
    $scope.actions = instantiateTerm($scope.term, {});
    $scope.data = instantiateTerm($scope.term, {});
    $scope.message = instantiateTerm($scope.term, {});
    $scope.readOnly = false;
    $scope.showDetailedView = false;

    $scope.loadTerm = function(term, isTemplate) {

        this.showDetailedView = true;
        this.term = term;
        this.data = instantiateTerm(this.term, {});
        this.message = instantiateTerm(this.term, {}, true);
        this.actions = [{
                icon: "famfamfam_silk_icons_v013/icons/pencil_add.png",
                text: "Edit",
                onClick: function(id, term, dataset, message, readOnly) {
                    if (isTemplate) {
                        var theTemplateApp = cris.template.index.app;
                        theTemplateApp.editTerm(term, dataset);
                    } else {
                        var theVocabApp = cris.vocabulary.upload;
                        theVocabApp.editTerm(term, dataset);
                    }
                },
                forField: true
            },
            {
                icon: "delete.png",
                text: "Delete",
                onClick: function(id, term, dataset, message, readOnly) {
                    deleteTerm(term, isTemplate);
                },
                forField: true
            }];
        this.$apply();
    };

    $scope.newTerm = function(isTemplate) {

        this.showDetailedView = true;
        this.term = [];
        this.data = {};
        this.message = {};
        this.actions = [{
                icon: "famfamfam_silk_icons_v013/icons/pencil_add.png",
                text: "Edit",
                onClick: function(id, term, dataset, message, readOnly) {
                    if (isTemplate) {
                        var theTemplateApp = cris.template.index.app;
                        theTemplateApp.editTerm(term, dataset);
                    } else {
                        var theVocabApp = cris.vocabulary.upload;
                        theVocabApp.editTerm(term, dataset);
                    }
                },
                forField: true
            },
            {
                icon: "delete.png",
                text: "Delete",
                onClick: function(id, term, dataset, message, readOnly) {
                    deleteTerm(term, isTemplate);
                },
                forField: true
            }];
        this.$apply();
    };
}
