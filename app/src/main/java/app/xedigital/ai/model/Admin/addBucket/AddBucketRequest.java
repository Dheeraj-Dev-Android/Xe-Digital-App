package app.xedigital.ai.model.Admin.addBucket;

import com.google.gson.annotations.SerializedName;

public class AddBucketRequest {

    @SerializedName("image")
    private String image;

    @SerializedName("bucketName")
    private String bucketName;

    @SerializedName("keyName")
    private String keyName;

    public AddBucketRequest(String image, String bucketName, String keyName) {
        this.image = image;
        this.bucketName = bucketName;
        this.keyName = keyName;
    }


    public void setImage(String image) {
        this.image = image;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}