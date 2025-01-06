package app.xedigital.ai.model.preApprovedVisitorRequest;

import com.google.gson.annotations.SerializedName;

public class FaceData {

    @SerializedName("FaceDetail")
    private FaceDetail faceDetail;

    @SerializedName("Face")
    private Face face;

    public void setFaceDetail(FaceDetail faceDetail) {
        this.faceDetail = faceDetail;
    }

    public void setFace(Face face) {
        this.face = face;
    }
}