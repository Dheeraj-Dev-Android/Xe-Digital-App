package app.xedigital.ai.model.visitorsDetails;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("visitor")
    private Visitor visitor;

    public Visitor getVisitor() {
        return visitor;
    }
}