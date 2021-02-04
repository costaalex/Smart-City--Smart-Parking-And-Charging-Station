package consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.SingletonDataCollector;
import dto.SmartObject;
import message.TelemetryMessage;
import model.GpsLocationDescriptor;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static device.ChargingStationMqttSmartObject.CHARGING_TOPIC;
import static device.ParkingLotMqttSmartObject.PARKING_TOPIC;

public class DataCollectorAndManager {

    private final static Logger logger = LoggerFactory.getLogger(DataCollectorAndManager.class);

    //IP Address of the target MQTT Broker
    private static String BROKER_ADDRESS = "127.0.0.1";

    //PORT of the target MQTT Broker
    private static int BROKER_PORT = 1883;

    private static final String TARGET_TOPIC = "#";

    private static ObjectMapper mapper;

    public static void main(String [ ] args) {

    	logger.info("MQTT Consumer Tester Started ...");

        try{

            //Generate a random MQTT client ID using the UUID class
            String clientId = UUID.randomUUID().toString();

            //Represents a persistent data store, used to store outbound and inbound messages while they
            //are in flight, enabling delivery to the QoS specified. In that case use a memory persistence.
            //When the application stops all the temporary data will be deleted.
            MqttClientPersistence persistence = new MemoryPersistence();

            //The the persistence is not passed to the constructor the default file persistence is used.
            //In case of a file-based storage the same MQTT client UUID should be used
            IMqttClient client = new MqttClient(
                    String.format("tcp://%s:%d", BROKER_ADDRESS, BROKER_PORT), //Create the URL from IP and PORT
                    clientId,
                    persistence);

            //Define MQTT Connection Options such as reconnection, persistent/clean session and connection timeout
            //Authentication option can be added -> See AuthProducer example
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to the target broker
            client.connect(options);

            logger.info("Connected ! Client Id: {}", clientId);

            //Subscribe to the target topic #. In that case the consumer will receive (if authorized) all the message
            //passing through the broker
            client.subscribe(TARGET_TOPIC, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message
            	byte[] payload = msg.getPayload();
                logger.info("Message Received -> Topic: {} - Payload: {}", topic, new String(payload));

                if (topic.contains(CHARGING_TOPIC))
                    updateChargingStationMap(topic, msg);
                else if (topic.contains(PARKING_TOPIC))
                    updateParkingLotMap(topic, msg);

            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private static Optional<TelemetryMessage<?>> parseTelemetryMessagePayload(MqttMessage mqttMessage){
        try{
            if(mqttMessage == null)
                return Optional.empty();

            byte[] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            return Optional.ofNullable(mapper.readValue(payloadString, new TypeReference<TelemetryMessage<?>>() {}));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    private static void updateChargingStationMap(String topic, MqttMessage msg) {
        String[] parts = topic.split("/");
        String smartObjectId = parts[6];

        Optional<TelemetryMessage<?>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);

        if(telemetryMessageOptional.isPresent() ) {
            switch (telemetryMessageOptional.get().getType()) {
                case EnergyConsumptionSensorResource.RESOURCE_TYPE:
                    Double newValue = (Double) telemetryMessageOptional.get().getDataValue();
                    long timestamp = telemetryMessageOptional.get().getTimestamp();
                    String sensor_type = telemetryMessageOptional.get().getType();

                    logger.info("New Energy Consumption Data Received : {}", newValue);

                    //If is the first value
                    if (!SingletonDataCollector.getInstance().chargingStationMap.containsKey(smartObjectId)) {
                        logger.info("New Battery Level Saved for: {}", topic);

                        SmartObject chargingStation = new SmartObject(smartObjectId, new GpsLocationDescriptor(/*latitude, longitude*/));
                        EnergyConsumptionSensorResource sensor = new EnergyConsumptionSensorResource(sensor_type, timestamp, newValue);

                        Map<String, EnergyConsumptionSensorResource> resourceMap = new HashMap<>();
                        resourceMap.put(sensor_type, sensor);

                        SingletonDataCollector.getInstance().chargingStationMap.put(smartObjectId, chargingStation);

                        //isAlarmNotified = false;
                    }

                    break;

                case TemperatureSensorResource.RESOURCE_TYPE:

                    break;
                case VehiclePresenceSensorResource.RESOURCE_TYPE:

                    break;
                case ChargeStatusSensorResource.RESOURCE_TYPE:

                    break;
                case LedActuatorResource.RESOURCE_TYPE:

                    break;
            }
        }
    }

    private static void updateParkingLotMap(String topic, MqttMessage msg) {

    }
}
