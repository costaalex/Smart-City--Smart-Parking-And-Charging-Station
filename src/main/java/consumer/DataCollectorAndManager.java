package consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import device.MqttSmartObject;
import dto.SingletonDataCollector;
import dto.SmartObject;
import message.TelemetryMessage;
import model.ChargeStatusDescriptor;
import model.GpsLocationDescriptor;
import model.Led;
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

            mapper = new ObjectMapper();
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
    private static Optional<GpsLocationDescriptor> parseGeneralMessagePayload(MqttMessage mqttMessage){
        try{
            if(mqttMessage == null)
                return Optional.empty();

            byte[] payloadByteArray = mqttMessage.getPayload();
            String payloadString = new String(payloadByteArray);

            return Optional.ofNullable(mapper.readValue(payloadString, new TypeReference<GpsLocationDescriptor>() {}));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    private static void updateChargingStationMap(String topic, MqttMessage msg) {
        String[] parts = topic.split("/");
        String smartObjectId = parts[5];
        if(topic.contains(MqttSmartObject.GENERAL)){
            Optional<GpsLocationDescriptor> generalMessageOptional = parseGeneralMessagePayload(msg);
            //set gps location to a charging station
            if (generalMessageOptional.isPresent() ) {
                Double latitude = generalMessageOptional.get().getLatitude();
                Double longitude = generalMessageOptional.get().getLongitude();
                logger.info("New Charging Station Gps Location Data Received. lat: {}, long: {}", latitude, longitude);

                if ( !SingletonDataCollector.getInstance().chargingStationMap.containsKey(smartObjectId) ) {
                    SmartObject smartObject = new SmartObject(smartObjectId, new GpsLocationDescriptor(latitude, longitude));
                    SingletonDataCollector.getInstance().chargingStationMap.put(smartObjectId, smartObject);
                }
                else{
                    SingletonDataCollector.getInstance().chargingStationMap.get(smartObjectId).setGpsLocation(new GpsLocationDescriptor(latitude, longitude));
                }
            }
        }
        else if(topic.contains(MqttSmartObject.TELEMETRY_TOPIC)) {
            Optional<TelemetryMessage<?>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);
            SmartObjectResource<?> sensor = null;
            long timestamp = telemetryMessageOptional.get().getTimestamp();
            String sensor_type = telemetryMessageOptional.get().getType();
            if (telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType() != null) {
                switch (telemetryMessageOptional.get().getType()) {
                    case EnergyConsumptionSensorResource.RESOURCE_TYPE:
                        Double newEnergyConsumptionValue = (Double) telemetryMessageOptional.get().getDataValue();
                        sensor = new EnergyConsumptionSensorResource(sensor_type, timestamp, newEnergyConsumptionValue);
                        logger.info("New Energy Consumption Data Received : {}", newEnergyConsumptionValue);
                        break;

                    case TemperatureSensorResource.RESOURCE_TYPE:
                        Double newTemperatureValue = (Double) telemetryMessageOptional.get().getDataValue();
                        sensor = new TemperatureSensorResource(sensor_type, timestamp, newTemperatureValue);
                        logger.info("New Temperature Data Received : {}", newTemperatureValue);
                        break;
                    case VehiclePresenceSensorResource.RESOURCE_TYPE:
                        Boolean newVehiclePresenceValue = (Boolean) telemetryMessageOptional.get().getDataValue();
                        sensor = new VehiclePresenceSensorResource(sensor_type, timestamp, newVehiclePresenceValue);
                        logger.info("New Vehicle Presence Data Received : {}", newVehiclePresenceValue);
                        break;
                    case ChargeStatusSensorResource.RESOURCE_TYPE:
                        ChargeStatusDescriptor newChargeStatusValue = ChargeStatusDescriptor.valueOf(telemetryMessageOptional.get().getDataValue().toString());
                        sensor = new ChargeStatusSensorResource(sensor_type, timestamp, newChargeStatusValue);
                        logger.info("New Charge Status Data Received : {}", newChargeStatusValue);
                        break;
                    case LedActuatorResource.RESOURCE_TYPE:
                        Led newLedValue = Led.valueOf(telemetryMessageOptional.get().getDataValue().toString());
                        sensor = new LedActuatorResource(sensor_type, timestamp, newLedValue);
                        logger.info("New Led Actuator Data Received : {}", newLedValue);
                        break;

                }

                //If is the first value (charge station not exists in DataCollector)
                Map<String, SmartObject> chargingStationMap = SingletonDataCollector.getInstance().chargingStationMap;
                if (!chargingStationMap.containsKey(smartObjectId) && sensor != null) {
                    logger.info("New Energy Consumption Saved for: {}", topic);

                    SmartObject chargingStation = new SmartObject(smartObjectId);
                    Map<String, SmartObjectResource<?>> resourceMap = new HashMap<>();
                    resourceMap.put(sensor_type, sensor);
                    chargingStation.setResourceMap(resourceMap);
                    SingletonDataCollector.getInstance().chargingStationMap.put(smartObjectId, chargingStation);
                }
                else{
                    logger.info("New Energy Consumption Saved for: {}", topic);
                    SingletonDataCollector.getInstance().chargingStationMap.get(smartObjectId).getResourceMap().put(sensor_type, sensor);
                }

            }
        }
    }

    private static void updateParkingLotMap(String topic, MqttMessage msg) {
        String[] parts = topic.split("/");
        String smartObjectId = parts[5];
        if(topic.contains(MqttSmartObject.GENERAL)){
            Optional<GpsLocationDescriptor> generalMessageOptional = parseGeneralMessagePayload(msg);
            //set gps location to a charging station
            if (generalMessageOptional.isPresent() ) {
                Double latitude = generalMessageOptional.get().getLatitude();
                Double longitude = generalMessageOptional.get().getLongitude();
                logger.info("New Parking Lot Gps Location Data Received. lat: {}, long: {}", latitude, longitude);

                if ( !SingletonDataCollector.getInstance().parkingLotMap.containsKey(smartObjectId) ) {
                    SmartObject smartObject = new SmartObject(smartObjectId, new GpsLocationDescriptor(latitude, longitude));
                    SingletonDataCollector.getInstance().parkingLotMap.put(smartObjectId, smartObject);
                }
                else{
                    SingletonDataCollector.getInstance().parkingLotMap.get(smartObjectId).setGpsLocation(new GpsLocationDescriptor(latitude, longitude));
                }
            }
        }
        else if(topic.contains(MqttSmartObject.TELEMETRY_TOPIC)) {
            Optional<TelemetryMessage<?>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);
            SmartObjectResource<?> sensor = null;
            long timestamp = telemetryMessageOptional.get().getTimestamp();
            String sensor_type = telemetryMessageOptional.get().getType();
            if (telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType() != null) {
                switch (telemetryMessageOptional.get().getType()) {
                    case VehiclePresenceSensorResource.RESOURCE_TYPE:
                        Boolean newVehiclePresenceValue = (Boolean) telemetryMessageOptional.get().getDataValue();
                        sensor = new VehiclePresenceSensorResource(sensor_type, timestamp, newVehiclePresenceValue);
                        logger.info("New Vehicle Presence Data Received : {}", newVehiclePresenceValue);
                        break;

                    case LedActuatorResource.RESOURCE_TYPE:
                        Led newLedValue = (Led) telemetryMessageOptional.get().getDataValue();
                        sensor = new LedActuatorResource(sensor_type, timestamp, newLedValue);
                        logger.info("New Led Actuator Data Received : {}", newLedValue);
                        break;
                }

                //If is the first value (parking lot not exists in DataCollector)
                if (!SingletonDataCollector.getInstance().parkingLotMap.containsKey(smartObjectId) && sensor != null) {
                    logger.info("New Energy Consumption Saved for: {}", topic);

                    SmartObject chargingStation = new SmartObject(smartObjectId);
                    Map<String, SmartObjectResource<?>> resourceMap = new HashMap<>();
                    resourceMap.put(sensor_type, sensor);
                    chargingStation.setResourceMap(resourceMap);
                    SingletonDataCollector.getInstance().parkingLotMap.put(smartObjectId, chargingStation);
                }
                else{
                    SingletonDataCollector.getInstance().parkingLotMap.get(smartObjectId).getResourceMap().put(sensor_type, sensor);
                }

            }
        }
    }
}
