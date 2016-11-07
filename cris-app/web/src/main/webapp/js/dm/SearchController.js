"use strict";

// Meta fields
var MetaField_Id = "_id";
var MetaField_Parent = "__parent";
var MetaField_ProjectId = "_project_id";
var MetaField_ExperimentId = "_experiment_id";
var MetaField_JobId = "_job_id";
var MetaField_TemplateId = "_template_id";
var MetaField_TemplateUuid = "_template_uuid";
var MetaField_TemplateVersion = "_template_version";
var MetaField_TaskId = "_task_id";

angular.module('crisSearch', ['dataset', 'angular-dojo', 'ui.bootstrap']);

angular.module('crisSearch').constant("CRIS_CONST_SEARCH", {
    "RESULTS_PER_PAGE": 10
});

angular.module('crisSearch').config([function () {
    console.log("cris-search: config()");
}]);

angular.module('crisSearch').filter("fieldFilter", [function () {
    return function(input) {
        var result = {};
        angular.forEach(input, function (value, key) {
            if (key.indexOf("_") !== 0) {
                result[key] = value;
            }
        });
        return result;
    };
}]);

angular.module('crisSearch').directive("crisSearchResult", ['$compile', '$parse', function ($compile, $parse) {
    return {
        restrict: "EA",
        scope: {
            hasError: "&hasError",
            onSelect: "=onSelect",
            data: "=data"
        },
        template: '<table class="form"><tbody>\n\
            <tr data-ng-click="onClick(hit)" data-ng-repeat="hit in data.data.hits.hits">\n\
                <td style="vertical-align: top;" data-ng-repeat="(key, value) in hit._source | fieldFilter">\n\
                    <div data-ng-class="{label: true, \'label-default\': selectedId !== hit._id, \'label-info\': selectedId === hit._id}">{{key}}</div>\n\
                    <div>{{value}}</div>\n\
                </td>\n\
            </tr>\n\
        </tbody></table>',
        link: function (scope, element, attrs) {
            scope.$watchCollection('data', function () {
                // invalidate the selectedID when data changes
                scope.selectedId = null;
            });
        },
        controller: ["$scope", function($scope) {
            $scope.onClick = function (hit) {
                $scope.selectedId = hit["_id"];
                $scope.onSelect(hit);
            };
        }]
    };
}]);

angular.module('crisSearch').directive("crisError", ['$compile', '$parse', function ($compile, $parse) {
    return {
        restrict: "EA",
        scope: {
            hasError: "&hasError",
            error: "=error"
        },
        template: "{{error.message}}",
        link: function (scope, element, attrs) {
        },
        controller: ["$scope", function($scope) {
        }]
    };
}]);

angular.module('crisSearch').directive("crisSearchResponse", ['$compile', '$parse', function ($compile, $parse) {
    return {
        restrict: "EA",
        scope: {
            response: "=response"

        },
        template: "<data-cris-error data-ng-show='error' error='error'><!-- --></data-cris-error><data-cris-search-result data-ng-show='data' data='data'><!-- --></data-cris-search-result>",
        link: function (scope, element, attrs) {
            scope.$watch("response", function(value) {
                console.dir(value);
                if (value) {
                    scope.error = value.data;
                    scope.data = value;
                }
            }, true);
        },
        controller: ["$scope", function($scope) {
        }]
    };
}]);

angular.module('crisSearch').run(["$rootScope", "$http", function($rootScope, $http) {
    console.log("cris-search: run()");

    // users
    $http({method: "GET", url: cris.baseUrl + "users"}).then(function (response) {
        console.dir(response);
        $rootScope.users = response.data;
    }, function (response) {
        console.dir(response);
    });

    // projects
    $http({method: "GET", url: cris.baseUrl + "projects"}).then(function (response) {
        console.dir(response);
        $rootScope.projects = response.data;
    }, function (response) {
        console.dir(response);
    });

    // experiments
    $http({method: "GET", url: cris.baseUrl + "experiments"}).then(function (response) {
        console.dir(response);
        $rootScope.experiments = response.data;
    }, function (response) {
        console.dir(response);
    });

    // jobs
    $http({method: "GET", url: cris.baseUrl + "jobs"}).then(function (response) {
        console.dir(response);
        $rootScope.jobs = response.data;
    }, function (response) {
        console.dir(response);
    });
}]);

angular.module('crisSearch').controller("SearchController", ["$scope", "$http", "CRIS_CONST_SEARCH", function ($scope, $http, CRIS_CONST_SEARCH) {
    // search response
    $scope.data = {};
    $scope.error = {};

    // document of the current selection
    $scope.term = {};
    $scope.doc = {};
    $scope.readOnly = true;

    // project, experiment and job of the current selection
    $scope.experiment = null;
    $scope.project = null;
    $scope.job = null;

    $scope.showDetailedView = false;

    $scope.hasError = function () {
        console.log("hasError() called");
        return $scope.error && $scope.error.hasError ? true: false;
    };

    // date input
    $scope.status = {
        dateFrom: {},
        dateTo: {}
    };

    $scope.open = function($event) {
        $scope.status.opened = true;
    };

    $scope.currentPage = 1;
    $scope.itemsPerPage = CRIS_CONST_SEARCH.RESULTS_PER_PAGE;

    $scope.searchGeneral = function(keywords, userId, projectId, experimentId, jobId, dateFrom, dateTo, advanced) {
        // clear and hide details
        $scope.showDetailedView = false;
        $scope.project = null;
        $scope.experiment = null;
        $scope.job = null;
        $scope.term = {};
        $scope.doc = {};

        search(keywords, userId, projectId, experimentId, jobId, dateFrom, dateTo, advanced);
    };

    $scope.searchUser = function(user) {
        if (user) {
            search(null, user.id, null, null, null, null, null, true);
        }
    };

    $scope.searchProject = function(project) {
        if (project) {
            search(null, null, project.id, null, null, null, null, true);
        }
    };

    $scope.searchExperiment = function(experiment) {
        if (experiment) {
            search(null, null, null, experiment.id, null, null, null, true);
        }
    };

    $scope.searchJob = function(job) {
        if (job) {
            search(null, null, null, null, job.id, null, null, true);
        }
    };

    $scope.pageChanged = function() {
        console.log("page changed: " + $scope.currentPage);
        queryContext.currentPage = $scope.currentPage - 1;
        submitQuery(queryContext);
    };

    $scope.onSelect = function (hit) {
        var source = hit._source;
        $scope.showDetailedView = true;
        $scope.user = idToUser(source._creator_id);
        $scope.project = idToProject(source._project_id);
        $scope.experiment = idToExperiment(source._experiment_id);
        $scope.job = idToJob(source._job_id);

        openDocument(hit);
    };

    // query context
    var queryContext = {
        queryData: null,
        currentPage: 0,
        resultPerPage: CRIS_CONST_SEARCH.RESULTS_PER_PAGE
    };

    function resetQueryContext () {
        queryContext.queryData = null;
        queryContext.currentPage = 0;
        queryContext.resultPerPage = CRIS_CONST_SEARCH.RESULTS_PER_PAGE;
    };

    function submitQuery (queryContext) {
        //TODO:  content: {queryContext : dojo.toJson(queryContext)},
        $http({method: "GET", url: cris.baseUrl + "search/search?queryContext=" + dojo.toJson(queryContext)}).then(function (data) {
            console.log("items: " + data.data.hits.total);
            $scope.data = data;
            $scope.error = {};
            $scope.currentPage = queryContext.currentPage + 1;
        }, function (error) {
            $scope.data = {};
            if (error.data) {
                $scope.error = error.data;
            } else {
                $scope.error = error;
            }
            $scope.currentPage = 1;
        });
    };

    function search (keywords, userId, projectId, experimentId, jobId, dateFrom, dateTo, advanced) {
        console.log("search...");
        console.log(keywords + ", " + userId + ", " + projectId + ", " + experimentId + ", " + jobId + ", " + dateFrom + ", " + dateTo + ", " + advanced);

        // build search query
        var queryData = [];
        var item;

        if (keywords) {
            item = {};
            item.andOr = null;
            item.templateId = null;
            item.termAlias = null;
            item.op = null;
            item.input = keywords;
            queryData.push(item);
        }

        if (advanced) {
            // handle userId
            if (userId) {
                item = {};
                item.andOr = "must";
                item.templateId = null;
                item.field = "_creator_id";
                item.op = "equals";
                item.input = userId;
                queryData.push(item);
            }

            // handle dateFrom and dateTo
            if (dateFrom && dateTo) {
                item = {};
                item.andOr = "must";
                item.templateId = null;
                item.field = "_time_updated";
                item.op = "range";
                item.input1 = dateFrom;
                item.input2 = dateTo;
                item.hiddenInput = "[" + dateFrom.valueOf() + "," + dateTo.valueOf() + "]";
                queryData.push(item);
            } else if (dateFrom) {
                item.andOr = "must";
                item.templateId = null;
                item.field = "_time_updated";
                item.op = "greater than or equal";
                item.input = dateFrom.valueOf();
                queryData.push(item);
            } else if (dateTo) {
                item.andOr = "must";
                item.templateId = null;
                item.field = "_time_updated";
                item.op = "less than or equal";
                item.input = dateTo.valueOf();
                queryData.push(item);
            }

            // handle project
            if (projectId) {
                item = {};
                item.andOr = "must";
                item.templateId = null;
                item.field = "_project_id";
                item.op = "equals";
                item.input = projectId;
                queryData.push(item);
            }

            // handle experiment
            if (experimentId) {
                item = {};
                item.andOr = "must";
                item.templateId = null;
                item.field = "_experiment_id";
                item.op = "equals";
                item.input = experimentId;
                queryData.push(item);
            }

            // handle job
            if (jobId) {
                item = {};
                item.andOr = "must";
                item.templateId = null;
                item.field = "_job_id";
                item.op = "equals";
                item.input = jobId;
                queryData.push(item);
            }
        }

        // submit query
        resetQueryContext();
        queryContext.queryData = queryData;
        submitQuery(queryContext);
    };

    // open an existing document
    function openDocument (hit) {
        $scope.showDetailedView = true;
        $scope.error = {};
        $scope.inProgress = true;

        var templateUuid = hit["_type"].replace("dataset_", "").replace(/_/g, "-");
        var id = hit["_id"];
        $http.get(cris.baseUrl + "rest/objectus/" + templateUuid + "/" + id).success(function (data, status, headers, config) {
            var templateVersion = data["_template_version"].$uuid;
            try {
                $scope.term = getTerm(templateUuid, templateVersion, true);
                $scope.doc = data;
                $scope.inProgress = false;
            } catch (e) {
                $scope.term = {};
                $scope.doc = {};
                $scope.error = {message: "template missing: uuid: " + templateUuid +", version: " + templateVersion};
                $scope.inProgress = false;
            }
        }).error(function (data, status, headers, config) {
            $scope.term = {};
            $scope.doc = {};
            $scope.error = {message: "data missing: id: " + id + ", template uuid: " + templateUuid +", version: " + templateVersion};
            $scope.inProgress = false;
        });
    };

    function idToUser(id) {
        var user = null;
        angular.forEach($scope.users, function (value, key) {
            if (value.id === id) {
                user = value;
                return;
            }
        });

        return user;
    }

    function idToProject(id) {
        var project = null;
        angular.forEach($scope.projects, function (value, key) {
            if (value.id === id) {
                project = value;
                return;
            }
        });

        return project;
    }

    function idToExperiment(id) {
        var experiment = null;
        angular.forEach($scope.experiments, function (value, key) {
            if (value.id === id) {
                experiment = value;
                return;
            }
        });

        return experiment;
    }

    function idToJob(id) {
        var job = null;
        angular.forEach($scope.jobs, function (value, key) {
            if (value.id === id) {
                job = value;
                return;
            }
        });

        return job;
    }

    function addHyphen(uuid) {
        if (!uuid || uuid.length !== 32) {
            return uuid;
        }

        return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
    }

}]);
