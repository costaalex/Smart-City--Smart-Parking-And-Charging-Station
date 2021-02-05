package httpServer;

import dto.SmartObject;
import model.GpsLocationDescriptor;

import java.util.Map;
import java.util.Optional;

public interface ISmartObjectDataManager {
    //General Smart Object Manager
    Optional<Map<String, SmartObject>> getSmartObjectList();                              //smartcity
    Optional<GpsLocationDescriptor> getSmartObjectLocationList();                         //smartcity/gps
    Optional<SmartObject> getSmartObjectById(String id);                                  //smartcity/id_smart_object
    Optional<Map<String, SmartObject>> getSensorBySmartObjectId(String sensorType);       //smartcity/id_smart_object/sensor_type

    //Charging Station Manager
    Optional<Map<String, SmartObject>> getChargingStationList();                          //smartcity/parkinglot

    //Parking Lot Manager
    Optional<Map<String, SmartObject>> getParkingLotList();                               //smartcity/charging_station

}
