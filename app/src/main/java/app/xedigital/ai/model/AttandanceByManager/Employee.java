package app.xedigital.ai.model.AttandanceByManager;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Employee implements Serializable {

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("joiningType")
    private String joiningType;

    @SerializedName("fatherName")
    private String fatherName;

    @SerializedName("components")
    private List<ComponentsItem> components;

    @SerializedName("isVerified")
    private boolean isVerified;

    @SerializedName("shift")
    private String shift;

    @SerializedName("addpayroll")
    private Object addpayroll;

    @SerializedName("totalMonthlySalary")
    private Object totalMonthlySalary;

    @SerializedName("joiningDate")
    private String joiningDate;

    @SerializedName("employeeCode")
    private String employeeCode;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("ctc")
    private int ctc;

    @SerializedName("contact")
    private String contact;

    @SerializedName("__v")
    private int v;

    @SerializedName("isHROrAdmin")
    private boolean isHROrAdmin;

    @SerializedName("company")
    private String company;

    @SerializedName("epf")
    private boolean epf;

    @SerializedName("state")
    private String state;

    @SerializedName("department")
    private String department;

    @SerializedName("profileImageUrl")
    private Object profileImageUrl;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("pincode")
    private String pincode;

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("address")
    private String address;

    @SerializedName("totalYearlySalary")
    private Object totalYearlySalary;

    @SerializedName("level")
    private String level;

    @SerializedName("differentlyAbled")
    private String differentlyAbled;

    @SerializedName("pfAccountNo")
    private String pfAccountNo;

    @SerializedName("panNo")
    private String panNo;

    @SerializedName("active")
    private boolean active;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("employeeType")
    private String employeeType;

    @SerializedName("partner")
    private String partner;

    @SerializedName("grade")
    private String grade;

    @SerializedName("esi")
    private boolean esi;

    @SerializedName("designation")
    private String designation;

    @SerializedName("fullname")
    private String fullname;

    @SerializedName("crossmanager")
    private String crossmanager;

    @SerializedName("uanno")
    private String uanno;

    @SerializedName("adharNo")
    private String adharNo;

    public String getFirstname() {
        return firstname;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getLastname() {
        return lastname;
    }

    public String getJoiningType() {
        return joiningType;
    }

    public String getFatherName() {
        return fatherName;
    }

    public List<ComponentsItem> getComponents() {
        return components;
    }

    public boolean isIsVerified() {
        return isVerified;
    }

    public String getShift() {
        return shift;
    }

    public Object getAddpayroll() {
        return addpayroll;
    }

    public Object getTotalMonthlySalary() {
        return totalMonthlySalary;
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

    public int getCtc() {
        return ctc;
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

    public boolean isEpf() {
        return epf;
    }

    public String getState() {
        return state;
    }

    public String getDepartment() {
        return department;
    }

    public Object getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getPincode() {
        return pincode;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public String getAddress() {
        return address;
    }

    public Object getTotalYearlySalary() {
        return totalYearlySalary;
    }

    public String getLevel() {
        return level;
    }

    public String getDifferentlyAbled() {
        return differentlyAbled;
    }

    public String getPfAccountNo() {
        return pfAccountNo;
    }

    public String getPanNo() {
        return panNo;
    }

    public boolean isActive() {
        return active;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
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

    public boolean isEsi() {
        return esi;
    }

    public String getDesignation() {
        return designation;
    }

    public String getFullname() {
        return fullname;
    }

    public String getCrossmanager() {
        return crossmanager;
    }

    public String getUanno() {
        return uanno;
    }

    public String getAdharNo() {
        return adharNo;
    }
}