require([
    "dojox/uuid/generateRandomUuid"
]);

if (!String.prototype.trim) {
    String.prototype.trim = function () {
        return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    };
}

angular.module('angular-dojo', []);

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
                    
                    if (dojoPropsNew.required === true) {
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
                            if (typeof child.item.isLatest !== 'undefined' && !child.item.isLatest && !isAttachTo) {
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
            removable: "="
        },
        template: "<span ng-show='item'><img class='inlineIcon' src='" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/tick.png' />&nbsp;</span><a href='{{buildDownLoadLink(item)}}'>{{item}}</a>",
        link: function(scope, element, attrs) {

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
            removable: "="
        },
        template: "<div ng-repeat='item in items track by $index'>\n\
                        <img class='inlineIcon' src='" + cris.imagesRoot + "/famfamfam_silk_icons_v013/icons/tick.png' />\n\
                        <span>\n\
                            {{$index}}:&nbsp;\n\
                            <cris-remove-button data-ng-show='removable' items='items' item='item' index='$index'><cris-remove-button>\n\
                        </span>&nbsp;\n\
                        <a href='{{buildDownLoadLink(item)}}'>{{item}}</a>\n\
                    </div>",
        link: function(scope, element, attrs) {

        },
        controller: function($scope) {
            $scope.buildDownLoadLink = function(storageFile) {
                var link = cris.baseUrl + "download/" + storageFile;
                return link;
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
                if (scope.readOnly !== "true") {
                    on(widget, "change", function (value) {
                       if ((value === undefined) || (value !== value)) {
                            scope.item = null;
                        } else {
                            scope.item = value;
                        }
                        scope.$apply();
                    });
                }

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
            items: "@",
            item: '=',
            ngBlur: '&'
        },
        link: function (scope, element, attrs) {
            console.log("======== cris-one-check-box: link ========");
            var elem = element[0];

            require(["dijit/dijit", "dijit/form/CheckBox", "dojo/on"], function(dijit, CheckBox, on) {
                var widget = new CheckBox({
                    disabled: (scope.readOnly === "true" ? true : false),
                    value: scope.item,
                    checked: scope.isChecked(),
                    onBlur: scope.ngBlur ? scope.ngBlur : null
                }, elem);
                widget.startup();

                /****************
                 * view -> model
                 ****************/
                if (scope.readOnly !== "true") {
                    on(widget, "change", function (value) {
                        scope.setItem(value);
                        scope.$apply();
                    });
                }

                /****************
                 * model -> view
                 ****************/
                scope.$watch('item', function (item) {
                    widget.set('value', item);
                    widget.set('checked', scope.isChecked());
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
            title: '@'
        },
        template: '<div>\n\
                    <cris-uploader id="{{id}}" title="{{title}}" label="{{labelText}}" placeholder="{{placeholder}}" allow-multiple="{{allowMultiple}}" submit-url="{{submitUrl}}">\n\
                        <!-- -->\n\
                    </cris-uploader>\n\
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