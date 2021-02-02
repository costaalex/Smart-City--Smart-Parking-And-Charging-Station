package resource;

import model.ChargeStatusDescriptor;
import model.Led;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LedActuatorResource extends SmartObjectResource<Led>{
    private static Logger logger = LoggerFactory.getLogger(Led.class);

    private static final String RESOURCE_TYPE = "iot.actuator.led";

    private Led isActive;

    public LedActuatorResource() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.isActive = Led.GREEN;
    }

    public Led getActive() {
        return isActive;
    }

    public void setActive(Led active) {
        isActive = active;
        notifyUpdate(isActive);
    }

    @Override
    public Led loadUpdatedValue() {
        return this.isActive;
    }

    public static void main(String[] args) {

        LedActuatorResource rawResource = new LedActuatorResource();
        logger.info("New {} Resource Created with Id: {} ! New Value: {}",
                rawResource.getType(),
                rawResource.getId(),
                rawResource.loadUpdatedValue());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    for(int i=0; i<100; i++){
                        if (rawResource.getActive() == Led.GREEN)
                            rawResource.setActive(Led.RED);
                        else
                            rawResource.setActive(Led.GREEN);
                        Thread.sleep(1000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        rawResource.addDataListener(new ResourceDataListener<Led>() {
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
