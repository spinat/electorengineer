'use strict';

angular.module('frontendApp')
  .controller('EvaluationDetailsCtrl', function ($scope, $log, $route, Evaluation, EvaluationService) {
    $log.info('EvaluationDetailsCtrl start', Evaluation);

    $scope.evaluation = Evaluation;
    $scope.units = ['', 'm', 'µ'];

    $scope.calculate = function() {
      $log.info('Calc', Evaluation.evaluationName, Evaluation.rmsAmperePeriodMs, Evaluation.rmsVoltPeriodMs);
      $scope.inProgress = true;

      EvaluationService.calc(Evaluation.evaluationName, Evaluation.rmsAmperePeriodMs, Evaluation.rmsVoltPeriodMs)
        .then(function() {
          $route.reload();
        });
    };

    $scope.normalize = function() {
      $log.info('normalize', $scope.bla);
    };

  });
