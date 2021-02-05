package resource;

import model.ChargeStatusDescriptor;
import model.Led;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

public class LedActuatorResource extends SmartObjectResource<Led> implements ResourceDataListener<Boolean>{
    private static Logger logger = LoggerFactory.getLogger(Led.class);

    public static final String RESOURCE_TYPE = "iot.actuator.led";

    private Led isActive;

    public LedActuatorResource() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.isActive = Led.GREEN;

    }

    public LedActuatorResource(String type, long timestamp, Led isActive) {  // server side
        super(type, timestamp);
        this.isActive = isActive;
    }

    public Led getIsActive() {
        return isActive;
    }

    public void setIsActive(Led active) {
        isActive = active;
        notifyUpdate(isActive);
    }

    @Override
    public Led loadUpdatedValue() {
        return this.isActive;
    }

    @Override
    public void onDataChanged(SmartObjectResource<Boolean> smartObjectResource, Boolean updatedValue) {
        if (smartObjectResource != null && smartObjectResource.getType().equals(VehiclePresenceSensorResource.RESOURCE_TYPE)) {
            if (updatedValue == true) {     //If a new vehicle arrived, set led red
                logger.info("Vehicle detected by sensor: {}.. setting parking led RED.", smartObjectResource.getId());
                isActive = Led.RED;
            }
            else      //If the parking lot is empty, set led GREEN
                isActive = Led.GREEN;

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
            public void onDataChanged(SmartObjectResource<Led> resource, Led updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }
}
