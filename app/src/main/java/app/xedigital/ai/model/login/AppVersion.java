package app.xedigital.ai.model.login;

import com.google.gson.annotations.SerializedName;

public class AppVersion {

    @SerializedName("platform")
    private String platform;

    public String getPlatform() {
        return platform;
    }
}