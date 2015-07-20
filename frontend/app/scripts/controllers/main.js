'use strict';

angular.module('frontendApp')
  .controller('MainCtrl', function ($scope, $log, EvaluationService) {
    $log.info('MainCtrl start');

    EvaluationService.listEvaluations()
      .then(function(data) {
        $log.info('Evaluations found:', data);
        $scope.evaluations = data;
      });
  });
