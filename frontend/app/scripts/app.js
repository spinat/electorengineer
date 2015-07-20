'use strict';

/**
 * @ngdoc overview
 * @name frontendApp
 * @description
 * # frontendApp
 *
 * Main module of the application.
 */
angular
  .module('frontendApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .when('/evaluation/:evaluationName', {
        templateUrl: 'views/evaluation_details.html',
        controller: 'EvaluationDetailsCtrl',
        resolve: {
          Evaluation: function($route, EvaluationService) {
            var evaluationName = $route.current.params.evaluationName;
            return EvaluationService.getEvaluation(evaluationName);
          }
        }

      })
      .otherwise({
        redirectTo: '/'
      });
  });
