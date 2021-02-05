package model;

public class AverageParkingDurationDescriptor {
    private Double sumParkingDuration;
    private Integer occurrences;

    public AverageParkingDurationDescriptor(Double sumParkingDuration, Integer occurrences) {
        this.sumParkingDuration = sumParkingDuration;
        this.occurrences = occurrences;
    }

    public AverageParkingDurationDescriptor() {
        sumParkingDuration = 0.0;
        occurrences = 0;
    }

    public Double getSumParkingDuration() {
        return sumParkingDuration;
    }

    public Double addParkingDuration(Double parkingDuration){
        sumParkingDuration += parkingDuration;
        occurrences++;

        return getAverageParkingDuration();
    }

    public void setSumParkingDuration(Double sumParkingDuration) {
        this.sumParkingDuration = sumParkingDuration;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public Double getAverageParkingDuration(){
        if(occurrences > 0)
            return sumParkingDuration / occurrences;
        else
            return null;
    }
}
