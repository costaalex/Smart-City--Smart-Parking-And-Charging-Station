package resource;

public interface ResourceDataListener<T> {

    public void onDataChanged(SensorResource<T> resource, T updatedValue);

}
