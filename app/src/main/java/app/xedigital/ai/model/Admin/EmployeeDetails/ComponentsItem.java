package app.xedigital.ai.model.Admin.EmployeeDetails;

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

    public String getComponentType() {
        return componentType;
    }

    public String getComponentCalculationType() {
        return componentCalculationType;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getId() {
        return id;
    }

    public Object getFixedAmount() {
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