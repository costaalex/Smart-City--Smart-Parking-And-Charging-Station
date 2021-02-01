package resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import model.Led;

import java.util.UUID;

public class ParkinglotSmartObject extends SmartObjectResource<Double> {

    private static final Logger logger = LoggerFactory.getLogger(ParkinglotSmartObject.class);

    public static final String RESOURCE_TYPE = "iot:sensor:parkinglot";

    Double isVehiclePresent;
    Led led;

    public ParkinglotSmartObject(String id, String type, Double latitude, Double longitude, Double isVehiclePresent, Led led) {
        super(id, type, latitude, longitude);
        this.isVehiclePresent = isVehiclePresent;
        this.led = led;
        init();
    }

    public ParkinglotSmartObject() {
        super(UUID.randomUUID().toString(), ParkinglotSmartObject.RESOURCE_TYPE, 0.0, 0.0);
        this.isVehiclePresent = 0.0;
        this.led = Led.GREEN;
        init();
    }
    private void init(){

        try{
            //codice presenza auto

        }catch (Exception e){
            logger.error("Error init ParkinglotSmartObject ! Msg: {}", e.getLocalizedMessage());
        }

    }
    @Override
    public Double loadUpdatedValue() {
        return null;
    }
}
