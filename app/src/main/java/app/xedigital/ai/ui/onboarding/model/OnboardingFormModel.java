package app.xedigital.ai.ui.onboarding.model;

import android.net.Uri;

import java.util.List;

/**
 * Field names match EXACTLY with web formControlName values
 */
public class OnboardingFormModel {

    // ── Meta (sent hidden in web form) ────────────────────────────────────────
    private String user = "";
    private String empFirstName = "";
    private String empLastName = "";
    private String company = "";
    private String empEmail = "";
    private boolean isDraft = false;

    // ── 1. Personal Details ───────────────────────────────────────────────────
    private String fullName = "";   // required
    private String dob = "";   // required
    private String gender = "";   // required
    private String fatherOrHusbandName = "";
    private String maritalStatus = "";
    private String nationality = "";
    private String bloodGroup = "";

    // ── 2. Contact Details ────────────────────────────────────────────────────
    private String personalMobile = "";   // required + pattern
    private String alternateMobile = "";
    private String personalEmail = "";   // required + email
    private String currentAddress = "";
    private String permanentAddress = "";
    private String city = "";
    private String state = "";
    private String pincode = "";
    private String country = "India";

    // ── 3. Emergency Contact ──────────────────────────────────────────────────
    private String emergencyName = "";
    private String emergencyRelationship = "";
    private String emergencyNumber = "";
    private String emergencyAlternateNumber = "";
    private String emergencyAddress = "";

    // ── 4. Employment Details ─────────────────────────────────────────────────
    private String employeeId = "";
    private String dateOfJoining = "";
    private String designation = "";
    private String department = "";
    private String reportingManager = "";
    private String workLocation = "";
    private String employmentType = "";
    private String grade = "";   // NOTE: "grade" not "gradeBand"

    // ── 5. Education ──────────────────────────────────────────────────────────
    private String qualification = "";
    private String institution = "";
    private String yearOfPassing = "";
    private String percentageGrade = "";

    // ── 6. Previous Employment ────────────────────────────────────────────────
    private String previousEmployer = "";
    private String previousDesignation = "";   // NOTE: "previousDesignation"
    private String previousDuration = "";   // NOTE: single field
    private String reasonForLeaving = "";
    private String lastDrawnCtc = "";
    private String relievingDate = "";

    // ── 7. Bank Details ───────────────────────────────────────────────────────
    private String bankName = "";
    private String branchName = "";
    private String accountNumber = "";
    private String ifscCode = "";
    private String accountHolderName = "";
    private String upiId = "";

    // ── 8. Statutory Details ──────────────────────────────────────────────────
    private String uanNumber = "";
    private String esiNumber = "";
    private String passportNumber = "";
    private String passportExpiryDate = "";

    // ── 9. Nominee Details ────────────────────────────────────────────────────
    private String nomineeName = "";
    private String nomineeRelationship = "";
    private String nomineeDob = "";
    private String sharePercentage = "";

    // ── 10. Medical & Health ──────────────────────────────────────────────────
    private String medicalCondition = "";   // NOTE: "medicalCondition"
    private String medicalConditionDetails = "";
    private String knownAllergies = "";

    // ── Family Details ────────────────────────────────────────────────────────
    private List<FamilyMember> familyDetails;  // NOTE: "familyDetails"

    // ── 11. Document Upload ───────────────────────────────────────────────────
    private String aadhaarNumber = "";   // required + pattern
    private Uri aadhaarFrontFile;            // required if no URL
    private String aadhaarFrontFileName = "";
    private String aadhaarFrontFileURL = "";
    private String aadhaarFrontFileURLKey = "";

    private Uri aadhaarBackFile;             // required if no URL
    private String aadhaarBackFileName = "";
    private String aadhaarBackFileURL = "";
    private String aadhaarBackFileURLKey = "";

    private String panNumber = "";          // required + pattern
    private Uri panFile;                     // required if no URL
    private String panFileName = "";
    private String panFileURL = "";
    private String panFileURLKey = "";

    private List<OtherDocument> documents;     // NOTE: "documents"

    // ── Masked values (display only – from server) ────────────────────────────
    private String aadhaarNumberMasked = "";
    private String panNumberMasked = "";
    private String accountNumberMasked = "";
    private String uanNumberMasked = "";
    private String esiNumberMasked = "";
    private String passportNumberMasked = "";

    // ── Status fields (from server response) ──────────────────────────────────
    private boolean status = false;
    private String remarks = "";

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================

    public String getUser() {
        return user;
    }

    public void setUser(String v) {
        user = v;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public void setEmpFirstName(String v) {
        empFirstName = v;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public void setEmpLastName(String v) {
        empLastName = v;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String v) {
        company = v;
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public void setEmpEmail(String v) {
        empEmail = v;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean v) {
        isDraft = v;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String v) {
        fullName = v;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String v) {
        dob = v;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String v) {
        gender = v;
    }

    public String getFatherOrHusbandName() {
        return fatherOrHusbandName;
    }

    public void setFatherOrHusbandName(String v) {
        fatherOrHusbandName = v;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String v) {
        maritalStatus = v;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String v) {
        nationality = v;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String v) {
        bloodGroup = v;
    }

    public String getPersonalMobile() {
        return personalMobile;
    }

    public void setPersonalMobile(String v) {
        personalMobile = v;
    }

    public String getAlternateMobile() {
        return alternateMobile;
    }

    public void setAlternateMobile(String v) {
        alternateMobile = v;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public void setPersonalEmail(String v) {
        personalEmail = v;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String v) {
        currentAddress = v;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String v) {
        permanentAddress = v;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String v) {
        city = v;
    }

    public String getState() {
        return state;
    }

    public void setState(String v) {
        state = v;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String v) {
        pincode = v;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String v) {
        country = v;
    }

    public String getEmergencyName() {
        return emergencyName;
    }

    public void setEmergencyName(String v) {
        emergencyName = v;
    }

    public String getEmergencyRelationship() {
        return emergencyRelationship;
    }

    public void setEmergencyRelationship(String v) {
        emergencyRelationship = v;
    }

    public String getEmergencyNumber() {
        return emergencyNumber;
    }

    public void setEmergencyNumber(String v) {
        emergencyNumber = v;
    }

    public String getEmergencyAlternateNumber() {
        return emergencyAlternateNumber;
    }

    public void setEmergencyAlternateNumber(String v) {
        emergencyAlternateNumber = v;
    }

    public String getEmergencyAddress() {
        return emergencyAddress;
    }

    public void setEmergencyAddress(String v) {
        emergencyAddress = v;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String v) {
        employeeId = v;
    }

    public String getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(String v) {
        dateOfJoining = v;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String v) {
        designation = v;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String v) {
        department = v;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String v) {
        reportingManager = v;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String v) {
        workLocation = v;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String v) {
        employmentType = v;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String v) {
        grade = v;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String v) {
        qualification = v;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String v) {
        institution = v;
    }

    public String getYearOfPassing() {
        return yearOfPassing;
    }

    public void setYearOfPassing(String v) {
        yearOfPassing = v;
    }

    public String getPercentageGrade() {
        return percentageGrade;
    }

    public void setPercentageGrade(String v) {
        percentageGrade = v;
    }

    public String getPreviousEmployer() {
        return previousEmployer;
    }

    public void setPreviousEmployer(String v) {
        previousEmployer = v;
    }

    public String getPreviousDesignation() {
        return previousDesignation;
    }

    public void setPreviousDesignation(String v) {
        previousDesignation = v;
    }

    public String getPreviousDuration() {
        return previousDuration;
    }

    public void setPreviousDuration(String v) {
        previousDuration = v;
    }

    public String getReasonForLeaving() {
        return reasonForLeaving;
    }

    public void setReasonForLeaving(String v) {
        reasonForLeaving = v;
    }

    public String getLastDrawnCtc() {
        return lastDrawnCtc;
    }

    public void setLastDrawnCtc(String v) {
        lastDrawnCtc = v;
    }

    public String getRelievingDate() {
        return relievingDate;
    }

    public void setRelievingDate(String v) {
        relievingDate = v;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String v) {
        bankName = v;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String v) {
        branchName = v;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String v) {
        accountNumber = v;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String v) {
        ifscCode = v;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String v) {
        accountHolderName = v;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String v) {
        upiId = v;
    }

    public String getUanNumber() {
        return uanNumber;
    }

    public void setUanNumber(String v) {
        uanNumber = v;
    }

    public String getEsiNumber() {
        return esiNumber;
    }

    public void setEsiNumber(String v) {
        esiNumber = v;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String v) {
        passportNumber = v;
    }

    public String getPassportExpiryDate() {
        return passportExpiryDate;
    }

    public void setPassportExpiryDate(String v) {
        passportExpiryDate = v;
    }

    public String getNomineeName() {
        return nomineeName;
    }

    public void setNomineeName(String v) {
        nomineeName = v;
    }

    public String getNomineeRelationship() {
        return nomineeRelationship;
    }

    public void setNomineeRelationship(String v) {
        nomineeRelationship = v;
    }

    public String getNomineeDob() {
        return nomineeDob;
    }

    public void setNomineeDob(String v) {
        nomineeDob = v;
    }

    public String getSharePercentage() {
        return sharePercentage;
    }

    public void setSharePercentage(String v) {
        sharePercentage = v;
    }

    public String getMedicalCondition() {
        return medicalCondition;
    }

    public void setMedicalCondition(String v) {
        medicalCondition = v;
    }

    public String getMedicalConditionDetails() {
        return medicalConditionDetails;
    }

    public void setMedicalConditionDetails(String v) {
        medicalConditionDetails = v;
    }

    public String getKnownAllergies() {
        return knownAllergies;
    }

    public void setKnownAllergies(String v) {
        knownAllergies = v;
    }

    public List<FamilyMember> getFamilyDetails() {
        return familyDetails;
    }

    public void setFamilyDetails(List<FamilyMember> v) {
        familyDetails = v;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String v) {
        aadhaarNumber = v;
    }

    public Uri getAadhaarFrontFile() {
        return aadhaarFrontFile;
    }

    public void setAadhaarFrontFile(Uri v) {
        aadhaarFrontFile = v;
    }

    public String getAadhaarFrontFileName() {
        return aadhaarFrontFileName;
    }

    public void setAadhaarFrontFileName(String v) {
        aadhaarFrontFileName = v;
    }

    public String getAadhaarFrontFileURL() {
        return aadhaarFrontFileURL;
    }

    public void setAadhaarFrontFileURL(String v) {
        aadhaarFrontFileURL = v;
    }

    public String getAadhaarFrontFileURLKey() {
        return aadhaarFrontFileURLKey;
    }

    public void setAadhaarFrontFileURLKey(String v) {
        aadhaarFrontFileURLKey = v;
    }

    public Uri getAadhaarBackFile() {
        return aadhaarBackFile;
    }

    public void setAadhaarBackFile(Uri v) {
        aadhaarBackFile = v;
    }

    public String getAadhaarBackFileName() {
        return aadhaarBackFileName;
    }

    public void setAadhaarBackFileName(String v) {
        aadhaarBackFileName = v;
    }

    public String getAadhaarBackFileURL() {
        return aadhaarBackFileURL;
    }

    public void setAadhaarBackFileURL(String v) {
        aadhaarBackFileURL = v;
    }

    public String getAadhaarBackFileURLKey() {
        return aadhaarBackFileURLKey;
    }

    public void setAadhaarBackFileURLKey(String v) {
        aadhaarBackFileURLKey = v;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String v) {
        panNumber = v;
    }

    public Uri getPanFile() {
        return panFile;
    }

    public void setPanFile(Uri v) {
        panFile = v;
    }

    public String getPanFileName() {
        return panFileName;
    }

    public void setPanFileName(String v) {
        panFileName = v;
    }

    public String getPanFileURL() {
        return panFileURL;
    }

    public void setPanFileURL(String v) {
        panFileURL = v;
    }

    public String getPanFileURLKey() {
        return panFileURLKey;
    }

    public void setPanFileURLKey(String v) {
        panFileURLKey = v;
    }

    public List<OtherDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<OtherDocument> v) {
        documents = v;
    }

    public String getAadhaarNumberMasked() {
        return aadhaarNumberMasked;
    }

    public void setAadhaarNumberMasked(String v) {
        aadhaarNumberMasked = v;
    }

    public String getPanNumberMasked() {
        return panNumberMasked;
    }

    public void setPanNumberMasked(String v) {
        panNumberMasked = v;
    }

    public String getAccountNumberMasked() {
        return accountNumberMasked;
    }

    public void setAccountNumberMasked(String v) {
        accountNumberMasked = v;
    }

    public String getUanNumberMasked() {
        return uanNumberMasked;
    }

    public void setUanNumberMasked(String v) {
        uanNumberMasked = v;
    }

    public String getEsiNumberMasked() {
        return esiNumberMasked;
    }

    public void setEsiNumberMasked(String v) {
        esiNumberMasked = v;
    }

    public String getPassportNumberMasked() {
        return passportNumberMasked;
    }

    public void setPassportNumberMasked(String v) {
        passportNumberMasked = v;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean v) {
        status = v;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String v) {
        remarks = v;
    }
}