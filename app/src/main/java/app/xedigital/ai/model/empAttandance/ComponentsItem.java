package app.xedigital.ai.model.empAttandance;

import com.google.gson.annotations.SerializedName;

public class ComponentsItem {

    @SerializedName("calculationType")
    private String calculationType;

    @SerializedName("componentType")
    private String componentType;

    @SerializedName("componentCalculationType")
    private String componentCalculationType;

    @SerializedName("percentage")
    private double percentage;

    @SerializedName("_id")
    private String id;

    @SerializedName("fixedAmount")
    private Object fixedAmount;

    @SerializedName("monthlyAmount")
    private double monthlyAmount;

    @SerializedName("yearlyAmount")
    private double yearlyAmount;

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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
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

    public double getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public double getYearlyAmount() {
        return yearlyAmount;
    }

    public void setYearlyAmount(double yearlyAmount) {
        this.yearlyAmount = yearlyAmount;
    }

    public String getEarningname() {
        return earningname;
    }

    public void setEarningname(String earningname) {
        this.earningname = earningname;
    }
}