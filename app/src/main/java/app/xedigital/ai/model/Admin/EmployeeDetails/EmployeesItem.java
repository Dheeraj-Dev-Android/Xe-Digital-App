package app.xedigital.ai.model.Admin.EmployeeDetails;

import com.google.gson.annotations.SerializedName;

public class EmployeesItem {

    @SerializedName("joiningType")
    private String joiningType;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("isVerified")
    private boolean isVerified;

    @SerializedName("shift")
    private Object shift;

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
    private Object department;

    @SerializedName("profileImageUrl")
    private Object profileImageUrl;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("reportingManager")
    private Object reportingManager;

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

    @SerializedName("designation")
    private String designation;

    @SerializedName("_id")
    private String id;

    @SerializedName("crossmanager")
    private String crossmanager;

    public String getJoiningType() {
        return joiningType;
    }

    public String getFirstname() {
        return firstname;
    }

    public boolean isIsVerified() {
        return isVerified;
    }

    public Object getShift() {
        return shift;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getContact() {
        return contact;
    }

    public int getV() {
        return v;
    }

    public boolean isIsHROrAdmin() {
        return isHROrAdmin;
    }

    public String getCompany() {
        return company;
    }

    public Object getDepartment() {
        return department;
    }

    public Object getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Object getReportingManager() {
        return reportingManager;
    }

    public String getLevel() {
        return level;
    }

    public boolean isActive() {
        return active;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public Object getPartner() {
        return partner;
    }

    public String getGrade() {
        return grade;
    }

    public String getDesignation() {
        return designation;
    }

    public String getId() {
        return id;
    }

    public String getCrossmanager() {
        return crossmanager;
    }
}