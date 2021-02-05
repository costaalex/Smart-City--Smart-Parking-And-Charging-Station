package model;

import consumer.DataCollectorAndManager;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AverageParkingDurationDescriptor {
    private long sumParkingDurationMillis;
    private Integer occurrences;

    private long lastParkingStartTimestamp;
    private Boolean lastParkingState;

    public AverageParkingDurationDescriptor(long sumParkingDurationMillis, Integer occurrences, Boolean lastParkingState, long lastParkingStartTimestamp) {
        this.sumParkingDurationMillis = sumParkingDurationMillis;
        this.occurrences = occurrences;
        this.lastParkingStartTimestamp = -1;
        this.lastParkingState = false;
    }

    public AverageParkingDurationDescriptor() {
        sumParkingDurationMillis = 0;
        occurrences = 0;
        lastParkingStartTimestamp = -1;
        lastParkingState = false;
    }

    public long getSumParkingDurationMillis() {
        return sumParkingDurationMillis;
    }

    public Double addParkingDurationFromStatusAndTimestamp(Boolean newParkingState, long timestamp){
        if(lastParkingStartTimestamp != -1){
            //if not parking lot is freed calculate occupation time and update average
            if(lastParkingState == true && newParkingState == false){
                long diffInMillis = getDateDiff(lastParkingStartTimestamp, timestamp, TimeUnit.MILLISECONDS);
                sumParkingDurationMillis += diffInMillis;
                occurrences++;
                lastParkingState = false;

                return diffInMillis * 1000.0; // return last parking duration in seconds
            }//if parked now
            else if(lastParkingState == false && newParkingState == true){
                lastParkingStartTimestamp = timestamp;
                lastParkingState = true;
            }
        }
        else{
            lastParkingStartTimestamp = timestamp;
            lastParkingState = newParkingState;
        }
        return 0.0;
    }

    public static long getDateDiff(long timeUpdate, long timeNow, TimeUnit timeUnit)
    {
        long diffInMillies = Math.abs(timeNow - timeUpdate);
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public Double addParkingDurationSeconds(Double parkingDurationMillis){
        sumParkingDurationMillis += Math.floor(parkingDurationMillis * 1000);
        occurrences++;
        return getAverageParkingDurationSeconds();
    }

    public void setSumParkingDuration(long sumParkingDurationMillis) {
        this.sumParkingDurationMillis = sumParkingDurationMillis;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public Double getAverageParkingDurationSeconds(){
        if(occurrences > 0)
            return sumParkingDurationMillis / 1000.0 / occurrences;
        else
            return null;
    }
}
