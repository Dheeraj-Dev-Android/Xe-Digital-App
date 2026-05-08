package app.xedigital.ai.model.AttandanceByManager;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeePunchData")
    private List<EmployeePunchDataItem> employeePunchData;

    public List<EmployeePunchDataItem> getEmployeePunchData() {
        return employeePunchData;
    }
}