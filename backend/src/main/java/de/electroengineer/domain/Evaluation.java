package de.electroengineer.domain;

import java.util.ArrayList;
import java.util.List;

public class Evaluation {

    private List<Measure> measures = new ArrayList<>();
    private List<Coordinate> data = new ArrayList<>();

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
