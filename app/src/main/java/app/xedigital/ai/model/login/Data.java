package app.xedigital.ai.model.login;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("appVersion")
    private AppVersion appVersion;

    @SerializedName("user")
    private User user;

    @SerializedName("token")
    private String token;

    public AppVersion getAppVersion() {
        return appVersion;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}