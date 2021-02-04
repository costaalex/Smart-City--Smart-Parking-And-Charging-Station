package HttpServer;

import dto.SingletonDataCollector;
import dto.SmartObject;
import model.GpsLocationDescriptor;
import resource.SmartObjectResource;

import java.util.Map;
import java.util.Optional;

public class DefaultSmartObjectDataManager implements ISmartObjectDataManager{
    SingletonDataCollector single_instance = SingletonDataCollector.getInstance();

    @Override
    public Optional<SingletonDataCollector> getSmartObjectList() {
        if (single_instance == null)
            return Optional.empty();
        return Optional.of(single_instance);
    }

    @Override
    public Optional<GpsLocationDescriptor> getSmartObjectLocationList() {
        if (single_instance == null)
            return Optional.empty();
        //Iterare sulla mappa e prendere gps
        return null;
    }

    @Override
    public Optional<Map<String, SmartObjectResource<?>>> getSmartObjectById(String id) {
        if (single_instance == null)
            return Optional.empty();

        SmartObject smartObject = single_instance.smartObjectsMap.get(id);
        if (smartObject != null)
            return Optional.of(smartObject.getResourceMap());
       // else if ()
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, SmartObject>> getSensorBySmartObjectId(String sensorType) {
        if (single_instance == null)
            return Optional.empty();
    }

    @Override
    public Optional<Map<String, SmartObject>> getChargingStationList() {
        if (single_instance == null)
            return Optional.empty();
    }

    @Override
    public Optional<Map<String, SmartObject>> getParkingLotList() {
        if (single_instance == null)
            return Optional.empty();
    }


}
