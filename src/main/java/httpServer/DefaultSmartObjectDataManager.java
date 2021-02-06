package httpServer;

import dto.SingletonDataCollector;
import dto.SmartObject;
import model.GpsLocationDescriptor;
import model.Led;
import model.SmartObjectTypeDescriptor;
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
        LedActuatorResource ledSensor = (LedActuatorResource)single_instance.smartObjectsMap.get(idSmartObject).getResourceMap().get("led");
        ledSensor.setIsActive(led);
        return true;
    }

}
