package resource;

import model.GpsLocationDescriptor;
import model.ParkingLotDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ParkingLotResource extends SmartObjectResource<ParkingLotDescriptor> {

    private static final Logger logger = LoggerFactory.getLogger(ParkingLotResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:parkingLot";

    private static final long UPDATE_PERIOD = 10000; //10 Seconds

    private static final long TASK_DELAY_TIME = 1000; //1 Seconds before starting the periodic update task

    ParkingLotDescriptor updatedParkingSensorStatus;

    private Random random = null;

    public ParkingLotResource() {
        super(UUID.randomUUID().toString(), ParkingLotResource.RESOURCE_TYPE, new GpsLocationDescriptor());

        init();
    }

    public ParkingLotResource(String id, String type, GpsLocationDescriptor gpsLocationDescriptor, ParkingLotDescriptor parkingLotSmartObject) {
        super(id, type, gpsLocationDescriptor);
        this.updatedParkingSensorStatus = parkingLotSmartObject;
        init();
    }

    public ParkingLotResource(String id, GpsLocationDescriptor gpsLocationDescriptor, ParkingLotDescriptor parkingLotSmartObject) {
        super(id, ParkingLotResource.RESOURCE_TYPE, gpsLocationDescriptor);
        this.updatedParkingSensorStatus = parkingLotSmartObject;
        init();
    }

    /**
     * - Start Periodic Parking Lot Availability update
     */
    private void init(){

        try{
            this.random = new Random(System.currentTimeMillis());
            this.updatedParkingSensorStatus = new ParkingLotDescriptor();

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error init ParkingLotSmartObject ! Msg: {}", e.getLocalizedMessage());
        }

    }

    private void startPeriodicEventValueUpdateTask() {
        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            Timer updateTimer = new Timer();

            updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updatedParkingSensorStatus.setIsVehiclePresent(random.nextBoolean());
                    //logger.info("Updated Parking Lot: {}", updatedParkingSensorStatus.getIsVehiclePresent());

                    notifyUpdate(updatedParkingSensorStatus);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic update ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public ParkingLotDescriptor loadUpdatedValue() {
        return this.updatedParkingSensorStatus;
    }

    public static void main(String[] args) {
        ParkingLotResource parkingLotResource = new ParkingLotResource();

        logger.info("New {} Resource Created with Id: {} ! Updated Value: {}",
                parkingLotResource.getType(),
                parkingLotResource.getId(),
                parkingLotResource.loadUpdatedValue());

        parkingLotResource.addDataListener((resource, updatedValue) -> {
            if(resource != null && updatedValue != null)
                logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
            else
                logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
        });
    }
}
