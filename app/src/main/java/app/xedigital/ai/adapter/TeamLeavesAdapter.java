package app.xedigital.ai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.TeamLeave.EmployeesItem;
import app.xedigital.ai.model.TeamLeave.LeavesItem;

public class TeamLeavesAdapter extends RecyclerView.Adapter<TeamLeavesAdapter.EmployeeViewHolder> {

    private List<EmployeesItem> employees;
    private final Context context;

    public TeamLeavesAdapter(Context context, List<EmployeesItem> employees) {
        this.context = context;
        this.employees = employees;
    }

    public void updateList(List<EmployeesItem> newList) {
        this.employees = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_leave, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        EmployeesItem employee = employees.get(position);
        Context containerContext = holder.llLeavesContainer.getContext();
        LayoutInflater inflater = LayoutInflater.from(containerContext);

        // Avatar initials
        String initials = getInitials(employee.getEmployeeName());
        holder.tvAvatar.setText(initials);

        holder.tvName.setText(employee.getEmployeeName());
        holder.tvEmail.setText(employee.getEmail());

        // Clear previous leave rows to avoid duplication during view recycling
        holder.llLeavesContainer.removeAllViews();

        // Track if at least one current year row was added for this employee
        boolean hasCurrentYearLeaves = false;

        // Populate leave rows
        if (employee.getLeaves() != null && !employee.getLeaves().isEmpty()) {

            // Get the current system year dynamically (e.g., 2026)
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            for (LeavesItem leave : employee.getLeaves()) {

                // Extract year from assignDate (Format example: "2026-05-29T05:57:31.565Z")
                String assignDate = leave.getAssignDate();
                int recordYear = 0;

                if (assignDate != null && assignDate.length() >= 4) {
                    try {
                        recordYear = Integer.parseInt(assignDate.substring(0, 4));
                    } catch (NumberFormatException e) {
                        recordYear = 0;
                    }
                }

                // Filter: Only allow records matching the current year
                if (recordYear != currentYear) {
                    continue;
                }

                hasCurrentYearLeaves = true;

                View leaveRow = inflater.inflate(R.layout.item_leave_row, holder.llLeavesContainer, false);

                TextView tvLeaveType = leaveRow.findViewById(R.id.tv_leave_type);
                TextView tvOpening = leaveRow.findViewById(R.id.tv_opening);
                TextView tvCredited = leaveRow.findViewById(R.id.tv_credited);
                TextView tvUsed = leaveRow.findViewById(R.id.tv_used);
                TextView tvBalance = leaveRow.findViewById(R.id.tv_balance);
                ProgressBar pbUsage = leaveRow.findViewById(R.id.pb_leave_usage);

                String leaveType = leave.getLeavetype();
                tvLeaveType.setText(leaveType != null ? leaveType : "Unspecified Leave");
                tvOpening.setText(String.valueOf(leave.getOpeningLeave()));
                tvCredited.setText(String.valueOf(leave.getCreditLeave()));
                tvUsed.setText(String.valueOf(leave.getDebitLeave()));

                double balance = leave.getCreditLeave() - leave.getDebitLeave();
                tvBalance.setText(String.valueOf(balance));

                int balanceColor = balance > 0
                        ? ContextCompat.getColor(containerContext, R.color.leave_balance_positive)
                        : ContextCompat.getColor(containerContext, R.color.leave_balance_zero);
                tvBalance.setTextColor(balanceColor);

                double totalAllotted = leave.getCreditLeave();
                if (totalAllotted > 0) {
                    int usedPct = (int) ((leave.getUsedLeave() / (float) totalAllotted) * 100);
                    pbUsage.setProgress(Math.min(usedPct, 100));
                } else {
                    pbUsage.setProgress(0);
                }

                holder.llLeavesContainer.addView(leaveRow);
            }
        }

        // If employee has no leaves at all OR none match the current year, display fallback item
        if (!hasCurrentYearLeaves) {
            View leaveRow = inflater.inflate(R.layout.item_leave_row, holder.llLeavesContainer, false);
            TextView tvLeaveType = leaveRow.findViewById(R.id.tv_leave_type);

            tvLeaveType.setText("No entries for " + Calendar.getInstance().get(Calendar.YEAR));
            leaveRow.findViewById(R.id.pb_leave_usage).setVisibility(View.GONE);

            holder.llLeavesContainer.addView(leaveRow);
        }
    }

    @Override
    public int getItemCount() {
        return employees != null ? employees.size() : 0;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0) return "?";
        if (parts.length == 1)
            return parts[0].isEmpty() ? "?" : parts[0].substring(0, 1).toUpperCase();

        String firstInitial = parts[0].isEmpty() ? "" : parts[0].substring(0, 1);
        String lastInitial = parts[parts.length - 1].isEmpty() ? "" : parts[parts.length - 1].substring(0, 1);

        String initials = (firstInitial + lastInitial).toUpperCase();
        return initials.isEmpty() ? "?" : initials;
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvEmail;
        LinearLayout llLeavesContainer;

        EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvName = itemView.findViewById(R.id.tv_employee_name);
            tvEmail = itemView.findViewById(R.id.tv_employee_email);
            llLeavesContainer = itemView.findViewById(R.id.ll_leaves_container);
        }
    }
}