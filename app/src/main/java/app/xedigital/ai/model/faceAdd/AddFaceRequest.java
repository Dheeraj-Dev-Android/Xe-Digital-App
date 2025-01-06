package app.xedigital.ai.model.faceAdd;

import com.google.gson.annotations.SerializedName;

public class AddFaceRequest {

    @SerializedName("image")
    private String image;

    @SerializedName("collection_name")
    private String collectionName;

    public void setImage(String image) {
        this.image = image;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}