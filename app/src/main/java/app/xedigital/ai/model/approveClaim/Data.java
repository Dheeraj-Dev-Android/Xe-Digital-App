package app.xedigital.ai.model.approveClaim;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeeClaimdata")
    private List<EmployeeClaimdataItem> employeeClaimdata;

    public List<EmployeeClaimdataItem> getEmployeeClaimdata() {
        return employeeClaimdata;
    }

    public void setEmployeeClaimdata(List<EmployeeClaimdataItem> employeeClaimdata) {
        this.employeeClaimdata = employeeClaimdata;
    }
}