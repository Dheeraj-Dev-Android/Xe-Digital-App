package app.xedigital.ai.model.shiftApplied;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeeShiftdata")
    private List<EmployeeShiftdataItem> employeeShiftdata;

    public List<EmployeeShiftdataItem> getEmployeeShiftdata() {
        return employeeShiftdata;
    }
}