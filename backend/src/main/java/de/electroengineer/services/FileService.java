package de.electroengineer.services;

import com.google.gson.Gson;
import de.electroengineer.domain.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class FileService {

    public static final String COMPRESS_FILE_EXTENSION = ".gz";
    public static final String DATA_FOLDER = "data/";

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    public List<String> listAllEvaluations() throws IOException {
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
    
    public Evaluation loadEvaluation(String evaluationName) throws IOException {
        String json = readFileAndDecompress(evaluationName);
        return parseJsonToEvaluation(json);
    }

    public void storeEvaluation(Evaluation evaluation, String fileName) {
        Gson gson = new Gson();
        String json = gson.toJson(evaluation);

        try(FileOutputStream output = new FileOutputStream(DATA_FOLDER + fileName + COMPRESS_FILE_EXTENSION)) {
            Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private String readFileAndDecompress(String evaluationName) throws IOException {
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(DATA_FOLDER + evaluationName + COMPRESS_FILE_EXTENSION));
        Reader decoder = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(decoder);
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    private Evaluation parseJsonToEvaluation(String json) {
        return new Gson().fromJson(json, Evaluation.class);
    }
}
