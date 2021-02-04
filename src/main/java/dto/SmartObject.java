package dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.GpsLocationDescriptor;
import resource.SmartObjectResource;

import java.util.HashMap;
import java.util.Map;

public class SmartObject {

    private String smartObjectId;

    private GpsLocationDescriptor gpsLocation;

    private ObjectMapper mapper;

    private Map<String, SmartObjectResource<?>> resourceMap;  //key = sensor_type, value = sensor

    public SmartObject() {
        this.mapper = new ObjectMapper();
    }

    public SmartObject(String smartObjectId, GpsLocationDescriptor gpsLocation) {
        this.smartObjectId = smartObjectId;
        this.gpsLocation = gpsLocation;
        this.mapper = new ObjectMapper();
        this.resourceMap = new HashMap<>();
    }

    public SmartObject(String smartObjectId) {
        this.smartObjectId = smartObjectId;
        this.gpsLocation = null;
        this.mapper = new ObjectMapper();
        this.resourceMap = new HashMap<>();
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
