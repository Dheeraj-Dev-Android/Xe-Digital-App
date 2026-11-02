package app.xedigital.ai.model.updateFoodClaim;

import com.google.gson.annotations.SerializedName;

public class UpdateFoodRequest {

    @SerializedName("empId")
    private String empId;

    @SerializedName("distance")
    private Integer distance;

    @SerializedName("statusHr")
    private String statusHr;

    @SerializedName("description")
    private String description;
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

    @SerializedName("statusRm")
    private String statusRm;

    @SerializedName("currency")
    private String currency;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("docFileURLKeyOne")
    private String docFileURLKeyOne;

    @SerializedName("department")
    private Department department;

    @SerializedName("meeting")
    private String meeting;

    @SerializedName("email")
    private String email;

    @SerializedName("expenseType")
    private String expenseType;

    @SerializedName("restaurant")
    private String restaurant;

    @SerializedName("reqEmployee")
    private ReqEmployee reqEmployee;

    @SerializedName("docFileURLKey")
    private String docFileURLKey;

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("persons")
    private int persons;

    @SerializedName("totalamount")
    private double totalamount;

    @SerializedName("claimDate")
    private String claimDate;

    @SerializedName("travelRefId")
    private String travelRefId;

    @SerializedName("_id")
    private String id;

    @SerializedName("Id")
    private String claimId;

    @SerializedName("status")
    private String status;

    @SerializedName("meantype")
    private String meantype;

    @SerializedName("docFileURLOne")
    private String docFileURLOne;

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public String getStatusHr() {
        return statusHr;
    }

    public void setStatusHr(String statusHr) {
        this.statusHr = statusHr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
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

    public int getPersons() {
        return persons;
    }

    public void setPersons(int persons) {
        this.persons = persons;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(double totalamount) {
        this.totalamount = totalamount;
    }

    public String getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(String claimDate) {
        this.claimDate = claimDate;
    }

    public String getTravelRefId() {
        return travelRefId;
    }

    public void setTravelRefId(String travelRefId) {
        this.travelRefId = travelRefId;
    }

    public void setclaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getClaimId() {
        return claimId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMeantype() {
        return meantype;
    }

    public void setMeantype(String meantype) {
        this.meantype = meantype;
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