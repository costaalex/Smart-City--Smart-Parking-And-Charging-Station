package dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.AverageChargingDurationDescriptor;
import model.AverageParkingDurationDescriptor;
import model.GpsLocationDescriptor;
import model.SmartObjectTypeDescriptor;
import resource.SmartObjectResource;

import java.util.HashMap;
import java.util.Map;

public class SmartObject {

    private String smartObjectId;

    private GpsLocationDescriptor gpsLocation;

    private ObjectMapper mapper;

    private SmartObjectTypeDescriptor smartObjectType; // CHARGING_STATION, PPARKING_LOT

    private Map<String, SmartObjectResource<?>> resourceMap;  //key = sensor_type, value = sensor

    public AverageChargingDurationDescriptor averageChargingDurationDescriptor;
    public AverageParkingDurationDescriptor averageParkingDurationDescriptor;

    public SmartObject() {
        this.mapper = new ObjectMapper();
        averageChargingDurationDescriptor = new AverageChargingDurationDescriptor();
        averageParkingDurationDescriptor = new AverageParkingDurationDescriptor();
    }

    public SmartObject(String smartObjectId, GpsLocationDescriptor gpsLocation, SmartObjectTypeDescriptor smartObjectType) {
        this.smartObjectId = smartObjectId;
        this.gpsLocation = gpsLocation;
        this.mapper = new ObjectMapper();
        this.resourceMap = new HashMap<>();
        this.smartObjectType = smartObjectType;
        averageChargingDurationDescriptor = new AverageChargingDurationDescriptor();
        averageParkingDurationDescriptor = new AverageParkingDurationDescriptor();
    }

    public SmartObject(String smartObjectId, SmartObjectTypeDescriptor smartObjectType) {
        this.smartObjectId = smartObjectId;
        this.gpsLocation = null;
        this.mapper = new ObjectMapper();
        this.resourceMap = new HashMap<>();
        this.smartObjectType = smartObjectType;
        averageChargingDurationDescriptor = new AverageChargingDurationDescriptor();
        averageParkingDurationDescriptor = new AverageParkingDurationDescriptor();
    }

    public AverageChargingDurationDescriptor getAverageChargingDurationDescriptor() {
        return averageChargingDurationDescriptor;
    }

    public AverageParkingDurationDescriptor getAverageParkingDurationDescriptor() {
        return averageParkingDurationDescriptor;
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

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, SmartObjectResource<?>> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<String, SmartObjectResource<?>> resourceMap) {
        this.resourceMap = resourceMap;
    }

}
