package de.electroengineer.services;

import com.google.gson.Gson;
import de.electroengineer.domain.Evaluation;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

@Service
public class EvaluationService {

    public static final String DATA_FOLDER = "data/";
    public static final String COMPRESS_FILE_EXENTSION = ".gz";

    public Evaluation getEvaluation(String evaluationName) throws IOException {
        String json = readFileAndDecompress(evaluationName);
        Evaluation evaluation = parseJsonToEvaluation(json);

        minimizeCoordinates(evaluation);

        return evaluation;
    }

    public List<String> findAllEvaluations() throws IOException {
        List<String> evaluations = Files.walk(Paths.get(DATA_FOLDER))
                .filter(path -> Files.isRegularFile(path))
                .map(path -> path.getFileName().toString())
                .map(filename -> filename.substring(0, filename.length() - 3))
                .collect(Collectors.toList());

        if(evaluations == null) {
            evaluations = new ArrayList<>();
        }

        return evaluations;
    }
    
    private void minimizeCoordinates(Evaluation evaluation) {

        int countMeasurePoints = evaluation.getX().size();
        int skip = countMeasurePoints / 1000;

        List<Double> x = generatePreview(evaluation.getX(), skip);
        List<Double> v = generatePreview(evaluation.getV(), skip);
        List<Double> a = generatePreview(evaluation.getA(), skip);

        evaluation.setX(x);
        evaluation.setV(v);
        evaluation.setA(a);
    }

    private List<Double> generatePreview(List<Double> measurePoints, int skip) {
        return IntStream.range(0, measurePoints.size())
                .filter(i -> i % skip == 0)
                .mapToObj(measurePoints::get)
                .collect(Collectors.toList());
    }

    private Evaluation parseJsonToEvaluation(String json) {
        return new Gson().fromJson(json, Evaluation.class);
    }

    private static String readFileAndDecompress(String evaluationName) throws IOException {
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(DATA_FOLDER + evaluationName + COMPRESS_FILE_EXENTSION));
        Reader decoder = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(decoder);
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }
}
