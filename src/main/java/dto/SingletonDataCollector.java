package dto;

import device.ChargingStationMqttSmartObject;
import device.ParkingLotMqttSmartObject;

import java.util.HashMap;
import java.util.Map;

public class SingletonDataCollector {
    // static variable single_instance of type Singleton
    static SingletonDataCollector single_instance = null;

    public Map<String, SmartObject> chargingStationMap;             //key: idSmartObject, value: SmartObject
    public Map<String, SmartObject> parkingLotMap;

    // private constructor restricted to this class itself
    private SingletonDataCollector()
    {
        chargingStationMap = new HashMap<>();
        parkingLotMap = new HashMap<>();
    }

    // static method to create instance of Singleton class
    public static SingletonDataCollector getInstance()
    {
        if (single_instance == null)
            single_instance = new SingletonDataCollector();

        return single_instance;
    }
}