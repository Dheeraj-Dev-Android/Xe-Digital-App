package app.xedigital.ai.admin.adminModal.partners;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("partners")
    private List<PartnersItem> partners;

    public List<PartnersItem> getPartners() {
        return partners;
    }
}