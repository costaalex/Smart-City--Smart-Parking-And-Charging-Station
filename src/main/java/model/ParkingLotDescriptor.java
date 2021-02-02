package model;

public class ParkingLotDescriptor {

    boolean isVehiclePresent;
    Led led;

    public ParkingLotDescriptor() {
        this.isVehiclePresent = false;
        this.led = Led.GREEN;
    }

    public ParkingLotDescriptor(boolean isVehiclePresent, Led led) {
        this.isVehiclePresent = isVehiclePresent;
        this.led = led;
    }

    public boolean getIsVehiclePresent() {
        return isVehiclePresent;
    }

    public Led getLed() {
        return led;
    }

    public void setIsVehiclePresent(boolean isVehiclePresent) {
        this.isVehiclePresent = isVehiclePresent;
    }

    public void setLed(Led led) {
        this.led = led;
    }

    @Override
    public String toString() {
        return "ParkingSensor{" +
                "isVehiclePresent=" + isVehiclePresent +
                ", led=" + led +
                '}';
    }
}
