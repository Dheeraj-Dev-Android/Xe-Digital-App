package app.xedigital.ai.model.Admin.EmployeeLeaves;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("leaves")
    private List<LeavesItem> leaves;

    public List<LeavesItem> getLeaves() {
        return leaves;
    }
}