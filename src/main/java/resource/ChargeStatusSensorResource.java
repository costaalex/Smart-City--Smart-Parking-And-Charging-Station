package resource;

import model.ChargeStatusDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

//This sensor depends on the presence of the vehicle

public class ChargeStatusSensorResource extends SmartObjectResource<ChargeStatusDescriptor> implements ResourceDataListener<Boolean>{

    private static final Logger logger = LoggerFactory.getLogger(ChargeStatusSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:charge_status";

    private static final long UPDATE_PERIOD = 10000; //10 Seconds

    private static final long TASK_DELAY_TIME = 5000; //5 Seconds before starting the periodic update task

    private ChargeStatusDescriptor updatedChargeStatus;

    private Random random = null;

    public ChargeStatusSensorResource(){
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    public ChargeStatusSensorResource(String id, String type, ChargeStatusDescriptor updatedChargeStatus) {
        super(id, type);
        this.updatedChargeStatus = updatedChargeStatus;
    }

    public ChargeStatusSensorResource(ChargeStatusDescriptor updatedChargeStatus) {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
        this.updatedChargeStatus = updatedChargeStatus;
    }

    /**
     * - Initialize ChargeStatusSensor
     */
    private void init(){

        try{
            this.random = new Random(System.currentTimeMillis());
            this.updatedChargeStatus = ChargeStatusDescriptor.UNPLUGGED;

            startPeriodicEventValueUpdateTask();

        }catch (Exception e){
            logger.error("Error init ChargeStatusSensorResource ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public ChargeStatusDescriptor loadUpdatedValue() {
        return this.updatedChargeStatus;
    }

    private void startPeriodicEventValueUpdateTask() {
        try{

            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            Timer updateTimer = new Timer();

            updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //logger.info("Updated Charge Status: {}", updatedChargeStatus);

                    notifyUpdate(updatedChargeStatus);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic update ! Msg: {}", e.getLocalizedMessage());
        }
    }

    /**
     * - VehiclePresenceSensor changed status, choose a new value for ChargeStatusSensor
     */
    @Override
    public void onDataChanged(SmartObjectResource<Boolean> smartObjectResource, Boolean updatedValue) {
        if (smartObjectResource != null && smartObjectResource.getType().equals(VehiclePresenceSensorResource.RESOURCE_TYPE)) {
            if (updatedValue == true) {     //If a new vehicle arrived, choose a random value
                logger.info("Vehicle detected by sensor: {}", smartObjectResource.getId());

                switch (random.nextInt(3)) {
                    case 0:
                        updatedChargeStatus = ChargeStatusDescriptor.UNPLUGGED;
                        break;
                    case 1:
                        updatedChargeStatus = ChargeStatusDescriptor.PLUGGED;
                        break;
                    case 2:
                        updatedChargeStatus = ChargeStatusDescriptor.CHARGING;
                        break;
                }
            }
            else{       //If the parking lot is empty, put in UNPLUGGED status
                updatedChargeStatus = ChargeStatusDescriptor.UNPLUGGED;
            }

        }
    }
}




