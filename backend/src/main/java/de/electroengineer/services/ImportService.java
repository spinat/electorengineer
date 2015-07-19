package de.electroengineer.services;

import com.google.gson.Gson;
import de.electroengineer.domain.Evaluation;
import de.electroengineer.domain.Measure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

@Service
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    public static final String DATA_FOLDER = "data/";
    public static final String SPOOL = "spool";
    public static final String COMPRESS_FILE_EXTENSION = ".gz";
    public static final int BEGIN_MEASURE_DATA = 17;

    public void startImport() throws IOException {

        //Group Measures
        Map<String, List<Path>> evaluations = new HashMap<>();
        Files.walk(Paths.get(SPOOL))
                .filter(path -> Files.isRegularFile(path))
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    String fileGroupName = extractGroupName(fileName);

                    if (evaluations.get(fileGroupName) == null) {
                        evaluations.put(fileGroupName, new ArrayList<>());
                    }

                    evaluations.get(fileGroupName).add(path);
                });

        //Import Group
        for(String key : evaluations.keySet()) {
            createEvaluations(evaluations.get(key));
        }
    }

    private void createEvaluations(List<Path> paths) {
        if(paths.size() < 2) {
            LOG.info("Die Auswertung hat nicht genügend Messungen. paths.size={}", paths.size());
            return;
        }

        //Collect Data
        Evaluation evaluation = new Evaluation();
        paths.stream()
                .forEach(path -> {
                    HashMap<String, Object> metaData = extractMetaDataFromFile(path);
                    Measure measure = createMeasure(path, metaData);
                    switch (measure.getUnit().toLowerCase()) {
                        case "a":
                            evaluation.setA(extractMeasureDataFromFile(path));
                            break;
                        case "v":
                            evaluation.setV(extractMeasureDataFromFile(path));
                            break;
                        default:
                            LOG.error("Unknown measure Unit! Measure.Unit={}", measure.getUnit());
                            return;
                    }
                    evaluation.getMeasures().add(measure);
                });

        evaluation.setX(generateXCoordinates(evaluation));

        Gson gson = new Gson();
        String json = gson.toJson(evaluation);

        String fileName = paths.get(0).getFileName().toString();
        try(FileOutputStream output = new FileOutputStream(DATA_FOLDER + extractGroupName(fileName) + COMPRESS_FILE_EXTENSION)) {
            Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return;
    }

    private List<Double> generateXCoordinates(Evaluation evaluation) {
        Measure maxSampleCountMeasure = evaluation.getMeasures().stream()
                .max((m1, m2) -> Integer.compare(m1.getSampleCount(), m2.getSampleCount()))
                .get();

        if(maxSampleCountMeasure == null) {
            LOG.error("Measures has no sample count. Can not generate x coordinates.");
            return null;
        }

        Integer sampleCount = maxSampleCountMeasure.getSampleCount();
        Double sampleIntervall = maxSampleCountMeasure.getSampleIntervall();

        List<Double> xCoordinates = new ArrayList<>();
        for(int i = 0; i < sampleCount; i++) {
            xCoordinates.add(i * sampleIntervall);
        }
        return xCoordinates;
    }

    private Measure createMeasure(Path path, HashMap<String, Object> metaData) {
        Measure measure = new Measure();

        measure.setMeasureName(path.getFileName().toString());
        measure.setChannelName(getMetaDataAsString("ChannelName", metaData));
        measure.setUnit(getMetaDataAsString("Unit", metaData));
        measure.setSampleIntervall(getMetaDataAsDouble("SampleIntervall", metaData));
        measure.setSampleCount(getMetaDataAsInteger("SampleCount", metaData));
        return measure;
    }


    private static Integer getMetaDataAsInteger(String attributeName, HashMap<String, Object> metaData) {
        return Integer.parseInt(metaData.get(attributeName).toString());
    }

    private static String getMetaDataAsString(String attributeName, HashMap<String, Object> metaData) {
        return metaData.get(attributeName).toString();
    }

    private static Double getMetaDataAsDouble(String attributeName, HashMap<String, Object> metaData) {
        return Double.parseDouble(metaData.get(attributeName).toString());
    }

    private static HashMap<String, Object> extractMetaDataFromFile(Path path) {
        HashMap<String, Object> metaData = new HashMap<String, Object>();
        try(Stream<String> lines = Files.lines(path).onClose(() -> LOG.info("File closed!"))) {
            lines
                    .map((line) -> Arrays.asList(line.split("=")))
                    .filter((data) -> data.size() == 2)
                    .forEach((data) -> metaData.put(data.get(0), data.get(1).replace(",", ".")));
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return metaData;
    }

    private List<Double> extractMeasureDataFromFile(Path path) {
        List<Double> measureData = new ArrayList<>();
        try(Stream<String> lines = Files.lines(path)) {
            measureData = lines
                    .skip(BEGIN_MEASURE_DATA)
                    .map((line) -> {
                        String str_number = line.replace(",", ".");
                        return Double.parseDouble(str_number);
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return measureData;
    }

    public String extractGroupName(String measureName) {
        return measureName.substring(0, measureName.length() - 8);
    }
}
