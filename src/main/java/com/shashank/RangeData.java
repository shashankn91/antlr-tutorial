package com.shashank;

public class RangeData {


    public Double getStartRange() {
        return startRange;
    }

    public Double getEndRange() {
        return endRange;
    }

    private final Double startRange;
    private final Double endRange;


    public RangeData( Double startRange, Double endRange) {
        this.startRange = startRange;
        this.endRange = endRange;
    }
}
