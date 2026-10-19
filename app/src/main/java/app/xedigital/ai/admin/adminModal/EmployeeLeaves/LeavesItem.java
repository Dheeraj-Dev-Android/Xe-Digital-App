package app.xedigital.ai.admin.adminModal.EmployeeLeaves;

import com.google.gson.annotations.SerializedName;

public class LeavesItem {

    @SerializedName("leavetype")
    private String leavetype;

    @SerializedName("assignDate")
    private String assignDate;

    @SerializedName("creditLeave")
    private double creditLeave;

    @SerializedName("debitLeave")
    private double debitLeave;

    @SerializedName("appliedLeave")
    private double appliedLeave;

    @SerializedName("_id")
    private String id;

    @SerializedName("usedLeave")
    private double usedLeave;

    @SerializedName("openingLeave")
    private double openingLeave;

    public String getLeavetype() {
        return leavetype;
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

    public double getAppliedLeave() {
        return appliedLeave;
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