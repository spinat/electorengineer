'use strict';

angular.module('frontendApp')
  .factory('_', function($window) {
    return $window._;
  });

angular.module('frontendApp')
  .factory('d3', function($window) {
    return $window.d3;
  });
