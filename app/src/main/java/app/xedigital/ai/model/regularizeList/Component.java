package app.xedigital.ai.model.regularizeList;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Component implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("earningname")
    private String earningname;

    @SerializedName("calculationType")
    private String calculationType;

    @SerializedName("componentCalculationType")
    private String componentCalculationType; // nullable

    @SerializedName("componentType")
    private String componentType;

    @SerializedName("percentage")
    private Double percentage;   // Double (nullable) not double

    @SerializedName("fixedAmount")
    private Double fixedAmount;  // Double (nullable) not double

    @SerializedName("monthlyAmount")
    private double monthlyAmount;

    @SerializedName("yearlyAmount")
    private double yearlyAmount;

    public String getId() {
        return id;
    }

    public String getEarningname() {
        return earningname;
    }

    public String getCalculationType() {
        return calculationType;
    }

    public String getComponentCalculationType() {
        return componentCalculationType;
    }

    public String getComponentType() {
        return componentType;
    }

    public Double getPercentage() {
        return percentage;
    }

    public Double getFixedAmount() {
        return fixedAmount;
    }

    public double getMonthlyAmount() {
        return monthlyAmount;
    }

    public double getYearlyAmount() {
        return yearlyAmount;
    }
}