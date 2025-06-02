package app.xedigital.ai.model.Admin.addFace;

import com.google.gson.annotations.SerializedName;

public class LandmarksItem {

    @SerializedName("Type")
    private String type;

    @SerializedName("X")
    private Object x;

    @SerializedName("Y")
    private Object y;

    public String getType() {
        return type;
    }

    public Object getX() {
        return x;
    }

    public Object getY() {
        return y;
    }
}