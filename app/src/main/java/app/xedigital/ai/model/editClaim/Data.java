package app.xedigital.ai.model.editClaim;

import com.google.gson.annotations.SerializedName;

import app.xedigital.ai.model.employeeClaim.EmployeeClaimdataItem;

public class Data {

    @SerializedName("claim")
    private EmployeeClaimdataItem claim;

    public EmployeeClaimdataItem getClaim() {
        return claim;
    }
}