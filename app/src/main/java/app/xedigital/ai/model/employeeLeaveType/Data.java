package app.xedigital.ai.model.employeeLeaveType;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("assignDate")
    private String assignDate;

    @SerializedName("creditLeave")
    private double creditLeave;

    @SerializedName("debitLeave")
    private float debitLeave;

    @SerializedName("_id")
    private String id;

    @SerializedName("usedLeave")
    private double usedLeave;

    @SerializedName("openingLeave")
    private double openingLeave;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getAssignDate() {
        return assignDate;
    }

    public void setAssignDate(String assignDate) {
        this.assignDate = assignDate;
    }

    public double getCreditLeave() {
        return creditLeave;
    }

    public void setCreditLeave(int creditLeave) {
        this.creditLeave = creditLeave;
    }

    public float getDebitLeave() {
        return debitLeave;
    }

    public void setDebitLeave(int debitLeave) {
        this.debitLeave = debitLeave;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getUsedLeave() {
        return usedLeave;
    }

    public void setUsedLeave(double usedLeave) {
        this.usedLeave = usedLeave;
    }

    public double getOpeningLeave() {
        return openingLeave;
    }

    public void setOpeningLeave(int openingLeave) {
        this.openingLeave = openingLeave;
    }
}