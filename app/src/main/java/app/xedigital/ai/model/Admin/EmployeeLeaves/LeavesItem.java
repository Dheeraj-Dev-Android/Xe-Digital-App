package app.xedigital.ai.model.Admin.EmployeeLeaves;

import com.google.gson.annotations.SerializedName;

public class LeavesItem {

    @SerializedName("leavetype")
    private String leavetype;

    @SerializedName("assignDate")
    private String assignDate;

    @SerializedName("creditLeave")
    private int creditLeave;

    @SerializedName("debitLeave")
    private int debitLeave;

    @SerializedName("_id")
    private String id;

    @SerializedName("usedLeave")
    private int usedLeave;

    @SerializedName("openingLeave")
    private int openingLeave;

    public String getLeavetype() {
        return leavetype;
    }

    public String getAssignDate() {
        return assignDate;
    }

    public int getCreditLeave() {
        return creditLeave;
    }

    public int getDebitLeave() {
        return debitLeave;
    }

    public String getId() {
        return id;
    }

    public int getUsedLeave() {
        return usedLeave;
    }

    public int getOpeningLeave() {
        return openingLeave;
    }
}