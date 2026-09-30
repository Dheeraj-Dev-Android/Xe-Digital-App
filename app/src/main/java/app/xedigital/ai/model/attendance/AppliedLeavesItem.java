package app.xedigital.ai.model.attendance;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppliedLeavesItem implements Serializable {

    @SerializedName("leaveName")
    private String leaveName;

    @SerializedName("reason")
    private String reason;

    @SerializedName("empLastName")
    private String empLastName;

    @SerializedName("punchDateFormat")
    private String punchDateFormat;

    @SerializedName("toDate")
    private Object toDate;

    @SerializedName("empFirstName")
    private String empFirstName;

    @SerializedName("empEmail")
    private String empEmail;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("appliedLeaveDateFormat")
    private String appliedLeaveDateFormat;

    @SerializedName("selectTypeFrom")
    private Object selectTypeFrom;

    @SerializedName("punchDate")
    private String punchDate;

    @SerializedName("selectTypeTo")
    private Object selectTypeTo;

    @SerializedName("shortLeaveStartTime")
    private String shortLeaveStartTime;

    @SerializedName("currentDaySelectType")
    private Object currentDaySelectType;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("appliedLeavetoDateFormat")
    private String appliedLeavetoDateFormat;

    @SerializedName("_id")
    private String id;

    @SerializedName("shortLeaveEndTime")
    private String shortLeaveEndTime;

    @SerializedName("status")
    private String status;

    public String getLeaveName() {
        return leaveName;
    }

    public String getReason() {
        return reason;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public String getPunchDateFormat() {
        return punchDateFormat;
    }

    public Object getToDate() {
        return toDate;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public String getAppliedLeaveDateFormat() {
        return appliedLeaveDateFormat;
    }

    public Object getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public Object getSelectTypeTo() {
        return selectTypeTo;
    }

    public String getShortLeaveStartTime() {
        return shortLeaveStartTime;
    }

    public Object getCurrentDaySelectType() {
        return currentDaySelectType;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getAppliedLeavetoDateFormat() {
        return appliedLeavetoDateFormat;
    }

    public String getId() {
        return id;
    }

    public String getShortLeaveEndTime() {
        return shortLeaveEndTime;
    }

    public String getStatus() {
        return status;
    }
}