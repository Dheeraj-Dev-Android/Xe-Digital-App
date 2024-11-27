package app.xedigital.ai.model.employeeClaim;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmployeeClaimResponse {

    @SerializedName("employeeClaimdata")
    private List<EmployeeClaimdataItem> employeeClaimdata;

    public List<EmployeeClaimdataItem> getEmployeeClaimdata() {
        return employeeClaimdata;
    }

    public void setEmployeeClaimdata(List<EmployeeClaimdataItem> employeeClaimdata) {
        this.employeeClaimdata = employeeClaimdata;
    }
}