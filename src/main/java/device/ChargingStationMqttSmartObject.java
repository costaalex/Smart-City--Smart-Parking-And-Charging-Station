package device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.ControlMessage;
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
import java.util.Optional;

public class ChargingStationMqttSmartObject extends MqttSmartObject{

    private static final Logger logger = LoggerFactory.getLogger(ChargingStationMqttSmartObject.class);

    public static final String CHARGING_TOPIC = BASIC_TOPIC + "/charging_station";

    private static final Integer SLEEP_TIME = 50;

    public ChargingStationMqttSmartObject() {
        super.setMapper(new ObjectMapper());
    }

    /**
     * Init the charging station smart object with its ID, the MQTT Client and the Map of managed resources
     * @param chargingStationId
     * @param mqttClient
     * @param resourceMap
     */
    public void init(String chargingStationId, GpsLocationDescriptor gpsLocation, IMqttClient mqttClient, Map<String, SensorResource<?>> resourceMap){

        super.setMqttSmartObjectId(chargingStationId);
        super.setGpsLocation(gpsLocation);
        super.setMqttClient(mqttClient);
        super.setResourceMap(resourceMap);

        logger.info("Charging Station Smart Object correctly created ! Resource Number: {}", resourceMap.keySet().size());
    }

    /**
     * Start Smart Object behaviour
     */
    public void start(){

        try{

            if(super.getMqttClient() != null &&
                super.getMqttSmartObjectId() != null  && super.getMqttSmartObjectId().length() > 0 &&
                super.getResourceMap() != null && super.getResourceMap().keySet().size() > 0){

                logger.info("Starting Charging Station Emulator ....");

                registerToControlChannel();

                registerToAvailableResources();

                try {
                    publishGeneralData(
                            String.format("%s/%s/%s", CHARGING_TOPIC, getMqttSmartObjectId(), GENERAL),
                            super.getGpsLocation());
                } catch (MqttException | JsonProcessingException e) {
                    e.printStackTrace();
                }

            }

        }catch (Exception e){
            logger.error("Error Starting the Charging Station Emulator ! Msg: {}", e.getLocalizedMessage());
        }

    }
    protected void registerToControlChannel() {

        final Led[] ledReceived = new Led[1];
        try{
            String deviceControlTopic = String.format("%s/%s/%s", CHARGING_TOPIC, getMqttSmartObjectId(), CONTROL_TOPIC);

            logger.info("Charging Station Mqtt Registering to Control Topic ({}) ... ", deviceControlTopic);

            getMqttClient().subscribe(deviceControlTopic, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    if (message != null){
                        logger.info("[CONTROL CHANNEL] -> Control Message Received -> {}", new String(message.getPayload()));
                        // TODO set led color from payload
                        Optional<ControlMessage<?>> generalMessageOptional = parseControlMessagePayload(message);

                        if (generalMessageOptional.isPresent() ) {
                            ledReceived[0] = (Led) generalMessageOptional.get().getDataValue();
                        }
                    }
                    else
                        logger.error("[CONTROL CHANNEL] -> Null control message received !");
                }
            });
            if(ledReceived[0] == Led.GREEN)
                ((LedActuatorResource) super.getResourceMap().get("led")).setIsActive(Led.GREEN);
            else if(ledReceived[0] == Led.RED)
                ((LedActuatorResource) super.getResourceMap().get("led")).setIsActive(Led.RED);
            else if(ledReceived[0] == Led.YELLOW)
                ((LedActuatorResource) super.getResourceMap().get("led")).setIsActive(Led.YELLOW);

        }catch (Exception e){
            logger.error("ERROR Registering to Control Channel ! Msg: {}", e.getLocalizedMessage());
        }
    }


    private void registerToAvailableResources(){
        try{
            super.getResourceMap().entrySet().forEach(resourceEntry -> {

                if(resourceEntry.getKey() != null && resourceEntry.getValue() != null){
                    SensorResource sensorResource = resourceEntry.getValue();

                    logger.info("Registering to Resource {} (id: {}) notifications ...",
                            sensorResource.getType(),
                            sensorResource.getId());

                    //Register to VehiclePresenceResource Notification
                    if(sensorResource.getType().equals(VehiclePresenceSensorResource.RESOURCE_TYPE)){

                        VehiclePresenceSensorResource vehiclePresenceSensorResource = (VehiclePresenceSensorResource) sensorResource;
                        vehiclePresenceSensorResource.addDataListener((ResourceDataListener<Boolean>) super.getResourceMap().get("charge_status"));
                        vehiclePresenceSensorResource.addDataListener((ResourceDataListener<Boolean>) super.getResourceMap().get("led"));
                        vehiclePresenceSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
                            @Override
                            public void onDataChanged(SensorResource<Boolean> resource, Boolean updatedValue) {
                                try {
                                    //Sleep to let all listeners be synchronized
                                    Thread.sleep(SLEEP_TIME);
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", CHARGING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(resource.getId(), resource.getType(), updatedValue));


                                } catch (MqttException | JsonProcessingException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //Register to ChargeStatusSensorResource Notification      -- Double
                    if(sensorResource.getType().equals(ChargeStatusSensorResource.RESOURCE_TYPE)){

                        ChargeStatusSensorResource chargeStatusSensorResource = (ChargeStatusSensorResource) sensorResource;
                        chargeStatusSensorResource.addDataListener((ResourceDataListener<ChargeStatusDescriptor>) super.getResourceMap().get("energy_consumption"));
                        chargeStatusSensorResource.addDataListener((ResourceDataListener<ChargeStatusDescriptor>) super.getResourceMap().get("temperature"));
                        chargeStatusSensorResource.addDataListener(new ResourceDataListener<ChargeStatusDescriptor>() {
                            @Override
                            public void onDataChanged(SensorResource<ChargeStatusDescriptor> resource, ChargeStatusDescriptor updatedValue) {
                                try {
                                     Thread.sleep(SLEEP_TIME);
                                     publishTelemetryData(
                                            String.format("%s/%s/%s/%s", CHARGING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(resource.getId(), resource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //Register to TemperatureSensorResource Notification  -- Double
                    if(sensorResource.getType().equals(TemperatureSensorResource.RESOURCE_TYPE)){

                        TemperatureSensorResource temperatureSensorResource = (TemperatureSensorResource) sensorResource;
                        temperatureSensorResource.addDataListener(new ResourceDataListener<Double>() {
                            @Override
                            public void onDataChanged(SensorResource<Double> resource, Double updatedValue) {
                               try {
                                   Thread.sleep(SLEEP_TIME);
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", CHARGING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(resource.getId(), resource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //Register to EnergyConsumptionResource Notification  -- Double
                    if(sensorResource.getType().equals(EnergyConsumptionSensorResource.RESOURCE_TYPE)){

                        EnergyConsumptionSensorResource energyConsumptionSensorResource = (EnergyConsumptionSensorResource) sensorResource;
                        energyConsumptionSensorResource.addDataListener(new ResourceDataListener<Double>() {
                            @Override
                            public void onDataChanged(SensorResource<Double> resource, Double updatedValue) {
                                try {
                                    Thread.sleep(SLEEP_TIME);
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", CHARGING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(resource.getId(), resource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //Register to LedActuatorResource         -- Led
                    if(sensorResource.getType().equals(LedActuatorResource.RESOURCE_TYPE)){

                        LedActuatorResource ledActuatorResource = (LedActuatorResource) sensorResource;
                        ledActuatorResource.addDataListener((ResourceDataListener<Led>) super.getResourceMap().get("vehicle_presence"));
                        ledActuatorResource.addDataListener(new ResourceDataListener<Led>() {
                            @Override
                            public void onDataChanged(SensorResource<Led> resource, Led updatedValue) {
                                try {
                                    Thread.sleep(SLEEP_TIME);
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", CHARGING_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(resource.getId(), resource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException | InterruptedException e) {
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

    public void publishGeneralData(String topic, GpsLocationDescriptor gpsLocationDescriptor) throws MqttException, JsonProcessingException {

        logger.info("Sending to topic: {} -> Data: {}", topic, gpsLocationDescriptor);

        if(getMqttClient() != null && getMqttClient().isConnected() && gpsLocationDescriptor != null && topic != null){

            String messagePayload = getMapper().writeValueAsString(gpsLocationDescriptor);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(2);
            mqttMessage.setRetained(true);                                              //Send when client connects

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
