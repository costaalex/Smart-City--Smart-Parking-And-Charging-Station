package data_collector_and_manager.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import resource.SensorResource;

import java.util.HashMap;
import java.util.Map;

public class SmartObject {

    private String smartObjectId;

    private GpsLocationDescriptor gpsLocation;

    private SmartObjectTypeDescriptor smartObjectType; // CHARGING_STATION, PARKING_LOT

    private Map<String, SensorResource<?>> resourceMap;  //key = sensor_type, value = sensor

    private AverageDurationDescriptor averageDurationDescriptor;

    public SmartObject() {
    }

    public SmartObject(String smartObjectId, GpsLocationDescriptor gpsLocation, SmartObjectTypeDescriptor smartObjectType) {
        this.smartObjectId = smartObjectId;
        this.gpsLocation = gpsLocation;
        this.resourceMap = new HashMap<>();
        this.smartObjectType = smartObjectType;
        if(smartObjectType == SmartObjectTypeDescriptor.CHARGING_STATION)
            averageDurationDescriptor = new AverageChargingDurationDescriptor();
        else
            averageDurationDescriptor = new AverageParkingDurationDescriptor();
    }

    public SmartObject(String smartObjectId, SmartObjectTypeDescriptor smartObjectType) {
        this.smartObjectId = smartObjectId;
        this.gpsLocation = null;
        this.resourceMap = new HashMap<>();
        this.smartObjectType = smartObjectType;
        if(smartObjectType == SmartObjectTypeDescriptor.CHARGING_STATION)
            averageDurationDescriptor = new AverageChargingDurationDescriptor();
        else
            averageDurationDescriptor = new AverageParkingDurationDescriptor();
    }

    public AverageDurationDescriptor getAverageDurationDescriptor() {
        return averageDurationDescriptor;
    }

   /*
    public void setAverageChargingDurationDescriptor(AverageChargingDurationDescriptor averageChargingDurationDescriptor) {
        this.averageChargingDurationDescriptor = averageChargingDurationDescriptor;
    }

    public void setAverageParkingDurationDescriptor(AverageParkingDurationDescriptor averageParkingDurationDescriptor) {
        this.averageParkingDurationDescriptor = averageParkingDurationDescriptor;
    }
*/

    public SmartObjectTypeDescriptor getSmartObjectType() {
        return smartObjectType;
    }

    public void setSmartObjectType(SmartObjectTypeDescriptor smartObjectType) {
        this.smartObjectType = smartObjectType;
    }

    public String getSmartObjectId() {
        return smartObjectId;
    }

    public void setSmartObjectId(String smartObjectId) {
        this.smartObjectId = smartObjectId;
    }

    public GpsLocationDescriptor getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(GpsLocationDescriptor gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public Map<String, SensorResource<?>> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<String, SensorResource<?>> resourceMap) {
        this.resourceMap = resourceMap;
    }


}
