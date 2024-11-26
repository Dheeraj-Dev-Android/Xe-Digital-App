package app.xedigital.ai.model.policy;

import com.google.gson.annotations.SerializedName;

public class PoliciesItem {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("policyFileURLKey")
    private String policyFileURLKey;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("__v")
    private int v;

    @SerializedName("name")
    private String name;

    @SerializedName("active")
    private boolean active;

    @SerializedName("policyFileURL")
    private String policyFileURL;

    @SerializedName("company")
    private String company;

    @SerializedName("_id")
    private String id;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPolicyFileURLKey() {
        return policyFileURLKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getV() {
        return v;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public String getPolicyFileURL() {
        return policyFileURL;
    }

    public String getCompany() {
        return company;
    }

    public String getId() {
        return id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}