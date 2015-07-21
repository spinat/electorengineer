'use strict';

angular.module('frontendApp')
  .directive('lineChart', function($window, $log, _, d3) {

    $log.info('lineChart start');

    var margin = {top: 20, right: 20, bottom: 30, left: 50};
    var width = 960 - margin.left - margin.right;
    var height = 500 - margin.top - margin.bottom;
    var svg;

    return {

      controller: function($log, $element, $scope) {
        $log.info('Start', $element, d3, margin, width, height, $scope.evaluation);

        var rawSvg=$element.find('svg');
        svg = d3.select(rawSvg[0]);

        svg = svg.attr('width', width + margin.left + margin.right)
          .attr('height', height + margin.top + margin.bottom)
          .append('g')
          .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

        var x = d3.scale.linear()
          .range([0, width]);

        var y = d3.scale.linear()
          .range([height, 0]);

        var xAxis = d3.svg.axis()
          .scale(x)
          .orient('bottom');

        var yAxis = d3.svg.axis()
          .scale(y)
          .orient('left');

        var line = d3.svg.line()
          .x(function(d) { return x(d.time); })
          .y(function(d) { return y(d.volt); });

        x.domain([
          _.min($scope.evaluation.data, 'time').time,
          _.max($scope.evaluation.data, 'time').time
        ]);

        y.domain([
          _.min($scope.evaluation.data, 'volt').volt,
          _.max($scope.evaluation.data, 'volt').volt
        ]);

        svg.append('g')
          .attr('class', 'x axis')
          .attr('transform', 'translate(0,' + height + ')')
          .call(xAxis);

        svg.append('g')
          .attr('class', 'y axis')
          .call(yAxis)
          .append('text')
          .attr('transform', 'rotate(-90)')
          .attr('y', 6)
          .attr('dy', '.71em')
          .style('text-anchor', 'end')
          .text('Temperature (ºF)');

        svg.append('svg:path')
          .attr({
              d: line($scope.evaluation.data),
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
