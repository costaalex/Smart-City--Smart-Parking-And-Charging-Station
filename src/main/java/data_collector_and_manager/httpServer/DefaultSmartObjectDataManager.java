package data_collector_and_manager.httpServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import data_collector_and_manager.consumer.DataCollectorAndManager;
import data_collector_and_manager.dto.SingletonDataCollector;
import data_collector_and_manager.dto.SmartObject;
import message.ControlMessage;
import model.GpsLocationDescriptor;
import model.Led;
import model.SmartObjectTypeDescriptor;
import org.eclipse.paho.client.mqttv3.MqttException;
import resource.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultSmartObjectDataManager implements ISmartObjectDataManager{
    SingletonDataCollector single_instance = SingletonDataCollector.getInstance();

    @Override
    public Optional<Map<String, GpsLocationDescriptor>> getsmartobjectlocationMap() {

        if (single_instance == null)
            return Optional.empty();
        //Iterate on map and take couples id, gps
        Map<String, SmartObject> smartObjectsMap = single_instance.smartObjectsMap;

        Map<String, GpsLocationDescriptor> smartObjectsLocationMap = new HashMap<>();
        for (Map.Entry<String, SmartObject> entry : smartObjectsMap.entrySet()) {

            String smartObjectId = entry.getKey();
            GpsLocationDescriptor gpsLocationDescriptor = entry.getValue().getGpsLocation();
            smartObjectsLocationMap.put(smartObjectId, gpsLocationDescriptor);

        }

        return  Optional.ofNullable(smartObjectsLocationMap);
    }

    @Override
    public Optional<SmartObject> getSmartObjectById(String id) {
        if (single_instance == null)
            return Optional.empty();

        return Optional.ofNullable(single_instance.smartObjectsMap.get(id));
    }

    @Override
    public Optional<SensorResource<?>> getSensorBySmartObjectId(String id, String sensorType) {
        Optional<SmartObject> smartObject = getSmartObjectById(id);

        if (!smartObject.isPresent())
            return Optional.empty();

        SensorResource<?> sensor = null;

        if(VehiclePresenceSensorResource.RESOURCE_TYPE.contains(sensorType))
            sensor = smartObject.get().getResourceMap().get(VehiclePresenceSensorResource.RESOURCE_TYPE);                  //Extract the requested sensor
        else if(TemperatureSensorResource.RESOURCE_TYPE.contains(sensorType))
            sensor = smartObject.get().getResourceMap().get(TemperatureSensorResource.RESOURCE_TYPE);
        else if(EnergyConsumptionSensorResource.RESOURCE_TYPE.contains(sensorType))
            sensor = smartObject.get().getResourceMap().get(EnergyConsumptionSensorResource.RESOURCE_TYPE);
        else if(ChargeStatusSensorResource.RESOURCE_TYPE.contains(sensorType))
            sensor = smartObject.get().getResourceMap().get(ChargeStatusSensorResource.RESOURCE_TYPE);
        else if(LedActuatorResource.RESOURCE_TYPE.contains(sensorType))
            sensor = smartObject.get().getResourceMap().get(LedActuatorResource.RESOURCE_TYPE);

        return Optional.ofNullable(sensor);
    }

    @Override
    public Optional<Map<String, SmartObject>> getSmartObjectsMap() {
        if (single_instance == null)
            return Optional.empty();
        return Optional.ofNullable(single_instance.smartObjectsMap);
    }

    @Override
    public Optional<Map<String, SmartObject>> getSmartObjectsMap(SmartObjectTypeDescriptor smartObjectType) {
        if (single_instance == null)
            return Optional.empty();

        Map<String, SmartObject> smartObjectsMap = single_instance.smartObjectsMap;
        Map<String, SmartObject> requiredSmartObjectsMap = new HashMap<>();
        for (Map.Entry<String, SmartObject> entry : smartObjectsMap.entrySet()) {
            String smartObjectId = entry.getKey();
            if(entry.getValue().getSmartObjectType() == smartObjectType)
                requiredSmartObjectsMap.put(smartObjectId, entry.getValue());
        }
        return  Optional.ofNullable(requiredSmartObjectsMap);
    }

    @Override
    public boolean setLed(String idSmartObject, Led led) {
        if (single_instance == null)
            return false;
       ((LedActuatorResource) single_instance.smartObjectsMap.get(idSmartObject).getResourceMap().get(LedActuatorResource.RESOURCE_TYPE)).setIsActive(led);

        //Publish New Led Actuator Status
        try {
            DataCollectorAndManager.publishControlData(idSmartObject, new ControlMessage<>(((LedActuatorResource) single_instance.smartObjectsMap.get(idSmartObject).getResourceMap().get(LedActuatorResource.RESOURCE_TYPE)).getType(), led));
        } catch (MqttException | JsonProcessingException e) {
            e.printStackTrace();
        }

        return true;
    }

}
