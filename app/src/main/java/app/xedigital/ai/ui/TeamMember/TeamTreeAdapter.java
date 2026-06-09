package app.xedigital.ai.ui.TeamMember;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.TeamMember.AllEmployeesItem;
import app.xedigital.ai.model.TeamMember.EmployeesItem;

public class TeamTreeAdapter extends RecyclerView.Adapter<TeamTreeAdapter.TreeViewHolder> {

    private final List<TreeNode> visibleList = new ArrayList<>();
    private OnMemberClickListener listener;

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMergedData(List<EmployeesItem> fromEmployees, List<AllEmployeesItem> fromAll, RelationType type) {
        visibleList.clear();
        if (fromEmployees != null) {
            for (EmployeesItem emp : fromEmployees) {
                if (emp == null) continue;
                visibleList.add(mapEmployeesItem(emp, type));
            }
        }
        if (fromAll != null) {
            for (AllEmployeesItem emp : fromAll) {
                if (emp == null) continue;
                visibleList.add(mapAllEmployeesItem(emp, type));
            }
        }
        notifyDataSetChanged();
    }

    private TreeNode mapEmployeesItem(EmployeesItem emp, RelationType type) {
        String name = trim(emp.getFirstname()) + " " + trim(emp.getLastname());
        String dept = emp.getDepartment() != null ? trim(emp.getDepartment().getName()) : "";
        String repMgr = emp.getReportingManager() != null ?
                trim(emp.getReportingManager().getFirstname()) + " " + trim(emp.getReportingManager().getLastname()) : "N/A";
        String crossMgr = emp.getCrossmanager() != null ?
                trim(emp.getCrossmanager().getFirstname()) + " " + trim(emp.getCrossmanager().getLastname()) : "N/A";
        String empId = emp.getEmployeeCode() != null ? trim(emp.getEmployeeCode()) : "N/A";

        String shift = emp.getShift() != null ? trim(emp.getShift().getStartTime() + "-" + emp.getShift().getEndTime()) : "Not Assigned";

        return new TreeNode(name, trim(emp.getDesignation()), dept, type,
                trim(emp.getId()), emp.getEmail(), trim(emp.getContact()),
                repMgr, crossMgr, shift, empId);
    }

    private TreeNode mapAllEmployeesItem(AllEmployeesItem emp, RelationType type) {
        String name = trim(emp.getFirstname()) + " " + trim(emp.getLastname());
        String dept = emp.getDepartment() != null ? trim(emp.getDepartment().getName()) : "";
        String repMgr = emp.getReportingManager() != null ?
                trim(emp.getReportingManager().getFirstname()) + " " + trim(emp.getReportingManager().getLastname()) : "N/A";
        String crossMgr = emp.getCrossmanager() != null ?
                trim(emp.getCrossmanager().getFirstname()) + " " + trim(emp.getCrossmanager().getLastname()) : "N/A";
        String empId = emp.getEmployeeCode();

        String shift = emp.getShift() != null ? trim(emp.getShift().getStartTime() + "-" + emp.getShift().getEndTime()) : "Not Assigned";

        return new TreeNode(name, trim(emp.getDesignation()), dept, type,
                trim(emp.getId()), emp.getEmail(), trim(emp.getContact()),
                repMgr, crossMgr, shift, empId);
    }

    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_tree, parent, false);
        return new TreeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder holder, int position) {
        TreeNode node = visibleList.get(position);
        holder.name.setText(node.name);
        holder.role.setText(node.department.isEmpty() ? node.role : node.role + " · " + node.department);
        holder.avatarInitials.setText(getInitials(node.name));

        if (node.relation == RelationType.DIRECT) {
            setAvatarStyle(holder, 0xFFE1F5EE, 0xFF0F6E56, "● Direct report", 0xFF9FE1CB);
        } else {
            setAvatarStyle(holder, 0xFFFAEEDA, 0xFF854F0B, "◆ Cross report", 0xFFFAC775);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION) {
                listener.onMemberClick(visibleList.get(currentPos));
            }
        });
    }

    private void setAvatarStyle(TreeViewHolder holder, int bgColor, int textColor, String label, int strokeColor) {
        Drawable bg = holder.avatarInitials.getBackground();
        if (bg instanceof GradientDrawable) ((GradientDrawable) bg).setColor(bgColor);
        holder.avatarInitials.setTextColor(textColor);
        holder.managerType.setText(label);
        holder.managerType.setTextColor(textColor);
        holder.managerType.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
        holder.managerType.setChipStrokeColor(ColorStateList.valueOf(strokeColor));
        holder.managerType.setChipStrokeWidth(1f);
    }

    @Override
    public int getItemCount() {
        return visibleList.size();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2 && !parts[1].isEmpty()) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return ("" + parts[0].charAt(0)).toUpperCase();
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }

    public enum RelationType {DIRECT, CROSS}

    public interface OnMemberClickListener {
        void onMemberClick(TreeNode node);
    }

    public static class TreeNode {
        public String name, role, department, id, email, contact,
                reportingManager, crossManager, shiftTiming, empId;
        public RelationType relation;

        public TreeNode(String name, String role, String department, RelationType relation,
                        String id, String email, String contact, String reportingManager,
                        String crossManager, String shiftTiming, String empId) {
            this.name = name;
            this.role = role;
            this.department = department;
            this.relation = relation;
            this.id = id;
            this.empId = empId;
            this.email = email;
            this.contact = contact;
            this.reportingManager = reportingManager;
            this.crossManager = crossManager;
            this.shiftTiming = shiftTiming;
        }
    }

    static class TreeViewHolder extends RecyclerView.ViewHolder {
        TextView avatarInitials, name, role;
        Chip managerType;

        TreeViewHolder(View v) {
            super(v);
            avatarInitials = v.findViewById(R.id.avatar_initials);
            name = v.findViewById(R.id.employee_name);
            role = v.findViewById(R.id.employee_role);
            managerType = v.findViewById(R.id.manager_type);
        }
    }
}