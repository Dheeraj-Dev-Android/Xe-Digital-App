package app.xedigital.ai.admin.adminModal.empTimesheet;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeeDcr")
    private List<EmployeeDcrItem> employeeDcr;

    public List<EmployeeDcrItem> getEmployeeDcr() {
        return employeeDcr;
    }
}