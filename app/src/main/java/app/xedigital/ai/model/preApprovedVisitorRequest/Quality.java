package app.xedigital.ai.model.preApprovedVisitorRequest;

import com.google.gson.annotations.SerializedName;

public class Quality {

    @SerializedName("Brightness")
    private Object brightness;

    @SerializedName("Sharpness")
    private Object sharpness;

    public void setBrightness(Object brightness) {
        this.brightness = brightness;
    }

    public void setSharpness(Object sharpness) {
        this.sharpness = sharpness;
    }
}