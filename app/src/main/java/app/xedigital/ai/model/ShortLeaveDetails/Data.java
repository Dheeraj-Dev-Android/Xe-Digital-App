package app.xedigital.ai.model.ShortLeaveDetails;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("shortLeaveExemption")
    private int shortLeaveExemption;

    @SerializedName("shortLeaveCount")
    private int shortLeaveCount;

    @SerializedName("confirmEmpShortLeave")
    private boolean confirmEmpShortLeave;

    @SerializedName("eveningShortLeave")
    private boolean eveningShortLeave;

    @SerializedName("probationEmpShortLeave")
    private boolean probationEmpShortLeave;

    @SerializedName("morningShortLeave")
    private boolean morningShortLeave;

    @SerializedName("shortLeave")
    private boolean shortLeave;

    public int getShortLeaveExemption() {
        return shortLeaveExemption;
    }

    public int getShortLeaveCount() {
        return shortLeaveCount;
    }

    public boolean isConfirmEmpShortLeave() {
        return confirmEmpShortLeave;
    }

    public boolean isEveningShortLeave() {
        return eveningShortLeave;
    }

    public boolean isProbationEmpShortLeave() {
        return probationEmpShortLeave;
    }

    public boolean isMorningShortLeave() {
        return morningShortLeave;
    }

    public boolean isShortLeave() {
        return shortLeave;
    }
}