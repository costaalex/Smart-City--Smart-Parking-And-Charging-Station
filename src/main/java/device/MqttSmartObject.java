package device;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.ControlMessage;
import model.GpsLocationDescriptor;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import resource.SensorResource;

import java.util.Map;
import java.util.Optional;

import static process.SmartObjectProcess.MQTT_USERNAME;

public abstract class MqttSmartObject {
    public static final String BASIC_TOPIC = "iot/user/" + MQTT_USERNAME + "/smartcity";

    public static final String TELEMETRY_TOPIC = "telemetry";

    public static final String EVENT_TOPIC = "event";

    public static final String CONTROL_TOPIC = "control";

    public static final String GENERAL = "general";

    public static final String COMMAND_TOPIC = "command";

    private String mqttSmartObjectId;

    private GpsLocationDescriptor gpsLocation;

    private ObjectMapper mapper;

    private IMqttClient mqttClient;

    private Map<String, SensorResource<?>> resourceMap;

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

    public Map<String, SensorResource<?>> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<String, SensorResource<?>> resourceMap) {
        this.resourceMap = resourceMap;
    }

    protected Optional<ControlMessage<?>> parseControlMessagePayload(MqttMessage mqttMessage){
        try{
            if(mqttMessage == null)
                return Optional.empty();

            byte[] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            return Optional.ofNullable(mapper.readValue(payloadString, new TypeReference<ControlMessage<?>>() {}));

        }catch (Exception e){
            return Optional.empty();
        }
    }


}
