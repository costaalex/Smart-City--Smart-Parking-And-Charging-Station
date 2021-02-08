package model;

import java.util.concurrent.TimeUnit;

public class AverageParkingDurationDescriptor extends AverageDurationDescriptor{

    private Boolean lastParkingState;

    public AverageParkingDurationDescriptor() {
        super();
        lastParkingState = false;
    }

    public Double addParkingDurationFromStatusAndTimestamp(Boolean newParkingState, long timestamp){
        if(super.getLastStartTimestamp() != -1){
            //if not parking lot is freed calculate occupation time and update average
            if(lastParkingState == true && newParkingState == false){
                long diffInMillis = getDateDiff(super.getLastStartTimestamp(), timestamp, TimeUnit.MILLISECONDS);
                super.addSumDurationMillis(diffInMillis);
                super.addOccurrence();
                lastParkingState = false;

                return diffInMillis / 1000.0; // return last parking duration in seconds
            }//if parked now
            else if(lastParkingState == false && newParkingState == true){
                super.setLastStartTimestamp(timestamp);
                lastParkingState = true;
            }
        }
        else{
            super.setLastStartTimestamp(timestamp);
            lastParkingState = newParkingState;
        }
        return 0.0;
    }

    public static long getDateDiff(long timeUpdate, long timeNow, TimeUnit timeUnit)
    {
        long diffInMillies = Math.abs(timeNow - timeUpdate);
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public Double addParkingDurationSeconds(Double parkingDurationSeconds){
        super.addSumDurationMillis((long) Math.floor(parkingDurationSeconds * 1000));
        super.addOccurrence();
        return getAverageParkingDurationSeconds();
    }

    public Double getAverageParkingDurationSeconds(){
        if(super.getOccurrences() > 0)
            return Math.floor(super.getSumDurationMillis() / 1.0) / 1000.0 / super.getOccurrences();
        else
            return 0.0;
    }


}
