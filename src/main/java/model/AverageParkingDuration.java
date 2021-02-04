package model;

public class AverageParkingDuration {
    private Double sumParkingDuration;
    private Integer occurrences;

    public AverageParkingDuration(Double sumParkingDuration, Integer occurrences) {
        this.sumParkingDuration = sumParkingDuration;
        this.occurrences = occurrences;
    }

    public AverageParkingDuration() {
    }

    public Double getSumParkingDuration() {
        return sumParkingDuration;
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
