package app.xedigital.ai.ui.mrm;

public class TimeSlot {

    private final String display;      // "01:00", "01:30"
    private final int hour;
    private final int minute;
    private final int totalMinutes;    // For easy comparison
    private boolean enabled = true;
    private boolean booked = false;    // Booked by existing meeting

    public TimeSlot(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.totalMinutes = hour * 60 + minute;
        this.display = String.format(java.util.Locale.getDefault(),
                "%02d:%02d", hour, minute);
    }

    public String getDisplay() {
        return display;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}