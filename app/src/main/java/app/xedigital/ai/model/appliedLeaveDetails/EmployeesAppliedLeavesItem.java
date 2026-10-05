package app.xedigital.ai.model.appliedLeaveDetails;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EmployeesAppliedLeavesItem implements Serializable {

    @SerializedName("leaveName")
    private String leaveName;

    @SerializedName("reason")
    private String reason;

    @SerializedName("empLastName")
    private String empLastName;

    @SerializedName("toDate")
    private Object toDate;

    @SerializedName("empFirstName")
    private String empFirstName;

    @SerializedName("leavePlanned")
    private String leavePlanned;

    @SerializedName("empEmail")
    private String empEmail;

    @SerializedName("reportingManagerName")
    private String reportingManagerName;

    @SerializedName("selectTypeFrom")
    private Object selectTypeFrom;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("selectTypeTo")
    private Object selectTypeTo;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("vacationAddress")
    private String vacationAddress;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("contactNumber")
    private String contactNumber;

    @SerializedName("comment")
    private String comment;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("_id")
    private String id;

    @SerializedName("status")
    private String status;

    @SerializedName("shortLeaveStartTime")
    private String shortLeaveStartTime;

    @SerializedName("shortLeaveEndTime")
    private String shortLeaveEndTime;

    public String getLeaveName() {
        return leaveName;
    }

    public String getReason() {
        return reason;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public Object getToDate() {
        return toDate;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public String getLeavePlanned() {
        return leavePlanned;
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public String getReportingManagerName() {
        return reportingManagerName;
    }

    public Object getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Object getSelectTypeTo() {
        return selectTypeTo;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getVacationAddress() {
        return vacationAddress;
    }

    public String getReportingManagerLastName() {
        return reportingManagerLastName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getComment() {
        return comment;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getShortLeaveStartTime() {
        return shortLeaveStartTime;
    }

    public String getShortLeaveEndTime() {
        return shortLeaveEndTime;
    }
}