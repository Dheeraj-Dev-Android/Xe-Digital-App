package app.xedigital.ai.model.employeeOnboarding;

import com.google.gson.annotations.SerializedName;

public class DocumentsItem {

    @SerializedName("documentName")
    private String documentName;

    @SerializedName("_id")
    private String id;

    @SerializedName("documentFile")
    private String documentFile;

    @SerializedName("documentFileURL")
    private String documentFileURL;

    @SerializedName("documentFileURLKey")
    private String documentFileURLKey;

    public String getDocumentName() {
        return documentName;
    }

    public String getId() {
        return id;
    }

    public String getDocumentFile() {
        return documentFile;
    }

    public String getDocumentFileURL() {
        return documentFileURL;
    }

    public String getDocumentFileURLKey() {
        return documentFileURLKey;
    }
}