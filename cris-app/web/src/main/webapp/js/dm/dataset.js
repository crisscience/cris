/**********************************************************
 * Render a template
 **********************************************************/

// This function clones an item. During the process it either excludes the properties from exclusionList
// or only includes the properties from the inclustionList but not both
// Currently the properties applies to any nesting level.
function duplicateItem(item, exclusionList, inclusionList) {
    if (typeof item === "undefined" || item === null) {
        return null;
    }

    var result;
    if (item instanceof Array) {
        result = dojo.map(item, function(entry){
            return duplicateItem(entry, exclusionList, inclusionList);
        });
    } else if (item instanceof Object) {
        result = {};
        var duplicate;
        for (var key in item) {
            duplicate = true;
            if (item.hasOwnProperty(key)) {
                if (exclusionList) {
                    if (dojo.indexOf(exclusionList, key) !== -1) {
                        duplicate = false;
                    }
                } else if (inclusionList) {
                    if (dojo.indexOf(inclusionList, key) === -1) {
                        duplicate = false;
                    }
                }
            } else {
                duplicate = false;
            }

            if (duplicate) {
                result[key] = duplicateItem(item[key], exclusionList, inclusionList);
            }
        }
    } else {
        result = item;
    }

    return result;
}

/**
 * Normalize the value according to the template specified by term
 * @param {type} term Contains the definition of the term
 * @param {type} value The value of the term
 * @param (type) doNotUseDefaultValue if true then do not use the default value from the definition of the term. It is intended to be used to initialize error messages
 * @returns {unresolved} The normalized value
 */
function instantiateTerm(term, value, doNotUseDefaultValue) {
    // there are three types of values: [nothing] <- [default] <- [from passed in value]

    var result;
    if (typeof term === "undefined") {
        result = null;
    } else if (term.isDefinition) {
        // leaf node
        var type = term.list ? "json" : term.type;
        if (!doNotUseDefaultValue && value === null) {
            if (term.value && !isExpression(term.value)) {
                // use default value
                var type;
                if (term.type !== "attachTo") {
                    type = term.type;
                } else {
                    type = term["id-field-validation"].type;
                }
                if ((["boolean", "numeric"].indexOf(type) !== -1) || isQuoted(term.value)) {
                    result = dojo.fromJson(term.value);
                } else {
                    if (type === 'list' && term.properties && term.properties.isMultiSelect) {
                        result = dojo.fromJson(term.value);
                    } else {
                        result = term.value;
                    }
                }
            } else {
                result = null;
            }
        } else {
            result = value;
        }
        if (type === "boolean" && result === null) {
            // user false for the default value of boolean
            result = false;
        }
    } else if (angular.isObject(term) && term.terms) {
        // terms contains a list of terms
        result = {};
        if (value && value._id) {
            // an existing object
            result._id = value._id;
        }
        angular.forEach(term.terms, function(t, k) {
            var actualValue = value && value[t.alias];
            if (typeof actualValue === "undefined") {
                actualValue = null;
            }

            if (t["list"]) {
                if (!actualValue && t.value && !doNotUseDefaultValue) { // handle default value
                    if (!isExpression(t.value)) {
                        actualValue = angular.fromJson(t.value);
                    }
                } else { // jsonIn/init value (Array value)
                    if (!actualValue) {
                        result[t.alias] = [];
                    } else {
                        // instantiate each element of the list
                        var valueList = [];
                        //t.list = false;
                        angular.forEach(actualValue, function(v) {
                            var vv = instantiateTerm(t, v, doNotUseDefaultValue);
                            valueList.push(vv);
                        });
                        //t.list = true;
                        result[t.alias] = valueList;
                    }
                }
            } else {
                result[t.alias] = instantiateTerm(t, actualValue, doNotUseDefaultValue);
            }
        });
    }

    return result;
}

// actions: an array of {icon: "", text: "", onHover: "", onClick: "", forField: true, forArrayContainer: true, forArrayElement: true}
// e.g. scope.actions = [{icon: "famfamfam_silk_icons_v013/icons/brick.png", text: "field info", forField: true}, {icon: "famfamfam_silk_icons_v013/icons/brick_add.png", onHover: "aaa", onClick: function(id, term, dataset, message, readOnly) {alert("hello " + id + " " + term.alias + ": " + dataset);}, forArrayContainer: true}, {icon: "famfamfam_silk_icons_v013/icons/brick_delete.png", forArrayElement: true}];
function generateIconLinks(actions, scope, isField, isArrayContainer, isArrayElement) {
    if (!(actions instanceof Array)) {
        return "";
    }

    var html = "";
    var context = {
        baseUrl: cris.imagesRoot
    };

    angular.forEach(actions, function(action, index) {
        if ((isField && action.forField) || (isArrayContainer && action.forArrayContainer) || (isArrayElement && action.forArrayElement)) {
            context.term = "term";
            context.dataset = "dataset";
            context.message = "message";

            context.id = dijit.registry.getUniqueId("cris.vocabulary.info");
            context.index = index;
            context.readOnly = scope.readOnly;
            context.icon = action.icon;
            context.text = action.text;
            context.onHover = action.onHover;
            context.onClick = action.onClick;

            scope["onClick" + index] = action.onClick;

            var htmlTemplate = "&nbsp;<img id='{id}' class='inlineIcon' src='{baseUrl}/{icon}' data-ng-mouseover='{onHover}(\"{id}\", {term}, {dataset}, {message}, {readOnly})' data-ng-click='onClick{index}(\"{id}\", {term}, {dataset}, {message}, {readOnly})'/>";

            htmlTemplate += '<div data-dojo-widget="dijit/Tooltip" data-dojo-props="connectId: \'{id}\', position: [\'above\']">{text}</div>';

            html += dojo.replace(htmlTemplate, context);
        }
    });

    return html;
}

function makeTermLabel(term, scope) {
    var id = dijit.registry.getUniqueId("cris.vocabulary.info");
    var info = term.description || term.alias || term.name;
    var context = {
        id: id,
        info: info
    };
    var htmlTemplate = "<span id='{id}'>";
    var html = dojo.replace(htmlTemplate, context);
    html += "{{prettyPrint(term.alias, '_')}}";
    htmlTemplate = '<div data-dojo-widget="dijit/Tooltip" data-dojo-props="connectId: \'{id}\', position: [\'above\']">{info}</div>';
    html += dojo.replace(htmlTemplate, context);

    html += "<span style='color:red;font-weight:bold;' ng-show='isRequired()'>&nbsp;*</span>";

    // user defined icon for object field
    var iconLick = generateIconLinks(scope.actions, scope, true, false, false);
    html += iconLick;

    html += "</span>";

    return html;
}

function makeAddButton(scope)  {
    // add a add button for list item
    var context = {
        baseUrl: cris.imagesRoot
    };
    var htmlTemplate = "<img class='inlineIcon' src='{baseUrl}/famfamfam_silk_icons_v013/icons/add.png' ng-hide='readOnly' data-ng-click='addItem(null)'/>";
    var html = dojo.replace(htmlTemplate, context);

    // user defined icon for array container
    html += generateIconLinks(scope.actions, scope, false, true, false);
    return html;
}

function makeRemoveButton(scope)  {
    var context = {
        baseUrl: cris.imagesRoot
    };
    var htmlTemplate = "<img class='inlineIcon' src='{baseUrl}/famfamfam_silk_icons_v013/icons/delete.png' ng-hide='readOnly' data-ng-click='removeItem($index)'/>";
    var html = dojo.replace(htmlTemplate, context);

    // user defined icon for array elements
    html += generateIconLinks(scope.actions, scope, false, false, true);

    return html;
}

angular.module("dataset", ['angular-dojo']);

// Service provides methods that controllers share
angular.module("dataset").factory('datasetService', ['$parse', function($parse) {
    return{
        addItem: function(value) {
            // build a value object from term
            var term = this.term;
            term.list = false;
            var result = instantiateTerm(term, value, false);
            term.list = true;
            this.dataset.push(result);
        },
        removeItem: function(index) {
            var _this = this;
            showConfirmYesNo({title: "Confirm", message: "Do you want to remove item: " + index + "?",
                buttons: [
                    {label: "Yes", callBack: function() {
                        _this.dataset.splice(index, 1);
                        _this.$apply();
                    }},
                    {label: "No", callBack: function() {
                    }}
                ]
            });
        },
        prettyPrint: function(text, delimit) {
            if (text && text.indexOf(delimit) !== -1) {
                return prettyPrint(text, delimit);
            } else {
                return prettyPrintCamelCase(text);
            }
        },
        isShow: function() {
            var yes;
            var showExpression = this.term.showExpression;
            if (showExpression) {
               var evalValue = $parse(showExpression)(this.context);
               yes = (evalValue === undefined || evalValue === null ? false : evalValue);
            } else {
               yes = true;
            }

            var parent = (this.show === "false" ? false : true);

            return yes && parent;
        },
        isReadOnly: function() {
            var readOnlyExpression = this.term['read-only-expression'];
            if (readOnlyExpression) {
                var evalValue = $parse(readOnlyExpression)(this.context);
                return !evalValue ? false : true;
            } else {
                return !this.term['read-only'] ? false : true;
            }
        },
        isRequired: function(term) {
            var term_ = term ? term : this.term;
            var requiredExpression = term_['required-expression'];
            if (requiredExpression) {
                var evalValue = $parse(requiredExpression)(this.context);
                return !evalValue ? false : true;
            } else {
                return !term_['required'] ? false : true;
            }
        }
    };
}]);

angular.module("dataset").directive("crisDataset", function($compile) {
    return {
        restrict: "E",
        scope: {
            // path
            path: "@",
            // context
            context: "=",
            // current scope
            term: "=",
            dataset: "=",
            message: "=",
            // whether read only
            readOnly: "=?",
            // whether to show to hide
            show: "@",
            // user defined actions: an array of {icon: "", text: "", onHover: "", onClick: ""}
            // the callbacks will be provided with term, data, message and readOnly information
            actions: "=",
            key: "="
        },
        template: "",
        link: function (scope, element, attrs) {
            console.log("==== crisDataset ====");

            if (typeof scope.key !== 'undefined') { // From member directive
                var isList = scope.term["list"] ? true : false;
                var isNode = scope.term["isDefinition"] ? true : false;
                var template = "";
                if ((scope.term.grid === true && !isNode) || (scope.term.grid === true && isList)) {
                    var termLabel = makeTermLabel(scope.term, scope);
                    template = "<ul><li data-ng-show='isShow()'><span class='termNameLabel'>" + termLabel + "</span><cris-grid term='term' dataset='dataset' message='message' context='context' path='{{path}}'><!----></cris-grid></li></ul>";
                } else {
                    template = "<collection term='term' dataset='dataset' message='message' context='context' read-only='readOnly' show='{{show}}' actions='actions' path='{{path}}' ><!----></collection>";
                }
                element.append(template);
                $compile(element.contents())(scope);
            } else { // From top-level/index page
                var scopeClone = null;
                var content = null;
                scope.$watchCollection('term', function () {
                    if (content) {
                        scopeClone.$destroy();
                        scopeClone = null;

                        if (content[0]) {
                            destroyNodeWidgets(content[0]); // Destroy element widgets before removing element from DOM
                        }
                        content.remove();
                        content = null;
                    }

                    var template = "";
                    if (scope.term.grid === true) {
                        template = "<cris-grid term='term' dataset='dataset' message='message' context='context' path='{{path}}'><!----></cris-grid>";
                    } else if (scope.term.grid === false) {
                        template = "<collection term='term' dataset='dataset' message='message' context='context' read-only='readOnly' show='{{show}}' actions='actions' path='{{path}}' ><!----></collection>";
                    }
                    scopeClone = scope.$new();
                    element.append(template);
                    content = $compile(element.contents())(scopeClone);
                });
            }

            scope.$on('$destroy', function() {
                if (element[0]) {
                    destroyNodeWidgets(element[0]);
                }
            });
        },
        controller: ["$scope", "datasetService", function($scope, datasetService) {
            $scope.prettyPrint = datasetService.prettyPrint;
            $scope.isRequired = datasetService.isRequired.bind($scope);
            $scope.isShow = datasetService.isShow.bind($scope);
        }]
    };
});

angular.module("dataset").directive("crisGrid", ['$compile', '$parse', 'datasetService', function ($compile, $parse, datasetService) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            term: '=',
            context: '=',
            dataset: '=',
            message: '=',
            path: '@'
        },
        template: '<div style="height:100%;width:100%;overflow:auto;"></div>',
        link: function (scope, element, attrs) {

            // If server-side validation fails, re-create grid. Cells with invalid data will be highlighted.
            scope.$watch('message', function () {
                if (scope.message && errorExists()) {
                    makeCrisGrid();
                    scope.gridHasErrors = true; // Set flag for displaying error notification above grid
                }

                function arrayIsValid(message) {
                    var hasError = false;
                    message.forEach(function (item) {
                        if (item[""] && item[""].valid === false) {
                            hasError = true;
                        }
                        if (!hasError) {
                            hasError = objectIsValid(item);
                        }
                    });
                    return hasError;
                }

                function objectIsValid(message) {
                    var hasError = false;
                    var propertyNames = Object.getOwnPropertyNames(message);
                    propertyNames.forEach(function (name) {
                        if (name.trim() && !(message[name] instanceof Array) && message[name] && message[name][""].valid === false) {
                            hasError = true;
                        } else if (name && message[name] instanceof Array && message[name].length) {
                            hasError = arrayIsValid(message[name]);
                        }
                    });
                    return hasError;
                }

                function errorExists() {
                    var hasError = false;
                    if (scope.message[""]) { // Grid of type "not list"
                        return objectIsValid(scope.message);
                    } else if (scope.message instanceof Array && scope.message.length) { // Grid of type "list"
                        return arrayIsValid(scope.message);
                    }
                    return hasError;
                }
            });

            makeCrisGrid();

            function makeCrisGrid() {
                require([
                    'dojo/store/Memory',
                    'dojo/store/Observable',
                    'gridx/Grid',
                    'gridx/core/model/cache/Sync',
                    'gridx/modules/CellWidget',
                    'gridx/modules/Edit',
                    'gridx/modules/IndirectSelect',
                    'gridx/modules/RowHeader',
                    'gridx/modules/select/Row',
                    'dijit/Toolbar',
                    'gridx/modules/Bar',
                    'gridx/modules/HiddenColumns'
                ], function(Store, Observable) {
                    var isList = scope.term && scope.term["list"] ? true : false;
                    var isNode = scope.term && scope.term["isDefinition"] ? true : false;

                    if (!scope.path) {
                        scope.path = scope.term.uuid;
                    }
                    if (scope.path.indexOf('.') === -1) {
                        scope.path = scope.path + '.' + scope.term.alias;
                    }

                    var grid = null;
                    var term = scope.term;
                    if (isList && isNode) { // Simple list grid
                        var column = scope.defineGridColumn(term);
                        column.width = '140px';
                        column.initializeCellWidget = function (cellWidget, cell) {
                            var id = cell.row.id;
                            var template = "<node term='term' dataset='dataset[" + id + "]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{path}}[" + id + "]' context='context'></node>";

                            if ((scope.message instanceof Array) && scope.message[id] && scope.message[id][""] && !scope.message[id][""].valid) {
                                dojo.setStyle(cell.node(), 'backgroundColor', '#FFB2B2'); // Highlight cell
                                new dijit.Tooltip({
                                    connectId: [cell.node()],
                                    label: scope.message[id][""].errorList[0].errorMessage
                                });
                            }
                            dojo.empty(cellWidget.domNode);
                            dojo.place(template, cellWidget.domNode, "only");
                            $compile(cellWidget.domNode)(scope);

                            // ReadOnly expression watch
                            scope.$watch(function() {
                                return scope.isReadOnly();
                            }, function(newValue, oldValue) {
                                if (newValue !== oldValue && !newValue) {
                                    var widgets = dijit.findWidgets(cellWidget.domNode);
                                    if (widgets.length) {
                                        widgets[0].set('disabled', false);
                                    }
                                } else if (newValue !== oldValue && newValue) {
                                    var widgets = dijit.findWidgets(cellWidget.domNode);
                                    if (widgets.length) {
                                        widgets[0].set('disabled', true);
                                    }
                                }
                            });

                            // Watch isRequired expression
                            scope.$watch(function() {
                                return scope.isRequired();
                            }, function(newValue, oldValue) {
                                if (newValue !== oldValue && !newValue) {
                                    var widgets = dijit.findWidgets(cellWidget.domNode);
                                    if (widgets.length) {
                                        widgets[0].set('required', false);
                                    }
                                } else if (newValue !== oldValue && newValue) {
                                    var widgets = dijit.findWidgets(cellWidget.domNode);
                                    if (widgets.length) {
                                        widgets[0].set('required', true);
                                    }
                                }
                            });
                        };

                        var store = new Store({
                            idProperty: 'id'
                        })
                        grid = scope.constructGrid(true, [column], store, {headerHidden: true});

                        grid.store.setData = function (data) { // Override "setData"
                            this.index = {};
                            this.data = [];
                            for (var i = 0; i < data.length; i++) {
                                var storeItem = {};
                                storeItem.id = i;
                                storeItem[term.alias] = data[i];
                                this.data.push(storeItem);
                                this.index[i] = storeItem;
                            }
                        };

                        grid.store.add = function () { // Override "add"
                            scope.dataset.unshift("");
                            if (!scope.message) {
                                scope.message = [];
                            }
                            scope.message.unshift({}); // persist order of highlighted cells (those with errors)
                            this.setData(scope.dataset);
                        };

                        grid.store.remove = function (id) { // Override "remove"
                            if (id >= 0) {
                                delete this.index[id];
                                this.data.splice(id, 1);
                                scope.dataset.splice(id, 1);
                                this.setData(scope.dataset);
                            }
                        }

                        // Set initial data
                        if (!scope.dataset) {
                            scope.dataset = [];
                        }
                        grid.store.setData(scope.dataset);
                        grid.model.clearCache();
                        grid.body.refresh();
                    } else {
                        // For 5 or few terms, each without child terms, equally distribute width
                        var termCount = 0;
                        var hasChildTerms = false;
                        var gridProperties = {};
                        for (var i = 0; i < term.terms.length; i++) {
                            hasChildTerms = hasChildTerms || (term.terms[i].terms && term.terms[i].terms.length > 0);
                            termCount++
                        }
                        var columnWidth = null;
                        if (!hasChildTerms && termCount <= 5) {
                            gridProperties.autoWidth = false;
                            columnWidth = (100 / termCount) + '%';
                        }
                        
                        var gridColumns = [];
                        for (var i = 0; i < term.terms.length; i++) {
                            var column = scope.defineGridColumn(term.terms[i], columnWidth);
                            gridColumns.push(column);
                            scope.columnHideWatch(term.terms[i], i+1);
                        }

                        if (!isList && !isNode) { // Non list composite grid
                            var data_ = scope.dataset ? scope.dataset : {};

                            var storeData = {};
                            var columnNames = Object.getOwnPropertyNames(data_);
                            columnNames.forEach(function (name) {
                                storeData[name] = data_[name];
                            });
                            storeData.id = 1; // Gridx requires an id
                            storeData._message_ = scope.message;

                            var store = new Observable(new Store({
                                idProperty: 'id',
                                data: [storeData]
                            }));
                            var results = store.query({});

                            // Observe for changes and update underlying dataset
                            results.observe(function (item) {
                                var propertyNames = Object.getOwnPropertyNames(data_);
                                propertyNames.forEach(function (name) {
                                    data_[name] = item[name]
                                });
                            }, true);
                            grid = scope.constructGrid(false, gridColumns, store, gridProperties);
                            
                        } else if (isList && !isNode) { // List composite grid
                            
                            var store = new Observable(new Store({
                                idProperty: 'id'
                            }));
                            grid = scope.constructGrid(true, gridColumns, store, gridProperties);

                            grid.store.setData = function (data) { // Override "setData"
                                this.index = {};
                                this.data = [];
                                for (var i = 0; i < data.length; i++) {
                                    var item = {};
                                    var columnNames = Object.getOwnPropertyNames(data[i]);
                                    columnNames.forEach(function (name) {
                                        item[name] = data[i][name];
                                    });
                                    item.id = i;
                                    item._message_ = scope.message;
                                    this.data.push(item);
                                    this.index[i] = item;
                                }
                            };

                            grid.store.add = function () { // Override "add"
                                var empty = instantiateTerm(scope.term, {});
                                var newItem = {};

                                var propertyNames = Object.getOwnPropertyNames(empty);
                                propertyNames.forEach(function (name) {
                                    newItem[name] = empty[name];
                                });
                                scope.dataset.unshift(newItem);

                                if (!scope.message) {
                                    scope.message = [];
                                }
                                scope.message.unshift({}); // persist order of highlighted cells (those with errors)
                                this.setData(scope.dataset);
                            };

                            grid.store.remove = function (id) { // Override remove
                                if (id >= 0) {
                                    delete this.index[id];
                                    this.data.splice(id, 1);
                                    scope.dataset.splice(id, 1);
                                    this.setData(scope.dataset);
                                }
                            };

                            // Set initial data
                            if (!scope.dataset) {
                                scope.dataset = [];
                            }
                            grid.store.setData(scope.dataset);
                            grid.model.clearCache();
                            grid.body.refresh();

                            var results = store.query({});

                            // Observe for changes and update underlying dataset
                            results.observe(function (item) {
                                var idProperty = grid.store.idProperty;
                                var index = item[idProperty];
                                delete item[idProperty];
                                scope.dataset[index] = item;
                            }, true);
                        }
                    }
                    dojo.empty(element[0]);
                    if (grid) {
                        grid.placeAt(element[0]);
                        grid.startup();
                        scope.crisGrid = grid;
                        scope.hideGridColumns(); // Evaluate show expression on initial load of empty grid
                        
                        // Cache ids of all grids on page
                        if (document._crisGridIds_) {
                            var exists = false;
                            document._crisGridIds_.forEach(function (_id) {
                                if (grid.id === _id) {
                                    exists = true;
                                }
                            });
                            if (!exists) {
                                document._crisGridIds_.push(grid.id);
                            }
                        } else {
                            document._crisGridIds_ = [grid.id];
                        }

                        // Custom event handler resizes all grids on page. This fixes several resize issues: 1) Some single-column grids are taller than content height, even with the 'autoHeight' grid property. 
                        // 2) For nested grids, if inner grid changes size, the outer grid needs resizing as well to fit content
                        angular.element(document).unbind('_onCrisGridResizeOnUpdate_').bind('_onCrisGridResizeOnUpdate_', function (event, id, rowHeight) {
                            setTimeout(function(){ 
                                for (var i = document._crisGridIds_.length - 1; i >= 0; i--) {
                                    var gridId = document._crisGridIds_[i];
                                    var grid_ = dijit.registry.byId(gridId);
                                    var gridInDom = dojo.byId(gridId) ? true : false; // Grid is in dom 
                                    if (typeof grid_ !== 'undefined' && gridInDom) {
                                        grid_.resize();
                                    }
                                }
                            }, 350); // Delay event handling by a few milliseconds, otherwise grid resize won't work sometimes as it may be called when grid hasn't completly renderd in dom
                            event.stopPropagation();
                        });

                        dojo.ready(function() {
                            // Delay event by a few milliseconds for proper grid resize in event handler
                            setTimeout(function(){ angular.element(document).trigger('_onCrisGridResizeOnUpdate_'); }, 50);    
                        });

                        // Scope watch at grid level...

                        // Gridx bug workaround: If grid is displayed as part of list (ul,li), and grid term has showExpression,
                        // switching from hidden to display won't show grid unless window or grid is resized.
                        if (scope.term.showExpression) {
                            scope.$watch(function () {
                                return scope.isShow();
                            }, function (show) {
                                if (show) {
                                    angular.element(document).trigger('_onCrisGridResizeOnUpdate_');
                                }
                            })
                        }
                    }
                });
            }
            // Append general error message. Hidden if no grid errors.
            if (!element.prev().hasClass("crisGridErrorMessage")) {
                scope.errorMessage = 'Highlighted cells have validation errors. Hover mouse over cell for details.';
                var messageDiv = angular.element('<div data-ng-show="gridHasErrors" class="error crisGridErrorMessage" ng-bind="errorMessage"></div>');
                messageDiv.insertBefore(element);
                $compile(messageDiv)(scope);
            }
        },
        controller: ["$scope", "$compile", "$parse", "datasetService", function($scope, $compile, $parse, datasetService) {
            $scope.isShow = datasetService.isShow.bind($scope);
            $scope.isReadOnly = datasetService.isReadOnly.bind($scope);
            $scope.isRequired = datasetService.isRequired.bind($scope);
            
            $scope.defineGridColumn = function (term, columnWidth) {
                var columnDefinition = {};
                columnDefinition.widgetsInCell = true;
                columnDefinition.field = term.alias;
                columnDefinition.name = makeGridHeaderLabel(term);
                columnDefinition.width = columnWidth ? columnWidth : gridColumnWidth(term) + 'px';
                var rowRequired = []; // keeps track of required flag for each cell in column (in multi-row grid)
                var scopePath = $scope.path;

                columnDefinition.decorator = function () {
                    return '<div></div>';
                };
                columnDefinition.initializeCellWidget = function (cellWidget, cell) {
                    // TODO: This callback fires many times. This is not good for performance because this is were we create widgets.
                    // Find a way to cache widgets.
                    var newScope = $scope.$new(true);
                    newScope.term = term;
                    newScope.dataset = cell.grid.store.data[cell.row.index()][cell.column.field()];
                    newScope.readOnly = datasetService.isReadOnly.call(newScope);
                    newScope.show = $scope.show;
                    newScope.path = scopePath;
                    var path = "path ? (path + \".\" + term.alias) : term.uuid";

                    // Each grid row must have a unique context
                    // For a list grid, the context will have a reference to the current row data using underscore notation E.g. _CompositeTerm == CompositeTerm[rowIndex]
                    var rowContext = cell.grid['_crisGridRow[' + cell.row.index() + ']_'];
                    if (rowContext) {
                        newScope.context = rowContext;
                    } else {
                        newScope.context =angular.copy($scope.context);
                        var _path = newScope.path.substring(newScope.path.indexOf('.') + 1); // Remove template id from term path
                        if (_path === $scope.term.alias) { // Top level of context
                            if ($scope.term.list) {
                                $scope.$watch('context["' + $scope.term.alias + '"][' + cell.row.index() + ']', function(value) {
                                    if (value) {
                                        newScope.context['_' + _path] = value;
                                    }
                                }, true);
                            } else {
                                $scope.$watch('context["' + $scope.term.alias + '"]', function(value) {
                                    if (value) {
                                        dojo.mixin(newScope.context[_path], value);
                                    }
                                }, true);
                            }
                        } else {
                            _path = _path.substring(0, _path.lastIndexOf('.')); // Move term path one level up E.g Term1.Term2.Term3 to Term1.Term2

                           // Replace indexes in path with underscore notation. E.G. From A.B[0].C.D[6] to A._B.C._D
                            _path = _path.replace(/\b[a-zA-Z0-9_]+\[[0-9]+\]/g, function(val) {
                                return '_' + val.replace(/\[[0-9]+\]/g, "");
                            });

                            if ($scope.term.list) {
                                $scope.$watch('context.' + _path, function(value) {
                                    if (value) {
                                        var subContext = $parse(_path)(newScope.context);
                                        subContext['_' + $scope.term.alias] = value[$scope.term.alias][cell.row.index()];
                                        dojo.mixin(subContext, value);
                                    }
                                }, true);
                            } else {
                                $scope.$watch('context.' + _path, function(value) {
                                    if (value) {
                                        var subContext = $parse(_path)(newScope.context);
                                        dojo.mixin(subContext, value);
                                    }
                                }, true);
                            }
                        }

                        // Update row context if context data that is not part of grid data changes
                        $scope.$watch('context', function(newValue, oldValue) {
                            if (newValue && !angular.equals(newValue, oldValue)) {
                                var propNames = Object.getOwnPropertyNames(newValue);
                                dojo.forEach(propNames, function(name) {
                                    if (name !== $scope.term.alias) {
                                        if (typeof newValue[name] === 'object' && newValue[name] !== null) {
                                            dojo.mixin(newScope.context[name], newValue[name]);
                                        } else {
                                            newScope.context[name] = newValue[name];
                                        }
                                    }
                                });
                            }
                        }, true);

                        cell.grid['_crisGridRow[' + cell.row.index() + ']_'] = newScope.context;
                    }

                    if ($scope.term.list) {
                        newScope.path = newScope.path + '[' + cell.row.index() + ']'; // Add row index to path
                    }

                    var message = cell.grid.store.data[cell.row.index()]._message_;
                    if (message instanceof Array && message.length && message[cell.row.index()]) {
                        newScope.message = message ? message[cell.row.index()][cell.column.field()] : "";
                    } else {
                        newScope.message = message ? message[cell.column.field()] : "";
                    }

                    // If error message, highlight and add tooltip to cell
                    if (newScope.message && newScope.message[""] && !newScope.message[""].valid) {
                        dojo.setStyle(cell.node(), 'backgroundColor', '#FFB2B2');

                        new dijit.Tooltip({
                            connectId: [cell.node()],
                            label: newScope.message[""].errorList[0].errorMessage
                        });
                    }

                    // Watch for edit changes
                    newScope.$watch('dataset', function (newValue, oldValue) {
                        if (newValue !== oldValue) {
                            var cellData = cell.grid.store.data[cell.row.index()][cell.column.field()];
                            if (cellData instanceof Array && newValue instanceof Array) { // Multiselects, etc.
                                while (cellData.length) {
                                    cellData.pop();
                                }
                                newValue.forEach(function (item) {
                                    cellData.push(item);
                                });
                            } else { // Single value fields (Date, textbox, checkbox)
                                cell.grid.store.data[cell.row.index()][cell.column.field()] = newValue;

                                // Notify this cell's parent grid of the data update
                                if (cell.grid.store.notify) {
                                    cell.grid.store.notify(cell.grid.store.data[cell.row.index()], cell.row.id);
                                }
                            }
                        }
                    });

                    newScope.addItem = datasetService.addItem.bind(newScope);
                    newScope.removeItem = datasetService.removeItem.bind(newScope);
                    newScope.prettyPrint = datasetService.prettyPrint;
                    newScope.isShow = datasetService.isShow.bind(newScope);
                    newScope.isReadOnly = datasetService.isReadOnly.bind(newScope);
                    newScope.isRequired = datasetService.isRequired.bind(newScope);

                    // Required expression watch...display/hide required asterisk in header
                    newScope.$watch(function() {
                        return newScope.isRequired();
                    }, function(newValue, oldValue) {
                        rowRequired = rowRequired.slice(0, cell.grid.rows().length);
                        rowRequired[cell.row.index()] = newValue ? true : false;
                        if (rowRequired.indexOf(true) !== -1) {
                            angular.element(cell.column.headerNode()).find('.crisGridRequiredFlag').css({'visibility' : 'visible'});
                        } else {
                            angular.element(cell.column.headerNode()).find('.crisGridRequiredFlag').css({'visibility' : 'hidden'});
                        }
                    });

                    // Hide grid cells or columns based on show-expression
                    if (newScope.term.showExpression) {
                        var cellScope = newScope.$new(true);
                        cellScope.term = newScope.term;
                        cellScope.context = newScope.context;
                        cellScope.isShow = datasetService.isShow.bind(cellScope);
                        
                        cellScope.$watch(function () {
                            return cellScope.isShow();
                        }, function (show) {
                            if (cellWidget.cell.row.id === cell.row.id) { // Important! only evaluate the watch for this cell in this column
                                var cellsToHide = cell.grid["_crisGridColumn" + cell.column.id  + "CellsToHide_"];
                                if (!cellsToHide) {
                                    cellsToHide = cell.grid["_crisGridColumn" + cell.column.id  + "CellsToHide_"] = [];
                                }

                                var index = cellsToHide.indexOf(cell.row.id)
                                if (!show) {
                                    if (index < 0) {
                                        cellsToHide.push(cell.row.id);
                                    }
                                    dojo.setStyle(cellWidget.domNode, 'visibility', 'hidden');
                                } else {
                                    if (index > -1) {
                                        cellsToHide.splice(index, 1);
                                    }
                                    //dojo.setStyle(cellWidget.domNode, 'visibility', 'visible');
                                    dojo.setStyle(cellWidget.domNode, 'visibility', 'inherit');
                                }

                                // Cleanup old references for cells-to-hide if rows are added/removed
                                if (cell.grid.rowCount() - 1 === cell.row.id) {
                                    var _cellsToHide = angular.copy(cellsToHide);
                                    dojo.forEach(_cellsToHide, function(cellId) {
                                        var _widget = cell.grid.cellWidget.getCellWidget(cellId, cell.column.id);
                                        if ((_widget && dojo.getStyle(_widget.domNode, 'visibility') !== 'hidden') || !_widget) {
                                            var index = cellsToHide.indexOf(cellId);
                                            if (index !== -1) {
                                                cellsToHide.splice(index, 1);
                                            }
                                        }
                                    });
                                }
                                
                                // Gridx has hiddenColumnsModule for hiding columns but it does not work well if grid has inner grids; e.g. hiding column 0 of parent grid also hides column 0 of all inner grid.
                                var columnIdentifier = cell.grid.id + '-' + cell.column.id;
                                var headerAndCellNodes = dojo.query('#' + columnIdentifier + '.gridxCell, [aria-describedby^="' + columnIdentifier + '"].gridxCell', cell.grid.domNode);
                                if (cellsToHide.length === cell.grid.rowCount()) { // All cells in column are hidden, therefore hide entire column
                                    dojo.forEach(headerAndCellNodes, function(node) {
                                        dojo.setStyle(node, 'display', 'none');
                                    });
                                    cell.grid.resize();
                                } else { // Not all cells in column are hidden, therefore unhide column
                                    dojo.forEach(headerAndCellNodes, function(node) {
                                       if (dojo.getStyle(node, 'display') === 'none') {
                                           dojo.setStyle(node, 'display', '');
                                       }
                                    });
                                    cell.grid.resize();
                                }
                            }
                        });
                        cellScope.$apply();
                    }

                    var isList = term && term["list"] ? term && term["list"] : false;
                    var isNode = term && term["isDefinition"] ? term && term["isDefinition"] : false;
                    if (typeof newScope.dataset === "undefined" || newScope.dataset === null) {
                        if (isList) {
                            newScope.dataset = [];
                        } else if (!isNode) {
                            newScope.dataset = {};
                        } else {
                            newScope.dataset = null;
                        }
                    }

                    var template = "";
                    var termLabel = makeTermLabel(newScope.term, newScope);
                    var htmlAddButton = null;
                    var htmlRemoveButton = null;
                    if (isList && !newScope.readOnly) {
                        htmlAddButton = makeAddButton(newScope);
                        htmlRemoveButton = makeRemoveButton(newScope);

                        // add a add button for list item
                        termLabel += ":&nbsp;" + htmlAddButton;
                    }
                    
                    if (isNode) {
                        if (!isList) {
                            newScope.message = ""; // For grid, do not display error message alongside node. Instead cell will be highlighted and a tooltip added.
                            template = "<span data-ng-show='isShow()'><node term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context'></node></span>";
                        } else {
                            dojo.setStyle(cell.node(), 'vertical-align', 'top');
                            if (term.grid) {
                                template = "<cris-grid term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context' ></cris-grid>";
                            } else {
                                template = "<span data-ng-show='isShow()'><span class='termNameLabel'>" + termLabel + "</span><ul><li ng-repeat='item in dataset track by $index'><span class='termIndexLabel'>{{$index}}:&nbsp;" + htmlRemoveButton + "</span><node term='term' dataset='dataset[$index]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}[{{$index}}]' context='context'></node></li></ul></span>";
                            }
                        }
                    } else { // Composite
                        if (!isList && !term.grid) {
                            template = "<span data-ng-show='isShow()' style='display:inline-block'><member term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context'></member></span>";
                        } else if (isList && !term.grid) {
                            dojo.setStyle(cell.node(), 'vertical-align', 'top'); 
                            template = "<span data-ng-show='isShow()'><span class='termNameLabel'>" + termLabel + "</span><ul><li ng-repeat='item in dataset track by $index'><span class='termIndexLabel'>{{$index}}:&nbsp;" + htmlRemoveButton + "</span><span style='display:inline-block'><member term='term' dataset='dataset[$index]' message='message[$index]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}[{{$index}}]' context='context'></member></span></li></ul></span>";
                        } else {
                            dojo.setStyle(cell.node(), 'vertical-align', 'top'); 
                            template = "<cris-grid term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context' ></cris-grid>";
                        }
                    }
                    
                    // If cell content increases in size, resize grid to fit content
                    // This is the case when a non-grid composite list term is in the cell. Adding/removing items affects grid height
                    newScope.cellWidgetDomNode = cell.widget().domNode;
                    newScope.$watch('cellWidgetDomNode.scrollHeight', function (newValue, oldValue) {
                        if (newValue && oldValue && newValue !== oldValue) {
                            angular.element(document).trigger('_onCrisGridResizeOnUpdate_');
                        }
                    });

                    dojo.empty(cellWidget.domNode);
                    dojo.place(template, cellWidget.domNode, "only");
                    $compile(cellWidget.domNode)(newScope);
                };

                function gridColumnWidth(term) { // Estimate width of grid column
                    var pixelCount = 0;
                    var termArray = [];
                    termArray.push(term);

                    while (termArray.length) {
                        var term_ = termArray.pop();
                        if (term_.terms && term_.grid) {
                            term_.terms.forEach(function (t) {
                                termArray.push(t);
                            });
                            pixelCount += 80; // Additional offset pixels if cell has grid
                        } else {
                            // Get sum of all leaf term pixels
                            if (term_.terms) { // term is composite but not grid
                                pixelCount += 300;
                                
                                // Loop through child terms of non-grid composite term. Adjust width based on whether some child terms are composite themselves.
                                var largestChild = null;
                                dojo.forEach(term_.terms, function(t_){
                                    if (t_.terms && (!largestChild
                                            || (t_.grid && largestChild.grid && largestChild.terms.length < t_.terms.length)
                                            || (t_.grid && !largestChild.grid) || (t_.terms.length > largestChild.terms.length))) {
                                        largestChild = t_;
                                    }
                                });
                                if (largestChild) {
                                    pixelCount = 70;
                                    pixelCount += gridColumnWidth(largestChild);
                                }
                            } else if (term_.grid) { // term is leaf node and grid
                                pixelCount += 190;
                            } else { // leaf term is neither composite nor a grid
                                pixelCount += 160;
                            }
                        }
                    }
                    return pixelCount;
                }

                function makeGridHeaderLabel(term) {
                    var label = term.alias ? term.alias : "";
                    label = prettyPrint(label, '_');
                    if (term.unit) {
                        label += ' (' + term.unit + ')';
                        term.unit = null; // remove unit to prevent node directive from displaying it too.
                    }
                    var visibility = datasetService.isRequired.call({context: $scope.context}, term) ? 'visible' : 'hidden';
                    label += '<span class="crisGridRequiredFlag" style="color:red;font-weight:bold;visibility:' + visibility + ';">&nbsp;*</span>';
                    return label;
                }

                return columnDefinition;
            }
            
            $scope.constructGrid = function (isList, columnStructure, store, optionalGridProperties) {
                function addItem(grid) {
                    console.log('___________Cris Grid - Add Item_________________');
                    grid.store.add();
                    grid.model.clearCache();
                    grid.body.refresh();
                    angular.element(document).trigger('_onCrisGridResizeOnUpdate_');
                }

                function removeItem(grid) {
                    console.log('___________Cris Grid - Remove Item_______________');
                    var rowsSelected = grid.select.row.getSelected();
                    var itemId = rowsSelected[0];
                    if (typeof itemId !== 'undefined') {
                        grid.store.remove(itemId);
                        grid.model.clearCache();
                        grid.body.refresh();
                        angular.element(document).trigger('_onCrisGridResizeOnUpdate_');
                    }
                }

                // add/remove toobar icons
                var style = dojo.create("style", {type: "text/css"}, dojo.query("head")[0]);
                dojo.attr(style, {innerHTML: ".addIcon { background-image: url('" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/add.png');background-repeat: no-repeat;width: 20px;height: 16px;text-align: center;}" +
                            ".removeIcon { background-image: url('" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/delete.png');background-repeat: no-repeat;width: 20px;height: 16px;text-align: center;"});

                var addButton = new dijit.form.Button({
                    label: "Add",
                    iconClass: 'addIcon',
                    showLabel: false,
                });

                var deleteButton = new dijit.form.Button({
                    label: "Remove",
                    iconClass: 'removeIcon',
                    showLabel: false,
                });

                //Attach toolbar to grid
                var toolbar = new dijit.Toolbar({}, 'toolbar');
                toolbar.addChild(addButton);
                toolbar.addChild(deleteButton);
                toolbar.startup();

                // Initial grid modules
                var modules = [
                    'gridx/modules/CellWidget',
                    'gridx/modules/Edit',
                    'gridx/modules/HiddenColumns'
                ];

                // Load more modules as needed
                if (isList) {
                    modules.push('gridx/modules/Bar');
                    modules.push('gridx/modules/IndirectSelect');
                    modules.push('gridx/modules/RowHeader');
                    modules.push('gridx/modules/select/Row');
                }

                var gridProperties = {
                    autoWidth:true,
                    autoHeight: true,
                    bodyRowHoverEffect: false,
                    selectRowMultiple: false,
                    cacheClass: 'gridx/core/model/cache/Sync',
                    structure: columnStructure,
                    store: store,
                    editLazySave: false,
                    barTop: isList ? [toolbar] : null,
                    modules: modules
                }
                dojo.mixin(gridProperties, optionalGridProperties);

                var grid = new gridx.Grid(gridProperties);

                if (grid.barTop) {
                    // Attach callbacks to toolbar buttons
                    var toolbarChildren = grid.barTop[0].getChildren();
                    for (var i = 0; i < toolbarChildren.length; i++) {
                        if (toolbarChildren[i].label === "Add") {
                            toolbarChildren[i].onClick = function () {
                                addItem(grid);
                            }
                        } else if (toolbarChildren[i].label === "Remove") {
                            toolbarChildren[i].onClick = function () {
                                removeItem(grid);
                            }
                        }
                    }
                }

                // Returns all widgets in grid. Without getChildren() client-side form validation won't work for grid widgets.
                grid.getChildren = function () {
                    var children = [];
                    var rows = this.rows();
                    var columns = this.columns();
                    rows.forEach(function (row) {
                        columns.forEach(function (col) {
                            var cellWidget = grid.cell(row.index(), col.id).widget();
                            if (cellWidget) {
                                var widgetsInCell = dijit.registry.findWidgets(cellWidget.domNode);
                                widgetsInCell.forEach(function (widget) {
                                    children.push(widget);
                                });
                            }
                        });
                    });
                    return children;
                }
                return grid;
            }

            // When defining grid columns, remember which columns to hide when grid loads
            // This watch is for empty grids. When grid has data, the watch in initializeCellWidget method handles column hiding.
            $scope.columnHideWatch = function(term, columnId) {
                if (!$scope.columnsToHide) {
                    $scope.columnsToHide = [];
                }

                if (term.showExpression) {
                    var columnScope = $scope.$new(true);
                    columnScope.term = term;
                    columnScope.context = $scope.context;
                    columnScope.isShow = datasetService.isShow.bind(columnScope);
                    
                    function showHide(show) {
                        var index = $scope.columnsToHide.indexOf(columnId);
                        if (!show) {
                            if (index < 0) {
                                $scope.columnsToHide.push(columnId);
                            }
                        } else {
                            if (index > -1) {
                                $scope.columnsToHide.splice(index, 1);
                            }
                        }
                        $scope.hideGridColumns();
                    }

                    columnScope.$watch(function () {
                        return columnScope.isShow();
                    }, function (show) {
                        showHide(show);
                    });
                    
                    showHide(columnScope.isShow()); // Initial call
                }
            };
            
            $scope.hideGridColumns = function() {
                if ($scope.columnsToHide && $scope.crisGrid && !$scope.crisGrid.store.data.length) {
                    dojo.forEach($scope.columnsToHide, function(colId) {
                        var headerNodeId = $scope.crisGrid.id + '-' + colId;
                        var headerCellNode = dojo.query('[id^="' + headerNodeId + '"].gridxCell', $scope.crisGrid.domNode);
                        if (headerCellNode.length) {
                            dojo.setStyle(headerCellNode[0], 'display', 'none');
                        }
                    });

                    // Unhide previously hidden columns
                    dojo.forEach($scope.crisGrid.columns(), function(column) {
                        var index = $scope.columnsToHide.indexOf(parseInt(column.id));
                        if (index === -1) {
                            var headerNodeId = $scope.crisGrid.id + '-' + parseInt(column.id);
                            var headerCellNode = dojo.query('[id^="' + headerNodeId + '"].gridxCell', $scope.crisGrid.domNode);
                            if (headerCellNode.length) {
                                if (dojo.getStyle(headerCellNode[0], 'display') === 'none') {
                                    dojo.setStyle(headerCellNode[0], 'display', '');
                                }
                            }
                        }
                    });
                    $scope.crisGrid.resize();
                }
            };
        }]
    };
}]);

angular.module("dataset").directive("collection", ["$compile", "$parse", function($compile, $parse) {
    return {
        restrict: "E",
        scope: {
            // path
            path: "@?",
            // context
            context: "=",
            // current scope
            term: "=",
            dataset: "=",
            message: "=",
            // whether read only
            readOnly: "=",
            // whether to show to hide
            show: "@",
            // user defined actions: an array of {icon: "", text: "", onHover: "", onClick: ""}
            // the callbacks will be provided with term, data, message and readOnly information
            actions: "="
        },
        template: "",
        link: function(scope, element, attrs) {
            console.log("==== collection ====");
            console.dir(scope);

            if (scope.readOnly === undefined) {
                scope.readOnly = false;
            }
            if (scope.show === undefined || scope.show === null || scope.show === "") {
                scope.show = "true";
            }
            if (!scope.context) {
                if (scope.dataset) {
                    scope.context = scope.dataset;
                } else {
                    scope.context = {};
                }
            }

            var isList = scope.term && scope.term["list"] ? scope.term && scope.term["list"] : false;
            var isNode = scope.term && scope.term["isDefinition"] ? scope.term && scope.term["isDefinition"] : false;
            if (typeof scope.dataset === "undefined" || scope.dataset === null) {
                if (isList) {
                    scope.dataset = [];
                } else if (!isNode) {
                    scope.dataset = {};
                } else {
                    scope.dataset = null;
                }
            }

            var termLabel = makeTermLabel(scope.term, scope);
            var htmlAddButton = null;
            var htmlRemoveButton = null;
            if (isList && !scope.readOnly) {
                htmlAddButton = makeAddButton(scope);
                htmlRemoveButton = makeRemoveButton(scope);

                // add a add button for list item
                termLabel += ":&nbsp;" + htmlAddButton;
                termLabel += "<span class='error' ng-show=\"message[''].valid === false\">&nbsp;* {{message[''].errorList[0].errorMessage ? 'required' : ''}}</span>";
            }

            var path = "path ? (path + \".\" + term.alias) : term.uuid";

            if (isNode) {
                // object(simple): {t1 : {}}
                if (!isList) {
                    // single node
                    console.log("0000 node 0000");
                    element.append("<ul><li data-ng-show='isShow()'><span class='termNameLabel'>" + termLabel + "</span><node term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context'></node></li></ul>");
                } else {
                    // list node
                    console.log("1111 node 1111");
                    element.append("<ul><li data-ng-show='isShow()'><span class='termNameLabel'>" + termLabel + "</span><ul><li ng-repeat='item in dataset track by $index'><span class='termIndexLabel'>{{$index}}:&nbsp;" + htmlRemoveButton + "</span><node term='term' dataset='dataset[$index]' message='message[$index]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}[{{$index}}]' context='context'></node></li></ul></li></ul>");
                }
            } else {
                // object(composite): {..., terms: {t1 : {}, t2 : {}, ...}}
                if (!isList) {
                    // single member
                    console.log("2222 member 2222");
                    if (isEmpty(scope.context) || !scope.path) {
                        element.append("<ul><member term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context'></member></ul>");
                    } else {
                        element.append("<ul><li data-ng-show='isShow()'><span class='termNameLabel'>" + termLabel + "</span><member term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context'></member></li></ul>");
                    }
                } else {
                    // list member
                    console.log("3333 node 3333");
                    element.append("<ul><li data-ng-show='isShow()'><span class='termNameLabel'>" + termLabel + "</span><ul><li ng-repeat='item in dataset track by $index'><span class='termIndexLabel'>{{$index}}:&nbsp;" + htmlRemoveButton + "</span><member term='term' dataset='dataset[$index]' message='message[$index]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}[{{$index}}]' context='context'>{{updateContext(term,$index,context," + path + ")}}</member></li></ul></li></ul>");
                }
            }

            $compile(element.contents())(scope);
        },
        controller: ["$scope", "datasetService", function($scope, datasetService) {
            $scope.addItem = datasetService.addItem.bind($scope);
            $scope.removeItem = datasetService.removeItem.bind($scope);
            $scope.prettyPrint = datasetService.prettyPrint;
            $scope.isShow = datasetService.isShow.bind($scope);
            $scope.isRequired = datasetService.isRequired.bind($scope);

            $scope.updateContext = function(term, index, _context, path) {
                // For a composite term list, add a reference of the current list index to the context using underscore notation E.g. _CompositeTerm == CompositeTerm[rowIndex]
                var _path = path.substring(path.indexOf('.') + 1); // remove template id from term path
                if (_path === term.alias) { // top level of context
                    _context['_' + term.alias] = _context[term.alias][index];
                } else {
                    _path = _path.substring(0,_path.lastIndexOf('.'));
                    var subContext = $parse(_path)(_context);
                    subContext['_' + term.alias] = subContext[term.alias][index];
                }
            }
         }]
    };
}]);

angular.module("dataset").directive("member", function($compile) {
    return {
        restrict: "E",
        scope: {
            // path
            path: "@",
            // context
            context: "=",
            // current scope
            term: "=",
            dataset: "=",
            message: "=",
            // whether read only
            readOnly: "=",
            // whether to show to hide
            show: "@",
            // user defined actions: an array of {icon: "", text: "", onHover: "", onClick: ""}
            // the callbacks will be provided with term, data, message and readOnly information
            actions: "="
        },
        template: "",
        link: function(scope, element, attrs) {
            console.log("==== member ====");
            console.dir(scope);

            if (scope.readOnly === undefined) {
                scope.readOnly = false;
            }
            if (scope.show === undefined || scope.show === null || scope.show === "") {
                scope.show = "true";
            }

            element.append("<cris-dataset ng-repeat='(key, definition) in term.terms' key='key' term='term.terms[key]' dataset='dataset[term.terms[key].alias]' message='message[term.terms[key].alias]' read-only='readOnly' show='{{show}}' actions='actions' path='{{path}}' context='context'></cris-dataset>");
            $compile(element.contents())(scope);
        },
        controller: ["$scope", function($scope) {
        }]
    };
});

angular.module("dataset").directive("node", function($compile) {
    return {
        restrict: "E",
        scope: {
            // path
            path: "@",
            // context
            context: "=",
            // current scope
            term: "=",
            dataset: "=",
            message: "=",
            // whether read only
            readOnly: "=",
            // whether to show to hide
            show: "@"
        },
        template: "",
        link: function(scope, element, attrs) {
            console.log("===== node =====");
            console.dir(scope);

            var currentTerm = scope.term;

            require([
                "dojo/ready",
                "dojo/json",
                "dojo/store/Memory",
                "dojo/data/ObjectStore",
                "dijit/Tooltip",
                "dijit/form/FilteringSelect",
                "dojo/aspect"
            ], function(ready, JSON, Memory, ObjectStore, Tooltip, FilteringSelect, aspect) {
                if (scope.readOnly === undefined) {
                    scope.readOnly = false;
                }
                if (scope.show === undefined || scope.show === null || scope.show === "") {
                    scope.show = "true";
                }
                scope.require = scope.isRequired();

                if (currentTerm.type === "attachTo") {
                    var idField = currentTerm["id-field"];
                    var nameField = currentTerm["name-field"];
                    var uuid = currentTerm.uuid;
                    var query = currentTerm.query;
                    var baseUrl = cris.baseUrl + "rest/objectus/" + uuid;
                    
                    var queryObj = {};
                    queryObj[idField] = {$exists:true,$ne:null};
                    queryObj[nameField] = {$exists:true,$ne:null};
                    
                    if (query) {
                        var q = dojo.fromJson(query);
                        dojo.mixin(queryObj, q);
                    }
                    query = dojo.toJson(queryObj);
                    var url = baseUrl + "/?query=" + query;
                    scope.store = new createJsonRestStore(url, (idField || 'id'), nameField);
                    
                    scope.$watch(function() {
                        with (scope.context) {
                            var newValue;
                            if (query) {
                                newValue = dojo.toJson(eval("(" + query + ")"));
                            } else {
                                newValue = null;
                            }
                        };
                        return newValue;
                    }, function(value) {
                        console.log("**** query: " + value);
                        url = baseUrl + "/?query=" + value;
                        scope.store.target = url;
                    });
                    
                    /*
                     * for attach-to we need to initialize the store otherwise we won't be able see the initial value if there's one
                     */
                    var value = scope.dataset;
                    if (value) {
                        scope.store.fetch({query : '?' + idField + '=' + dojo.toJson(value)});
                    }
                } else if (typeof currentTerm.properties !== "undefined" && currentTerm.properties && currentTerm.properties.items) {
                    var items = currentTerm.properties.items;

                    if (currentTerm.properties.isMultiSelect) {
                        scope.store = new ObjectStore({objectStore: new Memory({idProperty: "id", data: items}), labelProperty: "name"});
                    } else {
                        if (!scope.isRequired()) {
                            var a = [{id: "", name: ""}];
                            items = a.concat(items);
                        }
                        scope.store = new Memory({data: items});
                    }
                } else {
                    scope.store = new Memory({data: []});
                }

                // leaf nodes have values
                // create a widget creation template
                // Leaf node
                //  * attach-to
                //  * pick list
                //  * text input
                //  * boolean input
                //  * date/time picker
                //  * file upload
                // Non-leaf node
                //  * non-array: display name
                //  * array: display none with a Add button
                function getTermType(term) {
                    var type = null;

                    if (!term) {
                        type = null;
                    } else if (term.validation && term.validation.validator && term.validation.validator.length > 0) {
                        type = term.validation.validator[0].type;
                    } else {
                        type = term.type;
                    }

                    return type;
                }
                var currentTermType = getTermType(currentTerm);

                var readOnly = false;
                if (currentTerm.value && (typeof currentTerm.value === "string") && (currentTerm.value.indexOf("${") !== -1) &&  currentTerm.value.indexOf("}") !== -1) {
                    readOnly = true;
                    var value = currentTerm.value;
                    var expression;
                    if (value.substring(0, 1) === "\"" && value.substring(value.length - 1, value.length) === "\"") {
                        expression = value.substring(3, value.length - 2);
                    } else {
                        expression = value.substring(2, value.length - 1);
                    }
                    var REGEXP_UUID = /\b([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12})/i;
                    if (REGEXP_UUID.test(expression)) {
                        // query backend
                        // break expression into termPath and query string
                        var index = expression.indexOf("(");
                        var termPath;
                        var queryString;
                        if (index === -1) {
                            termPath = expression;
                            queryString = "{}";
                        } else {
                            termPath = expression.substring(0, expression.indexOf("("));
                            queryString = expression.substring(expression.indexOf("(") + 1, expression.length - 1);
                        }
                        scope.$watch(function() {
                            with(scope.context) {
                                var newValue;
                                if (queryString) {
                                    newValue = dojo.toJson(eval("(" + queryString + ")"));
                                } else {
                                    newValue = null;
                                }
                            };
                            return newValue;
                        }, function(newValue, oldValue) {
                            if (newValue !== "null") {
                                var requestUrl = cris.baseUrl + "rest/objectus/?" +"name=" + termPath + '&query=' + newValue;
                                // submit the query
                                dojo.xhrGet({
                                    url: requestUrl,
                                    handleAs: "text",
                                    load: function(data) {
                                        var result = data;
                                        scope.dataset = dojo.fromJson(result);
                                        scope.message = null;
                                        scope.$apply();
                                    },
                                    error: function(error) {
                                        scope.dataset = null;
                                        var errorMessage = error.responseText ? dojo.fromJson(error.responseText).message : error.message;
                                        scope.message = buildErrorMessage(errorMessage);
                                        scope.$apply();
                                    }
                                });
                            }
                        });
                    } else {
                        // expression
                        scope.$watch(function() {
                            with(scope.context) {
                                try {
                                    var newValue =  eval(expression);
                                } catch (e) {
                                    var newValue = null;
                                }
                            };
                            return newValue;
                        }, function(newValue, oldValue) {
                            if (typeof (newValue) !== "undefined" && !(isNaN(newValue) && typeof newValue === "number")) {
                                scope.dataset = newValue;
                                scope.message = null;
                            } else {
                                var invalidValue = "At least one of the dependent term has invalid value: " + newValue;
                                scope.dataset = null;
                                scope.message = buildErrorMessage(invalidValue);
                            }
                        });
                    }
                }

                var context = {ngModel: 'dataset', required: '{{isRequired()}}', show: '{{show}}', readOnly: '{{isReadOnly()}}', context: 'context', constraints: "{places: \"0,20\", pattern: \"#0.####################\"}"};
                var tag, propType, propDataDojoWidget, propDataDojoProps, propDataDojoStore, propNgModel, propNgBind;
                propDataDojoProps = "required: {required}";
                if (readOnly || scope.readOnly) {
                    propDataDojoProps += ", disabled: true";
                } else {
                    propDataDojoProps += ", disabled: {readOnly}";
                }
                propDataDojoStore = "";
                propNgModel = "{ngModel}";
                propNgBind = "";
                propNgClick = "";
                if (currentTermType === "boolean") {
                    tag = "input";
                    propType = "boolean";
                    propDataDojoWidget = "dijit/form/CheckBox";
                } else if (currentTermType === "numeric") {
                    tag = "input";
                    propType = "numeric";
                    propDataDojoWidget = "dijit/form/NumberTextBox";
                    propDataDojoProps += ", constraints: {constraints}";
                } else if (currentTermType === "text") {
                    tag = "input";
                    propType = "text";
                    if (currentTerm.properties && currentTerm.properties["ui-vertical-lines"] > 1) {
                        propDataDojoWidget = "dijit/form/SimpleTextarea";
                        propDataDojoProps += ", rows: " + currentTerm.properties["ui-vertical-lines"];
                    } else {
                        propDataDojoWidget = "dijit/form/ValidationTextBox";
                    }
                    if (currentTerm.properties && currentTerm.properties.length) {
                        propDataDojoProps += ", maxLength: " + currentTerm.properties.length;
                    }
                } else if (currentTermType === "textarea") {
                    tag = "input";
                    propType = "textarea";
                    propDataDojoWidget = "dijit/form/TextBox";
                } else if (currentTermType === "date") {
                    tag = "input";
                    propType = "date";
                    propDataDojoWidget = "dijit/form/DateTextBox";
                } else if (currentTermType === "time") {
                    tag = "input";
                    propType = "time";
                    propDataDojoWidget = "dijit/form/TimeTextBox";
                } else if (currentTermType === "date-time") {
                    tag = "input";
                    propType = "date-time";
                    propDataDojoWidget = "dijit/form/DateTextBox";
                } else if (currentTermType === "list") {
                    tag = "select";
                    propType = "text";
                    if (currentTerm.properties && currentTerm.properties.isMultiSelect) {
                        propDataDojoWidget = "dojox/form/CheckedMultiSelect";
                        propDataDojoProps += ", placeHolder: \"Choose value(s)...\"" + ", " + "multiple: " + "\"" + true + "\"";
                    } else {
                        propDataDojoWidget = "dijit/form/FilteringSelect";
                        propDataDojoProps += ", placeHolder: \"Choose a value...\"";
                    }
                    propDataDojoStore = "store";
                } else if (currentTermType === "file") {
                    context.path = scope.path;
                    if (currentTerm.properties && currentTerm.properties.multiple === "true") {
                        scope.multiple = true;
                        context.multiple = "true";
                    } else {
                        scope.multiple = false;
                        context.multiple = "false";
                    }
                    var globus = false;
                    if (currentTerm.properties && currentTerm.properties.globus === "true") {
                        globus = true;
                    } else {
                        globus = false;
                    }
                    if (globus) {
                        scope.browseFile = function(termAlias, multiple, storageFile) {
                            doGlobusDialog(termAlias, multiple, storageFile);
                        };
                        tag = "input";
                        propType = "button";
                        propDataDojoWidget = "dijit/form/Button";
                        propDataDojoProps += ", label: \"Browse Globus Files...\"";
                        propNgClick = "browseFile(path, multiple)";
                    } else {
                        tag = "input";
                        propType = "file";
                        propDataDojoWidget = "dojox/form/Uploader";
                        var label;
                        if (!scope.multiple) {
                            label = "Browse...";
                        } else {
                            label = "Browse to Add...";
                        }
                        propDataDojoProps += ", label: \"" + label + "\", name: \"{path}\", multiple: {multiple}";
                    }
                    propNgModel = "";
                    propNgBind = "{ngModel}";
                } else if (currentTermType === "attachTo") {
                    context.nameField = currentTerm["name-field"];
                    tag = "select";
                    var idFieldValidation = currentTerm["id-field-validation"];
                    propType = idFieldValidation ? idFieldValidation.type : 'text';
                    propDataDojoWidget = "dijit/form/FilteringSelect";
                    propDataDojoProps += ", searchAttr: \"{nameField}\", labelAttr: \"{nameField}\"";
                    propDataDojoProps += ", placeHolder: \"Choose a value...\"";
                    
                    if (scope.term["name-field-validation"].type === 'numeric') {
                        propDataDojoProps += ", queryExpr: \"{queryExpr}\"";
                        context.queryExpr = "${0}";
                    }
                    propDataDojoStore = "store";
                } else {
                    // everything else is treated like text
                    tag = "input";
                    propType = "text";
                    propDataDojoWidget = "dijit/form/ValidationTextBox";
                }

                context.tag = tag;
                context.propType = propType;
                context.propDataDojoWidget = propDataDojoWidget;
                context.propDataDojoProps = propDataDojoProps;
                context.propDataDojoStore = propDataDojoStore;
                context.propNgModel = propNgModel;
                context.propNgBind = propNgBind;
                context.propNgClick = propNgClick;
                var htmlTemplate;
                if (tag === "input") {
                    // input
                    htmlTemplate = "<input";
                } else {
                    // select
                    htmlTemplate = "<select";
                }

                htmlTemplate += " type='{propType}' data-dojo-widget='{propDataDojoWidget}' data-dojo-props='" + propDataDojoProps + "'" + " show='{show}'";
                if (propDataDojoStore) {
                    htmlTemplate += " data-dojo-store='store'";
                }
                if (propNgBind) {
                    htmlTemplate += " ng-bind='{ngModel}'";
                } else {
                    htmlTemplate += " ng-model='{ngModel}'";
                }

                if (context.propNgClick) {
                    // globus
                    htmlTemplate += " ng-click='{propNgClick}'";
                }

                if (tag === "input") {
                    // input
                    htmlTemplate += "/>";
                } else {
                    // select and etc.
                    htmlTemplate += "></" + tag + ">";
                }
                if (context.propNgClick) {
                    // globus
                    var fileLinks = buildGlobusDownloadLinks(scope.dataset);
                    htmlTemplate += "<span>&nbsp;(Existing File(s): " + (fileLinks === null ? "None" : fileLinks) + ")</span>";
                }
                var html = "<span>" + dojo.replace(htmlTemplate, context) + "</span>";
                element.append(html);

                if (currentTerm.unit) {
                    element.append("<span>&nbsp;" + currentTerm.unit + "</span>");
                }

                // error message
                element.append("<span class='error' ng-show=\"message[''].valid === false\" ng-bind-template=\"&nbsp;* {{message[''].errorList[0].errorMessage}}\"></span>"); 

                if (!scope.readOnly) {
                    // TODO: all nodes: need validation and communicate error messages
                    //element.append("<-- validation and message -->");
                } else {
                    // read only: not much to do
                }

                if (currentTermType === "file") {
                    // list existing file(s)
                    if (!scope.multiple) {
                        // element.append("Existing File: ");
                        element.append("<cris-storage-file item='dataset' removable='removable'></cris-storage-file>");
                    } else {
                        //element.append("Existing File(s):<br/>");
                        element.append("<cris-storage-files items='dataset' removable='removable'></cris-storage-files>");
                    }
                }

                $compile(element.contents())(scope);

                scope.$on("$destroy", function() {
                    // cleanup
                });

                element.on("$destroy", function() {
                    // cleanup
                });
            });
        },
        controller: ["$scope", "datasetService", function($scope, datasetService) {
            $scope.onValueChange = function() {
                alert("value changed");
            };
            $scope.isReadOnly = datasetService.isReadOnly.bind($scope);
            $scope.isRequired = datasetService.isRequired.bind($scope);
        }]
    };
});

/**********************************************************
 * XML to JSON
 **********************************************************/
/**
 * Check if the node is a leaf node
 * @param {type} node
 * @returns {Boolean}
 */
function isLeafNode(node) {
    var terms = node.getElementsByTagName("term");
    var attachTos = node.getElementsByTagName("attachTo");
    if (terms.length === 0 && attachTos.length === 0) {
        return true;
    } else {
        return false;
    }
}

/**
 * Return the value of the attribute contained in field in term
 * @param {type} term
 * @param {type} field
 * @returns {Array}
 */
function getAttribute(term, field) {
    var value;

    switch (field) {
    case "required":
    case "read-only":
    case "list":
        // from attributes: boolean
        var v = term.getAttribute(field);
        if (v && v === "true") {
            value = true;
        } else {
            value = false;
        }
        break;
    case "uuid":
    case "version":
    case "versionName":
    case "alias":
    case "id-field":
    case "name-field":
    case "use-alias":
    case "required-expression":
    case "read-only-expression":
        // from attributes: string
        value = term.getAttribute(field);
        break;
    case "ui-display-order":
        // from attributes: number
        value = +term.getAttribute(field);
        break;
    case "name":
    case "description":
    case "type":
    case "unit":
    case "scale":
    case "length":
    case "value":
    case "show-expression":
    case "query":
        // from string elements: only a single value is expected
        var nodes = dojo.query("> " + field, term);
        if (nodes.length > 0 && nodes[0].firstChild) {
            value = nodes[0].firstChild.nodeValue;
        } else {
            value = null;
        }
        break;
    case "grid":
        // from boolean elements: only a single value is expected
        var nodes = dojo.query("> " + field, term);
        if (nodes.length > 0 && nodes[0].firstChild) {
            v = nodes[0].firstChild.nodeValue;
        } else {
            v = null;
        }
        if (v && v === "true") {
            value = true;
        } else {
            value = false;
        }
        break;
    case "validation":
        // from an element: a nested structure
        value = [];
        var nodesValidation = dojo.query("> " + field, term);
        if (nodesValidation.length > 0) {
            var nodesValidator = nodesValidation[0].getElementsByTagName("validator");
            for (var i = 0; i < nodesValidator.length; i++) {
                var type = nodesValidator.item(i).getAttribute("type");
                var nodesProperty =  nodesValidator.item(i).getElementsByTagName("property");
                var properties = [];
                for (var j = 0; j < nodesProperty.length; j++) {
                    if (nodesProperty.item(j).firstChild) {
                        // only process non-empty properties
                        var property = {};
                        property["id"] = nodesProperty.item(j).getAttribute("id");
                        property[nodesProperty.item(j).getAttribute("name")] = nodesProperty.item(j).firstChild.nodeValue;
                        properties.push(property);
                    }
                }
                value.push({type : type, properties : properties});
            }
        }
        break;
    default:
        value = null;
    }

    return value;
}

function getAlias(term) {
    var alias = getAttribute(term, "alias");

    if (!alias) {
        alias = getAttribute(term, "use-alias");
        if (!alias) {
            alias = getAttribute(term, "name");
        }
    }

    return alias;
}

function countSubTerms(term) {
    var count = 0;
    for (var i = 0; i < term.childNodes.length; i++) {
        var node = term.childNodes[i];
        if (node.nodeName === "term" || node.nodeName === "attach-to") {
            count++;
        }
    }
    return count;
}

/**
 * Get the type of a term
 * @param {type} term
 * @returns {string} the type of term
 */
function getTermType(term) {
    var type;

    var validation = getAttribute(term, "validation");
    if (validation && validation.length > 0) {
        type = validation[0].type;
        if (type === "date-time") {
            // whether it is a date or time is determined by the "format" property of "date-time" type
            var properties = validation[0].properties;
            for (var i in properties) {
                if (properties[i].format) {
                    type = properties[i].format;
                    break;
                }
            }
        }
    }

    if (!type) {
        type = getAttribute(term, "type");
    }

    return type;
}

/**
 * Convert the XML version of template to JSON
 * @param {type} xml
 * @returns {convertTermToJson.definition}
 */
function convertXmlToJson(xml) {
    var dom = dojox.xml.parser.parse(xml);
    var docNode = dom.documentElement;
    var defaultValue;
    var nestLevel = 0;
    var path = getAttribute(docNode, "uuid");
    return convertTermToJson(docNode, defaultValue, path, nestLevel);
}

/**
 * Convert the DOM of XML term to JSON with the given nestLevel
 * @param {type} term
 * @param {type} nestLevel
 * @returns {convertTermToJson.definition}
 */
function convertTermToJson(term, defaultValue, path, nestLevel) {
    // process the term itself
    var definition = processTerm(term, defaultValue, path, nestLevel);

    var subTerms = countSubTerms(term);
    if (subTerms !== 0) {
        // process default value;
        var value = definition["value"];
        var jsonValue;
        if (value) {
            jsonValue = dojo.fromJson(value);
        } else {
            jsonValue = null;
        }

        // elements: nested term/attachTo
        var terms = [];
        for (var i = 0; i < term.childNodes.length; i++) {
            var node = term.childNodes[i];

            if (node.nodeName === "term" || node.nodeName === "attach-to") {
                var alias = getAlias(node);
                if (jsonValue) {
                    defaultValue = dojo.toJson(jsonValue[alias]);
                }

                var def = null;
                var newPath = path + "." + getAlias(node);
                if (node.nodeName === "term") {
                    def = convertTermToJson(node, defaultValue, newPath, nestLevel + 1);
                } else if (node.nodeName === "attach-to") {
                    def = convertAttachToToJson(node, defaultValue, newPath, nestLevel + 1);
                }

                if (def) {
                    terms.push(def);
                }
            }
        }

        if (terms.length !== 0) {
            // sort the terms according to display order.
            require([
                "dojo/store/Memory"
            ], function(Memory) {
                var store = new Memory({data: terms});
                var sortedTerms = store.query(null, {sort: [{attribute: "ui-display-order", descending: false}]});

                definition["terms"] = sortedTerms;
                definition["isDefinition"] = false;
            });
        }
    }

    return definition;
}

/**
 * Convert the AttachTo term to JSON at given level
 * @param {type} term
 * @param {type} path
 * @param {type} nestLevel
 * @returns {unresolved}
 */
function convertAttachToToJson(term, defaultValue, path, nestLevel) {
    return processTerm(term, defaultValue, path, nestLevel);
}

/**
 * Process the term itself at given level excluding any nested terms
 * @param {type} term
 * @param {type} path
 * @param {type} nestLevel
 * @returns {processTerm.definition}
 */
function processTerm(term, defaultValue, path, nestLevel) {
    var definition = {};

    var type;
    if (term.nodeName === "attach-to") {
        type = "attachTo";
    } else {
        type = getTermType(term);
    }
    var validation = getAttribute(term, "validation");

    definition["nestLevel"] = nestLevel;
    definition["isDefinition"] = true;

    definition["uuid"] = getAttribute(term, "uuid");
    definition["version"] = getAttribute(term, "version");
    definition["versionName"] = getAttribute(term, "versionName");
    definition["alias"] = getAttribute(term, "alias");
    definition["path"] = path;
    definition["required"] = getAttribute(term, "required");
    definition["required-expression"] = getAttribute(term, "required-expression");
    definition["read-only"] = getAttribute(term, "read-only");
    definition["read-only-expression"] = getAttribute(term, "read-only-expression");
    definition["list"] = getAttribute(term, "list");
    definition["query"] = getAttribute(term, "query");
    definition["id-field"] = getAttribute(term, "id-field");
    definition["name-field"] = getAttribute(term, "name-field");
    definition["use-alias"] = getAttribute(term, "use-alias");
    definition["name"] = getAttribute(term, "name");
    definition["description"] = getAttribute(term, "description");
    definition["type"] = type;
    definition["unit"] = getAttribute(term, "unit");
    definition["scale"] = getAttribute(term, "scale");
    definition["length"] = getAttribute(term, "length");
    definition["validation"] = validation;
    if (typeof defaultValue === "undefined") {
        definition["value"] = getAttribute(term, "value");
    } else {
        definition["value"] = defaultValue;
    }
    definition["showExpression"] = getAttribute(term, "show-expression");
    definition["ui-display-order"] = getAttribute(term, "ui-display-order");
    definition["grid"] = getAttribute(term, "grid");

    // fix alias
    var alias;
    if (definition["alias"]) {
        alias = definition["alias"];
    } else if (definition["use-alias"]) {
        alias = definition["use-alias"];
    } else {
        alias = definition["name"];
    }
    definition["alias"] = alias;

    var properties = {};
    if (type === "list") {
        // build item list
        var items = [];
        var isMultiSelect = false;

        if (validation && validation.length > 0 && validation[0].type === "list") {
            var props = validation[0].properties;
            angular.forEach(props, function(property, k) {
                if (property.item !== undefined) {
                    items.push({id: property.id ? property.id : property.item, name: property.item});
                } else if (property.isMultiSelect === "true") {
                    isMultiSelect = true;
                }
            });
        }

        properties["items"] = items;
        properties["isMultiSelect"] = isMultiSelect;
    } else if (type === "attachTo") {
        var idField = definition["id-field"];
        var nameField = definition["name-field"];
        var idTerm, nameTerm;
        var attachToTemplate = getLatestTerm(definition.uuid);
        attachToTemplate = attachToTemplate.term;
        for (var i in attachToTemplate) {
            if (attachToTemplate[i].alias === idField || attachToTemplate[i].name === idField) {
                idTerm = attachToTemplate[i];
            }
            if (attachToTemplate[i].alias === nameField || attachToTemplate[i].name === nameField) {
                nameTerm = attachToTemplate[i];
            }
        }
        definition["id-field-validation"] = idTerm.validation.validator[0];
        definition["name-field-validation"] = nameTerm.validation.validator[0];
    } else {
        // for everything else
        if (validation && validation.length > 0) {
            var props = validation[0].properties;
            angular.forEach(props, function(property) {
                angular.forEach(property, function(v, k) {
                    properties[k] = v;
                });
            });
        }
    }

    definition["properties"] = properties;

    return definition;
}

/***********************************************************************
 * Globus
 ***********************************************************************/
function doGlobusDialog(termAlias, multiple, storageFile) {
    // termAlias and multiple: needed to put file(s) into cris
    // storageFile: needed to get a file from cris

    var url = cris.baseUrl + "globus/browseFile";
    if (cris && cris.job && cris.job.task && cris.job.task.app && cris.job.task.app.jobId) {
        url += "?jobId=" + cris.job.task.app.jobId;
        url += "&alias=" + termAlias;
    } else {
        url += "?alias=" + termAlias;
    }
    if (multiple === false) {
        url += "&multiple=false";
    } else {
        url += "&multiple=true";
    }
    if (storageFile) {
        url += "&storageFile=" + storageFile;
    }

    var xhrArgs = {
        url: url,
        headers : {Accept: "application/json"},
        handleAs: "text"
    };

    var deferred = dojo.xhrGet(xhrArgs);
    deferred.then(
        function(data) {
            dojo.require("dijit/layout/BorderContainer");
            dojo.require("dijit/layout/ContentPane");

            var content = dojo.create("iframe", {
                "src": data, //cris.job.task.app.jobId,
                "style": "width: 100%; height: 100%"
            });

            var dialog = new dijit.Dialog({
                title: "Globus File Transfer...",
                content: content,
                style: "width: 1000px; height: 600px;"
            });

            dialog.show();
        },
        function (error) {
            showMessage("An unexpected error occurred: " + error);
        }
    );

}
