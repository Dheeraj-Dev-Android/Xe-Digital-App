package app.xedigital.ai.model.TeamMember;

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

    // Changed from Object to double
    @SerializedName("fixedAmount")
    private double fixedAmount;

    // Changed from Object to double
    @SerializedName("monthlyAmount")
    private double monthlyAmount;

    // CRITICAL FIX: Changed from int to double
    @SerializedName("yearlyAmount")
    private double yearlyAmount;

    @SerializedName("earningname")
    private String earningname;

    public String getCalculationType() {
        return calculationType;
    }

    public String getComponentType() {
        return componentType;
    }

    public String getComponentCalculationType() {
        return componentCalculationType;
    }

    public int getPercentage() {
        return percentage;
    }

    public String getId() {
        return id;
    }

    public double getFixedAmount() {
        return fixedAmount;
    }

    public double getMonthlyAmount() {
        return monthlyAmount;
    }

    public double getYearlyAmount() {
        return yearlyAmount;
    }

    public String getEarningname() {
        return earningname;
    }
}