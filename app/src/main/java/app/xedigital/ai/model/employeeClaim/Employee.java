package app.xedigital.ai.model.employeeClaim;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Employee {

	@SerializedName("joiningType")
	private String joiningType;

	@SerializedName("fatherName")
	private Object fatherName;

	@SerializedName("firstname")
	private String firstname;

	@SerializedName("components")
	private List<ComponentsItem> components;

	@SerializedName("isVerified")
	private boolean isVerified;

	@SerializedName("shift")
	private String shift;

	@SerializedName("addpayroll")
	private boolean addpayroll;

	@SerializedName("totalMonthlySalary")
	private double totalMonthlySalary;

	@SerializedName("joiningDate")
	private String joiningDate;

	@SerializedName("employeeCode")
	private String employeeCode;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("ctc")
	private double ctc;

	@SerializedName("bu")
	private String bu;

	@SerializedName("contact")
	private String contact;

	@SerializedName("__v")
	private int v;

	@SerializedName("isHROrAdmin")
	private boolean isHROrAdmin;

	@SerializedName("company")
	private String company;

	@SerializedName("epf")
	private boolean epf;

	@SerializedName("state")
	private Object state;

	@SerializedName("department")
	private String department;

	@SerializedName("profileImageUrl")
	private String profileImageUrl;

	@SerializedName("email")
	private String email;

	@SerializedName("updatedAt")
	private String updatedAt;

	@SerializedName("pincode")
	private Object pincode;

	@SerializedName("reportingManager")
	private String reportingManager;

	@SerializedName("address")
	private Object address;

	@SerializedName("totalYearlySalary")
	private Object totalYearlySalary;

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
	private String partner;

	@SerializedName("grade")
	private String grade;

	@SerializedName("esi")
	private boolean esi;

	@SerializedName("_id")
	private String id;

	@SerializedName("designation")
	private String designation;

	@SerializedName("fullname")
	private String fullname;

	@SerializedName("crossmanager")
	private String crossmanager;

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

	public String getFirstname() {
		return firstname;
	}

	public List<ComponentsItem> getComponents() {
		return components;
	}

	public boolean isIsVerified() {
		return isVerified;
	}

	public String getShift() {
		return shift;
	}

	public boolean isAddpayroll() {
		return addpayroll;
	}

	public double getTotalMonthlySalary() {
		return totalMonthlySalary;
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

	public double getCtc() {
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

	public String getCompany() {
		return company;
	}

	public boolean isEpf() {
		return epf;
	}

	public Object getState() {
		return state;
	}

	public String getDepartment() {
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

	public String getReportingManager() {
		return reportingManager;
	}

	public Object getAddress() {
		return address;
	}

	public Object getTotalYearlySalary() {
		return totalYearlySalary;
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

	public String getPartner() {
		return partner;
	}

	public String getGrade() {
		return grade;
	}

	public boolean isEsi() {
		return esi;
	}

	public String getId() {
		return id;
	}

	public String getDesignation() {
		return designation;
	}

	public String getFullname() {
		return fullname;
	}

	public String getCrossmanager() {
		return crossmanager;
	}

	public Object getUanno() {
		return uanno;
	}

	public Object getAdharNo() {
		return adharNo;
	}
}