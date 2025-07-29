package app.xedigital.ai.model.Admin.EmployeeDetails;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class EmployeesItem implements Serializable {

    @SerializedName("joiningType")
    private String joiningType;

    @SerializedName("fatherName")
    private String fatherName;

    @SerializedName("components")
    private List<ComponentsItem> components;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("isVerified")
    private boolean isVerified;

    @SerializedName("addpayroll")
    private Object addpayroll;

    @SerializedName("totalMonthlySalary")
    private Object totalMonthlySalary;

    @SerializedName("shift")
    private Shift shift;

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

    @SerializedName("epf")
    private boolean epf;

    @SerializedName("company")
    private String company;

    @SerializedName("state")
    private String state;

    @SerializedName("department")
    private Department department;

    @SerializedName("profileImageUrl")
    private Object profileImageUrl;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("pincode")
    private String pincode;

    @SerializedName("reportingManager")
    private ReportingManager reportingManager;

    @SerializedName("totalYearlySalary")
    private Object totalYearlySalary;

    @SerializedName("address")
    private String address;

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

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("employeeType")
    private String employeeType;

    @SerializedName("partner")
    private Object partner;

    @SerializedName("grade")
    private String grade;

    @SerializedName("esi")
    private boolean esi;

    @SerializedName("designation")
    private String designation;

    @SerializedName("_id")
    private String id;

    @SerializedName("crossmanager")
    private Crossmanager crossmanager;

    @SerializedName("uanno")
    private String uanno;

    @SerializedName("adharNo")
    private String adharNo;

    public String getJoiningType() {
        return joiningType;
    }

    public String getFatherName() {
        return fatherName;
    }

    public List<ComponentsItem> getComponents() {
        return components;
    }

    public String getFirstname() {
        return firstname;
    }

    public boolean isIsVerified() {
        return isVerified;
    }

    public Object getAddpayroll() {
        return addpayroll;
    }

    public Object getTotalMonthlySalary() {
        return totalMonthlySalary;
    }

    public Shift getShift() {
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

    public boolean isEpf() {
        return epf;
    }

    public String getCompany() {
        return company;
    }

    public String getState() {
        return state;
    }

    public Department getDepartment() {
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

    public String getPincode() {
        return pincode;
    }

    public ReportingManager getReportingManager() {
        return reportingManager;
    }

    public Object getTotalYearlySalary() {
        return totalYearlySalary;
    }

    public String getAddress() {
        return address;
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

    public boolean isEsi() {
        return esi;
    }

    public String getDesignation() {
        return designation;
    }

    public String getId() {
        return id;
    }

    public Crossmanager getCrossmanager() {
        return crossmanager;
    }

    public String getUanno() {
        return uanno;
    }

    public String getAdharNo() {
        return adharNo;
    }
}