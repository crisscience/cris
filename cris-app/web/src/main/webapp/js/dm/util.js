cris.require([
    "dojo/dom",
    "dojo/date",
    "dojo/date/stamp",
    "dojo/date/locale",
    "dojo/io/iframe",
    "dijit/Dialog",
    "dijit/ProgressBar",
    "dojox/data/JsonRestStore",
    "dojox/grid/EnhancedGrid",
    "dojox/grid/enhanced/plugins/Pagination",
    "dojox/grid/enhanced/plugins/Selector",
    "dojox/grid/enhanced/plugins/Filter",
    "dojox/grid/enhanced/plugins/DnD",
    "dojox/grid/enhanced/plugins/Menu"
]);

function dateIsoToLocale(sDate, options) {
    var date = fromIsoString(sDate);
    var localeDate = toLocaleString(date, options);
    return localeDate;
}

function toLocaleString(date, options) {
    if (!options) {
        options = {};
    }

    if (!options.locale) {
        options.locale = cris.locale;
    }
    if (!options.selector) {
        options.selector = "date and time";
    }
    if (!options.formatLength) {
        options.formatLength = "short";
    }

    var sDate;
    if (!date) {
        sDate = null;
    } else {
        sDate = dojo.date.locale.format(date, options);
    }
    return sDate;
}

function toIsoString(date) {
    var sDate;
    if (!date) {
        sDate = null;
    } else {
        sDate = dojo.date.stamp.toISOString(date);
    }
    return sDate;
}

function fromIsoString(sDate) {
    var date;
    if (!sDate) {
        date = null;
    } else {
        // warning: fromISOString canot handle +/-9999 use +/-99:99 instead
        var fixedDate = sDate.slice(0, 26) + ":" + sDate.slice(26);
        date = dojo.date.stamp.fromISOString(fixedDate);
    }
    return date;
}

function stringDateTime(ms) {
    if (!ms) {
        return "";
    } else if (!isNaN(ms)) {
        var dateTime = new Date(ms);
        var year = dateTime.getFullYear();
        var month = (dateTime.getMonth() + 1) >= 10 ? "" + (dateTime.getMonth() + 1) : "0" + (dateTime.getMonth() + 1);
        var date = dateTime.getDate() >= 10 ? "" + dateTime.getDate() : "0" + dateTime.getDate();
        var hour = dateTime.getHours() >= 10 ? "" + dateTime.getHours() : "0" + dateTime.getHours();
        var minute = dateTime.getMinutes() >= 10 ? "" + dateTime.getMinutes() : "0" + dateTime.getMinutes();
        var second = dateTime.getSeconds() >= 10 ? "" + dateTime.getSeconds() : "0" + dateTime.getSeconds();
        return "" + year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
    } else {
        return "";
    }
}

function stringDate(ms) {
    if (!ms) {
        return "";
    } else if (!isNaN(ms)) {
        var dateTime = new Date(ms);
        var year = dateTime.getFullYear();
        var month = (dateTime.getMonth() + 1) >= 10 ? "" + (dateTime.getMonth() + 1) : "0" + (dateTime.getMonth() + 1);
        var date = dateTime.getDate() >= 10 ? "" + dateTime.getDate() : "0" + dateTime.getDate();
        return "" + year + "-" + month + "-" + date;
    } else {
        return "";
    }
}

function createUUID() {

    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
}

function numberDateTime(dateTime) {
    return Date.parse(dateTime);
}

function createJsonRestStore(url, idAttribute, labelAttribute) {
    var jsonRestStore;
    if (labelAttribute) { // Store for filtering select
        jsonRestStore = new dojox.data.JsonRestStore({
            target: url,
            syncMode: true,
            idAttribute: idAttribute || "id",
            labelAttribute: labelAttribute
        });

        // For filtering select, add empty option on top of dropdown
        require(["dojo/aspect"], function(aspect) {
            aspect.after(jsonRestStore, 'fetch', function(fetchResult) {
                if (fetchResult && (fetchResult.results instanceof Array) && fetchResult.query && fetchResult.query[jsonRestStore.labelAttribute] === "") {
                    var noValue = {};
                    noValue[jsonRestStore.idAttribute || "id"] = "";
                    noValue[jsonRestStore.labelAttribute] = "";
                    fetchResult.results.unshift(noValue);
                }
            });
        });
    } else {
        jsonRestStore = new dojox.data.JsonRestStore({
            target: url,
            idAttribute: idAttribute || "id"
        });
    }
    return jsonRestStore;
}

function createGrid(store, layout, node, args) {
    var grid = new dojox.grid.EnhancedGrid(
    {
        id: args.id,
        store: store,
        structure: layout,
        query: args.query || {},
        autoHeight: args.autoHeight || 100,
        rowsPerPage: args.rowsPerPage || 25,
        sortFields: args.sortFields || [{attribute: "name", descending: false}],
        plugins: {
            selector: {
                row: "single",
                col: "disabled",
                cell: "disabled"
            },
            pagination: {
                pageSizes: args.pageSizes || ["10", "25", "50"],
                sizeSwitch: args.sizeSwitch,
                maxPageStep: args.maxPageStep || 1,
                gotoButton: args.gotoButton || true
            },
            filter: {
                ruleCount: 1,
                isServerSide: true,
                disabledConditions: {
                    "anycolumn": ["equalTo", "lessThan", "lessThanOrEqualTo", "largerThan", "largerThanOrEqualTo", "contains", "startsWith", "endsWith", "notEqualTo", "notContains", "notStartsWith", "notEndsWith", "range", "isEmpty"],
                    "string": [],
                    "number": [],
                    "boolean": [],
                    "date": [],
                    "time": []
                },
                setupFilterQuery: function(commands, request) {
                    if (commands.filter) {
                        if (commands.enable) {
                            request.query.filter = dojo.toJson(commands.filter);
                        }
                    } else {
                        request.query.filter = null;
                    }
                }
            },
            dnd: {
                //setIdentifierForNewItem: setIdentifierForNewItem,
                dndConfig: {
                    "copyOnly": true,
                    "out": {
                        row: true,
                        cell: false
                    },
                    "in": false
                }
            },
            menus: {
                headerMenu: args.headerMenu || "",
                rowMenu: args.rowMenu || "",
                cellMenu: args.cellMenu || "",
                selectedRegionMenu: args.selectedRegionMenu || ""
            }
        }
    },
    args.htmlTag || document.createElement('div'));
    dojo.byId(node).appendChild(grid.domNode);
    
    if (args.filter) {
        try {
            grid.setFilter(args.filter);
        } catch (e) {}
    }
    
    grid.startup();

    return grid;
}

function createScrollableGrid(store, layout, node, args) {
    var grid = new dojox.grid.EnhancedGrid(
    {
        id: args.id,
        store: store,
        structure: layout,
        query: args.query || {},
        rowsPerPage: args.rowsPerPage || 20,
        sortFields: args.sortFields || [{attribute: "name", descending: false}],
        plugins: {
            selector: {
                row: "single",
                col: "disabled",
                cell: "disabled"
            },
            filter: {
                ruleCount: 0,
                isServerSide: true,
                //isStateful: true,
                ruleCountToConfirmClearFilter: Number.POSITIVE_INFINITY,
                disabledConditions: {
                    "anycolumn": ["equalTo", "lessThan", "lessThanOrEqualTo", "largerThan", "largerThanOrEqualTo", "contains", "startsWith", "endsWith", "notEqualTo", "notContains", "notStartsWith", "notEndsWith", "range", "isEmpty"],
                    "string": [],
                    "number": [],
                    "boolean": [],
                    "date": [],
                    "time": []
                },
                setupFilterQuery: function(commands, request) {
                    if (commands.filter) {
                        if (commands.enable) {
                            request.query.filter = dojo.toJson(commands.filter);
                        }
                    } else {
                        request.query.filter = null;
                    }
                }
            },
            dnd: {
                //setIdentifierForNewItem: setIdentifierForNewItem,
                dndConfig: {
                    "copyOnly": true,
                    "out": {
                        row: true,
                        cell: false
                    },
                    "in": false
                }
            },
            menus: {
                headerMenu: args.headerMenu || "",
                rowMenu: args.rowMenu || "",
                cellMenu: args.cellMenu || "",
                selectedRegionMenu: args.selectedRegionMenu || ""
            }
        }
    }, node);
    grid.showFilterBar(false);
    
    if (args.filter) {
        grid.setFilter(args.filter);
    }
    
    grid.startup();

    return grid;
}

function createProgressDialog(attrs) {
    attrs = attrs || {};
    var pb = new dijit.ProgressBar({indeterminate: true}, document.createElement('div'));
    var d = new dijit.Dialog({title: attrs.title || "Working...", style: "width: 300px"});
    d.setContent(pb.domNode);
    d.show();
    return d;
}

function showMessage(attrs) {
    var title = "Message";
    var content = "";
    if (typeof(attrs) === "string") {
        content = attrs;
    } else {
        attrs = attrs || {};
        title = attrs.title || title;
        content = attrs.content;
    }
    var d = new dijit.Dialog({title: title, style: "width: 300px"});
    d.setContent(content);
    d.show();
    return d;
}

function showWarning(attrs) {
    var title = "Warning";
    var content = "";
    if (typeof(attrs) == "string") {
        content = attrs;
    } else {
        attrs = attrs || {};
        title = attrs.title || title;
        content = attrs.content;
    }
    var d = new dijit.Dialog({title: title, style: "width: 300px"});
    d.setContent(content);
    d.show();
    return d;
}

function showError(attrs) {
    var title = "Error";
    var content = "";
    if (typeof(attrs) == "string") {
        content = attrs;
    } else {
        attrs = attrs || {};
        title = attrs.title || title;
        content = attrs.content;
    }
    var d = new dijit.Dialog({title: title, style: "width: 300px"});
    d.setContent(content);
    d.show();
    return d;
}

function showConfirm(attrs) {
    /*
    var title = "Confirm";
    var content = "";
    if (typeof(attrs) == "string") {
        content = attrs;
    } else {
        attrs = attrs || {};
        title = attrs.title || title;
        content = attrs.content;
    }
    var d = new dijit.Dialog({title: title, style: "width: 300px"});
    d.setContent(content);
    d.show();
    */
    var yes = confirm(attrs);
    return yes;
}

function showConfirmYesNo(attrs) {
    var dialog = new dijit.Dialog({
        title: attrs.title,
        content: attrs.message,
        style: "width: 300px"
    });

    var onButtonClickEvent = function(button) {
        return function() {
            button.callBack.apply(this, []);
            dialog.onCancel();
        };
    };

    node = dojo.create("div", {
        style: "text-align: center"
    }, dialog.containerNode);
    for (idx in attrs.buttons) {
        new dijit.form.Button({
            label: attrs.buttons[idx].label,
            onClick: onButtonClickEvent.apply(dialog, [attrs.buttons[idx]])
        }).placeAt(node);
    }

    dialog.startup();
    dialog.show();

    return dialog;
}

function objectusPostAndResultHandling(url) {
    dojo.io.iframe.send({
        url: url,
        method: "POST",
        form: "form",
        handleAs: "json",
        load: function(data) {
            var i;
            var error

            var json = data; //dojo.fromJson(data);

            // clear all error message
            var errorNodes = dojo.query(".errors");
            for (i = 0; i < errorNodes.length; i++) {
                errorNodes[i].innerHTML = "";
            }

            if (json.isValid) {
                dojo.byId("id_errors").innerHTML = "Data has been saved successfully<p/>";
                return;
            }

            // go through error list and display errors accordingly

            // general errors
            var hasGeneralErrors = false
            for (i = 0; i < json.errorList.length; i++) {
                error = json.errorList[i];
                if (!error.fieldName) {
                    hasGeneralErrors = true;
                    break;
                }
            }

            var divError = dojo.byId("id_errors");
            if (hasGeneralErrors) {
                divError.innerHTML = "General Problems:";
                var list = dojo.create("ol", {}, divError);
                //<span class="errors">Validation Failed</span>
                for (i = 0; i < json.errorList.length; i++) {
                    error = json.errorList[i];
                    if (!error.fieldName) {
                        dojo.create("li", {
                            innerHTML: error.fieldName.split("_")[2] + ":" + error.errorMessage
                        }, list);
                    }
                }
            } else {
                divError.innerHTML = "";
            }

            // field specific errors
            var formField, nodes, node;
            for (i = 0; i < json.errorList.length; i++) {
                error = json.errorList[i];

                nodes = dojo.query("[name=\"" + error.fieldName + "\"]");
                if (nodes.length > 0) {
                    node = nodes[0];
                    formField = dijit.registry.getEnclosingWidget(node);
                    if (formField) {
                        //formField.promptMessage = "${error.errorCode}";
                        //formField.invalidMessage = "${error.errorCode}";
                    }
                }

                var errorNode = dojo.byId("id_error_" + error.fieldName);
                if (errorNode) {
                    errorNode.innerHTML = "* " + error.errorMessage;
                }
            }

        },
        error: function(errorMessage) {
            // unexpected errors
            dojo.byId("id_errors").innerHTML = errorMessage + "<p/>";
        }
    });
}

function objectusGetAndResultHandling(url, query, onLoad, onError, onHandle) {
    dojo.xhrGet({
        url: url,
        content: query,
        form: "emptyForm",
        handleAs: "json",

        load: function(response, ioArgs) {
            onLoad(response, ioArgs);
        },

        error: function(errorMessage) {
            if (onError) {
                onError(errorMessage);
            }
        },

        handle: function(response, ioArgs) {
            if (onHandle) {
                onHandle(response, ioArgs);
            }
        }
    });
}

function objectusPost(url, content, onLoad, onError, onHandle, sync) {
    dojo.xhrPost({
        sync: (sync ? true : false),
        url: url,
        method: "POST",
        postData: dojo.toJson(content),
        handleAs: "json",
        headers: {"Content-Type": "application/json", "Accept": "application/json"},

        load: function(response, ioArgs) {
            if (onLoad) {
                onLoad(response, ioArgs);
            }
        },

        error: function(errorMessage) {
            if (onError) {
                onError(errorMessage);
            }
        },

        handle: function(response, ioArgs) {
            if (onHandle) {
                onHandle(response, ioArgs);
            }
        }
    });
}

function objectusGet(url, query, onLoad, onError, onHandle, sync) {
    dojo.xhrGet({
        sync: (sync ? true : false),
        url: url,
        content: query,
        headers: {"Content-Type": "application/json", "Accept": "application/json"},
        handleAs: "json",

        load: function(response, ioArgs) {
            onLoad(response, ioArgs);
        },

        error: function(errorMessage) {
            if (onError) {
                onError(errorMessage);
            }
        },

        handle: function(response, ioArgs) {
            if (onHandle) {
                onHandle(response, ioArgs);
            }
        }
    });
}

function objectusPut(url, query, data, onLoad, onError, onHandle, sync) {
    dojo.xhrPut({
        sync: (sync ? true : false),
        url: url,
        content: query,
        putData: data,
        handleAs: "json",

        load: function(response, ioArgs) {
            if (onLoad) {
                onLoad(response, ioArgs);
            }
        },

        error: function(errorMessage) {
            if (onError) {
                onError(errorMessage);
            }
        },

        handle: function(response, ioArgs) {
            if (onHandle) {
                onHandle(response, ioArgs);
            }
        }
    });
}

function objectusDelete(url, query, onLoad, onError, onHandle, sync) {
    dojo.xhrDelete({
        sync: (sync ? true : false),
        url: url,
        content: query,
        handleAs: "json",

        load: function(response, ioArgs) {
            if (onLoad) {
                onLoad(response, ioArgs);
            }
        },

        error: function(errorMessage) {
            if (onError) {
                onError(errorMessage);
            }
        },

        handle: function(response, ioArgs) {
            if (onHandle) {
                onHandle(response, ioArgs);
            }
        }
    });
}

function prettyPrint(text, delimit) {
    if (text && delimit) {
        return text.split(delimit).join(" ").replace(/\b./g, function(x) {
            if (x === '_') {
                return '';
            } else {
                return x.toUpperCase();
            }
        });
    } else {
        return "";
    }
}

function prettyPrintCamelCase(text) {
    if (text) {
        return text.replace(/([A-Z][a-z])/g, function(w){
            return " " + w;
        }).replace(/([a-z][A-Z0-9])/g, function(w){
            return w.substring(0, 1) + " " + w.substring(1, 2);
        }).replace(/\b./g, function(x) {
            return x.toUpperCase();
        });
    } else {
        return "";
    }
}

function serializeObjectId(obj) {
    var timeSecond = obj["timeSecond"];
    if (timeSecond < 0) {
        timeSecond = 0xFFFFFFFF + timeSecond + 1;
    }
    timeSecond = timeSecond.toString(16);
    timeSecond = ("00000000" + timeSecond).substr(-8);

    var machine = obj["machine"];
    if (machine < 0) {
        machine = 0xFFFFFFFF + machine + 1;
    }
    machine = machine.toString(16);
    machine = ("00000000" + machine).substr(-8);

    var inc = obj["inc"];
    if (inc < 0) {
        inc = 0xFFFFFFFF + inc + 1;
    }
    inc = inc.toString(16);
    inc = ("00000000" + inc).substr(-8);

    return timeSecond + machine + inc;
}

function isCanvasSupported(){
    var elem = document.createElement('canvas');
    return !!(elem.getContext && elem.getContext('2d'));
}

function getAngularElementScope(elementId) {
    var scope = angular.element(document.getElementById(elementId)).scope();
    return scope;
}

function getTermName(term) {
    if (term === null) {
        return null;
    }

    var termName = "";

    if (term.id) {
        termName += term.id;
    } else if (term.uuid) {
        termName += term.uuid;
    } else {
        termName += "UUID_UNKNOWN";
    }

    if (term.isList !== null && term.isList) {
        termName += "[]";
    }

    if (term.version !== null) {
        termName += "({";
        termName += "\"_template_version\":\"" + term.version + "\"";
        termName += "})";
    }

    return termName;
}

function mergeErrorMessage(message, templateUuid, name, value) {
    if (!message[templateUuid]) {
        message[templateUuid] = {};
    }

    var parts = name.split(".");
    var currentNode = message[templateUuid];
    dojo.forEach(parts, function(part, index) {
        if (index === parts.length - 1) {
            // leaf node
            currentNode[part] = value;
        } else {
            // non-leaf node
            if (!currentNode[part]) {
                currentNode[part] = {};
            }
            currentNode = currentNode[part];
        }
    });
}

function parseErrorMessage(status) {
    var message = {};

    dojo.forEach(status, function(item, index) {
        var templateUuid = item.fieldName.substring(0, 36);
        var name = item.fieldName.substring(74);
        var value = item.errorMessage;
        mergeErrorMessage(message, templateUuid, name, value);
    });
    return message;
}

function buildErrorMessage(message) {
    var errorMessage = {"" : {valid: false, errorList: [{errorMessage: message}]}};
    return errorMessage;
}

function buildDownloadLink(storageFile, name) {
    var htmlTemplate =  "<a href='{link}'>{name}</a>";
    var html = dojo.replace(htmlTemplate, {
        link: cris.baseUrl + "download/" + storageFile,
        name: name ? name : storageFile
    });

    return html;
}

function buildDownloadLinks(storageFiles) {
    var fileList = "";
    if (typeof storageFiles === "undefined" || storageFiles === null) {
        fileList = null;
    } else if (storageFiles instanceof Array) {
        dojo.forEach(storageFiles, function(item, index) {
            if (index !== 0) {
                fileList += ", ";
            }
            fileList += buildDownloadLink(item);
        });
    } else {
        fileList = buildDownloadLink(storageFiles);
    }

    return fileList;
};

function buildGlobusDownloadLink(storageFile, name) {
    var htmlTemplate = "<a href='' data-ng-click='browseFile(path, multiple, {storageFile})'>{name}</a>";
    var html = dojo.replace(htmlTemplate, {
        storageFile: storageFile,
        name: name ? name : storageFile
    });

    return html;
}

function buildGlobusDownloadLinks(storageFiles) {
    var fileList = "";
    if (typeof storageFiles === "undefined" || storageFiles === null) {
        fileList = null;
    } else if (storageFiles instanceof Array) {
        dojo.forEach(storageFiles, function(item, index) {
            if (index !== 0) {
                fileList += ", ";
            }
            fileList += buildGlobusDownloadLink("dataset[" + index + "]", item);
        });
    } else {
        fileList = buildGlobusDownloadLink("dataset", storageFiles);
    }

    return fileList;
};

function resetFileUploaders(formId) {
    var widgets = dijit.findWidgets(dojo.byId(formId));
    dojo.forEach(widgets, function(widget) {
        if (widget.declaredClass === "dojox.form.Uploader") {
            widget.reset();
        }
    });
}

function getTerm(uuid, version, dereference) {
    var content = {};
    content.version = version;
    if (dereference) {
        content.dereference = true;
    }

    var urlTemplateXml = cris.baseUrl + "templates/xml/";
    var data = dojo.xhr.get({
        url: urlTemplateXml + uuid,
        content: content,
        handleAs: "text",
        sync: true
    }).results;

    var term = convertXmlToJson(data[0]);

    return term;
}

function getLatestTerm(uuid) {
    var url = cris.baseUrl + "templates/load/?uuid=" + uuid;
    var data = dojo.xhr.get({
        url: url,
        content: {dereference: true},
        handleAs: "json",
        sync: true
    }).results;
    var term = data[0];

    return term;
}

function convertAssetStatusIdToName(id) {
    var name;
    if (id === 1) {
        name = "Operational";
    } else if (id === 0) {
        name = "Deprecated";
    } else {
        name = "";
    }
    return name;
}

function getBaseUrl() {
    var href = window.location.href;
    var idx1 = href.indexOf("//");
    var href1 = href.substring(idx1 + 2);
    var idx2 = href1.indexOf(cris.baseUrl);

    var length = idx1 + 2 + idx2 + cris.baseUrl.length - 1;

    var baseUrl = href.substring(0, length);
    return baseUrl;
}

function startsWith(text, prefix) {
    if (text) {
        return text.lastIndexOf(prefix, 0) === 0;
    } else {
        return false;
    }
}

function endsWith(text, suffix) {
    if (text) {
        return text.indexOf(suffix, text.length - suffix.length) !== -1;
    } else {
        return false;
    }
}

function isQuoted(text) {
    return startsWith(text, '"') && endsWith(text, '"');
}

function isExpression(text) {
    return startsWith(text, '${') && endsWith(text, '}');
}

function isEmpty (object) {
    for (var key in object) {
        if (object.hasOwnProperty(key)) {
            return false;
        }
    }

    return true;
}

function isJsonLike(text) {
    return startsWith(text, '{') && endsWith(text, '}');
}

function destroyNodeWidgets (node) {
    var widgets = dijit.registry.findWidgets(node);
    dojo.forEach(widgets, function(widget){
       widget.destroyRecursive(); 
    });
}

function updateTermProperties (newTerm, oldTerm, propertiesToUpdate) {
    dojo.forEach(propertiesToUpdate, function(property) {
       oldTerm[property] = newTerm[property] ;
    });
}

function updateTerm (newTerm, oldTerm, propertiesToExclude) { // Update reference term...Vocabulary term -> Template term
    var propertiesToUpdate = ['version', 'name', 'description', 'validation', 'isLatest', 'unit', 'value', 'term', 'required', 'requiredExpression', 'readOnly', 'readOnlyExpression', 'list', 'grid'];
    if (propertiesToExclude) {
        dojo.forEach(propertiesToExclude, function(property){
            var index = propertiesToUpdate.indexOf(property);
            propertiesToUpdate.splice(index, 1);
        });
    }
    if (newTerm.type === 'composite' && newTerm.term.length !== oldTerm.term.length) {
        oldTerm.term = newTerm.term;
    }
    updateTermProperties(newTerm, oldTerm, propertiesToUpdate);
}
