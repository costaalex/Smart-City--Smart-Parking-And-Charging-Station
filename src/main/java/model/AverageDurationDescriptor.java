package model;

public class AverageDurationDescriptor {
    private long sumDurationMillis;
    private Integer occurrences;

    private long lastStartTimestamp;

    public AverageDurationDescriptor() {
        this.sumDurationMillis = 0;
        this.occurrences = 0;
        this.lastStartTimestamp = -1;
    }

    public long getSumDurationMillis() {
        return sumDurationMillis;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public long getLastStartTimestamp() {
        return lastStartTimestamp;
    }

    public void setLastStartTimestamp(long lastStartTimestamp) {
        this.lastStartTimestamp = lastStartTimestamp;
    }

    public void addOccurrence(){
        this.occurrences++;
    }
    public void addSumDurationMillis(long millis){
        this.sumDurationMillis += millis;
    }


}
