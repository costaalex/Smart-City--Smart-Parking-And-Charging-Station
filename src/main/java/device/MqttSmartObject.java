package device;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.ControlMessage;
import model.GpsLocationDescriptor;
import model.Led;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import resource.LedActuatorResource;
import resource.SensorResource;

import java.util.Map;
import java.util.Optional;

import static process.SmartObjectProcess.MQTT_USERNAME;

public abstract class MqttSmartObject implements IMqttMessageListener {
    public static final String BASIC_TOPIC = "iot/user/" + MQTT_USERNAME + "/smartcity";

    public static final String TELEMETRY_TOPIC = "telemetry";

    public static final String CONTROL_TOPIC = "control";

    public static final String GENERAL = "general";


    private String mqttSmartObjectId;

    private GpsLocationDescriptor gpsLocation;

    private ObjectMapper mapper;

    private IMqttClient mqttClient;

    private Map<String, SensorResource<?>> resourceMap;

    private Logger logger;

    public MqttSmartObject() {
        this.mapper = new ObjectMapper();
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
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
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

        if (mqttMessage != null) {
            logger.info("[CONTROL CHANNEL] -> Control Message Received -> {}", new String(mqttMessage.getPayload()));
            Optional<ControlMessage<?>> generalMessageOptional = parseControlMessagePayload(mqttMessage);

            if (generalMessageOptional.isPresent()) {
                LedActuatorResource ledReceived = (LedActuatorResource) getResourceMap().get("led");
                if (generalMessageOptional.get().getDataValue().equals("YELLOW"))
                    ledReceived.setIsActive(Led.YELLOW);
                else if (generalMessageOptional.get().getDataValue().equals("GREEN"))
                    ledReceived.setIsActive(Led.GREEN);
                if (generalMessageOptional.get().getDataValue().equals("RED"))
                    ledReceived.setIsActive(Led.RED);
            } else
                logger.error("[CONTROL CHANNEL] -> Null control message received !");
        }
    }


}
