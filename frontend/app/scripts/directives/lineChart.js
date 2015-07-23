'use strict';

angular.module('frontendApp')
  .directive('lineChart', function($window, $log, _, d3) {

    $log.info('lineChart start');

    var margin = {top: 20, right: 50, bottom: 30, left: 50};
    var width = 960 - margin.left - margin.right;
    var height = 500 - margin.top - margin.bottom;
    var svg;

    var scaleTime, scaleVolt, scaleAmpere;
    var axisTime, axisVolt, axisAmpere;
    var lineVolt, lineAmpere;

    function drawDiagram(element) {
      var rawSvg=element.find('svg');
      svg = d3.select(rawSvg[0]);

      svg = svg.attr('width', width + margin.left + margin.right)
        .attr('height', height + margin.top + margin.bottom)
        .append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

      scaleTime = d3.scale.linear()
        .range([0, width]);

      scaleVolt = d3.scale.linear()
        .range([height, 0]);

      scaleAmpere = d3.scale.linear()
        .range([height, 0]);

      axisTime = d3.svg.axis()
        .scale(scaleTime)
        .orient('bottom');

      axisVolt = d3.svg.axis()
        .scale(scaleVolt)
        .orient('left');

      axisAmpere = d3.svg.axis()
        .scale(scaleAmpere)
        .orient('right');

      lineVolt = d3.svg.line()
        .x(function(d) { return scaleTime(d.time); })
        .y(function(d) { return scaleVolt(d.volt); });

      lineAmpere = d3.svg.line()
        .x(function(d) { return scaleTime(d.time); })
        .y(function(d) { return scaleAmpere(d.ampere); });

      svg.append('g')
        .attr('class', 'x axis')
        .attr('transform', 'translate(0,' + height + ')')
        .call(axisTime);

      svg.append('g')
        .attr('class', 'y axis')
        .call(axisVolt)
        .append('text')
        .attr('transform', 'rotate(-90)')
        .attr('y', 6)
        .attr('dy', '.71em')
        .style('text-anchor', 'end')
        .text('Spannung (V)');

      svg.append('g')
        .attr('class', 'y axis')
        .attr('transform', 'translate('+width+',0)')
        .call(axisAmpere)
        .append('text')
        .attr('transform', 'rotate(90)')
        .attr('y', 6)
        .attr('dy', '.71em')
        .text('Strom (A)');
    }

    function drawData(evaluation) {
      scaleTime.domain([
        _.min(evaluation.data, 'time').time,
        _.max(evaluation.data, 'time').time
      ]);

      scaleVolt.domain([
        _.min(evaluation.data, 'volt').volt,
        _.max(evaluation.data, 'volt').volt
      ]);

      scaleAmpere.domain([
        _.min(evaluation.data, 'ampere').ampere,
        _.max(evaluation.data, 'ampere').ampere
      ]);

      svg.append('svg:path')
        .attr({
          d: lineVolt(evaluation.data),
          'stroke': 'blue',
          'stroke-width': 1.5,
          'opacity': 0.9,
          'fill': 'none',
          'class': 'path'
        });

      svg.append('svg:path')
        .attr({
          d: lineAmpere(evaluation.data),
          'stroke': 'red',
          'stroke-width': 1.5,
          'opacity': 0.9,
          'fill': 'none',
          'class': 'path'
        });
    }
    return {

      controller: function($log, $element, $scope) {
        $log.info('Start', $element, d3, margin, width, height, $scope.evaluation);

        drawDiagram($element);
        drawData($scope.evaluation);

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