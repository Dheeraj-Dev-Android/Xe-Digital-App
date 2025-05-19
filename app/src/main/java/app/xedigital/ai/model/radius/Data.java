package app.xedigital.ai.model.radius;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("radious")
    private List<RadiusItem> radious;

    public List<RadiusItem> getRadious() {
        return radious;
    }
}