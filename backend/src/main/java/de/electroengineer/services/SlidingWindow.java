package de.electroengineer.services;

import de.electroengineer.domain.Coordinate;
import de.electroengineer.domain.Evaluation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SlidingWindow {

    private static final Logger LOG = LoggerFactory.getLogger(SlidingWindow.class);

    private static final int WINDOW_SIZE = 5;

    public List<Coordinate> maximumTurningPoint(Evaluation evaluation, Coordinate t1StartCoordinate) {

        //Find start location
        OptionalInt startIndex = IntStream.range(0, evaluation.getData().size())
                .filter(i -> evaluation.getData().get(i).getTime() > t1StartCoordinate.getTime())
                .findFirst();

        //Slide Window
        List<Coordinate> coordinates = slideWindow(evaluation, startIndex.getAsInt());
        return coordinates;
    }

    private List<Coordinate> slideWindow(Evaluation evaluation, int startIndex) {

        List<Coordinate> found = new ArrayList<>();
        int countNegativeSlopes = 0;

        int startMaxMinIndex = 0;
        for(int currentStartWindow = startIndex; currentStartWindow < evaluation.getData().size() / 2; currentStartWindow++) {

            int currentEndWindow = currentStartWindow + WINDOW_SIZE;
            SimpleRegression regression = collectWindowData(evaluation, currentStartWindow, currentEndWindow);

            double slope = regression.getSlope() / evaluation.getMeasures().get(0).getSampleIntervall();
            countNegativeSlopes = slope < 0 ? countNegativeSlopes + 1 : 0;

            if(countNegativeSlopes == 10) {
                startMaxMinIndex = currentStartWindow + 2000; //Rauschen
                break;
            }
        }

        List<Coordinate> data = IntStream.range(startMaxMinIndex, startMaxMinIndex + 7500) //Bis
                .mapToObj(i -> evaluation.getData().get(i))
                .collect(Collectors.toList());

        Optional<Coordinate> max = data.stream()
                .max((o1, o2) -> Double.compare(o1.getAmpere(), o2.getAmpere()));

        Optional<Coordinate> min = data.stream()
                .min((o1, o2) -> Double.compare(o1.getAmpere(), o2.getAmpere()));

        found.add(max.get());
        found.add(min.get());

        return found;
    }

    private static SimpleRegression collectWindowData(Evaluation evaluation, int windowStart, int windowEnd) {
        SimpleRegression regression = new SimpleRegression();
        for(int currentWindowIndex = windowStart; currentWindowIndex < windowEnd; currentWindowIndex++) {
            Coordinate currentCoordinate = evaluation.getData().get(currentWindowIndex);
            regression.addData(currentCoordinate.getTime(), currentCoordinate.getAmpere());
        }
        return regression;
    }
}
