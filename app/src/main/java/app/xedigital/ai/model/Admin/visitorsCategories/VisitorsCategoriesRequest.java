package app.xedigital.ai.model.Admin.visitorsCategories;

import com.google.gson.annotations.SerializedName;

public class VisitorsCategoriesRequest {

    @SerializedName("image")
    private String image;

    @SerializedName("collection_name")
    private String collectionName;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}