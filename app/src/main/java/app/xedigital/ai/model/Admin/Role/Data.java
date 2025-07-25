package app.xedigital.ai.model.Admin.Role;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("roles")
    private List<RolesItem> roles;

    public List<RolesItem> getRoles() {
        return roles;
    }
}