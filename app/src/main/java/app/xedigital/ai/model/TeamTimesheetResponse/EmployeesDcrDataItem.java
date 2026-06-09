package app.xedigital.ai.model.TeamTimesheetResponse;

import com.google.gson.annotations.SerializedName;

public class EmployeesDcrDataItem {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("dcrDateFormat")
    private String dcrDateFormat;

    @SerializedName("dcrDate")
    private String dcrDate;

    @SerializedName("InTime")
    private String inTime;

    @SerializedName("OutTime")
    private String outTime;

    @SerializedName("Outcome")
    private String outcome;

    @SerializedName("shift")
    private Shift shift;

    @SerializedName("_id")
    private String id;

    @SerializedName("tommarowPlan")
    private String tommarowPlan;

    @SerializedName("todayReport")
    private String todayReport;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("todayFeeling")
    private String todayFeeling;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDcrDateFormat() {
        return dcrDateFormat;
    }

    public String getDcrDate() {
        return dcrDate;
    }

    public String getInTime() {
        return inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public String getOutcome() {
        return outcome;
    }

    public Shift getShift() {
        return shift;
    }

    public String getId() {
        return id;
    }

    public String getTommarowPlan() {
        return tommarowPlan;
    }

    public String getTodayReport() {
        return todayReport;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getTodayFeeling() {
        return todayFeeling;
    }
}