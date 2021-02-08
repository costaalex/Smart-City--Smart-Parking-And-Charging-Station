package data_collector_and_manager.httpServer;

import data_collector_and_manager.dto.SmartObject;
import model.GpsLocationDescriptor;
import model.Led;
import model.SmartObjectTypeDescriptor;
import resource.SensorResource;

import java.util.Map;
import java.util.Optional;

public interface ISmartObjectDataManager {
    //General Smart Object Manager
    Optional<Map<String, SmartObject>> getSmartObjectsMap();                                            //smartcity
    Optional<Map<String, SmartObject>> getSmartObjectsMap(SmartObjectTypeDescriptor smartObjectType);   //smartcity?type=type_smart_object

    Optional<Map<String, GpsLocationDescriptor>> getsmartobjectlocationMap();                           //smartcity/gps
    Optional<SmartObject> getSmartObjectById(String id);                                                //smartcity/id_smart_object
    Optional<SensorResource<?>> getSensorBySmartObjectId(String id, String sensorType);                       //smartcity/id_smart_object/sensor_type

    boolean setLed(String idSmartObject, Led led);
}
