/* global cris, dojo, dijit, dojox, gridx */

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
        if (type === "file" && result !== null && term.properties.multiple === "true" && typeof result === 'string') {
            // Default value for multiple files is comma separated string.
            result = result.split(',');
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

    var info = term.description || term.alias || term.name;
    info = info.replace('"', '&#34').replace("'", "&#39;");
    var html = "<span uib-popover='" + info + "' popover-trigger='mouseenter' popover-append-to-body='true' popover-placement='auto left-top'>{{prettyPrint(term.alias, '_')}}</span>";
    html += "<span style='color:red;font-weight:bold;' ng-show='isRequired()'>&nbsp;*</span>";

    var iconLick = generateIconLinks(scope.actions, scope, true, false, false);
    html += iconLick;

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

angular.module("dataset", ['angular-dojo', 'ui.grid', 'ui.grid.autoResize', 'ui.grid.selection']);

// Service provides methods that controllers share
angular.module("dataset").factory('datasetService', ['$parse', '$http', '$uibModal', function($parse, $http, $uibModal) {
    var storageFileMap = {};
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
            showYesNoModal({
                message: 'Do you want to remove item: ' + index + '?',
                okCallback: function(){
                    _this.dataset.splice(index, 1);
                },
                uibModal: $uibModal
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
            if (this.overrideHidden) {
                return true;
            }

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
        },
        fetchStorageFileName: function (storageFile) {
            if (storageFileMap[storageFile]) {
                return storageFileMap[storageFile];
            } else {
                var fileId = storageFile.substring(storageFile.indexOf(':') + 1, storageFile.length);
                var fileName = storageFile;
                if (typeof fileId !== 'undefined') {
                    fileName = storageFileMap[storageFile] = $http({
                                    method: 'GET',
                                    url: cris.baseUrl + "storagefiles/" + fileId
                                });
                }
                return fileName;
            }
        },
        fetchStorageFileNames: function (storageFiles) {
            var result = {};
            for (var i = 0; i < storageFiles.length; i++) {
                var storageFile = storageFiles[i];
                if (storageFileMap[storageFile]) {
                    result[storageFile] = storageFileMap[storageFile];
                } else {
                    var fileId = storageFile.substring(storageFile.indexOf(':') + 1, storageFile.length);
                    if (typeof fileId !== 'undefined') {
                        result[storageFile] = storageFileMap[storageFile] = $http({
                            method: 'GET',
                            url: cris.baseUrl + "storagefiles/" + fileId
                        });
                    }
                }
            }
            return result;
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
            key: "=",
            isGridContent: "@",
            overrideReadOnly: "=",
            overrideHidden: "="
        },
        template: "",
        link: function (scope, element, attrs) {
            console.log("==== crisDataset ====");

            if (typeof scope.key !== 'undefined') { // From member directive or grid cell
                var isList = scope.term["list"] ? true : false;
                var isNode = scope.term["isDefinition"] ? true : false;
                var template = "";
                if ((scope.term.grid === true && !isNode) || (scope.term.grid === true && isList)) {
                    var termLabel = makeTermLabel(scope.term, scope);
                    if (!scope.isGridContent) {
                        template = "<div class='form-group row form-horizontal' data-ng-show='isShow()'> \
                                        <label class='control-label col-md-2 col-lg-1'>" + termLabel + "</label>  \
                                        <div class='col-md-10 col-lg-11'> \
                                            <cris-grid term='term' dataset='dataset' message='message' context='context' path='{{path}}' read-only='readOnly' override-hidden='overrideHidden' override-read-only='overrideReadOnly'><!----></cris-grid> \
                                        </div> \
                                    </div>";
                    } else { // Widget in grid cell doesn't need a label
                        template = "<div data-ng-show='isShow()'><cris-grid term='term' dataset='dataset' message='message' context='context' path='{{path}}' read-only='readOnly' override-hidden='overrideHidden' override-read-only='overrideReadOnly'><!----></cris-grid></div>";
                    }
                } else {
                    template = "<collection term='term' dataset='dataset' message='message' context='context' read-only='readOnly' show='{{show}}' actions='actions' path='{{path}}' is-grid-content='{{isGridContent}}' override-hidden='overrideHidden' override-read-only='overrideReadOnly'><!----></collection>";
                }
                element.append(template);
                $compile(element.contents())(scope);
            } else { // From top-level/index page
                var scopeClone = null;
                var content = null;
                scope.$watchCollection('term', function () {
                    if (content) {
                        content.remove();
                        content = null;

                        scopeClone.$destroy();
                        scopeClone = null;
                    }

                    var template = "";
                    if (scope.term && scope.term.grid === true) {
                        template = "<cris-grid term='term' dataset='dataset' message='message' context='context' path='{{path}}' read-only='readOnly' show='{{show}}' override-hidden='overrideHidden' override-read-only='overrideReadOnly'><!----></cris-grid>";
                    } else if (scope.term && scope.term.grid === false) {
                        template = "<collection term='term' dataset='dataset' message='message' context='context' read-only='readOnly' show='{{show}}' actions='actions' path='{{path}}' override-hidden='overrideHidden' override-read-only='overrideReadOnly'><!----></collection>";
                    }
                    scopeClone = scope.$new();
                    element.append(template);
                    content = $compile(element.contents())(scopeClone);
                });
            }
        },
        controller: ["$scope", "datasetService", function($scope, datasetService) {
            $scope.prettyPrint = datasetService.prettyPrint;
            $scope.isRequired = datasetService.isRequired.bind($scope);
            $scope.isShow = datasetService.isShow.bind($scope);
        }]
    };
});

angular.module("dataset").directive("crisGrid", ['$interval', '$timeout', function ($interval, $timeout) {
    return {
        restrict: "E",
        replace: true,
        scope: {
            term: '=',
            context: '=',
            dataset: '=',
            message: '=',
            show: "@",
            path: '@',
            readOnly: "=?",
            overrideReadOnly: "=",
            overrideHidden: "="
        },
        template: '<div> \
                        <div ng-hide="noErrorMsgs" class="text-danger">Highlighted cells have validation errors. Hover mouse over cell for details.</div> \
                        <div ng-if="term.list"> \
                            <div ng-hide="readOnly" style="padding:5px;width:100%;border:1px solid #d4d4d4;"><img class="inlineIcon" src="' + cris.imagesRoot + '/famfamfam_silk_icons_v013/icons/add.png" data-ng-click="addToDataset(isNode)"/>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<img class="inlineIcon" src="' + cris.imagesRoot + '/famfamfam_silk_icons_v013/icons/delete.png" data-ng-click="removeFromDataset()"/></div> \
                            <div ng-if="!isNode" ui-grid="gridConfig" ui-grid-auto-resize ng-attr-ui-grid-selection ng-style="{height: (gridHeight ? gridHeight : 50) + 40 + \'px\'}"></div> \
                            <div ng-if="isNode" ui-grid="gridConfig" ui-grid-auto-resize ng-attr-ui-grid-selection ng-style="{height: (gridHeight ? gridHeight : 50) + \'px\', width: gridConfig.columnDefs[0].minWidth + 40 + \'px\'}"></div> \
                        </div> \
                        <div ng-if="!term.list" ui-grid="gridConfig" ui-grid-auto-resize ng-style="{height: (gridHeight ? gridHeight : 50)+ 40 + \'px\'}"></div> \
                   </div>',
        link: function (scope, element, attrs) {
            var isList = scope.term && scope.term["list"] ? true : false;
            var isNode = scope.term && scope.term["isDefinition"] ? true : false;
            scope.isNode = isNode;
            scope.noErrorMsgs = true;
            scope.rowHeightsMap = {}; // Holds refs to each row heights. Note: we need this for managing row heights because by default angular ui-grid does not support variable row heights

            if (!scope.path) {
                scope.path = scope.term.uuid;
            }
            if (scope.path.endsWith(scope.term.alias) === false && scope.path !== scope.term.uuid) {
                scope.path = scope.path + '.' + scope.term.alias;
            }

            if (!scope.message) {
                scope.message = {};
            }

            var columnDefs = [];
            scope.gridConfig = {
                columnDefs: columnDefs,
                appScopeProvider: scope,
                enableColumnMenus: false,
                enableHorizontalScrollbar: isNode ? 0 : 1,
                enableVerticalScrollbar: 0,
                showHeader: isNode ? false : true,
                rowHeight: 30,
                excessColumns: 30, // num of columns to render outside of viewport before virtualization
                onRegisterApi: function (gridApi)  {
                    console.dir('******* GRID API *********')
                    console.dir(gridApi);
                    scope.gridApi = gridApi;
                },
                rowTemplate: '<span ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ng-class="{\'ui-grid-row-header-cell\': col.isRowHeader }" ng-style="{height: (grid.appScope.rowHeightsMap[rowRenderIndex] ? grid.appScope.rowHeightsMap[rowRenderIndex] : 30) + \'px\'}" ui-grid-cell></span>'
            }
            var columnHideTimeout = null;

            var term = scope.term;
            if (isList && isNode) { // Simple list grid
                scope.termCopy = angular.copy(term);
                scope.termCopy.list = false;
                scope.termCopy.grid = false;

                scope.initColumnDefs(scope.termCopy, null, columnDefs, true);

                if (!scope.dataset) {
                    scope.dataset = [];
                }
                scope.gridConfig.data = scope.dataset;
            } else { // Composite term
                if (!isList && !isNode) { // Non-list composite grid
                    for (var i = 0; i < term.terms.length; i++) {
                        scope.initColumnDefs(term.terms[i], i, columnDefs);
                    }

                    if (!scope.dataset) {
                        scope.dataset = {};
                    }
                    scope.gridConfig.data = [scope.dataset];

                    // Show Hide columns
                    columnHideTimeout = null;
                    scope.$watch('context', function () {
                        if (columnHideTimeout) {
                            $timeout.cancel(columnHideTimeout);
                            columnHideTimeout = null;
                        }
                        columnHideTimeout = $timeout(function ( ) {
                            var cellNodes = angular.element(element.find('div.ui-grid-row')[0]).find('> div > .ui-grid-cell > .ui-grid-cell-contents > cris-dataset').find('> collection > div, > div');
                            var cellNodeContainers = angular.element(element.find('div.ui-grid-row')[0]).find('> div > .ui-grid-cell');
                            var headerCells = element.find('div.ui-grid-header-cell-row > div.ui-grid-header-cell');

                            for (var i = 0; i < cellNodes.length; i++) {
                                var doHide = angular.element(cellNodes[i]).hasClass('ng-hide');
                                if (doHide) {
                                    headerCells[i].style.display = "none";
                                    cellNodeContainers[i].style.display = "none";
                                } else {
                                    headerCells[i].style.display = "";
                                    cellNodeContainers[i].style.display = "";
                                }
                            }
                        }, 300, false);
                    }, true);

                } else if (isList && !isNode) { // List composite grid
                    for (var i = 0; i < term.terms.length; i++) {
                        scope.initColumnDefs(term.terms[i], i, columnDefs, true);
                    }

                    if (!scope.dataset || !(scope.dataset instanceof Array)) {
                        scope.dataset = [];
                    }
                    scope.gridConfig.data = scope.dataset;

                    // Show Hide columns
                    columnHideTimeout = null;
                    scope.$watch('context', function () {
                        if (columnHideTimeout) {
                            $timeout.cancel(columnHideTimeout);
                            columnHideTimeout = null;
                        }
                        columnHideTimeout = $timeout(function ( ) {
                            if (scope.dataset instanceof Array) {
                                scope.showHideColumns();
                            }
                        }, 300, false);
                    }, true);
                }
            }

            // Adjust grid height when dataset size changes
            var resizeTimeout = null;
            scope.$watch('dataset.length', function () {
                if (resizeTimeout) {
                    $timeout.cancel(resizeTimeout);
                    resizeTimeout = null;
                }

                resizeTimeout = $timeout(function ( ) {
                    scope.gridHeightResize();
                }, 300, false);
            });

            // Reset msg indicator whenever message object changes
            scope.$watch('message', function(newValue, oldValue) {
                if (!angular.equals(newValue, oldValue)) {
                    scope.noErrorMsgs = true;
                }
            });

            // Clean up before scope destroy
            scope.$on('$destroy', function () {
                $timeout.cancel(columnHideTimeout);
                $timeout.cancel(resizeTimeout);
            });
        },
        controller: ["$scope", "$element", "$parse", "datasetService", function($scope, $element, $parse, datasetService) {

            $scope.initColumnDefs = function (term, termIndex, columnDefsArray, isList) {
                var columnDef = {};
                var termName = $scope.makeGridHeaderLabel(term); // prettified term name
                var rawTermName = (term.alias || term.name || term.useAlias); // term name as written in term definition
                var path = $scope.path;
                var isGridContent = true;
                var isNode = $scope.term && $scope.term["isDefinition"] ? true : false; // Grid is of single node (non-composite term)

                columnDef.name = termName;
                columnDef.field = termName;
                columnDef.rawTermName = rawTermName;
                columnDef.term = term;
                columnDef.minWidth = $scope.gridColumnWidth(term);
                columnDef.enableSorting = false;

                if (isList && isNode) { // node list grid
                    columnDef.cellTemplate = "<div class='ui-grid-cell-contents' title='{{grid.appScope.getTooltip(grid.appScope.message[rowRenderIndex], " + isNode + ")}}' style='overflow-y:auto;background-color:{{(grid.appScope.message[rowRenderIndex][\"\"].valid === false) ? \"#FFB2B2\" : \"transparent\"}}'><cris-dataset term='grid.appScope.termCopy' dataset='grid.appScope.dataset[rowRenderIndex]' message='grid.appScope.message[rowRenderIndex]' path='" + path + "[{{rowRenderIndex}}]' context='grid.appScope.context' is-grid-content='" + isGridContent + "' show='{{grid.appScope.show}}' read-only='grid.appScope.readOnly' override-hidden='grid.appScope.overrideHidden' override-read-only='grid.appScope.overrideReadOnly' key='1'></cris-dataset></div>";
                } else if (isList && !isNode) { // composite list grid
                    columnDef.cellTemplate = "<div class='ui-grid-cell-contents' title='{{grid.appScope.getTooltip(grid.appScope.message[rowRenderIndex]." + rawTermName + ", grid.appScope.isNodeTerm(grid.appScope.term.terms[" + termIndex + "]))}}' style='overflow-y:auto;background-color:{{(grid.appScope.message[rowRenderIndex]." + rawTermName + "[\"\"].valid === false) ? \"#FFB2B2\" : \"transparent\"}}'><cris-dataset term='grid.appScope.term.terms[" + termIndex + "]' dataset='grid.appScope.dataset[rowRenderIndex]." + rawTermName + "' message='grid.appScope.message[rowRenderIndex]." + rawTermName + "' path='" + path + "[{{rowRenderIndex}}]' context='grid.appScope.context' is-grid-content='" + isGridContent + "' show='{{grid.appScope.show}}' read-only='grid.appScope.readOnly' override-hidden='grid.appScope.overrideHidden' override-read-only='grid.appScope.overrideReadOnly' key='1'>{{grid.appScope.updateContext(\"" + path + "\",grid.appScope.context,rowRenderIndex)}}</cris-dataset></div>";
                } else { // non-composite grid
                    columnDef.cellTemplate = "<div class='ui-grid-cell-contents' title='{{grid.appScope.getTooltip(grid.appScope.message." + rawTermName + ", grid.appScope.isNodeTerm(grid.appScope.term.terms[" + termIndex + "]))}}' style='overflow-y:auto;background-color:{{(grid.appScope.message." + rawTermName + "[\"\"].valid === false) ? \"#FFB2B2\" : \"transparent\"}}'><cris-dataset term='grid.appScope.term.terms[" + termIndex + "]' dataset='grid.appScope.dataset." + rawTermName + "' message='grid.appScope.message." + rawTermName + "' path='" + path + "' context='grid.appScope.context' is-grid-content='" + isGridContent + "' show='{{grid.appScope.show}}' read-only='grid.appScope.readOnly' override-hidden='grid.appScope.overrideHidden' override-read-only='grid.appScope.overrideReadOnly' key='1'></cris-dataset></div>";
                }

                columnDef.headerCellTemplate = '<div ng-class="{ \'sortable\': sortable }" ui-grid-one-bind-aria-labelledby-grid="col.uid + \'-header-text \' + col.uid + \'-sortdir-text\'" aria-sort="{{col.sort.direction == asc ? \'ascending\' : ( col.sort.direction == desc ? \'descending\' : (!col.sort.direction ? \'none\' : \'other\'))}}"> \
                                                    <div role="button" tabindex="0" class="ui-grid-cell-contents ui-grid-header-cell-primary-focus" col-index="renderIndex" title="TOOLTIP"> \
                                                      <span class="ui-grid-header-cell-label" ui-grid-one-bind-id-grid="col.uid + \'-header-text\'"> {{ (grid.appScope.termCopy ? grid.appScope.makeGridHeaderLabel(grid.appScope.termCopy) : grid.appScope.makeGridHeaderLabel(grid.appScope.term.terms[' + termIndex + '])) CUSTOM_FILTERS }} <span ng-show="grid.appScope.termCopy ? grid.appScope.isRequired(grid.appScope.termCopy) :  grid.appScope.isRequired(grid.appScope.term.terms[' + termIndex + '])" class="text-danger">*</span> </span> \
                                                      <span ui-grid-one-bind-id-grid="col.uid + \'-sortdir-text\'" ui-grid-visible="col.sort.direction" aria-label="{{getSortDirectionAriaLabel()}}"> \
                                                            <i ng-class="{ \'ui-grid-icon-up-dir\': col.sort.direction == asc, \'ui-grid-icon-down-dir\': col.sort.direction == desc, \'ui-grid-icon-blank\': !col.sort.direction }" title="{{isSortPriorityVisible() ? i18n.headerCell.priority + \' \' + ( col.sort.priority + 1 )  : null}}" aria-hidden="true"> </i> \
                                                            <sub ui-grid-visible="isSortPriorityVisible()" class="ui-grid-sort-priority-number"> {{col.sort.priority + 1}} </sub> \
                                                      </span> \
                                                    </div> \
                                                    <div role="button" tabindex="0" ui-grid-one-bind-id-grid="col.uid + \'-menu-button\'" class="ui-grid-column-menu-button" ng-if="grid.options.enableColumnMenus && !col.isRowHeader  && col.colDef.enableColumnMenu !== false" ng-click="toggleMenu($event)" ng-class="{\'ui-grid-column-menu-button-last-col\': isLastCol}" ui-grid-one-bind-aria-label="i18n.headerCell.aria.columnMenuButtonLabel" aria-haspopup="true"> \
                                                      <i class="ui-grid-icon-angle-down" aria-hidden="true"> &nbsp; </i> \
                                                    </div> \
                                                    <div ui-grid-filter></div> \
                                                </div>';

                columnDefsArray.push(columnDef);
            };

            $scope.makeGridHeaderLabel = function (term) {
                var termName = (term.alias || term.name || term.useAlias);
                termName = prettyPrint(termName, '_');
                if (term.unit) {
                    termName += ' [' + term.unit + ']';
                }
                return termName;
            };

            $scope.isRequired = function (term) {
                return datasetService.isRequired.call({term: term, context: $scope.context});
            };

            $scope.isShow = function (term) {
                return datasetService.isShow.call({term: term, context: $scope.context});
            };

            $scope.addToDataset = function (isNode) {
                if (!isNode) {
                    datasetService.addItem.call({term: $scope.term, dataset: $scope.dataset});
                }  else {
                    $scope.dataset.push(null);
                }
            };

            $scope.removeFromDataset = function () {
                var selectedRows = $scope.gridApi.selection.getSelectedGridRows();
                while (selectedRows.length) {
                    var row = selectedRows.pop();
                    var idx = $scope.dataset.indexOf(row.entity);
                    $scope.dataset.splice(idx, 1);
                }
            };

            // Each list composite term needs to have a reference to it from the parent level. We use underscore notation for this.
            // E.g. For parentTerm.term1[2], reference would be parentTerm._term1
            $scope.updateContext = function (path, context, rowIndex) {
                var _path = path.substring(path.indexOf('.') + 1); // remove template id from term path

                if (_path.indexOf('.') === -1) { // top level of context
                    context['_' + _path] = context[_path][rowIndex];
                } else {
                    var t = _path.substring(_path.lastIndexOf('.') + 1, _path.length);
                    _path = _path.substring(0,_path.lastIndexOf('.'));
                    var subContext = $parse(_path)(context);

                    subContext['_' + t] = subContext[t][rowIndex];
                }
            };

            $scope.getTooltip = function (msgObj, isNode) {
                var msg = "";
                if (isNode && msgObj && msgObj[''] && msgObj[''].valid === false) {
                    msg = msgObj[''].errorList[0].errorMessage;
                }
                $scope.noErrorMsgs = $scope.noErrorMsgs && (msg === "");
                return msg;
            };

            $scope.isNodeTerm = function (term) {
                var isNode = term && term["isDefinition"] ? true : false;
                return isNode;
            };

            // Shows/Hides columns for a list composite grid. For show/hide of a non-list composite grid see grid definition in link section.
            $scope.showHideColumns = function () {
                var columnShowHideMap = {};
                var rowNum = $scope.gridConfig.data.length;

                // Get header cells
                var headerCells = $element.find('div.ui-grid-header-cell-row > div.ui-grid-header-cell');
                var b = [];
                var y = 1;
                while (y <= $scope.gridApi.grid.options.columnDefs.length) {
                    b.push(headerCells[y]);
                    y++;
                }
                headerCells = b;

                // if no data, only hide header nodes and exit function
                if ($scope.dataset.length === 0) {
                    for (var i = 0; i < $scope.term.terms.length; i++) {
                        var isShow = $scope.isShow($scope.term.terms[i]);
                        var headerNode = headerCells[i];
                        if (isShow) {
                            headerNode.style.display = "";
                        } else {
                            headerNode.style.display = "none";
                        }
                    }
                    return;
                }

                var dataCells = {};
                while (--rowNum >= 0) {
                    var dataNodes = [];
                    if ($scope.dataset instanceof Array) { // list composite grid
                        dataNodes = angular.element($element.find('div.ui-grid-render-container-body > div.ui-grid-viewport div.ui-grid-row')[rowNum]).find('div > .ui-grid-cell');
                    }

                    for (var i = 0; i < $scope.gridApi.grid.options.columnDefs.length; i++) {
                        var node = angular.element(dataNodes[i]).find('div > cris-dataset div')[0];
                        var doHide = angular.element(node).hasClass('ng-hide');
                        if (typeof columnShowHideMap[i] === 'undefined') {
                            columnShowHideMap[i] = doHide;
                        } else {
                            columnShowHideMap[i] = columnShowHideMap[i] && doHide;
                        }
                    }
                    dataCells[rowNum] = dataNodes;
                }
                //console.log('********* COLUMN SHOW-HIDE MAP')
                //console.dir(columnShowHideMap);

                // Set column display (block or none) based on whether all cells in column are hidden
                // Do this for header node and all cell nodes in that column
                for (var colIndex in columnShowHideMap) {
                    var headerNode = headerCells[colIndex];
                    if (columnShowHideMap[colIndex]) { // hide
                        for (var rowIndex in dataCells) {
                            var dataNode = dataCells[rowIndex][colIndex];
                            dataNode.style.display = "none";
                        }
                        headerNode.style.display = "none";
                    } else {
                        for (var rowIndex in dataCells) {
                            var dataNode = dataCells[rowIndex][colIndex];
                            dataNode.style.display = "";
                        }
                        headerNode.style.display = "";
                    }
                }
            };

            $scope.gridHeightResize = function () {
                // Resize rowHeight as cell content changes in size.
                // By default ui-grid does not support variable row height...all rows must be the same configured height.
                // To overcome this limitation we have to query the cell nodes, read their scrollHeights, and manually update each row height based on its tallest cell node
                var rowHeights = {};
                var rowNum = $scope.gridConfig.data.length;
                while (--rowNum >= 0) {
                    if (typeof rowHeights[rowNum] === 'undefined') {
                        rowHeights[rowNum] = {height: 0};
                    }

                    var colNum = $scope.gridConfig.columnDefs.length;
                    var cellNodes = [];
                    var rowNode = null;
                    // Query cell nodes in current row and get their scrollHeight. The highest scrollHeight will be the row height
                    if ($scope.dataset instanceof Array) { // list composite grid
                        //cellNodes = angular.element($element.find('div.grid' + $scope.gridApi.grid.id + ' > div.ui-grid-contents-wrapper > div.ui-grid-render-container-body > div.ui-grid-viewport > div.ui-grid-canvas > div.ui-grid-row')[rowNum]).find('> div cris-dataset > collection > div, div cris-dataset > div');
                        rowNode = $element.find('div.grid' + $scope.gridApi.grid.id + ' > div.ui-grid-contents-wrapper > div.ui-grid-render-container-body > div.ui-grid-viewport > div.ui-grid-canvas > div.ui-grid-row')[rowNum];
                        cellNodes = angular.element(rowNode).find('> div cris-dataset > collection > div, div cris-dataset > div');
                        var rowHeaderNode = $element.find('div.grid' + $scope.gridApi.grid.id + ' > div.ui-grid-contents-wrapper > div.ui-grid-pinned-container-left > div.ui-grid-render-container-left > div.ui-grid-viewport > div.ui-grid-canvas > div.ui-grid-row')[rowNum];
                        rowHeights[rowNum].nodes = [angular.element(rowNode), angular.element(rowHeaderNode)];
                    } else { // non-list composite grid
                        rowNode = $element.find('div.grid' + $scope.gridApi.grid.id + ' > div.ui-grid-contents-wrapper > div.ui-grid-render-container-body > div.ui-grid-viewport > div.ui-grid-canvas > div.ui-grid-row');
                        cellNodes = rowNode.find('> div > .ui-grid-cell > .ui-grid-cell-contents > cris-dataset').find('> collection > div, > div');
                        rowHeights[rowNum].nodes = [rowNode];
                    }

                    while(--colNum >= 0 && cellNodes.length) {
                        if (cellNodes[colNum]) {
                            var h = cellNodes[colNum].scrollHeight;
                            if (h > rowHeights[rowNum].height) {
                                rowHeights[rowNum].height = h;
                            }
                        }
                    }

                    // If node inner html increases or decreases, re-calculate row height and resize row and grid accordingly
                    (function () {
                        if (!$scope.rowNodeRefs) {
                            $scope.rowNodeRefs = {};
                        }
                        if (!$scope.rowNodeRefs[rowNum]) {
                            $scope.rowNodeRefs[rowNum] = rowHeights[rowNum].height;

                            var cellNodes1 = cellNodes;
                            var resizeTimeout = null;
                            $scope.$watch(function () {
                                var currentRowHeight = 0;
                                if (cellNodes1.length) {
                                    for (var y = 0; y < $scope.gridConfig.columnDefs.length; y++) {
                                        if (cellNodes1[y].scrollHeight > currentRowHeight) {
                                            currentRowHeight  = cellNodes1[y].scrollHeight;
                                        }
                                    }
                                }
                                return currentRowHeight;
                            }, function (newValue, oldValue) {
                                if (newValue !== oldValue && newValue !== 0) {
                                    if (resizeTimeout) {
                                        $timeout.cancel(resizeTimeout);
                                        resizeTimeout = null;
                                    }

                                    resizeTimeout = $timeout(function ( ) {
                                        $scope.gridHeightResize();
                                    }, 50, false);
                                }
                            });

                            $scope.$on('$destroy', function () {
                                $timeout.cancel(resizeTimeout);
                            });
                        }
                    })();
                }
                //console.log('--------------------------------ROW HEIGHT OBJECTS ');
                //console.dir(rowHeights);

                // The row node and all cell nodes inside it must have same height
                var gridHeight = 0;
                for (var rowIndex in rowHeights) {
                    var rowNodes = rowHeights[rowIndex].nodes;
                    var rowHeight = rowHeights[rowIndex].height + 15; // add offset for things like padding, margin, borders, etc
                    for (var t = 0; t < rowNodes.length; t++) {
                        // Set row node height
                        rowNodes[t][0].style.height = rowHeight + "px";

                        // Set height of each cell node in the row
                        rowNodes[t].find('> div[ui-grid-row="row"] > .ui-grid-cell').each(function (idx, node) {
                            node.style.height = rowHeight + "px";
                        });
                    }
                    gridHeight += rowHeight;
                    $scope.rowHeightsMap[rowIndex] = rowHeight;
                }

                // Update grid height if new height is greater than old height
                if (gridHeight !== $scope.maxGridHeight) {
                    $scope.gridHeight = gridHeight;
                    $scope.maxGridHeight = gridHeight;
                    $scope.$apply();
                }
            };

            // Estimate minWidth for each grid column
            $scope.gridColumnWidth = function getColWidth (term) {
                var pixelCount = 0;
                var termArray = [];
                termArray.push(term);

                while (termArray.length) {
                    var term_ = termArray.pop();
                    if (term_.terms && term_.grid) {
                        term_.terms.forEach(function (t) {
                            termArray.push(t);
                        });
                        pixelCount += 160; // Additional offset pixels if cell has grid
                    } else {
                        // Get sum of all leaf term pixels
                        if (term_.terms) { // term is composite but not grid
                            pixelCount += 310;

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
                                pixelCount = 80;
                                pixelCount += getColWidth(largestChild);
                            }
                        } else if (term_.grid || term_.list) { // term is leaf node and grid or term is node and list
                            pixelCount += 255;
                        } else { // leaf term is neither composite nor a grid
                            pixelCount += 200;
                        }
                    }
                }
                return pixelCount;
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
            actions: "=",
            isGridContent: "@",
            overrideReadOnly: "=",
            overrideHidden: "="
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
            var htmlRemoveButton = "";
            if (isList && !scope.readOnly) {
                htmlAddButton = makeAddButton(scope);
                htmlRemoveButton = makeRemoveButton(scope);

                // add a add button for list item
                //termLabel += ":&nbsp;" + htmlAddButton;
                termLabel = htmlAddButton + '&nbsp;' + termLabel;
                termLabel += "<span class='error' ng-show=\"message[''].valid === false\">&nbsp;* {{message[''].errorList[0].errorMessage ? 'required' : ''}}</span>";
            }

            var path = "path ? (path + \".\" + term.alias) : term.uuid";

            if (isNode) {
                // object(simple): {t1 : {}}
                if (!isList) {
                    // single node
                    console.log("0000 node 0000");
                    if (!scope.isGridContent) {
                        element.append("<div class='form-group row form-horizontal' data-ng-show='isShow()'> \
                                            <label class='control-label col-md-2 col-lg-1'>" + termLabel + "</label>\
                                            <div class='col-md-10 col-lg-11' style='max-width:400px;'> \
                                                <node term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context' override-read-only='overrideReadOnly'></node> \
                                            </div> \
                                        </div>");
                    } else {
                       element.append("<div data-ng-show='isShow()'><node term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context' is-grid-content='{{isGridContent}}' override-read-only='overrideReadOnly'></node></div>");
                    }
                } else {
                    // list node
                    console.log("1111 node 1111");
                    if (!scope.isGridContent) {
                        element.append("<div class='form-group row form-horizontal' data-ng-show='isShow()'> \
                                            <label class='control-label col-md-2 col-lg-1'>" + termLabel + "</label>  \
                                            <div class='col-md-10 col-lg-11'> \
                                                <ul class='list-group' style='max-width:400px;'><li class='list-group-item form-inline' ng-repeat='item in dataset track by $index'>{{$index}}:&nbsp;" + htmlRemoveButton + "&nbsp;<node term='term' dataset='dataset[$index]' message='message[$index]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}[{{$index}}]' context='context' override-read-only='overrideReadOnly'></node></li></ul> \
                                            </div> \
                                        </div>");
                    } else {
                        element.append("<div data-ng-show='isShow()'><label>" + termLabel + "</label><ul class='list-group' style='max-width:400px;'><li class='list-group-item form-inline' ng-repeat='item in dataset track by $index'>{{$index}}:&nbsp;" + htmlRemoveButton + "&nbsp;<node term='term' dataset='dataset[$index]' message='message[$index]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}[{{$index}}]' context='context' override-read-only='overrideReadOnly'></node></li></ul></div>");
                    }
                }
            } else {
                // object(composite): {..., terms: {t1 : {}, t2 : {}, ...}}
                if (!isList) {
                    // single member
                    console.log("2222 member 2222");
                    if (isEmpty(scope.context) || !scope.path) {
                        element.append("<member term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context' override-hidden='overrideHidden' override-read-only='overrideReadOnly'></member>");
                    } else {
                        if (!scope.isGridContent) {
                            element.append("<div class='form-group row form-horizontal' data-ng-show='isShow()'> \
                                                <label class='control-label col-md-2 col-lg-1'>" + termLabel + "</label>  \
                                                <div class='col-md-10 col-lg-11'> \
                                                    <ul class='list-group'><li class='list-group-item'><member term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context' override-hidden='overrideHidden' override-read-only='overrideReadOnly'></member></li></ul> \
                                                </div> \
                                            </div>");
                        } else {
                            element.append("<div data-ng-show='isShow()'><member term='term' dataset='dataset' message='message' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}' context='context' override-hidden='overrideHidden' override-read-only='overrideReadOnly'></member></div>");
                        }
                    }
                } else {
                    // list member
                    console.log("3333 node 3333");
                    if (!scope.isGridContent) {
                        element.append("<div class='form-group row form-horizontal' data-ng-show='isShow()'> \
                                            <label class='control-label col-md-2 col-lg-1'>" + termLabel + "</label>  \
                                            <div class='col-md-10 col-lg-11'> \
                                                <ul class='list-group'><li class='list-group-item' ng-repeat='item in dataset track by $index'>{{$index}}:&nbsp;" + htmlRemoveButton + "<member term='term' dataset='dataset[$index]' message='message[$index]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}[{{$index}}]' context='context' override-hidden='overrideHidden' override-read-only='overrideReadOnly'>{{updateContext(term,$index,context," + path + ")}}</member></li></ul> \
                                            </div> \
                                        </div>");
                    } else {
                        element.append("<div data-ng-show='isShow()'><label>" + termLabel + "</label><ul class='list-group'><li class='list-group-item' ng-repeat='item in dataset track by $index'>{{$index}}:&nbsp;" + htmlRemoveButton + "<member term='term' dataset='dataset[$index]' message='message[$index]' read-only='readOnly' show='{{isShow()}}' actions='actions' path='{{" + path + "}}[{{$index}}]' context='context' override-hidden='overrideHidden' override-read-only='overrideReadOnly'>{{updateContext(term,$index,context," + path + ")}}</member></li></ul></div>");
                    }
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
            actions: "=",
            overrideReadOnly: "=",
            overrideHidden: "="
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

            element.append("<cris-dataset ng-repeat='(key, definition) in term.terms' key='key' term='term.terms[key]' dataset='dataset[term.terms[key].alias]' message='message[term.terms[key].alias]' read-only='readOnly' show='{{show}}' actions='actions' path='{{path}}' context='context' override-hidden='overrideHidden' override-read-only='overrideReadOnly'></cris-dataset>");
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
            show: "@",
            isGridContent: "@",
            overrideReadOnly: "="
        },
        template: "",
        link: function(scope, element, attrs) {
            console.log("===== node =====");
            console.dir(scope);

            var currentTerm = scope.term;

            if (scope.readOnly === undefined) {
                scope.readOnly = false;
            }
            if (scope.show === undefined || scope.show === null || scope.show === "") {
                scope.show = "true";
            }
            scope.require = scope.isRequired();

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
                    scope.$watch('context', function(newValue){
                        var val
                        with(newValue) {
                            try {
                                val =  eval(expression);
                            } catch (e) {
                                val = null;
                            }
                        };

                        if (typeof (val) !== "undefined" && !(isNaN(val) && typeof val === "number")) {
                            scope.dataset = val;
                            scope.message = null;
                        } else {
                            var invalidValue = "At least one of the dependent term has invalid value: " + val;
                            scope.dataset = null;
                            scope.message = buildErrorMessage(invalidValue);
                        }
                    }, true);
                }
            }

            var tag, propNgReadonly, propNgRequired, propNgModel, propNgClick, buttonText;
            propNgReadonly = '!overrideReadOnly && (isReadOnly() || readOnly || ' + readOnly + ')';
            propNgRequired = 'isRequired() && show==\'true\'';
            propNgModel = 'dataset';

            var htmlTemplate;
            if (currentTermType === "boolean") {
                tag = "input";
                // No ng-required on a boolean because angular considers "false" as failing the required test. Should we have a 3-state (true, false, null) checkbox?
                htmlTemplate = '<input ng-model="dataset" type="checkbox" ng-disabled="' + propNgReadonly + '"';
            } else if (currentTermType === "numeric") {
                tag = "input";
                htmlTemplate = '<input ng-model="dataset" type="number" ng-disabled="' + propNgReadonly + '" ng-required="' + propNgRequired + '" class="form-control"';
            } else if (currentTermType === "text") {
                if (currentTerm.properties && currentTerm.properties["ui-vertical-lines"] > 1) {
                    tag = "textarea";
                    htmlTemplate = '<textarea rows="' + currentTerm.properties["ui-vertical-lines"] + '" ng-disabled="' + propNgReadonly + '" ng-required="' + propNgRequired + '" class="form-control"';
                } else {
                    tag = "input";
                    htmlTemplate = '<input type="text" ng-disabled="' + propNgReadonly + '" ng-required="' + propNgRequired + '" class="form-control"';
                }
                if (currentTerm.properties && currentTerm.properties.length) {
                    htmlTemplate += ' maxLength="' + currentTerm.properties.length + '"'
                }
            } else if (currentTermType === "textarea") {
                tag = "textarea";
                htmlTemplate = '<textarea ng-disabled="' + propNgReadonly + '" ng-required="' + propNgRequired + '" class="form-control"';
            } else if (currentTermType === "date") {
                tag = "cris-date-picker";
                htmlTemplate = '<cris-date-picker type="date" is-read-only="{{' + propNgReadonly + '}}" ng-required="' + propNgRequired + '"'
            } else if (currentTermType === "time") {
                tag = 'cris-time-picker';
                htmlTemplate = '<cris-time-picker is-read-only="{{' + propNgReadonly + '}}" ng-required="' + propNgRequired + '"'
            } else if (currentTermType === "date-time") {
                tag = "cris-date-picker";
                htmlTemplate = '<cris-date-picker type="datetime-local" is-read-only="{{' + propNgReadonly + '}}" ng-required="' + propNgRequired + '"'
            } else if (currentTermType === "list") {
                scope.listItems = [];
                if (typeof currentTerm.properties !== "undefined" && currentTerm.properties && currentTerm.properties.items) {
                    scope.listItems =  currentTerm.properties.items;
                }

                if (currentTerm.properties && currentTerm.properties.isMultiSelect) {
                    tag = 'cris-multi-select';
                    htmlTemplate = '<cris-multi-select items="listItems" is-read-only="{{' + propNgReadonly + '}}" ng-required="' + propNgRequired + '"';
                } else {
                    tag = 'cris-dropdown';
                    htmlTemplate = '<cris-dropdown items="{{listItems}}" is-read-only="{{' + propNgReadonly + '}}" ng-required="' + propNgRequired + '"';
                }
            } else if (currentTermType === "file") {
                var globus = false;
                if (currentTerm.properties && currentTerm.properties.globus === "true") {
                    globus = true;
                } else {
                    globus = false;
                }

                if (globus) {
                    scope.fileList = [];
                    scope.browseFile = function(path, multiple, storageFile, fileList) {
                        doGlobusDialog(path, multiple, storageFile, fileList, scope);
                    };
                    tag = "button";
                    buttonText = '<span class="glyphicon glyphicon-folder-open"></span>&nbsp; Browse Globus Files...';
                    propNgClick = 'browseFile(path, multiple, null, fileList)'
                    htmlTemplate = '<button type="button" class="btn btn-primary" ng-hide="' + propNgReadonly + '"';
                } else {
                    tag = 'cris-file-uploader'
                    if (currentTerm.properties && currentTerm.properties.multiple === "true") {
                        scope.multiple = true;
                    } else {
                        scope.multiple = false;
                    }
                    htmlTemplate = '<cris-file-uploader is-multiple="multiple" path="{{path}}" is-required="' + propNgRequired + '" ng-hide="' + propNgReadonly + '"';
                }
            } else if (currentTermType === "attachTo") {
                var idField = currentTerm["id-field"];
                var nameField = currentTerm["name-field"];
                var uuid = currentTerm.uuid;
                var url = cris.baseUrl + "rest/objectus/" + uuid;
                var query = currentTerm.query;
                scope.query = query;

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
                    scope.query = value;
                });

                tag = 'cris-url-dropdown';
                htmlTemplate = '<cris-url-dropdown url="' + url + '" id-field="' + idField + '" name-field="' + nameField + '" query="{{query}}" use-mongo-query="{{true}}" items="listItems" is-read-only="{{' + propNgReadonly + '}}" ng-required="' + propNgRequired + '"';
            } else {
                // everything else is treated like text
                tag = "input";
                htmlTemplate = '<input type="text" class="form-control" ng-disabled="' + propNgReadonly + '" ng-required="' + propNgRequired + '" ';
            }

            if (propNgModel) {
                htmlTemplate += ' ng-model="' + propNgModel + '"';
            }
            if (propNgClick) {
                htmlTemplate += ' ng-click="' + propNgClick + '"';
            }

            if (tag === "input") {
                // input
                htmlTemplate += "/>";
            } else {
                // select and etc.
                if (tag === "button" && buttonText) {
                    htmlTemplate += ">" + buttonText + "</" + tag + ">";
                } else {
                    htmlTemplate += "></" + tag + ">";
                }
            }

            if (currentTerm.unit && !scope.isGridContent) {
                htmlTemplate = "<div class='input-group'>" + htmlTemplate + "<div class='input-group-addon'>{{term.unit}}</div></div>";
            }

            element.append(htmlTemplate);

            // error message
            if (!scope.isGridContent) { // Grid content uses tooltips for error msgs. Therefore this is not necessary in grid cells.
                element.append("<span class='error' style='white-space:normal;' ng-show=\"message[''].valid === false\" ng-bind-template=\"&nbsp;* {{message[''].errorList[0].errorMessage}}\"></span>");
            }

            var globus = false;
            if (currentTermType === "file") {
                if (globus) {
                    // list existing file(s)
                    if (!scope.multiple) {
                        element.append("<div><cris-globus-file path=\"path\" file='dataset' removable='removable'></cris-globus-file></div>");
                    } else {
                        element.append("<div><cris-globus-files path=\"path\" files='dataset' removable='removable'></cris-globus-files></div>");
                    }

                    // render a list of files for globus file selection
                    element.append("<div data-ng-show='fileList.length !== 0'>to be replaced by:");
                    element.append("<ul><li data-ng-repeat='item in fileList'>{{item}}</li></ul>");
                    element.append("</div>");
                } else {
                    // list existing file(s)
                    if (!scope.multiple) {
                        // element.append("Existing File: ");
                        element.append("<div><cris-storage-file item='dataset' removable='removable' get-storage-file-name='getStorageFileName(storageFile)'></cris-storage-file></div>");
                    } else {
                        //element.append("Existing File(s):<br/>");
                        element.append("<div><cris-storage-files items='dataset' removable='removable' get-storage-file-names='getStorageFileNames(storageFiles)' read-only='" + propNgReadonly + "'></cris-storage-files></div>");
                    }
                }
            }

            $compile(element.contents())(scope);

            scope.$on("$destroy", function() {
                // cleanup
            });

            element.on("$destroy", function() {
                // cleanup
            });
        },
        controller: ["$scope", "datasetService", function($scope, datasetService) {
            $scope.onValueChange = function() {
                alert("value changed");
            };
            $scope.isReadOnly = datasetService.isReadOnly.bind($scope);
            $scope.isRequired = datasetService.isRequired.bind($scope);
            $scope.getStorageFileName = function (storageFile) {
                if (storageFile) {
                    return datasetService.fetchStorageFileName(storageFile);
                } else {
                    return storageFile;
                }
            };
            $scope.getStorageFileNames = function (storageFiles) {
                if (storageFiles) {
                    return datasetService.fetchStorageFileNames(storageFiles);
                } else {
                    return storageFiles;
                }
            };
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
    case "required-expression":
    case "read-only-expression":
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
function doGlobusDialog(path, multiple, storageFile, fileList, scope) {
    // path and multiple: needed to put file(s) into cris
    // storageFile: needed to get a file from cris
    var templateUuid;
    var alias;
    if (path) {
        var parts = path.split(".");
        if (parts.length) {
            templateUuid = parts.shift();
            alias = parts.join(".");
        }
    }

    var url = cris.baseUrl + "globus/browseFile";
    url += "?key=" +  (templateUuid ? templateUuid : "");
    url += "&alias=" + (alias ? alias : "");
    url += "&multiple=" + (multiple === false ? "false" : "true");
    url += "&storageFile=" + (storageFile ? storageFile : "");;

    dojo.xhrGet({
        url: url,
        headers : {Accept: "text/plain"},
        handleAs: "text"
    }).then(
        function(data, ioargs) {
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

            dojo.connect(dialog, "onCancel", function(evt) {
                console.dir(evt);
                dojo.xhrGet({
                    url: cris.baseUrl + "globus/getStorageFiles?key=" + templateUuid + "&alias=" + alias,
                    handleAs: "json"
                }).then(
                    function(data, ioargs) {
                        console.dir(data);
                        if (fileList) {
                            fileList.splice(0, fileList.length);
                            for (var index in data) {
                                fileList.push(data[index]);
                            }
                        }
                        scope.$apply();
                    },
                    function (error, ioargs) {
                    }
                );
            });

            dialog.show();
        },
        function (error, ioargs) {
            showMessage("An unexpected error occurred: " + error);
        }
    );

}