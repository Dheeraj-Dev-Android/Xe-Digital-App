package app.xedigital.ai.model.claimPrice;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("claimPrices")
    private List<ClaimPricesItem> claimPrices;

    public List<ClaimPricesItem> getClaimPrices() {
        return claimPrices;
    }
}