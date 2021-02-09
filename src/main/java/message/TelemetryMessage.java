package message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelemetryMessage<T> {
    @JsonProperty("smartObjectId")
    private String smartObjectId;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("sensorType")
    private String type;

    @JsonProperty("dataValue")
    private T dataValue;

    public TelemetryMessage() {
    }

    public TelemetryMessage(String smartObjectId, String type, T dataValue) {
        this.smartObjectId = smartObjectId;
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.dataValue = dataValue;
    }

    public TelemetryMessage(String smartObjectId, long timestamp, String type, T dataValue) {
        this.smartObjectId = smartObjectId;
        this.timestamp = timestamp;
        this.type = type;
        this.dataValue = dataValue;
    }

    public String getSmartObjectId() {
        return smartObjectId;
    }

    public void setSmartObjectId(String smartObjectId) {
        this.smartObjectId = smartObjectId;
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TelemetryMessage{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", type='").append(type).append('\'');
        sb.append(", dataValue=").append(dataValue);
        sb.append('}');
        return sb.toString();
    }
}
