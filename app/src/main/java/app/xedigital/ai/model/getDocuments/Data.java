package app.xedigital.ai.model.getDocuments;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("documents")
    private List<DocumentsItem> documents;

    public List<DocumentsItem> getDocuments() {
        return documents;
    }
}