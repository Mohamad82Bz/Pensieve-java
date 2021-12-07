package me.mohamad82.pensieve.replaying;

public class PlayBackControl {

    private int progress = 0;
    private int maxProgress;
    private String progressFormatted;
    private boolean pause;
    private float volume;
    private Speed speed;

    protected PlayBackControl() {
        this.progress = 0;
        this.pause = false;
        this.volume = 1;
        this.speed = Speed.x1;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.min(progress, maxProgress);
    }

    public void addProgress(int progressToAdd) {
        setProgress(progress + progressToAdd);
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    protected void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public String getProgressFormatted() {
        return progressFormatted;
    }

    protected void setProgressFormatted(String progressFormatted) {
        this.progressFormatted = progressFormatted;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public Speed getSpeed() {
        return speed;
    }

    public void setSpeed(Speed speed) {
        this.speed = speed;
    }

    public enum Speed {
        x025,
        x050,
        x1,
        x2,
        x5
    }

}
