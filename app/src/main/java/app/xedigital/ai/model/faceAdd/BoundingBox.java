package app.xedigital.ai.model.faceAdd;

import com.google.gson.annotations.SerializedName;

public class BoundingBox {

    @SerializedName("Left")
    private Object left;

    @SerializedName("Top")
    private Object top;

    @SerializedName("Height")
    private Object height;

    @SerializedName("Width")
    private Object width;

    public Object getLeft() {
        return left;
    }

    public Object getTop() {
        return top;
    }

    public Object getHeight() {
        return height;
    }

    public Object getWidth() {
        return width;
    }
}