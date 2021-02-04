package device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.TelemetryMessage;
import model.ChargeStatusDescriptor;
import model.GpsLocationDescriptor;
import model.Led;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.*;

import java.util.Map;

public class ParkingLotMqttSmartObject extends MqttSmartObject{
        private static final Logger logger = LoggerFactory.getLogger(ChargingStationMqttSmartObject.class);

        public static final String PARKING_TOPIC = BASIC_TOPIC + "/parkinglot";

        /**
         * Init the charging station smart object with its ID, the MQTT Client and the Map of managed resources
         * @param chargingStationId
         * @param mqttClient
         * @param resourceMap
         */
        public void init(String chargingStationId, GpsLocationDescriptor gpsLocation, IMqttClient mqttClient, Map<String, SmartObjectResource<?>> resourceMap){

            super.setMqttSmartObjectId(chargingStationId);
            super.setGpsLocation(gpsLocation);
            this.setMqttClient(mqttClient);
            this.setResourceMap(resourceMap);

            logger.info("Charging Station Smart Object correctly created ! Resource Number: {}", resourceMap.keySet().size());
        }

        /**
         * Start vehicle behaviour
         */
        public void start(){

            try{

                if(super.getMqttClient() != null &&
                        super.getMqttSmartObjectId() != null  && super.getMqttSmartObjectId().length() > 0 &&
                        super.getResourceMap() != null && super.getResourceMap().keySet().size() > 0){

                    logger.info("Starting Charging Station Emulator ....");

                    registerToControlChannel();

                    registerToAvailableResources();

                }

            }catch (Exception e){
                logger.error("Error Starting the Charging Station Emulator ! Msg: {}", e.getLocalizedMessage());
            }

        }
    protected void registerToControlChannel() {
        final String[] a = new String[0];
        try{
            String deviceControlTopic = String.format("%s/%s/%s", PARKING_TOPIC, getMqttSmartObjectId(), CONTROL_TOPIC);

            logger.info("Registering to Control Topic ({}) ... ", deviceControlTopic);

            getMqttClient().subscribe(deviceControlTopic, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    if (message != null){
                        logger.info("[CONTROL CHANNEL] -> Control Message Received -> {}", new String(message.getPayload()));
                        // TODO set led color from payload
                        a[0] = "green";  // red, yellow
                    }
                    else
                        logger.error("[CONTROL CHANNEL] -> Null control message received !");
                }
            });
            if(a[0].equals("green"))
                ((LedActuatorResource) super.getResourceMap().get("led")).setIsActive(Led.GREEN);
            else if(a[0].equals("red"))
                ((LedActuatorResource) super.getResourceMap().get("led")).setIsActive(Led.RED);
            else if(a[0].equals("yellow"))
                ((LedActuatorResource) super.getResourceMap().get("led")).setIsActive(Led.YELLOW);

        }catch (Exception e){
            logger.error("ERROR Registering to Control Channel ! Msg: {}", e.getLocalizedMessage());
        }
    }

        private void registerToAvailableResources(){
            try{

                super.getResourceMap().entrySet().forEach(resourceEntry -> {

                    if(resourceEntry.getKey() != null && resourceEntry.getValue() != null){
                        SmartObjectResource smartObjectResource = resourceEntry.getValue();

                        logger.info("Registering to Resource {} (id: {}) notifications ...",
                                smartObjectResource.getType(),
                                smartObjectResource.getId());

                        //Register to VehiclePresenceResource Notification
                        if(smartObjectResource.getType().equals(VehiclePresenceSensorResource.RESOURCE_TYPE)){

                            VehiclePresenceSensorResource vehiclePresenceSensorResource = (VehiclePresenceSensorResource)smartObjectResource;
                            vehiclePresenceSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
                                @Override
                                public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                                    try {
                                        publishTelemetryData(
                                                String.format("%s/%s/%s/%s", PARKING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                                new TelemetryMessage<>(smartObjectResource.getType(), updatedValue));
                                    } catch (MqttException | JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                        //Register to LedActuatorResource         -- Led
                        if(smartObjectResource.getType().equals(LedActuatorResource.RESOURCE_TYPE)){

                            LedActuatorResource ledActuatorResource = (LedActuatorResource)smartObjectResource;
                            ledActuatorResource.addDataListener(new ResourceDataListener<Led>() {
                                @Override
                                public void onDataChanged(SmartObjectResource<Led> resource, Led updatedValue) {
                                    try {
                                        publishTelemetryData(
                                                String.format("%s/%s/%s/%s", PARKING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                                new TelemetryMessage<>(smartObjectResource.getType(), updatedValue));
                                    } catch (MqttException | JsonProcessingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    }
                });

            }catch (Exception e){
                logger.error("Error Registering to Resource ! Msg: {}", e.getLocalizedMessage());
            }
        }
    public void publishTelemetryData(String topic, TelemetryMessage<?> telemetryMessage) throws MqttException, JsonProcessingException {

        logger.info("Sending to topic: {} -> Data: {}", topic, telemetryMessage);

        if(getMqttClient() != null && getMqttClient().isConnected() && telemetryMessage != null && topic != null){

            String messagePayload = getMapper().writeValueAsString(telemetryMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(2);

            getMqttClient().publish(topic, mqttMessage);

            logger.info("Data Correctly Published to topic: {}", topic);

        }
        else
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
    }
        /**
         * Stop the emulated Smart Object
         */
        public void stop(){
            //TODO Implement a proper closing method
        }


}
