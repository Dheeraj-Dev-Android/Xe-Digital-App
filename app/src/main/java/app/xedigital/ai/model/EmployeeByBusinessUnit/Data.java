package app.xedigital.ai.model.EmployeeByBusinessUnit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("result")
    private List<ResultItem> result;

    public List<ResultItem> getResult() {
        return result;
    }
}