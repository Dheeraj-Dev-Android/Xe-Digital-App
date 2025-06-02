package app.xedigital.ai.model.Admin.VisitorManual;

import com.google.gson.annotations.SerializedName;

public class WhomToMeetItem {

    @SerializedName("joiningType")
    private String joiningType;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("isVerified")
    private boolean isVerified;

    @SerializedName("shift")
    private Shift shift;

    @SerializedName("joiningDate")
    private String joiningDate;

    @SerializedName("employeeCode")
    private String employeeCode;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("contact")
    private String contact;

    @SerializedName("__v")
    private int v;

    @SerializedName("isHROrAdmin")
    private boolean isHROrAdmin;

    @SerializedName("company")
    private String company;

    @SerializedName("department")
    private Department department;

    @SerializedName("profileImageUrl")
    private Object profileImageUrl;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("reportingManager")
    private ReportingManager reportingManager;

    @SerializedName("level")
    private String level;

    @SerializedName("active")
    private boolean active;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("employeeType")
    private String employeeType;

    @SerializedName("partner")
    private Object partner;

    @SerializedName("grade")
    private String grade;

    @SerializedName("name")
    private String name;

    @SerializedName("designation")
    private String designation;

    @SerializedName("_id")
    private String id;

    @SerializedName("crossmanager")
    private String crossmanager;

    public void setJoiningType(String joiningType) {
        this.joiningType = joiningType;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setV(int v) {
        this.v = v;
    }

    public void setIsHROrAdmin(boolean isHROrAdmin) {
        this.isHROrAdmin = isHROrAdmin;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setProfileImageUrl(Object profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setReportingManager(ReportingManager reportingManager) {
        this.reportingManager = reportingManager;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public void setPartner(Object partner) {
        this.partner = partner;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCrossmanager(String crossmanager) {
        this.crossmanager = crossmanager;
    }
}