package device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.TelemetryMessage;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import resource.SmartObjectResource;

import java.util.Map;

public abstract class MqttSmartObject {
    protected static final String TELEMETRY_TOPIC = "telemetry";

    protected static final String EVENT_TOPIC = "event";

    protected static final String CONTROL_TOPIC = "control";

    protected static final String COMMAND_TOPIC = "command";

    private String mqttSmartObjectId;

    private ObjectMapper mapper;

    private IMqttClient mqttClient;

    private Map<String, SmartObjectResource<?>> resourceMap;

    public MqttSmartObject() {
        this.mapper = new ObjectMapper();
    }

    abstract void publishTelemetryData(String topic, TelemetryMessage<?> telemetryMessage) throws MqttException, JsonProcessingException;

    public String getMqttSmartObjectId() {
        return mqttSmartObjectId;
    }

    public void setMqttSmartObjectId(String mqttSmartObjectId) {
        this.mqttSmartObjectId = mqttSmartObjectId;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public IMqttClient getMqttClient() {
        return mqttClient;
    }

    public void setMqttClient(IMqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public Map<String, SmartObjectResource<?>> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<String, SmartObjectResource<?>> resourceMap) {
        this.resourceMap = resourceMap;
    }
}
