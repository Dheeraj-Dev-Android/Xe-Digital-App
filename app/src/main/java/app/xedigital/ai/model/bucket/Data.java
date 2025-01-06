package app.xedigital.ai.model.bucket;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("imageData")
    private ImageData imageData;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("imageKey")
    private String imageKey;

    public ImageData getImageData() {
        return imageData;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageKey() {
        return imageKey;
    }
}