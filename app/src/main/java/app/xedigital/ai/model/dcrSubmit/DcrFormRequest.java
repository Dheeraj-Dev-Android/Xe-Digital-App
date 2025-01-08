package app.xedigital.ai.model.dcrSubmit;

import com.google.gson.annotations.SerializedName;

public class DcrFormRequest {

    @SerializedName("employeeName")
    private String employeeName;

    @SerializedName("dcrDate")
    private String dcrDate;

    @SerializedName("OutTime")
    private String outTime;

    @SerializedName("employeeEmail")
    private String employeeEmail;

    @SerializedName("employee")
    private String employee;

    @SerializedName("reportingManagerEmail")
    private String reportingManagerEmail;

    @SerializedName("employeeLastName")
    private String employeeLastName;

    @SerializedName("InTime")
    private String inTime;

    @SerializedName("Outcome")
    private String outcome;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("reportingManagerFirstName")
    private String reportingManagerFirstName;

    @SerializedName("tommarowPlan")
    private String tommarowPlan;

    @SerializedName("todayReport")
    private String todayReport;

    @SerializedName("hrEmail")
    private String hrEmail;

    @SerializedName("todayFeeling")
    private String todayFeeling;

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public void setDcrDate(String dcrDate) {
        this.dcrDate = dcrDate;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public void setReportingManagerEmail(String reportingManagerEmail) {
        this.reportingManagerEmail = reportingManagerEmail;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public void setReportingManagerLastName(String reportingManagerLastName) {
        this.reportingManagerLastName = reportingManagerLastName;
    }

    public void setReportingManagerFirstName(String reportingManagerFirstName) {
        this.reportingManagerFirstName = reportingManagerFirstName;
    }

    public void setTommarowPlan(String tommarowPlan) {
        this.tommarowPlan = tommarowPlan;
    }

    public void setTodayReport(String todayReport) {
        this.todayReport = todayReport;
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
    }

    public void setTodayFeeling(String todayFeeling) {
        this.todayFeeling = todayFeeling;
    }
}