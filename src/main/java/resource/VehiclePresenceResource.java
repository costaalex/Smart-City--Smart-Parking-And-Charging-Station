package resource;

import model.GpsLocationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VehiclePresenceResource extends SmartObjectResource<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(VehiclePresenceResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:parkingLot";

    private static final long UPDATE_PERIOD = 10000; //10 Seconds

    private static final long TASK_DELAY_TIME = 5000; //5 Seconds before starting the periodic update task

    private Boolean updatedVehiclePresenceSensorStatus;

    private Random random = null;

    public VehiclePresenceResource() {
        super(UUID.randomUUID().toString(), VehiclePresenceResource.RESOURCE_TYPE, new GpsLocationDescriptor());

        init();
    }

    public VehiclePresenceResource(String id, String type, GpsLocationDescriptor gpsLocationDescriptor, Boolean vehiclePresenceSensorStatus) {
        super(id, type, gpsLocationDescriptor);
        this.updatedVehiclePresenceSensorStatus = vehiclePresenceSensorStatus;
        init();
    }

    public VehiclePresenceResource(GpsLocationDescriptor gpsLocationDescriptor, Boolean vehiclePresenceSensorStatus) {
        super(UUID.randomUUID().toString(), VehiclePresenceResource.RESOURCE_TYPE, gpsLocationDescriptor);
        this.updatedVehiclePresenceSensorStatus = vehiclePresenceSensorStatus;
        init();
    }

    /**
     * - Start Periodic Parking Lot Availability update
     */
    private void init(){

        try{
            this.random = new Random(System.currentTimeMillis());
            this.updatedVehiclePresenceSensorStatus = false;

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
                    updatedVehiclePresenceSensorStatus = random.nextBoolean();
                    //logger.info("Updated Parking Lot: {}", updatedParkingSensorStatus.getIsVehiclePresent());

                    notifyUpdate(updatedVehiclePresenceSensorStatus);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic update ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.updatedVehiclePresenceSensorStatus;
    }

    public static void main(String[] args) {
        VehiclePresenceResource vehiclePresenceResource = new VehiclePresenceResource();

        logger.info("New {} Resource Created with Id: {} ! Updated Value: {}",
                vehiclePresenceResource.getType(),
                vehiclePresenceResource.getId(),
                vehiclePresenceResource.loadUpdatedValue());

        vehiclePresenceResource.addDataListener((resource, updatedValue) -> {
            if(resource != null && updatedValue != null)
                logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
            else
                logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
        });
    }
}
