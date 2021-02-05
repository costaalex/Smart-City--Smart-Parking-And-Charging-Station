package httpServer;

import dto.SingletonDataCollector;
import dto.SmartObject;
import model.GpsLocationDescriptor;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class DefaultSmartObjectDataManager implements ISmartObjectDataManager{
    SingletonDataCollector single_instance = SingletonDataCollector.getInstance();

    @Override
    public Optional<Map<String, SmartObject>> getSmartObjectList() {
        if (single_instance == null)
            return Optional.empty();
        return Optional.of(single_instance.smartObjectsMap);
    }

    @Override
    public Optional<GpsLocationDescriptor> getSmartObjectLocationList() {
        if (single_instance == null)
            return Optional.empty();
        //Iterare sulla mappa e prendere gps
        return Optional.empty();
    }

    @Override
    public Optional<SmartObject> getSmartObjectById(String id) {
        if (single_instance == null)
            return Optional.empty();

        return Optional.ofNullable(single_instance.smartObjectsMap.get(id));
    }

    @Override
    public Optional<Map<String, SmartObject>> getSensorBySmartObjectId(String sensorType) {
        if (single_instance == null)
            return Optional.empty();

        single_instance.smartObjectsMap.get("ss").getResourceMap().get("mcd");

        return Optional.empty();
    }

    @Override
    public Optional<Map<String, SmartObject>> getChargingStationList() {
        if (single_instance == null)
            return Optional.empty();

        Map<String, SmartObject> smartObjectsMap = single_instance.smartObjectsMap;



        return Optional.empty();
    }

    @Override
    public Optional<Map<String, SmartObject>> getParkingLotList() {
        if (single_instance == null)
            return Optional.empty();

        return Optional.empty();
    }


}
