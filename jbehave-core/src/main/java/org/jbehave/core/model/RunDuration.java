package org.jbehave.core.model;

public class RunDuration {
    private final long timeoutInSecs;
    private long totalDurationInSecs;

    public RunDuration(long timeoutInSecs) {
        this.timeoutInSecs = timeoutInSecs;
    }

    public long getTotalDurationInSecs() {
        return totalDurationInSecs;
    }

    public void updateTotalDurationInSecs(long storyDurationInSecs) {
        this.totalDurationInSecs = totalDurationInSecs + storyDurationInSecs;
    }

    public long getTimeoutInSecs() {
        return timeoutInSecs;
    }

    public boolean timedOut(long storyDurationInSecs) {
        return timeoutInSecs != 0 && getTotalDurationInSecs() + storyDurationInSecs > timeoutInSecs;
    }
}
