package app.xedigital.ai.model.radius;

import com.google.gson.annotations.SerializedName;

public class RadiusItem {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("lattitude")
    private Double lattitude;

    @SerializedName("radious")
    private int radious;

    @SerializedName("lognitude")
    private Double lognitude;

    @SerializedName("__v")
    private int v;

    @SerializedName("_id")
    private String id;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public Double getLattitude() {
        return lattitude;
    }

    public int getRadious() {
        return radious;
    }

    public Double getLognitude() {
        return lognitude;
    }

    public int getV() {
        return v;
    }

    public String getId() {
        return id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}