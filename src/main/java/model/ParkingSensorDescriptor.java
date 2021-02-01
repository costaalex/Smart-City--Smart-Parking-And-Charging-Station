package model;

public class ParkingSensorDescriptor {
    double isVehiclePresent;
    Led led;

    public ParkingSensorDescriptor() {
    }

    public ParkingSensorDescriptor(double isVehiclePresent, Led led) {
        this.isVehiclePresent = isVehiclePresent;
        this.led = led;
    }

    public double getIsVehiclePresent() {
        return isVehiclePresent;
    }

    public Led getLed() {
        return led;
    }

    public void setIsVehiclePresent(double isVehiclePresent) {
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
