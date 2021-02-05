package model;

import consumer.DataCollectorAndManager;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class AverageChargingDurationDescriptor {
    private long sumChargingDurationMillis;
    private Integer occurrences;

    private long lastChargingStartTimestamp;
    private ChargeStatusDescriptor lastChargeStatus;

    public AverageChargingDurationDescriptor() {
        sumChargingDurationMillis = 0;
        occurrences=0;
        lastChargingStartTimestamp = -1;
        lastChargeStatus = ChargeStatusDescriptor.UNPLUGGED;
    }

    public AverageChargingDurationDescriptor(long sumChargingDuration, Integer occurrences, ChargeStatusDescriptor lastChargeStatus, long lastChargingStartTimestamp) {
        this.sumChargingDurationMillis = sumChargingDuration;
        this.occurrences = occurrences;
        this.lastChargingStartTimestamp = lastChargingStartTimestamp;
        this.lastChargeStatus = lastChargeStatus;
    }

    public Double getSumChargingDurationSeconds() {
        return sumChargingDurationMillis / 1000.0;
    }

    public Double addChargingDurationFromStatusAndTimestamp(ChargeStatusDescriptor newChargeStatus, long timestamp){
        if(lastChargingStartTimestamp != -1){
            //if not charging anymore calculate occupation time and update average
            if(lastChargeStatus == ChargeStatusDescriptor.CHARGING
                    && (newChargeStatus == ChargeStatusDescriptor.PLUGGED || newChargeStatus == ChargeStatusDescriptor.UNPLUGGED)){
                long diffInMillis = getDateDiff(lastChargingStartTimestamp, timestamp, TimeUnit.MILLISECONDS);
                sumChargingDurationMillis += diffInMillis;
                occurrences++;
                LoggerFactory.getLogger(DataCollectorAndManager.class).info("---lastChargingStartTimestamp: {}, actual: {}, sumChargingDurationMinutes : {}, sum: {}",lastChargingStartTimestamp, timestamp, diffInMillis, sumChargingDurationMillis);
                lastChargeStatus = newChargeStatus;


                return diffInMillis * 1000.0; // return last charging duration in seconds
            } //if started charging
            else if((lastChargeStatus == ChargeStatusDescriptor.PLUGGED || lastChargeStatus == ChargeStatusDescriptor.UNPLUGGED)
                    && newChargeStatus == ChargeStatusDescriptor.CHARGING){
                lastChargingStartTimestamp = timestamp;
                lastChargeStatus = newChargeStatus;
            }
        }
        else{
            lastChargeStatus=  newChargeStatus;
            lastChargingStartTimestamp = timestamp;
        }

        return 0.0;
    }
    public Double addChargingDurationSeconds(Double chargingDurationSeconds){
        sumChargingDurationMillis += Math.floor(chargingDurationSeconds * 1000);
        occurrences++;
        return getAverageChargingDurationSeconds();
    }


    public static long getDateDiff(long timeUpdate, long timeNow, TimeUnit timeUnit)
    {
        long diffInMillies = Math.abs(timeNow - timeUpdate);
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public long getLastChargingStartTimestamp() {
        return lastChargingStartTimestamp;
    }

    public void setLastChargingStartTimestamp(long lastChargingStartTimestamp) {
        this.lastChargingStartTimestamp = lastChargingStartTimestamp;
    }

    public ChargeStatusDescriptor getLastChargeStatus() {
        return lastChargeStatus;
    }

    public void setLastChargeStatus(ChargeStatusDescriptor lastChargeStatus) {
        this.lastChargeStatus = lastChargeStatus;
    }

    public void setSumChargingDurationMillis(long sumChargingDurationMillis) {
        this.sumChargingDurationMillis = sumChargingDurationMillis;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public Double getAverageChargingDurationSeconds(){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if(occurrences > 0)
            return Math.floor(sumChargingDurationMillis / 1000.0) / occurrences;
        else
            return 0.0;
    }
}
