package app.xedigital.ai.model.employeeClaim;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EmployeeClaimdataItem implements Serializable {

    @SerializedName("distance")
    private Double distance;

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

    @SerializedName("expenseType")
    private String expenseType;

    @SerializedName("tolllocation")
    private String tolllocation;

    @SerializedName("billingperiod")
    private String billingperiod;

    @SerializedName("netprovider")
    private String netprovider;

    @SerializedName("reqEmployee")
    private ReqEmployee reqEmployee;

    @SerializedName("expancecategory")
    private String expancecategory;

    @SerializedName("travelRefId")
    private String travelRefId;

    @SerializedName("billnumber")
    private String billnumber;

    @SerializedName("parkinglocation")
    private String parkinglocation;

    @SerializedName("Id")
    private String ClaimId;

    @SerializedName("tollplazaname")
    private String tollplazaname;

    @SerializedName("status")
    private String status;

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

    @SerializedName("Accommodationtype")
    private String accommodationtype;

    @SerializedName("checkin")
    private String checkin;

    @SerializedName("state")
    private String state;

    @SerializedName("checkout")
    private String checkout;

    @SerializedName("location")
    private String location;

    @SerializedName("accommodationname")
    private String accommodationname;

    @SerializedName("description")
    private String description;

    @SerializedName("restaurant")
    private String restaurant;

    @SerializedName("persons")
    private Integer persons;

    @SerializedName("meantype")
    private String meantype;

    public Double getDistance() {
        return distance;
    }

    public String getStatusHr() {
        return statusHr;
    }

    public String getDocFileURL() {
        return docFileURL;
    }

    public String getProject() {
        return project;
    }

    public String getPerposeofmeet() {
        return perposeofmeet;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getDocFileURLKey() {
        return docFileURLKey;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public String getStatusRm() {
        return statusRm;
    }

    public String getClaimDate() {
        return claimDate;
    }

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public String getDocFileURLKeyOne() {
        return docFileURLKeyOne;
    }

    public Department getDepartment() {
        return department;
    }

    public String getMeeting() {
        return meeting;
    }

    public String getEmail() {
        return email;
    }

    public String getDocFileURLOne() {
        return docFileURLOne;
    }

    public String getEmpId() {
        return empId;
    }

    public String getFueltype() {
        return fueltype;
    }

    public String getFuelstationname() {
        return fuelstationname;
    }

    public String getFuelquantity() {
        return fuelquantity;
    }

    public String getDuration() {
        return duration;
    }

    public String getParkingdate() {
        return parkingdate;
    }

    public String getVehiclenumber() {
        return vehiclenumber;
    }

    public String getTollvehiclenumber() {
        return tollvehiclenumber;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public String getTolllocation() {
        return tolllocation;
    }

    public String getBillingperiod() {
        return billingperiod;
    }

    public String getNetprovider() {
        return netprovider;
    }

    public ReqEmployee getReqEmployee() {
        return reqEmployee;
    }

    public String getExpancecategory() {
        return expancecategory;
    }

    public String getTravelRefId() {
        return travelRefId;
    }

    public String getBillnumber() {
        return billnumber;
    }

    public String getParkinglocation() {
        return parkinglocation;
    }

    public String getClaimId() {
        return ClaimId;
    }

    public String getTollplazaname() {
        return tollplazaname;
    }

    public String getStatus() {
        return status;
    }

    public String getShared() {
        return shared;
    }

    public String getEmpTransport() {
        return empTransport;
    }

    public String getDedicated() {
        return dedicated;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public String getToaddress() {
        return toaddress;
    }

    public String getModeoftransport() {
        return modeoftransport;
    }

    public String getIndividually() {
        return individually;
    }

    public String getComment() {
        return comment;
    }

    public String getFromaddress() {
        return fromaddress;
    }

    public String getTravelCategory() {
        return travelCategory;
    }

    public String getAccommodationtype() {
        return accommodationtype;
    }

    public String getCheckin() {
        return checkin;
    }

    public String getState() {
        return state;
    }

    public String getCheckout() {
        return checkout;
    }

    public String getLocation() {
        return location;
    }

    public String getAccommodationname() {
        return accommodationname;
    }

    public String getDescription() {
        return description;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public Integer getPersons() {
        return persons;
    }

    public String getMeantype() {
        return meantype;
    }
}