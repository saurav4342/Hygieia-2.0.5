(function () {
    'use strict';

    angular
        .module(HygieiaConfig.module)
        .controller('monitorViewController', monitorViewController)
        .controller('monitorStatusController', monitorStatusController);

    monitorViewController.$inject = ['$scope', 'monitorData', 'DashStatus', '$uibModal', '$q'];
    function monitorViewController($scope, monitorData, DashStatus, $uibModal, $q) {
        /*jshint validthis:true */
        var ctrl = this;
        ctrl.showChart = showChart;
        // public variables
        function showChart(){
        var ctx = document.getElementById("myChart").getContext('2d');
        var myChart = new Chart(ctx, {
            type: 'pie',
            data : {
                datasets: [{
                    data: errorCountArray,
                    backgroundColor: [
                        'rgba(5,172,69,1)',
                        'rgba(255, 99, 132, 1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(216,81,77,1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(153, 102, 255, 1)',
                        'rgba(255, 159, 64, 1)'
                    ],
                    borderColor: [
                        'rgb(255,255,255)'
                    ]
                }],
            
                // These labels appear in the legend and in the tooltips when hovering different arcs
                labels: label
                
            },
            options: {
                
            }
        });
    }
        ctrl.statuses = DashStatus;
        ctrl.services = [];
        ctrl.dependencies = [];
        var errorCountArray = [];
        var label = [];
        
        // public methods
        ctrl.openStatusWindow = openStatusWindow;
        ctrl.hasMessage = hasMessage;

        ctrl.load = function () {
            // grab data from the api
            var deferred = $q.defer();
            monitorData.details($scope.dashboard.id).then(function (data) {
                processResponse(data.result);
                deferred.resolve(data.lastUpdated);
            });
            return deferred.promise;
        };


        // method implementations
        function hasMessage(service) {
            return service.message && service.message.length;
        }

        function openStatusWindow(service) {
            // open up a new modal window for the user to set the status
            $uibModal.open({
                templateUrl: 'monitorStatus.html',
                controller: 'monitorStatusController',
                controllerAs: 'ctrl',
                scope: $scope,
                size: 'lg',
                resolve: {
                    // make sure modal has access to the status and selected
                    statuses: function () {
                        return DashStatus;
                    },
                    service: function () {
                        return {
                            id: service.id,
                            pod: service.pod,
                            errorCount: service.errorCount,
                            ucids: service.ucidCount,
                            percentage: service.percentage,
                            sysCodes: service.syscodes,
                            app: service.app,
                            custids: service.custids,
                            errorMessages: service.errorMessages
                        };
                    }
                }
            }).result
                .then(function (updatedService) {
                    // if the window is closed without saving updatedService will be null
                    if (!updatedService) {
                        return;
                    }

                    // update locally
                    _(ctrl.services).forEach(function (service, idx) {
                        if (service.id == updatedService.id) {
                            ctrl.services[idx] = angular.extend(service, updatedService);
                        }
                    });

                    // update the api
                    monitorData.updateService($scope.dashboard.id, updatedService);
                });
        }

        function processResponse(response) {
            var worker = {
                doWork: workerDoWork
            };

            worker.doWork(response, DashStatus, workerCallback);
        }

        function workerDoWork(data, statuses, cb) {
            cb({
                services: get(data.services, false),
                dependencies: get(data.dependencies, true)
            });

            function get(services, dependency) {
                return _.map(services, function (item) {
                    var name = item.name;
                    errorCountArray.push(item.errorCount);
                    label.push(item.pod);
                    if (dependency && item.applicationName) {
                        name = item.applicationName + ': ' + name;
                    }

                    if (item.status && (typeof item.status == 'string' || item.status instanceof String)) {
                        item.status = item.status.toLowerCase();
                    }

                    switch (item.status) {
                        case 'ok':
                            item.status = statuses.PASS;
                            break;
                        case 'warning':
                            item.status = statuses.WARN;
                            break;
                        case 'alert':
                            item.status = statuses.FAIL;
                            break;
                    }

                    return {
                        id: item.id,
                        name: name,
                        status: item.status,
                        app: item.app,
                        pod: item.pod,
                        errorCount: item.errorCount,
                        ucids: item.custIDs.length,
                        custids: item.custIDs,
                        syscodes: item.syscodes,
                        percentage: item.percentage,
                        errorMessages: item.errorMessages
                    };
                });
            }
        }

        function workerCallback(data) {
            //$scope.$apply(function () {
            ctrl.services = data.services;
            ctrl.dependencies = data.dependencies;
            //});
        }
    }

    monitorStatusController.$inject = ['service', 'statuses', '$uibModalInstance'];
    function monitorStatusController(service, statuses, $uibModalInstance) {
        /*jshint validthis:true */
        var ctrl = this;

        // public variables
        ctrl.service = service;
        ctrl.statuses = statuses;
        ctrl.setStatus = setStatus;

        // public methods
        ctrl.submit = submit;

        function setStatus(status) {
            ctrl.service.status = status;

        }

        function submit() {
            // pass the service back so the widget can update
            $uibModalInstance.close(ctrl.service);
        }
    }
})();
