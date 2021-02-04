package HttpServer;

import dto.SingletonDataCollector;
import dto.SmartObject;
import model.GpsLocationDescriptor;
import resource.SmartObjectResource;

import java.util.Map;
import java.util.Optional;

public interface ISmartObjectDataManager {
    //General Smart Object Manager
    Optional<SingletonDataCollector> getSmartObjectList();                                //smartcity
    Optional<GpsLocationDescriptor> getSmartObjectLocationList();                         //smartcity/gps
    Optional<Map<String, SmartObjectResource<?>>> getSmartObjectById(String id);                     //smartcity/id_smart_object
    Optional<Map<String, SmartObject>> getSensorBySmartObjectId(String sensorType);       //smartcity/id_smart_object/sensor_type

    //Charging Station Manager
    Optional<Map<String, SmartObject>> getChargingStationList();                          //smartcity/parkinglot


    //Parking Lot Manager
    Optional<Map<String, SmartObject>> getParkingLotList();                               //smartcity/charging_station

}
