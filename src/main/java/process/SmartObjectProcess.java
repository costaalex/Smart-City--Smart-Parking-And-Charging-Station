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

    //Message Limit generated and sent by the producer
    private static final int MESSAGE_COUNT = 1000;

    //MQTT account username to connect to the target broker
    private static final String MQTT_USERNAME = "254892";

    //MQTT account password to connect to the target broker
    private static final String MQTT_PASSWORD = "zpfupimt";

    //Basic Topic used to publish generated demo data (the topic is associated to the user)
    private static final String MQTT_BASIC_TOPIC = "/iot/user/254892/";

    //Additional Topic structure used to publish generated demo data. It is merged with the Basic Topic to obtain
    //the final used topic
    private static final String TOPIC = "?????????????";//"sensor/temperature";

    private static final Double MIN_LATITUDE = 44000000.0;
    private static final Double MAX_LATITUDE = 46000000.0;
    private static final Double MIN_LONGITUDE = 8000000.0;
    private static final Double MAX_LONGITUDE = 12000000.0;
    private static final String CHARGING_STATION = "charging_station";
    private static final String PARKING_LOT = "parking_lot";

    public static void main(String[] args) {
        logger.info("MQTT Auth Producer Tester Started ...");

        try{

            for(int i=1; i<5; i++) {
                createChargingStationMqttSmartObject(CHARGING_STATION);
            }

            for(int i=1; i<10; i++) {
                createChargingStationMqttSmartObject(PARKING_LOT);
            }


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
    private static void createChargingStationMqttSmartObject(String mqttSmartObjectType){
        try{
            //Generate Random Charging Station UUID
            String mqttSmartObjectId = UUID.randomUUID().toString();
            Random random = new Random(System.currentTimeMillis());
            Double latitude = (MIN_LATITUDE + (MAX_LATITUDE - MIN_LATITUDE) * random.nextDouble()) / 0.000001;
            Double longitude = (MIN_LONGITUDE + (MAX_LONGITUDE - MIN_LONGITUDE) * random.nextDouble()) / 0.000001;
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
                logger.info("Connected new Charging Station!");

                ChargingStationMqttSmartObject charhingstationMqttSmartObject = new ChargingStationMqttSmartObject();
                charhingstationMqttSmartObject.init(mqttSmartObjectId, gpsLocation, mqttClient, new HashMap<String, resource.SmartObjectResource<?>>() {
                    {
                        put("energy_consumption", new EnergyConsumptionSensorResource());
                        put("temperature", new TemperatureSensorResource());
                        put("vehicle_presence", new VehiclePresenceSensorResource());
                        put("charge_status", new ChargeStatusSensorResource());
                        put("led", new LedActuatorResource());
                    }
                });
                charhingstationMqttSmartObject.start();
            }
            else if(mqttSmartObjectType.equals(PARKING_LOT)){
                logger.info("Connected new Parking Lot!");

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
