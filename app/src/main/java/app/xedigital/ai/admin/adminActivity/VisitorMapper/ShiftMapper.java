package app.xedigital.ai.admin.adminActivity.VisitorMapper;

import app.xedigital.ai.admin.adminModal.VisitorManual.Shift;

public class ShiftMapper {

    public static Shift mapFromEmployeeDetailsShift(app.xedigital.ai.admin.adminModal.EmployeeDetails.Shift shift) {
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