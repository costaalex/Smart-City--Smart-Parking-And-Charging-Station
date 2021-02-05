package httpServer;

import dto.SingletonDataCollector;
import dto.SmartObject;
import model.GpsLocationDescriptor;
import model.SmartObjectTypeDescriptor;

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
        return Optional.empty();
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
        return Optional.of(single_instance.smartObjectsMap);
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

}
