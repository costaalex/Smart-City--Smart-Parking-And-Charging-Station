package resource;

import model.Led;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VehiclePresenceSensorResource extends SensorResource<Boolean> implements ResourceDataListener<Led>{

    private static final Logger logger = LoggerFactory.getLogger(VehiclePresenceSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:vehicle_presence";

    private static final long UPDATE_PERIOD = 2000; //10 Seconds

    private static final long TASK_DELAY_TIME = 0; //5 mls before starting the periodic update task

    private static final Integer VEHICLE_PRESENCE_PROBABILITY = 4;

    private Boolean updatedVehiclePresenceStatus;

    private Random random = null;

    private boolean isActive = true;

    public VehiclePresenceSensorResource() {
        super(UUID.randomUUID().toString(), VehiclePresenceSensorResource.RESOURCE_TYPE);

        init();
    }

    public VehiclePresenceSensorResource(String id, String type, long timestamp, Boolean updatedVehiclePresenceStatus) {  // server side
        super(id, type, timestamp);
        this.updatedVehiclePresenceStatus = updatedVehiclePresenceStatus;
    }

    /**
     * - Start Periodic Parking Lot Availability update
     */
    private void init(){

        try{
            this.random = new Random(System.currentTimeMillis());
            this.updatedVehiclePresenceStatus = false;

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error init VehiclePresenceResource ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public void onDataChanged(SensorResource<Led> sensorResource, Led updatedValue) {
        if (sensorResource != null && sensorResource.getType().equalsIgnoreCase(LedActuatorResource.RESOURCE_TYPE)) {
            if (updatedValue == Led.YELLOW) {     //If led value changed to Yellow, set parking lot to unusable
                logger.info("Led YELLOW color detected - sensor: {}.. setting parking inactive.", sensorResource.getId());
                isActive = false;
            }
            else if(updatedValue == Led.GREEN){
                logger.info("Led GREEN color detected - sensor: {}.. setting parking active.", sensorResource.getId());
                isActive = true;
            }

        }
    }

    private void startPeriodicEventValueUpdateTask() {
        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            Timer updateTimer = new Timer();

            updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isActive) {  // parking lot is usable
                        if (updatedVehiclePresenceStatus == true) {                    //if vehicle present, it has 3/4 probability to stay present
                            if (random.nextInt(VEHICLE_PRESENCE_PROBABILITY) == 0)
                                updatedVehiclePresenceStatus = false;
                        }
                        else
                            updatedVehiclePresenceStatus = random.nextBoolean();
                    }else
                        updatedVehiclePresenceStatus = false;

                    notifyUpdate(updatedVehiclePresenceStatus);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic update ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.updatedVehiclePresenceStatus;
    }

    public static void main(String[] args) {
        VehiclePresenceSensorResource vehiclePresenceSensorResource = new VehiclePresenceSensorResource();

        logger.info("New {} Resource Created with Id: {} ! Updated Value: {}",
                vehiclePresenceSensorResource.getType(),
                vehiclePresenceSensorResource.getId(),
                vehiclePresenceSensorResource.loadUpdatedValue());

        vehiclePresenceSensorResource.addDataListener(new ResourceDataListener<Boolean>() {
            @Override
            public void onDataChanged(SensorResource<Boolean> resource, Boolean updatedValue) {
                if (resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });
    }

    public Boolean getUpdatedVehiclePresenceStatus() {
        return updatedVehiclePresenceStatus;
    }

    public void setUpdatedVehiclePresenceStatus(Boolean updatedVehiclePresenceStatus) {
        this.updatedVehiclePresenceStatus = updatedVehiclePresenceStatus;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
