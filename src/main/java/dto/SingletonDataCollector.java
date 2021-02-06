package dto;

import model.AverageChargingDurationDescriptor;
import model.AverageParkingDurationDescriptor;
import model.SmartObjectTypeDescriptor;

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


    public SmartObjectTypeDescriptor getSmartObjectTypeFromId(String smartObjectId){
        for (Map.Entry<String, SmartObject> entry : smartObjectsMap.entrySet()) {
            String id = entry.getKey();
            if(id.equals(smartObjectId))
                return entry.getValue().getSmartObjectType();
        }
        return  null;
    }

    @Override
    public String toString() {
        return "SingletonDataCollector{" +
                "smartObjectsMap=" + smartObjectsMap.toString() +
                ", averageChargingDurationDescriptor=" + averageChargingDurationDescriptor +
                ", averageParkingDurationDescriptor=" + averageParkingDurationDescriptor +
                '}';
    }
}