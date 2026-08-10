package app.xedigital.ai.model.regularizeUpdateStatus;

import com.google.gson.annotations.SerializedName;

public class Component {

    @SerializedName("_id")
    private String id;

    @SerializedName("earningname")
    private String earningname;

    @SerializedName("calculationType")
    private String calculationType;

    @SerializedName("componentCalculationType")
    private String componentCalculationType;

    @SerializedName("componentType")
    private String componentType;

    @SerializedName("percentage")
    private Double percentage;

    @SerializedName("fixedAmount")
    private Double fixedAmount;

    @SerializedName("monthlyAmount")
    private double monthlyAmount;

    @SerializedName("yearlyAmount")
    private double yearlyAmount;

    // Getters
    public String getId() {
        return id;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public String getEarningname() {
        return earningname;
    }

    public void setEarningname(String earningname) {
        this.earningname = earningname;
    }

    public String getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    public String getComponentCalculationType() {
        return componentCalculationType;
    }

    public void setComponentCalculationType(String componentCalculationType) {
        this.componentCalculationType = componentCalculationType;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Double getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(Double fixedAmount) {
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
}