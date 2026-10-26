package app.xedigital.ai.model.claimLimit;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("claimBackDateLimit")
    private int claimBackDateLimit;

    public int getClaimBackDateLimit() {
        return claimBackDateLimit;
    }
}