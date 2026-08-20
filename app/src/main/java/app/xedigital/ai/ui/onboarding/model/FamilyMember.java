package app.xedigital.ai.ui.onboarding.model;

import android.net.Uri;

/**
 * Field names match EXACTLY with web formArrayName "familyDetails"
 */
public class FamilyMember {

    // Matches web: familyMemberName, familyRelationship,
    //              familyDob, familyMobile
    private String familyMemberName = "";
    private String familyRelationship = "";
    private String familyDob = "";
    private String familyMobile = "";

    // File handling
    private Uri addressProofFile;
    private String addressProofFileName = "No file chosen";
    private String addressProofFileURL = "";
    private String addressProofFileURLKey = "";

    public FamilyMember() {
    }

    public String getFamilyMemberName() {
        return familyMemberName;
    }

    public void setFamilyMemberName(String v) {
        familyMemberName = v;
    }

    public String getFamilyRelationship() {
        return familyRelationship;
    }

    public void setFamilyRelationship(String v) {
        familyRelationship = v;
    }

    public String getFamilyDob() {
        return familyDob;
    }

    public void setFamilyDob(String v) {
        familyDob = v;
    }

    public String getFamilyMobile() {
        return familyMobile;
    }

    public void setFamilyMobile(String v) {
        familyMobile = v;
    }

    public Uri getAddressProofFile() {
        return addressProofFile;
    }

    public void setAddressProofFile(Uri v) {
        addressProofFile = v;
    }

    public String getAddressProofFileName() {
        return addressProofFileName;
    }

    public void setAddressProofFileName(String v) {
        addressProofFileName = v;
    }

    public String getAddressProofFileURL() {
        return addressProofFileURL;
    }

    public void setAddressProofFileURL(String v) {
        addressProofFileURL = v;
    }

    public String getAddressProofFileURLKey() {
        return addressProofFileURLKey;
    }

    public void setAddressProofFileURLKey(String v) {
        addressProofFileURLKey = v;
    }
}