package resource;

import model.GpsLocationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class SmartObjectResource<T> {

    private static final Logger logger = LoggerFactory.getLogger(SmartObjectResource.class);

    protected List<ResourceDataListener<T>> resourceListenerList;

    private String type;
    private String id;
    private GpsLocationDescriptor smartObjectLocation;


    public SmartObjectResource() {
        this.resourceListenerList = new ArrayList<>();
    }

    public SmartObjectResource(String id, String type, Double latitude, Double longitude) {
        this.id = id;
        this.type = type;
        this.smartObjectLocation = new GpsLocationDescriptor(latitude, longitude, 0.0, "");
        this.resourceListenerList = new ArrayList<>();

    }

    public abstract T loadUpdatedValue();

    public void addDataListener(ResourceDataListener<T> resourceDataListener){
        if(this.resourceListenerList != null)
            this.resourceListenerList.add(resourceDataListener);
    }

    public void removeDataListener(ResourceDataListener<T> resourceDataListener){
        if(this.resourceListenerList != null && this.resourceListenerList.contains(resourceDataListener))
            this.resourceListenerList.remove(resourceDataListener);
    }

    protected void notifyUpdate(T updatedValue){
        if(this.resourceListenerList != null && this.resourceListenerList.size() > 0)
            this.resourceListenerList.forEach(resourceDataListener -> {
                if(resourceDataListener != null)
                    resourceDataListener.onDataChanged(this, updatedValue);
            });
        else
            logger.error("Empty or Null Resource Data Listener ! Nothing to notify ...");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GpsLocationDescriptor getSmartObjectLocation() {
        return smartObjectLocation;
    }

    public void setSmartObjectLocation(GpsLocationDescriptor smartObjectLocation) {
        this.smartObjectLocation = smartObjectLocation;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SmartObjectResource{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
