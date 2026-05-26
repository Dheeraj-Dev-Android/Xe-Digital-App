package app.xedigital.ai.model.TeamLeave;

import com.google.gson.annotations.SerializedName;

public class LeavesItem {

    @SerializedName("employeeName")
    private String employeeName;

    @SerializedName("assignDate")
    private String assignDate;

    @SerializedName("creditLeave")
    private double creditLeave;

    @SerializedName("debitLeave")
    private double debitLeave;

    @SerializedName("employeeId")
    private String employeeId;

    @SerializedName("_id")
    private String id;

    @SerializedName("usedLeave")
    private double usedLeave;

    @SerializedName("email")
    private String email;

    @SerializedName("openingLeave")
    private double openingLeave;

    @SerializedName("leavetype")
    private String leavetype;

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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

    public double getDebitLeave() {
        return debitLeave;
    }

    public void setDebitLeave(int debitLeave) {
        this.debitLeave = debitLeave;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public void setUsedLeave(int usedLeave) {
        this.usedLeave = usedLeave;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getOpeningLeave() {
        return openingLeave;
    }

    public void setOpeningLeave(int openingLeave) {
        this.openingLeave = openingLeave;
    }

    public String getLeavetype() {
        return leavetype;
    }

    public void setLeavetype(String leavetype) {
        this.leavetype = leavetype;
    }
}