package app.xedigital.ai.model.preApprovedVisitorRequest;

import com.google.gson.annotations.SerializedName;

public class FaceData {

    @SerializedName("Similarity")
    private Object similarity;

    @SerializedName("Face")
    private Face face;

    public void setSimilarity(Object similarity) {
        this.similarity = similarity;
    }

    public void setFace(Face face) {
        this.face = face;
    }
}