package de.electroengineer.services;

import de.electroengineer.domain.Coordinate;
import de.electroengineer.domain.Evaluation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class EvaluationService {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluationService.class);

    @Autowired
    FileService fileService;

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

        //tStart
        Coordinate tStartCoordinate = findT1StartCoordinate(evaluation);
        evaluation.addCalculationPoint("tStart", tStartCoordinate);

        //RMS Ampere
        double rmsAmperePeriod = evaluation.getRmsAmperePeriodMs() == null ? 10d / 1000d : evaluation.getRmsAmperePeriodMs() / 1000d;
        Double rmsAmpere = rmsAmpere(evaluation, tStartCoordinate, rmsAmperePeriod);
        evaluation.setRmsAmpere(rmsAmpere);

        //T63
        Coordinate t63Coordinate = findT63Coordinate(evaluation);
        evaluation.addCalculationPoint("t63", t63Coordinate);

        Coordinate tRMSCoordinate = findTRMSCoordinate(evaluation);
        evaluation.addCalculationPoint("tRMS", tRMSCoordinate);

        //T1
        double t1 = t63Coordinate.getTime() - tStartCoordinate.getTime();
        evaluation.setT1(t1);

        //T2
        double t2 = tRMSCoordinate.getTime() - tStartCoordinate.getTime();
        evaluation.setT2(t2);

        //RMS Volt
        double rmsVoltPeriod = evaluation.getRmsVoltPeriodMs() == null ? 10d / 1000d : evaluation.getRmsVoltPeriodMs() / 1000d;
        double rmsVolt = rmsVolt(evaluation, rmsVoltPeriod);
        evaluation.setRmsVolt(rmsVolt);

        //R
        double r = rmsVolt / rmsAmpere;
        evaluation.setR(r);

        //L
        evaluation.setL(t1 * r);

        return evaluation;
    }

    private double rmsVolt(Evaluation evaluation, double seconds) {

        List<Coordinate> data = evaluation.getData();

        //---------------- Find left Amplitude ---------------------
        Coordinate leftAmplitude = data.stream()
                .limit(10000)
                .max((o1, o2) -> Double.compare(o1.getVolt(), o2.getVolt()))
                .get();
        evaluation.addCalculationPoint("v_leftAmplitude", leftAmplitude);

        //---------------- Find left Amplitude ---------------------
        int indexLeftAmplitude = findIndex(data, leftAmplitude);
        Coordinate rightAmplitude = data.stream()
                .skip(indexLeftAmplitude)
                .filter(coordinate -> coordinate.getTime() - leftAmplitude.getTime() >= seconds)
                .findFirst()
                .get();
        evaluation.addCalculationPoint("v_rightAmplitude", rightAmplitude);

        //---------------- Calc RMS ---------------------
        int lowerInterval = findIndex(data, leftAmplitude);
        int upperInterval = findIndex(data, rightAmplitude);
        double periodOfTime = rightAmplitude.getTime() - leftAmplitude.getTime();
        double sampleInterval = evaluation.getMeasures().get(0).getSampleIntervall();

        double rms = IntStream.range(lowerInterval, upperInterval)
                .mapToDouble(i -> data.get(i).getVolt() * sampleInterval / periodOfTime)
                .sum();

        return rms;
    }

    private Coordinate findTRMSCoordinate(Evaluation evaluation) {
        return evaluation.getData().stream()
                .filter(coordinate -> coordinate.getAmpere() >= evaluation.getRmsAmpere())
                .findFirst()
                .get();
    }

    private Coordinate findT63Coordinate(Evaluation evaluation) {
        return evaluation.getData().stream()
                .filter(coordinate -> coordinate.getAmpere() >= evaluation.getRmsAmpere() * 0.63)
                .findFirst()
                .get();
    }


    private static SimpleRegression collectWindowData(Evaluation evaluation, int windowStart, int windowEnd) {
        SimpleRegression regression = new SimpleRegression();
        for(int currentWindowIndex = windowStart; currentWindowIndex < windowEnd; currentWindowIndex++) {
            Coordinate currentCoordinate = evaluation.getData().get(currentWindowIndex);
            regression.addData(currentCoordinate.getTime(), currentCoordinate.getAmpere());
        }
        return regression;
    }

    private static int findIndex(List<Coordinate> evaluationData, Coordinate coordinate) {
        OptionalInt startIndex = IntStream.range(0, evaluationData.size())
                .filter(i -> evaluationData.get(i) == coordinate)
                .findFirst();

        return startIndex.getAsInt();
    }

    private Double rmsAmpere(Evaluation evaluation, Coordinate tStartCoordinate, double seconds) {

        List<Coordinate> evaluationData = evaluation.getData();

        //---------- Sliding Window - Find negative Slope -------------------
        int windowSize = 100;
        int slideStep = 10;
        int startIndex = findIndex(evaluationData, tStartCoordinate);

        Integer firstAmplitudeIndex = null; //Result

        for(int currentStartWindow = startIndex; currentStartWindow < evaluationData.size() / 2; currentStartWindow += slideStep) {
            //Collect Data in Window
            int currentEndWindow = currentStartWindow + windowSize;
            SimpleRegression regression = collectWindowData(evaluation, currentStartWindow, currentEndWindow);

            //Evaluate Data
            boolean isSlopNegative = regression.getSlope() < 0;
            if(isSlopNegative) {
                firstAmplitudeIndex = currentEndWindow;
                break;
            }
        }

        //---------- Finde erste Amplitudenspitze -------------------
        int skipAmplitudeInMeasurePoints = 10000;
        int lastAmplitudeIndex = firstAmplitudeIndex + skipAmplitudeInMeasurePoints;
        Coordinate leftAmplitudeCoordinate = IntStream.range(firstAmplitudeIndex, lastAmplitudeIndex)
                .mapToObj(evaluationData::get)
                .max((o1, o2) -> Double.compare(o1.getAmpere(), o2.getAmpere()))
                .get();
        evaluation.addCalculationPoint("leftAmplitude", leftAmplitudeCoordinate);

        //---------- Finde nächste Amplitudenspitze ------
        Coordinate rightAmplitudeCoordinate = evaluationData.stream()
                .filter(coordinate -> coordinate.getTime() - leftAmplitudeCoordinate.getTime() >= seconds)
                .findFirst()
                .get();
        evaluation.addCalculationPoint("rightAmplitude", rightAmplitudeCoordinate);


        //---------- RMS bestimmen (Integral zwischen beiden Punten) -------------------
        int lowerInterval = findIndex(evaluationData, leftAmplitudeCoordinate);
        int upperInterval = findIndex(evaluationData, rightAmplitudeCoordinate);
        double periodOfTime = rightAmplitudeCoordinate.getTime() - leftAmplitudeCoordinate.getTime();
        double sampleInterval = evaluation.getMeasures().get(0).getSampleIntervall();

        double rms = IntStream.range(lowerInterval, upperInterval)
                .mapToDouble(i -> evaluationData.get(i).getAmpere() * sampleInterval / periodOfTime)
                .sum();

        return rms;
    }

    private Coordinate findT1StartCoordinate(Evaluation evaluation) {

//        List<Coordinate> evaluationData = evaluation.getData();
//        Double sampleInterval = evaluation.getMeasures().get(0).getSampleIntervall();
//
//
//        //---------- Sliding Window - Find T1 Coordinate -------------------
//        int windowSize = 1000;
//        int slideStep = 10;
//        Integer stopValue = null;
//
//        System.out.println("Start");
//        for(int currentStartWindow = 0; currentStartWindow < evaluationData.size() / 2; currentStartWindow += slideStep) {
//            //Collect Data in Window
//            int currentEndWindow = currentStartWindow + windowSize;
//
//            SimpleRegression regression = collectWindowData(evaluation, currentStartWindow, currentEndWindow);
//
//            double mean = IntStream.range(currentStartWindow, currentEndWindow)
//                    .mapToDouble(i -> evaluationData.get(i).getAmpere())
//                    .average()
//                    .getAsDouble();
//
//            if(currentStartWindow == 0) {
//                stopValue = (int) mean + 1;
//            }
//
//            System.out.println(mean);
//            if(mean >= stopValue) {
//                evaluation.addCalculationPoint("t1_new", evaluationData.get(currentEndWindow));
//                break;
//            }
//        }

        Double totalMaxAmpere = evaluation.getData().parallelStream()
                .max((v1, v2) -> Double.compare(v1.getAmpere(), v2.getAmpere()))
                .get()
                .getAmpere();

        Double totalMinAmpere = evaluation.getData().parallelStream()
                .min((v1, v2) -> Double.compare(v1.getAmpere(), v2.getAmpere()))
                .get()
                .getAmpere();

        final Double max = Math.pow(totalMaxAmpere - totalMinAmpere, 2); //min Wert auf 0 setzen

        List<Double> normedMeasurePointsTotal = evaluation.getData().stream()
                .map(Coordinate::getAmpere)             //Strom
                .map(value -> value - totalMinAmpere)   //min Wert auf 0 setzen
                .map(value -> value * value)            //Quadieren für die Genauigkeit
                .map(value -> 100.0f * ((value - 0) / (max - 0))) //Normieren auf 100%
                .collect(Collectors.toList());

        int firstIndex = IntStream.range(0, normedMeasurePointsTotal.size())
                //Erster Wert, der 0.05% Übersteigt. !!Achtung Quadrierung. Es ist nicht linear
                .filter(i -> normedMeasurePointsTotal.get(i) > 0.05)
                .findFirst()
                .getAsInt();

        return evaluation.getData().get(firstIndex-16);
    }
}
