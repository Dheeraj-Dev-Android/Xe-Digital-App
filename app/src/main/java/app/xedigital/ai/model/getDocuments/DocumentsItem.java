package app.xedigital.ai.model.getDocuments;

import com.google.gson.annotations.SerializedName;

public class DocumentsItem {

    @SerializedName("docFileURLKey")
    private String docFileURLKey;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("empLastName")
    private String empLastName;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("empFirstName")
    private String empFirstName;

    @SerializedName("__v")
    private int v;

    @SerializedName("docFileURL")
    private String docFileURL;

    @SerializedName("_id")
    private String id;

    @SerializedName("documentName")
    private String documentName;

    @SerializedName("user")
    private String user;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getDocFileURLKey() {
        return docFileURLKey;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public int getV() {
        return v;
    }

    public String getDocFileURL() {
        return docFileURL;
    }

    public String getId() {
        return id;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getUser() {
        return user;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}