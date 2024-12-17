package app.xedigital.ai.model.uploadDocument;

import com.google.gson.annotations.SerializedName;

public class UploadDocumentRequest {

    @SerializedName("docFileURLKey")
    private String docFileURLKey;

    @SerializedName("image")
    private String image;

    @SerializedName("empLastName")
    private String empLastName;

    @SerializedName("empFirstName")
    private String empFirstName;

    @SerializedName("empEmail")
    private String empEmail;

    @SerializedName("docFileURL")
    private String docFileURL;

    @SerializedName("company")
    private String company;

    @SerializedName("documentName")
    private String documentName;

    @SerializedName("user")
    private String user;

    public String getDocFileURLKey() {
        return docFileURLKey;
    }

    public void setDocFileURLKey(String docFileURLKey) {
        this.docFileURLKey = docFileURLKey;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public void setEmpLastName(String empLastName) {
        this.empLastName = empLastName;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public void setEmpFirstName(String empFirstName) {
        this.empFirstName = empFirstName;
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public void setEmpEmail(String empEmail) {
        this.empEmail = empEmail;
    }

    public String getDocFileURL() {
        return docFileURL;
    }

    public void setDocFileURL(String docFileURL) {
        this.docFileURL = docFileURL;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}