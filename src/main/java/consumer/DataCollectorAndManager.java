package consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import device.MqttSmartObject;
import dto.SingletonDataCollector;
import dto.SmartObject;
import message.ControlMessage;
import message.TelemetryMessage;
import model.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.*;
import services.AppService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static device.ChargingStationMqttSmartObject.CHARGING_TOPIC;
import static device.MqttSmartObject.CONTROL_TOPIC;
import static device.ParkingLotMqttSmartObject.PARKING_TOPIC;
import static resource.LedActuatorResource.RESOURCE_NAME;

public class DataCollectorAndManager {

    private final static Logger logger = LoggerFactory.getLogger(DataCollectorAndManager.class);

    //IP Address of the target MQTT Broker
    private static String BROKER_ADDRESS = "127.0.0.1";

    //PORT of the target MQTT Broker
    private static int BROKER_PORT = 1883;

    private static final String TARGET_TOPIC = "#";
    private static Integer conta = 0;
    private static ObjectMapper mapper;
    static IMqttClient client;

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
            client = new MqttClient(
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

                updateSmartObjectsMap(topic, msg);

            });

        }catch (Exception e){
            e.printStackTrace();
        }

        Thread thread = new Thread(() -> {
            try {
                new AppService().run(new String[]{"server", args.length > 0 ? args[0] : "configuration.yml"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

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

    private static void updateSmartObjectsMap(String topic, MqttMessage msg) {
        String[] parts = topic.split("/");
        String smartObjectId = parts[5];
        if(topic.contains(MqttSmartObject.GENERAL)){
            Optional<GpsLocationDescriptor> generalMessageOptional = parseGeneralMessagePayload(msg);
            //set gps location to a charging station
            if (generalMessageOptional.isPresent() ) {
                Double latitude = generalMessageOptional.get().getLatitude();
                Double longitude = generalMessageOptional.get().getLongitude();
                logger.info("New Charging Station Gps Location Data Received. lat: {}, long: {}", latitude, longitude);

                if ( !SingletonDataCollector.getInstance().smartObjectsMap.containsKey(smartObjectId) ) {
                    SmartObject smartObject;
                    if(topic.contains(CHARGING_TOPIC))
                        smartObject = new SmartObject(smartObjectId, new GpsLocationDescriptor(latitude, longitude), SmartObjectTypeDescriptor.CHARGING_STATION);
                    else
                        smartObject = new SmartObject(smartObjectId, new GpsLocationDescriptor(latitude, longitude), SmartObjectTypeDescriptor.PARKING_LOT);
                    SingletonDataCollector.getInstance().smartObjectsMap.put(smartObjectId, smartObject);
                }
                else{

                    SingletonDataCollector.getInstance().smartObjectsMap.get(smartObjectId).setGpsLocation(new GpsLocationDescriptor(latitude, longitude));
                }

            }
            Map<String, SmartObject> smartObjectsMap = SingletonDataCollector.getInstance().smartObjectsMap;
            logger.info("SINGLETON New Charging Station Gps Location Data Received. lat: {}, long: {}", SingletonDataCollector.getInstance().smartObjectsMap.get(smartObjectId).getGpsLocation().getLatitude(), SingletonDataCollector.getInstance().smartObjectsMap.get(smartObjectId).getGpsLocation().getLongitude());

        }
        else if(topic.contains(MqttSmartObject.TELEMETRY_TOPIC)) {
            Optional<TelemetryMessage<?>> telemetryMessageOptional = parseTelemetryMessagePayload(msg);
            SensorResource<?> sensor = null;
            long timestamp = telemetryMessageOptional.get().getTimestamp();
            String sensor_type = telemetryMessageOptional.get().getType();

            if (telemetryMessageOptional.isPresent() && telemetryMessageOptional.get().getType() != null) {

                //If is the first value (charge station not exists in DataCollector)
                Map<String, SmartObject> smartObjectsMapSingleton = SingletonDataCollector.getInstance().smartObjectsMap;
                if (!smartObjectsMapSingleton.containsKey(smartObjectId)) {
                    //logger.info("New Energy Consumption Saved for: {}", topic);

                    SmartObject smartObject;
                    if(topic.contains(CHARGING_TOPIC))
                        smartObject = new SmartObject(smartObjectId, SmartObjectTypeDescriptor.CHARGING_STATION);
                    else
                        smartObject = new SmartObject(smartObjectId, SmartObjectTypeDescriptor.PARKING_LOT);

                    Map<String, SensorResource<?>> resourceMap = new HashMap<>();
                    // resourceMap.put(sensor_type, sensor);
                    smartObject.setResourceMap(resourceMap);
                    smartObjectsMapSingleton.put(smartObjectId, smartObject);

                }

                switch (telemetryMessageOptional.get().getType()) {
                    case EnergyConsumptionSensorResource.RESOURCE_TYPE:
                        Double newEnergyConsumptionValue = (Double) telemetryMessageOptional.get().getDataValue();
                        sensor = new EnergyConsumptionSensorResource(telemetryMessageOptional.get().getSmartObjectId(),sensor_type, timestamp, newEnergyConsumptionValue);
                        logger.info("New Energy Consumption Data Received : {}", newEnergyConsumptionValue);
                        break;

                    case TemperatureSensorResource.RESOURCE_TYPE:
                        Double newTemperatureValue = (Double) telemetryMessageOptional.get().getDataValue();
                        sensor = new TemperatureSensorResource(telemetryMessageOptional.get().getSmartObjectId(), sensor_type, timestamp, newTemperatureValue);
                        logger.info("New Temperature Data Received : {}", newTemperatureValue);
                        break;

                    case VehiclePresenceSensorResource.RESOURCE_TYPE:
                        Boolean newVehiclePresenceValue = (Boolean) telemetryMessageOptional.get().getDataValue();
                        sensor = new VehiclePresenceSensorResource(telemetryMessageOptional.get().getSmartObjectId(), sensor_type, timestamp, newVehiclePresenceValue);
                        logger.info("New Vehicle Presence Data Received : {}", newVehiclePresenceValue);

                        if(topic.contains(PARKING_TOPIC))
                            updateParkingDurationAverage(newVehiclePresenceValue, timestamp, smartObjectId);

                        break;
                    case ChargeStatusSensorResource.RESOURCE_TYPE:
                        ChargeStatusDescriptor newChargeStatusValue = ChargeStatusDescriptor.valueOf(telemetryMessageOptional.get().getDataValue().toString());
                        sensor = new ChargeStatusSensorResource(telemetryMessageOptional.get().getSmartObjectId(), sensor_type, timestamp, newChargeStatusValue);
                        logger.info("New Charge Status Data Received : {}", newChargeStatusValue);

                        updateChargingDurationAverage(newChargeStatusValue, timestamp, smartObjectId);

                        break;
                    case LedActuatorResource.RESOURCE_TYPE:
                        Led newLedValue = Led.valueOf(telemetryMessageOptional.get().getDataValue().toString());
                        sensor = new LedActuatorResource(telemetryMessageOptional.get().getSmartObjectId(), sensor_type, timestamp, newLedValue);

                        logger.info("New Led Actuator Data Received: {}", newLedValue);
                        break;
                }

                logger.info("New Sensor Saved for: {}", topic);
                smartObjectsMapSingleton.get(smartObjectId).getResourceMap().put(sensor_type, sensor);

                //System.err.println("ciao" + smartObjectsMapSingleton.values());
              /*  if(conta==100) {
                    AverageChargingDurationDescriptor acdd = SingletonDataCollector.getInstance().averageChargingDurationDescriptor;
                    Map<String, SmartObject> som= SingletonDataCollector.getInstance().smartObjectsMap;
                    logger.info("mappa: {}", som.toString());
                }
*/
                conta++;
            }
        }
    }
    private static void updateChargingDurationAverage(ChargeStatusDescriptor newChargeStatusValue, long timestamp, String smartObjectId){
        //add charging duration to a specific charging station and calculate new average
        Double lastChargingDurationForStationSeconds = ((AverageChargingDurationDescriptor) SingletonDataCollector.getInstance().smartObjectsMap.get(smartObjectId).getAverageDurationDescriptor()).addChargingDurationFromStatusAndTimestamp(newChargeStatusValue, timestamp);
        logger.info("New Average Charging duration for: {}: {} seconds.", smartObjectId,  ((AverageChargingDurationDescriptor) SingletonDataCollector.getInstance().smartObjectsMap.get(smartObjectId).getAverageDurationDescriptor()).getAverageChargingDurationSeconds());

        //calculate new overall charging average
        if(lastChargingDurationForStationSeconds != 0) {
            Double newOverallAverageChargingDuration = SingletonDataCollector.getInstance().averageChargingDurationDescriptor.addChargingDurationSeconds(lastChargingDurationForStationSeconds);
            logger.info("New Overall Charging Average {} seconds.", newOverallAverageChargingDuration);
        }
    }
    private static void updateParkingDurationAverage(Boolean newParkingStatusValue, long timestamp, String smartObjectId){
        Double lastParkingDurationForParkingLotSeconds = ((AverageParkingDurationDescriptor) SingletonDataCollector.getInstance().smartObjectsMap.get(smartObjectId).getAverageDurationDescriptor()).addParkingDurationFromStatusAndTimestamp(newParkingStatusValue, timestamp);
        logger.info("New Average Parking duration for: {}: {} seconds.", smartObjectId, ((AverageParkingDurationDescriptor) SingletonDataCollector.getInstance().smartObjectsMap.get(smartObjectId).getAverageDurationDescriptor()).getAverageParkingDurationSeconds());
        //calculate new overall charging average

        if(lastParkingDurationForParkingLotSeconds != 0) {
            Double newOverallAverageParkingDuration = SingletonDataCollector.getInstance().averageParkingDurationDescriptor.addParkingDurationSeconds(lastParkingDurationForParkingLotSeconds);
            logger.info("New Overall Parking Average {} seconds.", newOverallAverageParkingDuration);
        }
    }

    public static void publishControlData(String idSmartObject, ControlMessage<?> controlMessage) throws MqttException, JsonProcessingException {

        SmartObjectTypeDescriptor smartObjectTypeDescriptor = SingletonDataCollector.getInstance().getSmartObjectTypeFromId(idSmartObject);
        String topic = null;
        switch (smartObjectTypeDescriptor){
            case PARKING_LOT:
                topic = String.format("%s/%s/%s/%s", PARKING_TOPIC, idSmartObject, CONTROL_TOPIC, RESOURCE_NAME);
                break;
            case CHARGING_STATION:
                topic = String.format("%s/%s/%s/%s", CHARGING_TOPIC, idSmartObject, CONTROL_TOPIC, RESOURCE_NAME);
                break;
        }
        logger.info("Sending to topic: {} -> Data: {}", topic, controlMessage);

        if(client != null && client.isConnected() && controlMessage != null && topic != null){

            String messagePayload = mapper.writeValueAsString(controlMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(2);

            client.publish(topic, mqttMessage);

            logger.info("Data Correctly Published to idSmartObject: {}", idSmartObject);

        }
        else
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
    }
}
