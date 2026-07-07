package app.xedigital.ai.model.profile;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Employee {

    @SerializedName("joiningType")
    private String joiningType;

    @SerializedName("fatherName")
    private Object fatherName;

    @SerializedName("components")
    private List<ComponentsItem> components;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("isVerified")
    private boolean isVerified;

    @SerializedName("addpayroll")
    private boolean addpayroll;

    @SerializedName("totalMonthlySalary")
    private int totalMonthlySalary;

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

    @SerializedName("bu")
    private String bu;

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
    private Object state;

    @SerializedName("department")
    private Department department;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("pincode")
    private Object pincode;

    @SerializedName("reportingManager")
    private ReportingManager reportingManager;

    @SerializedName("totalYearlySalary")
    private Object totalYearlySalary;

    @SerializedName("address")
    private Object address;

    @SerializedName("level")
    private String level;

    @SerializedName("differentlyAbled")
    private Object differentlyAbled;

    @SerializedName("pfAccountNo")
    private Object pfAccountNo;

    @SerializedName("panNo")
    private Object panNo;

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
    private Object uanno;

    @SerializedName("adharNo")
    private Object adharNo;

    public String getJoiningType() {
        return joiningType;
    }

    public Object getFatherName() {
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

    public boolean isAddpayroll() {
        return addpayroll;
    }

    public int getTotalMonthlySalary() {
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

    public String getBu() {
        return bu;
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

    public Object getState() {
        return state;
    }

    public Department getDepartment() {
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

    public Object getPincode() {
        return pincode;
    }

    public ReportingManager getReportingManager() {
        return reportingManager;
    }

    public Object getTotalYearlySalary() {
        return totalYearlySalary;
    }

    public Object getAddress() {
        return address;
    }

    public String getLevel() {
        return level;
    }

    public Object getDifferentlyAbled() {
        return differentlyAbled;
    }

    public Object getPfAccountNo() {
        return pfAccountNo;
    }

    public Object getPanNo() {
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

    public Object getUanno() {
        return uanno;
    }

    public Object getAdharNo() {
        return adharNo;
    }
}