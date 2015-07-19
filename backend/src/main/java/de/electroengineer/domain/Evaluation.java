package de.electroengineer.domain;

import java.util.ArrayList;
import java.util.List;

public class Evaluation {
    private List<Double> x = new ArrayList<>();
    private List<Double> v = new ArrayList<>();

    public List<Double> getX() {
        return x;
    }

    public void setX(List<Double> x) {
        this.x = x;
    }

    public List<Double> getV() {
        return v;
    }

    public void setV(List<Double> v) {
        this.v = v;
    }

    public List<Double> getA() {
        return a;
    }

    public void setA(List<Double> a) {
        this.a = a;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }

    private List<Double> a = new ArrayList<>();
    private List<Measure> measures = new ArrayList<>();
}
