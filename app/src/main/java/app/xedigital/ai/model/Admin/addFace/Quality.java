package app.xedigital.ai.model.Admin.addFace;

import com.google.gson.annotations.SerializedName;

public class Quality {

    @SerializedName("Brightness")
    private Object brightness;

    @SerializedName("Sharpness")
    private Object sharpness;

    public Object getBrightness() {
        return brightness;
    }

    public Object getSharpness() {
        return sharpness;
    }
}