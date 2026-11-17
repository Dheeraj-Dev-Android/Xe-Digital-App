package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.AttendanceRegularizationApproval.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.admin.adminModal.AttendanceRegularizationApproval.Employee;
import app.xedigital.ai.admin.adminModal.AttendanceRegularizationApproval.Shift;
import app.xedigital.ai.utills.DateTimeUtils;

public class AttendanceRegularizationApprovalAdapter extends RecyclerView.Adapter<AttendanceRegularizationApprovalAdapter.ViewHolder> {

    private final List<AttendanceRegularizeAppliedItem> allItems = new ArrayList<>();
    private final List<AttendanceRegularizeAppliedItem> displayedItems = new ArrayList<>();
    private final Set<String> expandedItemKeys = new HashSet<>();

    public AttendanceRegularizationApprovalAdapter(List<AttendanceRegularizeAppliedItem> items) {
        setItems(items);
    }

    public void setItems(List<AttendanceRegularizeAppliedItem> items) {
        this.allItems.clear();
        this.displayedItems.clear();
        this.expandedItemKeys.clear();
        if (items != null) {
            this.allItems.addAll(items);
            this.displayedItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        displayedItems.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedItems.addAll(allItems);
        } else {
            String lower = query.toLowerCase().trim();
            for (AttendanceRegularizeAppliedItem item : allItems) {
                Employee emp = item.getEmployee();
                String name = emp != null ? (emp.getFirstname() + " " + emp.getLastname()) : "";
                String email = emp != null && emp.getEmail() != null ? emp.getEmail() : "";
                String code = emp != null && emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "";

                if (name.toLowerCase().contains(lower) || email.toLowerCase().contains(lower) || code.toLowerCase().contains(lower)) {
                    displayedItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_regularization_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRegularizeAppliedItem item = displayedItems.get(position);
        if (item == null) return;

        Employee emp = item.getEmployee();
        Shift shift = item.getShift();

        // 1. Name (Summary)
        String name = emp != null ? ((emp.getFirstname() != null ? emp.getFirstname() : "") + " " + (emp.getLastname() != null ? emp.getLastname() : "")).trim() : "--";
        holder.tvName.setText(name.isEmpty() ? "--" : name);

        // 2. Email
        holder.tvEmail.setText(emp != null && emp.getEmail() != null ? emp.getEmail() : "--");

        // 3. Contact
        holder.tvContact.setText(emp != null && emp.getContact() != null ? emp.getContact() : "--");

        // 4. Type
        holder.tvType.setText(emp != null && emp.getEmployeeType() != null ? emp.getEmployeeType() : "--");

        // 5. Shift
        holder.tvShift.setText(shift != null && shift.getName() != null ? shift.getName() : "--");

        // 6. Punch Date (Formatted via DateTimeUtils)
        String rawPunchDate = item.getPunchDate();
        String formattedPunchDate = DateTimeUtils.formatToReadableDate(rawPunchDate);
        holder.tvPunchDate.setText(formattedPunchDate != null && !formattedPunchDate.isEmpty() ? formattedPunchDate : "--");

        // 7. Punch-in Time (Formatted via DateTimeUtils)
        String rawPunchIn = item.getPunchIn();
        holder.tvPunchInTime.setText(DateTimeUtils.formatTime(rawPunchIn));

        // 8. Punch-out Time (Formatted via DateTimeUtils)
        String rawPunchOut = item.getPunchOut();
        holder.tvPunchOutTime.setText(DateTimeUtils.formatTime(rawPunchOut));

        // 9. Total Time (Calculated via DateTimeUtils)
        holder.tvTotalTime.setText(DateTimeUtils.calculateTotalTime(rawPunchIn, rawPunchOut));

        // 10. Late Time (Calculated via DateTimeUtils against Shift Start Time)
        String shiftStartTime = shift != null && shift.getStartTime() != null ? shift.getStartTime() : "";
        holder.tvLateTime.setText(DateTimeUtils.calculateLateTime(rawPunchIn, shiftStartTime));

        // 11. Punch-in Address
        holder.tvPunchInAddress.setText(item.getPunchInAddress() != null ? item.getPunchInAddress() : "--");

        // 12. Punch-out Address
        holder.tvPunchOutAddress.setText(item.getPunchOutAddress() != null ? item.getPunchOutAddress() : "--");

        // 13. Punch-in Time Update (Formatted via DateTimeUtils)
        holder.tvPunchInTimeUpdate.setText(DateTimeUtils.formatTime(item.getPunchInUpdated()));

        // 14. Punch-out Time Update (Formatted via DateTimeUtils)
        holder.tvPunchOutTimeUpdate.setText(DateTimeUtils.formatTime(item.getPunchOutUpdated()));

        // 15. Punch-in Address Update
        holder.tvPunchInAddressUpdate.setText(item.getPunchInAddressUpdated() != null ? item.getPunchInAddressUpdated() : "--");

        // 16. Punch-out Address Update
        holder.tvPunchOutAddressUpdate.setText(item.getPunchOutAddressUpdated() != null ? item.getPunchOutAddressUpdated() : "--");

        // 17. Requested Date (Summary - Formatted via DateTimeUtils)
        String rawAppliedDate = item.getAppliedDate();
        String formattedAppliedDate = DateTimeUtils.formatToReadableDate(rawAppliedDate);
        holder.tvRequestedDate.setText(formattedAppliedDate != null && !formattedAppliedDate.isEmpty() ? formattedAppliedDate : "--");

        // 18. Status (Summary)
        String status = item.getStatus() != null ? item.getStatus() : "unapproved";
        holder.tvStatus.setText(status.toUpperCase());
        applyStatusBadgeStyle(holder.tvStatus, status);

        // 19. Status Updated By
        holder.tvStatusUpdatedBy.setText(item.getApprovedByName() != null ? item.getApprovedByName() : "--");

        // 20. Status Updated Date (Formatted via DateTimeUtils)
        String rawApprovedDate = item.getApprovedDate();
        String formattedApprovedDate = DateTimeUtils.formatToReadableDate(rawApprovedDate);
        holder.tvStatusUpdatedDate.setText(formattedApprovedDate != null && !formattedApprovedDate.isEmpty() ? formattedApprovedDate : "--");

        // 21. Remarks
        String remark = item.getAttendenceRegularizationRemark();
        holder.tvRemarks.setText(remark != null && !remark.trim().isEmpty() ? remark : "--");

        // 22. Actions Visibility (Only show Approve, Reject, Cancel if status is "unapproved")
        if ("unapproved".equalsIgnoreCase(status)) {
            holder.layoutActions.setVisibility(View.VISIBLE);
        } else {
            holder.layoutActions.setVisibility(View.GONE);
        }

        // 23. Expand / Collapse State
        final String itemKey = item.getId() != null ? item.getId() : String.valueOf(position);
        boolean isExpanded = expandedItemKeys.contains(itemKey);

        holder.layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.btnSeeMore.setText(isExpanded ? "See Less ▲" : "See More ▼");

        holder.btnSeeMore.setOnClickListener(v -> {
            if (expandedItemKeys.contains(itemKey)) {
                expandedItemKeys.remove(itemKey);
            } else {
                expandedItemKeys.add(itemKey);
            }
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(currentPos);
            }
        });
    }

    private void applyStatusBadgeStyle(TextView tvStatus, String status) {
        if ("APPROVED".equalsIgnoreCase(status)) {
            tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
            tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("REJECTED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
            tvStatus.setTextColor(Color.parseColor("#C62828"));
        } else {
            // Default unapproved
            tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
            tvStatus.setTextColor(Color.parseColor("#E65100"));
        }
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvContact, tvType, tvShift;
        TextView tvPunchDate, tvPunchInTime, tvPunchOutTime, tvTotalTime, tvLateTime;
        TextView tvPunchInAddress, tvPunchOutAddress;
        TextView tvPunchInTimeUpdate, tvPunchOutTimeUpdate, tvPunchInAddressUpdate, tvPunchOutAddressUpdate;
        TextView tvRequestedDate, tvStatus, tvStatusUpdatedBy, tvStatusUpdatedDate, tvRemarks;
        LinearLayout layoutDetails, layoutActions;
        Button btnSeeMore, btnApprove, btnReject, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvType = itemView.findViewById(R.id.tvType);
            tvShift = itemView.findViewById(R.id.tvShift);

            tvPunchDate = itemView.findViewById(R.id.tvPunchDate);
            tvPunchInTime = itemView.findViewById(R.id.tvPunchInTime);
            tvPunchOutTime = itemView.findViewById(R.id.tvPunchOutTime);
            tvTotalTime = itemView.findViewById(R.id.tvTotalTime);
            tvLateTime = itemView.findViewById(R.id.tvLateTime);

            tvPunchInAddress = itemView.findViewById(R.id.tvPunchInAddress);
            tvPunchOutAddress = itemView.findViewById(R.id.tvPunchOutAddress);

            tvPunchInTimeUpdate = itemView.findViewById(R.id.tvPunchInTimeUpdate);
            tvPunchOutTimeUpdate = itemView.findViewById(R.id.tvPunchOutTimeUpdate);
            tvPunchInAddressUpdate = itemView.findViewById(R.id.tvPunchInAddressUpdate);
            tvPunchOutAddressUpdate = itemView.findViewById(R.id.tvPunchOutAddressUpdate);

            tvRequestedDate = itemView.findViewById(R.id.tvRequestedDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStatusUpdatedBy = itemView.findViewById(R.id.tvStatusUpdatedBy);
            tvStatusUpdatedDate = itemView.findViewById(R.id.tvStatusUpdatedDate);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);

            layoutDetails = itemView.findViewById(R.id.layoutDetails);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnSeeMore = itemView.findViewById(R.id.btnSeeMore);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}