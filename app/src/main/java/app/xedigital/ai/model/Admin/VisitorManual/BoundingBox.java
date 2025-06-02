package app.xedigital.ai.model.Admin.VisitorManual;

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

    public void setLeft(Object left) {
        this.left = left;
    }

    public void setTop(Object top) {
        this.top = top;
    }

    public void setHeight(Object height) {
        this.height = height;
    }

    public void setWidth(Object width) {
        this.width = width;
    }
}