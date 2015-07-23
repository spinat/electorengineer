package de.electroengineer.services;

import de.electroengineer.domain.Coordinate;
import de.electroengineer.domain.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
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

        return evaluation;
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

        final Double min = totalMinAmpere > 0 ? totalMaxAmpere : 0d;

        List<Double> normedMeasurePointsTotal = evaluation.getData().stream()
                .map(Coordinate::getAmpere)
                .filter(val -> val > 0)
                .map(val -> 100.0f * ((val - min) / (totalMaxAmpere - min)))
                .collect(Collectors.toList());

        OptionalInt firstIndex = IntStream.range(0, normedMeasurePointsTotal.size())
                .filter(i -> normedMeasurePointsTotal.get(i) > 20)
                .findFirst();

        return evaluation.getData().get(firstIndex.getAsInt());
    }

    private Function<Coordinate, Double> normieren(Double max, Double min) {
        return coordinate -> 100.0f * ((coordinate.getAmpere() - min) / (max - min));
    }
}
