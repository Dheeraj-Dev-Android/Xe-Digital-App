package app.xedigital.ai.model.claimPrice;

import com.google.gson.annotations.SerializedName;

public class ClaimPricesItem {

    @SerializedName("shared")
    private String shared;

    @SerializedName("dedicated")
    private String dedicated;

    @SerializedName("modeoftransport")
    private String modeoftransport;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("price")
    private int price;

    @SerializedName("grade")
    private String grade;

    @SerializedName("__v")
    private int v;

    @SerializedName("individually")
    private String individually;

    @SerializedName("currency")
    private String currency;

    @SerializedName("_id")
    private String id;

    @SerializedName("travelCategory")
    private String travelCategory;

    public String getShared() {
        return shared;
    }

    public String getDedicated() {
        return dedicated;
    }

    public String getModeoftransport() {
        return modeoftransport;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getPrice() {
        return price;
    }

    public String getGrade() {
        return grade;
    }

    public int getV() {
        return v;
    }

    public String getIndividually() {
        return individually;
    }

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public String getTravelCategory() {
        return travelCategory;
    }
}