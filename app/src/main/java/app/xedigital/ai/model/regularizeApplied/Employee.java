package app.xedigital.ai.model.regularizeApplied;

import com.google.gson.annotations.SerializedName;

public class Employee {

    @SerializedName("joiningType")
    private String joiningType;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("isVerified")
    private boolean isVerified;

    @SerializedName("shift")
    private String shift;

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
    private String department;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("reportingManager")
    private String reportingManager;

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
    private String partner;

    @SerializedName("grade")
    private String grade;

    @SerializedName("_id")
    private String id;

    @SerializedName("designation")
    private String designation;

    @SerializedName("fullname")
    private String fullname;

    public String getJoiningType() {
        return joiningType;
    }

    public String getFirstname() {
        return firstname;
    }

    public boolean isIsVerified() {
        return isVerified;
    }

    public String getShift() {
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

    public String getDepartment() {
        return department;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getReportingManager() {
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

    public String getPartner() {
        return partner;
    }

    public String getGrade() {
        return grade;
    }

    public String getId() {
        return id;
    }

    public String getDesignation() {
        return designation;
    }

    public String getFullname() {
        return fullname;
    }
}