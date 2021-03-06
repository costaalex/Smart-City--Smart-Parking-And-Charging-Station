package resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class SensorResource<T> {

    private static final Logger logger = LoggerFactory.getLogger(SensorResource.class);

    private String type;
    private String id;
    private long timestamp;
    protected List<ResourceDataListener<T>> resourceListenerList;

    public SensorResource() {
        this.resourceListenerList = new ArrayList<>();
    }

    public SensorResource(String type, long timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    public SensorResource(String id, String type) {
        this.id = id;
        this.type = type;
        this.resourceListenerList = new ArrayList<>();
    }

    public SensorResource(String id, String type, long timestamp) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
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

    //notify updated value to all listeners
    protected void notifyUpdate(T updatedValue){

        if(this.resourceListenerList != null && this.resourceListenerList.size() > 0)
            this.resourceListenerList.forEach(resourceDataListener -> {
                if(resourceDataListener != null)
                    resourceDataListener.onDataChanged(this, updatedValue);
            });
        else if(this.resourceListenerList == null)
            logger.error("Null Resource Data Listener ! Nothing to notify ...");
        else
            logger.info("Empty Resource Data Listener ! Nothing to notify ...");
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SensorResource{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
