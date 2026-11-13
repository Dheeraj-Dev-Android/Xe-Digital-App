package app.xedigital.ai.model.employeeOnboardDetails;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class EmployeeOnBoardDetailsItem implements Serializable {

    @SerializedName("country")
    private String country;

    @SerializedName("documents")
    private List<DocumentsItem> documents;

    @SerializedName("isDraft")
    private boolean isDraft;

    @SerializedName("uanNumber")
    private String uanNumber;

    @SerializedName("lastDrawnCtc")
    private String lastDrawnCtc;

    @SerializedName("employee")
    private String employee;

    @SerializedName("personalEmail")
    private String personalEmail;

    @SerializedName("bloodGroup")
    private String bloodGroup;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("institution")
    private String institution;

    @SerializedName("emergencyRelationship")
    private String emergencyRelationship;

    @SerializedName("__v")
    private int v;

    @SerializedName("aadhaarBackFileURLKey")
    private String aadhaarBackFileURLKey;

    @SerializedName("state")
    private String state;

    @SerializedName("panFileURLKey")
    private String panFileURLKey;

    @SerializedName("percentageGrade")
    private String percentageGrade;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("passportNumber")
    private String passportNumber;

    @SerializedName("pincode")
    private String pincode;

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("yearOfPassing")
    private String yearOfPassing;

    @SerializedName("fatherOrHusbandName")
    private String fatherOrHusbandName;

    @SerializedName("panNumber")
    private String panNumber;

    @SerializedName("knownAllergies")
    private String knownAllergies;

    @SerializedName("qualification")
    private String qualification;

    @SerializedName("aadhaarNumber")
    private String aadhaarNumber;

    @SerializedName("nationality")
    private String nationality;

    @SerializedName("alternateMobile")
    private String alternateMobile;

    @SerializedName("dob")
    private String dob;

    @SerializedName("grade")
    private String grade;

    @SerializedName("updateRequestedDate")
    private Object updateRequestedDate;

    @SerializedName("_id")
    private String id;

    @SerializedName("designation")
    private String designation;

    @SerializedName("maritalStatus")
    private String maritalStatus;

    @SerializedName("status")
    private boolean status;

    @SerializedName("gender")
    private String gender;

    @SerializedName("city")
    private String city;

    @SerializedName("bankName")
    private String bankName;

    @SerializedName("passportExpiryDate")
    private String passportExpiryDate;

    @SerializedName("upiId")
    private String upiId;

    @SerializedName("accountHolderName")
    private String accountHolderName;

    @SerializedName("workLocation")
    private String workLocation;

    @SerializedName("previousDesignation")
    private String previousDesignation;

    @SerializedName("aadhaarFrontFileURL")
    private String aadhaarFrontFileURL;

    @SerializedName("aadhaarFrontFileURLKey")
    private String aadhaarFrontFileURLKey;

    @SerializedName("nomineeDob")
    private String nomineeDob;

    @SerializedName("permanentAddress")
    private String permanentAddress;

    @SerializedName("department")
    private String department;

    @SerializedName("familyDetails")
    private List<FamilyDetailsItem> familyDetails;

    @SerializedName("ifscCode")
    private String ifscCode;

    @SerializedName("sharePercentage")
    private String sharePercentage;

    @SerializedName("nomineeRelationship")
    private String nomineeRelationship;

    @SerializedName("panFileURL")
    private String panFileURL;

    @SerializedName("employmentType")
    private String employmentType;

    @SerializedName("medicalConditionDetails")
    private String medicalConditionDetails;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("emergencyAddress")
    private String emergencyAddress;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("employeeId")
    private String employeeId;

    @SerializedName("nomineeName")
    private String nomineeName;

    @SerializedName("emergencyNumber")
    private String emergencyNumber;

    @SerializedName("previousEmployer")
    private String previousEmployer;

    @SerializedName("accountNumber")
    private String accountNumber;

    @SerializedName("currentAddress")
    private String currentAddress;

    @SerializedName("personalMobile")
    private String personalMobile;

    @SerializedName("reasonForLeaving")
    private String reasonForLeaving;

    @SerializedName("aadhaarBackFileURL")
    private String aadhaarBackFileURL;

    @SerializedName("dateOfJoining")
    private String dateOfJoining;

    @SerializedName("emergencyName")
    private String emergencyName;

    @SerializedName("medicalCondition")
    private String medicalCondition;

    @SerializedName("previousDuration")
    private String previousDuration;

    @SerializedName("esiNumber")
    private String esiNumber;

    @SerializedName("emergencyAlternateNumber")
    private String emergencyAlternateNumber;

    @SerializedName("remarks")
    private String remarks;

    public String getCountry() {
        return country;
    }

    public List<DocumentsItem> getDocuments() {
        return documents;
    }

    public boolean isIsDraft() {
        return isDraft;
    }

    public String getUanNumber() {
        return uanNumber;
    }

    public String getLastDrawnCtc() {
        return lastDrawnCtc;
    }

    public String getEmployee() {
        return employee;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getInstitution() {
        return institution;
    }

    public String getEmergencyRelationship() {
        return emergencyRelationship;
    }

    public int getV() {
        return v;
    }

    public String getAadhaarBackFileURLKey() {
        return aadhaarBackFileURLKey;
    }

    public String getState() {
        return state;
    }

    public String getPanFileURLKey() {
        return panFileURLKey;
    }

    public String getPercentageGrade() {
        return percentageGrade;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public String getPincode() {
        return pincode;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public String getYearOfPassing() {
        return yearOfPassing;
    }

    public String getFatherOrHusbandName() {
        return fatherOrHusbandName;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public String getKnownAllergies() {
        return knownAllergies;
    }

    public String getQualification() {
        return qualification;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public String getAlternateMobile() {
        return alternateMobile;
    }

    public String getDob() {
        return dob;
    }

    public String getGrade() {
        return grade;
    }

    public Object getUpdateRequestedDate() {
        return updateRequestedDate;
    }

    public String getId() {
        return id;
    }

    public String getDesignation() {
        return designation;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public boolean isStatus() {
        return status;
    }

    public String getGender() {
        return gender;
    }

    public String getCity() {
        return city;
    }

    public String getBankName() {
        return bankName;
    }

    public String getPassportExpiryDate() {
        return passportExpiryDate;
    }

    public String getUpiId() {
        return upiId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public String getPreviousDesignation() {
        return previousDesignation;
    }

    public String getAadhaarFrontFileURL() {
        return aadhaarFrontFileURL;
    }

    public String getAadhaarFrontFileURLKey() {
        return aadhaarFrontFileURLKey;
    }

    public String getNomineeDob() {
        return nomineeDob;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public String getDepartment() {
        return department;
    }

    public List<FamilyDetailsItem> getFamilyDetails() {
        return familyDetails;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getSharePercentage() {
        return sharePercentage;
    }

    public String getNomineeRelationship() {
        return nomineeRelationship;
    }

    public String getPanFileURL() {
        return panFileURL;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public String getMedicalConditionDetails() {
        return medicalConditionDetails;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getEmergencyAddress() {
        return emergencyAddress;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getNomineeName() {
        return nomineeName;
    }

    public String getEmergencyNumber() {
        return emergencyNumber;
    }

    public String getPreviousEmployer() {
        return previousEmployer;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public String getPersonalMobile() {
        return personalMobile;
    }

    public String getReasonForLeaving() {
        return reasonForLeaving;
    }

    public String getAadhaarBackFileURL() {
        return aadhaarBackFileURL;
    }

    public String getDateOfJoining() {
        return dateOfJoining;
    }

    public String getEmergencyName() {
        return emergencyName;
    }

    public String getMedicalCondition() {
        return medicalCondition;
    }

    public String getPreviousDuration() {
        return previousDuration;
    }

    public String getEsiNumber() {
        return esiNumber;
    }

    public String getEmergencyAlternateNumber() {
        return emergencyAlternateNumber;
    }

    public String getRemarks() {
        return remarks;
    }
}