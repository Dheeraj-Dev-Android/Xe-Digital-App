package app.xedigital.ai.model.attendance;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeePunchData")
    private List<EmployeePunchDataItem> employeePunchData;

    public List<EmployeePunchDataItem> getEmployeePunchData() {
        return employeePunchData;
    }

    public void setEmployeePunchData(List<EmployeePunchDataItem> employeePunchData) {
        this.employeePunchData = employeePunchData;
    }
}