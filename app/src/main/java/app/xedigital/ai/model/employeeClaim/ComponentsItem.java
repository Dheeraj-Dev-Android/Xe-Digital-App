package app.xedigital.ai.model.employeeClaim;

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
    private int monthlyAmount;

    @SerializedName("yearlyAmount")
    private int yearlyAmount;

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

    public Object getFixedAmount() {
        return fixedAmount;
    }

    public int getMonthlyAmount() {
        return monthlyAmount;
    }

    public int getYearlyAmount() {
        return yearlyAmount;
    }

    public String getEarningname() {
        return earningname;
    }
}