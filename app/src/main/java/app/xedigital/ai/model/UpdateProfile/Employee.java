package app.xedigital.ai.model.UpdateProfile;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Employee {

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
    private Object pfAccountNo;

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
    private Object uanno;

    @SerializedName("adharNo")
    private String adharNo;

    public String getJoiningType() {
        return joiningType;
    }

    public void setJoiningType(String joiningType) {
        this.joiningType = joiningType;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public List<ComponentsItem> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentsItem> components) {
        this.components = components;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public boolean isIsVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public boolean isAddpayroll() {
        return addpayroll;
    }

    public void setAddpayroll(boolean addpayroll) {
        this.addpayroll = addpayroll;
    }

    public Object getTotalMonthlySalary() {
        return totalMonthlySalary;
    }

    public void setTotalMonthlySalary(Object totalMonthlySalary) {
        this.totalMonthlySalary = totalMonthlySalary;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getCtc() {
        return ctc;
    }

    public void setCtc(int ctc) {
        this.ctc = ctc;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public boolean isIsHROrAdmin() {
        return isHROrAdmin;
    }

    public void setIsHROrAdmin(boolean isHROrAdmin) {
        this.isHROrAdmin = isHROrAdmin;
    }

    public boolean isEpf() {
        return epf;
    }

    public void setEpf(boolean epf) {
        this.epf = epf;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Object getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(Object profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public ReportingManager getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(ReportingManager reportingManager) {
        this.reportingManager = reportingManager;
    }

    public Object getTotalYearlySalary() {
        return totalYearlySalary;
    }

    public void setTotalYearlySalary(Object totalYearlySalary) {
        this.totalYearlySalary = totalYearlySalary;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDifferentlyAbled() {
        return differentlyAbled;
    }

    public void setDifferentlyAbled(String differentlyAbled) {
        this.differentlyAbled = differentlyAbled;
    }

    public Object getPfAccountNo() {
        return pfAccountNo;
    }

    public void setPfAccountNo(Object pfAccountNo) {
        this.pfAccountNo = pfAccountNo;
    }

    public String getPanNo() {
        return panNo;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public Object getPartner() {
        return partner;
    }

    public void setPartner(Object partner) {
        this.partner = partner;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public boolean isEsi() {
        return esi;
    }

    public void setEsi(boolean esi) {
        this.esi = esi;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Crossmanager getCrossmanager() {
        return crossmanager;
    }

    public void setCrossmanager(Crossmanager crossmanager) {
        this.crossmanager = crossmanager;
    }

    public Object getUanno() {
        return uanno;
    }

    public void setUanno(Object uanno) {
        this.uanno = uanno;
    }

    public String getAdharNo() {
        return adharNo;
    }

    public void setAdharNo(String adharNo) {
        this.adharNo = adharNo;
    }
}