package resource;


import model.ChargeStatusDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class EnergyConsumptionSensorResource extends SensorResource<Double> implements ResourceDataListener<ChargeStatusDescriptor>{

    private static Logger logger = LoggerFactory.getLogger(EnergyConsumptionSensorResource.class);

    //kWh - kilowatt-hour
    private static final double MIN_ENERGY_VALUE = 0.5;

    //kWh - kilowatt-hour
    private static final double MAX_ENERGY_VALUE = 1.0;

    private static final String LOG_DISPLAY_NAME = "EnergyConsumptionSensor";

    //Ms associated to data update
    private static final long UPDATE_PERIOD = 2000;

    private static final long TASK_DELAY_TIME = 0;

    public static final String RESOURCE_TYPE = "iot:sensor:energy_consumption";

    private Double updatedValue;

    private Random random;

    private Timer updateTimer = null;

    private boolean isConsumingEnergy = false;

    public EnergyConsumptionSensorResource() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    public EnergyConsumptionSensorResource(String id, String type, long timestamp, Double updatedValue) {
        super(id, type, timestamp);
        this.updatedValue = updatedValue;
    }

    private void init(){

        try{
            this.random = new Random(System.currentTimeMillis());
            this.updatedValue = 0.0;

            startPeriodicEventValueUpdateTask();
        }catch (Exception e){
            logger.error("Error initializing the IoT Resource ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public void onDataChanged(SensorResource<ChargeStatusDescriptor> sensorResource, ChargeStatusDescriptor updatedValue) {
        if (sensorResource != null && sensorResource.getType().equalsIgnoreCase(ChargeStatusSensorResource.RESOURCE_TYPE)) {
            if (updatedValue == ChargeStatusDescriptor.CHARGING) {     //If a vehicle is CHARGING, the temperature is rising
                logger.info("Energy Consumption Sensor is notified that a vehicle is CHARGING - charge status sensor: {}", sensorResource.getId());
                isConsumingEnergy = true;
            }
            else{
                isConsumingEnergy = false;
            }
        }
    }

    private void startPeriodicEventValueUpdateTask(){

        try{
            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);

            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    if(isConsumingEnergy){
                        double value = MIN_ENERGY_VALUE + (MAX_ENERGY_VALUE * random.nextDouble()) ;
                        updatedValue = value;
                    }
                    else
                        updatedValue = 0.0;

                    notifyUpdate(updatedValue);

                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedValue;
    }

    public void setActive(boolean active) {
        isConsumingEnergy = active;
    }

    public Double getUpdatedValue() {
        return updatedValue;
    }

    public static void main(String[] args) {

        EnergyConsumptionSensorResource rawResource = new EnergyConsumptionSensorResource();
        rawResource.setActive(false);
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                rawResource.getType(),
                rawResource.getId(),
                LOG_DISPLAY_NAME,
                rawResource.loadUpdatedValue());

        rawResource.addDataListener(new ResourceDataListener<Double>() {
            @Override
            public void onDataChanged(SensorResource<Double> resource, Double updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }

}