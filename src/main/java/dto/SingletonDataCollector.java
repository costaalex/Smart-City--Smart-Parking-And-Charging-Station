package dto;

import device.ChargingStationMqttSmartObject;
import device.ParkingLotMqttSmartObject;

import java.util.HashMap;
import java.util.Map;

public class SingletonDataCollector {
    // static variable single_instance of type Singleton
    private static SingletonDataCollector single_instance = null;

    public Map<String, SmartObject> smartObjectsMap;             //key: idSmartObject, value: SmartObject


    // private constructor restricted to this class itself
    private SingletonDataCollector()
    {
        smartObjectsMap = new HashMap<>();
    }

    // static method to create instance of Singleton class
    public static SingletonDataCollector getInstance()
    {
        if (single_instance == null)
            single_instance = new SingletonDataCollector();

        return single_instance;
    }
}