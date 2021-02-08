package resource;

import model.Led;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LedActuatorResource extends SensorResource<Led> implements ResourceDataListener<Boolean>{
    private static Logger logger = LoggerFactory.getLogger(Led.class);

    public static final String RESOURCE_TYPE = "iot:actuator:led";

    private Led isActive;

    public LedActuatorResource() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.isActive = Led.GREEN;

    }

    public LedActuatorResource(String id, String type, long timestamp, Led isActive) {  // server side
        super(id, type, timestamp);
        this.isActive = isActive;
    }

    public Led getIsActive() {
        return isActive;
    }

    public void setIsActive(Led isActive) {
        this.isActive = isActive;
        notifyUpdate(this.isActive);
    }

    @Override
    public Led loadUpdatedValue() {
        return this.isActive;
    }

    @Override
    public void onDataChanged(SensorResource<Boolean> sensorResource, Boolean updatedValue) {
        if (sensorResource != null && sensorResource.getType().equalsIgnoreCase(VehiclePresenceSensorResource.RESOURCE_TYPE)) {
            if (updatedValue == true && getIsActive() != Led.YELLOW) {     //If a new vehicle arrived, set led red
                logger.info("Vehicle detected by sensor: {}.. setting led RED.", sensorResource.getId());
                isActive = Led.RED;
            }
            else if(getIsActive() != Led.YELLOW)     //If the parking lot is empty, set led GREEN
                isActive = Led.GREEN;
            notifyUpdate(isActive);
        }
    }

    public static void main(String[] args) {

        LedActuatorResource ledActuatorResourceResource = new LedActuatorResource();
        logger.info("New {} Resource Created with Id: {} ! New Value: {}",
                ledActuatorResourceResource.getType(),
                ledActuatorResourceResource.getId(),
                ledActuatorResourceResource.loadUpdatedValue());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    for(int i=0; i<100; i++){
                        if (ledActuatorResourceResource.getIsActive() == Led.GREEN)
                            ledActuatorResourceResource.setIsActive(Led.RED);
                        else
                            ledActuatorResourceResource.setIsActive(Led.GREEN);
                        Thread.sleep(1000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        ledActuatorResourceResource.addDataListener(new ResourceDataListener<Led>() {
            @Override
            public void onDataChanged(SensorResource<Led> resource, Led updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }
}
