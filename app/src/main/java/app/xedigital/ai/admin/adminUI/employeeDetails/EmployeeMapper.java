package app.xedigital.ai.admin.adminUI.employeeDetails;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;

public class EmployeeMapper {

    private static final Gson gson = new Gson();

    // ============================================================
    //  Mapper for employeeFaceAdd.Employee
    // ============================================================

    public static app.xedigital.ai.admin.adminModal.employeeFaceAdd.Employee mapToEmployeeFaceAdd(EmployeesItem source) {
        if (source == null) return null;

        app.xedigital.ai.admin.adminModal.employeeFaceAdd.Employee target =
                new app.xedigital.ai.admin.adminModal.employeeFaceAdd.Employee();

        target.setId(source.getId());
        target.setFirstname(source.getFirstname());
        target.setLastname(source.getLastname());
        target.setEmail(source.getEmail());
        target.setContact(source.getContact());
        target.setEmployeeCode(source.getEmployeeCode());
        target.setDesignation(source.getDesignation());
        target.setJoiningDate(source.getJoiningDate());
        target.setJoiningType(source.getJoiningType());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setFatherName(source.getFatherName());
        target.setAddress(source.getAddress());
        target.setState(source.getState());
        target.setPincode(source.getPincode());
        target.setPanNo(source.getPanNo());
        target.setAdharNo(source.getAdharNo());
        target.setPfAccountNo(source.getPfAccountNo());
        target.setUanno(source.getUanno());
        target.setLevel(source.getLevel());
        target.setGrade(source.getGrade());
        target.setDifferentlyAbled(source.getDifferentlyAbled());
        target.setEmployeeType(source.getEmployeeType());
        target.setCompany(source.getCompany());
        target.setCtc(source.getCtc());
        target.setV(source.getV());
        target.setActive(source.isActive());
        target.setIsVerified(source.isIsVerified());
        target.setIsHROrAdmin(source.isIsHROrAdmin());
        target.setEpf(source.isEpf());
        target.setEsi(source.isEsi());
        target.setProfileImageUrl(source.getProfileImageUrl());
        target.setBu(null);

        // Object → boolean (addpayroll)
        try {
            Object ap = source.getAddpayroll();
            target.setAddpayroll(ap != null && Boolean.parseBoolean(ap.toString()));
        } catch (Exception e) {
            target.setAddpayroll(false);
        }

        // Object → int (totalMonthlySalary)
        try {
            Object tms = source.getTotalMonthlySalary();
            target.setTotalMonthlySalary(tms != null ? Integer.parseInt(tms.toString()) : 0);
        } catch (Exception e) {
            target.setTotalMonthlySalary(0);
        }

        // Object → int (totalYearlySalary)
        try {
            Object tys = source.getTotalYearlySalary();
            target.setTotalYearlySalary(tys != null ? Integer.parseInt(tys.toString()) : 0);
        } catch (Exception e) {
            target.setTotalYearlySalary(0);
        }

        // Object → Object (partner) — pass through
        target.setPartner(source.getPartner());

        // List<ComponentsItem> → List<Object> — safe upcast
        if (source.getComponents() != null) {
            target.setComponents(new ArrayList<>(source.getComponents()));
        }

        // Nested: Shift
        if (source.getShift() != null) {
            app.xedigital.ai.admin.adminModal.employeeFaceAdd.Shift shift =
                    new app.xedigital.ai.admin.adminModal.employeeFaceAdd.Shift();
            shift.setName(source.getShift().getName());
            shift.setStartTime(source.getShift().getStartTime());
            shift.setEndTime(source.getShift().getEndTime());
            shift.setActive(source.getShift().isActive());
            target.setShift(shift);
        }

        // Nested: Department
        if (source.getDepartment() != null) {
            app.xedigital.ai.admin.adminModal.employeeFaceAdd.Department dept =
                    new app.xedigital.ai.admin.adminModal.employeeFaceAdd.Department();
            dept.setName(source.getDepartment().getName());
            dept.setId(source.getDepartment().getId());
            target.setDepartment(dept);
        }

        // Nested: ReportingManager
        if (source.getReportingManager() != null) {
            app.xedigital.ai.admin.adminModal.employeeFaceAdd.ReportingManager rm =
                    new app.xedigital.ai.admin.adminModal.employeeFaceAdd.ReportingManager();
            rm.setId(source.getReportingManager().getId());
            rm.setFirstname(source.getReportingManager().getFirstname());
            rm.setLastname(source.getReportingManager().getLastname());
            rm.setEmail(source.getReportingManager().getEmail());
            target.setReportingManager(rm);
        }

        // Nested: Crossmanager
        if (source.getCrossmanager() != null) {
            app.xedigital.ai.admin.adminModal.employeeFaceAdd.Crossmanager cm =
                    new app.xedigital.ai.admin.adminModal.employeeFaceAdd.Crossmanager();
            cm.setId(source.getCrossmanager().getId());
            cm.setFirstname(source.getCrossmanager().getFirstname());
            cm.setLastname(source.getCrossmanager().getLastname());
            cm.setEmail(source.getCrossmanager().getEmail());
            target.setCrossmanager(cm);
        }

        return target;
    }

    // ============================================================
    //  Mapper for employeeProfileImage.Employee
    // ============================================================

    public static app.xedigital.ai.admin.adminModal.employeeProfileImage.Employee mapToEmployeeProfileImage(EmployeesItem source) {
        if (source == null) return null;

        app.xedigital.ai.admin.adminModal.employeeProfileImage.Employee target =
                new app.xedigital.ai.admin.adminModal.employeeProfileImage.Employee();

        target.setId(source.getId());
        target.setFirstname(source.getFirstname());
        target.setLastname(source.getLastname());
        target.setEmail(source.getEmail());
        target.setContact(source.getContact());
        target.setEmployeeCode(source.getEmployeeCode());
        target.setDesignation(source.getDesignation());
        target.setJoiningDate(source.getJoiningDate());
        target.setJoiningType(source.getJoiningType());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setFatherName(source.getFatherName());
        target.setAddress(source.getAddress());
        target.setState(source.getState());
        target.setPincode(source.getPincode());
        target.setPanNo(source.getPanNo());
        target.setAdharNo(source.getAdharNo());
        target.setPfAccountNo(source.getPfAccountNo());
        target.setUanno(source.getUanno());
        target.setLevel(source.getLevel());
        target.setGrade(source.getGrade());
        target.setDifferentlyAbled(source.getDifferentlyAbled());
        target.setEmployeeType(source.getEmployeeType());
        target.setCompany(source.getCompany());
        target.setCtc(source.getCtc());
        target.setV(source.getV());
        target.setActive(source.isActive());
        target.setIsVerified(source.isIsVerified());
        target.setIsHROrAdmin(source.isIsHROrAdmin());
        target.setEpf(source.isEpf());
        target.setEsi(source.isEsi());
        target.setProfileImageUrl(source.getProfileImageUrl());
        target.setBu(null);

        // Object → boolean (addpayroll)
        try {
            Object ap = source.getAddpayroll();
            target.setAddpayroll(ap != null && Boolean.parseBoolean(ap.toString()));
        } catch (Exception e) {
            target.setAddpayroll(false);
        }

        // Object → Object (totalMonthlySalary) — pass through directly
        target.setTotalMonthlySalary(source.getTotalMonthlySalary());

        // Object → Object (totalYearlySalary) — pass through directly
        target.setTotalYearlySalary(source.getTotalYearlySalary());

        // FIX: Object → Partner (use Gson to safely convert)
        try {
            Object rawPartner = source.getPartner();
            if (rawPartner != null) {
                String json = gson.toJson(rawPartner);
                app.xedigital.ai.admin.adminModal.employeeProfileImage.Partner partner =
                        gson.fromJson(json, app.xedigital.ai.admin.adminModal.employeeProfileImage.Partner.class);
                target.setPartner(partner);
            } else {
                target.setPartner(null);
            }
        } catch (Exception e) {
            target.setPartner(null);
        }

        // FIX: List<EmployeeDetails.ComponentsItem> → List<employeeProfileImage.ComponentsItem> (use Gson)
        try {
            if (source.getComponents() != null && !source.getComponents().isEmpty()) {
                String json = gson.toJson(source.getComponents());
                List<app.xedigital.ai.admin.adminModal.employeeProfileImage.ComponentsItem> components =
                        gson.fromJson(json, new com.google.gson.reflect.TypeToken<List<app.xedigital.ai.admin.adminModal.employeeProfileImage.ComponentsItem>>() {
                        }.getType());
                target.setComponents(components);
            } else {
                target.setComponents(new ArrayList<>());
            }
        } catch (Exception e) {
            target.setComponents(new ArrayList<>());
        }

        // Nested: Shift
        if (source.getShift() != null) {
            app.xedigital.ai.admin.adminModal.employeeProfileImage.Shift shift =
                    new app.xedigital.ai.admin.adminModal.employeeProfileImage.Shift();
            shift.setName(source.getShift().getName());
            shift.setStartTime(source.getShift().getStartTime());
            shift.setEndTime(source.getShift().getEndTime());
            shift.setActive(source.getShift().isActive());
            target.setShift(shift);
        }

        // Nested: Department
        if (source.getDepartment() != null) {
            app.xedigital.ai.admin.adminModal.employeeProfileImage.Department dept =
                    new app.xedigital.ai.admin.adminModal.employeeProfileImage.Department();
            dept.setName(source.getDepartment().getName());
            dept.setId(source.getDepartment().getId());
            target.setDepartment(dept);
        }

        // Nested: ReportingManager
        if (source.getReportingManager() != null) {
            app.xedigital.ai.admin.adminModal.employeeProfileImage.ReportingManager rm =
                    new app.xedigital.ai.admin.adminModal.employeeProfileImage.ReportingManager();
            rm.setId(source.getReportingManager().getId());
            rm.setFirstname(source.getReportingManager().getFirstname());
            rm.setLastname(source.getReportingManager().getLastname());
            rm.setEmail(source.getReportingManager().getEmail());
            target.setReportingManager(rm);
        }

        // Nested: Crossmanager
        if (source.getCrossmanager() != null) {
            app.xedigital.ai.admin.adminModal.employeeProfileImage.Crossmanager cm =
                    new app.xedigital.ai.admin.adminModal.employeeProfileImage.Crossmanager();
            cm.setId(source.getCrossmanager().getId());
            cm.setFirstname(source.getCrossmanager().getFirstname());
            cm.setLastname(source.getCrossmanager().getLastname());
            cm.setEmail(source.getCrossmanager().getEmail());
            target.setCrossmanager(cm);
        }

        return target;
    }
}