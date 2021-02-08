package model;

import java.util.concurrent.TimeUnit;

public class AverageChargingDurationDescriptor extends AverageDurationDescriptor{

    private ChargeStatusDescriptor lastChargeStatus;

    public AverageChargingDurationDescriptor() {
        super();
        lastChargeStatus = ChargeStatusDescriptor.UNPLUGGED;
    }

    public Double addChargingDurationFromStatusAndTimestamp(ChargeStatusDescriptor newChargeStatus, long timestamp){
        if(super.getLastStartTimestamp() != -1){
            //if not charging anymore calculate occupation time and update average
            if(lastChargeStatus == ChargeStatusDescriptor.CHARGING
                    && (newChargeStatus == ChargeStatusDescriptor.PLUGGED || newChargeStatus == ChargeStatusDescriptor.UNPLUGGED)){
                long diffInMillis = getDateDiff(super.getLastStartTimestamp(), timestamp, TimeUnit.MILLISECONDS);
                super.addSumDurationMillis(diffInMillis);
                super.addOccurrence();
                lastChargeStatus = newChargeStatus;


                return diffInMillis / 1000.0; // return last charging duration in seconds
            } //if started charging
            else if((lastChargeStatus == ChargeStatusDescriptor.PLUGGED || lastChargeStatus == ChargeStatusDescriptor.UNPLUGGED)
                    && newChargeStatus == ChargeStatusDescriptor.CHARGING){
                super.setLastStartTimestamp(timestamp);
                lastChargeStatus = newChargeStatus;
            }
        }
        else{
            lastChargeStatus=  newChargeStatus;
            super.setLastStartTimestamp(timestamp);
        }

        return 0.0;
    }
    public Double addChargingDurationSeconds(Double chargingDurationSeconds){
        super.addSumDurationMillis( (long) Math.floor(chargingDurationSeconds * 1000));
        super.addOccurrence();
        return getAverageChargingDurationSeconds();
    }

    public static long getDateDiff(long timeUpdate, long timeNow, TimeUnit timeUnit)
    {
        long diffInMillies = Math.abs(timeNow - timeUpdate);
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public Double getAverageChargingDurationSeconds(){
        if(super.getOccurrences() > 0)
            return Math.floor(super.getSumDurationMillis() / 1.0) / 1000.0 / super.getOccurrences();
        else
            return 0.0;
    }
}
