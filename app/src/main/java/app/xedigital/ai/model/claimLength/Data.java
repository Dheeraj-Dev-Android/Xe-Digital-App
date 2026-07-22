package app.xedigital.ai.model.claimLength;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("employeeCount")
    private int employeeCount;

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }
}