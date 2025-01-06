package app.xedigital.ai.model.preApprovedVisitorRequest;

import com.google.gson.annotations.SerializedName;

public class LandmarksItem {

    @SerializedName("Type")
    private String type;

    @SerializedName("X")
    private Object x;

    @SerializedName("Y")
    private Object y;

    public void setType(String type) {
        this.type = type;
    }

    public void setX(Object x) {
        this.x = x;
    }

    public void setY(Object y) {
        this.y = y;
    }
}