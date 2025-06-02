package app.xedigital.ai.model.Admin.VisitorManual;

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

    public void setConfidence(Object confidence) {
        this.confidence = confidence;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public void setLandmarks(List<LandmarksItem> landmarks) {
        this.landmarks = landmarks;
    }
}