package app.xedigital.ai.model.shiftUpdate;

import com.google.gson.annotations.SerializedName;

public class ShiftUpdateRequest {

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("shiftType")
    private String shiftType;

    @SerializedName("reportingManager")
    private ReportingManager reportingManager;

    @SerializedName("shift")
    private String shift;

    @SerializedName("reportingManagerName")
    private String reportingManagerName;

    @SerializedName("employee")
    private String employee;

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("employeeCode")
    private String employeeCode;

    @SerializedName("reportingManagerEmail")
    private String reportingManagerEmail;

    @SerializedName("contact")
    private String contact;

    @SerializedName("department")
    private String department;

    @SerializedName("hrEmail")
    private String hrEmail;

    @SerializedName("email")
    private String email;

    @SerializedName("status")
    private String status;
    @SerializedName("comment")
    private String comment;

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setShiftType(String shiftType) {
        this.shiftType = shiftType;
    }

    public void setReportingManager(ReportingManager reportingManager) {
        this.reportingManager = reportingManager;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public void setReportingManagerName(String reportingManagerName) {
        this.reportingManagerName = reportingManagerName;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public void setReportingManagerEmail(String reportingManagerEmail) {
        this.reportingManagerEmail = reportingManagerEmail;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}