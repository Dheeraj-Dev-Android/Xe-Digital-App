package app.xedigital.ai.model.Admin.addFace;

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

    public String getFaceId() {
        return faceId;
    }

    public Object getConfidence() {
        return confidence;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public String getImageId() {
        return imageId;
    }
}