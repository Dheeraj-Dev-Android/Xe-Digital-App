package app.xedigital.ai.model.claimLength;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("cliamlength")
    private int cliamlength;

    public int getCliamlength() {
        return cliamlength;
    }

    public void setCliamlength(int cliamlength) {
        this.cliamlength = cliamlength;
    }
}