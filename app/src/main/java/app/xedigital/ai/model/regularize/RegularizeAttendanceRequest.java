package app.xedigital.ai.model.regularize;

import com.google.gson.annotations.SerializedName;

public class RegularizeAttendanceRequest {

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("attendenceRegularizationRemark")
    private String attendenceRegularizationRemark;

    @SerializedName("employeeEmail")
    private String employeeEmail;

    @SerializedName("employeeFirstName")
    private String employeeFirstName;

    @SerializedName("employeeId")
    private String employeeId;

    @SerializedName("punchInTime")
    private String punchInTime;

    @SerializedName("employee")
    private String employee;

    @SerializedName("punchDate")
    private String punchDate;

    @SerializedName("punchIn")
    private String punchIn;

    @SerializedName("punchOutUpdate")
    private String punchOutUpdate;

    @SerializedName("reportingManagerEmail")
    private String reportingManagerEmail;

    @SerializedName("employeeLastName")
    private String employeeLastName;

    @SerializedName("punchOut")
    private String punchOut;

    @SerializedName("empDesignation")
    private String empDesignation;

    @SerializedName("punchOutTime")
    private String punchOutTime;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("punchInUpdate")
    private String punchInUpdate;

    @SerializedName("empDepartment")
    private String empDepartment;

    @SerializedName("punchOutAddress")
    private String punchOutAddress;

    @SerializedName("reportingManagerFirstName")
    private String reportingManagerFirstName;

    @SerializedName("punchInAddress")
    private String punchInAddress;

    @SerializedName("hrEmail")
    private String hrEmail;

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public void setAttendenceRegularizationRemark(String attendenceRegularizationRemark) {
        this.attendenceRegularizationRemark = attendenceRegularizationRemark;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setPunchInTime(String punchInTime) {
        this.punchInTime = punchInTime;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public void setPunchDate(String punchDate) {
        this.punchDate = punchDate;
    }

    public void setPunchIn(String punchIn) {
        this.punchIn = punchIn;
    }

    public void setPunchOutUpdate(String punchOutUpdate) {
        this.punchOutUpdate = punchOutUpdate;
    }

    public void setReportingManagerEmail(String reportingManagerEmail) {
        this.reportingManagerEmail = reportingManagerEmail;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public void setPunchOut(String punchOut) {
        this.punchOut = punchOut;
    }

    public void setEmpDesignation(String empDesignation) {
        this.empDesignation = empDesignation;
    }

    public void setPunchOutTime(String punchOutTime) {
        this.punchOutTime = punchOutTime;
    }

    public void setReportingManagerLastName(String reportingManagerLastName) {
        this.reportingManagerLastName = reportingManagerLastName;
    }

    public void setPunchInUpdate(String punchInUpdate) {
        this.punchInUpdate = punchInUpdate;
    }

    public void setEmpDepartment(String empDepartment) {
        this.empDepartment = empDepartment;
    }

    public void setPunchOutAddress(String punchOutAddress) {
        this.punchOutAddress = punchOutAddress;
    }

    public void setReportingManagerFirstName(String reportingManagerFirstName) {
        this.reportingManagerFirstName = reportingManagerFirstName;
    }

    public void setPunchInAddress(String punchInAddress) {
        this.punchInAddress = punchInAddress;
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
    }
}