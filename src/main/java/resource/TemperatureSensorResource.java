package resource;

import model.ChargeStatusDescriptor;
import model.GpsLocationDescriptor;
import model.Led;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class TemperatureSensorResource extends SmartObjectResource<Double> implements ResourceDataListener<ChargeStatusDescriptor>{

    private static final Logger logger = LoggerFactory.getLogger(VehiclePresenceSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:temperature";

    private static final long UPDATE_PERIOD = 2000; //10 Seconds

    private static final long TASK_DELAY_TIME = 0; //5 mls before starting the periodic update task

    private static final Double MIN_TEMPERATURE = 20.0;

    private static final Double MAX_TEMPERATURE = 80.0;

    private static final Double MAX_TEMPERATURE_VARIATION = 3.0;

    private Boolean temperatureIsRising = false;

    private Double updatedTemperatureSensorValue;

    private Random random = null;

    public TemperatureSensorResource() {
        super(UUID.randomUUID().toString(), TemperatureSensorResource.RESOURCE_TYPE);
        init();
    }

    public TemperatureSensorResource(String id,String type, long timestamp, Double updatedTemperatureSensorValue) {  // server side
        super(id, type, timestamp);
        this.updatedTemperatureSensorValue = updatedTemperatureSensorValue;
    }

    public TemperatureSensorResource(String id, String type, Double temperatureSensorValue) {
        super(id, type);
        this.updatedTemperatureSensorValue = temperatureSensorValue;
        init();
    }

    public TemperatureSensorResource(GpsLocationDescriptor gpsLocationDescriptor, Double temperatureSensorValue) {
        super(UUID.randomUUID().toString(), VehiclePresenceSensorResource.RESOURCE_TYPE);
        this.updatedTemperatureSensorValue = temperatureSensorValue;
        init();
    }

    /**
     * - Start Periodic Parking Lot Availability update
     */
    private void init(){

        try{
            this.random = new Random(System.currentTimeMillis());
            this.updatedTemperatureSensorValue = MIN_TEMPERATURE;

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
                    if(temperatureIsRising) {    //if temperatureIsRising because CHARGING and < than max temp posible, increase it
                        updatedTemperatureSensorValue += MAX_TEMPERATURE_VARIATION * random.nextDouble();
                    }
                    else {
                        Double newTemperature = MAX_TEMPERATURE_VARIATION * random.nextDouble();
                        if ((updatedTemperatureSensorValue - newTemperature) > MIN_TEMPERATURE)
                            updatedTemperatureSensorValue -= newTemperature;
                        //logger.info("Updated Parking Lot: {}", updatedParkingSensorStatus.getIsVehiclePresent());
                    }
                    notifyUpdate(updatedTemperatureSensorValue);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic update ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedTemperatureSensorValue;
    }

    @Override
    public void onDataChanged(SmartObjectResource<ChargeStatusDescriptor> smartObjectResource, ChargeStatusDescriptor updatedValue) {
        if (smartObjectResource != null && smartObjectResource.getType().equals(ChargeStatusSensorResource.RESOURCE_TYPE)) {
            if (updatedValue == ChargeStatusDescriptor.CHARGING) {     //If a vehicle is CHARGING, the temperature is rising
                logger.info("Temperature Sensor is notified that a vehicle is CHARGING - charge status sensor: {}", smartObjectResource.getId());
                temperatureIsRising = true;
            }
            else{
                temperatureIsRising = false;
            }
        }
    }



    public static void main(String[] args) {
        TemperatureSensorResource temperatureSensorResource = new TemperatureSensorResource();

        logger.info("New {} Resource Created with Id: {} ! Updated Value: {}",
                temperatureSensorResource.getType(),
                temperatureSensorResource.getId(),
                temperatureSensorResource.loadUpdatedValue());

        temperatureSensorResource.addDataListener((resource, updatedValue) -> {
            if(resource != null && updatedValue != null)
                logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
            else
                logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
        });
    }
}
