package process;

import device.ChargingStationMqttSmartObject;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.VehiclePresenceSensorResource;

import java.util.HashMap;
import java.util.UUID;

public class ChargingStationSmartObjectProcess {

    private static final Logger logger = LoggerFactory.getLogger(ChargingStationSmartObjectProcess.class);

    private static String MQTT_BROKER_IP = "155.185.228.19";

    private static int MQTT_BROKER_PORT = 7883;

    public static void main(String[] args) {

        try{

            //Generate Random Charging Station UUID
            String chargingStationId = UUID.randomUUID().toString();

            //Create MQTT Client
            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(String.format("tcp://%s:%d",
                    MQTT_BROKER_IP,
                    MQTT_BROKER_PORT),
                    chargingStationId,
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to MQTT Broker
            mqttClient.connect(options);

            logger.info("MQTT Client Connected ! Client Id: {}", chargingStationId);

            ChargingStationMqttSmartObject vehicleMqttSmartObject = new ChargingStationMqttSmartObject();
            vehicleMqttSmartObject.init(chargingStationId, mqttClient, new HashMap<String, resource.SmartObjectResource<?>>(){
                {
                    put("vehiclePresence", new VehiclePresenceSensorResource());
                    //put("battery", new TemperatureSensorResource());
                }
            });

            vehicleMqttSmartObject.start();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
