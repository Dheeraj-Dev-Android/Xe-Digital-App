package app.xedigital.ai.admin.adminModal.visitorFace;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("visitor")
    private Visitor visitor;

    public Visitor getVisitor() {
        return visitor;
    }
}