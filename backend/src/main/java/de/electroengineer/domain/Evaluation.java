package de.electroengineer.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluation {

    private String evaluationName;
    private Coordinate t1Start;
    private Map<String, Coordinate> calculationCoordinates = new HashMap<>();

    private List<Measure> measures = new ArrayList<>();
    private List<Coordinate> data = new ArrayList<>();

    public void addCalculationPoint(String name, Coordinate coordinate) {
        calculationCoordinates.put(name, coordinate);
    }

    public Map<String, Coordinate> getCalculationPoints() {
        return calculationCoordinates;
    }

    public void setCalculationPoints(Map<String, Coordinate> calculationPoints) {
        this.calculationCoordinates = calculationPoints;
    }

    public String getEvaluationName() {
        return evaluationName;
    }

    public void setEvaluationName(String evaluationName) {
        this.evaluationName = evaluationName;
    }

    public Coordinate getT1Start() {
        return t1Start;
    }

    public void setT1Start(Coordinate t1Start) {
        this.t1Start = t1Start;
    }

    public List<Coordinate> getData() {
        return data;
    }

    public void setData(List<Coordinate> data) {
        this.data = data;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }
}
