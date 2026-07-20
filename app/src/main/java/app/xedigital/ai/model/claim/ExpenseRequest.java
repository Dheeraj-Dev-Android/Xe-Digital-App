package app.xedigital.ai.model.claim;

import com.google.gson.annotations.SerializedName;

public class ExpenseRequest {

    // --- Core Employee & Profile Metadata ---
    @SerializedName("employee")
    private String employee;
    @SerializedName("empId")
    private String empId;
    @SerializedName("employeeCode")
    private String employeeCode;
    @SerializedName("firstname")
    private String firstname;
    @SerializedName("lastname")
    private String lastname;
    @SerializedName("hrEmail")
    private String hrEmail;
    @SerializedName("email")
    private String email;
    @SerializedName("designation")
    private String designation;
    @SerializedName("grade")
    private String grade;
    @SerializedName("reportingManager")
    private Manager reportingManager;
    @SerializedName("reportingManagerEmail")
    private String reportingManagerEmail;
    @SerializedName("reportingManagerName")
    private String reportingManagerName;
    @SerializedName("crossmanager")
    private Manager crossmanager;
    @SerializedName("department")
    private Department department;
    @SerializedName("bu")
    private String bu;
    @SerializedName("buEmail")
    private String buEmail;
    @SerializedName("buName")
    private String buName;
    @SerializedName("requestedfirstname")
    private String requestedfirstname;
    @SerializedName("requestedlastname")
    private String requestedlastname;
    @SerializedName("reqDepartment")
    private Object reqDepartment; // keeping object or null
    @SerializedName("status")
    private String status;
    @SerializedName("request")
    private String request;

    // --- Base Expense / Claim Parameters ---
    @SerializedName("claimDate")
    private String claimDate;
    @SerializedName("modeofcal")
    private String modeofcal;
    @SerializedName("Modeofcal")
    private String modeofcalPascal; // Matches "Modeofcal": ""
    @SerializedName("project")
    private String project;
    @SerializedName("meeting")
    private String meeting;
    @SerializedName("perposeofmeet")
    private String perposeofmeet;
    @SerializedName("totalClaim")
    private Double totalClaim;
    @SerializedName("currency")
    private String currency;
    @SerializedName("confbutton")
    private Boolean confbutton;
    @SerializedName("totalamount")
    private String totalamount;
    @SerializedName("remark")
    private String remark;
    @SerializedName("expenseType")
    private String expenseType;

    // --- Food Details ---
    @SerializedName("guest")
    private Boolean guest;
    @SerializedName("meantype")
    private String meantype;
    @SerializedName("restaurant")
    private String restaurant;
    @SerializedName("persons")
    private String persons;
    @SerializedName("description")
    private String description;

    // --- Travel Details ---
    @SerializedName("fromaddress")
    private String fromaddress;
    @SerializedName("toaddress")
    private String toaddress;
    @SerializedName("distance")
    private String distance;
    @SerializedName("varients")
    private String varients;
    @SerializedName("modeoftransport")
    private String modeoftransport;
    @SerializedName("travelCategory")
    private String travelCategory;
    @SerializedName("shared")
    private String shared;
    @SerializedName("dedicated")
    private String dedicated;
    @SerializedName("individually")
    private String individually;
    @SerializedName("empTransport")
    private String empTransport;
    @SerializedName("travelRefId")
    private String travelRefId;

    // --- Accommodation Details ---
    @SerializedName("Accommodationtype")
    private String accommodationtype;
    @SerializedName("accommodationname")
    private String accommodationname;
    @SerializedName("location")
    private String location;
    @SerializedName("state")
    private String state;
    @SerializedName("checkin")
    private String checkin;
    @SerializedName("checkout")
    private String checkout;
    @SerializedName("purposeofstay")
    private String purposeofstay;

    // --- Miscellaneous / Utility Details ---
    @SerializedName("expancecategory")
    private String expancecategory;
    @SerializedName("billingperiod")
    private String billingperiod;
    @SerializedName("netprovider")
    private String netprovider;
    @SerializedName("billnumber")
    private String billnumber;
    @SerializedName("vehiclenumber")
    private String vehiclenumber;
    @SerializedName("fueltype")
    private String fueltype;
    @SerializedName("fuelquantity")
    private String fuelquantity;
    @SerializedName("fuelstationname")
    private String fuelstationname;
    @SerializedName("parkinglocation")
    private String parkinglocation;
    @SerializedName("parkingdate")
    private String parkingdate;
    @SerializedName("duration")
    private String duration;
    @SerializedName("tollvehiclenumber")
    private String tollvehiclenumber;
    @SerializedName("tollplazaname")
    private String tollplazaname;
    @SerializedName("tolllocation")
    private String tolllocation;

    // --- Documents & URL Storage (Images 0 to 9) ---
    @SerializedName("image")
    private String image;
    @SerializedName("docFileURL")
    private String docFileURL;
    @SerializedName("docFileURLKey")
    private String docFileURLKey;
    @SerializedName("imageOne")
    private String imageOne;
    @SerializedName("docFileURLOne")
    private String docFileURLOne;
    @SerializedName("docFileURLKeyOne")
    private String docFileURLKeyOne;
    @SerializedName("imageTwo")
    private String imageTwo;
    @SerializedName("docFileURLTwo")
    private String docFileURLTwo;
    @SerializedName("docFileURLKeyTwo")
    private String docFileURLKeyTwo;
    @SerializedName("imageThree")
    private String imageThree;
    @SerializedName("docFileURLThree")
    private String docFileURLThree;
    @SerializedName("docFileURLKeyThree")
    private String docFileURLKeyThree;
    @SerializedName("imageFour")
    private String imageFour;
    @SerializedName("docFileURLFour")
    private String docFileURLFour;
    @SerializedName("docFileURLKeyFour")
    private String docFileURLKeyFour;
    @SerializedName("imageFive")
    private String imageFive;
    @SerializedName("docFileURLFive")
    private String docFileURLFive;
    @SerializedName("docFileURLKeyFive")
    private String docFileURLKeyFive;
    @SerializedName("imageSix")
    private String imageSix;
    @SerializedName("docFileURLSix")
    private String docFileURLSix;
    @SerializedName("docFileURLKeySix")
    private String docFileURLKeySix;
    @SerializedName("imageSeven")
    private String imageSeven;
    @SerializedName("docFileURLSeven")
    private String docFileURLSeven;
    @SerializedName("docFileURLKeySeven")
    private String docFileURLKeySeven;
    @SerializedName("imageEight")
    private String imageEight;
    @SerializedName("docFileURLEight")
    private String docFileURLEight;
    @SerializedName("docFileURLKeyEight")
    private String docFileURLKeyEight;
    @SerializedName("imageNine")
    private String imageNine;
    @SerializedName("docFileURLNine")
    private String docFileURLNine;
    @SerializedName("docFileURLKeyNine")
    private String docFileURLKeyNine;

    // --- Validation Helper ---
    private String validateField(String value) {
        return (value == null || value.trim().isEmpty() || "Please Select".equalsIgnoreCase(value)) ? "N/A" : value.trim();
    }

    // --- Setters and Validated Getters ---
    public String getEmployee() {
        return validateField(employee);
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getEmpId() {
        return validateField(empId);
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmployeeCode() {
        return validateField(employeeCode);
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFirstname() {
        return validateField(firstname);
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return validateField(lastname);
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getHrEmail() {
        return validateField(hrEmail);
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
    }

    public String getEmail() {
        return validateField(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesignation() {
        return validateField(designation);
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getGrade() {
        return validateField(grade);
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Manager getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(Manager reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getReportingManagerEmail() {
        return validateField(reportingManagerEmail);
    }

    public void setReportingManagerEmail(String reportingManagerEmail) {
        this.reportingManagerEmail = reportingManagerEmail;
    }

    public String getReportingManagerName() {
        return validateField(reportingManagerName);
    }

    public void setReportingManagerName(String reportingManagerName) {
        this.reportingManagerName = reportingManagerName;
    }

    public Manager getCrossmanager() {
        return crossmanager;
    }

    public void setCrossmanager(Manager crossmanager) {
        this.crossmanager = crossmanager;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getBu() {
        return validateField(bu);
    }

    public void setBu(String bu) {
        this.bu = bu;
    }

    public String getBuEmail() {
        return validateField(buEmail);
    }

    public void setBuEmail(String buEmail) {
        this.buEmail = buEmail;
    }

    public String getBuName() {
        return validateField(buName);
    }

    public void setBuName(String buName) {
        this.buName = buName;
    }

    public String getRequestedfirstname() {
        return validateField(requestedfirstname);
    }

    public void setRequestedfirstname(String requestedfirstname) {
        this.requestedfirstname = requestedfirstname;
    }

    public String getRequestedlastname() {
        return validateField(requestedlastname);
    }

    public void setRequestedlastname(String requestedlastname) {
        this.requestedlastname = requestedlastname;
    }

    public Object getReqDepartment() {
        return reqDepartment;
    }

    public void setReqDepartment(Object reqDepartment) {
        this.reqDepartment = reqDepartment;
    }

    public String getStatus() {
        return validateField(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequest() {
        return validateField(request);
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getClaimDate() {
        return validateField(claimDate);
    }

    public void setClaimDate(String claimDate) {
        this.claimDate = claimDate;
    }

    public String getModeofcal() {
        return validateField(modeofcal);
    }

    public void setModeofcal(String modeofcal) {
        this.modeofcal = modeofcal;
    }

    public String getModeofcalPascal() {
        return validateField(modeofcalPascal);
    }

    public void setModeofcalPascal(String modeofcalPascal) {
        this.modeofcalPascal = modeofcalPascal;
    }

    public String getProject() {
        return validateField(project);
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getMeeting() {
        return validateField(meeting);
    }

    public void setMeeting(String meeting) {
        this.meeting = meeting;
    }

    public String getPerposeofmeet() {
        return validateField(perposeofmeet);
    }

    public void setPerposeofmeet(String perposeofmeet) {
        this.perposeofmeet = perposeofmeet;
    }

    public Double getTotalClaim() {
        return totalClaim != null ? totalClaim : 0.0;
    }

    public void setTotalClaim(Double totalClaim) {
        this.totalClaim = totalClaim;
    }

    public String getCurrency() {
        return validateField(currency);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getConfbutton() {
        return confbutton != null ? confbutton : false;
    }

    public void setConfbutton(Boolean confbutton) {
        this.confbutton = confbutton;
    }

    public String getTotalamount() {
        return validateField(totalamount);
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }

    public String getRemark() {
        return validateField(remark);
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getExpenseType() {
        return validateField(expenseType);
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public Boolean getGuest() {
        return guest != null ? guest : false;
    }

    public void setGuest(Boolean guest) {
        this.guest = guest;
    }

    public String getMeantype() {
        return validateField(meantype);
    }

    public void setMeantype(String meantype) {
        this.meantype = meantype;
    }

    public String getRestaurant() {
        return validateField(restaurant);
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public String getPersons() {
        return validateField(persons);
    }

    public void setPersons(String persons) {
        this.persons = persons;
    }

    public String getDescription() {
        return validateField(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFromaddress() {
        return validateField(fromaddress);
    }

    public void setFromaddress(String fromaddress) {
        this.fromaddress = fromaddress;
    }

    public String getToaddress() {
        return validateField(toaddress);
    }

    public void setToaddress(String toaddress) {
        this.toaddress = toaddress;
    }

    public String getDistance() {
        return validateField(distance);
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getVarients() {
        return validateField(varients);
    }

    public void setVarients(String varients) {
        this.varients = varients;
    }

    public String getModeoftransport() {
        return validateField(modeoftransport);
    }

    public void setModeoftransport(String modeoftransport) {
        this.modeoftransport = modeoftransport;
    }

    public String getTravelCategory() {
        return validateField(travelCategory);
    }

    public void setTravelCategory(String travelCategory) {
        this.travelCategory = travelCategory;
    }

    public String getShared() {
        return validateField(shared);
    }

    public void setShared(String shared) {
        this.shared = shared;
    }

    public String getDedicated() {
        return validateField(dedicated);
    }

    public void setDedicated(String dedicated) {
        this.dedicated = dedicated;
    }

    public String getIndividually() {
        return validateField(individually);
    }

    public void setIndividually(String individually) {
        this.individually = individually;
    }

    public String getEmpTransport() {
        return validateField(empTransport);
    }

    public void setEmpTransport(String empTransport) {
        this.empTransport = empTransport;
    }

    public String getTravelRefId() {
        return validateField(travelRefId);
    }

    public void setTravelRefId(String travelRefId) {
        this.travelRefId = travelRefId;
    }

    public String getAccommodationtype() {
        return validateField(accommodationtype);
    }

    public void setAccommodationtype(String accommodationtype) {
        this.accommodationtype = accommodationtype;
    }

    public String getAccommodationname() {
        return validateField(accommodationname);
    }

    public void setAccommodationname(String accommodationname) {
        this.accommodationname = accommodationname;
    }

    public String getLocation() {
        return validateField(location);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getState() {
        return validateField(state);
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCheckin() {
        return validateField(checkin);
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getCheckout() {
        return validateField(checkout);
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }

    public String getPurposeofstay() {
        return validateField(purposeofstay);
    }

    public void setPurposeofstay(String purposeofstay) {
        this.purposeofstay = purposeofstay;
    }

    public String getExpancecategory() {
        return validateField(expancecategory);
    }

    public void setExpancecategory(String expancecategory) {
        this.expancecategory = expancecategory;
    }

    public String getBillingperiod() {
        return validateField(billingperiod);
    }

    public void setBillingperiod(String billingperiod) {
        this.billingperiod = billingperiod;
    }

    public String getNetprovider() {
        return validateField(netprovider);
    }

    public void setNetprovider(String netprovider) {
        this.netprovider = netprovider;
    }

    public String getBillnumber() {
        return validateField(billnumber);
    }

    public void setBillnumber(String billnumber) {
        this.billnumber = billnumber;
    }

    public String getVehiclenumber() {
        return validateField(vehiclenumber);
    }

    public void setVehiclenumber(String vehiclenumber) {
        this.vehiclenumber = vehiclenumber;
    }

    public String getFueltype() {
        return validateField(fueltype);
    }

    public void setFueltype(String fueltype) {
        this.fueltype = fueltype;
    }

    public String getFuelquantity() {
        return validateField(fuelquantity);
    }

    public void setFuelquantity(String fuelquantity) {
        this.fuelquantity = fuelquantity;
    }

    public String getFuelstationname() {
        return validateField(fuelstationname);
    }

    public void setFuelstationname(String fuelstationname) {
        this.fuelstationname = fuelstationname;
    }

    public String getParkinglocation() {
        return validateField(parkinglocation);
    }

    public void setParkinglocation(String parkinglocation) {
        this.parkinglocation = parkinglocation;
    }

    public String getParkingdate() {
        return validateField(parkingdate);
    }

    public void setParkingdate(String parkingdate) {
        this.parkingdate = parkingdate;
    }

    public String getDuration() {
        return validateField(duration);
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTollvehiclenumber() {
        return validateField(tollvehiclenumber);
    }

    public void setTollvehiclenumber(String tollvehiclenumber) {
        this.tollvehiclenumber = tollvehiclenumber;
    }

    public String getTollplazaname() {
        return validateField(tollplazaname);
    }

    public void setTollplazaname(String tollplazaname) {
        this.tollplazaname = tollplazaname;
    }

    public String getTolllocation() {
        return validateField(tolllocation);
    }

    public void setTolllocation(String tolllocation) {
        this.tolllocation = tolllocation;
    }

    // --- Media Getters / Setters ---
    public String getImage() {
        return validateField(image);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDocFileURL() {
        return validateField(docFileURL);
    }

    public void setDocFileURL(String docFileURL) {
        this.docFileURL = docFileURL;
    }

    public String getDocFileURLKey() {
        return validateField(docFileURLKey);
    }

    public void setDocFileURLKey(String docFileURLKey) {
        this.docFileURLKey = docFileURLKey;
    }

    public String getImageOne() {
        return validateField(imageOne);
    }

    public void setImageOne(String imageOne) {
        this.imageOne = imageOne;
    }

    public String getDocFileURLOne() {
        return validateField(docFileURLOne);
    }

    public void setDocFileURLOne(String docFileURLOne) {
        this.docFileURLOne = docFileURLOne;
    }

    public String getDocFileURLKeyOne() {
        return validateField(docFileURLKeyOne);
    }

    public void setDocFileURLKeyOne(String docFileURLKeyOne) {
        this.docFileURLKeyOne = docFileURLKeyOne;
    }

    public String getImageTwo() {
        return validateField(imageTwo);
    }

    public void setImageTwo(String imageTwo) {
        this.imageTwo = imageTwo;
    }

    public String getDocFileURLTwo() {
        return validateField(docFileURLTwo);
    }

    public void setDocFileURLTwo(String docFileURLTwo) {
        this.docFileURLTwo = docFileURLTwo;
    }

    public String getDocFileURLKeyTwo() {
        return validateField(docFileURLKeyTwo);
    }

    public void setDocFileURLKeyTwo(String docFileURLKeyTwo) {
        this.docFileURLKeyTwo = docFileURLKeyTwo;
    }

    public String getImageThree() {
        return validateField(imageThree);
    }

    public void setImageThree(String imageThree) {
        this.imageThree = imageThree;
    }

    public String getDocFileURLThree() {
        return validateField(docFileURLThree);
    }

    public void setDocFileURLThree(String docFileURLThree) {
        this.docFileURLThree = docFileURLThree;
    }

    public String getDocFileURLKeyThree() {
        return validateField(docFileURLKeyThree);
    }

    public void setDocFileURLKeyThree(String docFileURLKeyThree) {
        this.docFileURLKeyThree = docFileURLKeyThree;
    }

    public String getImageFour() {
        return validateField(imageFour);
    }

    public void setImageFour(String imageFour) {
        this.imageFour = imageFour;
    }

    public String getDocFileURLFour() {
        return validateField(docFileURLFour);
    }

    public void setDocFileURLFour(String docFileURLFour) {
        this.docFileURLFour = docFileURLFour;
    }

    public String getDocFileURLKeyFour() {
        return validateField(docFileURLKeyFour);
    }

    public void setDocFileURLKeyFour(String docFileURLKeyFour) {
        this.docFileURLKeyFour = docFileURLKeyFour;
    }

    public String getImageFive() {
        return validateField(imageFive);
    }

    public void setImageFive(String imageFive) {
        this.imageFive = imageFive;
    }

    public String getDocFileURLFive() {
        return validateField(docFileURLFive);
    }

    public void setDocFileURLFive(String docFileURLFive) {
        this.docFileURLFive = docFileURLFive;
    }

    public String getDocFileURLKeyFive() {
        return validateField(docFileURLKeyFive);
    }

    public void setDocFileURLKeyFive(String docFileURLKeyFive) {
        this.docFileURLKeyFive = docFileURLKeyFive;
    }

    public String getImageSix() {
        return validateField(imageSix);
    }

    public void setImageSix(String imageSix) {
        this.imageSix = imageSix;
    }

    public String getDocFileURLSix() {
        return validateField(docFileURLSix);
    }

    public void setDocFileURLSix(String docFileURLSix) {
        this.docFileURLSix = docFileURLSix;
    }

    public String getDocFileURLKeySix() {
        return validateField(docFileURLKeySix);
    }

    public void setDocFileURLKeySix(String docFileURLKeySix) {
        this.docFileURLKeySix = docFileURLKeySix;
    }

    public String getImageSeven() {
        return validateField(imageSeven);
    }

    public void setImageSeven(String imageSeven) {
        this.imageSeven = imageSeven;
    }

    public String getDocFileURLSeven() {
        return validateField(docFileURLSeven);
    }

    public void setDocFileURLSeven(String docFileURLSeven) {
        this.docFileURLSeven = docFileURLSeven;
    }

    public String getDocFileURLKeySeven() {
        return validateField(docFileURLKeySeven);
    }

    public void setDocFileURLKeySeven(String docFileURLKeySeven) {
        this.docFileURLKeySeven = docFileURLKeySeven;
    }

    public String getImageEight() {
        return validateField(imageEight);
    }

    public void setImageEight(String imageEight) {
        this.imageEight = imageEight;
    }

    public String getDocFileURLEight() {
        return validateField(docFileURLEight);
    }

    public void setDocFileURLEight(String docFileURLEight) {
        this.docFileURLEight = docFileURLEight;
    }

    public String getDocFileURLKeyEight() {
        return validateField(docFileURLKeyEight);
    }

    public void setDocFileURLKeyEight(String docFileURLKeyEight) {
        this.docFileURLKeyEight = docFileURLKeyEight;
    }

    public String getImageNine() {
        return validateField(imageNine);
    }

    public void setImageNine(String imageNine) {
        this.imageNine = imageNine;
    }

    public String getDocFileURLNine() {
        return validateField(docFileURLNine);
    }

    public void setDocFileURLNine(String docFileURLNine) {
        this.docFileURLNine = docFileURLNine;
    }

    public String getDocFileURLKeyNine() {
        return validateField(docFileURLKeyNine);
    }

    public void setDocFileURLKeyNine(String docFileURLKeyNine) {
        this.docFileURLKeyNine = docFileURLKeyNine;
    }

    // --- Sub-Classes for Nested JSON Objects ---
    public static class Manager {
        @SerializedName("_id")
        private String id;
        @SerializedName("firstname")
        private String firstname;
        @SerializedName("lastname")
        private String lastname;
        @SerializedName("email")
        private String email;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
    }

    public static class Department {
        @SerializedName("_id")
        private String id;
        @SerializedName("name")
        private String name;
        @SerializedName("description")
        private String description;
        @SerializedName("default")
        private Boolean isDefault;
        @SerializedName("active")
        private Boolean active;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }
}