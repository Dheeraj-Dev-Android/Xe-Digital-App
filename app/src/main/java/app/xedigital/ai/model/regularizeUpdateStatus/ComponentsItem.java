package app.xedigital.ai.model.regularizeUpdateStatus;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ComponentsItem implements Serializable {

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
    private double monthlyAmount;

    @SerializedName("yearlyAmount")
    private double yearlyAmount;

    @SerializedName("earningname")
    private String earningname;

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public void setComponentCalculationType(String componentCalculationType) {
        this.componentCalculationType = componentCalculationType;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFixedAmount(Object fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public void setMonthlyAmount(double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public void setYearlyAmount(double yearlyAmount) {
        this.yearlyAmount = yearlyAmount;
    }

    public void setEarningname(String earningname) {
        this.earningname = earningname;
    }
}