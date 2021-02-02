package resource;

import model.GpsLocationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VehiclePresenceSensorResource extends SmartObjectResource<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(VehiclePresenceSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:vehiclePresence";

    private static final long UPDATE_PERIOD = 10000; //10 Seconds

    private static final long TASK_DELAY_TIME = 5000; //5 Seconds before starting the periodic update task

    private Boolean updatedVehiclePresenceStatus;

    private Random random = null;

    private boolean isActive = true;

    public VehiclePresenceSensorResource() {
        super(UUID.randomUUID().toString(), VehiclePresenceSensorResource.RESOURCE_TYPE);

        init();
    }

    public VehiclePresenceSensorResource(String id, String type, Boolean vehiclePresenceSensorStatus) {
        super(id, type);
        this.updatedVehiclePresenceStatus = vehiclePresenceSensorStatus;
        init();
    }

    public VehiclePresenceSensorResource(Boolean vehiclePresenceSensorStatus) {
        super(UUID.randomUUID().toString(), VehiclePresenceSensorResource.RESOURCE_TYPE);
        this.updatedVehiclePresenceStatus = vehiclePresenceSensorStatus;
        init();
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

    private void startPeriodicEventValueUpdateTask() {
        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            Timer updateTimer = new Timer();

            updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isActive())
                        updatedVehiclePresenceStatus = random.nextBoolean();
                    else
                        updatedVehiclePresenceStatus = false;
                    //logger.info("Updated Parking Availability: {}", updatedParkingSensorStatus.getIsVehiclePresent());

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
            public void onDataChanged(SmartObjectResource<Boolean> resource, Boolean updatedValue) {
                if (resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
