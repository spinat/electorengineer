'use strict';

angular.module('frontendApp')
  .directive('lineChart', function($window, $log, _, d3) {

    $log.info('lineChart start');

    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    var width = 960 - margin.left - margin.right;
    var height = 500 - margin.top - margin.bottom;

    var svg;

    var xScale, xAxis;
    var vScale, vAxis, vLine;
    //var aScale, aAxis, aLine;

    function configureX(min, max) {
      $log.info('min', min, 'max', max);

      xScale = d3.scale.linear()
        .range([0, width])
        .domain([min, max]);

      xAxis = d3.svg.axis()
        .scale(xScale)
        .orient('bottom')
        .ticks(10);
    }

    function configureV(min, max) {
      $log.info('min', min, 'max', max);

      vScale = d3.scale.linear()
        .range([0, width])
        .domain([min, max]);

      vAxis = d3.svg.axis()
        .scale(vScale)
        .orient('left')
        .ticks(10);

      vLine = d3.svg.line()
        .x(function (d) {
          //$log.info('data', d.x);
          return xScale(d.time);
        })
        .y(function (d) {
          return vScale(d.volt);
        });
    }


    return {

      controller: function($log, $element, $scope) {
        $log.info('Start', $element, d3, margin, width, height, $scope.evaluation);

        var rawSvg=$element.find('svg');
        rawSvg.attr('width', width + margin.left + margin.right);
        rawSvg.attr('height', height + margin.top + margin.bottom);
        svg = d3.select(rawSvg[0]);

        //svg
        //  .append('g')
        //  .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

        configureX(_.min($scope.evaluation.data, 'time').time, _.max($scope.evaluation.data, 'time').time);
        configureV(_.min($scope.evaluation.data, 'volt').volt, _.max($scope.evaluation.data, 'volt').volt);

        //Axen
        svg.append('svg:g')
          .attr('class', 'x axis')
          .attr('transform', 'translate(0,' + height + ')')
          .call(xAxis);

        svg.append('svg:g')
          .attr('class', 'y axis')
          .attr('transform', 'translate(' + width + ', 0)')
          .call(vAxis);

        svg.append('svg:path')
          .attr({
            d: vLine($scope.evaluation.data),
            'stroke': 'blue',
            'stroke-width': 1.5,
            'opacity': 0.9,
            'fill': 'none',
            'class': 'path'
          });

      },

      template: '<svg width="0" height="0"></svg>',

      scope: {
        evaluation: '='
      },

      //DOM Manipulationen
      link: function(/*scope, element, attr, ctrl*/) {
      }
    };
  }
);
