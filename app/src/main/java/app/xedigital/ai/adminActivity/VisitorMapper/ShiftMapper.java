package app.xedigital.ai.adminActivity.VisitorMapper;

import app.xedigital.ai.model.Admin.VisitorManual.Shift;

public class ShiftMapper {

    public static Shift mapFromEmployeeDetailsShift(app.xedigital.ai.model.Admin.EmployeeDetails.Shift shift) {
        Shift shiftToSet = new Shift();

        if (shift != null) {
            shiftToSet.setId(shift.getId());
            shiftToSet.setName(shift.getName());
            shiftToSet.setStartTime(shift.getStartTime());
            shiftToSet.setEndTime(shift.getEndTime());
            shiftToSet.setFormat(shift.getFormat());
            shiftToSet.setActive(shift.isActive());
            shiftToSet.setTimeWaiver(shift.getTimeWaiver());
        }

        return shiftToSet;
    }
}