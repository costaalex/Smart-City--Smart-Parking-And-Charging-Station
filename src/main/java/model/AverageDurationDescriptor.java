package model;

public class AverageDurationDescriptor {
    protected long sumDurationMillis;
    protected Integer occurrences;

    protected long lastStartTimestamp;

    public AverageDurationDescriptor() {
        this.sumDurationMillis = 0;
        this.occurrences = 0;
        this.lastStartTimestamp = -1;
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
