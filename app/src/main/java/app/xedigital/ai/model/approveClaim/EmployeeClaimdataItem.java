package app.xedigital.ai.model.approveClaim;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EmployeeClaimdataItem implements Serializable {

    @SerializedName("distance")
    private Integer distance;

    @SerializedName("statusHr")
    private String statusHr;

    @SerializedName("docFileURL")
    private String docFileURL;

    @SerializedName("project")
    private String project;

    @SerializedName("perposeofmeet")
    private String perposeofmeet;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("docFileURLKey")
    private String docFileURLKey;

    @SerializedName("totalamount")
    private double totalamount;

    @SerializedName("statusRm")
    private String statusRm;

    @SerializedName("claimDate")
    private String claimDate;

    @SerializedName("currency")
    private String currency;

    @SerializedName("_id")
    private String id;

    @SerializedName("docFileURLKeyOne")
    private String docFileURLKeyOne;

    @SerializedName("department")
    private Department department;

    @SerializedName("meeting")
    private String meeting;

    @SerializedName("email")
    private String email;

    @SerializedName("docFileURLOne")
    private String docFileURLOne;

    @SerializedName("empId")
    private String empId;

    @SerializedName("Accommodationtype")
    private String accommodationtype;

    @SerializedName("checkin")
    private String checkin;

    @SerializedName("state")
    private String state;

    @SerializedName("checkout")
    private String checkout;

    @SerializedName("expenseType")
    private String expenseType;

    @SerializedName("travelRefId")
    private String travelRefId;

    @SerializedName("location")
    private String location;

    @SerializedName("Id")
    private String claimId;

    @SerializedName("accommodationname")
    private String accommodationname;

    @SerializedName("status")
    private String status;

    @SerializedName("description")
    private String description;

    @SerializedName("restaurant")
    private String restaurant;

    @SerializedName("reqEmployee")
    private ReqEmployee reqEmployee;

    @SerializedName("persons")
    private int persons;

    @SerializedName("meantype")
    private String meantype;

    @SerializedName("shared")
    private String shared;

    @SerializedName("empTransport")
    private String empTransport;

    @SerializedName("dedicated")
    private String dedicated;

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("toaddress")
    private String toaddress;

    @SerializedName("modeoftransport")
    private String modeoftransport;

    @SerializedName("individually")
    private String individually;

    @SerializedName("comment")
    private String comment;

    @SerializedName("fromaddress")
    private String fromaddress;

    @SerializedName("travelCategory")
    private String travelCategory;

    @SerializedName("fueltype")
    private String fueltype;

    @SerializedName("fuelstationname")
    private String fuelstationname;

    @SerializedName("fuelquantity")
    private String fuelquantity;

    @SerializedName("duration")
    private String duration;

    @SerializedName("parkingdate")
    private String parkingdate;

    @SerializedName("vehiclenumber")
    private String vehiclenumber;

    @SerializedName("tollvehiclenumber")
    private String tollvehiclenumber;

    @SerializedName("tolllocation")
    private String tolllocation;

    @SerializedName("billingperiod")
    private String billingperiod;

    @SerializedName("netprovider")
    private String netprovider;

    @SerializedName("expancecategory")
    private String expancecategory;

    @SerializedName("billnumber")
    private String billnumber;

    @SerializedName("parkinglocation")
    private String parkinglocation;

    @SerializedName("tollplazaname")
    private String tollplazaname;

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

    public String getDocFileURLKey() {
        return docFileURLKey;
    }

    public void setDocFileURLKey(String docFileURLKey) {
        this.docFileURLKey = docFileURLKey;
    }

    public Double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(Double totalamount) {
        this.totalamount = totalamount;
    }

    public String getStatusRm() {
        return statusRm;
    }

    public void setStatusRm(String statusRm) {
        this.statusRm = statusRm;
    }

    public String getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(String claimDate) {
        this.claimDate = claimDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDocFileURLOne() {
        return docFileURLOne;
    }

    public void setDocFileURLOne(String docFileURLOne) {
        this.docFileURLOne = docFileURLOne;
    }

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

    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCheckout() {
        return checkout;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public String getTravelRefId() {
        return travelRefId;
    }

    public void setTravelRefId(String travelRefId) {
        this.travelRefId = travelRefId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getPersons() {
        return persons;
    }

    public void setPersons(Integer persons) {
        this.persons = persons;
    }

    public String getMeantype() {
        return meantype;
    }

    public void setMeantype(String meantype) {
        this.meantype = meantype;
    }

    public String getShared() {
        return shared;
    }

    public void setShared(String shared) {
        this.shared = shared;
    }

    public String getEmpTransport() {
        return empTransport;
    }

    public void setEmpTransport(String empTransport) {
        this.empTransport = empTransport;
    }

    public String getDedicated() {
        return dedicated;
    }

    public void setDedicated(String dedicated) {
        this.dedicated = dedicated;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getToaddress() {
        return toaddress;
    }

    public void setToaddress(String toaddress) {
        this.toaddress = toaddress;
    }

    public String getModeoftransport() {
        return modeoftransport;
    }

    public void setModeoftransport(String modeoftransport) {
        this.modeoftransport = modeoftransport;
    }

    public String getIndividually() {
        return individually;
    }

    public void setIndividually(String individually) {
        this.individually = individually;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFromaddress() {
        return fromaddress;
    }

    public void setFromaddress(String fromaddress) {
        this.fromaddress = fromaddress;
    }

    public String getTravelCategory() {
        return travelCategory;
    }

    public void setTravelCategory(String travelCategory) {
        this.travelCategory = travelCategory;
    }

    public String getFueltype() {
        return fueltype;
    }

    public void setFueltype(String fueltype) {
        this.fueltype = fueltype;
    }

    public String getFuelstationname() {
        return fuelstationname;
    }

    public void setFuelstationname(String fuelstationname) {
        this.fuelstationname = fuelstationname;
    }

    public String getFuelquantity() {
        return fuelquantity;
    }

    public void setFuelquantity(String fuelquantity) {
        this.fuelquantity = fuelquantity;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getParkingdate() {
        return parkingdate;
    }

    public void setParkingdate(String parkingdate) {
        this.parkingdate = parkingdate;
    }

    public String getVehiclenumber() {
        return vehiclenumber;
    }

    public void setVehiclenumber(String vehiclenumber) {
        this.vehiclenumber = vehiclenumber;
    }

    public String getTollvehiclenumber() {
        return tollvehiclenumber;
    }

    public void setTollvehiclenumber(String tollvehiclenumber) {
        this.tollvehiclenumber = tollvehiclenumber;
    }

    public String getTolllocation() {
        return tolllocation;
    }

    public void setTolllocation(String tolllocation) {
        this.tolllocation = tolllocation;
    }

    public String getBillingperiod() {
        return billingperiod;
    }

    public void setBillingperiod(String billingperiod) {
        this.billingperiod = billingperiod;
    }

    public String getNetprovider() {
        return netprovider;
    }

    public void setNetprovider(String netprovider) {
        this.netprovider = netprovider;
    }

    public String getExpancecategory() {
        return expancecategory;
    }

    public void setExpancecategory(String expancecategory) {
        this.expancecategory = expancecategory;
    }

    public String getBillnumber() {
        return billnumber;
    }

    public void setBillnumber(String billnumber) {
        this.billnumber = billnumber;
    }

    public String getParkinglocation() {
        return parkinglocation;
    }

    public void setParkinglocation(String parkinglocation) {
        this.parkinglocation = parkinglocation;
    }

    public String getTollplazaname() {
        return tollplazaname;
    }

    public void setTollplazaname(String tollplazaname) {
        this.tollplazaname = tollplazaname;
    }
}