package app.xedigital.ai.model.employeeOnboardDetails;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeeOnBoardDetails")
    private List<EmployeeOnBoardDetailsItem> employeeOnBoardDetails;

    public List<EmployeeOnBoardDetailsItem> getEmployeeOnBoardDetails() {
        return employeeOnBoardDetails;
    }
}