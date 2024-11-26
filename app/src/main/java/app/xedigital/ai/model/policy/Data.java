package app.xedigital.ai.model.policy;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("policies")
    private List<PoliciesItem> policies;

    public List<PoliciesItem> getPolicies() {
        return policies;
    }
}