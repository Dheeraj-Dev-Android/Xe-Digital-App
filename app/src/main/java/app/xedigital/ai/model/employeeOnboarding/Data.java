package app.xedigital.ai.model.employeeOnboarding;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("employeeOnBoardDetails")
    private EmployeeOnBoardDetails employeeOnBoardDetails;

    public EmployeeOnBoardDetails getEmployeeOnBoardDetails() {
        return employeeOnBoardDetails;
    }
}