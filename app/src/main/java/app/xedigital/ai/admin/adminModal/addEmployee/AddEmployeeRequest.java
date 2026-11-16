package app.xedigital.ai.admin.adminModal.addEmployee;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddEmployeeRequest {

    @SerializedName("joiningType")
    private String joiningType;

    @SerializedName("fatherName")
    private String fatherName;

    @SerializedName("firstname")
    private String firstname;

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

    @SerializedName("bu")
    private String bu;

    @SerializedName("ctc")
    private int ctc;

    @SerializedName("contact")
    private String contact;

    @SerializedName("esiNo")
    private String esiNo;

    @SerializedName("company")
    private String company;

    @SerializedName("isHROrAdmin")
    private String isHROrAdmin;

    @SerializedName("epf")
    private boolean epf;

    @SerializedName("state")
    private String state;

    @SerializedName("department")
    private String department;

    @SerializedName("email")
    private String email;

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

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("employeeType")
    private String employeeType;

    @SerializedName("partner")
    private String partner;

    @SerializedName("grade")
    private String grade;

    @SerializedName("esi")
    private boolean esi;

    @SerializedName("salaryComponents")
    private List<Object> salaryComponents;

    @SerializedName("designation")
    private String designation;

    @SerializedName("crossmanager")
    private String crossmanager;

    @SerializedName("uanno")
    private String uanno;

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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public List<ComponentsItem> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentsItem> components) {
        this.components = components;
    }

    public boolean isIsVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public Object getAddpayroll() {
        return addpayroll;
    }

    public void setAddpayroll(Object addpayroll) {
        this.addpayroll = addpayroll;
    }

    public Object getTotalMonthlySalary() {
        return totalMonthlySalary;
    }

    public void setTotalMonthlySalary(Object totalMonthlySalary) {
        this.totalMonthlySalary = totalMonthlySalary;
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

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu;
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

    public String getEsiNo() {
        return esiNo;
    }

    public void setEsiNo(String esiNo) {
        this.esiNo = esiNo;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getIsHROrAdmin() {
        return isHROrAdmin;
    }

    public void setIsHROrAdmin(String isHROrAdmin) {
        this.isHROrAdmin = isHROrAdmin;
    }

    public boolean isEpf() {
        return epf;
    }

    public void setEpf(boolean epf) {
        this.epf = epf;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Object getTotalYearlySalary() {
        return totalYearlySalary;
    }

    public void setTotalYearlySalary(Object totalYearlySalary) {
        this.totalYearlySalary = totalYearlySalary;
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

    public String getPfAccountNo() {
        return pfAccountNo;
    }

    public void setPfAccountNo(String pfAccountNo) {
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

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
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

    public List<Object> getSalaryComponents() {
        return salaryComponents;
    }

    public void setSalaryComponents(List<Object> salaryComponents) {
        this.salaryComponents = salaryComponents;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getCrossmanager() {
        return crossmanager;
    }

    public void setCrossmanager(String crossmanager) {
        this.crossmanager = crossmanager;
    }

    public String getUanno() {
        return uanno;
    }

    public void setUanno(String uanno) {
        this.uanno = uanno;
    }

    public String getAdharNo() {
        return adharNo;
    }

    public void setAdharNo(String adharNo) {
        this.adharNo = adharNo;
    }
}