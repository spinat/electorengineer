'use strict';

angular.module('frontendApp')
  .factory('EvaluationService', function ($log, $http, $q) {

    $log.info('EvaluationService start');
    var evaluationService ={};

    evaluationService.listEvaluations = function() {
      var deferred = $q.defer();

      $http.get('http://localhost:8080/api/evaluation/list')
        .success(function(response) {
          deferred.resolve(response);
        })
        .error(function(response) {
          deferred.reject(response);
        });

      return deferred.promise;
    };

    evaluationService.getEvaluation = function(evaluationName) {
      var deferred = $q.defer();

      $http.get('http://localhost:8080/api/evaluation/' + evaluationName)
        .success(function(response) {
          deferred.resolve(response);
        })
        .error(function(response) {
          deferred.reject(response);
        });

      return deferred.promise;
    };

    evaluationService.calc = function(evaluationName, rmsAmperePeriodMs, rmsVoltPeriodMs) {
      var deferred = $q.defer();

      $http.post('http://localhost:8080/api/evaluation/' + evaluationName + '/calc/' + rmsAmperePeriodMs + '/' + rmsVoltPeriodMs)
        .success(function(response) {
          deferred.resolve(response);
        })
        .error(function(response) {
          deferred.reject(response);
        });

      return deferred.promise;
    };

    return evaluationService;
  });
