package app.xedigital.ai.model.regularizeUpdateStatus;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Employee {

    @SerializedName("_id")
    private String id;

    @SerializedName("employeeCode")
    private String employeeCode;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("joiningDate")
    private String joiningDate;

    @SerializedName("joiningType")
    private String joiningType;

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    @SerializedName("designation")
    private String designation;

    @SerializedName("active")
    private boolean active;

    @SerializedName("isVerified")
    private boolean isVerified;

    @SerializedName("isHROrAdmin")
    private boolean isHROrAdmin;

    @SerializedName("employeeType")
    private String employeeType;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("email")
    private String email;

    @SerializedName("contact")
    private String contact;

    @SerializedName("company")
    private String company;

    @SerializedName("department")
    private String department;

    @SerializedName("partner")
    private String partner;

    @SerializedName("shift")
    private String shift;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("__v")
    private int v;

    @SerializedName("grade")
    private String grade;

    @SerializedName("level")
    private String level;

    @SerializedName("addpayroll")
    private boolean addpayroll;

    @SerializedName("address")
    private String address;

    @SerializedName("adharNo")
    private String adharNo;

    @SerializedName("components")
    private List<Component> components;

    @SerializedName("crossmanager")
    private String crossmanager;

    @SerializedName("ctc")
    private long ctc;

    @SerializedName("differentlyAbled")
    private String differentlyAbled;

    @SerializedName("epf")
    private boolean epf;

    @SerializedName("esi")
    private boolean esi;

    @SerializedName("fatherName")
    private String fatherName;

    @SerializedName("panNo")
    private String panNo;

    @SerializedName("pfAccountNo")
    private String pfAccountNo;

    @SerializedName("pincode")
    private String pincode;

    @SerializedName("state")
    private String state;

    @SerializedName("totalMonthlySalary")
    private double totalMonthlySalary;

    @SerializedName("totalYearlySalary")
    private String totalYearlySalary;

    @SerializedName("uanno")
    private String uanno;

    @SerializedName("bu")
    private String bu;

    @SerializedName("fullname")
    private String fullname;

    // Getters
    public String getId() {
        return id;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getJoiningType() {
        return joiningType;
    }

    public void setJoiningType(String joiningType) {
        this.joiningType = joiningType;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isIsVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public boolean isIsHROrAdmin() {
        return isHROrAdmin;
    }

    public void setIsHROrAdmin(boolean isHROrAdmin) {
        this.isHROrAdmin = isHROrAdmin;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isAddpayroll() {
        return addpayroll;
    }

    public void setAddpayroll(boolean addpayroll) {
        this.addpayroll = addpayroll;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAdharNo() {
        return adharNo;
    }

    public void setAdharNo(String adharNo) {
        this.adharNo = adharNo;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public String getCrossmanager() {
        return crossmanager;
    }

    public void setCrossmanager(String crossmanager) {
        this.crossmanager = crossmanager;
    }

    public long getCtc() {
        return ctc;
    }

    public void setCtc(long ctc) {
        this.ctc = ctc;
    }

    public String getDifferentlyAbled() {
        return differentlyAbled;
    }

    public void setDifferentlyAbled(String differentlyAbled) {
        this.differentlyAbled = differentlyAbled;
    }

    public boolean isEpf() {
        return epf;
    }

    public void setEpf(boolean epf) {
        this.epf = epf;
    }

    public boolean isEsi() {
        return esi;
    }

    public void setEsi(boolean esi) {
        this.esi = esi;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getPanNo() {
        return panNo;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public String getPfAccountNo() {
        return pfAccountNo;
    }

    public void setPfAccountNo(String pfAccountNo) {
        this.pfAccountNo = pfAccountNo;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getTotalMonthlySalary() {
        return totalMonthlySalary;
    }

    public void setTotalMonthlySalary(double totalMonthlySalary) {
        this.totalMonthlySalary = totalMonthlySalary;
    }

    public String getTotalYearlySalary() {
        return totalYearlySalary;
    }

    public void setTotalYearlySalary(String totalYearlySalary) {
        this.totalYearlySalary = totalYearlySalary;
    }

    public String getUanno() {
        return uanno;
    }

    public void setUanno(String uanno) {
        this.uanno = uanno;
    }

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}