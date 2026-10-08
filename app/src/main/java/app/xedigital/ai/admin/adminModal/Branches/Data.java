package app.xedigital.ai.admin.adminModal.Branches;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("branches")
    private List<BranchesItem> branches;

    public List<BranchesItem> getBranches() {
        return branches;
    }
}