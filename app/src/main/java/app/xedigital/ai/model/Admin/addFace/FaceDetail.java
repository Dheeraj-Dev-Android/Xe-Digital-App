package app.xedigital.ai.model.Admin.addFace;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FaceDetail {

    @SerializedName("Confidence")
    private Object confidence;

    @SerializedName("Quality")
    private Quality quality;

    @SerializedName("BoundingBox")
    private BoundingBox boundingBox;

    @SerializedName("Pose")
    private Pose pose;

    @SerializedName("Landmarks")
    private List<LandmarksItem> landmarks;

    public Object getConfidence() {
        return confidence;
    }

    public Quality getQuality() {
        return quality;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Pose getPose() {
        return pose;
    }

    public List<LandmarksItem> getLandmarks() {
        return landmarks;
    }
}