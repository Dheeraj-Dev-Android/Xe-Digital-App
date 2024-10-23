package app.xedigital.ai.model.dcrData;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EmployeesDcrDataItem implements Serializable {

    @SerializedName("dcrDate")
    private String dcrDate;

    @SerializedName("OutTime")
    private String outTime;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("reportingManagerEmail")
    private String reportingManagerEmail;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("dcrDateFormat")
    private String dcrDateFormat;

    @SerializedName("InTime")
    private String inTime;

    @SerializedName("Outcome")
    private String outcome;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("reportingManagerFirstName")
    private String reportingManagerFirstName;

    @SerializedName("_id")
    private String id;

    @SerializedName("tommarowPlan")
    private String tommarowPlan;

    @SerializedName("todayReport")
    private String todayReport;

    @SerializedName("todayFeeling")
    private String todayFeeling;

    public String getDcrDate() {
        return dcrDate;
    }

    public String getOutTime() {
        return outTime;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getReportingManagerEmail() {
        return reportingManagerEmail;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDcrDateFormat() {
        return dcrDateFormat;
    }

    public String getInTime() {
        return inTime;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getReportingManagerLastName() {
        return reportingManagerLastName;
    }

    public String getReportingManagerFirstName() {
        return reportingManagerFirstName;
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

    public String getTodayFeeling() {
        return todayFeeling;
    }
}