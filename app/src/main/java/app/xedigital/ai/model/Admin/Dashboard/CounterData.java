package app.xedigital.ai.model.Admin.Dashboard;

import com.google.gson.annotations.SerializedName;

public class CounterData {

    @SerializedName("totalVisitors")
    private int totalVisitors;

    @SerializedName("totalBranches")
    private int totalBranches;

    @SerializedName("totalSigninVisitors")
    private int totalSigninVisitors;

    @SerializedName("totalSignoutVisitors")
    private int totalSignoutVisitors;

    @SerializedName("totalDepartments")
    private int totalDepartments;

    @SerializedName("totalEmployees")
    private int totalEmployees;

    public int getTotalVisitors() {
        return totalVisitors;
    }

    public int getTotalBranches() {
        return totalBranches;
    }

    public int getTotalSigninVisitors() {
        return totalSigninVisitors;
    }

    public int getTotalSignoutVisitors() {
        return totalSignoutVisitors;
    }

    public int getTotalDepartments() {
        return totalDepartments;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }
}