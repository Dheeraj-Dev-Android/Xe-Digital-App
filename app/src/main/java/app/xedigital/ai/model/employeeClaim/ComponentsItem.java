package app.xedigital.ai.model.employeeClaim;

import java.io.Serializable;

public class ComponentsItem implements Serializable {
    private String calculationType;
    private String componentType;
    private String componentCalculationType;
    private int percentage;
    private String id;
    private Object fixedAmount;
    private double monthlyAmount;
    private double yearlyAmount;
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
