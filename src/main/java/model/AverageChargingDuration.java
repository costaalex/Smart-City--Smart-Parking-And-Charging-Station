package model;

public class AverageChargingDuration {
    private Double sumChargingDuration;
    private Integer occurrences;

    public AverageChargingDuration() {
        sumChargingDuration = 0.0;
        occurrences=0;
    }

    public AverageChargingDuration(Double sumChargingDuration, Integer occurrences) {
        this.sumChargingDuration = sumChargingDuration;
        this.occurrences = occurrences;
    }

    public Double getSumChargingDuration() {
        return sumChargingDuration;
    }

    public Double addChargingDuration(Double chargingDuration){
        sumChargingDuration += chargingDuration;
        occurrences++;

        return getAverageChargingDuration();
    }

    public void setSumChargingDuration(Double sumChargingDuration) {
        this.sumChargingDuration = sumChargingDuration;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public Double getAverageChargingDuration(){
        if(occurrences > 0)
            return sumChargingDuration / occurrences;
        else
            return null;
    }
}
