package de.electroengineer.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluation {

    public enum NormalizeType { NORMAL, RMS, MAX }

    private String evaluationName;
    private Double rmsAmpere;
    private Double rmsVolt;
    private Double r;
    private Double l;
    private Double t1;
    private Double t2;
    private Double voltMax;
    private Double ampereMax;

    private Double rmsVoltPeriodMs = 10d;
    private Double rmsAmperePeriodMs = 10d;
    private NormalizeType normalizeMode = NormalizeType.NORMAL;

    private List<Measure> measures = new ArrayList<>();
    private List<Coordinate> data = new ArrayList<>();
    private Map<String, Coordinate> calculationCoordinates = new HashMap<>();

    public NormalizeType getNormalizeMode() {
        return normalizeMode;
    }

    public void setNormalizeMode(NormalizeType normalizeMode) {
        this.normalizeMode = normalizeMode;
    }

    public Double getVoltMax() {
        return voltMax;
    }

    public void setVoltMax(Double voltMax) {
        this.voltMax = voltMax;
    }

    public Double getAmpereMax() {
        return ampereMax;
    }

    public void setAmpereMax(Double ampereMax) {
        this.ampereMax = ampereMax;
    }

    public Double getRmsVoltPeriodMs() {
        return rmsVoltPeriodMs;
    }

    public void setRmsVoltPeriodMs(Double rmsVoltPeriodMs) {
        this.rmsVoltPeriodMs = rmsVoltPeriodMs;
    }

    public Double getRmsAmperePeriodMs() {
        return rmsAmperePeriodMs;
    }

    public void setRmsAmperePeriodMs(Double rmsAmperePeriodMs) {
        this.rmsAmperePeriodMs = rmsAmperePeriodMs;
    }

    public Double getL() {
        return l;
    }

    public void setL(Double l) {
        this.l = l;
    }

    public Double getR() {
        return r;
    }

    public void setR(Double r) {
        this.r = r;
    }

    public Double getRmsVolt() {
        return rmsVolt;
    }

    public void setRmsVolt(Double rmsVolt) {
        this.rmsVolt = rmsVolt;
    }

    public Double getT2() {
        return t2;
    }

    public void setT2(Double t2) {
        this.t2 = t2;
    }

    public Double getT1() {
        return t1;
    }

    public void setT1(Double t1) {
        this.t1 = t1;
    }

    public Double getRmsAmpere() {
        return rmsAmpere;
    }

    public void setRmsAmpere(Double rmsAmpere) {
        this.rmsAmpere = rmsAmpere;
    }

    public void addCalculationPoint(String name, Coordinate coordinate) {
        calculationCoordinates.put(name, coordinate);
    }

    public Map<String, Coordinate> getCalculationCoordinates() {
        return calculationCoordinates;
    }

    public void setCalculationCoordinates(Map<String, Coordinate> calculationCoordinates) {
        this.calculationCoordinates = calculationCoordinates;
    }

    public String getEvaluationName() {
        return evaluationName;
    }

    public void setEvaluationName(String evaluationName) {
        this.evaluationName = evaluationName;
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
