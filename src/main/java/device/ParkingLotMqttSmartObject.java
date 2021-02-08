package device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.ControlMessage;
import message.TelemetryMessage;
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
import java.util.Optional;

import static resource.LedActuatorResource.RESOURCE_NAME;

public class ParkingLotMqttSmartObject extends MqttSmartObject {


        public static final String PARKING_TOPIC = BASIC_TOPIC + "/parking_lot";

        /**
         * Init the charging station smart object with its ID, the MQTT Client and the Map of managed resources
         * @param chargingStationId
         * @param mqttClient
         * @param resourceMap
         */
        public void init(String chargingStationId, GpsLocationDescriptor gpsLocation, IMqttClient mqttClient, Map<String, SensorResource<?>> resourceMap){

            super.setMqttSmartObjectId(chargingStationId);
            super.setGpsLocation(gpsLocation);
            super.setLogger(LoggerFactory.getLogger(ParkingLotMqttSmartObject.class));
            super.getLogger().info("Parking Lot Smart Object correctly created ! Resource Number: {}", resourceMap.keySet().size());
            super.setMqttClient(mqttClient);
            super.setResourceMap(resourceMap);
        }

        /**
         * Start Smart Object behaviour
         */
        public void start(){

            try{

                if(super.getMqttClient() != null &&
                        super.getMqttSmartObjectId() != null  && super.getMqttSmartObjectId().length() > 0 &&
                        super.getResourceMap() != null && super.getResourceMap().keySet().size() > 0){

                    super.getLogger().info("Starting Charging Station Emulator ....");

                    registerToControlChannel();

                    registerToAvailableResources();

                    try {
                        publishGeneralData(
                                String.format("%s/%s/%s", PARKING_TOPIC, getMqttSmartObjectId(), GENERAL), super.getGpsLocation());
                    } catch (MqttException | JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }

            }catch (Exception e){
                super.getLogger().error("Error Starting the Parking Lot Emulator ! Msg: {}", e.getLocalizedMessage());
            }

        }
    protected void registerToControlChannel() {


        try{
            String deviceControlTopic = String.format("%s/%s/%s/%s", PARKING_TOPIC, getMqttSmartObjectId(), CONTROL_TOPIC, LedActuatorResource.RESOURCE_NAME);

            super.getLogger().info("Parking Lot Mqtt Registering to Control Topic ({}) ... ", deviceControlTopic);

            getMqttClient().subscribe(deviceControlTopic, this);  //subscribe to control topic for receiving the Led actuator update

        }catch (Exception e){
            super.getLogger().error("ERROR Registering to Control Channel ! Msg: {}", e.getLocalizedMessage());
        }
    }


    private void registerToAvailableResources(){
        try{

            super.getResourceMap().entrySet().forEach(resourceEntry -> {

                if(resourceEntry.getKey() != null && resourceEntry.getValue() != null){
                    SensorResource sensorResource = resourceEntry.getValue();

                    super.getLogger().info("Registering to Resource {} (id: {}) notifications ...",
                            sensorResource.getType(),
                            sensorResource.getId());

                    //Register to VehiclePresenceResource Notification
                    if(sensorResource.getType().equalsIgnoreCase(VehiclePresenceSensorResource.RESOURCE_TYPE)){

                        VehiclePresenceSensorResource vehiclePresenceSensorResource = (VehiclePresenceSensorResource) sensorResource;
                        vehiclePresenceSensorResource.addDataListener((ResourceDataListener<Boolean>) super.getResourceMap().get("led"));  //LedActuator listens for changes in VehiclePresence
                        vehiclePresenceSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
                            @Override
                            public void onDataChanged(SensorResource<Boolean> resource, Boolean updatedValue) {
                                try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", PARKING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(sensorResource.getId(), sensorResource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //Register to LedActuatorResource         -- Led
                    if(sensorResource.getType().equalsIgnoreCase(LedActuatorResource.RESOURCE_TYPE)){

                        LedActuatorResource ledActuatorResource = (LedActuatorResource) sensorResource;
                        ledActuatorResource.addDataListener((ResourceDataListener<Led>) super.getResourceMap().get("vehicle_presence"));
                        ledActuatorResource.addDataListener(new ResourceDataListener<Led>() {
                            @Override
                            public void onDataChanged(SensorResource<Led> resource, Led updatedValue) {
                                try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", PARKING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(resource.getId(), sensorResource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }
            });

        }catch (Exception e){
            super.getLogger().error("Error Registering to Resource ! Msg: {}", e.getLocalizedMessage());
        }
    }
    public void publishTelemetryData(String topic, TelemetryMessage<?> telemetryMessage) throws MqttException, JsonProcessingException {

        super.getLogger().info("Sending to topic: {} -> Data: {}", topic, telemetryMessage);

        if(getMqttClient() != null && getMqttClient().isConnected() && telemetryMessage != null && topic != null){

            String messagePayload = getMapper().writeValueAsString(telemetryMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(2);

            getMqttClient().publish(topic, mqttMessage);

            super.getLogger().info("Data Correctly Published to topic: {}", topic);

        }
        else
            super.getLogger().error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
    }

    public void publishGeneralData(String topic, GpsLocationDescriptor gpsLocationDescriptor) throws MqttException, JsonProcessingException {

        super.getLogger().info("Sending to topic: {} -> Data: {}", topic, gpsLocationDescriptor);

        if(getMqttClient() != null && getMqttClient().isConnected() && gpsLocationDescriptor != null && topic != null){

            String messagePayload = getMapper().writeValueAsString(gpsLocationDescriptor);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(2);
            mqttMessage.setRetained(true);                                              //Send when client connects

            getMqttClient().publish(topic, mqttMessage);

            super.getLogger().info("Data Correctly Published to topic: {}", topic);

        }
        else
            super.getLogger().error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
    }

    /**
     * Stop the emulated Smart Object
     */
    public void stop(){
        //TODO Implement a proper closing method
    }

}
