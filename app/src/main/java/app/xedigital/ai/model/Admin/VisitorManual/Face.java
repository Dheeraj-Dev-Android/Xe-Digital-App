package app.xedigital.ai.model.Admin.VisitorManual;

import com.google.gson.annotations.SerializedName;

public class Face {

    @SerializedName("FaceId")
    private String faceId;

    @SerializedName("Confidence")
    private Object confidence;

    @SerializedName("BoundingBox")
    private BoundingBox boundingBox;

    @SerializedName("ImageId")
    private String imageId;

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public void setConfidence(Object confidence) {
        this.confidence = confidence;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}