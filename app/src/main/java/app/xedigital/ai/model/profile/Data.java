package app.xedigital.ai.model.profile;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("employee")
    private Employee employee;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}