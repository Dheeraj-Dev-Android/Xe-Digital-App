package app.xedigital.ai.admin.adminModal.employeeProfileImage;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Employee implements Serializable {

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
    private boolean addpayroll;

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
    private Partner partner;

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

    public void setJoiningType(String joiningType) {
        this.joiningType = joiningType;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public void setComponents(List<ComponentsItem> components) {
        this.components = components;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public void setAddpayroll(boolean addpayroll) {
        this.addpayroll = addpayroll;
    }

    public void setTotalMonthlySalary(Object totalMonthlySalary) {
        this.totalMonthlySalary = totalMonthlySalary;
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

    public void setCtc(int ctc) {
        this.ctc = ctc;
    }

    public void setBu(String bu) {
        this.bu = bu;
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

    public void setEpf(boolean epf) {
        this.epf = epf;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setState(String state) {
        this.state = state;
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

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public void setReportingManager(ReportingManager reportingManager) {
        this.reportingManager = reportingManager;
    }

    public void setTotalYearlySalary(Object totalYearlySalary) {
        this.totalYearlySalary = totalYearlySalary;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setDifferentlyAbled(String differentlyAbled) {
        this.differentlyAbled = differentlyAbled;
    }

    public void setPfAccountNo(String pfAccountNo) {
        this.pfAccountNo = pfAccountNo;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
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

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setEsi(boolean esi) {
        this.esi = esi;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCrossmanager(Crossmanager crossmanager) {
        this.crossmanager = crossmanager;
    }

    public void setUanno(String uanno) {
        this.uanno = uanno;
    }

    public void setAdharNo(String adharNo) {
        this.adharNo = adharNo;
    }
}