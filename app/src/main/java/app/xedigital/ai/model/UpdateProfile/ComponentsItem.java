package app.xedigital.ai.model.UpdateProfile;

import com.google.gson.annotations.SerializedName;

public class ComponentsItem {

    @SerializedName("calculationType")
    private String calculationType;

    @SerializedName("componentType")
    private String componentType;

    @SerializedName("componentCalculationType")
    private String componentCalculationType;

    @SerializedName("percentage")
    private int percentage;

    @SerializedName("_id")
    private String id;

    @SerializedName("fixedAmount")
    private Object fixedAmount;

    @SerializedName("monthlyAmount")
    private Object monthlyAmount;

    @SerializedName("yearlyAmount")
    private int yearlyAmount;

    @SerializedName("earningname")
    private String earningname;

    public String getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentCalculationType() {
        return componentCalculationType;
    }

    public void setComponentCalculationType(String componentCalculationType) {
        this.componentCalculationType = componentCalculationType;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(Object fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public Object getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(Object monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public int getYearlyAmount() {
        return yearlyAmount;
    }

    public void setYearlyAmount(int yearlyAmount) {
        this.yearlyAmount = yearlyAmount;
    }

    public String getEarningname() {
        return earningname;
    }

    public void setEarningname(String earningname) {
        this.earningname = earningname;
    }
}