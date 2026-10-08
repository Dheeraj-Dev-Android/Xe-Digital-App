package app.xedigital.ai.admin.adminModal.AdminUsers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("users")
    private List<UsersItem> users;

    public List<UsersItem> getUsers() {
        return users;
    }
}