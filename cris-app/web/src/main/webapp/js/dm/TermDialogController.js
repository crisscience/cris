
function TermDialogController($scope) {

    $scope.term = {};
    $scope.actions = instantiateTerm($scope.term, {});
    $scope.data = instantiateTerm($scope.term, {});
    $scope.message = instantiateTerm($scope.term, {});
    $scope.readOnly = false;
    $scope.showDetailedView = false;
    $scope.isEdit = false;

    var urlVocabs = cris.baseUrl + "vocabularys";
    var urlVocabTerms = cris.baseUrl + "vocabularys/fetchVocabTermsDetails/";
    var urlVocabTermDetails = cris.baseUrl + "vocabularys/fetchVocabTermDetails/";
    var urlVocabFromTerm = cris.baseUrl + "vocabularys/fetchTermAndVocabFromTerm/";
    var urlTemplateTerms = cris.baseUrl + "templates/load/";
    var urlTemplates = cris.baseUrl + "templates";


    $scope.loadMainTemplateTerm = function(isEdit) {
        var theTemplateApp = cris.template.index.app;
        this.showDetailedView = true;
        this.data = instantiateTerm(this.term, {});
        this.message = instantiateTerm(this.term, {}, true);
        this.readOnly = false;
        this.isEdit = isEdit;
        var _this = this;

        this.$watch(function() {
            return _this.data["type of term"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                if (value === "Database Record") {
                    attachToTermDialogTerm(urlTemplates);
                } else if (value === "Existing Term") {
                    copyTermDialogTerm(urlVocabs, false, _this, true);
                }
            }
        }, true);

        this.$watch(function() {
            return _this.data["target template"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                templateTermDialogTerm(value, urlTemplateTerms, true, _this);
            }
        }, true);

        this.$watch(function() {
            return _this.data["vocabulary"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                if (_this.data["type of term"] !== "New Term") {
                    if (_this.term[6]["name"] === "properties") {
                        _this.term.splice(6, 1);
                    }
                    vocabTermDialogTerm(value, urlVocabTerms, _this);
                }
            }
        }, true);


        this.$watch(function() {
            return _this.data["vocabulary term"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                if (_this.term[6]["name"] === "properties") {
                    _this.term.splice(6, 1);
                }
                theTemplateApp.resetNestedTermDialog();
                var nestedTermCount = vocabTermDetailsDialogTerm(value, urlVocabTermDetails, urlVocabFromTerm, false, _this, true);
                theTemplateApp.setNestedTermCount(nestedTermCount);
            }
        }, true);

        this.$apply();
    };

    $scope.loadMainVocabTerm = function(isEdit) {
        var theVocabApp = cris.vocabulary.upload;
        this.showDetailedView = true;
        this.data = instantiateTerm(this.term, {});
        this.message = instantiateTerm(this.term, {}, true);
        this.readOnly = false;
        this.isEdit = isEdit;
        var _this = this;

        this.$watch(function() {
            return _this.data["type of term"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                if (value === "New Term") {
                    newTermDialogTerm(theVocabApp.currentVocab.name, _this);
                    theVocabApp.showNestedTerm();
                } else if (value === "Existing Term") {
                    copyTermDialogTerm(urlVocabs, false, _this, false);
                    theVocabApp.showNestedTerm();
                }
            }
        }, true);

        this.$watch(function() {
            return _this.data["vocabulary"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                if (_this.data["type of term"] !== "New Term") {
                    if (_this.term[6]["name"] === "properties") {
                        _this.term.splice(6, 1);
                    }
                    vocabTermDialogTerm(value, urlVocabTerms, _this);
                }
            }
        }, true);


        this.$watch(function() {
            return _this.data["vocabulary term"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                if (_this.term[6]["name"] === "properties") {
                    _this.term.splice(6, 1);
                }
                theVocabApp.resetVocabTermDialog();
                var nestedTermCount = vocabTermDetailsDialogTerm(value, urlVocabTermDetails, urlVocabFromTerm, false, _this, false);
                theVocabApp.setNestedTermCount(nestedTermCount);
            }
        }, true);

        this.$watch(function() {
            return _this.data["validation type"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                if (_this.data["type of term"] === "New Term") {
                    if (_this.term[5]["name"] === "properties") {
                        _this.term.splice(5, 1);
                    }
                    if (value !== "boolean" && value !== "not defined") {
                        validationDialogTerm(value);
                    }
                }
            }
        }, true);

        this.$watch(function() {
            return _this.data["properties"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined && !_this.isEdit) {
                if (_this.data["properties"]["alias"]) {
                    predefinedDialogTerm(_this.data["properties"]["alias"]);
                }

            }
        }, true);

        this.$apply();
    };

    $scope.loadEditTerm = function(isEdit) {
        this.showDetailedView = true;
        this.data = instantiateTerm(this.term, {});
        this.message = instantiateTerm(this.term, {}, true);
        this.readOnly = false;
        this.isEdit = isEdit;
        this.$apply();
    };

    $scope.loadNestedVocabTerm = function(isTemplate) {
        this.showDetailedView = true;
        this.term = {};
        this.data = instantiateTerm(this.term, {});
        this.message = instantiateTerm(this.term, {}, true);
        this.readOnly = false;
        var _this = this;

        this.$watch(function() {
            return _this.data["vocabulary"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined) {
                if (_this.term[5]["name"] === "properties") {
                    _this.term.splice(5, 1);
                }
                vocabTermDialogTerm(value, urlVocabTerms, _this);
            }
        }, true);

        this.$watch(function() {
            return _this.data["vocabulary term"];
        }, function(value) {
            if (value !== null && value !== "" && value !== undefined) {
                if (_this.term[5]["name"] === "properties") {
                    _this.term.splice(5, 1);
                }
                vocabTermDetailsDialogTerm(value, urlVocabTermDetails, null, true, _this, isTemplate);
            }
        }, true);

        this.$apply();
    };
}
