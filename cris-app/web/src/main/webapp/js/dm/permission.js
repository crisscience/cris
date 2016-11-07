/* global dojo, dijit, dojox, cris */

angular.module("permission", ["angular-dojo"]);

angular.module("permission").constant("PERMISSION", {
    urlPermissions: cris.baseUrl + "permissions",
    urlProjects: cris.baseUrl + "projects",
    urlExperiments: cris.baseUrl + "experiments",
    urlWorkflows: cris.baseUrl + "workflows",
    urlTools: cris.baseUrl + "tools",
    urlUsers: cris.baseUrl + "users",
    urlGroups: cris.baseUrl + "groups"
});

angular.module("permission").config([function() {
    cris.require([
        "dojo/data/ObjectStore",
        "dojo/store/Memory",
        "dijit/Dialog",
        "dijit/form/FilteringSelect",
        "dijit/form/Form",
        "dijit/form/Button",
        "dijit/layout/TabContainer"
    ]);
}]);

angular.module("permission").factory('crisPermissionService', ["PERMISSION", function(PERMISSION) {
    return {

        objectClassToUrl: function(objectClass) {
            var url = null;
            if (objectClass === "Project") {
                url = PERMISSION.urlProjects;
            } else if (objectClass === "Experiment") {
                url = PERMISSION.urlExperiments;
            } else if (objectClass === "Workflow") {
                url = PERMISSION.urlWorkflows;
            } else if (objectClass === "Tool") {
                url = PERMISSION.urlTools;
            }
            return url;
        },

        getObjects: function(objectClass) {
            var url = this.objectClassToUrl(objectClass);
            return dojo.xhrGet({
                url: url,
                handleAs: "json"
            });
        },

        getPermission: function(userId, groupId, objectClass, objectIds) {
            return dojo.xhrGet({
                url: PERMISSION.urlPermissions + "?sId=" + (groupId ? groupId : userId) + "&group=" + (groupId ? true : false) + "&objectClass=" + objectClass + "&objectIds=" + objectIds,
                handleAs: "json"
            });
        },

        getGroupPermissionByUser: function(userId, objectClass, objectId) {
            return dojo.xhrGet({
                url: PERMISSION.urlPermissions + "/groups?userId=" + userId + "&objectClass=" + objectClass + "&objectId=" + objectId,
                handleAs: "json"
            });
        },

        getUserPermissionByGroup: function(groupId, objectClass, objectId) {
            return dojo.xhrGet({
                url: PERMISSION.urlPermissions + "/users?groupId=" + groupId + "&objectClass=" + objectClass + "&objectId=" + objectId,
                handleAs: "json"
            });
        },

        savePermission: function(userId, groupId, objectClass, objectId, permission) {
            return dojo.xhrPost({
                url: PERMISSION.urlPermissions,
                headers: {"Content-Type": "application/json", "Accept": "application/json"},
                postData: dojo.toJson({"sId": (groupId ? groupId : userId), "group": (groupId ? true: false), "objectId": objectId, "objectClass": objectClass, "read": permission.read, "update": permission.update, "create": permission.create, "delete": permission.delete, "execute": permission.execute, "owner": permission.owner}),
                handleAs: "json"
            });
        },

        deletePermission: function(userId, groupId, objectClass, objectId) {
            return dojo.xhrDelete({
                url: PERMISSION.urlPermissions,
                headers: {"Content-Type": "application/json", "Accept": "application/json"},
                postData: dojo.toJson({"sId": (groupId ? groupId : userId), "group": (groupId ? true: false), "objectId": objectId, "objectClass": objectClass}),
                handleAs: "json"
            });
        }
    };
}]);

angular.module("permission").controller("crisPermissionController", ["$scope", "crisPermissionService", "PERMISSION", function($scope, crisPermissionService, PERMISSION) {

    cris.ready(function() {

        var layoutUsers = [{cells: [
                    {field: 'id', name: 'ID', datatype: "number", width: '50px', hidden: true},
                    {field: 'lastName', name: 'Last Name', width: 'auto'},
                    {field: 'firstName', name: 'First Name', width: 'auto'},
                    {field: 'username', name: 'User Name', width: 'auto'}
                ]}];

        var layoutGroups = [{cells: [
                    {field: 'id', name: 'ID', datatype: "number", width: '50px', hidden: true},
                    {field: 'name', name: 'Name', width: 'auto'}
                ]}];

        var layout = [{cells: [
                    {field: 'id', name: 'ID', width: '40px', hidden: true},
                    {field: 'name', name: 'Name', width: 'auto'},
                    {field: 'create', name: 'Create', width: '50px', styles: 'text-align: center;', get: formatterCheckBox},
                    {field: 'read', name: 'Read', width: '50px', styles: 'text-align: center;', get: formatterCheckBox},
                    {field: 'update', name: 'Update', width: '55px', styles: 'text-align: center;', get: formatterCheckBox},
                    {field: 'delete', name: 'Delete', width: '50px', styles: 'text-align: center;', get: formatterCheckBox},
                    {field: 'execute', name: 'Use', width: '45px', styles: 'text-align: center;', get: formatterCheckBox},
                    {field: 'owner', name: 'Owner', width: '50px', styles: 'text-align: center;', get: formatterCheckBox}
                ]}];

        var layoutAdmin = [{cells: [
                    {field: 'id', name: 'ID', width: '40px', hidden: true},
                    {field: 'name', name: 'Name', width: 'auto'},
                    {field: 'create', name: 'Create', width: '50px', styles: 'text-align: center;', get: formatterCheckBox},
                    {field: 'read', name: 'Read', width: '50px', styles: 'text-align: center;', get: formatterCheckBox},
                    {field: 'update', name: 'Update', width: '55px', styles: 'text-align: center;', get: formatterCheckBox},
                    {field: 'delete', name: 'Delete', width: '50px', styles: 'text-align: center;', get: formatterCheckBox}
                ]}];

        function formatterCheckBox(row, item) {
            return new dijit.form.CheckBox({value: item[this.field], checked: item[this.field], disabled: true});
        }

        var asc = true;
        var args = {
            pageSizes: [],
            autoHeight: 100,
            rowsPerPage: 10
        };

        // user grid
        args.sortFields = [{attribute: "lastName", descending: !asc}];
        args.query = {enabled: true};
        var storeUsers = createJsonRestStore(PERMISSION.urlUsers);
        var gridUsers = createGrid(storeUsers, layoutUsers, "idGridUsers", args);
        gridUsers.setSortIndex(1, asc);
        gridUsers.onRowClick = function(e) {
            onSelectSubject();
        };

        // group grid
        args.sortFields = [{attribute: "name", descending: !asc}];
        var storeGroups = createJsonRestStore(PERMISSION.urlGroups);
        var gridGroups = createGrid(storeGroups, layoutGroups, "idGridGroups", args);
        delete args.query;
        gridGroups.setSortIndex(1, asc);
        gridGroups.onRowClick = function(e) {
            onSelectSubject();
        };

        // user/group tab container
        var tabContainer = dijit.byId("idTabContainer");
        tabContainer.watch("selectedChildWidget", function(name, oval, nval){
            var tabName = nval.get("id");
            switch (tabName) {
                case "idTabUsers":
                    onSelectSubject();
                    break;
                case "idTabGroups":
                    onSelectSubject();
                    break;
            }
        });

        // permission grid
        var objectStore = new dojo.store.Memory({data: []});
        var store = new dojo.data.ObjectStore({objectStore: objectStore});
        args.sortFields = [{attribute: "name", descending: !asc}];
        var gridPermissions = createGrid(store, layout, "idGridPermissions", args);
        gridPermissions.setSortIndex(1, asc);
        gridPermissions.onCellDblClick = function(e) {
            onEditPermission(e, false);
        };

        // admin permission grid
        var objectStoreAdmin = new dojo.store.Memory({data: []});
        var storeAdmin = new dojo.data.ObjectStore({objectStore: objectStoreAdmin});
        args.sortFields = [{attribute: "name", descending: !asc}];
        var gridPermissionsAdmin = createGrid(storeAdmin, layoutAdmin, "idGridPermissionsAdmin", args);
        gridPermissionsAdmin.setSortIndex(1, asc);
        gridPermissionsAdmin.onCellDblClick = function(e) {
            onEditPermission(e, true);
        };

        // object class selection
        dojo.connect(dijit.byId("idSelectObjectClass"), "onChange", function(value) {
            var className = value;
            if (className) {
                crisPermissionService.getObjects(className).then(function(objects) {
                    objectStore = new dojo.store.Memory({data: []});
                    for (var i = 0; objects.length > i; i++) {
                        objectStore.put({id: objects[i].id, name: objects[i].name});
                    }
                    store = new dojo.data.ObjectStore({objectStore: objectStore});
                    gridPermissions.setStore(store);
                    gridPermissions.setQuery({});

                    getPermission(className, false);
                }, function(error) {
                    showMessage(error);
                });
            } else {
                objectStore = new dojo.store.Memory({data: []});
                store = new dojo.data.ObjectStore({objectStore: objectStore});
                gridPermissions.setStore(store);
                gridPermissions.setQuery({});
            }
        });

        // admin object class selection
        dojo.connect(dijit.byId("idSelectObjectClassAdmin"), "onChange", function(value) {
            objectStoreAdmin = new dojo.store.Memory({data: []});
            storeAdmin = new dojo.data.ObjectStore({objectStore: objectStoreAdmin});
            var className = value;
            if (className) {
                var name = "ALL-" + className + "s";
                if (className === "Project") {
                    name += "/Experiments";
                }
                objectStoreAdmin.put({id: 0, name: name});
            }
            gridPermissionsAdmin.setStore(storeAdmin);
            gridPermissionsAdmin.setQuery({});
            getPermission(className, true);
        });

        // permission edit dialog
        dojo.connect(dijit.byId("save"), "onClick", function(evt) {
            savePermission();
        });
        dojo.connect(dijit.byId("cancel"), "onClick", function(evt) {
            dialogPermissions.hide();
        });

        function updateObjectPermission(objectPermissions, isAdminType) {
            var store = isAdminType ? objectStoreAdmin : objectStore;

            for (var objectId in objectPermissions) {
                var objectPermission = store.get(objectId);
                objectPermission.create = objectPermissions[objectId]["create"];
                objectPermission.read = objectPermissions[objectId]["read"];
                objectPermission.update = objectPermissions[objectId]["update"];
                objectPermission.delete = objectPermissions[objectId]["delete"];
                objectPermission.execute = objectPermissions[objectId]["execute"];
                objectPermission.owner = objectPermissions[objectId]["owner"];
                objectPermission.inheritFromGroup = objectPermissions[objectId]["inheritFromGroup"];
            }

            if (isAdminType) {
                gridPermissionsAdmin.setQuery({});
            } else {
                gridPermissions.setQuery({});
            }
        }

        function isUser(name) {
            var yes = false;
            var selectedUsers = dijit.byId("idTabUsers").selected;
            if (selectedUsers) {
                if (gridUsers.selection.getSelected("row")[0] !== undefined) {
                    var userItem = gridUsers.selection.getSelected("row")[0];
                    var userName = userItem.username;
                    if (userName === name) {
                        yes = true;
                    }
                }
            }
            return yes;
        }

        function isGroup(name) {
            var yes = false;
            var selectedGroups = dijit.byId("idTabGroups").selected;
            if (selectedGroups) {
                if (gridGroups.selection.getSelected("row")[0] !== undefined) {
                    var groupItem = gridGroups.selection.getSelected("row")[0];
                    var groupName = groupItem.name;
                    if (groupName === name) {
                        yes = true;
                    }
                }
            }
            return yes;
        }

        function isPublicUser() {
            return isUser("public");
        }

        function isAdminUser() {
            return isUser("administrator");
        }

        function isAdminGroup() {
            return isGroup("Admin Group");
        }

        function onSelectSubject() {
            var objectClass = dijit.byId("idSelectObjectClass").get("value");
            if (objectClass) {
                getPermission(objectClass, false);
            }

            objectClass = dijit.byId("idSelectObjectClassAdmin").get("value");
            if (objectClass) {
                getPermission(objectClass, true);
            }
        }

        function getPermission(objectClass, isAdminType) {
            if (!objectClass) {
                return;
            }

            var selectedUsers = dijit.byId("idTabUsers").selected;
            var selectedGroups = dijit.byId("idTabGroups").selected;

            var userId;
            var groupId;
            if (selectedUsers) {
                userId = gridUsers.selection.getSelected("row")[0] === undefined ? null : gridUsers.selection.getSelected("row")[0].id;
                groupId = null;
            } else if (selectedGroups) {
                userId = null;
                groupId = gridGroups.selection.getSelected("row")[0] === undefined ? null : gridGroups.selection.getSelected("row")[0].id;
            }

            if (userId || groupId) {
                var objectIds = "";
                if (!isAdminType) {
                    var objects = objectStore.query({});
                    for (var i = 0; objects.length > i; i++) {
                        objectIds = objectIds + "," + objects[i].id;
                    }
                } else {
                    objectIds = ",0";
                }

                crisPermissionService.getPermission(userId, groupId, objectClass, objectIds).then(function(data) {
                    updateObjectPermission(data, isAdminType);
                }, function(error) {
                    showMessage(error);
                });
            } else {
                // if no previous permission set, do nothing; otherwise clear permission
                var store = isAdminType ? objectStoreAdmin : objectStore;
                store.query(function(item) {
                    item.create = null;
                    item.read = null;
                    item.update = null;
                    item.delete = null;
                    item.execute = null;
                    item.owner = null;
                });
                if (isAdminType) {
                    gridPermissionsAdmin.setQuery({});
                } else {
                    gridPermissions.setQuery({});
                }
            }
        }

        function onEditPermission(e, isAdminType) {
            var permission = e.grid.getItem(e.rowIndex);

            var publicUser = isPublicUser();
            var adminUser = isAdminUser();
            var adminGroup = isAdminGroup();

            var objectClass = isAdminType ? dijit.byId("idSelectObjectClassAdmin").get("value") : dijit.byId("idSelectObjectClass").get("value");
            var objectId = permission.id;

            if (adminUser || adminGroup || (isAdminType && publicUser)) {
                // these users/groups should change the permission
                dijit.byId("save").set("disabled", true);
            } else {
                // everyone else
                dijit.byId("save").set("disabled", false);
            }

            var isUser = dijit.byId("idTabUsers").selected;
            var user;
            var group;

            var deferred;
            if (isUser) {
                user = gridUsers.selection.getSelected("row")[0];
                user.isAdmin = adminUser;
                group = null;
                deferred = crisPermissionService.getGroupPermissionByUser(user.id, objectClass, objectId);
            } else {
                user = null;
                group = gridGroups.selection.getSelected("row")[0];
                group.isAdmin = adminGroup;
                deferred = crisPermissionService.getUserPermissionByGroup(group.id, objectClass, objectId);
            }

            deferred.then(function(data) {
                $scope.isUser = isUser;
                $scope.isAdminType = isAdminType;

                // gather all the information needed
                // fill user/group info
                if ($scope.isUser) {
                    // userPermission.user
                    $scope.userPermission.user = user;
                    $scope.userPermission.user.type = adminUser ? "admin" : (publicUser ? "public" : "user");
                    // userPermission.permission
                    $scope.userPermission.permission.create = permission.create;
                    $scope.userPermission.permission.read = permission.read;
                    $scope.userPermission.permission.update = permission.update;
                    $scope.userPermission.permission.delete = permission.delete;
                    $scope.userPermission.permission.execute = permission.execute;
                    $scope.userPermission.permission.owner = permission.owner;

                    // groupsPermission
                    $scope.groupsPermission = data;

                    $scope.userPermission.inheritFromGroup = permission.inheritFromGroup;
                } else {
                    // groupPermission.group
                    $scope.groupPermission.group = group;
                    $scope.groupPermission.group.type = adminGroup ? "admin" : "user";

                    // groupPermission.permission
                    $scope.groupPermission.permission = permission;

                    // usersPermission: this is the "raw" permission of the users not the effective permission
                    $scope.usersPermission = data;
                }

                $scope.resource = {id: objectId, type: objectClass, name: permission.name};

                $scope.$apply();

                // do the edit
                dialogPermissions.show();
            }, function(error) {
                showMessage(error);
            });
        }

        function savePermission() {
            var selectedGroups = dijit.byId("idTabGroups").selected;
            var isGroup = selectedGroups === true ? true : false;

            var isAdminType = $scope.isAdminType;
            var userId, groupId, objectClass, objectId, permission;
            if (isGroup) {
                var countGroups = gridGroups.selection.getSelectedCount("row");
                if (countGroups === 0) {
                    showMessage("You must pick a group");
                    return;
                }
                userId = null;
                groupId = gridGroups.selection.getSelected("row")[0].id;
                permission = $scope.groupPermission.permission;
            } else {
                var countUsers = gridUsers.selection.getSelectedCount("row");
                if (countUsers === 0) {
                    showMessage("You must pick a user");
                    return;
                }
                userId = gridUsers.selection.getSelected("row")[0].id;
                groupId = null;
                permission = $scope.userPermission.permission;
            }

            if (permission.create === true || permission.update === true || permission.delete === true) {
                // force read flag
                permission.read = true;
            }
            if (!isAdminType) {
                if (permission.read === true || permission.owner === true) {
                    // force execute flag
                    permission.execute = true;
                }
            }

            var grid;
            if (isAdminType) {
                grid = gridPermissionsAdmin;
                objectClass = dijit.byId("idSelectObjectClassAdmin").get("value");
            } else {
                grid = gridPermissions;
                objectClass = dijit.byId("idSelectObjectClass").get("value");
            }
            objectId = grid.selection.getSelected("row")[0].id;

            if (!isGroup && $scope.userPermission.inheritFromGroup) {
                crisPermissionService.deletePermission(userId, groupId, objectClass, objectId).then(function(response) {
                    var objectPermission = isAdminType ? objectStoreAdmin.get(objectId) : objectStore.get(objectId);
                    objectPermission.create = $scope.effectivePermission.create;
                    objectPermission.read = $scope.effectivePermission.read;
                    objectPermission.update = $scope.effectivePermission.update;
                    objectPermission.delete = $scope.effectivePermission.delete;
                    objectPermission.execute = $scope.effectivePermission.execute;
                    objectPermission.owner = $scope.effectivePermission.owner;
                    objectPermission.inheritFromGroup = $scope.userPermission.inheritFromGroup;

                    grid.setQuery({});
                }, function(error) {
                    showMessage(error);
                });
            } else {
                // replace null with false
                permission.create = (permission.create === null ? false : permission.create);
                permission.read = (permission.read === null ? false : permission.read);
                permission.update = (permission.update === null ? false : permission.update);
                permission.delete = (permission.delete === null ? false : permission.delete);
                permission.execute = (permission.execute === null ? false : permission.execute);
                permission.owner = (permission.owner === null ? false : permission.owner);
                crisPermissionService.savePermission(userId, groupId, objectClass, objectId, permission).then(function(response) {
                    var objectPermission = isAdminType ? objectStoreAdmin.get(objectId) : objectStore.get(objectId);
                    objectPermission.create = permission.create;
                    objectPermission.read = permission.read;
                    objectPermission.update = permission.update;
                    objectPermission.delete = permission.delete;
                    objectPermission.execute = permission.execute;
                    objectPermission.owner = permission.owner;
                    objectPermission.inheritFromGroup = $scope.userPermission.inheritFromGroup;

                    grid.setQuery({});
                }, function(error) {
                    showMessage(error);
                });
            }
        }

    });

    //////////////////////////////////////////////////////////////////////////////////////////
    // all the code above is move from permissions/index.jsp
    //  * isUser
    //  * isAdminType
    //  * userPermission
    //  * groupsPermission
    //  * groupPermission
    //  * usersPermission
    //  * resource
    //////////////////////////////////////////////////////////////////////////////////////////
    $scope.isUser;
    $scope.isAdminType;

    // for user permission
    $scope.userPermission = {user: {}, permission: {}};
    $scope.effectivePermission = {};
    $scope.groupsPermission = [];

    // for group permission
    $scope.groupPermission = {group: {}, permission: {}};
    $scope.usersPermission = [];

    // resource type
    $scope.resource = {}; // = {id: 1, type: "dataset", name: "project 1"};
}]);

angular.module("permission").directive("crisUserPermissionEditor", ["crisPermissionService", function(crisPermissionService) {
    return {
        restrict: "E",
        scope: {
            groupsPermission: "=",
            userPermission: "=",
            effectivePermission: "=",
            resource: "="
        },
        template: '\
            <h3>User Permission for {{resource.name}}</h3>\n\
            <h4>Permission from Member Groups</h4>\n\
            <div data-ng-repeat="groupPermission in groupsPermission">\n\
            <table>\n\
                <tr>\n\
                    <td></td>\n\
                    <td>C</td>\n\
                    <td>R</td>\n\
                    <td>U</td>\n\
                    <td>D</td>\n\
                    <td data-ng-if="resource.id !== 0">E</td>\n\
                    <td data-ng-if="resource.id !== 0">O</td>\n\
                </tr>\n\
                <tr>\n\
                    <td><label>{{groupPermission.group.name}}: </label></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="groupPermission.permission.create" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="groupPermission.permission.read" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="groupPermission.permission.update" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="groupPermission.permission.delete" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box items="[false, true]" item="groupPermission.permission.execute" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box items="[false, true]" item="groupPermission.permission.owner" disabled="true"><!----></cris-one-check-box></td>\n\
                </tr>\n\
            </table>\n\
            </div>\n\
            <h4>User Permission</h4>\n\
            <table>\n\
                <tr>\n\
                    <td></td>\n\
                    <td>C</td>\n\
                    <td>R</td>\n\
                    <td>U</td>\n\
                    <td>D</td>\n\
                    <td data-ng-if="resource.id !== 0">E</td>\n\
                    <td data-ng-if="resource.id !== 0">O</td>\n\
                </tr>\n\
                <tr>\n\
                    <td><label>{{userPermission.user.firstName}}: </label></td>\n\
                    <td><cris-one-check-box disabled="{{userPermission.inheritFromGroup}}" items="[false, true]" item="userPermission.permission.create"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box disabled="{{userPermission.inheritFromGroup}}" items="[false, true]" item="userPermission.permission.read"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box disabled="{{userPermission.inheritFromGroup}}" items="[false, true]" item="userPermission.permission.update"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box disabled="{{userPermission.inheritFromGroup}}" items="[false, true]" item="userPermission.permission.delete"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box disabled="{{userPermission.inheritFromGroup}}" items="[false, true]" item="userPermission.permission.execute"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box disabled="{{userPermission.inheritFromGroup}}" items="[false, true]" item="userPermission.permission.owner"><!----></cris-one-check-box></td>\n\
                    <td>&nbsp;Inherit from Group(s): <cris-one-check-box disabled="{{userPermission.user.isAdmin}}" items="[false, true]" item="userPermission.inheritFromGroup"><!----></cris-one-check-box></td>\n\
                </tr>\n\
            </table>\n\
            <h4>Effective Permission: {{testname}}</h4>\n\
            <table>\n\
                <tr>\n\
                    <td></td>\n\
                    <td>C</td>\n\
                    <td>R</td>\n\
                    <td>U</td>\n\
                    <td>D</td>\n\
                    <td data-ng-if="resource.id !== 0">E</td>\n\
                    <td data-ng-if="resource.id !== 0">O</td>\n\
                </tr>\n\
                <tr>\n\
                    <td><label>{{userPermission.user.firstName}}: </label></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="effectivePermission.create" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="effectivePermission.read" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="effectivePermission.update" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="effectivePermission.delete" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box items="[false, true]" item="effectivePermission.execute" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box items="[false, true]" item="effectivePermission.owner" disabled="true"><!----></cris-one-check-box></td>\n\
                </tr>\n\
            </table>\n\
            ',
        link: function (scope, element, attrs) {
            scope.$watch("userPermission.permission", function(permission) {
                scope.effectivePermission.create = permission.create;
                scope.effectivePermission.read = permission.read;
                scope.effectivePermission.update = permission.update;
                scope.effectivePermission.delete = permission.delete;
                scope.effectivePermission.execute = permission.execute;
                scope.effectivePermission.owner = permission.owner;
            }, true);

            scope.$watch("userPermission.inheritFromGroup", function(inherit) {
                scope.effectivePermission.create = (!inherit ? scope.userPermission.permission.create : scope.getGroupPermission(scope.groupsPermission, "create"));
                scope.effectivePermission.read = (!inherit ? scope.userPermission.permission.read : scope.getGroupPermission(scope.groupsPermission, "read"));
                scope.effectivePermission.update = (!inherit ? scope.userPermission.permission.update : scope.getGroupPermission(scope.groupsPermission, "update"));
                scope.effectivePermission.delete = (!inherit ? scope.userPermission.permission.delete : scope.getGroupPermission(scope.groupsPermission, "delete"));
                scope.effectivePermission.execute = (!inherit ? scope.userPermission.permission.execute : scope.getGroupPermission(scope.groupsPermission, "execute"));
                scope.effectivePermission.owner = (!inherit ? scope.userPermission.permission.owner : scope.getGroupPermission(scope.groupsPermission, "owner"));
            });
        },
        controller: ["$scope", function($scope) {
            $scope.getGroupPermission = function(groupsPermission, op) {
                var permission = false;
                angular.forEach(groupsPermission, function(groupPermission, idx) {
                    permission = permission || groupPermission.permission[op];
                });
                return permission;
            };
        }]
    };
}]);

angular.module("permission").directive("crisGroupPermissionEditor", ["crisPermissionService", function(crisPermissionService) {
    return {
        restrict: "E",
        scope: {
            groupPermission: "=",
            usersPermission: "=",
            resource: "="
        },
        template: '\
            <h3>Group Permission for {{resource.name}}</h3>\n\
            <h4>Group Permission</h4>\n\
            <table>\n\
                <tr>\n\
                    <td></td>\n\
                    <td>C</td>\n\
                    <td>R</td>\n\
                    <td>U</td>\n\
                    <td>D</td>\n\
                    <td data-ng-if="resource.id !== 0">E</td>\n\
                    <td data-ng-if="resource.id !== 0">O</td>\n\
                </tr>\n\
                <tr>\n\
                    <td><label>{{groupPermission.group.name}}: </label></td>\n\
                    <td><cris-one-check-box disabled="{{groupPermission.group.isAdmin}}" items="[false, true]" item="groupPermission.permission.create"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box disabled="{{groupPermission.group.isAdmin}}" items="[false, true]" item="groupPermission.permission.read"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box disabled="{{groupPermission.group.isAdmin}}" items="[false, true]" item="groupPermission.permission.update"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box disabled="{{groupPermission.group.isAdmin}}" items="[false, true]" item="groupPermission.permission.delete"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box disabled="{{groupPermission.group.isAdmin}}" items="[false, true]" item="groupPermission.permission.execute"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box disabled="{{groupPermission.group.isAdmin}}" items="[false, true]" item="groupPermission.permission.owner"><!----></cris-one-check-box></td>\n\
                </tr>\n\
            </table>\n\
            <h4>Permission from Member Users</h4>\n\
            <table>\n\
                <tr>\n\
                    <td></td>\n\
                    <td>C</td>\n\
                    <td>R</td>\n\
                    <td>U</td>\n\
                    <td>D</td>\n\
                    <td data-ng-if="resource.id !== 0">E</td>\n\
                    <td data-ng-if="resource.id !== 0">O</td>\n\
                </tr>\n\
                <tr data-ng-repeat="userPermission in usersPermission">\n\
                    <td><label>{{userPermission.user.firstName}}: </label></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="userPermission.permission.create" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="userPermission.permission.read" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="userPermission.permission.update" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="userPermission.permission.delete" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box items="[false, true]" item="userPermission.permission.execute" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box items="[false, true]" item="userPermission.permission.owner" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td>&nbsp;Inherit from Group(s): <cris-one-check-box items="[false, true]" item="userPermission.inheritFromGroup" disabled="true"><!----></cris-one-check-box></td>\n\
                </tr>\n\
            </table>\n\
            <h4>Effective Permission for Member Users</h4>\n\
            <table>\n\
                <tr>\n\
                    <td></td>\n\
                    <td>C</td>\n\
                    <td>R</td>\n\
                    <td>U</td>\n\
                    <td>D</td>\n\
                    <td data-ng-if="resource.id !== 0">E</td>\n\
                    <td data-ng-if="resource.id !== 0">O</td>\n\
                </tr>\n\
                <tr data-ng-repeat="effectivePermission in effectivePermissions">\n\
                    <td><label>{{effectivePermission.user.firstName}}: </label></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="effectivePermission.permission.create" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="effectivePermission.permission.read" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="effectivePermission.permission.update" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td><cris-one-check-box items="[false, true]" item="effectivePermission.permission.delete" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box items="[false, true]" item="effectivePermission.permission.execute" disabled="true"><!----></cris-one-check-box></td>\n\
                    <td data-ng-if="resource.id !== 0"><cris-one-check-box items="[false, true]" item="effectivePermission.permission.owner" disabled="true"><!----></cris-one-check-box></td>\n\
                </tr>\n\
            </table>\n\
            ',
        link: function (scope, element, attrs) {
            scope.$watch("groupPermission.permission", function(permission) {
                scope.effectivePermissions = [];
                angular.forEach(scope.usersPermission, function(userPermission, idx) {
                    var effectiveUserPermission = scope.getEffectiveUserPermission(scope.groupPermission, userPermission);
                    scope.effectivePermissions.push(effectiveUserPermission);
                });
            }, true);
        },
        controller: ["$scope", function($scope) {
            $scope.getEffectiveUserPermission = function(groupPermission, userPermission) {
                var effectiveUserPermission = {user: userPermission.user};
                if (userPermission.inheritFromGroup) {
                    effectiveUserPermission.permission = groupPermission.permission;
                } else {
                    effectiveUserPermission.permission = userPermission.permission;
                }
                return effectiveUserPermission;
            };
        }]
    };
}]);

angular.module("permission").directive("crisPermissionEditor", ["crisPermissionService", function(crisPermissionService) {
    return {
        restrict: "E",
        scope: {
            isUser: "=",
            groupsPermission: "=",
            userPermission: "=",
            effectivePermission: "=",
            groupPermission: "=",
            usersPermission: "=",
            resource: "="
        },
        template: '\n\
            <div>\n\
                <div data-ng-if="isUser">\n\
                    <cris-user-permission-editor user-permission="userPermission" effective-permission="effectivePermission" groups-permission="groupsPermission" resource="resource">\n\
                        <!---->\n\
                    </cris-user-permission-editor>\n\
                </div>\n\
                <div data-ng-if="!isUser">\n\
                    <cris-group-permission-editor group-permission="groupPermission" users-permission="usersPermission" resource="resource">\n\
                        <!---->\n\
                    </cris-group-permission-editor>\n\
                </div>\n\
            </div>\n\
        ',
        link: function (scope, element, attrs) {
        },
        controller: ["$scope", function($scope) {
        }]
    };
}]);
