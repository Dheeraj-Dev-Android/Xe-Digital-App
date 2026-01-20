package app.xedigital.ai.model.shiftApprovalList;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeeShiftdata")
    private List<EmployeeApproveShiftdataItem> employeeShiftdata;

    public List<EmployeeApproveShiftdataItem> getEmployeeShiftdata() {
        return employeeShiftdata;
    }
}