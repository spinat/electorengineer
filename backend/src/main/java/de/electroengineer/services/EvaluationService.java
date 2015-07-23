package de.electroengineer.services;

import de.electroengineer.domain.Coordinate;
import de.electroengineer.domain.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.OptionalInt;
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
        Coordinate t1StartCoordinate = findT1StartCoordinate(evaluation);
        evaluation.setT1Start(t1StartCoordinate);

        List<Coordinate> coordinates = rmsAmpere(evaluation, t1StartCoordinate);
        evaluation.setTest(coordinates);

        return evaluation;
    }

    private List<Coordinate> rmsAmpere(Evaluation evaluation, Coordinate t1StartCoordinate) {
        SlidingWindow slidingWindow = new SlidingWindow();
        return slidingWindow.maximumTurningPoint(evaluation, t1StartCoordinate);
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
