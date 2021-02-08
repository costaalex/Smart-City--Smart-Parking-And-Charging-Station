package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SensorDescriptor<T>  {
    @JsonProperty("sensorId")
    private String sensorId;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("type")
    private String type;

    @JsonProperty("dataValue")
    private T dataValue;

    public SensorDescriptor() {
    }

    public SensorDescriptor(String sensorId, long timestamp, String type, T dataValue) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.type = type;
        this.dataValue = dataValue;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getDataValue() {
        return dataValue;
    }

    public void setDataValue(T dataValue) {
        this.dataValue = dataValue;
    }
}
