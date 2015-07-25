'use strict';

angular.module('frontendApp')
  .controller('EvaluationDetailsCtrl', function ($scope, $log, Evaluation) {
    $log.info('EvaluationDetailsCtrl start', Evaluation);

    $scope.evaluation = Evaluation;
    $scope.units = ['', 'm', 'µ'];
    $scope.calc = {};

    $scope.calculate = function() {
      $log.info('Calc', $scope.calc);
    };

  });
