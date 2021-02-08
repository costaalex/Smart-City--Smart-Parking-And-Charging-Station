package resource;

import model.ChargeStatusDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

//This sensor depends on the presence of the vehicle

public class ChargeStatusSensorResource extends SensorResource<ChargeStatusDescriptor> implements ResourceDataListener<Boolean>{

    private static final Logger logger = LoggerFactory.getLogger(ChargeStatusSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:charge_status";

    private static final long UPDATE_PERIOD = 2000; //10 Seconds

    private static final long TASK_DELAY_TIME = 0; //5 mls before starting the periodic update task

    private static final Integer CHARGING_IF_PRESENT_PROBABILITY = 4;

    private ChargeStatusDescriptor updatedChargeStatus;

    private Random random = null;

    public ChargeStatusSensorResource(){
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    public ChargeStatusSensorResource(String id, String type, long timestamp, ChargeStatusDescriptor updatedChargeStatus) {
        super(id, type, timestamp);
        this.updatedChargeStatus = updatedChargeStatus;
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
    public void onDataChanged(SensorResource<Boolean> sensorResource, Boolean updatedValue) {
        if (sensorResource != null && sensorResource.getType().equals(VehiclePresenceSensorResource.RESOURCE_TYPE)) {
            if (updatedValue == true) {     //If a new vehicle arrived, choose a random value
                logger.info("Vehicle detected by sensor: {}", sensorResource.getId());

                if(updatedChargeStatus == ChargeStatusDescriptor.CHARGING) {
                    if (random.nextInt(CHARGING_IF_PRESENT_PROBABILITY) == 0)       //25% probability
                        updatedChargeStatus = ChargeStatusDescriptor.PLUGGED;
                }else {
                    if (random.nextBoolean())
                        updatedChargeStatus = ChargeStatusDescriptor.CHARGING;
                    else
                        updatedChargeStatus = ChargeStatusDescriptor.PLUGGED;
                }
            }
            else      //If the parking lot is empty, put in UNPLUGGED status
                updatedChargeStatus = ChargeStatusDescriptor.UNPLUGGED;
        }
    }

    public ChargeStatusDescriptor getUpdatedChargeStatus() {
        return updatedChargeStatus;
    }

    public void setUpdatedChargeStatus(ChargeStatusDescriptor updatedChargeStatus) {
        this.updatedChargeStatus = updatedChargeStatus;
    }

    public static void main(String[] args) {

        ChargeStatusSensorResource chargeStatusSensorResource = new ChargeStatusSensorResource();
        logger.info("New {} Resource Created with Id: {} ! New Value: {}",
                chargeStatusSensorResource.getType(),
                chargeStatusSensorResource.getId(),
                chargeStatusSensorResource.loadUpdatedValue());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    for(int i=0; i<100; i++){
                        if (chargeStatusSensorResource.getUpdatedChargeStatus() == ChargeStatusDescriptor.PLUGGED)
                            chargeStatusSensorResource.setUpdatedChargeStatus(ChargeStatusDescriptor.UNPLUGGED);
                        else
                            chargeStatusSensorResource.setUpdatedChargeStatus(ChargeStatusDescriptor.PLUGGED);
                        Thread.sleep(1000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        chargeStatusSensorResource.addDataListener(new ResourceDataListener<ChargeStatusDescriptor>() {
            @Override
            public void onDataChanged(SensorResource<ChargeStatusDescriptor> resource, ChargeStatusDescriptor updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }
}