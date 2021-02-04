package device;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.GpsLocationDescriptor;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.slf4j.Logger;
import resource.SmartObjectResource;

import java.util.Map;

public abstract class MqttSmartObject {
    protected static final String BASIC_TOPIC = "smartcity";

    protected static final String TELEMETRY_TOPIC = "telemetry";

    protected static final String EVENT_TOPIC = "event";

    protected static final String CONTROL_TOPIC = "control";

    protected static final String COMMAND_TOPIC = "command";

    private String mqttSmartObjectId;

    private GpsLocationDescriptor gpsLocation;

    private ObjectMapper mapper;

    private IMqttClient mqttClient;

    private Map<String, SmartObjectResource<?>> resourceMap;

    private Logger logger;

    public MqttSmartObject() {
        this.mapper = new ObjectMapper();
    }

    public MqttSmartObject(String mqttSmartObjectId, GpsLocationDescriptor gpsLocation) {
        this.mqttSmartObjectId = mqttSmartObjectId;
        this.gpsLocation = gpsLocation;
        this.mapper = new ObjectMapper();
    }

    public String getMqttSmartObjectId() {
        return mqttSmartObjectId;
    }

    public void setMqttSmartObjectId(String mqttSmartObjectId) {
        this.mqttSmartObjectId = mqttSmartObjectId;
    }

    public GpsLocationDescriptor getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(GpsLocationDescriptor gpsLocation) {
        this.gpsLocation = gpsLocation;
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
