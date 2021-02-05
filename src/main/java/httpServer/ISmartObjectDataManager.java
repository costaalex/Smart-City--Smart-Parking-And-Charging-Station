package httpServer;

import dto.SmartObject;
import model.GpsLocationDescriptor;
import model.SmartObjectTypeDescriptor;

import java.util.Map;
import java.util.Optional;

public interface ISmartObjectDataManager {
    //General Smart Object Manager
    Optional<Map<String, SmartObject>> getSmartObjectsList();                              //smartcity

    Optional<GpsLocationDescriptor> getSmartObjectLocationList();                         //smartcity/gps
    Optional<SmartObject> getSmartObjectById(String id);                                  //smartcity/id_smart_object
    Optional<Map<String, SmartObject>> getSensorBySmartObjectId(String sensorType);       //smartcity/id_smart_object/sensor_type

    Optional<Map<String, SmartObject>> getSmartObjectsList(SmartObjectTypeDescriptor smartObjectType); //smartcity/parkinglot or //smartcity/charging_station
}
