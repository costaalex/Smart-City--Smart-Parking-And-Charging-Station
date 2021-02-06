package httpServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import consumer.DataCollectorAndManager;
import dto.SingletonDataCollector;
import dto.SmartObject;
import message.ControlMessage;
import message.TelemetryMessage;
import model.GpsLocationDescriptor;
import model.Led;
import model.SmartObjectTypeDescriptor;
import org.eclipse.paho.client.mqttv3.MqttException;
import resource.LedActuatorResource;
import resource.SmartObjectResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class DefaultSmartObjectDataManager implements ISmartObjectDataManager{
    SingletonDataCollector single_instance = SingletonDataCollector.getInstance();

    @Override
    public Optional<Map<String, GpsLocationDescriptor>> getSmartObjectLocationList() {
        if (single_instance == null)
            return Optional.empty();
        //Iterare sulla mappa e prendere coppie id, gps
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
    public Optional<Map<String, SmartObject>> getSmartObjectsList() {
        if (single_instance == null)
            return Optional.empty();
        return Optional.ofNullable(single_instance.smartObjectsMap);
    }

    @Override
    public Optional<Map<String, SmartObject>> getSmartObjectsList(SmartObjectTypeDescriptor smartObjectType) {
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
        LedActuatorResource ledActuator = (LedActuatorResource)single_instance.smartObjectsMap.get(idSmartObject).getResourceMap().get("led");
        ledActuator.setIsActive(led);
        //Publish New Led Actuator Status
        try {
            DataCollectorAndManager.publishControlData(idSmartObject, new ControlMessage<>(ledActuator.getType(), led));
        } catch (MqttException | JsonProcessingException e) {
            e.printStackTrace();
        }

        return true;
    }

}
