package de.electroengineer.services;

import de.electroengineer.domain.Coordinate;
import de.electroengineer.domain.Evaluation;
import de.electroengineer.domain.Measure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class ImportService {

    @Autowired
    FileService fileService;

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    public static final String SPOOL = "spool";

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

        //Geht bestimmt irgendwie hübscher
        List<Double> voltData = new ArrayList<>();
        List<Double> ampereData = new ArrayList<>();

        paths.stream()
                .forEach(path -> {
                    HashMap<String, Object> metaData = extractMetaDataFromFile(path);
                    Measure measure = createMeasure(path, metaData);
                    switch (measure.getUnit().toLowerCase()) {
                        case "a":
                            ampereData.addAll(extractMeasureDataFromFile(path));
                            break;
                        case "v":
                            voltData.addAll(extractMeasureDataFromFile(path));
                            break;
                        default:
                            LOG.error("Unknown measure Unit! Measure.Unit={}", measure.getUnit());
                            return;
                    }
                    evaluation.getMeasures().add(measure);
                });

        List<Coordinate> coordinates = generateCoordinates(voltData, ampereData, evaluation.getMeasures().get(0).getSampleIntervall());
        evaluation.setData(coordinates);
        evaluation.setEvaluationName(extractGroupName(paths.get(0).getFileName().toString()));

        fileService.storeEvaluation(evaluation);

        return;
    }

    private List<Coordinate> generateCoordinates(List<Double> voltData, List<Double> ampereData, Double sampleIntervall) {

        int min = voltData.size() > ampereData.size() ? ampereData.size()  : voltData.size();

        List<Coordinate> coordinates = IntStream.range(0, min)
                .mapToObj(i -> {
                    Coordinate coordinate = new Coordinate();
                    coordinate.setAmpere(ampereData.get(i));
                    coordinate.setVolt(voltData.get(i));
                    coordinate.setTime(i * sampleIntervall);
                    return coordinate;
                })
                .collect(Collectors.toList());

        return coordinates;
    }

    private static Measure createMeasure(Path path, HashMap<String, Object> metaData) {
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
