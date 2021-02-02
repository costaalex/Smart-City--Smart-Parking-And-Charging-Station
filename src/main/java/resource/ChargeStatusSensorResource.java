package resource;

import model.ChargeStatusDescriptor;
import model.GpsLocationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

//This sensor depends on the presence of the vehicle

public class ChargeStatusSensorResource extends SmartObjectResource<ChargeStatusDescriptor> implements ResourceDataListener<Boolean>{

    private static final Logger logger = LoggerFactory.getLogger(VehiclePresenceSensorResource.class);

    public static final String RESOURCE_TYPE = "iot:sensor:chargeStatus";

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
            Random random = new Random(System.currentTimeMillis());
            this.updatedChargeStatus = ChargeStatusDescriptor.UNPLUGGED;

        }catch (Exception e){
            logger.error("Error init ChargeStatusSensorResource ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public ChargeStatusDescriptor loadUpdatedValue() {
        return this.updatedChargeStatus;
    }

    @Override
    public void onDataChanged(SmartObjectResource<Boolean> smartObjectResource, Boolean updatedValue) {
        if (smartObjectResource != null && smartObjectResource.getType().equals(VehiclePresenceSensorResource.RESOURCE_TYPE) && updatedValue == true) {
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
        //logger.info("Updated Charge Status: {}", updatedChargeStatus);

        notifyUpdate(updatedChargeStatus);
    }
}
