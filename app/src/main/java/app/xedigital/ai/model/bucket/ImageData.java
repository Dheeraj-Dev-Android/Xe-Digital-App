package app.xedigital.ai.model.bucket;

import com.google.gson.annotations.SerializedName;

public class ImageData {

    @SerializedName("ETag")
    private String eTag;

    @SerializedName("Bucket")
    private String bucket;

    @SerializedName("ServerSideEncryption")
    private String serverSideEncryption;

    @SerializedName("key")
    private String key;

    @SerializedName("Location")
    private String location;

    public String getETag() {
        return eTag;
    }

    public String getBucket() {
        return bucket;
    }

    public String getServerSideEncryption() {
        return serverSideEncryption;
    }

    public String getKey() {
        return key;
    }

    public String getLocation() {
        return location;
    }
}