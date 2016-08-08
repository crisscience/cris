cris.require([
    "dojo/store/Memory",
    "dijit/form/TextBox",
    "dijit/form/Button"
]);

function aliasListForValidationDialogTerm() {
    var validation = [];
    var properties = [];
    properties.push(listProperty("item", "zipcode"));
    properties.push(listProperty("item", "phone"));
    properties.push(listProperty("item", "email"));
    properties.push(listProperty("isMultiSelect", false));
    validation.push({type: "list", properties: properties});
    return validation;
}

function attachToDefinition(data) {
    var definition = {};
    definition["nestLevel"] = 1;
    definition["isDefinition"] = true;
    definition["uuid"] = data["uuid"];
    definition["version"] = data["version"];
    definition["required"] = data["required"] === null ? false : data["required"];
    definition["list"] = data["list"] === null ? false : data["list"];
    definition["name"] = null;
    definition["description"] = data["description"];
    definition["type"] = "attachTo";
    definition["validation"] = null;
    definition["ui-display-order"] = data["display order"];
    definition["alias"] = data["alias"];
    definition["id-field"] = data["id"];
    definition["name-field"] = data["name"];
    definition["use-alias"] = data["alias"];
    definition["versionName"] = null;
    definition["read-only"] = false;
    definition["scale"] = null;
    definition["length"] = null;
    definition["unit"] = null;
    definition["value"] = data["default value"];
    definition["properties"] = {};
    definition["term"] = [];
    return definition;
}

function attachToTermDialogTerm(url) {
    var term = [];
    var scopeData = {};
    var order = 1;
    dojo.xhrGet({
        url: url,
        sync: true,
        load: function(data) {
            var templateData = JSON.parse(data);
            var templateValidation = vocabListForDialogTerm(templateData);
            var templateProperties = listProperties(templateValidation[0]["properties"]);

            term.push(typeOfDialogTerm(order, "Database Record", true, false));
            scopeData["type of term"] = "Database Record";
            order++;
            term.push(createDialogTerm("target template", "list", templateValidation, templateProperties, order, null, false, 1, true, false, null));
            scopeData["target template"] = null;
            order++;
            term.push(createDialogTerm("id", "list", [], [], order, null, false, 1, true, false, null));
            scopeData["id"] = null;
            order++;
            term.push(createDialogTerm("name", "list", [], [], order, null, false, 1, true, false, null));
            scopeData["name"] = null;
            order++;
            term.push(createDialogTerm("alias", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["alias"] = null;
            order++;
            term.push(createDialogTerm("description", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["description"] = null;
            order++;
            term.push(createDialogTerm("default value", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["default value"] = null;
            order++;
            term.push(createDialogTerm("required", "boolean", [], [], order, null, false, 1, true, false, null));
            scopeData["required"] = null;
            order++;
            term.push(createDialogTerm("list", "boolean", [], [], order, null, false, 1, true, false, null));
            scopeData["list"] = null;
            order++;
            term.push(createDialogTerm("display order", "text", [], [], order, null, false, 1, true, false, null));
            var mainScope = getAngularElementScope("idTemplateController");
            var length = mainScope.term.length + 1;
            scopeData["display order"] = length;

            var scope = getAngularElementScope("idTemplateDialogController");
            scope.term = term;
            scope.data = scopeData;
            scope.$apply();
        }
    });
}

function copyNestedTermDialogTerm(nestedTerm, vocabTermDetail, vocabDetail, isTemplate, count, isEdit) {

    var alias = nestedTerm.alias;
    var name = vocabTermDetail.name;
    var description = nestedTerm.description === null ? vocabTermDetail.description : nestedTerm.description;
    var value = nestedTerm.value === null ? vocabTermDetail.value : nestedTerm.value;
    var required = nestedTerm.required === null ? vocabTermDetail.required : nestedTerm.required;
    var list = nestedTerm.list === null ? vocabTermDetail.list : nestedTerm.list;
    var unit = vocabTermDetail.unit;
    var uuid = nestedTerm.uuid;
    var version = nestedTerm.version === null ? vocabTermDetail.version : nestedTerm.version;
    var type = null, properties = null, isEditProperties = false;
    if (nestedTerm.validation !== null) {
        if (isEdit) {
            properties = nestedTerm["validation"][0].properties;
            type = nestedTerm["validation"][0].type;
            isEditProperties = true;
        } else {
            properties = nestedTerm.validation.validator[0].property;
            type = getType(nestedTerm.validation.validator[0].type, properties);
        }
    } else if (vocabTermDetail.validation !== null) {
        properties = vocabTermDetail.validation.validator[0].property;
        type = getType(vocabTermDetail.validation.validator[0].type, properties);
    } else {
        properties = [];
        type = "not defined";
    }

    var id = isTemplate ? "idTemplateDialogController" + count : "idVocabDialogController" + count;
    createRemoveButton(count, isTemplate ? "idTemplateTermForm" : "idVocabTermForm", isTemplate);
    var htmlTemplate = "<div id=" + "'" + id + "'" + " data-ng-controller='TermDialogController' class='ng-scope'>";
    htmlTemplate += "<collection term='term' dataset='data' message='message' read-only='false' actions='actions'></collection>";
    htmlTemplate += "</div>";
    dojo.place(htmlTemplate, isTemplate ? "idTemplateTermForm" : "idVocabTermForm");
    angular.bootstrap(document.getElementById(id), ['app']);
    var scope = getAngularElementScope(id);
//    scope.loadNestedVocabTerm(isTemplate);

    var scopeData = {};
    var term = [];

    term.push(createDialogTerm("vocabulary", "text", [], [], 1, null, true, 1, true, false, null));
    scopeData["vocabulary"] = vocabDetail.name;
    term.push(createDialogTerm("vocabulary term", "text", [], [], 2, null, true, 2, true, false, null));
    scopeData["vocabulary term"] = vocabTermDetail.name;
    term.push(createDialogTerm("alias", "text", [], [], 3, null, false, 1, true, false, null));
    scopeData["alias"] = alias;
    scopeData["name"] = name;
    term.push(createDialogTerm("description", "text", [], [], 4, null, false, 1, true, false, null));
    scopeData["description"] = description;
    term.push(createDialogTerm("validation type", "text", [], [], 5, null, true, 1, true, false, null));
    scopeData["validation type"] = type;
    term.push(createDialogTerm("default value", "text", [], [], 6, null, false, 1, true, false, null));
    scopeData["default value"] = value;
    term.push(createDialogTerm("required", "boolean", [], [], 7, null, false, 1, true, false, null));
    scopeData["required"] = required;
    term.push(createDialogTerm("list", "boolean", [], [], 8, null, false, 1, true, false, null));
    scopeData["list"] = list;
    term.push(createDialogTerm("unit", "text", [], [], 9, null, true, 1, true, false, null));
    scopeData["unit"] = unit;
    scopeData["uuid"] = uuid;
    scopeData["version"] = version;
    scopeData["terms"] = [];

    if (type !== "boolean" && type !== "not defined") {
        var returnValue = propertiesDialogTerm(type, term, 2, 6);
        term = returnValue[0];
        scopeData["properties"] = isEditProperties ? propertiesEditDataDialogTerm(type, properties) : propertiesDataDialogTerm(type, properties);
    }

    scope.term = term;
    scope.data = scopeData;
//    scope.isEdit = true;
    scope.$apply();
}

function copyTermDialogTerm(url, isNested, scope, isTemplate) {
    var term = [];
    var scopeData = {};
    var order = 1;
    dojo.xhrGet({
        url: url,
        sync: true,
        load: function(data) {
            var vocabData = JSON.parse(data);
            var vocabValidation = vocabListForDialogTerm(vocabData);
            var vocabProperties = listProperties(vocabValidation[0]["properties"]);

            if (!isNested) {
                term.push(typeOfDialogTerm(order, "Existing Term", isTemplate, false));
                scopeData["type of term"] = "Existing Term";
                order++;
            }
            term.push(createDialogTerm("vocabulary", "list", vocabValidation, vocabProperties, order, null, false, 1, true, false, null));
            scopeData["vocabulary"] = null;
            order++;
            term.push(createDialogTerm("vocabulary term", "list", [], [], order, null, false, 2, true, false, null));
            scopeData["vocabulary term"] = null;
            order++;
            term.push(createDialogTerm(isNested || isTemplate ? "alias" : "name", "text", [], [], order, null, false, 1, true, false, null));
            if (isNested || isTemplate) {
                scopeData["alias"] = null;
            } else {
                scopeData["name"] = null;
            }
            order++;
            term.push(createDialogTerm("description", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["description"] = null;
            order++;
            term.push(createDialogTerm("validation type", "text", [], [], order, null, true, 1, true, false, null));
            scopeData["validation type"] = null;
            order++;
            term.push(createDialogTerm("default value", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["default value"] = null;
            order++;
            term.push(createDialogTerm("required", "boolean", [], [], order, null, false, 1, true, false, null));
            scopeData["required"] = null;
            order++;
            term.push(createDialogTerm("list", "boolean", [], [], order, null, false, 1, true, false, null));
            scopeData["list"] = null;
            order++;
            term.push(createDialogTerm("unit", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["unit"] = null;
            if (isTemplate) {
                order++;
                term.push(createDialogTerm("display order", "text", [], [], order, null, false, 1, true, false, null));
                var mainScope = getAngularElementScope("idTemplateController");
                var length = mainScope.term.length + 1;
                scopeData["display order"] = length;
            }

            scopeData["terms"] = [];
            scopeData["properties"] = null;

            scope.term = term;
            scope.data = scopeData;
            if (isNested) {
                scope.$apply();
            }
        }
    });
}

function createDialogTerm(name, type, validation, properties, order, value, readOnly, nestLevel, isDefinition, list, terms) {
    var definition = {};
    definition["nestLevel"] = nestLevel;
    definition["isDefinition"] = isDefinition;
    definition["uuid"] = null;
    definition["version"] = null;
    definition["versionName"] = null;
    definition["alias"] = name;
    definition["required"] = false;
    definition["read-only"] = readOnly;
    definition["list"] = list;
    definition["id-field"] = null;
    definition["name-field"] = null;
    definition["use-alias"] = null;
    definition["name"] = name;
    definition["description"] = name;
    definition["type"] = type;
    definition["unit"] = null;
    definition["scale"] = null;
    definition["length"] = null;
    definition["validation"] = validation;
    definition["value"] = value;
    definition["ui-display-order"] = order;
    definition["properties"] = properties;
    if (terms !== null) {
        definition["terms"] = terms;
    }
    return definition;
}

function createRemoveButton(count, widgetNode, isTemplate) {
    var myButton = new dijit.form.Button({id: "button_" + count, label: "Remove",
        onClick: function() {
            var result = this.id.split("_");
            removeNestedTerm(result[1], isTemplate);
        }
    });
    dojo.byId(widgetNode).appendChild(myButton.domNode);
}

function dateTimeTypeListForValidationDialogTerm() {
    var validation = [];
    var properties = [];
    properties.push(listProperty("item", "date"));
    properties.push(listProperty("item", "time"));
    properties.push(listProperty("item", "dateTime"));
    properties.push(listProperty("item", "gDay"));
    properties.push(listProperty("item", "gMonth"));
    properties.push(listProperty("item", "gMonthDay"));
    properties.push(listProperty("item", "gYear"));
    properties.push(listProperty("item", "gYearMonth"));
    properties.push(listProperty("isMultiSelect", false));
    validation.push({type: "list", properties: properties});
    return validation;
}

function deleteTerm(term, isTemplate) {
    var id = isTemplate ? "idTemplateController" : "idVocabController";
    var scope = getAngularElementScope(id);
    var currentTerms = scope.term;
    var index = null;
    var isTerm = false;
    for (var i = 0; i < currentTerms.length; i++) {
        if (isTerm) {
            var dOrder = parseInt(currentTerms[i]["ui-display-order"]);
            dOrder = dOrder - 1;
            currentTerms[i]["ui-display-order"] = dOrder;
        }
        if (currentTerms[i].alias === term.alias) {
            index = i;
            if (!isTemplate) {
                break;
            } else {
                isTerm = true;
            }
        }
    }
    currentTerms.splice(index, 1);
    scope.term = isTemplate ? sortTermsByOrder(currentTerms) : sortTermsByName(currentTerms);
    scope.$apply();
}

function editAttachToTermDialogTerm(url, editTerm) {
    var term = [];
    var scopeData = {};
    var order = 1;
    dojo.xhrGet({
        url: url + editTerm.uuid + "/" + editTerm.version,
        sync: true,
        load: function(data) {
            var templateData = JSON.parse(data);

            term.push(typeOfDialogTerm(order, "Database Record", true, true));
            scopeData["type of term"] = "Database Record";
            order++;
            term.push(createDialogTerm("target template", "text", [], [], order, null, true, 1, true, false, null));
            scopeData["target template"] = templateData.name;
            scopeData["uuid"] = editTerm["uuid"];
            scopeData["version"] = editTerm["version"];
            order++;
            term.push(createDialogTerm("id", "text", [], [], order, null, true, 1, true, false, null));
            scopeData["id"] = editTerm["id-field"];
            order++;
            term.push(createDialogTerm("name", "text", [], [], order, null, true, 1, true, false, null));
            scopeData["name"] = editTerm["name-field"];
            order++;
            term.push(createDialogTerm("alias", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["alias"] = editTerm["alias"];
            order++;
            term.push(createDialogTerm("description", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["description"] = editTerm["description"];
            order++;
            term.push(createDialogTerm("default value", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["default value"] = editTerm["value"];
            order++;
            term.push(createDialogTerm("required", "boolean", [], [], order, null, false, 1, true, false, null));
            scopeData["required"] = editTerm["required"];
            order++;
            term.push(createDialogTerm("list", "boolean", [], [], order, null, false, 1, true, false, null));
            scopeData["list"] = editTerm["list"];
            order++;
            term.push(createDialogTerm("display order", "text", [], [], order, null, false, 1, true, false, null));
            scopeData["display order"] = editTerm["ui-display-order"];

            var scope = getAngularElementScope("idTemplateDialogController");
            scope.term = term;
            scope.data = scopeData;
            scope.$apply();
        }
    });
}

function editCopyTermDialogTerm(url, scope, isTemplate, isNested, editTerm) {
    var term = [];
    var scopeData = {};
    var order = 1;
    var message = null;
    dojo.xhrGet({
        url: url + editTerm["uuid"] + "/" + editTerm["version"],
        sync: true,
        load: function(data) {
            var detailedData = JSON.parse(data);
            if (detailedData.hasError) {
                message = detailedData.message;
            } else {
                var vocab = JSON.parse(detailedData.vocabulary);
//                var vocabularies = JSON.parse(detailedData.vocabularies);
//                var terms = JSON.parse(detailedData.terms);
                var vocabTerm = JSON.parse(detailedData.term);
//                var vocabValidation = vocabListForDialogTerm(vocabularies);
//                var vocabProperties = listProperties(vocabValidation[0]["properties"]);
//                var vocabTermValidation = vocabTermListForDialogTerm(terms);
//                var vocabTermProperties = listProperties(vocabTermValidation[0]["properties"]);
//                var validation = validationListForDialogTerm();
//                var validationTypeProperties = listProperties(validation[0]["properties"]);
                var properties = null, type = null, isEditValidation = false;
                if (editTerm["validation"] !== null) {
                    isEditValidation = true;
                    properties = editTerm["validation"][0].properties;
                    type = getType(editTerm["type"], properties);
                } else if (vocabTerm["validation"] !== null) {
                    properties = vocabTerm.validation.validator[0].property;
                    type = getType(vocabTerm.validation.validator[0].type, properties);
                } else {
                    type = "not defined";
                }

                term.push(typeOfDialogTerm(order, "Existing Term", isTemplate, true));
                scopeData["type of term"] = "Existing Term";
                order++;
                term.push(createDialogTerm("vocabulary", "text", [], [], order, null, true, 1, true, false, null));
                scopeData["vocabulary"] = vocab.name;
                order++;
                term.push(createDialogTerm("vocabulary term", "text", [], [], order, null, true, 1, true, false, null));
                scopeData["vocabulary term"] = vocabTerm.name;
                order++;
                term.push(createDialogTerm(isTemplate || isNested ? "alias" : "name", "text", [], [], order, null, false, 1, true, false, null));
                if (isTemplate || isNested) {
                    scopeData["alias"] = editTerm["alias"];
                } else {
                    scopeData["name"] = editTerm["name"] === null ? vocabTerm.name : editTerm["name"];
                    scopeData["alias"] = null;
                }
                order++;
                term.push(createDialogTerm("description", "text", [], [], order, null, false, 1, true, false, null));
                scopeData["description"] = editTerm["description"] === null ? vocabTerm["description"] : editTerm["description"];
                order++;
                term.push(createDialogTerm("validation type", "text", [], [], order, null, true, 1, true, false, null));
                scopeData["validation type"] = type;
                order++;
                term.push(createDialogTerm("default value", "text", [], [], order, null, false, 1, true, false, null));
                scopeData["default value"] = editTerm["value"] === null ? vocabTerm["value"] : editTerm["value"];
                order++;
                term.push(createDialogTerm("required", "boolean", [], [], order, null, false, 1, true, false, null));
                scopeData["required"] = editTerm["required"] === null ? vocabTerm["required"] : editTerm["required"];
                order++;
                term.push(createDialogTerm("list", "boolean", [], [], order, null, false, 1, true, false, null));
                scopeData["list"] = editTerm["list"] === null ? vocabTerm["list"] : editTerm["list"];
                order++;
                term.push(createDialogTerm("unit", "text", [], [], order, null, true, 1, true, false, null));
                scopeData["unit"] = editTerm["unit"] === null ? vocabTerm["unit"] : editTerm["unit"];
                if (isTemplate) {
                    order++;
                    term.push(createDialogTerm("display order", "text", [], [], order, null, false, 1, true, false, null));
                    scopeData["display order"] = editTerm["ui-display-order"];
                }
                scopeData["uuid"] = editTerm["uuid"];
                scopeData["version"] = editTerm["version"];
                scopeData["terms"] = [];
                scopeData["properties"] = null;


                if (type !== "boolean" && type !== "not defined") {
                    var returnValue = propertiesDialogTerm(type, term, 2, 7);
                    term = returnValue[0];
                    scopeData["properties"] = isEditValidation ? propertiesEditDataDialogTerm(type, properties) : propertiesDataDialogTerm(type, properties);
                }
                scope.term = term;
                scope.data = scopeData;
                scope.$apply();
            }
        }
    });
    return message;
}

function editNewTermDialogTerm(vocabName, editTerm) {
    var term = [];
    var scopeData = {};

//    var validation = validationListForDialogTerm();
//    var validationTypeProperties = listProperties(validation[0]["properties"]);

    var properties = editTerm["validation"][0].properties;
    var type = getType(editTerm["type"], properties);

    term.push(typeOfDialogTerm(1, "New Term", false, true));
    scopeData["type of term"] = "New Term";

    term.push(createDialogTerm("vocabulary", "text", [], [], 2, vocabName, true, 1, true, false, null));
    scopeData["vocabulary"] = vocabName;

    term.push(createDialogTerm("name", "text", [], [], 3, null, false, 1, true, false, null));
    scopeData["name"] = editTerm["name"];
    scopeData["alias"] = editTerm["alias"];

    term.push(createDialogTerm("description", "text", [], [], 4, null, false, 1, true, false, null));
    scopeData["description"] = editTerm["description"];

    term.push(createDialogTerm("validation type", "text", [], [], 5, null, false, 1, true, false, null));
    scopeData["validation type"] = type;

    term.push(createDialogTerm("default value", "text", [], [], 6, null, false, 1, true, false, null));
    scopeData["default value"] = editTerm["value"];

    term.push(createDialogTerm("required", "boolean", [], [], 7, null, false, 1, true, false, null));
    scopeData["required"] = editTerm["required"];

    term.push(createDialogTerm("list", "boolean", [], [], 8, null, false, 1, true, false, null));
    scopeData["list"] = editTerm["list"];

    term.push(createDialogTerm("unit", "text", [], [], 9, null, false, 1, true, false, null));
    scopeData["unit"] = editTerm["unit"];
    scopeData["uuid"] = editTerm["uuid"];
    scopeData["version"] = editTerm["version"];
    scopeData["terms"] = [];
    scopeData["properties"] = null;

    if (type !== "boolean" && type !== "not defined") {
        var returnValue = propertiesDialogTerm(type, term, 2, 6);
        term = returnValue[0];
        scopeData["properties"] = propertiesEditDataDialogTerm(type, properties);
    }

    var scope = getAngularElementScope("idVocabDialogController");
    scope.term = term;
    scope.data = scopeData;
    scope.$apply();
}

function getTermIndex(term, item) {
    var index = null;
    for (var i = 0; i < term.length; i++) {
        if (term[i].alias === item) {
            index = i;
            break;
        }
    }
    return index;
}

function getType(type, properties) {
    var returnValue = null;
    if (type === "time" || type === "date") {
        returnValue = "date-time";
    } else if (type === "advanced") {
        if (properties.length > 1) {
            returnValue = "pre-defined";
        } else {
            returnValue = type;
        }
    } else {
        returnValue = type;
    }
    return returnValue;
}

function listProperty(key, value) {
    var property = {};
    property[key] = value;
    return property;
}

function listProperties(properties) {
    var termProperties = {};
    var items = [];
    var isMultiSelect = false;
    for (var i = 0; i < properties.length; i++) {
        if (properties[i].isMultiSelect) {
            isMultiSelect = properties[i].isMultiSelect;
        } else if (properties[i].item) {
            items.push({id: properties[i].item, name: properties[i].item});
        }
    }
    termProperties["items"] = items;
    termProperties["isMultiSelect"] = isMultiSelect;
    return termProperties;
}

function newTermDialogTerm(vocabName, scope) {
    var term = [];
    var scopeData = {};

    var validation = validationListForDialogTerm();
    var validationTypeProperties = listProperties(validation[0]["properties"]);

    term.push(typeOfDialogTerm(1, "New Term", false, false));
    scopeData["type of term"] = "New Term";

    term.push(createDialogTerm("vocabulary", "text", [], [], 2, vocabName, true, 1, true, false, null));
    scopeData["vocabulary"] = vocabName;

    term.push(createDialogTerm("name", "text", [], [], 3, null, false, 1, true, false, null));
    scopeData["name"] = null;
    scopeData["alias"] = null;

    term.push(createDialogTerm("description", "text", [], [], 4, null, false, 1, true, false, null));
    scopeData["description"] = null;

    term.push(createDialogTerm("validation type", "list", validation, validationTypeProperties, 5, null, false, 1, true, false, null));
    scopeData["validation type"] = null;

    term.push(createDialogTerm("default value", "text", [], [], 6, null, false, 1, true, false, null));
    scopeData["default value"] = null;

    term.push(createDialogTerm("required", "boolean", [], [], 7, null, false, 1, true, false, null));
    scopeData["required"] = null;

    term.push(createDialogTerm("list", "boolean", [], [], 8, null, false, 1, true, false, null));
    scopeData["list"] = null;

    term.push(createDialogTerm("unit", "text", [], [], 9, null, false, 1, true, false, null));
    scopeData["unit"] = null;
    scopeData["uuid"] = createUUID();
    scopeData["version"] = createUUID();
    scopeData["terms"] = [];
    scopeData["properties"] = null;

    scope.term = term;
    scope.data = scopeData;
}

function populateNestedTerms(urlVocab, terms, isTemplate, isEdit) {
    var count = 0;
    for (var i = 0; i < terms.length; i++) {
        dojo.xhrGet({
            url: urlVocab + terms[i].uuid + "/" + terms[i].version,
            sync: true,
            load: function(data) {
                var detailedData = JSON.parse(data);
                var vocab = detailedData.vocabulary === null ? null : JSON.parse(detailedData.vocabulary);
                var term = detailedData.term === null ? null : JSON.parse(detailedData.term);
                if (term !== null && vocab !== null) {
                    copyNestedTermDialogTerm(terms[i], term, vocab, isTemplate, i, isEdit);
                    count++;
                }

            }
        });
    }
    return count;
}

function predefinedDialogTerm(alias) {
    var pItems = {};
    if (alias === "email") {
        pItems["alias"] = "email";
        pItems["regular expression"] = "[0-9_a-zA-Z]@[a-zA-Z].[a-zA-Z]{3}";
    } else if (alias === "zipcode") {
        pItems["alias"] = "zipcode";
        pItems["regular expression"] = "[0-9]{5}-[0-9]{4}";
    } else if (alias === "phone") {
        pItems["alias"] = "phone";
        pItems["regular expression"] = "[0-9]{3}-[0-9]{3}-[0-9]{3}";
    }
    var scope = getAngularElementScope("idVocabDialogController");
    scope.data["properties"] = pItems;
//    scope.$apply();
}

function propertiesEditDataDialogTerm(type, properties) {
    var scopeData = {};

    if (type === "numeric") {
        var isRange = false, isMinPrecision = false, isMaxPrecision = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i]["range"]) {
                isRange = true;
                scopeData["range"] = properties[i]["range"];
            } else if (properties[i]["minPrecision"]) {
                isMinPrecision = true;
                scopeData["minimum precision"] = properties[i]["minPrecision"];
            } else if (properties[i]["maxPrecision"]) {
                isMaxPrecision = true;
                scopeData["maximum precision"] = properties[i]["maxPrecision"];
            }
        }
        if (isRange === false) {
            scopeData["range"] = null;
        }
        if (isMinPrecision === false) {
            scopeData["minimum precision"] = null;
        }
        if (isMaxPrecision === false) {
            scopeData["maximum precision"] = null;
        }

    } else if (type === "text") {
        var isType = false, isLength = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i]["type"]) {
                isType = true;
                scopeData["type"] = properties[i]["type"] === "text" ? "printable" : properties[i]["type"];
            } else if (properties[i]["length"]) {
                isLength = true;
                scopeData["length"] = properties[i]["length"];
            }
        }
        if (isType === false) {
            scopeData["type"] = "printable";
        }
        if (isLength === false) {
            scopeData["length"] = null;
        }

    } else if (type === "date-time") {
        var isFormat = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i]["format"]) {
                isFormat = true;
                scopeData["format"] = properties[i]["format"];
            }
        }
        if (isFormat === false) {
            scopeData["format"] = "date";
        }

    } else if (type === "advanced") {
        var isRegExp = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i]["regexp"]) {
                isRegExp = true;
                scopeData["regular expression"] = properties[i]["regexp"];
            }
        }
        if (isRegExp === false) {
            scopeData["regular expression"] = null;
        }
    } else if (type === "file") {
        var isMultiple = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i]["multiple"]) {
                if (properties[i]["multiple"] === "true" || properties[i]["multiple"] === true) {
                    isMultiple = true;
                }
                scopeData["multiple"] = isMultiple;
            }
        }       
    } else if (type === "pre-defined") {
        for (var i = 0; i < properties.length; i++) {
            if (properties[i]["regexp"]) {
                scopeData["regular expression"] = properties[i]["regexp"];
            } else if (properties[i]["alias"]) {
                scopeData["alias"] = properties[i]["alias"];
            }
        }

    } else if (type === "list") {
        var items = [];
        var isMultiSelect = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i]["item"]) {
                items.push(properties[i]["item"]);
            } else if (properties[i]["isMultiSelect"]) {
                if (properties[i]["isMultiSelect"] === "true" || properties[i]["isMultiSelect"] === true) {
                    isMultiSelect = true;
                }
            }
        }
        scopeData["items"] = items;
        scopeData["isMultiSelect"] = isMultiSelect;
    }

    return scopeData;
}

function propertiesDataDialogTerm(type, properties) {
    var scopeData = {};

    if (type === "numeric") {
        var isRange = false, isMinPrecision = false, isMaxPrecision = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i].name === "range") {
                isRange = true;
                scopeData["range"] = properties[i].value;
            } else if (properties[i].name === "minPrecision") {
                isMinPrecision = true;
                scopeData["minimum precision"] = properties[i].value;
            } else if (properties[i].name === "maxPrecision") {
                isMaxPrecision = true;
                scopeData["maximum precision"] = properties[i].value;
            }
        }
        if (isRange === false) {
            scopeData["range"] = null;
        }
        if (isMinPrecision === false) {
            scopeData["minimum precision"] = null;
        }
        if (isMaxPrecision === false) {
            scopeData["maximum precision"] = null;
        }

    } else if (type === "text") {
        var isType = false, isLength = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i].name === "type") {
                isType = true;
                scopeData["type"] = properties[i].value === "text" ? "printable" : properties[i].value;
            } else if (properties[i].name === "length") {
                isLength = true;
                scopeData["length"] = properties[i].value;
            }
        }
        if (isType === false) {
            scopeData["type"] = "printable";
        }
        if (isLength === false) {
            scopeData["length"] = null;
        }

    } else if (type === "date-time") {
        var isFormat = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i].name === "format") {
                isFormat = true;
                scopeData["format"] = properties[i].value;
            }
        }
        if (isFormat === false) {
            scopeData["format"] = "date";
        }

    } else if (type === "advanced") {
        var isRegExp = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i].name === "regexp") {
                isRegExp = true;
                scopeData["regular expression"] = properties[i].value;
            }
        }
        if (isRegExp === false) {
            scopeData["regular expression"] = null;
        }
    } else if (type === "file") {
        var isMultiple = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i].name === "multiple") {
                if (properties[i].value === "true" || properties[i].value === true) {
                    isMultiple = true;
                }
                scopeData["multiple"] = isMultiple;
            }
        }        
    } else if (type === "pre-defined") {
        for (var i = 0; i < properties.length; i++) {
            if (properties[i].name === "regexp") {
                scopeData["regular expression"] = properties[i].value;
            } else if (properties[i].name === "alias") {
                scopeData["alias"] = properties[i].value;
            }
        }

    } else if (type === "list") {
        var items = [];
        var isMultiSelect = false;
        for (var i = 0; i < properties.length; i++) {
            if (properties[i].name === "item") {
                items.push(properties[i].value);
            } else if (properties[i].name === "isMultiSelect") {
                if (properties[i].value === "true" || properties[i].value === true) {
                    isMultiSelect = true;
                }
            }
        }
        scopeData["items"] = items;
        scopeData["isMultiSelect"] = isMultiSelect;
    }

    return scopeData;
}

function propertiesDialogTerm(type, term, nestLevel, order) {
    var nestedTerms = [];
    var returnValue = [];
    var pItems = {};
    var i = order === 6 ? 5 : 6;

    for (i; i < term.length; i++) {
        var displayOrder = term[i]["ui-display-order"];
        displayOrder++;
        term[i]["ui-display-order"] = displayOrder;
    }

    if (type === "numeric") {
        nestedTerms.push(createDialogTerm("range", "text", [], [], 0, null, false, nestLevel, true, false, null));
        pItems["range"] = null;
        nestedTerms.push(createDialogTerm("minimum precision", "text", [], [], 0, null, false, nestLevel, true, false, null));
        pItems["minimum precision"] = null;
        nestedTerms.push(createDialogTerm("maximum precision", "text", [], [], 0, null, false, nestLevel, true, false, null));
        pItems["maximum precision"] = null;
    } else if (type === "text") {
        var textTypeValidation = textTypeListForValidationDialogTerm();
        var textTypeProperties = listProperties(textTypeValidation[0]["properties"]);
        nestedTerms.push(createDialogTerm("type", "list", textTypeValidation, textTypeProperties, 0, "printable", false, nestLevel, true, false, null));
        pItems["type"] = "printable";
        nestedTerms.push(createDialogTerm("length", "text", [], [], 0, null, false, nestLevel, true, false, null));
        pItems["length"] = null;
    } else if (type === "date-time") {
        var dateTimeTypeValidation = dateTimeTypeListForValidationDialogTerm();
        var dateTimeTypeProperties = listProperties(dateTimeTypeValidation[0]["properties"]);
        nestedTerms.push(createDialogTerm("format", "list", dateTimeTypeValidation, dateTimeTypeProperties, 0, "date", false, nestLevel, true, false, null));
        pItems["format"] = "date";
    } else if (type === "list") {
        var validation = [];
        validation.push({type: "text", properties: []});
        nestedTerms.push(createDialogTerm("isMultiSelect", "boolean", [], [], 0, null, false, nestLevel, true, false, null));
        pItems["isMultiSelect"] = null;
        nestedTerms.push(createDialogTerm("items", "text", validation, {}, 0, null, false, nestLevel, true, true, null));
        pItems["items"] = [];
    } else if (type === "advanced") {
        nestedTerms.push(createDialogTerm("regular expression", "text", [], [], 0, null, false, nestLevel, true, false, null));
        pItems["regular expression"] = null;
    } else if (type === "file") {
        nestedTerms.push(createDialogTerm("multiple", "boolean", [], [], 0, null, false, nestLevel, true, false, null));
        pItems["multiple"] = null;
    } else if (type === "pre-defined") {
        var aliasTypeValidation = aliasListForValidationDialogTerm();
        var aliasTypeProperties = listProperties(aliasTypeValidation[0]["properties"]);
        nestedTerms.push(createDialogTerm("alias", "list", aliasTypeValidation, aliasTypeProperties, 0, null, false, nestLevel, true, false, null));
        pItems["alias"] = null;
        nestedTerms.push(createDialogTerm("regular expression", "text", [], [], 0, null, false, nestLevel, true, false, null));
        pItems["regular expression"] = null;
    }

    term.push(createDialogTerm("properties", "text", [], [], order, null, false, nestLevel - 1, false, false, nestedTerms));
    var sortedTerms = sortTermsByOrder(term);
    returnValue.push(sortedTerms);
    returnValue.push(pItems);

    return returnValue;
}

function removeNestedTerm(count, isTemplate) {
    var id = isTemplate ? "idTemplateDialogController" : "idVocabDialogController";
    var scope = getAngularElementScope(id + count);
    if (scope !== undefined) {
        scope.term = {};
        scope.data = null;
        var e1 = document.getElementById(id + count);
        dojo.destroy(e1);
        dojo.destroy(dijit.byId("button_" + count).domNode);
        dijit.byId("button_" + count).destroyRecursive(true);
    }
}

function setOrderOfTerms(definition, scope, isEdit, isTemplate) {
    var currentScopeTerm = scope.term;
    if (!isTemplate) {
        if (!isEdit) {
            currentScopeTerm.push(definition);
            var sortedTerms = sortTermsByName(currentScopeTerm);
            scope.term = sortedTerms;
        } else {
            var index = null;
            for (var i = 0; i < currentScopeTerm.length; i++) {
                if (currentScopeTerm[i].version === definition["version"]) {
                    index = i;
                    break;
                }
            }
            currentScopeTerm.splice(index, 1);
            currentScopeTerm.push(definition);
            scope.term = sortTermsByName(currentScopeTerm);
        }
    } else {
        if (!isEdit) {
            currentScopeTerm.push(definition);
            var sortedTerms = sortTermsByOrder(currentScopeTerm);
            for (var i = 0; i < sortedTerms.length; i++) {
                if (sortedTerms[i]["ui-display-order"] >= definition["ui-display-order"] && sortedTerms[i].alias !== definition["alias"]) {
                    var dOrder = parseInt(sortedTerms[i]["ui-display-order"]);
                    dOrder = dOrder + 1;
                    sortedTerms[i]["ui-display-order"] = dOrder;
                }
            }
            scope.term = sortTermsByOrder(sortedTerms);
        } else {
            var index = null;
            var isTerm = false;
            for (var i = 0; i < currentScopeTerm.length; i++) {
                if (isTerm) {
                    var dOrder = parseInt(currentScopeTerm[i]["ui-display-order"]);
                    dOrder = dOrder - 1;
                    currentScopeTerm[i]["ui-display-order"] = dOrder;
                }
                if (currentScopeTerm[i].alias === definition["alias"]) {
                    index = i;
                    isTerm = true;
                }
            }
            currentScopeTerm.splice(index, 1);
            currentScopeTerm.push(definition);
            for (var i = 0; i < currentScopeTerm.length; i++) {
                if (currentScopeTerm[i]["ui-display-order"] >= definition["ui-display-order"] && currentScopeTerm[i].alias !== definition["alias"]) {
                    var dOrder = parseInt(currentScopeTerm[i]["ui-display-order"]);
                    dOrder = dOrder + 1;
                    currentScopeTerm[i]["ui-display-order"] = dOrder;
                }
            }
            scope.term = sortTermsByOrder(currentScopeTerm);
        }
    }
//    scope.data = instantiateTerm(scope.term, {});
    scope.$apply();
}

function sortTermsByName(currentTerms) {
    var store = new dojo.store.Memory({data: currentTerms});
    var sortedTerms = store.query(null, {sort: [{attribute: "name", descending: false}]});
    return sortedTerms;
}

function sortTermsByOrder(currentTerms) {
    var store = new dojo.store.Memory({data: currentTerms});
    var sortedTerms = store.query(null, {sort: [{attribute: "ui-display-order", descending: false}]});
    return sortedTerms;
}

function templateTermDialogTerm(templateName, url, sync, scope) {
    var res = templateName.split(":");
    dojo.xhrGet({
        url: url + res[0],
        sync: (sync ? true : false),
        load: function(data) {
            var template = JSON.parse(data);
            var templateTerms = JSON.parse(template.terms);
            var templateDetail = JSON.parse(template.definition);
            var templateTermValidation = templateTermListForDialogTerm(templateTerms);
            var templateTermProperties = listProperties(templateTermValidation[0]["properties"]);

            var term = scope.term;
            var index = getTermIndex(term, "id");
            term.splice(index, 2);
            index++;
            term.push(createDialogTerm("id", "list", templateTermValidation, templateTermProperties, index, null, false, 1, true, false, null));
            index++;
            term.push(createDialogTerm("name", "list", templateTermValidation, templateTermProperties, index, null, false, 1, true, false, null));
            var sortedTerms = sortTermsByOrder(term);
            scope.term = sortedTerms;
            var scopeData = scope.data;
            scopeData["uuid"] = templateDetail.uuid;
            scopeData["version"] = templateDetail.version;
            scopeData["id"] = null;
            scopeData["name"] = null;
            scopeData["alias"] = null;
            scopeData["description"] = null;
            scopeData["value"] = null;
            scopeData["required"] = null;
            scopeData["list"] = null;
            scope.data = scopeData;
            scope.$apply();
        }
    });
}

function templateTermListForDialogTerm(templateTermsData) {
    var templateTermValidation = [];
    var templateTermValidationProperties = [];
    for (var i = 0; i < templateTermsData.length; i++) {
        templateTermValidationProperties.push(listProperty("item", templateTermsData[i].alias));
    }
    templateTermValidationProperties.push(listProperty("isMultiSelect", false));
    templateTermValidation.push({type: "list", properties: templateTermValidationProperties});
    return templateTermValidation;

}

function termDefinition(data, count, isTemplate, nestLevel) {
    var definition = {};
    definition["nestLevel"] = nestLevel;
    definition["isDefinition"] = true;
    definition["uuid"] = data["uuid"];
    definition["version"] = data["version"];
    definition["required"] = data["required"] === null ? false : data["required"];
    definition["list"] = data["list"] === null ? false : data["list"];
    definition["name"] = data["name"];
    definition["description"] = data["description"];
    definition["type"] = data["validation type"] === "pre-defined" ? "advanced" : data["validation type"];
    definition["validation"] = termValidation(data["validation type"], data["properties"]);
    definition["ui-display-order"] = isTemplate ? data["display order"] : 0;
    definition["alias"] = isTemplate ? data["alias"] : data["name"];
    definition["id-field"] = null;
    definition["name-field"] = null;
    definition["use-alias"] = null;
    definition["versionName"] = null;
    definition["read-only"] = false;
    definition["scale"] = null;
    definition["length"] = null;
    definition["unit"] = data["unit"];
    definition["value"] = data["default value"];
    definition["properties"] = termProperties(data["validation type"], data["properties"]);
    definition["term"] = null;
    var nestedTermsData = [];
    for (var i = 0; i < count; i++) {
        var id = isTemplate ? "idTemplateDialogController" : "idVocabDialogController";
        var scope = getAngularElementScope(id + i);
        if (scope !== undefined) {
            definition["isDefinition"] = false;
            var nestedTermDefinition = termDefinition(scope.data, 0, isTemplate, 2);
            nestedTermsData.push(nestedTermDefinition);
        }
    }
    definition["terms"] = nestedTermsData;
    return definition;
}

function termProperties(type, properties) {
    var property = {};
    if (type === "list") {
        var items = [];
        for (var i = 0; i < properties["items"].length; i++) {
            items.push({id: properties["items"][i], name: properties["items"][i]});
        }
        property["items"] = items;
        property["isMultiSelect"] = properties["isMultiSelect"] === null ? false : properties["isMultiSelect"];
    } else {
        property = properties;
    }
    return property;
}

function termValidation(type, properties) {
    var validation = [];
    var validationProperties = [];
    if (type !== "boolean" && type !== "not defined") {
        if (type === "list") {
            for (var i = 0; i < properties["items"].length; i++) {
                var property = {};
                property["item"] = properties["items"][i];
                validationProperties.push(property);
            }
            var property = {};
            property["isMultiSelect"] = properties["isMultiSelect"] === null ? false : properties["isMultiSelect"];
            validationProperties.push(property);

        } else if (type === "numeric") {
            var property = {};
            property["range"] = properties["range"] === null || properties["range"] === "" ? null : properties["range"];
            validationProperties.push(property);
            property = {};
            property["minPrecision"] = properties["minimum precision"] === null || properties["minimum precision"] === "" ? null : properties["minimum precision"];
            validationProperties.push(property);
            property = {};
            property["maxPrecision"] = properties["maximum precision"] === null || properties["maximum precision"] === "" ? null : properties["maximum precision"];
            validationProperties.push(property);

        } else if (type === "text") {
            var property = {};
            property["type"] = properties["type"];
            validationProperties.push(property);
            property = {};
            property["length"] = properties["length"] === null || properties["length"] === "" ? null : properties["length"];
            validationProperties.push(property);

        } else if (type === "date-time") {
            var property = {};
            property["format"] = properties["format"];
            validationProperties.push(property);

        } else if (type === "advanced") {
            var property = {};
            property["regexp"] = properties["regular expression"] === null || properties["regular expression"] === "" ? null : properties["regular expression"];
            validationProperties.push(property);

        } else if (type === "file") {
            var property = {};
            property["multiple"] = properties["multiple"] === null ? false : properties["multiple"];
            validationProperties.push(property);

        } else if (type === "pre-defined") {
            var property = {};
            property["alias"] = properties["alias"];
            validationProperties.push(property);
            property = {};
            property["regexp"] = properties["regular expression"];
            validationProperties.push(property);
            type = "advanced";

        }
    }
    validation.push({type: type, properties: validationProperties});
    return validation;
}

function textTypeListForValidationDialogTerm() {
    var validation = [];
    var properties = [];
    properties.push(listProperty("item", "printable"));
    properties.push(listProperty("item", "alphanumeric"));
    properties.push(listProperty("item", "alpha"));
    properties.push(listProperty("item", "numeric"));
    properties.push(listProperty("isMultiSelect", false));
    validation.push({type: "list", properties: properties});
    return validation;
}

function typeOfDialogTerm(order, value, isTemplate, readOnly) {
    var validation = [];
    var properties = [];
    if (isTemplate) {
        properties.push(listProperty("item", "Database Record"));
    } else {
        properties.push(listProperty("item", "New Term"));
    }
    properties.push(listProperty("item", "Existing Term"));
    properties.push(listProperty("isMultiSelect", false));
    validation.push({type: "list", properties: properties});
    var termProperties = {};
    var items = [];
    var isMultiSelect = false;
    for (var i = 0; i < properties.length; i++) {
        if (properties[i].isMultiSelect) {
            isMultiSelect = properties[i].isMultiSelect;
        } else if (properties[i].item) {
            items.push({id: properties[i].item, name: properties[i].item});
        }
    }
    termProperties["items"] = items;
    termProperties["isMultiSelect"] = isMultiSelect;
    return createDialogTerm("type of term", "list", validation, termProperties, order, value, readOnly, 1, true, false, null);
}

function validationDialogTerm(type) {
    var scope = getAngularElementScope("idVocabDialogController");
    var term = scope.term;
    var scopeData = scope.data;
    var value = propertiesDialogTerm(type, term, 2, 6);
    scopeData["validation type"] = type;
    scopeData["properties"] = value[1];
    scope.term = value[0];
    scope.data = scopeData;
//    scope.$apply();
}

function validateAttachToTerm(data, isEdit) {
    var error = null;

    if (data["target template"] === "") {
        error = "Please select a target template";
    } else if (data["id"] === "") {
        error = "Please select the id term";
    } else if (data["name"] === "") {
        error = "Please select the name term";
    } else if (data["alias"] === "" || data["alias"] === null) {
        error = "Please select an alias for the term";
    } else {
        var scope = getAngularElementScope("idTemplateController");
        var length = isEdit ? scope.term.length : scope.term.length + 1;
        if (parseInt(data["display order"]) < 1 || parseInt(data["display order"]) > length) {
            error = "Please select display order between 1 and " + length;
        }
    }

    return error;
}

function validateExistTerm(data, nestedTermCount, isTemplate, isEdit) {
    var error = null;

    if (data["vocabulary"] === "") {
        error = "Please select a vocabulary";
    } else if (data["vocabulary term"] === "") {
        error = "Please select a vocabulary term";
    } else if (isTemplate) {
        var scope = getAngularElementScope("idTemplateController");
        var length = isEdit ? scope.term.length : scope.term.length + 1;
        if (data["alias"] === "" || data["alias"] === null) {
            error = "Please enter an alias for the main term";
        } else if (parseInt(data["display order"]) < 1 || parseInt(data["display order"]) > length) {
            error = "Please select display order between 1 and " + length;
        }
    } else if (nestedTermCount > 0) {
        var actualTermCount = 0;
        for (var i = 0; i < nestedTermCount; i++) {
            var id = isTemplate ? "idTemplateDialogController" : "idVocabDialogController";
            var scope = getAngularElementScope(id + i);
            if (scope !== undefined) {
                var nestedData = scope.data;
                if (nestedData["vocabulary"] === "") {
                    error = "Please select the vocabulary for nested term " + actualTermCount;
                    break;
                } else if (nestedData["vocabulary term"] === "") {
                    error = "Please select the vocabulary term for nested term " + actualTermCount;
                    break;
                } else if (nestedData["alias"] === "" || nestedData["alias"] === null) {
                    error = "Please enter an alias for nested term " + actualTermCount;
                    break;
                }
                actualTermCount++;
            }
        }
    }

    return error;
}

function validateNewTerm(data, nestedTermCount) {
    var error = null;
    if (data["name"] === "" || data["name"] === null) {
        error = "Please enter name of term";
    } else if (data["description"] === "" || data["description"] === null) {
        error = "Please enter brief description of term";
    } else if (data["validation type"] === "" || data["validation type"] === null) {
        error = "Please select a validation type of term";
    } else if (data["validation type"] === "numeric" && data["properties"]["range"] === null && data["properties"]["minimum precision"] === null && data["properties"]["maximum precision"] === null) {
        error = "Please enter a property for numeric validation type";
    } else if (data["validation type"] === "list") {
        if (data["properties"]["items"].length === 0) {
            error = "Please enter at least one item for list validation type";
        } else {
            for (var i = 0; i < data["properties"]["items"].length; i++) {
                if (data["properties"]["items"][i] === null) {
                    error = "Please remove null Item(s) from list validation type";
                    break;
                }
            }
        }

    } else if (data["validation type"] === "advanced" && data["properties"]["regular expression"] === null) {
        error = "Please enter a regular expression for advanced validation type";
    } else if (data["validation type"] === "pre-defined" && data["properties"]["alias"] === "") {
        error = "Please select an alias for pre-defined validation type";
    } else if (nestedTermCount > 0) {
        var actualTermCount = 0;
        for (var i = 0; i < nestedTermCount; i++) {
            var id = "idVocabDialogController";
            var scope = getAngularElementScope(id + i);
            if (scope !== undefined) {
                var nestedData = scope.data;
                if (nestedData["vocabulary"] === "") {
                    error = "Please select the vocabulary for nested term " + actualTermCount;
                    break;
                } else if (nestedData["vocabulary term"] === "") {
                    error = "Please select the vocabulary term for nested term " + actualTermCount;
                    break;
                } else if (nestedData["alias"] === "" || nestedData["alias"] === null) {
                    error = "Please enter an alias for nested term " + actualTermCount;
                    break;
                }
                actualTermCount++;
            }
        }
    }

    return error;
}

function validationListForDialogTerm() {
    var validation = [];
    var properties = [];
    properties.push(listProperty("item", "numeric"));
    properties.push(listProperty("item", "text"));
    properties.push(listProperty("item", "list"));
    properties.push(listProperty("item", "date-time"));
    properties.push(listProperty("item", "advanced"));
    properties.push(listProperty("item", "pre-defined"));
    properties.push(listProperty("item", "boolean"));
    properties.push(listProperty("item", "file"));
//    properties.push(listProperty("item", "not defined"));
    properties.push(listProperty("isMultiSelect", false));
    validation.push({type: "list", properties: properties});
    return validation;
}

function vocabListForDialogTerm(vocabData) {
    var vocabValidation = [];
    var vocabValidationProperties = [];
    for (var i = 0; i < vocabData.length; i++) {
        vocabValidationProperties.push(listProperty("item", vocabData[i].id + ":" + vocabData[i].name));
    }
    vocabValidationProperties.push(listProperty("isMultiSelect", false));
    vocabValidation.push({type: "list", properties: vocabValidationProperties});
    return vocabValidation;
}

function vocabTermListForDialogTerm(vocabTermsData) {
    var vocabTermValidation = [];
    var vocabTermValidationProperties = [];
    for (var i = 0; i < vocabTermsData.length; i++) {
        vocabTermValidationProperties.push(listProperty("item", vocabTermsData[i].name + ":" + vocabTermsData[i].uuid + ":" + vocabTermsData[i].version));
    }
    vocabTermValidationProperties.push(listProperty("isMultiSelect", false));
    vocabTermValidation.push({type: "list", properties: vocabTermValidationProperties});
    return vocabTermValidation;

}

function vocabTermDetailsDialogTerm(vocabTermName, url, urlVocab, isNested, scope, isTemplate) {
    var count = 0;
    var res = vocabTermName.split(":");
    dojo.xhrGet({
        url: url + res[1] + "/" + res[2],
        sync: true,
        load: function(data) {
            var vocabTermDetail = JSON.parse(data);
            if (vocabTermDetail.hasError) {
                showMessage(vocabTermDetail.message);
                var scopeData = scope.data;
                scopeData["vocabulary term"] = "";
                scopeData["alias"] = "";
                scopeData["uuid"] = null;
                scopeData["version"] = null;
                scopeData["name"] = "";
                scopeData["description"] = "";
                scopeData["validation type"] = "";
                scopeData["default value"] = "";
                scopeData["required"] = false;
                scopeData["list"] = false;
                scopeData["unit"] = "";
                scope.data = scopeData;
//                scope.$apply();
            } else {
                var name = vocabTermDetail.name;
                var description = vocabTermDetail.description;
                var value = vocabTermDetail.value;
                var required = vocabTermDetail.required;
                var list = vocabTermDetail.list;
                var unit = vocabTermDetail.unit;
                var uuid = vocabTermDetail.uuid;
                var version = vocabTermDetail.version;
                var type = null, properties = null;
                if (vocabTermDetail.validation !== null) {
                    properties = vocabTermDetail.validation.validator[0].property;
                    type = getType(vocabTermDetail.validation.validator[0].type, properties);
                } else {
                    type = "not defined";
                }

//                var validationTypeValidation = validationListForDialogTerm();
//                var validationTypeProperties = listProperties(validationTypeValidation[0]["properties"]);

                var scopeData = scope.data;
                var term = scope.term;
                var index = getTermIndex(term, "validation type");
                term.splice(index, 1);
                index++;
                term.push(createDialogTerm("validation type", "text", [], [], index, null, true, 1, true, false, null));
                var sortedTerms = sortTermsByOrder(term);
                index++;
                if (type !== "boolean" && type !== "not defined") {
                    var returnValue = propertiesDialogTerm(type, sortedTerms, 2, index);
                    sortedTerms = returnValue[0];
                    scopeData["properties"] = propertiesDataDialogTerm(type, properties);
                }

                scopeData["alias"] = null;
                scopeData["uuid"] = uuid;
                scopeData["version"] = version;
                scopeData["name"] = name;
                scopeData["description"] = description;
                scopeData["validation type"] = type;
                scopeData["default value"] = value;
                scopeData["required"] = required;
                scopeData["list"] = list;
                scopeData["unit"] = unit;
                if (vocabTermDetail["term"].length > 0 && !isNested) {
                    count = populateNestedTerms(urlVocab, vocabTermDetail["term"], isTemplate, false);
                }
                scope.term = sortedTerms;
                scope.data = scopeData;
//                scope.$apply();
            }
        }
    });
    return count;
}

function vocabTermDialogTerm(vocabName, url, scope) {
    var res = vocabName.split(":");
    dojo.xhrGet({
        url: url + res[0],
        sync: true,
        load: function(data) {
            var vocabTerms = JSON.parse(data);
            var vocabTermValidation = vocabTermListForDialogTerm(vocabTerms);
            var vocabTermProperties = listProperties(vocabTermValidation[0]["properties"]);

            var term = scope.term;
            var index = getTermIndex(term, "vocabulary term");
            term.splice(index, 1);
            index++;
            term.push(createDialogTerm("vocabulary term", "list", vocabTermValidation, vocabTermProperties, index, null, false, 1, true, false, null));
            var sortedTerms = sortTermsByOrder(term);            
            var scopeData = scope.data;
            scopeData["vocabulary term"] = null;
            scopeData["name"] = "";
            scopeData["alias"] = "";
            scopeData["description"] = "";
            scopeData["validation type"] = "";
            scopeData["default value"] = "";
            scopeData["required"] = false;
            scopeData["list"] = false;
            scopeData["unit"] = "";
            scope.data = scopeData;
            scope.term = sortedTerms;
        }
    });
}



