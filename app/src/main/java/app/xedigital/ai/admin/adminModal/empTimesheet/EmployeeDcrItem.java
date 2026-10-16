package app.xedigital.ai.admin.adminModal.empTimesheet;

import com.google.gson.annotations.SerializedName;

public class EmployeeDcrItem {

    @SerializedName("employeeName")
    private String employeeName;

    @SerializedName("dcrDate")
    private String dcrDate;

    @SerializedName("OutTime")
    private String outTime;

    @SerializedName("employeeEmail")
    private String employeeEmail;

    @SerializedName("employee")
    private Object employee;

    @SerializedName("reportingManagerEmail")
    private String reportingManagerEmail;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("employeeLastName")
    private String employeeLastName;

    @SerializedName("InTime")
    private String inTime;

    @SerializedName("Outcome")
    private String outcome;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("__v")
    private int v;

    @SerializedName("reportingManagerFirstName")
    private String reportingManagerFirstName;

    @SerializedName("tommarowPlan")
    private String tommarowPlan;

    @SerializedName("_id")
    private String id;

    @SerializedName("todayReport")
    private String todayReport;

    @SerializedName("hrEmail")
    private String hrEmail;

    @SerializedName("todayFeeling")
    private String todayFeeling;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDcrDate() {
        return dcrDate;
    }

    public String getOutTime() {
        return outTime;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public Object getEmployee() {
        return employee;
    }

    public String getReportingManagerEmail() {
        return reportingManagerEmail;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getEmployeeLastName() {
        return employeeLastName;
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

    public int getV() {
        return v;
    }

    public String getReportingManagerFirstName() {
        return reportingManagerFirstName;
    }

    public String getTommarowPlan() {
        return tommarowPlan;
    }

    public String getId() {
        return id;
    }

    public String getTodayReport() {
        return todayReport;
    }

    public String getHrEmail() {
        return hrEmail;
    }

    public String getTodayFeeling() {
        return todayFeeling;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}