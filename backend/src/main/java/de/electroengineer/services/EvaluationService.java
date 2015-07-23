package de.electroengineer.services;

import com.google.gson.Gson;
import de.electroengineer.domain.Coordinate;
import de.electroengineer.domain.Evaluation;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

@Service
public class EvaluationService {

    public static final String DATA_FOLDER = "data/";
    public static final String COMPRESS_FILE_EXENTSION = ".gz";

    public Evaluation getEvaluation(String evaluationName) throws IOException {
        String json = readFileAndDecompress(evaluationName);
        Evaluation evaluation = parseJsonToEvaluation(json);

        generatePreviewData(evaluation);

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
    
    private void generatePreviewData(Evaluation evaluation) {
        int countMeasurePoints = evaluation.getData().size();
        int skip = countMeasurePoints / 1000;

        List<Coordinate> coordinates = IntStream.range(0, evaluation.getData().size())
                .filter(i -> i % skip == 0)
                .mapToObj(i -> evaluation.getData().get(i))
                .collect(Collectors.toList());

        evaluation.setData(coordinates);
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
