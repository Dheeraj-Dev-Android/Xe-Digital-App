package app.xedigital.ai.model.Admin.addFace;

import com.google.gson.annotations.SerializedName;

public class Pose {

    @SerializedName("Pitch")
    private Object pitch;

    @SerializedName("Roll")
    private Object roll;

    @SerializedName("Yaw")
    private Object yaw;

    public Object getPitch() {
        return pitch;
    }

    public Object getRoll() {
        return roll;
    }

    public Object getYaw() {
        return yaw;
    }
}