/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
require([
    "dojox/uuid/generateRandomUuid",
    "dojo/store/Memory"
]);

function parseDataMessage(data, defaultMessage) {
    var message = null;
    if (data.hasError) {
        message = data.message;
    } else {
        if (data.message) {
            message = data.message;
        } else {
            message = defaultMessage ? defaultMessage : "";
        }
    }
    return message;
}

function parseErrorMessage(error) {
    var message = null;

    if (error && error.response && error.response.data) {
        var data = error.response.data;
        if (data.hasError) {
            message = data.message;
        } else {
            message = data;
        }
    } else if (error && error.response && error.response.text) {
        message = error.response.text;
    } else if (error && error.message) {
        message = error.message;
    } else {
        message = error;
    }
    return message;
}

function _getTermType(term) {
    var type = null;

    if (term) {
        if (term.hasOwnProperty('domain')) {
            type = "vocabulary";
        } else if (term.hasOwnProperty('idField') && term.hasOwnProperty('nameField')) {
            type = "attachTo";
        } else if (term.hasOwnProperty('alias') && term.alias) {
            type = "reference";
        } else if ((term.validation && term.validation.validator[0].type === "composite") || term.isRoot) {
            type = "composite";
        } else {
            type = "simple";
        }
    }

    return type;
}

function moveAttachToToTerm(template) {
    var terms = template.term;
    for (var index in terms) {
        moveAttachToToTerm(terms[index]);
    }

    var attachTos = template.attachTo;
    for (var index in attachTos) {
        var attachTo = attachTos[index];
        attachTo.type = "attachTo";
        template.term.push(attachTo);
    }
    template.attachTo.length = 0;
    template.term = sortOnUiDisplayOrder(template.term);

    return template;
}

function moveTermToAttachTo(template) {
    if (!template) {
        return template;
    }

    if (dojo.isArray(template.term)) {
        for (var index in template.term) {
            if (dojo.isArray(template.term[index].term)) {
                moveTermToAttachTo(template.term[index]);
            }
        }

        var length = template.term.length;
        if (!template.attachTo) {
            template.attachTo = [];
        }
        for (var i = length - 1; i >= 0; i--) {
            var term = template.term[i];
            if (term.type === "attachTo") {
                template.term.splice(i, 1);
                template.attachTo.push(term);
            }
        }
    }

    return template;
}

function setUiDisplayOrder(template) {
    // this should be called before moveTermToAttachTo

    if (!template) {
        return;
    }

    var terms = template.term;
    for (var index in terms) {
        var term = terms[index];
        term.uiDisplayOrder = index;
        setUiDisplayOrder(term);
    }
}

function sortOnUiDisplayOrder(term) {
    for (var index in term.term) {
        // sort sub-terms first
        term.term[index] = sortOnUiDisplayOrder(term.term[index]);
    }

    var store = new dojo.store.Memory({data: term.term});
    var sortedTerms = store.query(null, {sort: [{attribute: "uiDisplayOrder", descending: false}]});
    term.term = sortedTerms;
    return term;
}

function addUniqueId(term) {
    for (var index in term.term) {
        term.term[index] = addUniqueId(term.term[index]);
    }

    term.$$uuid = dojox.uuid.generateRandomUuid();

    return term;
}

function removeUniqueId(term) {
    if (!term) {
        return term;
    }

    for (var index in term.term) {
        term.term[index] = removeUniqueId(term.term[index]);
    }

    delete term.$$uuid;

    return term;
}

var crisVocabulary = angular.module("crisVocabulary", ['angular-dojo', 'dataset', 'ui.bootstrap']);

crisVocabulary.directive("crisView", function($compile) {
    return {
        restrict: "E",
        replace: true,
        templateUrl: cris.baseUrl + "vocabularys/partials/view",
        link: function(scope, element, attrs) {
            console.log("==== view link: begin ====");

            /****************
             * model -> view
             ****************/
            scope.$watch('view', function (value) {
                console.log("==== view controller: $watch ====");
                console.dir(value);
            });
        },
        controller: function($scope) {
            console.log("==== view controller: begin ====");
            console.log("==== term controller: end ====");
        }
    };
});

crisVocabulary.directive("crisTerm", function($compile, $timeout) {
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
        link: function(scope, element, attrs) {
            console.log("==== term link: begin ====");
            scope.rootParentScope = scope.$parent;

            /****************
             * model -> view
             ****************/
            scope.$watch('ngModel', function (value) {
                if (scope.ngModel) {
                    var type = _getTermType(scope.ngModel);
                    if (type === "vocabulary") {
                        // vocabulary metadata
                        scope.type = "view_vocabulary_metadata";
                    } else if (type === "attachTo") {
                        // attach-to term
                        scope.type = "view_term_attachto";
                    } else if (type === "reference") {
                        // reference term
                        scope.type = "view_term_reference";
                    } else if (type === "composite") {
                        // composite
                        scope.type = "view_term_composite";
                    } else if (type === "simple") {
                        // simple term
                        scope.type = "view_term_simple";
                    } else {
                        //TODO: what to do
                        scope.type = "view_term_empty";
                    }
                    
                    if ((type === "reference" || type === "simple") && scope.ngModel.childOfCompositeReference) {
                        scope.type = "view_term_composite_reference_child";
                        $timeout(function () {
                            // Disable all widgets in template
                            dojo.query('input', element[0]).forEach(function (input) {
                                var widget = dijit.getEnclosingWidget(input);
                                widget.set('disabled', true);
                            });
                            // Remove images in template...E.g. the add/remove (+/-) img buttons in validation
                            dojo.query('img', element[0]).forEach(function (img) {
                                dojo.destroy(img);
                            });
                        }, 300);
                    }
                } else {
                    scope.type = "view_term_empty";
                }
                console.log("==== term controller: $watch ====");
                console.dir(value);
            });
        },
        controller: function($scope) {
            console.log("==== term controller: begin ====");
            $scope.isNew = function() {
                if ($scope.term.uuid) {
                    return false;
                } else {
                    return true;
                }
            };

            $scope.create = function(type) {
                $scope.term = {};
                $scope.$apply();
            };

            $scope.put = function(term) {
                $scope.term = term;
                $scope.$apply();
            };

            $scope.get = function() {
                return $scope.term;
            };
            console.log("==== term controller: end ====");
        }
    };
});

crisVocabulary.directive("crisTermValidator", function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: false,
        scope: {
            isNew: "@",
            ngModel: "="
        },
        templateUrl: cris.baseUrl + "vocabularys/partials/view_term_validator",
        link: function(scope, element, attrs) {
            console.log("==== validator link: begin ====");
            
            /****************
             * model -> view
             ****************/
            scope.$watch('ngModel.type', function () {
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
                        scope.cleanupValiationProperties([]);
                    } else if (scope.ngModel.type === "numeric") {
                        scope.viewType = "view_term_validator_numeric";
                        var props = store.query({name: "range"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "range", value: "[-infinity, +infinity]"});
                        }
                        scope.cleanupValiationProperties(['range']);
                    } else if (scope.ngModel.type === "text") {
                        scope.viewType = "view_term_validator_text";
                        var props = store.query({name: "type"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "type", value: "printable"});
                        }
                        var props = store.query({name: "length"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "length", value: null});
                        }
                        var props = store.query({name: "ui-vertical-lines"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "ui-vertical-lines", value: "1"});
                        }
                        scope.cleanupValiationProperties(['type', 'length', 'ui-vertical-lines']);
                    } else if (scope.ngModel.type === "date" || scope.ngModel.type === "time" || scope.ngModel.type === "date-time") {
                        scope.viewType = "view_term_validator_date";
                        scope.type = "date";
                        var props = store.query({name: "format"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "format", value: "date"});
                        }
                        scope.cleanupValiationProperties(['format']);
                    } else if (scope.ngModel.type === "list") {
                        scope.viewType = "view_term_validator_list";
                        var props = store.query({name: "isMultiSelect"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "isMultiSelect", value: null});
                        }
                        scope.cleanupValiationProperties(['isMultiSelect', 'item']);
                    } else if (scope.ngModel.type === "file") {
                        scope.viewType = "view_term_validator_file";
                        var props = store.query({name: "multiple"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "multiple", value: null});
                        }
                        var props = store.query({name: "globus"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "globus", value: null});
                        }
                        scope.cleanupValiationProperties(['multiple', 'globus']);
                    } else if (scope.ngModel.type === "advanced") {
                        scope.viewType = "view_term_validator_advanced";
                        var props = store.query({name: "regexp"});
                        if (props.length === 0) {
                            scope.ngModel.property.push({name: "regexp", value: null});
                        }
                        scope.cleanupValiationProperties(['regexp']);
                    } else if (scope.ngModel.type === "composite") {
                        scope.viewType = "view_term_validator_composite";
                        scope.cleanupValiationProperties([]);
                    } else {
                        //TODO: what to do
                        scope.viewType = "view_term_validator_text";
                    }
                } else {

                }
            });
            console.log("==== validator link: end ====");
        },
        controller: function($scope) {
            console.log("==== validator controller: begin ====");

            // remove properties left behind by previous validation type (e.g. when switching from file to numeric)
            $scope.cleanupValiationProperties = function (propertyNames) {
                var properties = $scope.ngModel.property;
                for (var i = 0; i < properties.length; i++) {
                    if (propertyNames.indexOf(properties[i].name) === -1) {
                        properties.splice(i--, 1);
                    }
                }
            }

            console.log("==== validator controller: end ====");
        }
    };
});

crisVocabulary.directive("crisTermDefaultValue", function($compile) {
    return {
        restrict: "E",
        replace: true,
        scope: {
          ngModel: "="
        },
        link: function(scope, element, attrs) {

            require([
                "dijit/form/DateTextBox",
                "dojox/form/CheckedMultiSelect",
                "dijit/form/FilteringSelect",
                "dijit/form/TimeTextBox",
                "dijit/form/TextBox",
                "dijit/form/ValidationTextBox",
                "dojo/store/Memory",
                "dojo/data/ObjectStore",
                "dojo/aspect"], function(DateTextBox, CheckedMultiSelect, FilteringSelect, TimeTextBox, TextBox, ValidationTextBox, MemoryStore, ObjectStore, aspect) {

                function makeDefaultValueWidget(){
                    console.log("======= Term default value ==========");
                    console.dir(scope);

                    var widget, validator;
                    if (scope.ngModel && scope.ngModel.type === 'attachTo') {
                        var idField = scope.ngModel["idField"] ? scope.ngModel["idField"] : "id";
                        var uuid = scope.ngModel.uuid;
                        var query = scope.ngModel.query;
                        var baseUrl = cris.baseUrl + "rest/objectus/" + uuid;
                        
                        var queryObj = {};
                        queryObj[idField] = {$exists:true,$ne:null};
                        queryObj[scope.ngModel.nameField] = {$exists:true,$ne:null};

                        if (query) {
                            var q = dojo.fromJson(query);
                            dojo.mixin(queryObj, q);
                        }
                        query = dojo.toJson(queryObj);
                        var url = baseUrl + "/?query=" + query;
                        var store = new createJsonRestStore(url, idField, scope.ngModel.nameField);

                        widget = new FilteringSelect({
                            searchAttr: scope.ngModel.nameField,
                            labelAttr: scope.ngModel.nameField,
                            store: store
                        });
                        store.fetch({}); // Fetch initial values

                        scope.$watchCollection('[ngModel.idField,ngModel.nameField,ngModel.uuid]', function(newValue, oldValue){
                            if ((newValue[0] && newValue[0] !== oldValue[0] || newValue[1] && newValue[1] !== oldValue[1]) && newValue[2] === oldValue[2]) {
                                store.idAttribute = scope.ngModel.idField;
                                store.labelAttribute = scope.ngModel.nameField;
                                widget.searchAttr = scope.ngModel.nameField;
                                widget.labelAttr = scope.ngModel.nameField;
                                store.fetch({});
                            }
                        });

                        widget.onChange = function (value) {
                            var type = 'string';
                            dojo.forEach(scope.ngModel.fieldList, function(fieldObj){
                                if (fieldObj.id === scope.ngModel.idField) {
                                    type = fieldObj.validation ? fieldObj.validation.validator[0].type : 'string';
                                }
                            });

                            if (['numeric', 'boolean'].indexOf(type) > -1) {
                                scope.setDefaultValue(value ? value : "");
                            } else {
                                scope.setDefaultValue(value ? dojo.toJson(value) : "");
                            }
                        };
                    } else { // Simple terms (those with validation properties...numeric, text, etc.)
                        if (!scope.ngModel) {
                            return;
                        }

                        validator = scope.ngModel.validation && scope.ngModel.validation.validator ? scope.ngModel.validation.validator[0] : {};
                        if (validator.type === "boolean") {
                            var store = new MemoryStore({
                                data: [
                                    {id: "", name: ""},
                                    {id: "true", name: "True"},
                                    {id: "false", name: "False"}
                                ]
                            });

                            widget = new FilteringSelect({
                                store: store
                            });

                            widget.onChange = function (value) {
                                scope.setDefaultValue(value ? value : "");
                            };
                        } else if (validator.type === "date-time") {
                            widget = new DateTextBox({value: null});
                            if (validator.property[0] && validator.property[0].name === 'format' && validator.property[0].value === 'time') {
                                widget = new TimeTextBox({name: 'defaultTimeTextBox', value: null});
                            }

                            widget.onChange = function (value) {
                                scope.setDefaultValue(value ? dojo.toJson(value) : "");
                            };
                        } else if (validator.type === "list") {
                            var data = [];
                            var isMultiSelect = false;

                            dojo.forEach (validator.property, function(prop) {
                                if (prop.name === 'isMultiSelect' && prop.value === 'true') {
                                    isMultiSelect = true;
                                }
                                if (typeof prop.id !== 'undefined' && prop['value'] && prop.name === 'item') {
                                    data.push({id: prop['id'] ? prop['id'] : prop['value'], name: prop['value']});
                                }
                            });

                            if (isMultiSelect) {
                                widget = new CheckedMultiSelect({
                                    multiple: true,
                                    store: new ObjectStore({objectStore: new MemoryStore({idProperty: 'id', data: data}), labelProperty: 'name'})
                                });
                                widget.onChange = function (value) {
                                    if (!isExpression(scope.ngModel.value)) {
                                        scope.setDefaultValue(value ? dojo.toJson(value) : "");
                                    }
                                };
                            } else { // Dropdown select
                                data.unshift({id: "", name: ""});
                                widget = new FilteringSelect({
                                    searchAttr: 'name',
                                    store: new MemoryStore({idProperty: 'id', data: data})
                                });
                                widget.onChange = function (value) {
                                   scope.setDefaultValue(this.value ? dojo.toJson(value) : "");
                                };
                            }
                        } else if (validator.type === "numeric") {
                            widget = new ValidationTextBox({});
                            widget.onChange = function (value) {
                                scope.setDefaultValue(this.value ? this.value : "");
                            };
                        } else { // All others, e.g. composite terms, etc.
                            widget = new ValidationTextBox({});
                            widget.onChange = function(value) {
                                var value_ = value ? value : "";
                                if (isExpression(value_) || isJsonLike(value_)) {
                                    scope.setDefaultValue(value_);
                                } else {
                                    scope.setDefaultValue(value_ ? dojo.toJson(value_) : null);
                                }
                            }
                        }
                        // Cleanup existing values that aren't well formatted
                        if (['boolean', 'numeric', 'list'].indexOf(validator.type) === -1 && !isQuoted(scope.ngModel.value) && scope.ngModel.value) {
                            if (!isExpression(scope.ngModel.value) && !isJsonLike(scope.ngModel.value)) {
                                scope.ngModel.value = dojo.toJson(scope.ngModel.value);
                            }
                        }
                    }
                    dojo.empty(element[0]);
                    if (widget) {
                        var expressionTextbox = new ValidationTextBox({
                            regExp: '^(?![$][{]).*' // Must not start with '${' (an expression)
                        });

                        widget.placeAt(element[0]);
                        expressionTextbox.placeAt(element[0]);

                        expressionTextbox.onChange = function(value) {
                            if (value && this.isValid()) {
                                widget.set('value', "", false);

                                if (widget.domNode.className.indexOf('dojoxCheckedMultiSelect') !== -1) {
                                    widget._updateSelection(); // Necessary to clear checkedMultiSelect
                                }

                                // For some widgets we need to clear the dom node value too
                                var widgetInDom = dojo.byId(widget.id);
                                if (widgetInDom) {
                                    dojo.setAttr(widgetInDom, 'value', "");
                                }

                                widget.set('disabled', true);
                                scope.setDefaultValue('${' + value + '}');
                            } else {
                                widget.set('disabled', false);
                                scope.setDefaultValue("");
                            }
                        }

                        // Disable expression box where it is not necessary
                        if (validator && validator.type === 'composite') {
                            expressionTextbox.set('disabled', true);
                        }

                        var defaultValue;
                        if (isExpression(scope.ngModel.value)) {
                            defaultValue = scope.ngModel.value.trim().substring(2, scope.ngModel.value.length - 1);
                            expressionTextbox.set('value', defaultValue);
                        } else {
                            if ((validator && validator.type === 'list' && scope.ngModel.value) || isQuoted(scope.ngModel.value)) {
                                defaultValue = dojo.fromJson(scope.ngModel.value);
                            } else {
                                defaultValue = scope.ngModel.value;
                            }

                            if (typeof defaultValue === 'string' && defaultValue.trim() === "") {
                                widget.set('value', null);
                            } else {
                                widget.set('value', defaultValue);
                            }
                        }

                        angular.element(expressionTextbox.domNode).wrap('<div style="display:table;margin-top:2px;"></div>');
                        dojo.setStyle(expressionTextbox.domNode, 'display', 'table-cell');
                        dojo.place('<span style="display:table-cell;vertical-align:middle">Exp:&nbsp;</span>', expressionTextbox.domNode, "before");
                    }
                }

                makeDefaultValueWidget();
                scope.$watchCollection('ngModel', function () {
                    makeDefaultValueWidget();

                    if (scope.validatorWatcher) {
                        scope.validatorWatcher(); // unbind previous validator watcher
                    }

                    var validatorWatcher = scope.$watch('ngModel.validation.validator[0]', function (newValue, oldValue) { // Deep watch properties (e.g. list items, date-time format, etc)
                        if (dojo.toJson(newValue) !== dojo.toJson(oldValue)) {
                            makeDefaultValueWidget();
                        }
                    }, true);
                    scope.validatorWatcher = validatorWatcher;
                });
            });
        },
        controller: function($scope) {
            $scope.setDefaultValue = function(value) {
                $scope.ngModel.value = value;
                $scope.$apply();
            }
        }
    }
});

crisVocabulary.directive("crisCheckboxAndExpression", function($compile) {
    return {
        restrict: "E",
        replace: true,
        scope: {
          ngModel: "=",
          field: "=",
          readOnly: "@"
        },
        link: function(scope, element, attrs) {
            require([
                "dijit/form/CheckBox",
                "dijit/form/ValidationTextBox",
                "dojo/on"], function(CheckBox, ValidationTextBox, on) {

                var field = attrs.field;
                var fieldExpression = field + "Expression";

                var checkBox = new CheckBox({
                    disabled: (scope.ngModel[fieldExpression] || scope.readOnly) ? true : false,
                });
                checkBox.setValue(scope.ngModel[field] === true ? true : false);

                var validationTextBox = new ValidationTextBox({
                    disabled: scope.readOnly ? true : false
                });
                validationTextBox.setValue(scope.ngModel[fieldExpression] ? scope.ngModel[fieldExpression] : "");

                on(checkBox, "change", function (checked) {
                    if (checked) {
                        scope.ngModel[field] = true;
                    } else {
                        scope.ngModel[field] = null;
                    }
                    scope.$apply();
                });

                on(validationTextBox, "change", function (value) {
                    if (value) {
                        scope.ngModel[fieldExpression] = value;
                        scope.ngModel[field] = null;
                        checkBox.setValue(0);
                        checkBox.set('disabled', true);
                    } else {
                        scope.ngModel[fieldExpression] = "";
                        checkBox.set('disabled', false);
                    }
                    scope.$apply();
                });

                scope.$watchCollection('[ngModel.' + field + ',' + 'ngModel.' + fieldExpression + ']', function(newValue, oldValue){
                    if (scope.ngModel && newValue[0] !== oldValue[0]) {
                        checkBox.set('value', newValue[0] === true ? true : false);
                    }
                    if (scope.ngModel && newValue[1] !== oldValue[1]) {
                        validationTextBox.set('value', newValue[1]);
                    }
                });

                checkBox.placeAt(element[0]);
                validationTextBox.placeAt(element[0]);

                // Align widgets side-by-side
                angular.element(checkBox.domNode).wrap('<span style="display:inline-block;width:10%;"></span>')
                angular.element(validationTextBox.domNode).wrap('<span style="display:inline-block;width:90%;"></span>')
            });
        }
    }
});

crisVocabulary.directive("crisTemplateVersion", function() {
    return {
        restrict: "E",
        replace: true,
        scope: {
          ngModel: "=",
          rootParentScope: "="
        },
        link: function(scope, element, attrs) {
            require([
                "dijit/form/FilteringSelect",
                "dojo/request/xhr",
                "dojo/store/Memory",
                "dojo/date/locale"], function(FilteringSelect, xhr, MemoryStore, locale) {

                scope.$watch('ngModel.id', function(value){
                    if (value) {
                        dojo.empty(element[0]);

                        var url = cris.baseUrl +  "templates/versions/" + scope.ngModel.uuid;
                        xhr(url, {
                            handleAs: "json"
                        }).then(function(data){
                            var templateInfo = {};
                            var storeData = [];
                            dojo.forEach(data, function(item){
                                var version = item['versionNumber']['$uuid'];
                                var timeUpdated = locale.format(new Date(item['timeUpdated']['$date']), {selector: "date", datePattern: "yyyy/MM/dd, hh:mm:ss a"});
                                templateInfo[version] = {id: item.id, data: item};
                                storeData.push({id: version, name: timeUpdated});
                            });

                            var filteringSelect = new FilteringSelect({
                                searchAttr: 'name',
                                value: scope.ngModel.version,
                                store: new MemoryStore({idProperty: 'id', data: storeData})
                            });

                            filteringSelect.onChange = function(value) {
                                var templateId = templateInfo[value].id;
                                var templateData = templateInfo[value].data;
                                xhr(cris.baseUrl + 'templates/load/' + templateId, {
                                    handleAs: "json"
                                }).then(function(data){
                                    scope.rootParentScope.loadTemplate(templateData, data)
                                });
                            };
                            filteringSelect.placeAt(element[0]);
                        }, function(err){
                            // Handle Error
                            var filteringSelect = new FilteringSelect({});
                            filteringSelect.placeAt(element[0]);
                        });
                    }
                });
            });
        }
    }
});

crisVocabulary.directive("crisVocabularyVersion", function() {
    return {
        restrict: "E",
        replace: true,
        scope: {
          ngModel: "=",
          rootParentScope: "="
        },
        link: function(scope, element, attrs) {
            require([
                "dijit/form/FilteringSelect",
                "dojo/request/xhr",
                "dojo/store/Memory",
                "dojo/date/locale"], function(FilteringSelect, xhr, MemoryStore, locale) {

                scope.$watch('ngModel.id', function(value){
                    if (value) {
                        dojo.empty(element[0]);

                        var url = cris.baseUrl +  "vocabularys/versions/" + scope.ngModel.uuid;
                        xhr(url, {
                            handleAs: "json"
                        }).then(function(data){
                            var vocabularyInfo = {};
                            var storeData = [];
                            dojo.forEach(data, function(item){
                                var version = item['versionNumber']['$uuid'];
                                var timeUpdated = locale.format(new Date(item['timeUpdated']['$date']), {selector: "date", datePattern: "yyyy/MM/dd, hh:mm:ss a"});
                                vocabularyInfo[version] = {id: item.id, data: item};
                                storeData.push({id: version, name: timeUpdated});
                            });

                            var filteringSelect = new FilteringSelect({
                                searchAttr: 'name',
                                value: scope.ngModel.version,
                                store: new MemoryStore({idProperty: 'id', data: storeData})
                            });

                            filteringSelect.onChange = function(value) {
                                var vocabularyId = vocabularyInfo[value].id;
                                var vocabularyData = vocabularyInfo[value].data;
                                xhr(cris.baseUrl + 'vocabularys/load/' + vocabularyId, {
                                    handleAs: "json"
                                }).then(function(data){
                                    scope.rootParentScope.loadVocabulary(vocabularyData, data)
                                });
                            };
                            filteringSelect.placeAt(element[0]);
                        }, function(err){
                            // Handle Error
                            var filteringSelect = new FilteringSelect({});
                            filteringSelect.placeAt(element[0]);
                        });
                    }
                });
            });
        }
    }
});

crisVocabulary.controller("PageController", ['$scope', '$uibModal', function ($scope, $uibModal) {

    $scope.ContributorDefinition = {list: true, type: "text", isDefinition: true, name: "contributor"};

    $scope.errors = {message: ""};
    $scope.timer = null;

    $scope.tabContainer = null;
    $scope.vocabularyGrid = null;
    $scope.templateGrid = null;
    $scope.referenceGrid = null;
    $scope.attachToGrid = null;
    $scope.view = "view_empty";

    $scope.vocabulary = {};
    $scope.vocabulary.definition = null;

    $scope.template = {};
    $scope.template.definition = null;
    
    $scope.showVocabulary = "active";
    $scope.showTemplate = "active";

    $scope.getCurrentView = function() {
        var view = null;
        if ($scope.tabContainer) {
            var tab = $scope.tabContainer.selectedChildWidget;
            if (tab.title === "Vocabularies") {
                view = "vocabulary";
            } else if (tab.title === "Templates") {
                view = "template";
            }
        }
        return view;
    };

    $scope.createVocabulary = function() {
        require(["dojox/uuid/generateRandomUuid"], function(generateRandomUuid) {
            console.log("======== PageController: createVocabulary ========");
            var data = {uuid: generateRandomUuid(), version: generateRandomUuid(), name: "", description: "", domain: "", copyright: "", contributors: {contributor: []}, terms: {term: []}};
            data.term = data.terms.term;
            $scope.vocabulary = {};
            $scope.vocabulary.definition = data;
            $scope.vocabulary.ContributorDefinition = [{type: "text", isDefinition: true, name: "contributor", alias: "", list: true}];
            $scope.view = "view_vocabulary";
            $scope.$apply();
        });
    };

    $scope.loadVocabulary = function(item, data) {
        console.log("======== PageController: loadVocabulary ========");
        //TODO: remember to remove data.term (i.e. $scope.vocabulary.definition.term) before saving
        data.terms = sortOnUiDisplayOrder(data.terms);
        data.term = data.terms.term;
        addUniqueId(data);
        $scope.vocabulary = item;
        $scope.vocabulary.definition = data;
        $scope.vocabulary.definition.id = item.id;
        $scope.vocabulary.ContributorDefinition = [{type: "text", isDefinition: true, name: "contributor", alias: "", list: true}];
        $scope.$apply();

        //var scope = getAngularElementScope("idView");
        //scope.load(item);

        console.log("======== PageController: loadVocabulary: Vocabulary: " + item.name);
        console.dir(data);
    };

    $scope.importVocabulary = function() {
        console.log("======== PageController: importVocabulary ========");
        var dialog = dijit.byId("idDialogVocabulary");
        dialog.show();
    };

    $scope.saveVocabulary = function() {
        require(["dojo/request/xhr"], function(xhr){
            console.log("======== PageController: saveVocabulary ========");
            var data = dojo.clone($scope.vocabulary.definition);
            removeUniqueId(data);
            removeUniqueId(data.terms);
            setUiDisplayOrder(data.terms);
            delete data.term;
            console.dir(data);

            // Prevent save if there are duplicate names at the same level in the vocabulary
            var termNameDuplicates = [];
            $scope.findDuplicateAliases(data.terms, termNameDuplicates);
            if (termNameDuplicates.length) {
                $scope.errors.message = "Duplicate names are not allowed: " + termNameDuplicates.join(', ');
                return;
            }

            // Prevent save if a term name contains a character other than the specified: [a-zA-Z0-9] and an underscore (_)
            var invalidNames = [];
            $scope.findInvalidAliases(data.terms, invalidNames);
            if (invalidNames.length) {
                $scope.errors.message = "Some term names have invalid characters: " + invalidNames.join(', ');
                return;
            }

            // Prevent save if any term has invalid validation properties
            var termsWithInvalidValidation = [];
            $scope.validateTermValidation(data.terms, termsWithInvalidValidation);
            if (termsWithInvalidValidation.length) {
                $scope.errors.message = "Some terms have invalid validation properties: " + termsWithInvalidValidation.join(', ');
                return;
            }

            // Prevent save if invalid terms exist
            var invalidTerms = [];
            $scope.findInvalidTerms(data.terms, invalidTerms);
            if (invalidTerms.length) {
                $scope.errors.message = "Some terms are invalid: " + invalidTerms.join(', ');
                return;
            }

            var query = {showAllStatus: true};
            xhr(cris.baseUrl + "vocabularys/save", {
                method : "POST",
                headers: {Accept: "application/json"},
                handleAs : "json",
                query: query,
                data: {vocabulary: angular.toJson(data)},
                timeout: null,
                sync: false,
                preventCache: false
            }).then(function(data) {
                console.dir(data);
                if (data.hasError) {
                    $scope.errors.message = data.message;
                } else {
                    $scope.errors.message = parseDataMessage(data, "Saved Successfully");
                }

                dojo.mixin($scope.vocabulary, data);
                $scope.vocabulary.definition.version = data.versionNumber.$uuid;
                $scope.vocabulary.definition.id = data.id;
                $scope.headVocabularyVersion = data.versionNumber.$uuid;

            	$scope.vocabularyGrid.setQuery(query);
            	$scope.referenceGrid.setQuery(query);
                $scope.$apply();
            }, function(error) {
                console.dir(error);
                $scope.errors.message = parseErrorMessage(error);
                $scope.$apply();
            });
        });
    };

    $scope.deprecateVocabulary = function() {
        require(["dojo/request/xhr"], function(xhr){
            console.log("======== PageController: deprecateVocabulary ========");
            var data = dojo.clone($scope.template.definition);
            removeUniqueId(data);
            setUiDisplayOrder(data);
            var data = moveTermToAttachTo(data);
            console.dir(data);

            var query = {showAllStatus: true};
            xhr(cris.baseUrl + "vocabularys/status/" + $scope.vocabulary.id, {
                method : "PUT",
                headers: {Accept: "application/json"},
                handleAs : "json",
                query: query,
                data: {template: angular.toJson(data)},
                timeout: null,
                sync: false,
                preventCache: false
            }).then(function(data) {
                console.dir(data);
                if (data.hasError) {
                    $scope.errors.message = data.message;
                } else {
                    $scope.errors.message = parseDataMessage(data, "Status changed Successfully");
                }
            	$scope.vocabularyGrid.setQuery(query);
            	$scope.referenceGrid.setQuery(query);
                $scope.$apply();
            }, function(error) {
                console.dir(error);
                $scope.errors.message = parseErrorMessage(error);
                $scope.$apply();
            });
        });
    };

    $scope.createTemplate = function() {
        require(["dojox/uuid/generateRandomUuid"], function(generateRandomUuid) {
            console.log("======== PageController: createTemplate ========");
            $scope.template = {};
            $scope.template.definition = {uuid: generateRandomUuid(), version: generateRandomUuid(), name: "", description: "", term: []};
            $scope.view = "view_template";
            $scope.$apply();
        });
    };

    $scope.loadTemplate = function(item, data) {
        console.log("======== PageController: loadTemplate ========");
        console.dir(item);

        $scope.template = item;
        var template = $scope.deReferenceAttachTo(data);
        $scope.template.definition = sortOnUiDisplayOrder(moveAttachToToTerm(template));
        $scope.template.definition.id = item.id;
        addUniqueId($scope.template.definition);

        // Set isInEditMode flag if template is being edited
        $scope.template.isInEditMode = false;
        $scope.$watch('[template.selectedTerm, template.definition.term, template.id]', function(newValue, oldValue){
            var selectedTermCurrent = newValue[0];
            var selectedTermPrevious = oldValue[0];
            
            if (selectedTermCurrent && selectedTermPrevious && selectedTermCurrent.$$uuid === selectedTermPrevious.$$uuid && selectedTermCurrent.version === selectedTermPrevious.version) {
                var newVal = angular.copy(selectedTermCurrent);
                var oldVal = angular.copy(selectedTermPrevious);
                if (newVal.type === 'composite') {
                    delete newVal.validation;
                    delete oldVal.validation;
                }
                removeUniqueId(newVal);
                removeUniqueId(oldVal);

                // Standardize "" and false values as null before jsonifying
                newVal = JSON.stringify(newVal, function(key, value){
                    if ((typeof value === 'string' && value.trim() === "") || value === false) {
                        return null;
                    } else {
                        return value;
                    }
                });
                oldVal = JSON.stringify(oldVal, function(key, value){
                    if ((typeof value === 'string' && value.trim() === "") || value === false) {
                        return null;
                    } else {
                        return value;
                    }
                });

                if (!angular.equals(newVal, oldVal)) {
                    $scope.template.isInEditMode = true;
                }
            }

            // Check if there is a change in number of terms for the template
            if (newValue[2] === oldValue[2] && newValue[1] && oldValue[1] && $scope.template.isInEditMode === false) {
                var currentTermCount = 0;
                var currentTerms = angular.copy(newValue[1]);
                while (currentTerms.length) {
                    var term_ = currentTerms.pop();
                    if (term_.term && term_.term instanceof Array) {
                        dojo.forEach(term_.term, function(t){
                           currentTerms.push(t);
                        });
                    }
                    currentTermCount++;
                }

                var previousTermCount = 0;
                var previousTerms = angular.copy(oldValue[1]);
                while (previousTerms.length) {
                    var term_ = previousTerms.pop();
                    if (term_.term && term_.term instanceof Array) {
                        dojo.forEach(term_.term, function(t){
                           previousTerms.push(t);
                        });
                    }
                    previousTermCount++;
                }
                if (currentTermCount !== previousTermCount) {
                    $scope.template.isInEditMode = true;
                }
            }
        }, true);

        $scope.$apply();

        console.log("======== PageController: loadTemplate: Template: " + item.name);
    };

    $scope.importTemplate = function() {
        console.log("======== PageController: importTemplate ========");
        var dialog = dijit.byId("idDialogTemplate");
        dialog.show();
    };

    $scope.saveTemplate = function() {
        require(["dojo/request/xhr"], function(xhr){
            console.log("======== PageController: saveTemplate ========");
            var data = dojo.clone($scope.template.definition);
            removeUniqueId(data);
            setUiDisplayOrder(data);

            // Prevent save if there are duplicate aliases at the same level in the template
            var termAliasDuplicates = [];
            $scope.findDuplicateAliases(data, termAliasDuplicates);
            if (termAliasDuplicates.length) {
                $scope.errors.message = "Duplicate aliases are not allowed: " + termAliasDuplicates.join(', ');
                return;
            }

            // Prevent save if an alias contains a character other than the specified: [a-zA-Z0-9] and an underscore (_)
            var invalidAliases = [];
            $scope.findInvalidAliases(data, invalidAliases);
            if (invalidAliases.length) {
                $scope.errors.message = "Some aliases have invalid characters: " + invalidAliases.join(', ');
                return;
            }

            // Prevent save if any term has invalid validation properties
            var termsWithInvalidValidation = [];
            $scope.validateTermValidation(data, termsWithInvalidValidation);
            if (termsWithInvalidValidation.length) {
                $scope.errors.message = "Some terms have invalid validation properties: " + termsWithInvalidValidation.join(', ');
                return;
            }

            // Prevent save if invalid terms exist
            var invalidTerms = [];
            $scope.findInvalidTerms(data, invalidTerms);
            if (invalidTerms.length) {
                $scope.errors.message = "Some terms are invalid: " + invalidTerms.join(', ');
                return;
            }

            var data = moveTermToAttachTo(data);
            console.dir(data);

            var query = {showAllStatus: true};
            xhr(cris.baseUrl + "templates/save", {
                method : "POST",
                headers: {Accept: "application/json"},
                handleAs : "json",
                query: query,
                data: {template: angular.toJson(data)},
                timeout: null,
                sync: false,
                preventCache: false
            }).then(function(data) {
                console.dir(data);
                if (data.hasError) {
                    $scope.errors.message = data.message;
                } else {
                    $scope.errors.message = parseDataMessage(data, "Saved Successfully");
                    $scope.template.isInEditMode = false;
                }
                if (data.template) {
                    var templateDetails = angular.fromJson(data.template);
                    dojo.mixin($scope.template, templateDetails);
                    $scope.template.definition.version = templateDetails["versionNumber"]["$uuid"];
                    $scope.template.definition.id = templateDetails.id;
                    $scope.headTemplateVersion = templateDetails["versionNumber"]["$uuid"];
                }

            	$scope.templateGrid.setQuery(query);
            	$scope.attachToGrid.setQuery(query);
                $scope.$apply();
            }, function(error) {
                console.dir(error);
                $scope.errors.message = parseErrorMessage(error);
                $scope.$apply();
            });
        });
    };

    $scope.findInvalidAliases = function (term, invalidAliases) { // Find invalid characters in the aliases or names of a template or vocabulary's terms
        // A term alias or name must contain only the specified characters: [a-zA-Z0-9], underscore
        var compositeTermName = arguments[2];
        var regex = new RegExp("^(?!_)[a-zA-Z0-9_]+$");
        dojo.forEach(term.term, function(term) {
            var _alias = term.alias || term.useAlias || term.name;
            var isValid = regex.test(_alias);

            var aliasPath = compositeTermName ? compositeTermName + '.' + _alias : _alias;
            if (!isValid) {
                invalidAliases.push(aliasPath);
            }
            if (term.term && term.term instanceof Array && term.term.length) {
                $scope.findInvalidAliases (term, invalidAliases, aliasPath);
            }
        });
    };

    $scope.findDuplicateAliases = function (term, aliasDuplicates) { // Find duplicate aliases/names in a template/vocabulary
        var compositeTermName = arguments[2];
        var aliases = arguments[3] ? arguments[3] : [];
        dojo.forEach(term.term, function(term) {
            var _alias = term.alias || term.useAlias || term.name;
            var path = compositeTermName ? compositeTermName + '.' + _alias : _alias;
            if (path) {
                if (aliases.indexOf(path) > -1) {
                    aliasDuplicates.push(path);
                } else {
                    aliases.push(path);
                }
            }

            if (term.term && term.term instanceof Array && term.term.length) {
                $scope.findDuplicateAliases (term, aliasDuplicates, path, aliases);
            }
        });
    };

    $scope.validateTermValidation = function (term, termsWithInvalidValidation) { // validate validatio propterties for terms
        var compositeTermName = arguments[2];
        dojo.forEach(term.term, function(term) {
            var _alias = term.alias || term.name;
            var path = compositeTermName ? compositeTermName + '.' + _alias : _alias;

            if (term.validation && term.validation.validator[0]) {
                var validator = term.validation.validator[0].property;
                var validatorType = term.validation.validator[0].type;

                if (validatorType === "numeric") {
                    var range;
                    dojo.forEach(validator, function(item) {
                        if (item.name === 'range') {
                            range = item.value;
                        }
                    });
                    var rangeRegex = new RegExp('^((\\[|\\()\\-?\\+?(([0-9]+\\.)?[0-9]+|infinity),\\s*\\-?\\+?(([0-9]+\\.)?[0-9]+|infinity)((\\]|\\))(\\,(?!$))?))*$'); // Expected format: [NUM,NUM] or (NUM,NUM) — Multiple comma separated ranges allowed. NUM is any numeric or 'infinity'
                    var rangeIsValid = rangeRegex.test(range);

                    if (!rangeIsValid) {
                        termsWithInvalidValidation.push(path)
                    }
                } else if (validatorType === 'text') {
                    var maxLength;
                    var uiVerticalLines;
                    var type;
                    dojo.forEach(validator, function(item) {
                        if (item.name === 'type') {
                            type = item.value;
                        } else if (item.name === 'length') {
                            maxLength = item.value;
                        } else if (item.name === 'ui-vertcal-lines') {
                            uiVerticalLines = item.value;
                        }
                    });
                    var regex = new RegExp("^(\\s*|[0-9]*)$"); // Only numbers
                    var isValid = maxLength ? regex.test(maxLength) : true && uiVerticalLines ? regex.test(uiVerticalLines) : true;
                    if (!isValid || !type) {
                        termsWithInvalidValidation.push(path);
                    }
                } else if (validatorType === 'date-time') {
                    dojo.forEach(validator, function(item) {
                        if (item.name === 'format') {
                            if (!item.value) {
                                termsWithInvalidValidation.push(path);
                            }
                        }
                    });
                } else if (!validatorType) { // Validation type required
                    termsWithInvalidValidation.push(path);
                }
            }

            if (term.term && term.term instanceof Array && term.term.length) {
                $scope.validateTermValidation (term, termsWithInvalidValidation, path);
            }
        });
    }

    $scope.findInvalidTerms = function(term, invalidTerms) {
        var compositeTermName = arguments[2];
        dojo.forEach(term.term, function(term) {
            var _alias = term.alias || term.useAlias || term.name;
            var path = compositeTermName ? compositeTermName + '.' + _alias : _alias;
            if (term.term && term.term instanceof Array && term.term.length) {
                $scope.findInvalidTerms (term, invalidTerms, path);
            } else {
                if (typeof term.isTermValid !== 'undefined' && !term.isTermValid) {
                    invalidTerms.push(path);
                }
            }
        });
    };

    $scope.deprecateTemplate = function() {
        require(["dojo/request/xhr"], function(xhr){
            console.log("======== PageController: deprecateTemplate ========");
            var data = dojo.clone($scope.template.definition);
            removeUniqueId(data);
            setUiDisplayOrder(data);
            var data = moveTermToAttachTo(data);
            console.dir(data);

            var query = {showAllStatus: true};
            xhr(cris.baseUrl + "templates/status/" + $scope.template.id, {
                method : "PUT",
                headers: {Accept: "application/json"},
                handleAs : "json",
                query: query,
                data: {template: angular.toJson(data)},
                timeout: null,
                sync: false,
                preventCache: false
            }).then(function(data) {
                console.dir(data);
                if (data.hasError) {
                    $scope.errors.message = data.message;
                } else {
                    $scope.errors.message = parseDataMessage(data, "Status changed Successfully");
                }
            	$scope.templateGrid.setQuery(query);
            	$scope.attachToGrid.setQuery(query);
                $scope.$apply();
            }, function(error) {
                console.dir(error);
                $scope.errors.message = parseErrorMessage(error);
                $scope.$apply();
            });
        });
    };

    $scope.save = function() {
        if ($scope.getCurrentView() === "vocabulary") {
            $scope.saveVocabulary();
        } else if ($scope.getCurrentView() === "template") {
            $scope.saveTemplate();
        }
    };

    $scope.makeCurrentVersion = function() {
        var _url = "";
        if ($scope.getCurrentView() === "template") {
            _url = cris.baseUrl + 'templates/head/' + $scope.template.id;
        } else if ($scope.getCurrentView() === "vocabulary") {
            _url = cris.baseUrl + 'vocabularys/head/' + $scope.vocabulary.id;
        }

        dojo.xhrPut({
            url: _url,
            load: function(data){
                if (data.hasError) {
                    $scope.errors.message = data.message;
                } else {
                    $scope.errors.message = parseDataMessage(data, "Saved Successfully");
                }

                var query = {showAllStatus: true};
                if ($scope.getCurrentView() === "template") {
                    $scope.headTemplateVersion = $scope.template['versionNumber']['$uuid']; // Triggers hide of "Make Current Version" button
                    $scope.templateGrid.setQuery(query);
                    $scope.attachToGrid.setQuery(query);
                } else if ($scope.getCurrentView() === "vocabulary") {
                    $scope.headVocabularyVersion = $scope.vocabulary['versionNumber']['$uuid']; // Triggers hide of "Make Current Version" button
                    $scope.vocabularyGrid.setQuery(query);
                    $scope.attachToGrid.setQuery(query);
                }
                $scope.$apply();
            },
            error: function(error){
                $scope.errors.message = "Unable to save changes";
                $scope.$apply();
            }
        });
    }

    $scope.$watch("errors.message", function(value) {
        if ($scope.timer) {
            clearTimeout($scope.timer);
        }

        $scope.timer = setTimeout(function() {
            $scope.errors.message = "";
            $scope.$apply();
        }, 4000);
    });

    //TODO: may move the dereference to the backend
    $scope.deReferenceAttachTo = function(template) {
        var terms = template.term;
        for (var index in terms) {
            $scope.deReferenceAttachTo(terms[index]);
        }

        var attachTos = template.attachTo;
        for (var index in attachTos) {
            var attachTo = template.attachTo[index];
            attachTo.type = "attachTo";
            attachTo.fieldList = [];
            // fetch the attachTo template and populate fieldList
            var uuid = attachTo.uuid;
            var version = attachTo.latestVersion ? attachTo.latestVersion : attachTo.version;
            require(["dojo/request/xhr"], function(xhr){
                console.log("======== deReferenceAttachTo: fetch the attachto template ========");
                xhr(cris.baseUrl + "templates/load/" + uuid + "/" + version, {
                    method : "GET",
                    headers: {Accept: "application/json"},
                    handleAs : "json",
                    sync: true
                }).then(function(data) {
                    console.dir(data);
                    console.log("index = " + index);
                    if (data.hasError) {
                        $scope.errors.message = data.message;
                    } else {
                        $scope.errors.message = parseDataMessage(data);

                        for (var idx in data.term) {
                            var alias = data.term[idx].alias || data.term[idx].name;
                            if (alias) {
                                attachTo.fieldList.push({id: alias, name: alias, validation: data.term[idx].validation});
                            }
                        }
                    }
                    $scope.$apply();
                }, function(error) {
                    console.dir(error);
                    $scope.errors.message = parseErrorMessage(error);
                    $scope.$apply();
                });
            });
        }

        return template;
    };

    $scope.$watch("showVocabulary", function(newValue, oldValue) {
        console.log("******** show vocabulary");
        console.log("new: " + newValue + ", old: " + oldValue);

        if (newValue === oldValue) {
            return;
        }

        var query = {"showAllStatus" : true};
        switch (newValue) {
            case "active":
                var filter = {column: 2, type: "number", condition: "equalTo", value: 1};
            	$scope.vocabularyGrid.setFilter(filter);
            	$scope.referenceGrid.setQuery(query);
                break;
            case "deprecated":
                var filter = {column: 2, type: "number", condition: "equalTo", value: 0};
            	$scope.vocabularyGrid.setFilter(filter);
            	$scope.referenceGrid.setQuery(query);
                break;
            case "both":
            	$scope.vocabularyGrid.setFilter(null);
            	$scope.referenceGrid.setQuery(query);
                break;
            default:
                // do nothing
        }
    });

    $scope.$watch("showTemplate", function(newValue, oldValue) {
        console.log("******** show template");
        console.log("new: " + newValue + ", old: " + oldValue);

        if (newValue === oldValue) {
            return;
        }

        var query = {"showAllStatus" : true};
        switch (newValue) {
            case "active":
                var filter = {column: 2, type: "number", condition: "equalTo", value: 1};
            	$scope.templateGrid.setFilter(filter);
            	$scope.attachToGrid.setQuery(query);
                break;
            case "deprecated":
                var filter = {column: 2, type: "number", condition: "equalTo", value: 0};
            	$scope.templateGrid.setFilter(filter);
            	$scope.attachToGrid.setQuery(query);
                break;
            case "both":
            	$scope.templateGrid.setFilter(null);
            	$scope.attachToGrid.setQuery(query);
                break;
            default:
                // do nothing
        }
    });

    $scope.disableSaveButton = function () {
        if (($scope.getCurrentView() === "vocabulary" && $scope.vocabulary.statusId === 0) || ($scope.getCurrentView() === "template" && $scope.template.statusId === 0)) {
            return true;
        } else {
            return false;
        }
    };

    $scope.previewTemplate = function () {
        if ($scope.template.definition) {
            
            var scope = $scope.$new(true);
            scope.term = getTerm($scope.template.definition.uuid, $scope.template.definition.version, true);
            scope.data = instantiateTerm(scope.term, {});
            scope.context = scope.data;
            
            var modalTemplate = '<div class="modal-header"> \
                                    <b>Template</b>:&nbsp{{term.name}} \
                                </div> \
                                <div class="modal-body" style="max-height:80vh;overflow-y:auto;"> \
                                    <cris-dataset term="term" dataset="data" context="context" message="message" readOnly="false"><!----></cris-dataset>  \
                                </div> \
                                <div class="modal-footer"> \
                                    <span class="pull-right"><input type="button" value="Close" class="btn btn-warning" ng-click="close()" /></span> \
                                </div>';
            
            $uibModal.open({
                animation: true,
                template: modalTemplate,
                scope: scope,
                windowClass : 'largeModal',
                controller: function ($scope, $uibModalInstance) {
                     $scope.close = function () {
                         $uibModalInstance.close();
                     };
                }
            });
        }
    };

    $scope.disableVersionButton = function () {
        if ($scope.getCurrentView() === "vocabulary") {
            if (!$scope.headVocabularyVersion || ($scope.vocabulary.definition && !$scope.vocabulary.definition.id) || ($scope.vocabulary.versionNumber && $scope.vocabulary.versionNumber['$uuid'] === $scope.headVocabularyVersion)) {
                return true;
            }
        } else if ($scope.getCurrentView() === "template") {
            if (!$scope.headTemplateVersion || ($scope.template.definition && !$scope.template.definition.id) || ($scope.template.versionNumber && $scope.template.versionNumber['$uuid'] === $scope.headTemplateVersion)) {
                return true;
            }
        }
        return false;
    };

    $scope.updateTermToLatestVersion = function(isTemplateTerm) {
        console.log('========== Update Term to Latest Version ====================================');

        require(["dojo/request/xhr", "dijit/Dialog", "dijit/form/Button"], function(xhr, Dialog, Button) {
            var selectedTerm = isTemplateTerm ? $scope.template.selectedTerm : $scope.vocabulary.selectedTerm;
            var url = cris.baseUrl +  "terms/fetchDetails/" + selectedTerm.uuid + "/" + selectedTerm.latestVersion;
            xhr(url, {
                handleAs: "json"
            }).then(function(data){
                // Ask user what properties they do not want to update
                var updatableTermProperties = [ {name: 'description', prettyName: 'Description'},
                                                {name: 'value', prettyName: 'Default Value'},
                                                {name: 'required', prettyName: 'Required'},
                                                {name: 'readOnly', prettyName: 'Read Only'},
                                                {name: 'list', prettyName: 'List'},
                                                {name: 'validation', prettyName: 'Validation'}];
                                            
                if (selectedTerm.type === 'attachTo') {
                    updatableTermProperties = [ {name: 'description', prettyName: 'Description'}];
                }

                var htmlStr = "";
                dojo.forEach(updatableTermProperties, function(property) {
                   htmlStr += '<div><input data-dojo-type="dijit/form/CheckBox" value="' + property.name + '">&nbsp;' + property.prettyName + '</input></div><br />'
                });

                var propertiesToExclude = [];
                var dialog = new Dialog({
                    title: "Properties To Exclude From Update",
                    content: htmlStr,
                    style: "width: 400px",
                    onContinue: function () {
                        dojo.query('input:checked', this.domNode).forEach(function (node) {
                            switch (node.value) {
                                case 'readOnly':
                                    propertiesToExclude.push('readOnly');
                                    propertiesToExclude.push('readOnlyExpression');
                                    break;
                                case 'required':
                                    propertiesToExclude.push('required');
                                    propertiesToExclude.push('requiredExpression');
                                    break;
                                case 'list':
                                    propertiesToExclude.push('list');
                                    propertiesToExclude.push('grid');
                                    break;
                                default:
                                    propertiesToExclude.push(node.value);
                                    break;
                            }
                        });
                        updateTerm(data, selectedTerm, propertiesToExclude);
                        $scope.$apply();
                        this.destroyRecursive();
                    }
                });
                var continueButton = new Button({
                    label: "Continue",
                    onClick: function () {
                        dialog.onContinue();
                    }
                });
                var cancelButton = new Button({
                    label: "Cancel",
                    onClick: function () {
                        dialog.onCancel()
                    }
                });
                dialog.containerNode.appendChild(continueButton.domNode);
                dialog.containerNode.appendChild(cancelButton.domNode);
                dojo.setStyle(continueButton.domNode, 'float', 'left');
                dojo.setStyle(cancelButton.domNode, 'float', 'right');
                dojo.setStyle(dialog.domNode, 'background-color', '#FFFFFF');
                dialog.show();
            }, function(err){
                $scope.errors.message = "Unable to save changes";
                $scope.$apply();
            });
        });
    };

    $scope.deleteInvalidTerm = function(deleteTermCallback) {
        if (deleteTermCallback) {
            deleteTermCallback();
        }
    }
}]);
