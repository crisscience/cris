/* global cris, dojo, dijit, dojox */

require([
    "dojox/uuid/generateRandomUuid"
]);

if (!String.prototype.trim) {
    String.prototype.trim = function () {
        return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    };
}

angular.module('angular-dojo', ['ui.bootstrap', 'ui.grid', 'ui.grid.autoResize', 'ui.grid.selection', 'ui.grid.pagination', 'ngSanitize', 'ui.grid.resizeColumns']);

angular.module('angular-dojo').directive('crisContextmenu', function($parse) {
    return function(scope, element, attrs) {
        var fn = $parse(attrs.crisContextmenu);
        element.bind('contextmenu', function(evt) {
            scope.$apply(function() {
                fn(scope, {evt: evt});
                evt.preventDefault();
            });
        });
    };
});

angular.module('angular-dojo').directive('crisDndsourceover', function($parse) {
    return function(scope, element, attrs) {
        var fn = $parse(attrs.crisDndsourceover);
        dojo.subscribe("/dnd/source/over", function(source) {
            scope.$apply(function() {
                fn(scope, {source: source});
            });
        });
    };
});

angular.module('angular-dojo').directive('crisDndstart', function($parse) {
    return function(scope, element, attrs) {
        var fn = $parse(attrs.crisDndstart);
        dojo.subscribe("/dnd/start", function(source, nodes, copy) {
            console.log("/dnd/start", source, nodes, copy);
            scope.$apply(function() {
                fn(scope, {source: source, nodes: nodes, copy: copy});
            });
        });
    };
});

angular.module('angular-dojo').directive('crisDnddropbefore', function($parse) {
    return function(scope, element, attrs) {
        var fn = $parse(attrs.crisDnddropbefore);
        dojo.subscribe("/dnd/drop/before", function(source, nodes, copy, target) {
            console.log("/dnd/drop/before", source, nodes, copy, target);
            scope.$apply(function() {
                fn(scope, {source: source, nodes: nodes, copy: copy, target: target});
            });
        });
    };
});

angular.module('angular-dojo').directive('crisDnddrop', function($parse) {
    return function(scope, element, attrs) {
        var fn = $parse(attrs.crisDnddrop);
        dojo.subscribe("/dnd/drop", function(source, nodes, copy, target) {
            scope.$apply(function() {
                fn(scope, {source: source, nodes: nodes, copy: copy, target: target});
            });
        });
    };
});

angular.module('angular-dojo').directive('crisDndcancel', function($parse) {
    return function(scope, element, attrs) {
        var fn = $parse(attrs.crisDndcancel);
        dojo.subscribe("/dnd/cancel", function() {
            console.log("/dnd/cancel");
            scope.$apply(function() {
                fn(scope);
            });
        });
    };
});

angular.module('angular-dojo').directive('dojoWidget', ["$compile", "$parse", function($compile, $parse) {
    function parseProps(props) {
        var result = {};
        if (props) {
            result = eval("[{" + props + "}]")[0];
        }
        return result;
    }

    return {
        restrict: 'A',
        require: '?ngModel',
        scope: {
            'ngModel': '=',
            'ngBind': '=',
            'ngClick': '&',
            'ngChange': '&',
            'ngBlur': '&',
            'dojoStore': '&',
            'dojoProps': '@',
            'show': '@'
        },
        link: function (scope, element, attrs) {
            var elem = element[0];

            require(["dijit/dijit", attrs.dojoWidget, "dijit/form/ValidationTextBox", "dojox/form/uploader/FileList", "dijit/TitlePane", "dojo/on"], function(dijit, DojoWidget, ValidationTextBox, FileList, TitlePane, on) {
                /***********************************
                 * create and initialize the widget
                 ***********************************/
                var dojoProps = {};
                if (scope.dojoProps) {
                    dojoProps = parseProps(scope.dojoProps);
                }

                if (attrs.id) {
                    dojoProps.id = attrs.id;
                }

                if (scope.dojoStore) {
                    dojoProps.store = scope.dojoStore();
                }

                var value;
                if ((scope.ngModel === undefined) || (scope.ngModel !== scope.ngModel)) {
                    value = null;
                } else {
                    value = scope.ngModel;
                }

                if (attrs.type === "file") {
                    var existingFileList = scope.ngBind;
                } else {
                    dojoProps.value = value;
                }

                scope.widget = new DojoWidget(dojoProps, elem);
                scope.widget.startup();

                if (scope.ngBlur) {
                    scope.widget.onBlur = scope.ngBlur;
                }

                // need to remember the original value of required
                var required = scope.widget.get('required');
                scope.required = (required === undefined || required === null ? false : required);

                if (attrs.type === "file") {
                    var parentNode = scope.widget.domNode.parentNode;

                    // create a widget to display selected file name(s)
                    var uploaderId = scope.widget.id;
                    var file = dojo.create("div", {}, parentNode);
                    scope.widgetFileList = new FileList({uploaderId : uploaderId}, file);

                    // Hide widget file details i.e. size, index, etc.
                    dojo.query('.dojoxUploaderFileListTable th', scope.widgetFileList.domNode).forEach(function(node){
                        dojo.setStyle(node, 'display', 'none');
                    });
                    dojo.addClass(scope.widgetFileList.domNode, 'storageFileContainer');
                } else {
                    scope.widgetFileList = null;

                    if (attrs.type === "checkbox") {
                        // extra setting for checkbox
                        scope.widget.set("checked", value);
                    }
                }

                /****************
                 * view -> model
                 ****************/
                on(scope.widget, "change", function (value) {
                    if (attrs.type === "file") {
                        // file selection goes only one way: view -> model
                        // but the model for file upload is managed separately so so nothing here

                        // Only display filename on upload instead of other attributes (size, icon, index)
                        dojo.query('.dojoxUploaderIndex, .dojoxUploaderIcon, .dojoxUploaderSize', scope.widgetFileList.domNode).forEach(function(node) {
                            dojo.setStyle(node, 'display', 'none');
                        });

                        dojo.query('.dojoxUploaderFileName', scope.widgetFileList.domNode).forEach(function(node) {
                            dojo.place('<img class="inlineIcon" src="' + cris.imagesRoot + '/famfamfam_silk_icons_v013/icons/tick.png" />&nbsp;', node, 'first');
                        });
                    } else {
                        //if (scope.widget.getValue) {
                            if ((value === undefined) || (value !== value) || (typeof value === 'string' && value.trim() === "")) {
                                scope.ngModel = null;
                            } else {
                                var v;
                                switch (attrs.type) {
                                    case "boolean":
                                        v = value;
                                        break;
                                    case "numeric":
                                        v = +value;
                                        break;
                                    case "date":
                                    case "time":
                                    case "date-time":
                                        v = value;
                                        break;
                                    case "text":
                                    case "textarea":
                                    case "file":
                                    default:
                                        v = value;
                                        break;
                                }
                                scope.ngModel = v;
                            }
                        //}
                        scope.$apply();
                    }
                });

                /*
                 *  do not handle the click event for
                 *      FilteringSelect
                 */
                if (attrs.hasOwnProperty('ngClick')) {
                    on(scope.widget, 'click', function () {
                        scope.ngClick();
                        scope.$apply();
                    });
                }

                /****************
                 * model -> view
                 ****************/
                scope.$watch('ngModel', function (value) {
                    if (attrs.type === "file") {
                        // file selection goes only one way: view -> model
                        // so do nothing here
                    } else {
                        // make sure that the widget has a getValue() function
                        if (scope.widget.getValue) {
                            if ((value === undefined) || (value !== value)) {
                                if (attrs.dojoWidget === "dijit/form/FilteringSelect") {
                                    scope.widget.set('value', " ");
                                } else {
                                    scope.widget.set('value', null);
                                }
                            } else {
                                scope.widget.set('value', value);
                            }
                        }
                    }
                });

                scope.$watch("show", function(value) {
                    var show = (value === 'true' ? true : false);
                    scope.widget.set('required', scope.required && show);
                });

                attrs.$observe("dojoProps", function(newValue, oldValue) {
                    var dojoPropsNew = parseProps(newValue);
                    var dojoPropsOld = parseProps(oldValue);

                    if (dojoPropsNew.disabled === true) {
                        scope.widget.set('disabled', true);
                        if (scope.widget.id.indexOf('form_Uploader') >= 0) { // If File term is readonly hide browse button
                            dojo.setStyle(scope.widget.domNode, 'visibility', 'hidden');
                        }
                    } else if (dojoPropsNew.disabled === false) {
                        scope.widget.set('disabled', false);
                        if (scope.widget.id.indexOf('form_Uploader') >= 0) {
                            dojo.setStyle(scope.widget.domNode, 'visibility', 'visible');
                        }
                    }

                    if (dojoPropsNew.required === true && scope.show === 'true') {
                        scope.widget.set('required', true);
                    } else if (dojoPropsNew.required === false) {
                        scope.widget.set('required', false);
                    }

                    if (dojoPropsNew.label && dojoPropsNew.label !== dojoPropsOld.label) {
                        scope.widget.set("label", dojoPropsNew.label);
                    }
                });

                if (scope.widget.id.indexOf('form_Uploader') >= 0) {
                    // On initial load a read-only File term's Browse button isn't hidden. This hides it on-load.
                    var dojoProps = parseProps(scope.dojoProps);
                    if (dojoProps.disabled) {
                        dojo.setStyle(scope.widget.domNode, 'visibility', 'hidden');
                    }
                }
            });
        }
    };
}]);

angular.module('angular-dojo').directive('crisTree', function($compile) {
    function getTermType(term) {
        var type = null;

        if (term) {
            if (term.hasOwnProperty('domain')) {
                type = "vocabulary";
            } else if (term.hasOwnProperty('idField') && term.hasOwnProperty('nameField')) {
                type = "attachTo";
            } else if (term.hasOwnProperty('alias') && term.alias) {
                type = "reference";
                if (term.validation && term.validation.validator[0].type === "composite") { // Composite term
                    type = "reference_composite";
                }
            } else if ((term.validation && term.validation.validator[0].type === "composite") || term.isRoot) {
                type = "composite";
            } else {
                type = "simple";
            }
        }

        return type;
    }

    function isCompositeTerm(term) {
        var type = getTermType(term);
        if (type === "composite" || type === "vocabulary" || type === "reference_composite") {
            return true;
        } else {
            return false;
        }
    }

    function removeProperty(object, property) {
        if (object === undefined || object === null) {
            return;
        }

        if (object[property]) {
            delete object[property];
        }

        for (var p in object) {
            if (dojo.isObject(object[p]) || dojo.isArray(object[p])) {
                removeProperty(object[p], property);
            }
        }
    }

    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            showRoot: "@",
            ngModel: '=',
            selectedTerm: "=",
            errors: "="
        },
        template: "<div style='height:100%'>" +
                    '<input type="button" data-dojo-widget="dijit/form/Button" data-dojo-props=\'label : "Simple"\' data-ng-click="newSimpleTerm()"/>' +
                    '<input type="button" data-dojo-widget="dijit/form/Button" data-dojo-props=\'label : "Composite"\' data-ng-click="newCompositeTerm()"/>' +
                    '<input type="button" data-dojo-widget="dijit/form/Button" data-dojo-props=\'label : "Delete"\' data-ng-click="deleteTerm()"/>' +
                    '<div class="idCrisTreeContainer" style="overflow-y:auto;height:95%;"></div>' +
                  "</div>",
        link: function (scope, element, attrs) {
            if (!element) {
                return;
            }

            //var elem = element[0];
            var elem = dojo.query('.idCrisTreeContainer', element[0])[0]; // wrap tree in own div to allow scrolling of tall trees without affecting the buttons above tree

            /***********************************
             * create and initialize the widget
             ***********************************/
            console.log("======== cris-tree: link ========");
            console.dir(scope.ngModel);

            /****************
             * view -> model
             ****************/
            // no change is allowed

            /****************
             * model -> view
             ****************/
            scope.$watch('ngModel', function (value) {
                scope.updateTree(value, elem);
            });

            scope.$watch('selectedTerm', function (value, oldValue) {
                // notify tree that a node is changed
                if (scope.tree) {
	            var model = scope.tree.model;
	            model.onChange(value);
                }

                // If term is updated to latest version, remove the "not latest" flag
                if (value && value.isLatest && scope.tree.selectedNode) {
                    dojo.query('.isLatestFlag', scope.tree.selectedNode.domNode.firstChild).forEach(function(node) {
                        dojo.destroy(node);
                    });

                    // If updating composite reference terms to latest version, reload tree if the number of component terms changes
                    if (value.type === 'composite' && oldValue && value.isLatest !== oldValue.isLatest && value['$$uuid'] === oldValue['$$uuid']) {
                        // After refresh restore previous selection using path
                        var paths = [];
                        dojo.forEach(scope.tree.path, function(p) {
                            paths.push(p['$$uuid']);
                        });
                        scope.updateTree(scope.ngModel, elem, paths);
                    }
                }
            }, true);
        },
        controller: function($scope) {
            console.log("======== cris-tree: controller ========");
            $scope.newSimpleTerm = function() {
                console.log("======== cris-tree: controller: newSimpleTerm ========");
                var term = {name: "", description: "", type: "simple"};
                term.validation = {};
                term.validation.validator = [{type: ""}];
                $scope.newTerm(term);
            };

            $scope.newCompositeTerm = function() {
                console.log("======== cris-tree: controller: newCompositeTerm ========");
                var term = {name: "", description: "", type: "composite"};
                term.term = [];
                term.validation = {};
                term.validation.validator = [{type: "composite"}];
                $scope.newTerm(term);
            };

            $scope.newTerm = function(term) {
                // 1. figure out the current selected node and its parent
                var tree = $scope.tree;
                if (!tree) {
                    $scope.errors.message = "Please select a vocabulary/template";
                    $scope.$apply();
                    return;
                }

                var selectedNode = tree.lastFocused; //attr("selectedItem")
                if (!selectedNode) { // Template/Vocabulary root node not selected...automatically select it
                    tree.rootNode.setSelected();
                    selectedNode = tree.rootNode;
                }

                var parentItem;
                var insertIndex;
                var before = true;

                var selectedItem = selectedNode.item;
                var parentNode;
                if (selectedItem.isRoot) {
                    parentNode = selectedNode;
                    parentItem = parentNode.item;
                    insertIndex = parentItem.term.length;
                } else {
                    parentNode = selectedNode.getParent();
                    parentItem = parentNode.item;
                    insertIndex = dojo.indexOf(parentItem.term, selectedItem);
                }

                // 2. added the term to the model
                term.isNew = true;
                term.$$uuid = dojox.uuid.generateRandomUuid();
                term.uuid = dojox.uuid.generateRandomUuid();
                term.version = dojox.uuid.generateRandomUuid();
                var model = tree.model;
                console.dir(term);
                model.newItem(term, parentItem, insertIndex, before);
            };

            $scope.deleteTerm = function() {
                console.log("======== cris-tree: controller: deleteTerm ========");
                // 1. figure out the current selected node and its parent
                var tree = $scope.tree;
                if (!tree) {
                    $scope.errors.message = "Please select a vocabulary/template";
                    $scope.$apply();
                    return;
                }

                var selectedNode = tree.lastFocused; //attr("selectedItem")
                if (selectedNode) {
                    var selectedItem = selectedNode.item;
                    var parentNode;
                    if (selectedItem.childOfCompositeReference) {
                        $scope.errors.message = "You can not delete a sub term of a composite reference term";
                        $scope.$apply();
                    } else if (!selectedItem.isRoot) {
                        parentNode = selectedNode.getParent();
                        var parentItem = parentNode.item;

                        // 2. remove the node
                        var model = tree.model;
                        model.deleteItem(selectedItem, parentItem);
                    } else {
                        // give useer some feedback: root cannot be deleted
                        $scope.errors.message = "You cannot delete the root";
                        $scope.$apply();
                    }
                } else {
                    // give useer some feedback
                    $scope.errors.message = "Please select a term to delete";
                    $scope.$apply();
                }
            };

            $scope.updateTree = function(value, elem, paths) {
                console.log("======== cris-tree: link: $watch ========");
                if (!value || !value.uuid) {
                    return;
                }

                require(["dojo/aspect", "dijit/dijit", "dojo/store/Memory", "dojo/store/Observable", "dijit/tree/ObjectStoreModel", "dijit/Tree", "dijit/tree/dndSource", "dijit/tree/TreeStoreModel", "dojo/on", "dojo/topic", "dojo/data/ObjectStore", "dojo/domReady!"], function(aspect, dijit, Memory, Observable, ObjectStoreModel, Tree, dndSource, TreeStoreModel, on, topic, ObjectStore) {
                    var scope = $scope;
                    value.isRoot = true;
                    var store = new Memory({
                        data: [value],
                        idProperty: "$$uuid",
                        getIdentity: function(object) {
                            //console.log("================ getIdentity: " + object.name);
                            //console.dir(object);
                            if (object) {
                                return object.$$uuid ? object.$$uuid : (object.alias + object.uuid);
                            } else {
                                return null;
                            }
                        },
                        getChildren: function(parent, options) {
                            console.log("================ getChildren: " + parent.name);
                            console.dir(parent);
                            if (isCompositeTerm(parent)) { //(parent.term && parent.term.length > 0) {
                                return parent.term;
                            } else {
                                return false;
                            }
                        },
                        add: function(object, options) {
                            console.log("================ add: " + object.name);
                            this.put(object, options);
                        },
                        put: function(object, options) {
                            console.log("================ put: " + object.name);
                            console.dir(object);
                            console.dir(options);

                            if (options.before) {
                                //TODO: change the display order of the object
                            } else if (options.parent && options.parent.term) {
                                options.parent.term.push(object);
                            }

                            if (options.overwrite) {
                                //TODO: remove the original object
                            }
                        },
                        remove: function(id) {
                            console.log("================ remove: " + id);
                            // We call onDelete to signal to the tree to remove the child. The
                            // remove(id) gets and id, but onDelete expects an object, so we create
                            // a fake object that has an identity matching the id of the object we
                            // are removing.
                            this.onDelete({id: id});
                            // note that you could alternately wait for this inherited add function to
                            // finish (using .then()) if you don't want the event to fire until it is
                            // confirmed by the server
                            return JsonRest.prototype.remove.apply(this, arguments);
                        }
                    });

                    // wrap the store in an ObjectStore and then Observable
                    store = new ObjectStore({objectStore: store});
                    store = new Observable(store);

                    // Create the model
                    var model = new TreeStoreModel({
                        store: store,
                        childrenAttrs: ["item", "items"],
                        query: {$$uuid: value.$$uuid},
                        getLabel: function(object) {
                            // use name <- alias <- useAlias
                            //console.log("================ getLabel: " + object.name);
                            return object.useAlias ? object.useAlias : (object.alias ? object.alias : object.name);
                        },
                        getRoot: function(onItem, onError) {
                            console.log("================ getRoot");
                            console.dir(onItem);
                            console.dir(value);
                            onItem(value);
                        },
                        mayHaveChildren: function(object) {
                            //console.log("================ mayHaveChildren: " + object.name);
                            if (isCompositeTerm(object)) { //(object.term && object.term.length > 0) {
                                return true;
                            } else {
                                return false;
                            }
                        },
                        getChildren: function(parent, onComplete, onError) {
                            //console.log("================ getChildren: " + parent.name);
                            //console.dir(parent);
                            if (isCompositeTerm(parent)) { //(parent.term && parent.term.length > 0) {
                                var type = getTermType(parent);
                                if (type === 'reference_composite') {
                                    // Flag child terms of a composite reference term
                                    var children = [].concat(parent.term);
                                    while (children.length) {
                                        var child = children.pop();
                                        dojo.forEach(child.term, function(_child){
                                           children.push(_child);
                                        });
                                        child.childOfCompositeReference = true;
                                    }
                                }
                                onComplete(parent.term);
                            } else {
                                onComplete([]);
                            }
                        },
                        newItem: function(args, parent, insertIndex, before) {
                            console.log("================ newItem: ");
                            for (var idx in arguments) {
                                console.dir(arguments[idx]);
                            }
                            var children = parent.term;
                            if (before === null || before === undefined) {
                                children.push(args);
                            } else {
                                children.splice(insertIndex, 0, args);
                            }
                            this.onChildrenChange(parent, parent.term);
                            $scope.$apply();
                        },
                        deleteItem: function(child, parent) {
                            console.log("================ deleteItem: ");
                            for (var idx in arguments) {
                                console.dir(arguments[idx]);
                            }
                            if (parent && parent.term) {
                                var term = parent.term;
                                var index = dojo.indexOf(term, child);
                                if (index >= 0) {
                                    term.splice(index, 1);
                                }
                            }
                            this.onChildrenChange(parent, parent.term);
                        },
                        pasteItem: function(child, oldParent, newParent, bCopy, insertIndex, before){
                            console.log("================ pasteItem: ");
                            for (var idx in arguments) {
                                console.dir(arguments[idx]);
                            }

                            // remove old one
                            this.deleteItem(child, oldParent);
                            // add new one
                            this.newItem(child, newParent, insertIndex, before);
                        },
                        onChildrenChange: function(parent,newChildrenList) {
                            // Update UI Display order if number of terms changes
                            updateUIDisplayOrder(newChildrenList);
                            function updateUIDisplayOrder(children) {
                                dojo.forEach(children, function(child, index){
                                    child.uiDisplayOrder = index;
                                    if (child.term && child.term instanceof Array) {
                                        updateUIDisplayOrder(child.term);
                                    }
                                });
                            }
                        }
                    });

                    // Create the Tree.
                    if (scope.tree) {
                        scope.tree.destroy();
                    }
                    scope.tree = new Tree({
                        model: model,
                        persist: false,
                        showRoot: (scope.showRoot === "true" ? true : false),
                        autoExpand: true,
                        getIconStyle: function(item, opened) {
                            var image = "accept.png";
                            var type;
                            if (item) {
                                type = getTermType(item);
                            } else {
                                type = null;
                            }
                            if (type === "vocabulary") {
                                // vocabulary metadata
                                if (opened) {
                                    image = "book_open.png";
                                } else {
                                    image = "book.png";
                                }
                            } else if (type === "attachTo") {
                                // attach-to term
                                image = "link.png";
                            } else if (type === "reference") {
                                // reference term
                                image = "link_edit.png";
                            } else if (type === "reference_composite") {
                                // composite reference term
                                image = "text_columns_link_edit.png";
                            } else if (type === "composite") {
                                // composite
                                if (item.isRoot === true) {
                                    // template
                                    image = "text_columns.png";
                                } else {
                                    // normal composite
                                    image = "text_columns.png";
                                }
                            } else if (type === "simple") {
                                // simple term
                                image = "text_align_justify.png";
                            } else {
                                //TODO: what to do
                                image = "exclamation.png";
                            }
                            return {background: "url(" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/" + image + ") no-repeat"};
                        }
                    });

                    on(scope.tree, "load", function() {
                        var children = this.rootNode.getChildren();
                        while (children.length) {
                            var child = children.pop();
                            dojo.forEach(child.getChildren(), function(child_) {
                                children.push(child_);
                            });
                            var isAttachTo = (child.item.hasOwnProperty('idField') && child.item.hasOwnProperty('nameField')) ? true : false;
                            // If term is not latest version, add an asterisk flag next to its node
                            if (typeof child.item.isLatest !== 'undefined' && !child.item.isLatest) {
                                angular.element(child.domNode.firstChild).append('<span class="isLatestFlag" style="color:red;">*</span>');
                            }

                            // For invalid term, color label red
                            if (typeof child.item.isTermValid !== 'undefined' && !child.item.isTermValid && (getTermType(child.item).startsWith('reference') || isAttachTo)) {
                                child.labelNode.style.color = 'red';
                                scope.$parent.deleteTermCallback = scope.deleteTerm;
                                scope.$apply();
                            }
                        }

                        if (paths) {
                            scope.tree.set('path', paths);
                        }
                    });

                    scope.tree.placeAt(elem);
                    scope.tree.startup();

                    on(scope.tree, "click", function(item, node, evt) {
                        console.log("=== onSelect ===");
                        for (var idx in arguments) {
                            console.dir(arguments[idx]);
                        }
                        console.dir(scope);
                        if (scope.selectedTerm === scope.$parent.selectedTerm) {
                                console.log("parent == child");
                        } else {
                                console.log("parent != child");
                        }
                        scope.selectedTerm = item;
                        scope.$apply();
                    });

                    var dndParams = {
                        checkAcceptance: function(source, nodes) {
                            //console.log("=== checkAcceptance ===");
                            //for (var idx in arguments) {
                            //    console.dir(arguments[idx]);
                            //}
                            return true;
                        },
                        checkItemAcceptance: function(target, source, position) {
                            console.log("=== checkItemAcceptance ===");
                            for (var idx in arguments) {
                                console.dir(arguments[idx]);
                            }

                            var acceptable = true;
                            var item = dijit.getEnclosingWidget(target).item;
                            var parent = dijit.getEnclosingWidget(target).getParent().item;
                            var isVocabularyRoot = item.isRoot ? item.contributors : parent.contributors; // Current position is in Vocabulary root
                            var isExistingTreeNode = source.node.classList.contains('dijitTree'); // Node being dragged is an existing tree node (simply switching positions)
                            if (isVocabularyRoot && !isExistingTreeNode) { // Prevent drop if position is in vocabulary root, unless node being dragged is existing tree node
                                acceptable = false;
                            }

                            if (item.hasOwnProperty('alias') && item.childOfCompositeReference) { // dnd not allowed within composite reference terms
                                acceptable = false;
                            }

                            if (source.anchor && source.anchor.item.childOfCompositeReference) { // Prevent dnd of child terms of a composite reference
                                return false;
                            }

                            if (position === "over") {
                                console.dir(item);
                                // check if the node is a composite
                                if (item.isRoot || isCompositeTerm(item)) {
                                    if (!item.contributors && getTermType(item) !== 'reference_composite') { // Not vocabulary root and not composite reference
                                        acceptable = true;
                                    } else {
                                        acceptable = false;
                                    }
                                } else {
                                    acceptable = false;
                                }
                            }

                            return acceptable;
                        },
                        itemCreator: function (nodes, target, source) {
                            console.log("=== itemCreator ===");
                            for (var idx in arguments) {
                                console.dir(arguments[idx]);
                            }

                            var item = source.grid.selection.getSelected("row")[0];

                            //TODO: create a term/reference/attachto
                            var term;
                            if (item.type === "template") {
                                // attachto
                                term = dojo.mixin({"useAlias": item.name, "idField": item.id, "nameField": item.name}, item);
                                term.type = "attachTo";
                                term.term = null;
                                term.isTermValid = true;
                                term.fieldList = [];
                                for (var idx in item.term) {
                                    if (item.term[idx].alias) {
                                        term.fieldList.push({id: item.term[idx].alias, name: item.term[idx].alias});
                                    }
                                }
                            } else if (item.type === "term") {
                                // reference
                                term = dojo.mixin({}, item);
                                term.type = "reference";
                                term.alias = term.name;
                                term.isLatest = true;
                                term.isTermValid = true;
                                term.isVersionValid = true;
                                term.latestVersion = term.version;

                                if (term.term && term.term instanceof Array && !term.validation) {
                                    term.validation = {};
                                    term.validation.validator = [{type: "composite"}];
                                }
                            } else {
                                // default to new term
                                term = dojo.mixin({}, item);
                            }
                            removeProperty(term, "__parent");
                            term.$$uuid = dojox.uuid.generateRandomUuid();
                            console.dir(term);

                            return [term];
                        },
                        singular: true,
                        betweenThreshold: 5,
                        accept: ["text", "treeNode", "grid/rows"]
                    };

                    new dndSource(scope.tree, dndParams);
                });
            };
        }
    };
});

angular.module('angular-dojo').directive('crisAddButton', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            itemTemplate: "@",
            items: '='
        },
        template: "<img class='inlineIcon' src='" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/add.png' ng-hide='readOnly' data-ng-click='addItem(null)'/>",
        controller: function($scope) {
            console.log("======== cris-add-button: controller ========");
            $scope.addItem = function(value) {
                console.log("======== cris-add-button: an item is added ========");
                if ($scope.itemTemplate && !value) {
                    $scope.items.push(dojo.fromJson($scope.itemTemplate));
                } else {
                    $scope.items.push(value || "");
                }
            };
        }
    };
});

angular.module('angular-dojo').directive('crisRemoveButton', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            item: "=",
            index: "=",
            items: '='
        },
        template: "<img class='inlineIcon' src='" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/delete.png' ng-hide='readOnly' data-ng-click='removeItem(index)'/>",
        controller: function($scope) {
            console.log("======== cris-remove-button: controller ========");
            $scope.removeItem = function(index) {
                if ($scope.item !== null) {
                    $scope.items.splice($scope.items.indexOf($scope.item), 1);
                } else {
                    $scope.items.splice(index, 1);
                }
            };

        }
    };
});

angular.module('angular-dojo').directive('crisStorageFile', ["$compile", function($compile) {
    return {
        restrict: "E",
        scope: {
            item: "=",
            removable: "=",
            getStorageFileName: "&"
        },
        template: "<div ng-show='item' style='padding:2px 8px;margin-top:4px;'><img class='inlineIcon' src='" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/tick.png' />&nbsp;<a href='{{downloadLink}}'>{{storageFileName}}</a></div>",
        link: function(scope, element, attrs) {
            scope.$watch('item', function(value){
                if (value) {
                    scope.getStorageFileName({storageFile: value}).then(function(result){
                        scope.storageFileName = result.data.fileName;
                        scope.downloadLink = scope.buildDownLoadLink(value);
                    }, function(error){
                        scope.storageFileName = scope.item;
                        scope.downloadLink = scope.buildDownLoadLink(scope.item);
                    });
                }
            });
        },
        controller: function($scope) {
            $scope.buildDownLoadLink = function(storageFile) {
                var link = cris.baseUrl + "download/" + storageFile;
                return link;
            };
        }
    };
}]);

angular.module('angular-dojo').directive('crisStorageFiles', ["$compile", function($compile) {
    return {
        restrict: "E",
        scope: {
            items: "=",
            removable: "=",
            getStorageFileNames: '&',
            readOnly: "="
        },
        template: "<div ng-repeat='item in items track by $index' style='padding:2px 8px;margin-top:4px;'>\n\
                        <img class='inlineIcon' src='" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/tick.png' />\n\
                        <span ng-hide='readOnly'>\n\
                            {{$index}}:&nbsp;\n\
                            <cris-remove-button data-ng-show='removable' items='items' item='item' index='$index'><cris-remove-button>\n\
                        </span>&nbsp;\n\
                        <a href='{{downloadLinks[item]}}'>{{storageFileNames[item]}}</a>\n\
                    </div>",
        link: function(scope, element, attrs) {
            scope.$watchCollection('items', function(value){
                if (value) {
                    scope.storageFileNames = {};
                    scope.downloadLinks = {};
                    var fileData = scope.getStorageFileNames({storageFiles: value});
                    for (var key in fileData) {
                        (function(storageFileId) {
                            fileData[storageFileId].then(function(result){
                                scope.storageFileNames[storageFileId] = result.data.fileName;
                                scope.downloadLinks[storageFileId] = scope.buildDownLoadLink(storageFileId);
                            },function(){
                                scope.storageFileNames[storageFileId] = storageFileId;
                                scope.downloadLinks[storageFileId] = scope.buildDownLoadLink(storageFileId);
                            })
                        })(key);
                    };
                }
            });
        },
        controller: function($scope) {
            $scope.buildDownLoadLink = function(storageFile) {
                var link = cris.baseUrl + "download/" + storageFile;
                return link;
            };
        }
    };
}]);

angular.module('angular-dojo').directive('crisGlobusFile', ["$compile", function($compile) {
    return {
        restrict: "E",
        scope: {
            path: "=",
            file: "=",
            removable: "="
        },
        template: "<span ng-show='file'><img class='inlineIcon' src='" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/tick.png' />&nbsp;</span>\n\
                    <span>\n\
                        <a href='' data-ng-click='browse(path, file)'>{{file}}</a>\n\
                    </span>",
        controller: function($scope) {
            $scope.browse= function (path, file) {
                doGlobusDialog(path, false, file, null, $scope);
            };
        }
    };
}]);

angular.module('angular-dojo').directive('crisGlobusFiles', ["$compile", function($compile) {
    return {
        restrict: "E",
        scope: {
            path: "=",
            files: "=",
            removable: "="
        },
        template: "<div ng-repeat='file in files track by $index'>\n\
                        <img class='inlineIcon' src='" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/tick.png' />\n\
                        <span>\n\
                            {{$index}}:&nbsp;\n\
                            <cris-remove-button data-ng-show='removable' items='files' item='file' index='$index'><cris-remove-button>\n\
                        </span>&nbsp;\n\
                        <a href='' data-ng-click='browse(path, file)'>{{file}}</a>\n\
                    </div>",
        controller: function($scope) {
            $scope.browse= function (path, file) {
                doGlobusDialog(path, false, file, null, $scope);
            };
        }
    };
}]);

angular.module('angular-dojo').directive('crisItemList', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            items: '='
        },
        template: '<div>' +
                      '<cris-add-button items="items"><!-- --></cris-add-button>' +
                      '<table>' +
                      '<tr data-ng-repeat="item in items track by $index">' +
                          '<td><input type="text" data-dojo-widget="dijit/form/ValidationTextBox" data-dojo-props="" data-ng-model="items[$index]"/></td>' +
                          '<td><cris-remove_button items="items" item="item"><!-- --></cris-remove_button></td>' +
                      '</tr>' +
                      '</table>' +
                  '</div>',
        link: {
        },
        controller: function($scope) {
            console.log("======== cris-item-list: controller ========");
        }
    };
});

angular.module('angular-dojo').directive('crisSelect', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            readOnly: "@",
            value: "@",
            items: "@",
            style: "@",
            placeholder: '@',
            ngBlur: '&',
            item: '='
        },
        template: '<div></div>',
        link: function (scope, element, attrs) {
            console.log("======== cris-select: link ========");
            var elem = element[0];

            console.log(scope.items);
            var items;
            if (scope.items) {
                items = scope.items;
            } else {
                items = "[]";
            }
            var data = dojo.fromJson(items);
            console.dir(data);
            console.log(scope.value);
            require(["dijit/dijit", "dojo/store/Memory", "dijit/form/FilteringSelect", "dojo/on"], function(dijit, Memory, FilteringSelect, on) {
                var store = new Memory({
                    data: data
                });

                var widget = new FilteringSelect({
                    disabled: (scope.readOnly === "true" ? true : false),
                    value: scope.value || null,
                    store: store,
                    style: scope.style,
                    placeholder: scope.placeholder ? scope.placeholder : "",
                    searchAttr: "name",
                    onBlur: scope.ngBlur ? scope.ngBlur : null
                }, elem);
                widget.startup();

                /****************
                 * view -> model
                 ****************/
                //if (scope.readOnly !== "true") {
                    on(widget, "change", function (value) {
                        if ((value === undefined) || (value !== value)) {
                            scope.item = null;
                        } else {
                            scope.item = value;
                        }
                        scope.$apply();
                    });
                //}

                /****************
                 * model -> view
                 ****************/
                scope.$watch('items', function (value) {
                    var items;
                    if (value) {
                        items = value;
                    } else {
                        items = "[]";
                    }
                    var store = new Memory({
                        data: dojo.fromJson(items)
                    });
                    widget.set('store', store);
                });
                scope.$watch('value', function (value) {
                    widget.set('value', value || null);
                });
                scope.$watch('readOnly', function (readOnly) {
                    widget.set('disabled', (readOnly === "true" ? true : false));
                });
            });
        },
        controller: function($scope) {
            console.log("======== cris-select: controller ========");
        }
    };
});

angular.module('angular-dojo').directive('crisRestSelect', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            readOnly: "@",
            url: "@",
            value: "=",
            style: "@?"
        },
        template: '<div></div>',
        link: function (scope, element, attrs) {
            console.log("======== cris-rest-select: link ========");
            var elem = element[0];

            require(["dijit/dijit", "dojo/store/JsonRest", "dijit/form/FilteringSelect", "dojo/on"], function(dijit, JsonRest, FilteringSelect, on) {
                var store = new JsonRest({
                    idProperty: attrs.valueField || "id",
                    target: scope.url
                });

                var widget = new FilteringSelect({
                    disabled: (scope.readOnly === "true" ? true : true),
                    value: scope.value || null,
                    store: store,
                    labelAttr: attrs.labelField || "name",
                    searchAttr: attrs.labelField || "name",
                    style: scope.style
                }, elem);
                widget.startup();

                /****************
                 * view -> model
                 ****************/
                if (scope.readOnly !== "true") {
                    on(widget, "change", function (value) {
                        if ((value === undefined) || (value !== value)) {
                            scope.value = null;
                        } else {
                            scope.value = value;
                        }
                        scope.$apply();
                    });
                }

                /****************
                 * model -> view
                 ****************/
                scope.$watch('url', function (value) {
                    var store = new JsonRest({
                        idProperty: attrs.valueField || "id",
                        target: value
                    });
                    widget.set('store', store);
                });
                scope.$watch('value', function (value) {
                    widget.set('value', value || null);
                });
                scope.$watch('readOnly', function (readOnly) {
                    widget.set('disabled', (readOnly === "true" ? true : false));
                });
            });
        },
        controller: function($scope) {
            console.log("======== cris-select: controller ========");
        }
    };
});

angular.module('angular-dojo').directive('crisRadioButton', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            readOnly: "@",
            name: "@",
            value: "@",
            ngModel: "=",
            items: "@",
            orientation: "@"
        },
        link: function (scope, element, attrs) {
            var elem = element[0];

            require(["dojo", "dijit/dijit", "dijit/form/RadioButton", "dojo/on"], function(dojo, dijit, RadioButton, on) {
                var items = dojo.fromJson(scope.items);
                var defaultValue;
                if (scope.value) {
                    defaultValue = dojo.fromJson(scope.value);
                } else {
                    defaultValue = 0;
                }

                angular.forEach(items, function(value, key){
                    var widget = new RadioButton({
                        disabled: (scope.readOnly === "true" ? true : false),
                        name: scope.name,
                        value: value,
                        checked: value === defaultValue
                    });
                    widget.startup();
                    var nodeLabel = dojo.create("span", {innerHTML: key});
                    elem.appendChild(widget.domNode);
                    elem.appendChild(nodeLabel);
                    if (scope.orientation === "vertical") {
                        var br = dojo.create("br");
                        elem.appendChild(br);
                    } else {
                        var nbsp = dojo.create("span", {innerHTML: "&nbsp;"});
                        elem.appendChild(nbsp);
                    }

                    /****************
                     * view -> model
                     ****************/
                    if (scope.readOnly !== "true") {
                        on(widget, "change", function (checked) {
                            if (checked === true) {
                                var widgetValue = widget.get("value");
                                scope.ngModel = widgetValue;
                                scope.$apply();
                            }
                        });
                    }

                    /****************
                     * model -> view
                     ****************/
                    scope.$watch('ngModel', function (newValue) {
                        var widgetValue = widget.value;
                        if (widgetValue === newValue) {
                            widget.set('checked', true);
                        }
                    });
                });
            });
        },
        controller: function($scope) {
        }
    };
});


angular.module('angular-dojo').directive('crisOneCheckBox', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            readOnly: "@",
            disabled: "@",
            items: "@",
            item: '=',
            ngBlur: '&'
        },
        link: function (scope, element, attrs) {
            console.log("======== cris-one-check-box: link ========");
            var elem = element[0];

            require(["dijit/dijit", "dijit/form/CheckBox", "dojo/on"], function(dijit, CheckBox, on) {
                var widget = new CheckBox({
                    readOnly: (scope.readOnly === "true" ? true : false),
                    disabled: (scope.disabled === "true" ? true : false),
                    value: scope.item,
                    checked: scope.isChecked(),
                    onBlur: scope.ngBlur ? scope.ngBlur : null
                }, elem);
                widget.startup();

                /****************
                 * view -> model
                 ****************/
                on(widget, "change", function (value) {
                    scope.setItem(value);
                    scope.$apply();
                });

                /****************
                 * model -> view
                 ****************/
                scope.$watch('item', function (item) {
                    widget.set('value', item);
                    widget.set('checked', scope.isChecked());
                });
                scope.$watch('readOnly', function (value) {
                    widget.set('readOnly', value === "true");
                });
                scope.$watch('disabled', function (value) {
                    widget.set('disabled', value === "true");
                });
            });
        },
        controller: function($scope) {
            console.log("======== cris-one-check-box: controller ========");

            $scope.isChecked = function() {
                var items = $scope.items ? dojo.fromJson($scope.items) : ["false", "true"];
                var index = items.indexOf($scope.item);
                var checked = (index === 1 ? true : false);
                return checked;
            };

            $scope.setItem = function(value) {
                var checked = (value === true ? true : false);
                var index = checked ? 1 : 0;
                var items = $scope.items ? dojo.fromJson($scope.items) : ["false", "true"];
                $scope.item = items[index];
            };
        }
    };
});

angular.module('angular-dojo').directive('crisCheckBox', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            readOnly: "@",
            name: "@",
            value: "@",
            ngModel: "=",
            items: "@",
            orientation: "@"
        },
        link: function (scope, element, attrs) {
            console.log("++++++++ crisCheckBox: link");
            var elem = element[0];
            scope.watchers = [];

            scope.$watch('items', function (newItems) {
                console.log("**************** items changed");
                console.log(newItems);

                // clean up
                angular.forEach(scope.watchers, function(watcher) {
                    watcher();
                });
                angular.forEach(dijit.findWidgets(elem), function(w) {
                    w.destroyRecursive();
                });
                dojo.empty(elem);

                var items;
                items = scope.$parent.$eval(newItems);
                var defaultValues;
                if (scope.ngModel) {
                    defaultValues = scope.ngModel;
                } else if (scope.value) {
                    defaultValues = dojo.fromJson(scope.value);
                } else {
                    defaultValues = [];
                }

                require(["dijit/dijit", "dijit/form/CheckBox", "dojo/on"], function(dijit, CheckBox, on) {
                    angular.forEach(items, function(key, value) {
                        var widget = new CheckBox({
                            disabled: (scope.readOnly === "true" ? true : false),
                            name: scope.name,
                            value: key,
                            checked: defaultValues.indexOf(key) > -1
                        });
                        widget.startup();
                        var nodeLabel = dojo.create("span", {innerHTML: value});
                        if (key !== 0) {
                            elem.appendChild(dojo.create("span", {innerHTML: "&nbsp;"}));
                        }
                        elem.appendChild(widget.domNode);
                        elem.appendChild(nodeLabel);
                        if (scope.orientation === "vertical") {
                            var br = dojo.create("br");
                            elem.appendChild(br);
                        }

                        /****************
                         * view -> model
                         ****************/
                        if (scope.readOnly !== "true") {
                            on(widget, "change", function (checked) {
                                console.log("**************** view -> model: checkbox: " + widget.value + ", checked: " + checked);
                                var widgetValue = widget.value;
                                if (checked === true) {
                                    // add
                                    if (scope.ngModel.indexOf(widgetValue) === -1) {
                                        scope.ngModel.push(widgetValue);
                                        scope.$apply();
                                    }
                                } else {
                                    // remove
                                    scope.ngModel.splice(scope.ngModel.indexOf(widgetValue), 1);
                                    scope.$apply();
                                }
                                console.log("**************** view -> model: checkbox: " + widget.value + ", checked: " + checked);
                            });
                        }

                        /****************
                         * model -> view
                         ****************/
                        var watcher = scope.$watch('ngModel', function (newValue) {
                            console.log("**************** model -> view: checkbox: " + widget.value + ", list of values: " + newValue);
                            var widgetValue = widget.value;
                            if (newValue && newValue.indexOf(widgetValue) > -1) {
                                widget.set('checked', true);
                            } else if (newValue && newValue.indexOf(widgetValue) === -1) {
                                widget.set('checked', false);
                            }
                            console.log("**************** model -> view: checkbox: " + widget.value + ", list of values: " + newValue);
                        });
                        scope.watchers.push(watcher);
                    });
                });
            });
        },
        controller: function($scope) {
            console.log("++++++++ crisCheckBox: controller");
        }
    };
});

angular.module('angular-dojo').directive('crisUploader', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            id: "@",
            submitUrl: "@",
            allowMultiple: "@",
            required: "@",
            name: "@",
            label: "@",
            placeholder: "@",
            title: '@'
        },
        template: '<div>\n\
                    <form id="{{id}}_form" data-dojo-id="{{id}}_form" method="POST" enctype="multipart/form-data" data-dojo-type="dijit/form/Form">\n\
                        <h2>{{title}}</h2>\n\
                        <input id="{{id}}_uploader" name="{{name}}" data-dojo-type="dojox/form/Uploader" data-dojo-props=\'label : "{{label}}", multiple : "{{allowMultiple}}"\' style="width: 100px;"/>\n\
                        <div data-dojo-type="dojox/form/uploader/FileList" uploaderId="{{id}}_uploader"></div>\n\
                    </form>\n\
                   </div>',
        link: function (scope, element, attrs) {
            console.log("======== cris-uploader: link ========");
        },
        controller: function($scope) {
            console.log("======== cris-uploader: controller ========");
        }
    };
});

angular.module('angular-dojo').directive('crisUploaderDialog', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            dialogId: "@",
            errors: "=",
            submitUrl: "@",
            ajax: "@",
            onSubmit: "&",
            id: "@",
            allowMultiple: "@",
            required: "@",
            name: "@",
            labelText: "@",
            placeholder: "@",
            title: '@',
            showCheckBox: "=",
            checkBoxName: "@",
            checkBoxMessage: "@"
        },
        template: '<div>\n\
                    <cris-uploader id="{{id}}" title="{{title}}" label="{{labelText}}" placeholder="{{placeholder}}" allow-multiple="{{allowMultiple}}" submit-url="{{submitUrl}}">\n\
                        <!-- -->\n\
                    </cris-uploader>\n\
                    <div data-ng-show="showCheckBox">\n\
                        <input type="checkbox" data-dojo-widget="dijit/form/CheckBox" data-ng-model="checkBoxValue" /> {{checkBoxMessage}} <br/>\n\
                    </div>\n\
                    <input type="button" data-dojo-widget="dijit/form/Button" data-dojo-props=\'label : "Upload"\' data-ng-click="submit()"/>&nbsp;\n\
                  </div>',
        link: function (scope, element, attrs) {
            console.log("======== cris-uploader-dialog: link ========");
            var elem = element[0];

            require(["dijit/dijit", "dijit/Dialog", "dojox/form/Uploader", "dojo/on"], function(dijit, Dialog, Uploader, on) {
                /****************
                 * view -> model
                 ****************/

                /****************
                 * model -> view
                 ****************/
                scope.$watch('abc', function (item) {
                });
            });
        },
        controller: function($scope) {
            console.log("======== cris-uploader-dialog: controller ========");

            $scope.submit = function() {

                console.log("=== " + $scope.errors.message);
                if (!$scope.ajax || $scope.ajax === "true") {
                    require(["dojo/_base/lang", "dojo/request/iframe"], function(lang, iframe){
                        var handleAs = "json";
                        var form = dojo.byId($scope.id + "_form");
                        var query = null;
                        var submitQuery = lang.mixin(lang.clone(query), {isIframe: true});
                        if ($scope.showCheckBox) {
                            submitQuery[$scope.checkBoxName] = $scope.checkBoxValue;
                        }
                        var timeout = null;
                        var preventCache = false;

                        var dialog;
                        if ($scope.dialogId) {
                            dialog = dijit.byId($scope.dialogId);
                        } else {
                            dialog = null;
                        }

                        console.log("======== cris-uploader-dialog: controller: submit() " + cris.baseUrl + $scope.submitUrl + " ========");
                        console.dir(form);
                        iframe(cris.baseUrl + $scope.submitUrl, {
                            method : "POST",
                            headers: {Accept: "application/json"},
                            handleAs : handleAs,
                            form : form,
                            query: submitQuery,
                            timeout: timeout,
                            preventCache: preventCache
                        }).then(function(data) {
                            console.log("======== cris-uploader-dialog: controller: submit(): OK ========");
                            console.dir(data);
                            if (data.hasError) {
                                $scope.errors.message = data.message;
                            } else {
                                if (data.message) {
                                    $scope.errors.message = data.message;
                                } else {
                                    $scope.errors.message = "Imported Successfully";
                                }
                            }
                            $scope.$apply();
                            if (dialog) {
                                dialog.hide();
                            }
                        }, function(error) {
                            console.log("======== cris-uploader-dialog: controller: submit() failed ========");
                            console.log(error);
                            if (error && error.response && error.response.data) {
                                var data = error.response.data;
                                if (data.hasError) {
                                    $scope.errors.message = data.message;
                                } else {
                                    $scope.errors.message = data;
                                }
                            } else if (error && error.response && error.response.text) {
                                $scope.errors.message = error.response.text;
                            } else if (error && error.message) {
                                $scope.errors.message = error.message;
                            } else {
                                $scope.errors.message = error;
                            }
                            $scope.$apply();
                            if (dialog) {
                                dialog.hide();
                            }
                        });
                    });
                } else {
                    var form = dijit.byId($scope.id + "_form");
                    if (form.isValid()) {
                        form.submit();
                    } else {
                        //TODO: display error message
                    }
                }
            };
        }
    };
});

angular.module('angular-dojo').directive('crisDatePicker', function($compile) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            ngModel: "=",
            type: "@",
            isReadOnly: "@",
            ngChange: '&'
        },
        template: ' <span class="input-group"> \
                        <input type="type" uib-datepicker-popup="{{dateFormat}}" class="form-control textField" ng-model="date" is-open="opened" datepicker-options="dateOptions" datepicker-append-to-body="true" ng-disabled="isReadOnly===\'true\'" close-text="Close" /> \
                        <span class="input-group-btn"> \
                          <button type="button" class="btn btn-default" data-ng-click="open()" ng-disabled="isReadOnly===\'true\'"><i class="glyphicon glyphicon-calendar"></i></button> \
                        </span> \
                    </span>',
        link: function (scope, element, attrs) {
            /*
             * uib-datepicker requires a date object for the ng-model, but our model requires a string value.
             * We need this custom directive to convert the date object to a string before updating the model.
             */
            if (scope.ngModel) {
                if (typeof scope.ngModel !== 'object' && isNaN(scope.ngModel) && !isNaN(Date.parse(scope.ngModel))) {
                    scope.date = new Date(scope.ngModel); // uib-datepicker requires a date object
                } else {
                    scope.date = null;
                }
            }
            scope.dateOptions = {};
            scope.opened = false; // datepicker initially closed
            scope.dateFormat = "MM/dd/yyyy"; // default date format

            scope.$watch('date', function(value){
                if (value) {
                    scope.ngModel = value.toISOString();
                    if (scope.ngChange) {
                        scope.ngChange();
                    }
                } else {
                    scope.ngModel = null;
                }
            });

            // Format date as user types. Month, day, yr separator is either "/" or "-"; use whichever user picks
            element.find('.textField').keyup(function(evt){
                var target = evt.target;
                if (/^[1-9](\/|\-)$/.test(target.value.trim())) { // matches M/ or M-. Format as MM/ or MM-
                    target.value = target.value.replace(/^([1-9])(\/|\-)$/, '0$1' + '$2');
                } else if (/^\d{2}(\/|\-)[1-9](\/|\-)$/.test(target.value.trim())) {  // matches MM/d/ or MM/d-. Format as MM/dd/ or MM-dd-
                    target.value = target.value.replace(/^(\d{2})(?:\/|\-)([1-9])(\/|\-)$/, '$1' + '$3' + '0$2' + '$3')
                }

            });

            // On blur write out full date as MM/dd/yyyy or MM-dd-yyyy
            element.find('.textField').blur(function(evt) {
                var target = evt.target;
                if (/^\d{2}(\/|\-)\d{2}(\/|\-)\d{2}$/.test(target.value.trim())) { // matches MM/dd/yy or MM-dd-yy. Format as MM/dd/yyyy or MM-dd-yyyy
                    target.value = target.value.replace(/^(\d{2})(?:\/|\-)(\d{2})(\/|\-)(\d{2})$/, '$1' + '$3' + '$2' + '$3' + '20$4')
                    scope.date = new Date(target.value);

                    if (target.value.indexOf('-') !== -1) {
                        scope.dateFormat = "MM-dd-yyyy";
                    } else {
                        scope.dateFormat = "MM/dd/yyyy";
                    }
                }
            });
        },
        controller: function ($scope) {
            $scope.open = function () {
                if ($scope.opened) {
                    $scope.opened = false;
                } else {
                    $scope.opened = true;
                }
            };
        }
    };
});

angular.module('angular-dojo').directive('crisTimePicker', function($timeout) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            ngModel: "=",
            isReadOnly: "@",
            ngChange: '&'
        },
        template: '<div><uib-timepicker ng-model="dateTime" ng-change="changed()" hour-step="1" minute-step="1" show-meridian="true" ng-disabled="isReadOnly===\'true\'"></uib-timepicker></div>',
        link: function (scope, element, attrs) {
            /*
             * uib-timepicker requires a date object for the ng-model, but our model requires a string value.
             * We need this custom directive to convert the date object to a string before updating the model.
             */
            if (scope.ngModel) {
                scope.dateTime = new Date(scope.ngModel); // uib-timepicker requires a date object
                if (isNaN(scope.dateTime.getHours())) {
                    scope.dateTime = null;
                }
            }

            scope.$watch('dateTime', function(value){
                if (value) {
                    try {
                        scope.ngModel = value.toISOString();
                        if (scope.ngChange) {
                            scope.ngChange();
                        }
                    } catch (e) {
                        scope.ngModel = null;
                    }
                } else {
                    scope.ngModel = null;
                }
            });

            // By default the Meridian button [AM/PM] does not react when pressing "P" or "A" keyboard keys.
            // Bind event to react when user clicks "P" (for PM) or "A" (AM), and toggle meridian value
            $timeout(function(){
                element.find('button').keyup(function(evt) {
                    if (evt.keyCode === 80 || evt.keyCode === 65) { // "p" = 80, "a" = 65
                        if (scope.previousKeyCode !== evt.keyCode) {
                            element.find('button').click();
                            scope.previousKeyCode = evt.keyCode;
                        }
                    }
                });
            },500);
        },
        controller: function ($scope) {
        }
    };
});

angular.module('angular-dojo').directive('crisDropdown', function($compile) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            ngModel: "=",
            items: "@",
            isReadOnly: "@",
            onBlur: "&",
            onChange: "&"
        },
        template: ' <div class="btn-group btn-group-justified" dropdown-append-to-body uib-dropdown ng-cloak is-open="isOpen"> \
                        <input type=text focus-element="{{focusElement}}" class="form-control filterBox" ng-model="filterValue" style="width:100%;display:inline-block;" ng-show="hideDropButton" ng-click="$event.stopPropagation();" placeHolder="Filter..." /> \
                        <button type="button" class="btn btn-default" style="width:100%;" uib-dropdown-toggle ng-disabled="isReadOnly===\'true\'" ng-hide="hideDropButton"> \
                            <div style="display:table;width:100%;table-layout:fixed;"> \
                                <div style="display:table-row;"> \
                                    <div style="display:table-cell;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:85%;"><span uib-popover="{{getDisplayName(ngModel)}}" popover-trigger="mouseenter" popover-placement="auto left-top">{{getDisplayName(ngModel)}}&nbsp;</span></div> \
                                    <div style="display:table-cell;" class="text-right;"><span class="glyphicon glyphicon-menu-down pull-right"></span></div> \
                                </div> \
                            </div> \
                        </button> \
                        <ul class="dropdown-menu" uib-dropdown-menu role="menu" style="max-height:300px;min-width:250px;overflow-y:auto;"> \
                            <li role="menuitem" ng-click="selectItem()"><a>&nbsp;</a></li> \
                            <li ng-repeat="item in listItems track by $index" role="menuitem" ng-click="selectItem(item)"><a>{{item.name}}</a></li> \
                        </ul> \
                    </div>',
        link: function (scope, element, attrs) {
            scope.isOpen = false;
            
            scope.$watch('items', function(value) {
                if (value) {
                    scope.allItems = dojo.fromJson(scope.items);
                } else {
                    scope.allItems = [];
                }
                scope.listItems = scope.allItems;
            });

            scope.$watch('ngModel', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    scope.onChange();
                }
            });
            
             scope.$watch('filterValue', function(newValue, oldValue) {
                if (scope.item && scope.item["name"] === scope.filterValue) {
                    return;
                }
                 
                if (newValue !== oldValue) {
                    scope.listItems = [];
                    if (!scope.isOpen) {
                        scope.listItems = scope.allItems;
                    } else {
                        angular.forEach(scope.allItems, function (item) {
                            if (item["name"].toString().toLowerCase().indexOf(newValue.toLowerCase()) !== -1) {
                                scope.listItems.push(item);
                            }
                        });
                    }
                }
            });
            
            scope.$watch('isOpen', function(value){
                if (value) {
                    scope.hideDropButton = true;
                    scope.focusElement = true; // Focus the search box
                    scope.filterValue = scope.item ? scope.item["name"] : "";
                } else {
                    scope.hideDropButton = false;
                    scope.focusElement = false;
                    if (scope.item) {
                        scope.filterValue = scope.item["name"];
                    } else {
                        scope.filterValue = null;
                    }
                    scope.listItems = scope.allItems;
                }
            });
        },
        controller: function ($scope) {
            $scope.selectItem = function (item) {
                if (item) {
                    $scope.ngModel = item.id;
                } else {
                    $scope.ngModel = null;
                }
                if ($scope.onBlur) {
                    $scope.onBlur();
                }
            };
            $scope.getDisplayName = function () {
                var name = "";
                if ($scope.ngModel !== null && typeof $scope.ngModel !== 'undefined') {
                    for (var key in $scope.allItems) {
                        if ($scope.allItems[key].id === $scope.ngModel) {
                            name = $scope.allItems[key].name.toString();
                            $scope.item = $scope.allItems[key];
                            break;
                        }
                        $scope.item = null;
                    }
                }
                return name;
            };
        }
    };
});

angular.module('angular-dojo').directive('crisUrlDropdown', function($compile, $http) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            ngModel: "=",
            url: "@",
            idField: "@", // default is "id"
            nameField: "@", // default is "name"
            item: "=?", // Optional access to selected item
            items: "=?", // Optional access to all items
            query: "@",
            useMongoQuery: "@", // Optional: Use mongo query in url, i.e. ...?query={....}
            isReadOnly: "@"
        },
        template: ' <div class="btn-group btn-group-justified" dropdown-append-to-body uib-dropdown is-open="isOpen"> \
                        <input type=text focus-element="{{focusElement}}" class="form-control" ng-model="filterValue" style="width:100%;display:inline-block;" ng-show="hideDropButton" ng-click="$event.stopPropagation();" placeHolder="Filter..." /> \
                        <button type="button" class="btn btn-default" style="width:100%;" uib-dropdown-toggle ng-disabled="isReadOnly===\'true\'" ng-hide="hideDropButton"> \
                            <div style="display:table;width:100%;table-layout:fixed;"> \
                                <div style="display:table-row;"> \
                                    <div style="display:table-cell;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:85%;">{{selectedName}}&nbsp;</div> \
                                    <div style="display:table-cell;" class="text-right;"><span class="glyphicon glyphicon-menu-down pull-right"></span></div> \
                                </div> \
                            </div> \
                        </button><br /> \
                        <ul class="dropdown-menu" uib-dropdown-menu role="menu" aria-labelledby="btn-append-to-single-button" style="max-height:300px;min-width:200px;overflow-y:auto;"> \
                            <!--<li><div style="padding: 5px 10px;"><input type="text" class="form-control" placeholder="--filter--" ng-model="filterValue" ng-click="onFilterClicked($event)"></div></li>--> \
                            <li role="menuitem" ng-click="selectItem()"><a>&nbsp;</a></li> \
                            <li ng-repeat="item in listItems" role="menuitem" ng-click="selectItem(item)"><a>{{item[nameField]}}</a></li> \
                        </ul> \
                    </div>',
        link: function (scope, element, attrs) {
            scope.isOpen = false;
            
            if (!scope.idField) {
                scope.idField = "id";
            }
            if (!scope.nameField) {
                scope.nameField = "name";
            }
            
            scope.$watch('query', function(newValue, oldValue) {
                if (newValue && newValue !== oldValue) {
                    scope.fetchItems();
                }
            });

            scope.$watch('url', function(newValue, oldValue) {
                if (newValue && newValue !== oldValue) {
                    scope.fetchItems(null, true);
                }
            });

            scope.$watch('ngModel', function(value){
                if (value && scope.items) {
                    for (var i = 0; i < scope.items.length; i++) {
                        if (scope.items[i][scope.idField] === scope.ngModel) {
                            scope.item = scope.items[i];
                            scope.selectedName = scope.item[scope.nameField];
                            break;
                        }
                    }
                } else {
                    scope.item = null;
                    scope.selectedName = "";
                }
            });

            scope.$watch('isOpen', function(value){
                if (value) {
                    scope.hideDropButton = true;
                    scope.focusElement = true; // Focus the search box
                    if (scope.item) {
                        scope.filterValue = scope.item[scope.nameField];
                    }
                } else {
                    scope.hideDropButton = false;
                    scope.focusElement = false;
                    if (scope.item) {
                        scope.filterValue = scope.item[scope.nameField];
                    } else {
                        scope.filterValue = null;
                    }
                    scope.fetchItems(); // Initial load
                }
            });

            scope.$watch('filterValue', function(value) {
                if (scope.item && scope.item[scope.nameField] === value) {
                    return;
                }
                if (!scope.isOpen) {
                    // If user searches but doesn't make selection, fetch all after dropdown closes (filterValue will be current selection(ngModel); i.e. don't filter result using ngModel)
                    scope.fetchItems();
                } else {
                    scope.fetchItems(value);
                }
            });
        },
        controller: function ($scope) {
            $scope.selectItem = function (item) {
                if (item) {
                    $scope.ngModel = item[$scope.idField];
                    $scope.selectedName = item[$scope.nameField];
                    $scope.item = item;
                } else {
                    $scope.ngModel = null;
                    $scope.selectedName = null;
                    $scope.filterValue = null; // Clear search text
                    $scope.item = null; // Clear selected Item
                }
            };
            $scope.onFilterClicked = function (e) {
                // Do not close UL after click the filter textbox (which is part of the UL element)
                e.stopPropagation()
            };
            $scope.fetchItems = function (filterValue, setSelected) {
                var httpFetchTimeout;
                var queryStr = "";
                var url = $scope.url;

                var qryObj = {};
                if ($scope.query) {
                    try {
                        qryObj = JSON.parse($scope.query);
                    } catch (e) {
                        console.error('*********** Attach-to custom query invalid **** ' + $scope.query)
                    }
                }

                // Mixin in search value with existing mongo query
                if (filterValue) {
                    if ($scope.useMongoQuery === 'true') {
                        var mongoRgx = filterValue; // "contains" search
                        var rgxOptions = 'i'; // case-insensitive search
                        if (!qryObj[$scope.nameField]) {
                            qryObj[$scope.nameField] = {};
                            qryObj[$scope.nameField].$regex = mongoRgx;
                            qryObj[$scope.nameField].$options =  rgxOptions;
                        } else {
                            if (typeof qryObj[$scope.nameField] === 'string') {
                                var obj = {};
                                obj.$eq = qryObj[$scope.nameField];
                                obj.$regex = mongoRgx;
                                obj.$options =  rgxOptions;
                                qryObj[$scope.nameField] = obj;
                            } else if (typeof qryObj[$scope.nameField] === 'object' && !(qryObj[$scope.nameField] instanceof Array)) {
                                qryObj[$scope.nameField].$regex = mongoRgx;
                                qryObj[$scope.nameField].$options =  rgxOptions;
                            }
                        }
                    } else {
                        // TODO......
                        // For now we are using the dojo way to filter records...this will change in the future with server-side changes
                        // E.g.1 filter string: filter={"op":"equal","data":[{"op":"number","data":"statusId","isCol":true},{"op":"number","data":1,"isCol":false}]}
                        // E.g.2 filter string(multiple filters): filter={"op":"any","data":[{"op":"equal","data":[{"op":"number","data":"projectId.id","isCol":true},{"op":"number","data":5000,"isCol":false}]},{"op":"equal","data":[{"op":"number","data":"experimentId.id","isCol":true},{"op":"number","data":2000,"isCol":false}]}]}
                        if (url.indexOf('filter=') !== -1) {
                            var filterStr = /filter\s*=\s*(\{.+\})\s*(?:$|\&)/.exec(url)[1];

                            if (filterStr) { // A filter query string exists
                                // Remove old filter string. A new one will be created that includes the search term
                                url = url.replace(/\/?\?filter\s*=\s*\{.+\}\s*\&/g, '/?').replace(/\&filter\s*=\s*\{.+\}\s*/g, '');

                                var filterObj = JSON.parse(filterStr);
                                var newObj = {};
                                newObj.op = 'and';
                                newObj.data = [];
                                newObj.data.push(filterObj)

                                // Add "contains" filter for search term
                                newObj.data.push({
                                                    op: "contains",
                                                    data: [
                                                        {op:"string", data:$scope.nameField, isCol:true},
                                                        {op:"string", data:filterValue, isCol:false}
                                                    ]
                                                });
                            }
                            queryStr += '&filter=' + JSON.stringify(newObj);
                        } else {
                            var ampersand = "";
                            if ((url.indexOf('&') !== -1 && !url.endsWith('&')) || url.indexOf('/?') !== -1) {
                                ampersand = "&";
                            }
                            if (url.indexOf('&') === -1 && !url.trim().endsWith('/?') && url.indexOf('/?') === -1) {
                                ampersand = '/?';
                            }
                            //var ampersand = url.match(/\/\?.+$/) ? "" : "&"; // If there is a query string, do not add ampersand before filter string
                            queryStr += (ampersand + 'filter={"op":"contains","data":[{"op":"string","data":"' + $scope.nameField + '","isCol":true},{"op":"string","data":"' + filterValue + '","isCol":false}]}&sort(+name)');
                        }
                    }
                }

                if ($scope.useMongoQuery === 'true') {
                    if (!qryObj.$limit) {
                        qryObj.$limit = 100;
                    }
                    queryStr += "/?query=" + JSON.stringify(qryObj);
                }

                // cancel the current waiting fetch before creating a new one (fetches occur after timeout of 500ms)
                if (httpFetchTimeout) {
                    clearTimeout(httpFetchTimeout);
                    httpFetchTimeout = null;
                }

                httpFetchTimeout = setTimeout(function() {
                    $http({
                        method: 'GET',
                        url: url + queryStr
                    }).then(function(result) {
                        $scope.listItems = [];
                        $scope.items = result.data;

                        for (var key in result.data) {
                            var item = {};
                            // Allow only records that have the specified id and name fields
                            item[$scope.idField] =  result.data[key][$scope.idField];
                            item[$scope.nameField] =  result.data[key][$scope.nameField];
                            if (typeof item[$scope.idField] !== 'undefined' && item[$scope.idField] !== null && typeof item[$scope.nameField] !== 'undefined' && item[$scope.nameField] !== null) {
                                $scope.listItems.push(item);
                            }
                        }

                        // On initial load set the selected value (ngModel)
                        if ((!$scope.selectedName && $scope.ngModel) || (setSelected && $scope.ngModel)) {
                            for (var key in $scope.listItems) {
                                if ($scope.listItems[key][$scope.idField] === $scope.ngModel) {
                                    $scope.selectedName = $scope.listItems[key][$scope.nameField];
                                    $scope.item = $scope.listItems[key];
                                }
                            }
                        }
                    }, function(error) {
                        console.log('****** Searcheable Dropdown: Fetch failed **********************');
                    });
                    $scope.$apply();
                }, 200);
            };
        }
    };
});

// This directive is used to set focus to an input (e.g. in the searcheable dropdown directive). This fixes issue were ng-focus doesn't work.
angular.module('angular-dojo').directive('focusElement', function($timeout) {
    return {
        restrict: 'A',
        scope: {
            trigger: '@focusElement'
        },
        link: function(scope, element, attrs) {
            scope.$watch('trigger', function(value) {
                if (value === 'true') {
                    $timeout(function() {
                        element[0].focus();
                    }, 5);
                }
            });
        }
    }
});

angular.module('angular-dojo').directive('crisMultiSelect', function($compile) {
    return {
        restrict: 'E',
        require: 'ngModel',
        replace: true,
        scope: {
            ngModel: "=",
            items: "=",
            isReadOnly: "@",
            ngRequired: "="
        },
        template: ' <div style="border: 1px solid #ccc; border-radius: 4px;"> \
                        <div ng-repeat="item in items" class="form-horizontal" style="padding:6px 12px;"> \
                            <input type="checkbox" ng-click="toggleItem(item, $event)" class="checkbox-inline" ng-disabled="isReadOnly===\'true\'" ng-checked="isItemChecked(item.id)">&nbsp;{{item.name}} \
                        </div> \
                    </div>',
        link: function (scope, element, attrs, ngModelController) {
            if (!scope.ngModel) {
                scope.ngModel = [];
            }

            scope.$watch('ngRequired', function(value){
                if (value === true) {
                    ngModelController.$setValidity(attrs.ngModel, scope.ngModel ? scope.ngModel.length > 0 : false);
                } else {
                    ngModelController.$setValidity(attrs.ngModel, true);
                }
            });

            scope.$watchCollection('ngModel', function (newValue, oldValue) {
                if (scope.ngRequired) {
                    ngModelController.$setValidity(attrs.ngModel, newValue ? newValue.length > 0 : false);
                }
            });
        },
        controller: function ($scope) {
            $scope.toggleItem = function (item, evt) {
                if (evt.target.checked) {
                    $scope.ngModel.push(item.id);
                } else {
                    var idx = $scope.ngModel.indexOf(item.id);
                    if (idx !== -1) {
                        $scope.ngModel.splice(idx, 1);
                    }
                }
            }
            $scope.isItemChecked = function (itemId) {
                var result = false;
                if ($scope.ngModel) {
                    result = ($scope.ngModel.indexOf(itemId) !== -1);
                }
                return result;
            }
        }
    };
});

angular.module('angular-dojo').directive('crisFileUploader', function($compile) {
    return {
        restrict: 'E',
        require: 'ngModel',
        replace: true,
        scope: {
            path: "@", // Use for element name
            isMultiple: "=",
            ngModel: "=",
            isRequired: "="
        },
        template: ' <div> \
                        <span class="btn btn-default btn-file btn-primary" style="min-width:165px;text-align:left;"> \
                            <span class="glyphicon glyphicon-folder-open"></span>&nbsp; \
                            <span ng-if="isMultiple" class="crisFileUploaderMultiple">Browse Files<input type="file" name="{{path + \'[]\'}}" multiple="multiple" /></span> \
                            <span ng-if="!isMultiple" class="crisFileUploaderSingle">Browse File<input type="file" name="{{path}}" /></span> \
                        </span> \
                        <div ng-show="!isMultiple" ng-repeat="item in fileList" style="padding:2px 8px;margin-top:4px;"><img class="inlineIcon" src="' + cris.imagesRoot + '/famfamfam_silk_icons_v013/icons/tick.png" />&nbsp;{{item.name}}</div> \
                        <table ng-show="fileList && fileList.length && isMultiple"> \
                            <tr> \
                                <td style="padding-left:8px;padding-right:8px;"><img class="inlineIcon" src="' + cris.imagesRoot + '/famfamfam_silk_icons_v013/icons/tick.png" /></td> \
                                <td style="border-right:1px solid gray;padding-right:8px;"><img class="inlineIcon" src="' + cris.imagesRoot + '/famfamfam_silk_icons_v013/icons/delete.png" ng-click="removeUploadedFiles(true)" /></td> \
                                <td><div ng-repeat="item in fileList" style="padding:2px 8px;margin-top:4px;">{{item.name}}</div></td> \
                            </tr> \
                        </table> \
                    </div>',
        link: function (scope, element, attrs, ngModelController) {
            element.bind('change', function(evt) {
                scope.fileList = evt.target.files;
                scope.$emit('FilesToUpload', {fileList: scope.fileList, uploaderName: scope.path, isMultiple: scope.isMultiple}); // notify users of files to upload
                if (!scope.isMultiple) {
                    // Remove existing file for single file upload
                    scope.ngModel = null;
                }
                scope.$apply();
            });

            // Set validity based on the isRequired flag
            scope.$watch('isRequired', function(value){
                if (value === true && (!scope.ngModel || (scope.ngModel instanceof Array && scope.ngModel.length === 0))) {
                    ngModelController.$setValidity('fileError', scope.fileList ? (scope.fileList.length > 0) : false);
                } else {
                    ngModelController.$setValidity('fileError', true);
                }
            });

            // Set validity based on list on files to upload
            scope.$watchCollection('fileList', function (value) {
                if (value && (!scope.ngModel || (scope.ngModel instanceof Array && scope.ngModel.length === 0))) {
                    ngModelController.$setValidity('fileError', (value.length > 0 || !scope.isRequired));
                }
            });
            
            // Set validity based on number of existing files
            scope.$watch('ngModel', function (value) {
                var filesUploaded = (scope.fileList && scope.fileList.length > 0);
                if (!value) {
                    ngModelController.$setValidity('fileError', ((!scope.isRequired || filesUploaded) ? true : false));
                } else if (value) {
                    if (value instanceof Array) {
                        ngModelController.$setValidity('fileError', (value.length > 0 || filesUploaded || !scope.isRequired));
                    } else {
                        ngModelController.$setValidity('fileError', true);
                    }
                }
            }, true);
            
            // event to reset single-file uploader
            scope.$on('ResetSingleFileUploader', function () {
                scope.removeUploadedFiles();
            });
            
            // event to reset multi-file uploader
            scope.$on('ResetMultiFileUploader', function () {
                scope.removeUploadedFiles(true);
            })
        },
        controller: function ($scope, $element) {
            $scope.removeUploadedFiles = function (isMultiple) {
                // Remove all uploaded (but not yet saved) files....
                // For security reasons it is not possible to manipulate the file upload element, E.g. by deleting individual items.
                // Therefore, the only way to clear it is to replace the entire element
                if (isMultiple) {
                    var multiFileInput = $element.find('.crisFileUploaderMultiple')[0];
                    if (multiFileInput) {
                        var name = /name="([a-zA-Z0-9_\-\.(\[\])]+)"/.exec(multiFileInput.innerHTML); // Get name/path of the multi-file upload
                        multiFileInput.innerHTML = 'Browse Files<input type="file" name="' + (name ? name[1] : "") + '" multiple="multiple" />';
                        $scope.fileList = []; // Clear list of files to display
                    }
                    $scope.$emit('FilesRemoved', {uploaderName: $scope.path, isMultiple: $scope.isMultiple}); // notify users if file(s)-to-upload are deleted
                } else {
                    var singleFileInput = $element.find('.crisFileUploaderSingle')[0];
                    if (singleFileInput) {
                        var name = /name="([a-zA-Z0-9_\-\.]+)"/.exec(singleFileInput.innerHTML); // Get name/path of the multi-file upload
                        singleFileInput.innerHTML = 'Browse File<input type="file" name="' + (name ? name[1] : "") + '" />';
                        $scope.fileList = []; // Clear list of files to display
                    }
                    $scope.$emit('FilesRemoved', {uploaderName: $scope.path, isMultiple: $scope.isMultiple}); // notify users if file(s)-to-upload are deleted
                }
            };
        }
    };
});

angular.module('angular-dojo').directive('crisUiGrid', ['$http', '$timeout', 'uiGridConstants', function($http, $timeout, uiGridConstants) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            //data: "=?", // Optional data array for grid if not using a url. TODO: fix pagination if using this option. current pagination depends on url fetched data.
            columnDefs: "=",
            enableFiltering: "=?",
            gridRef: "=", // call function to refresh grid
            rowSelectCallback: "&",
            formatDisplayValue: "&", // function to modify cell display value. E.g. adding currency and format symbols. Function must be in cellTemplate property of column definition
            url: "=?",
            urlFilter: "=?",
            enableRowSelect: "@",
            sortField: "=?",
            sortDirection: "@" // "asc" or "desc"
        },
        template: '<div> \
                        <div ui-grid="gridOptions" ui-grid-auto-resize ui-grid-pagination ui-grid-selection ui-grid-resize-columns ng-style="{height: getGridHeight() + \'px\'}"></div> \
                   </div>',
        link: function (scope, element, attrs) {
            console.log('**** ui-grid init *******');
            console.dir(uiGridConstants);
            
            var paginationOptions = {
                pageNumber: 1,
                pageSize: 10,
                sort: (scope.sortDirection ? scope.sortDirection : null)
            };
            
            if (!scope.columnDefs) {
                scope.columnDefs = [];
            }
            
            var filterTimeout;
            scope.gridOptions = {
                columnDefs: scope.columnDefs,
                data: scope.data,
                enableFiltering: scope.enableFiltering,
                appScopeProvider: scope,
                enableColumnMenus: false,
                enableRowSelection: (scope.enableRowSelect === 'false') ? false : true, 
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                noUnselect: true,
                enableVerticalScrollbar: 0,
                paginationPageSizes: [10, 25, 50, 75],
                paginationPageSize: 10,
                useExternalPagination: true,
                useExternalSorting: true,
                enableColumnResizing: true,
                onRegisterApi: function (gridApi)  {
                    console.dir(gridApi)
                    scope.gridApi = gridApi;
                    scope.gridRef = {
                        refreshGrid: function (callback) {
                            var to = $timeout(function() {
                                fetchData(callback);
                                $timeout.cancel(to);
                            }, 0);
                        },
                        selectRow: function (idValue, idField) {
                            var row;
                            for (var i = 0; i < scope.data.length; i++) {
                                if (idValue === scope.data[i][idField]) {
                                    row = scope.data[i];
                                    break;
                                }
                            }
                            gridApi.selection.selectRow(row);
                        },
                        clearSelection: function () {
                            gridApi.selection.clearSelectedRows();
                        },
                        grid: gridApi.grid,
                        // notifies grid of a configuration change so it re-adjusts. Pass one argument...could be "all","column","row","edit"
                        // See uiGridConstants.dataChange
                        notifyGridChange: scope.gridApi.core.notifyDataChange
                    };
                    scope.gridApi.selection.on.rowSelectionChanged(scope, function (row, evt) {
                        scope.rowSelectCallback({rowData: row.entity});
                    });

                    scope.gridApi.core.on.sortChanged(scope, function(grid, sortColumns) {
                        if (sortColumns.length == 0) {
                            paginationOptions.sort = null;
                        } else {
                            paginationOptions.sort = sortColumns[0].sort.direction;
                            scope.sortField = unformatFieldName(sortColumns[0].field);
                            fetchData();
                        }
                    });
                    
                    scope.gridApi.core.on.filterChanged(scope, function() {
                        if (filterTimeout) {
                            $timeout.cancel(filterTimeout);
                            filterTimeout = null;
                        }
                        
                        filterTimeout = $timeout(function ( ) {
                            var filters = [];
                            var filterObj = {};
                            for (var i = 0; i < scope.gridApi.grid.columns.length; i++) {
                                var col = scope.gridApi.grid.columns[i];
                                if (col.visible && col.filters[0].term) {
                                    var ob = {};
                                    ob[unformatFieldName(col.field)] = col.filters[0].term;
                                    filters.push(ob);
                                }
                            }
                            
                            var filterObj = {};
                            if (scope.urlFilter) {
                                filterObj.op = "and";
                                filterObj.data = [];
                                filterObj.data.push(JSON.parse(scope.urlFilter));
                                for (var k in filters) {
                                    var o = {};
                                    o.op = "contains";
                                    o.data = [];

                                    var field = Object.keys(filters[k])[0];
                                    o.data.push({op:"string", data:field, isCol:true});
                                    o.data.push({op:"string", data:filters[k][field], isCol:false});
                                    filterObj.data.push(o);
                                }
                            } else {
                                if (filters.length === 1) {
                                    var field = Object.keys(filters[0])[0];
                                    filterObj.op = "contains";
                                    filterObj.data = [];
                                    filterObj.data.push({op:"string", data:field, isCol:true});
                                    filterObj.data.push({op:"string", data:filters[0][field], isCol:false});
                                } else if (filters.length > 1) {
                                    filterObj.op = "and";
                                    filterObj.data = [];
                                    for (var k in filters) {
                                        var o = {};
                                        o.op = "contains";
                                        o.data = [];

                                        var field = Object.keys(filters[k])[0];
                                        o.data.push({op:"string", data:field, isCol:true});
                                        o.data.push({op:"string", data:filters[k][field], isCol:false});
                                        filterObj.data.push(o);
                                    }
                                }
                            }
                            
                            paginationOptions.pageNumber = 1; // Reset pagination page number on filter
                            if (filters.length > 0) {
                                scope.currentGridFilter = JSON.stringify(filterObj);
                            } else {
                                scope.currentGridFilter = "";
                            }
                            fetchData();
                        }, 500, false);
                    });
                    
                    gridApi.pagination.on.paginationChanged(scope, function (newPage, pageSize) {
                        paginationOptions.pageNumber = newPage;
                        paginationOptions.pageSize = pageSize;
                        fetchData();
                    });
                    
                    // Clean up before scope destroy
                    scope.$on('$destroy', function () {
                        $timeout.cancel(filterTimeout);
                    });
                }
            };
            
            function fetchData (callback) {
                var rangeBegin = (paginationOptions.pageNumber * paginationOptions.pageSize) - paginationOptions.pageSize;
                var rangeEnd = (rangeBegin + paginationOptions.pageSize) - 1;
                
                var url = getUrl();
                $http({
                    method: 'GET',
                    headers: {
                        'Range': rangeBegin + '-' + rangeEnd
                    },
                    url: url
                }).then(function (success) {
                    var responseHeaders = success.headers();
                    var contentRange = responseHeaders['content-range'];
                    var totalItems = 100;
                    if (contentRange) {
                        totalItems = contentRange.substring(contentRange.indexOf('/') + 1, contentRange.length);
                    }
                    
                    scope.gridOptions.totalItems = parseInt(totalItems);
                    scope.data = success.data;
                    scope.gridApi.selection.clearSelectedRows();
                    
                    if (callback) {
                        $timeout(function() {
                            callback();
                        }, 0);
                    }
                }, function (error) {
                    
                });
            }
            
            function getUrl () { // Get url that includes query filter
                var sortSymbol = '+';
                if (paginationOptions.sort === uiGridConstants.ASC) {
                    sortSymbol = '+';
                } else if (paginationOptions.sort === uiGridConstants.DESC) {
                    sortSymbol = '-';
                }
                
                // remove existing sort command from query string
                if (scope.sortField) {
                    scope.url = scope.url.replace(/(&?sort\((?:\+|\-)[\w\-\.]+\))/g, '');
                }
                
                var url = scope.url + (scope.url.indexOf('/?') === -1 ? '/?' : '') + (scope.sortField ? '&sort(' + sortSymbol + scope.sortField + ')' : '');
                if (scope.urlFilter) {
                    var ob = JSON.parse(scope.urlFilter);
                    var filterObj = ob;
                    if (scope.currentGridFilter) {
                        var ob1 = JSON.parse(scope.currentGridFilter);
                        if (ob.op === 'and') {
                            if (ob1.op === 'and') {
                                ob.data = ob.data.concat(ob1.data);
                            } else {
                                ob.data.push(ob1);
                            }
                        } else {
                            if (ob1.op === 'and') {
                                ob1.data.push(ob);
                                filterObj = ob1;
                            } else {
                                filterObj.op = 'and';
                                filterObj.data = [ob1,ob];
                            }
                        }
                    }
                    url = url + '&filter=' + JSON.stringify(filterObj);
                } else {
                    if (scope.currentGridFilter) {
                        url = url + '&filter=' + scope.currentGridFilter;
                    }
                }
                return url;
            }
            
            // Restore field names that originally contained unsupported characters (e.g. dots). These fields were formated in formatUnsupportedFieldName()
            function unformatFieldName (fieldName) {
                var result = fieldName;
                if (result.indexOf('____') !== -1) { // 4 underscores substituted dot.
                    result = result.replace(/____/g, '.')
                }
                return result;
            }
            
            // ui-grid does not support some characters (e.g. dots) in field names. Temporarily format unsupported field names into a supported form.
            function formatUnsupportedFieldName (fieldName) {
                var result = fieldName;
                if (result.indexOf('.') !== -1){
                    result = result.replace(/\./g, '____'); // 4 underscores
                }
                return result;
            }
            
            // After fetching data check for unsupported characters (e.g. dots) in field names
            function correctUnsupportedFieldNames (data) {
                angular.forEach(data, function(row, index){
                    angular.forEach(row, function(element, key){
                        if (key.indexOf('.') !== -1){
                            delete row[key];
                            var newKey = formatUnsupportedFieldName(key);
                            row[newKey] = element;
                        }
                    });
                    
                    // format function to be accessed from a grid row object by grid users
                    if (!row.formatUnsupportedFieldName) {
                        row.formatUnsupportedFieldName = formatUnsupportedFieldName;
                    }
                });
            }
            
            // Append additional properties to column Defs
            scope.$watchCollection('columnDefs', function (value) {
                if (value) {
                    for (var j = 0; j < scope.columnDefs.length; j++) {
                        // check for unsupported field names in column definitions
                        var field = scope.columnDefs[j].field;
                        scope.columnDefs[j].field = formatUnsupportedFieldName(field);
                        
                        // overried default sort cycle: [null, ASC, DESC]
                        scope.columnDefs[j].sortDirectionCycle = [uiGridConstants.DESC, uiGridConstants.ASC];
                        
                        scope.columnDefs[j].filter = {
                            placeholder: '🔍' // magnifying glass icon for filter placeholder. U+1F50D (left tilting) or U+1F50E (right tilting)
                        };
                    }
                }
            });

            scope.$watchCollection('data', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    correctUnsupportedFieldNames(newValue);
                    scope.gridApi.grid.options.data = newValue;
                }
            });
            
            if (scope.url) {
                fetchData(); // Initial data load
            }
        },
        controller: function ($scope, $element) {
            $scope.getGridHeight = function () {
                // ui-grid does not auto adjust height based on number of displayed rows. We need to manually calcuate height.
                // In this case we count number of displayed rows, multiply that by default row height, and add necessary offsets to account for things like filter bar, pagination bar, etc.
                var rows = 2;
                if ($scope.gridApi && $scope.columnDefs.length) {
                    rows = $element.find('div.grid' + $scope.gridApi.grid.id + ' > div.ui-grid-contents-wrapper > div.ui-grid-render-container-body > div.ui-grid-viewport > div.ui-grid-canvas > div.ui-grid-row').length;
                }
                var offset = ($scope.gridApi && $scope.gridApi.grid.options.enableFiltering) ? 110 : 80;
                return ((rows * 30) + offset);
            };
        }
    };
}]);

angular.module('angular-dojo').directive('crisRadioButtonGroup', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            readOnly: "@",
            name: "@",
            value: "@",
            ngModel: "=",
            items: "@",
            orientation: "@"
        },
        template: '<div> \
                        <div ng-if="orientation===\'horizontal\'" class="form-horizontal"> \
                            <span ng-repeat="option in options track by $index"> \
                                <input name="{{name}}" type="radio" value="{{option.value}}" ng-checked="option.value==defaultValue" ng-click="clicked(option, $event)" class="radio-inline" ng-disabled="readOnly===\'true\'" />&nbsp;{{option.name}}&nbsp;&nbsp; \
                            </span> \
                        </div> \
                        <div ng-if="orientation===\'vertical\'" ng-repeat="option in options track by $index" class="form-horizontal" style="padding-bottom:5px;"> \
                            <input name="{{name}}" type="radio" value="{{option.value}}" ng-checked="option.value==defaultValue" ng-click="clicked(option, $event)" class="radio-inline" ng-disabled="readOnly===\'true\'" />&nbsp;{{option.name}}&nbsp;&nbsp; \
                        </div> \
                    </div>',
        link: function (scope, element, attrs) {
            var items = dojo.fromJson(scope.items);
            scope.options = [];
            for (var key in items) {
                scope.options.push({name: key, value: items[key]});
            }
            
            scope.defaultValue;
            if (typeof scope.ngModel !== 'undefined') {
                scope.defaultValue = scope.ngModel;
            } else if (scope.value) {
                scope.defaultValue = JSON.parse(scope.value);
            } else {
                scope.defaultValue = 0;
            }
            
            scope.$watch('ngModel', function (value) {
                if (typeof value !== 'undefined') {
                    scope.defaultValue = scope.ngModel;
                }
            });
        },
        controller: function($scope) {
            $scope.clicked = function (option, evt) {
                $scope.ngModel = option.value;
                evt.stopPropagation();
            };
        }
    };
});

angular.module('angular-dojo').directive('crisCheckboxGroup', function($compile) {
    return {
        restrict: "E",
        replace: true,
        transclude: true,
        scope: {
            readOnly: "@",
            //name: "@",
            value: "@",
            ngModel: "=",
            items: "@",
            orientation: "@"
        },
        template: '<div> \
                        <div ng-if="orientation===\'horizontal\'" class="navbar-btn form-horizontal"> \
                            <span ng-repeat="option in options track by $index"> \
                                <input type="checkbox" value="{{option.value}}" ng-checked="defaultValues.indexOf(option.value) > -1" ng-click="clicked(option, $event)" ng-disabled="readOnly===\'true\'" class="checkbox-inline" />&nbsp;{{option.name}}&nbsp;&nbsp; \
                            </span> \
                        </div> \
                        <div ng-if="orientation===\'vertical\'" ng-repeat="option in options track by $index" class="form-horizontal" style="padding-bottom:5px;"> \
                            <input type="checkbox" value="{{option.value}}" ng-checked="defaultValues.indexOf(option.value) > -1" ng-click="clicked(option, $event)" ng-disabled="readOnly===\'true\'" class="checkbox-inline" />&nbsp;{{option.name}} \
                        </div> \
                    </div>',
        link: function (scope, element, attrs) {
            // scope.items = scope.items.replace(/ /g, '').replace(/([\w\-\.]+)(?=(\:|\}))/g, '"$1"').replace(/'/g, '"');
            scope.$watchCollection('items', function (newItems) {
                if (typeof newItems !== 'undefined' && newItems !== null && newItems !== '') {
                    var items = dojo.fromJson(scope.items);
                    scope.options = [];
                    for (var key in items) {
                        scope.options.push({name: key, value: items[key]});
                    }

                    scope.defaultValues;
                    if (typeof scope.ngModel !== 'undefined') {
                        scope.defaultValues = scope.ngModel;
                    } else if (scope.value) {
                        scope.defaultValues = JSON.parse(scope.value);
                    } else {
                        scope.defaultValues = [];
                    }
                }
            });
            
            scope.$watch('ngModel', function (value, oldValue) {
                if (typeof value !== 'undefined' && value !== null) {
                    scope.defaultValues = scope.ngModel;
                } else if (scope.value) {
                    scope.defaultValues = JSON.parse(scope.value);
                } else {
                    scope.defaultValues = [];
                }
            });
        },
        controller: function($scope) {
            $scope.clicked = function (option, evt) {
                var isChecked = evt.currentTarget.checked;
                if (isChecked && $scope.ngModel.indexOf(option.value) === -1) {
                    $scope.ngModel.push(option.value);
                } else {
                    var idx = $scope.ngModel.indexOf(option.value);
                    $scope.ngModel.splice(idx, 1);
                }
                evt.stopPropagation();
            };
        }
    };
});

angular.module('angular-dojo').directive('crisTriStateCheckBox', ["$compile", "$parse", function($compile, $parse) {
    return {
        restrict: 'E',
        require: 'ngModel',
        scope: {
            ngModel: "=",
            disabled: "="
        },
        link: function (scope, element, attrs) {
            var elem = element[0];

            require(["dojox/form/TriStateCheckBox", "dojo/on"], function(TriStateCheckBox, on) {
                function setCheckState(value) {
                    if (value === true) {
                        checkbox.set("checked", true);
                    } else if (value === false) {
                        checkbox.set("checked", false);
                    } else if (value === null) {
                        checkbox.set("checked", "mixed");
                    }
                }

                function setModelValue(value) {
                    if (value === true) {
                        scope.ngModel = true;
                    } else if (value === false) {
                        scope.ngModel = false;
                    } else if (value === 'mixed') {
                        scope.ngModel = null;
                    }
                    scope.$apply();
                }

                /***********************************
                 * create and initialize the widget
                 ***********************************/
                var dojoProps = {states: ['mixed', true, false]};
                if (attrs.name) {
                    dojoProps.name = attrs.name;
                }
                if ("disabled" in attrs) {
                    dojoProps.disabled = attrs.disabled;
                }
                var checkbox = new TriStateCheckBox(dojoProps, elem);
                setCheckState(scope.ngModel);
                checkbox.startup();

                // on widget state change
                on(checkbox, "change", function(value) {
                    setModelValue(value);
                    scope.$apply();
                });

                // on model value change
                scope.$watch("ngModel", setCheckState);

                scope.$watch("disabled", function(value) {
                    if (value === true) {
                        checkbox.set("disabled", true);
                    } else {
                        checkbox.set("disabled", false);
                    }
                });
            });
        }
    };
}]);
