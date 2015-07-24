package de.electroengineer.services;

import de.electroengineer.domain.Coordinate;
import de.electroengineer.domain.Evaluation;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.AllowedSolution;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.math3.analysis.solvers.AllowedSolution.*;

@Service
public class SlidingWindow {

    private static final Logger LOG = LoggerFactory.getLogger(SlidingWindow.class);

    private static final int WINDOW_SIZE = 5;

    public Map<String, Coordinate> maximumTurningPoint(Evaluation evaluation, Coordinate t1StartCoordinate) {

        //Find start location
        OptionalInt startIndex = IntStream.range(0, evaluation.getData().size())
                .filter(i -> evaluation.getData().get(i).getTime() > t1StartCoordinate.getTime())
                .findFirst();

        //Slide Window
        Map<String, Coordinate> coordinates = slideWindow(evaluation, startIndex.getAsInt());
        return coordinates;
    }

    private Map<String, Coordinate> slideWindow(Evaluation evaluation, int startIndex) {

        Map<String, Coordinate> found = new HashMap<>();
        int countNegativeSlopes = 0;
        boolean foundMin = false;

        for(int currentStartWindow = startIndex; currentStartWindow < evaluation.getData().size() - WINDOW_SIZE; currentStartWindow++) {

            int currentEndWindow = currentStartWindow + WINDOW_SIZE;
            SimpleRegression regression = collectWindowData(evaluation, currentStartWindow, currentEndWindow);

            Double sampleIntervall = evaluation.getMeasures().get(0).getSampleIntervall();
            double slope = regression.getSlope() / sampleIntervall;
            countNegativeSlopes = slope < 0 ? countNegativeSlopes + 1 : 0;

            if(countNegativeSlopes == 10 && foundMin == false) {


                found.put("min", evaluation.getData().get(currentStartWindow));
                int ende = currentStartWindow + 17500;
                found.put("max", evaluation.getData().get(ende));

                double periodenDauer = evaluation.getData().get(ende).getTime() - evaluation.getData().get(currentEndWindow).getTime();
                double rms = 0d;
                for(int x = currentStartWindow; x < ende; x++) {
                    double i = evaluation.getData().get(x).getAmpere();
                    rms += i*sampleIntervall / periodenDauer;
                }
                LOG.info("Neuer RMS =" + rms);
                foundMin = true;
            }

        }

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
