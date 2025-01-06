package app.xedigital.ai.model.faceAdd;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("FaceDetail")
    private FaceDetail faceDetail;

    @SerializedName("Face")
    private Face face;

    public FaceDetail getFaceDetail() {
        return faceDetail;
    }

    public Face getFace() {
        return face;
    }
}