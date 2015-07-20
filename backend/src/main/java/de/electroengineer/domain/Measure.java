package de.electroengineer.domain;

import java.util.Date;

public class Measure {

    private String measureName;
    private String channelName;
    private Date created = new Date();
    private String unit;
    private Double sampleIntervall;
    private Integer sampleCount;

    public String getMeasureName() {
        return measureName;
    }

    public void setMeasureName(String measureName) {
        this.measureName = measureName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Measure{" +
                "measureName='" + measureName + '\'' +
                ", channelName='" + channelName + '\'' +
                ", created=" + created +
                ", unit='" + unit + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Measure measure = (Measure) o;

        return measureName.equals(measure.measureName);
    }

    @Override
    public int hashCode() {
        return measureName.hashCode();
    }

    public Double getSampleIntervall() {
        return sampleIntervall;
    }

    public void setSampleIntervall(Double sampleIntervall) {
        this.sampleIntervall = sampleIntervall;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }
}
