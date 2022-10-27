package me.mohamad82.pensieve.replaying;

public class PlayBackControl {

    private int progress;
    private int maxProgress;
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
        this.progress = Math.min(Math.max(progress, 1), maxProgress - 1);
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

    public String getMaxProgressFormatted() {
        return formatTime(maxProgress);
    }

    public String getProgressFormatted() {
        return formatTime(progress);
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

    private String formatTime(int time) {
        int seconds = time / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
