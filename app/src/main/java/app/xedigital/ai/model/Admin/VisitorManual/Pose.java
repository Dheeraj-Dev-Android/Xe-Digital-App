package app.xedigital.ai.model.Admin.VisitorManual;

import com.google.gson.annotations.SerializedName;

public class Pose {

    @SerializedName("Pitch")
    private Object pitch;

    @SerializedName("Roll")
    private Object roll;

    @SerializedName("Yaw")
    private Object yaw;

    public void setPitch(Object pitch) {
        this.pitch = pitch;
    }

    public void setRoll(Object roll) {
        this.roll = roll;
    }

    public void setYaw(Object yaw) {
        this.yaw = yaw;
    }
}