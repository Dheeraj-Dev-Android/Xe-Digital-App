package app.xedigital.ai.model.login;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("expiresIn")
    private String expiresIn;

    @SerializedName("user")
    private User user;

    @SerializedName("token")
    private String token;

    @SerializedName("refreshToken")
    private String refreshToken;

    public String getExpiresIn() {
        return expiresIn;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}