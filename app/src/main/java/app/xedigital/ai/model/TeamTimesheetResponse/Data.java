package app.xedigital.ai.model.TeamTimesheetResponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeesDcrData")
    private List<EmployeesDcrDataItem> employeesDcrData;

    public List<EmployeesDcrDataItem> getEmployeesDcrData() {
        return employeesDcrData;
    }
}