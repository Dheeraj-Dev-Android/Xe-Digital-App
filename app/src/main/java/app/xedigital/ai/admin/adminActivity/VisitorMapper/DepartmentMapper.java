package app.xedigital.ai.admin.adminActivity.VisitorMapper;

import app.xedigital.ai.admin.adminModal.VisitorManual.Department;

public class DepartmentMapper {
    public static Department mapFromEmployeeDetailsDepartment(app.xedigital.ai.admin.adminModal.EmployeeDetails.Department department) {
        Department departmentToSet = new Department();
        if (department != null) {
            departmentToSet.setId(department.getId());
            departmentToSet.setName(department.getName());
            departmentToSet.setDescription(department.getDescription());
            departmentToSet.setActive(department.isActive());
            departmentToSet.setJsonMemberDefault(department.isJsonMemberDefault());
        }
        return departmentToSet;
    }
}
