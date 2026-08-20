package app.xedigital.ai.model.employeeOnboarding;

import com.google.gson.annotations.SerializedName;

public class FamilyDetailsItem {

    @SerializedName("familyMobile")
    private String familyMobile;

    @SerializedName("addressProofFileURL")
    private String addressProofFileURL;

    @SerializedName("addressProofFileURLKey")
    private String addressProofFileURLKey;

    @SerializedName("familyDob")
    private String familyDob;

    @SerializedName("familyRelationship")
    private String familyRelationship;

    @SerializedName("_id")
    private String id;

    @SerializedName("familyMemberName")
    private String familyMemberName;

    public String getFamilyMobile() {
        return familyMobile;
    }

    public String getAddressProofFileURL() {
        return addressProofFileURL;
    }

    public String getAddressProofFileURLKey() {
        return addressProofFileURLKey;
    }

    public String getFamilyDob() {
        return familyDob;
    }

    public String getFamilyRelationship() {
        return familyRelationship;
    }

    public String getId() {
        return id;
    }

    public String getFamilyMemberName() {
        return familyMemberName;
    }
}