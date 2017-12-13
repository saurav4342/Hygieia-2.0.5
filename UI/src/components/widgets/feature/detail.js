(function () {
    'use strict';

    angular
        .module(HygieiaConfig.module)
        .controller('FeatureDetailController', FeatureDetailController);
       
    FeatureDetailController.$inject = ['$uibModalInstance', 'feature', 'featureType','totalObject','release'];
    function FeatureDetailController($uibModalInstance, feature, featureType,totalObject,release) {
        /*jshint validthis:true */
        var ctrl = this;

        ctrl.feature = feature;
        ctrl.featureType = featureType;
        ctrl.release = release.split('-')[1].trim();
        ctrl.close = close;
        ctrl.totalObject = totalObject;
        ctrl.keys;
        ctrl.data; 
        ctrl.showChart = showChart;
        ctrl.showDChart = showDChart;
        var teams = [];
        var defCount=[];
         ctrl.feature.forEach( f => {
             if(f.defectData.dCount>0){
               teams.push(f.team);
               defCount.push(f.defectData.dCount);
             }
            });
        feature.forEach(element => {
            var obj = element.submittedDefectCountByDate;
            ctrl.keys = Object.keys(obj);
            ctrl.data = Object.values(obj);
        });
      function showChart(){
            var ctx = document.getElementById('myChart').getContext('2d');
            var chart = new Chart(ctx, {
                // The type of chart we want to create
                type: 'line',
            
                // The data for our dataset
                data: {
                    labels: ctrl.keys,
                    datasets: [{
                        label: "Defects submitted by Date",
                        backgroundColor: 'rgba(216,81,77,0.5)',
                        borderColor: 'rgba(255, 99, 132,1)',
                        data: ctrl.data,
                        borderWidth: 1
                    }]
                },
                options: {
                    scales: {
                        yAxes: [{
                            ticks: {
                                beginAtZero:true
                            }
                        }]
                    }
                }
            });
      }
        
      function showDChart(){
        var ctx = document.getElementById('myChart2').getContext('2d');
        var chart = new Chart(ctx, {
            // The type of chart we want to create
            type: 'horizontalBar',
        
            // The data for our dataset
            data: {
                labels: teams,
                datasets: [{
                    label: "Deferred Defects by Team",
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.2)',
                        'rgba(54, 162, 235, 0.2)',
                        'rgba(255, 206, 86, 0.2)',
                        'rgba(75, 192, 192, 0.2)',
                        'rgba(153, 102, 255, 0.2)',
                        'rgba(255, 159, 64, 0.2)'
                    ],
                    borderColor: [
                        'rgba(255,99,132,1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(75, 192, 192, 1)',
                        'rgba(153, 102, 255, 1)',
                        'rgba(255, 159, 64, 1)'
                    ],
                    borderWidth: 1,
                    data: defCount,
                }]
            },
        
            // Configuration options go here
            options: {
                scales: {
                    xAxes: [{
                        ticks: {
                            beginAtZero:true,
                            stepSize: 1
                        }
                    }]
                }
            }
        });
      }
        function close() {
            $uibModalInstance.dismiss('close');
        }
    }
})();
