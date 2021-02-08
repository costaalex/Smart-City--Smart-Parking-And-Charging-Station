package process;

import device.ChargingStationMqttSmartObject;

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
    private static final Integer NUM_CHARGING_STATIONS = 2;
    private static final Integer NUM_PARKING_LOT = 2;

    public static void main(String[] args) {
        logger.info("MQTT Auth Producer Tester Started ...");

        try{

            for(int i=0; i<NUM_CHARGING_STATIONS; i++) {
                createMqttSmartObject(CHARGING_STATION);
            }

            for(int i=0; i<NUM_PARKING_LOT; i++) {
                createMqttSmartObject(PARKING_LOT);
            }

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

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(BROKER_URL, mqttSmartObjectId, persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            // options.setUserName(MQTT_USERNAME);
            // options.setPassword(new String(MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to the target broker
            mqttClient.connect(options);

            if(mqttSmartObjectType.equalsIgnoreCase(CHARGING_STATION)) {
                logger.info("Connected new Charging Station - id: {}, lat: {}, long: {}",mqttSmartObjectId, latitude, longitude);

                ChargingStationMqttSmartObject charhingstationMqttSmartObject = new ChargingStationMqttSmartObject();
                charhingstationMqttSmartObject.init(mqttSmartObjectId, gpsLocation, mqttClient, new HashMap<String, SensorResource<?>>() {
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
            else if(mqttSmartObjectType.equalsIgnoreCase(PARKING_LOT)){
                logger.info("Connected new Parking Lot - id: {}, lat: {}, long: {}",mqttSmartObjectId, latitude, longitude);

                ParkingLotMqttSmartObject parkingLotMqttSmartObject = new ParkingLotMqttSmartObject();
                parkingLotMqttSmartObject.init(mqttSmartObjectId, gpsLocation, mqttClient, new HashMap<String, SensorResource<?>>(){
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
