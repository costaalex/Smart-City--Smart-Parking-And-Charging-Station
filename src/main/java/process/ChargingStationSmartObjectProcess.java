package process;

import device.ChargingStationMqttSmartObject;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.*;

import java.util.HashMap;
import java.util.UUID;

public class ChargingStationSmartObjectProcess {

    private static final Logger logger = LoggerFactory.getLogger(ChargingStationSmartObjectProcess.class);

    //IP Address of the target MQTT Broker
    private static String MQTT_BROKER_IP = "155.185.228.19";

    //PORT of the target MQTT Broker
    private static int MQTT_BROKER_PORT = 7883;

    //MQTT account username to connect to the target broker
    private static final String MQTT_USERNAME = "254892";

    //MQTT account password to connect to the target broker
    private static final String MQTT_PASSWORD = "zpfupimt";

    //Basic Topic used to consume generated demo data (the topic is associated to the user)
    private static final String MQTT_BASIC_TOPIC = "/iot/user/254892/";

    public static void main(String[] args) {
        logger.info("MQTT Auth Consumer Tester Started ...");

        try{

            //Generate Random Charging Station UUID
            String chargingStationId = UUID.randomUUID().toString();

            //Represents a persistent data store, used to store outbound and inbound messages while they
            //are in flight, enabling delivery to the QoS specified. In that case use a memory persistence.
            //When the application stops all the temporary data will be deleted.
            MqttClientPersistence persistence = new MemoryPersistence();

            //The the persistence is not passed to the constructor the default file persistence is used.
            //In case of a file-based storage the same MQTT client UUID should be used
            IMqttClient mqttClient = new MqttClient(
                    String.format("tcp://%s:%d", MQTT_BROKER_IP, MQTT_BROKER_PORT),
                    chargingStationId,
                    persistence);

            //Define MQTT Connection Options such as reconnection, persistent/clean session and connection timeout
            //Authentication option can be added -> See AuthProducer example
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MQTT_USERNAME);
            options.setPassword(new String(MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            //Connect to the target broker
            mqttClient.connect(options);

            logger.info("MQTT Client Connected ! Client Id: {}", chargingStationId);

            //Subscribe to the target topic #. In that case the consumer will receive (if authorized) all the message
            //passing through the broker
            mqttClient.subscribe(MQTT_BASIC_TOPIC + "#", (topic, msg) -> {
                //The topic variable contain the specific topic associated to the received message. Using MQTT wildcards
                //messaged from multiple and different topic can be received with the same subscription
                //The msg variable is a MqttMessage object containing all the information about the received message
                byte[] payload = msg.getPayload();
                logger.info("Message Received ({}) Message Received: {}", topic, new String(payload));
            });


            ChargingStationMqttSmartObject charhingstationMqttSmartObject = new ChargingStationMqttSmartObject();
            charhingstationMqttSmartObject.init(chargingStationId, mqttClient, new HashMap<String, resource.SmartObjectResource<?>>(){
                {
                    put("energy_consumption", new EnergyConsumptionSensorResource());
                    put("temperature", new TemperatureSensorResource());
                    put("vehicle_presence", new VehiclePresenceSensorResource());
                    put("charge_status", new ChargeStatusSensorResource());
                    put("led", new LedActuatorResource());
                }
            });

            charhingstationMqttSmartObject.start();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
