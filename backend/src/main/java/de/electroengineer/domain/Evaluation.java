package de.electroengineer.domain;

import java.util.ArrayList;
import java.util.List;

public class Evaluation {

    private String evaluationName;
    private Coordinate t1Start;
    private List<Coordinate> test;

    private List<Measure> measures = new ArrayList<>();
    private List<Coordinate> data = new ArrayList<>();

    public List<Coordinate> getTest() {
        return test;
    }

    public void setTest(List<Coordinate> test) {
        this.test = test;
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
