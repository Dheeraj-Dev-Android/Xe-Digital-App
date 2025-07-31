package app.xedigital.ai.model.Admin.department;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("departments")
    private List<DepartmentsItem> departments;

    public List<DepartmentsItem> getDepartments() {
        return departments;
    }
}