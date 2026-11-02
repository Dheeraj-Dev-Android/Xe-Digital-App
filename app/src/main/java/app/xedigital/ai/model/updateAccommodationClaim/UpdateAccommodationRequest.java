package app.xedigital.ai.model.updateAccommodationClaim;

import com.google.gson.annotations.SerializedName;

public class UpdateAccommodationRequest {

    @SerializedName("empId")
    private String empId;

    @SerializedName("Accommodationtype")
    private String accommodationtype;

    @SerializedName("statusHr")
    private String statusHr;
    @SerializedName("comment")
    private String comment;

    @SerializedName("docFileURL")
    private String docFileURL;

    @SerializedName("project")
    private String project;

    @SerializedName("perposeofmeet")
    private String perposeofmeet;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("checkin")
    private String checkin;

    @SerializedName("statusRm")
    private String statusRm;

    @SerializedName("currency")
    private String currency;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("state")
    private String state;

    @SerializedName("docFileURLKeyOne")
    private String docFileURLKeyOne;

    @SerializedName("department")
    private Department department;

    @SerializedName("meeting")
    private String meeting;

    @SerializedName("checkout")
    private String checkout;

    @SerializedName("email")
    private String email;

    @SerializedName("expenseType")
    private String expenseType;

    @SerializedName("reqEmployee")
    private ReqEmployee reqEmployee;

    @SerializedName("docFileURLKey")
    private String docFileURLKey;

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("totalamount")
    private double totalamount;

    @SerializedName("travelRefId")
    private String travelRefId;

    @SerializedName("claimDate")
    private String claimDate;

    @SerializedName("location")
    private String location;

    @SerializedName("_id")
    private String id;

    @SerializedName("Id")
    private String claimId;

    @SerializedName("accommodationname")
    private String accommodationname;

    @SerializedName("status")
    private String status;

    @SerializedName("docFileURLOne")
    private String docFileURLOne;

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getAccommodationtype() {
        return accommodationtype;
    }

    public void setAccommodationtype(String accommodationtype) {
        this.accommodationtype = accommodationtype;
    }

    public String getStatusHr() {
        return statusHr;
    }

    public void setStatusHr(String statusHr) {
        this.statusHr = statusHr;
    }

    public String getDocFileURL() {
        return docFileURL;
    }

    public void setDocFileURL(String docFileURL) {
        this.docFileURL = docFileURL;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPerposeofmeet() {
        return perposeofmeet;
    }

    public void setPerposeofmeet(String perposeofmeet) {
        this.perposeofmeet = perposeofmeet;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getStatusRm() {
        return statusRm;
    }

    public void setStatusRm(String statusRm) {
        this.statusRm = statusRm;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDocFileURLKeyOne() {
        return docFileURLKeyOne;
    }

    public void setDocFileURLKeyOne(String docFileURLKeyOne) {
        this.docFileURLKeyOne = docFileURLKeyOne;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getMeeting() {
        return meeting;
    }

    public void setMeeting(String meeting) {
        this.meeting = meeting;
    }

    public String getCheckout() {
        return checkout;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public ReqEmployee getReqEmployee() {
        return reqEmployee;
    }

    public void setReqEmployee(ReqEmployee reqEmployee) {
        this.reqEmployee = reqEmployee;
    }

    public String getDocFileURLKey() {
        return docFileURLKey;
    }

    public void setDocFileURLKey(String docFileURLKey) {
        this.docFileURLKey = docFileURLKey;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(double totalamount) {
        this.totalamount = totalamount;
    }

    public String getTravelRefId() {
        return travelRefId;
    }

    public void setTravelRefId(String travelRefId) {
        this.travelRefId = travelRefId;
    }

    public String getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(String claimDate) {
        this.claimDate = claimDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setclaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getclaimId() {
        return claimId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccommodationname() {
        return accommodationname;
    }

    public void setAccommodationname(String accommodationname) {
        this.accommodationname = accommodationname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocFileURLOne() {
        return docFileURLOne;
    }

    public void setDocFileURLOne(String docFileURLOne) {
        this.docFileURLOne = docFileURLOne;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}