package app.xedigital.ai.adminActivity.VisitorMapper;

import app.xedigital.ai.model.Admin.VisitorManual.ReportingManager;

public class ReportingManagerMapper {

    public static ReportingManager mapFromEmployeeDetailsReportingManager(app.xedigital.ai.model.Admin.EmployeeDetails.ReportingManager reportingManager) {
        ReportingManager reportingManagerToSet = new ReportingManager();

        if (reportingManager != null) {
            reportingManagerToSet.setId(reportingManager.getId());
            reportingManagerToSet.setFirstname(reportingManager.getFirstname());
            reportingManagerToSet.setLastname(reportingManager.getLastname());
            reportingManagerToSet.setEmail(reportingManager.getEmail());
        }

        return reportingManagerToSet;
    }
}