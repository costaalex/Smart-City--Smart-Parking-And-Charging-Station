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

public class ChargingStationMqttSmartObject extends MqttSmartObject{

    private static final Logger logger = LoggerFactory.getLogger(ChargingStationMqttSmartObject.class);

    private static final String BASIC_TOPIC = "smartcity/charging_station";

    public ChargingStationMqttSmartObject() {
        super.setMapper(new ObjectMapper());
    }

    /**
     * Init the charging station smart object with its ID, the MQTT Client and the Map of managed resources
     * @param chargingStationId
     * @param mqttClient
     * @param resourceMap
     */
    public void init(String chargingStationId, GpsLocationDescriptor gpsLocation, IMqttClient mqttClient, Map<String, SmartObjectResource<?>> resourceMap){

        super.setMqttSmartObjectId(chargingStationId);
        super.setGpsLocation(gpsLocation);
        super.setMqttClient(mqttClient);
        super.setResourceMap(resourceMap);

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

    private void registerToControlChannel() {

        try{

            String deviceControlTopic = String.format("%s/%s/%s", BASIC_TOPIC, super.getMqttSmartObjectId(), CONTROL_TOPIC);

            logger.info("Registering to Control Topic ({}) ... ", deviceControlTopic);

            super.getMqttClient().subscribe(deviceControlTopic, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    if(message != null)
                        logger.info("[CONTROL CHANNEL] -> Control Message Received -> {}", new String(message.getPayload()));
                    else
                        logger.error("[CONTROL CHANNEL] -> Null control message received !");
                }
            });

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
                        //vehiclePresenceSensorResource.addDataListener(super.getResourceMap().get("charge_status").);
                        vehiclePresenceSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
                            @Override
                            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                                //logger.info(String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()), smartObjectResource.getType()+": "+updatedValue);
                                try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(smartObjectResource.getType(), updatedValue));


                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //Register to ChargeStatusSensorResource Notification      -- Double
                    if(smartObjectResource.getType().equals(ChargeStatusSensorResource.RESOURCE_TYPE)){

                        ChargeStatusSensorResource chargeStatusSensorResource = (ChargeStatusSensorResource)smartObjectResource;
                        chargeStatusSensorResource.addDataListener(new ResourceDataListener<ChargeStatusDescriptor>() {
                            @Override
                            public void onDataChanged(SmartObjectResource<ChargeStatusDescriptor> resource, ChargeStatusDescriptor updatedValue) {
                                // logger.info(String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()), smartObjectResource.getType()+": "+updatedValue);
                                 try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(smartObjectResource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //Register to TemperatureSensorResource Notification  -- Double
                    if(smartObjectResource.getType().equals(TemperatureSensorResource.RESOURCE_TYPE)){

                        TemperatureSensorResource temperatureSensorResource = (TemperatureSensorResource)smartObjectResource;
                        temperatureSensorResource.addDataListener(new ResourceDataListener<Double>() {
                            @Override
                            public void onDataChanged(SmartObjectResource<Double> resource, Double updatedValue) {
                                //logger.info(String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()), smartObjectResource.getType()+": "+updatedValue);

                               try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
                                            new TelemetryMessage<>(smartObjectResource.getType(), updatedValue));
                                } catch (MqttException | JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    //Register to EnergyConsumptionResource Notification  -- Double
                    if(smartObjectResource.getType().equals(EnergyConsumptionSensorResource.RESOURCE_TYPE)){

                        EnergyConsumptionSensorResource energyConsumptionSensorResource = (EnergyConsumptionSensorResource)smartObjectResource;
                        energyConsumptionSensorResource.addDataListener(new ResourceDataListener<Double>() {
                            @Override
                            public void onDataChanged(SmartObjectResource<Double> resource, Double updatedValue) {
                                //logger.info(String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()), smartObjectResource.getType()+": "+updatedValue);
                                try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
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
                                //logger.info(String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()), smartObjectResource.getType()+": "+updatedValue);
                                try {
                                    publishTelemetryData(
                                            String.format("%s/%s/%s/%s", BASIC_TOPIC, getMqttSmartObjectId(), TELEMETRY_TOPIC, resourceEntry.getKey()),
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

    /**
     * Stop the emulated vehicle
     */
    public void stop(){
        //TODO Implement a proper closing method
    }

    protected void publishTelemetryData(String topic, TelemetryMessage<?> telemetryMessage) throws MqttException, JsonProcessingException {

        logger.info("Sending to topic: {} -> Data: {}", topic, telemetryMessage);

        if(super.getMqttClient() != null && super.getMqttClient().isConnected() && telemetryMessage != null && topic != null){

            String messagePayload = super.getMapper().writeValueAsString(telemetryMessage);

            MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
            mqttMessage.setQos(2);

            super.getMqttClient().publish(topic, mqttMessage);

            logger.info("Data Correctly Published to topic: {}", topic);

        }
        else
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
    }
}
