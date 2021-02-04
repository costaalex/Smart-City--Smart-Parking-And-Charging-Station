package process;

import device.ChargingStationMqttSmartObject;

import device.MqttSmartObject;
import device.ParkingLotMqttSmartObject;
import model.GpsLocationDescriptor;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.*;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class SmartObjectProcess {

    private static final Logger logger = LoggerFactory.getLogger(SmartObjectProcess.class);

    //BROKER URL
    private static String BROKER_URL = "tcp://localhost:1883";

    //MQTT account username to connect to the target broker
    public static final String MQTT_USERNAME = "254892";

    //MQTT account password to connect to the target broker
    private static final String MQTT_PASSWORD = "zpfupimt";

    private static final Double MIN_LATITUDE = 44.0;
    private static final Double MAX_LATITUDE = 46.0;
    private static final Double MIN_LONGITUDE = 8.0;
    private static final Double MAX_LONGITUDE = 12.0;
    private static final String CHARGING_STATION = "charging_station";
    private static final String PARKING_LOT = "parking_lot";

    public static void main(String[] args) {
        logger.info("MQTT Auth Producer Tester Started ...");

        try{

            for(int i=0; i<1; i++) {
                createMqttSmartObject(CHARGING_STATION);
            }
/*
            for(int i=0; i<2; i++) {
                createMqttSmartObject(PARKING_LOT);
            }
*/


            //Start to publish MESSAGE_COUNT messages
            //for(int i = 0; i < MESSAGE_COUNT; i++) {
            while(true){
                    //Send data as simple numeric value
               // double sensorValue = engineTemperatureSensor.getTemperatureValue();
               // String payloadString = Double.toString(sensorValue);

                    //Internal Method to publish MQTT data using the created MQTT Client
                    //The final topic is obtained merging the MQTT_BASIC_TOPIC and TOPIC in order to send the messages
                    //to the correct topic root associated to the authenticated user
                    //Eg. /iot/user/000001/sensor/temperature
                //publishData(client, MQTT_BASIC_TOPIC + TOPIC, payloadString);

                //Sleep for 1 Second
               // Thread.sleep(1000);
            }

            //Disconnect from the broker and close the connection
            //mqttClient.disconnect();
            //mqttClient.close();

            //logger.info("Disconnected !");

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private static void createMqttSmartObject(String mqttSmartObjectType){
        try{
            //Generate Random Charging Station UUID
            String mqttSmartObjectId = UUID.randomUUID().toString();
            Random random = new Random(System.currentTimeMillis());
            Double latitude = (MIN_LATITUDE + (MAX_LATITUDE - MIN_LATITUDE) * random.nextDouble());
            Double longitude = (MIN_LONGITUDE + (MAX_LONGITUDE - MIN_LONGITUDE) * random.nextDouble());
            GpsLocationDescriptor gpsLocation  = new GpsLocationDescriptor(latitude, longitude);

            //Represents a persistent data store, used to store outbound and inbound messages while they
            //are in flight, enabling delivery to the QoS specified. In that case use a memory persistence.
            //When the application stops all the temporary data will be deleted.
            MqttClientPersistence persistence = new MemoryPersistence();

            //The the persistence is not passed to the constructor the default file persistence is used.
            //In case of a file-based storage the same MQTT client UUID should be used
            IMqttClient mqttClient = new MqttClient(BROKER_URL, mqttSmartObjectId, persistence);

            //Define MQTT Connection Options such as reconnection, persistent/clean session and connection timeout
            //Authentication option can be added -> See AuthProducer example
            MqttConnectOptions options = new MqttConnectOptions();
            // options.setUserName(MQTT_USERNAME);
            // options.setPassword(new String(MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to the target broker
            mqttClient.connect(options);

            if(mqttSmartObjectType.equals(CHARGING_STATION)) {
                logger.info("Connected new Charging Station - id: {}, lat: {}, long: {}",mqttSmartObjectId, latitude, longitude);

                ChargingStationMqttSmartObject charhingstationMqttSmartObject = new ChargingStationMqttSmartObject();
                charhingstationMqttSmartObject.init(mqttSmartObjectId, gpsLocation, mqttClient, new HashMap<String, resource.SmartObjectResource<?>>() {
                    {
                        put("vehicle_presence", new VehiclePresenceSensorResource());
                        put("charge_status", new ChargeStatusSensorResource());
                        put("temperature", new TemperatureSensorResource());
                        put("energy_consumption", new EnergyConsumptionSensorResource());
                        put("led", new LedActuatorResource());
                    }
                });
                charhingstationMqttSmartObject.start();
            }
            else if(mqttSmartObjectType.equals(PARKING_LOT)){
                logger.info("Connected new Parking Lot - id: {}, lat: {}, long: {}",mqttSmartObjectId, latitude, longitude);

                ParkingLotMqttSmartObject parkingLotMqttSmartObject = new ParkingLotMqttSmartObject();
                parkingLotMqttSmartObject.init(mqttSmartObjectId, gpsLocation, mqttClient, new HashMap<String, resource.SmartObjectResource<?>>(){
                    {
                        put("vehicle_presence", new VehiclePresenceSensorResource());
                        put("led", new LedActuatorResource());
                    }
                });
                parkingLotMqttSmartObject.start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
