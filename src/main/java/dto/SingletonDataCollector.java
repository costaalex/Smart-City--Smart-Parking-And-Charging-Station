package dto;

import model.AverageChargingDurationDescriptor;
import model.AverageParkingDurationDescriptor;

import java.util.HashMap;
import java.util.Map;

public class SingletonDataCollector {
    // static variable single_instance of type Singleton
    private static SingletonDataCollector single_instance = null;

    public Map<String, SmartObject> smartObjectsMap;             //key: idSmartObject, value: SmartObject

    public AverageChargingDurationDescriptor averageChargingDurationDescriptor;
    public AverageParkingDurationDescriptor averageParkingDurationDescriptor;

    // private constructor restricted to this class itself
    private SingletonDataCollector()
    {
        smartObjectsMap = new HashMap<>();
        averageChargingDurationDescriptor = new AverageChargingDurationDescriptor();
        averageParkingDurationDescriptor = new AverageParkingDurationDescriptor();
    }

    // static method to create instance of Singleton class
    public static SingletonDataCollector getInstance()
    {
        if (single_instance == null)
            single_instance = new SingletonDataCollector();

        return single_instance;
    }
}