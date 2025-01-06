package app.xedigital.ai.model.bucket;

import com.google.gson.annotations.SerializedName;

public class BucketRequest {

    @SerializedName("bucketName")
    private String bucketName;

    @SerializedName("image")
    private String image;

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setImage(String image) {
        this.image = image;
    }
}