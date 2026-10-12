package app.xedigital.ai.admin.adminModal.SignInGraph;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("signInVisitors")
    private SignInVisitors signInVisitors;

    public SignInVisitors getSignInVisitors() {
        return signInVisitors;
    }
}