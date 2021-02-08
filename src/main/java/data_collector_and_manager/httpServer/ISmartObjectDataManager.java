package data_collector_and_manager.httpServer;

import data_collector_and_manager.dto.SmartObject;
import model.GpsLocationDescriptor;
import model.Led;
import model.SmartObjectTypeDescriptor;

import java.util.Map;
import java.util.Optional;

public interface ISmartObjectDataManager {
    //General Smart Object Manager
    Optional<Map<String, SmartObject>> getSmartObjectsMap();                              //smartcity?type=type_smart_object

    Optional<Map<String, GpsLocationDescriptor>> getsmartobjectlocationMap();             //smartcity/gps
    Optional<SmartObject> getSmartObjectById(String id);                                  //smartcity/id_smart_object  or smartcity/id_smart_object/sensor_type

    Optional<Map<String, SmartObject>> getSmartObjectsMap(SmartObjectTypeDescriptor smartObjectType); //smartcity/parkinglot or //smartcity/charging_station

    boolean setLed(String idSmartObject, Led led);
}
