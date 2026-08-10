package app.xedigital.ai.model.regularizeList;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Employee implements Serializable {

    // ✅ Existing fields (keep all)
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

    // ❌ MISSING fields — ADD THESE
    @SerializedName("addpayroll")
    private boolean addpayroll;

    @SerializedName("address")
    private String address;         // nullable in payload

    @SerializedName("adharNo")
    private String adharNo;         // nullable in payload

    @SerializedName("components")
    private List<Component> components;

    @SerializedName("crossmanager")
    private String crossmanager;

    @SerializedName("ctc")
    private long ctc;

    @SerializedName("differentlyAbled")
    private String differentlyAbled; // nullable in payload

    @SerializedName("epf")
    private boolean epf;

    @SerializedName("esi")
    private boolean esi;

    @SerializedName("fatherName")
    private String fatherName;      // nullable in payload

    @SerializedName("panNo")
    private String panNo;           // nullable in payload

    @SerializedName("pfAccountNo")
    private String pfAccountNo;     // nullable in payload

    @SerializedName("pincode")
    private String pincode;         // nullable in payload

    @SerializedName("state")
    private String state;           // nullable in payload

    @SerializedName("totalMonthlySalary")
    private double totalMonthlySalary;

    @SerializedName("totalYearlySalary")
    private String totalYearlySalary; // nullable in payload

    @SerializedName("uanno")
    private String uanno;           // nullable in payload

    @SerializedName("bu")
    private String bu;

    // ✅ Existing getters (keep all)
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

    // ✅ New getters for added fields
    public boolean isAddpayroll() {
        return addpayroll;
    }

    public String getAddress() {
        return address;
    }

    public String getAdharNo() {
        return adharNo;
    }

    public List<Component> getComponents() {
        return components;
    }

    public String getCrossmanager() {
        return crossmanager;
    }

    public long getCtc() {
        return ctc;
    }

    public String getDifferentlyAbled() {
        return differentlyAbled;
    }

    public boolean isEpf() {
        return epf;
    }

    public boolean isEsi() {
        return esi;
    }

    public String getFatherName() {
        return fatherName;
    }

    public String getPanNo() {
        return panNo;
    }

    public String getPfAccountNo() {
        return pfAccountNo;
    }

    public String getPincode() {
        return pincode;
    }

    public String getState() {
        return state;
    }

    public double getTotalMonthlySalary() {
        return totalMonthlySalary;
    }

    public String getTotalYearlySalary() {
        return totalYearlySalary;
    }

    public String getUanno() {
        return uanno;
    }

    public String getBu() {
        return bu;
    }
}