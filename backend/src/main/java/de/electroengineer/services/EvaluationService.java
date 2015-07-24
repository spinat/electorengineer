package de.electroengineer.services;

import de.electroengineer.domain.Coordinate;
import de.electroengineer.domain.Evaluation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class EvaluationService {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluationService.class);

    @Autowired
    FileService fileService;

    @Autowired
    SlidingWindow slidingWindow;

    public Evaluation getEvaluation(String evaluationName) throws IOException {
        return fileService.loadEvaluation(evaluationName);
    }

    public List<String> findAllEvaluations() throws IOException {
        return fileService.listAllEvaluations();
    }
    
    public void generatePreviewData(Evaluation evaluation) {
        int countMeasurePoints = evaluation.getData().size();
        int skip = countMeasurePoints / 1000;

        List<Coordinate> coordinates = IntStream.range(0, evaluation.getData().size())
                .filter(i -> i % skip == 0)
                .mapToObj(i -> evaluation.getData().get(i))
                .collect(Collectors.toList());

        evaluation.setData(coordinates);
    }

    public Evaluation calc(Evaluation evaluation) {
        Coordinate t1StartCoordinate = findT1StartCoordinate(evaluation);
        evaluation.setT1Start(t1StartCoordinate);

        rmsAmpere(evaluation, t1StartCoordinate);
//        evaluation.setCalculationPoints(coordinates);

        return evaluation;
    }


    private static SimpleRegression collectWindowData(Evaluation evaluation, int windowStart, int windowEnd) {
        SimpleRegression regression = new SimpleRegression();
        for(int currentWindowIndex = windowStart; currentWindowIndex < windowEnd; currentWindowIndex++) {
            Coordinate currentCoordinate = evaluation.getData().get(currentWindowIndex);
            regression.addData(currentCoordinate.getTime(), currentCoordinate.getAmpere());
        }
        return regression;
    }

    private void rmsAmpere(Evaluation evaluation, Coordinate t1StartCoordinate) {

        List<Coordinate> evaluationData = evaluation.getData();
        OptionalInt startIndex = IntStream.range(0, evaluationData.size())
                .filter(i -> evaluationData.get(i).getTime() > t1StartCoordinate.getTime())
                .findFirst();


        int WINDOW_SIZE = 100;
        int size = 10000;
        double sampleIntervall = evaluation.getMeasures().get(0).getSampleIntervall();
        int gipfelIndex = 0;
        //Finde Gipfel
        for(int currentStartWindow = startIndex.getAsInt(); currentStartWindow < evaluationData.size() / 2; currentStartWindow += 10) {
            //Collect Data in Window
            int currentEndWindow = currentStartWindow + WINDOW_SIZE;
            SimpleRegression regression = collectWindowData(evaluation, currentStartWindow, currentEndWindow);

            double slope = regression.getSlope() / sampleIntervall;
//            System.out.println(slope);
            if(slope < 0) {
//                evaluation.addCalculationPoint("gipfel", evaluationData.get(currentEndWindow));
                gipfelIndex = currentEndWindow;
                break;
            }
        }

        //Zwischen Gipfel und 10.000 Messpunkte max finden
        int endExclusive = gipfelIndex + 10000;
        List<Coordinate> leftWindow = IntStream.range(gipfelIndex, endExclusive)
                .mapToObj(i -> evaluationData.get(i))
                .collect(Collectors.toList());
        Optional<Coordinate> max = leftWindow.stream().max((o1, o2) -> Double.compare(o1.getAmpere(), o2.getAmpere()));

        evaluation.addCalculationPoint("linksMax", max.get());

        //Min
        List<Coordinate> collect = IntStream.range(endExclusive + 15000, endExclusive + 25000)
                .mapToObj(i -> evaluationData.get(i))
                .collect(Collectors.toList());
        Optional<Coordinate> min = collect.stream().min((o1, o2) -> Double.compare(o1.getAmpere(), o2.getAmpere()));
//        evaluation.addCalculationPoint("linksmin", min.get());

        Optional<Coordinate> _20ms = evaluationData.stream()
                .filter(coordinate -> coordinate.getTime() - max.get().getTime() >= 0.01)
                .findFirst();

        evaluation.addCalculationPoint("20ms", _20ms.get());

        //RMS Bestimmung

        OptionalInt first = IntStream.range(0, evaluationData.size())
                .filter(i -> evaluationData.get(i) == max.get())
                .findFirst();

        OptionalInt last = IntStream.range(0, evaluationData.size())
                .filter(i -> evaluationData.get(i) == min.get())
                .findFirst();

        double periodenDauer = min.get().getTime() - max.get().getTime();
        double rms = 0d;

        for(int x = first.getAsInt(); x < last.getAsInt(); x++) {
            double i = evaluation.getData().get(x).getAmpere();
            rms += i*sampleIntervall / periodenDauer;
        }



        LOG.info("Ampere RMS={}", rms);

//        Map<String, Coordinate> coordinates = slidingWindow.maximumTurningPoint(evaluation, t1StartCoordinate);

//        Coordinate max = coordinates.get("max");
//        Coordinate min = coordinates.get("min");

//        evaluation.addCalculationPoint("maxAmpere", max);
//        evaluation.addCalculationPoint("minAmpere", min);

//        double gleichanteil = (min.getAmpere() + max.getAmpere()) / 2;
//        double amplitude = max.getAmpere() - gleichanteil;
//        double wechselEffektivWert = amplitude / Math.sqrt(2);
//        double rms = Math.sqrt(Math.pow(gleichanteil, 2) + Math.pow(wechselEffektivWert, 2));

    }

    private Coordinate findT1StartCoordinate(Evaluation evaluation) {
        Double totalMaxAmpere = evaluation.getData().parallelStream()
                .max((v1, v2) -> Double.compare(v1.getAmpere(), v2.getAmpere()))
                .get()
                .getAmpere();

        Double totalMinAmpere = evaluation.getData().parallelStream()
                .min((v1, v2) -> Double.compare(v1.getAmpere(), v2.getAmpere()))
                .get()
                .getAmpere();

        LOG.info("min={}", totalMinAmpere);

        final Double max = Math.pow(totalMaxAmpere - totalMinAmpere, 2); //min Wert auf 0 setzen

        List<Double> normedMeasurePointsTotal = evaluation.getData().stream()
                .map(Coordinate::getAmpere)             //Strom
                .map(value -> value - totalMinAmpere)   //min Wert auf 0 setzen
                .map(value -> value * value)            //Quadieren für die Genauigkeit
                .map(value -> 100.0f * ((value - 0) / (max - 0))) //Normieren auf 100%
                .collect(Collectors.toList());

        OptionalInt firstIndex = IntStream.range(0, normedMeasurePointsTotal.size())
                //Erster Wert, der 0.05% Übersteigt. !!Achtung Quadrierung. Es ist nicht linear
                .filter(i -> normedMeasurePointsTotal.get(i) > 0.05)
                .findFirst();

        return evaluation.getData().get(firstIndex.getAsInt());
    }
}
