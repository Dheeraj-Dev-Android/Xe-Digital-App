package app.xedigital.ai.model.Admin.leaveBalance;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("assignDate")
    private String assignDate;

    @SerializedName("creditLeave")
    private double creditLeave;

    @SerializedName("debitLeave")
    private double debitLeave;

    @SerializedName("_id")
    private String id;

    @SerializedName("usedLeave")
    private double usedLeave;

    @SerializedName("openingLeave")
    private double openingLeave;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getAssignDate() {
        return assignDate;
    }

    public double getCreditLeave() {
        return creditLeave;
    }

    public double getDebitLeave() {
        return debitLeave;
    }

    public String getId() {
        return id;
    }

    public double getUsedLeave() {
        return usedLeave;
    }

    public double getOpeningLeave() {
        return openingLeave;
    }
}