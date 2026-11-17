package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.AddAttendanceApproval.AddAttendanceRegularizeAppliedItem;
import app.xedigital.ai.admin.adminModal.AddAttendanceApproval.Employee;
import app.xedigital.ai.admin.adminModal.AddAttendanceApproval.Shift;
import app.xedigital.ai.utills.DateTimeUtils;

public class AddAttendancePendingApprovalAdapter extends RecyclerView.Adapter<AddAttendancePendingApprovalAdapter.ViewHolder> {

    private final List<AddAttendanceRegularizeAppliedItem> allItems = new ArrayList<>();
    private final List<AddAttendanceRegularizeAppliedItem> displayedItems = new ArrayList<>();
    private final Set<String> expandedItemKeys = new HashSet<>();
    private final OnActionListener actionListener;

    public AddAttendancePendingApprovalAdapter(List<AddAttendanceRegularizeAppliedItem> items, OnActionListener listener) {
        this.actionListener = listener;
        updateList(items);
    }

    public void updateList(List<AddAttendanceRegularizeAppliedItem> items) {
        this.allItems.clear();
        this.displayedItems.clear();
        this.expandedItemKeys.clear();

        if (items != null) {
            for (AddAttendanceRegularizeAppliedItem item : items) {
                if (item != null) {
                    this.allItems.add(item);
                    this.displayedItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        displayedItems.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedItems.addAll(allItems);
        } else {
            String lower = query.toLowerCase().trim();
            for (AddAttendanceRegularizeAppliedItem item : allItems) {
                if (item == null) continue;

                Employee emp = item.getEmployee();
                String firstName = (emp != null && emp.getFirstname() != null) ? emp.getFirstname() : "";
                String lastName = (emp != null && emp.getLastname() != null) ? emp.getLastname() : "";
                String name = (firstName + " " + lastName).trim();

                String email = (emp != null && emp.getEmail() != null) ? emp.getEmail() : "";
                String code = (emp != null && emp.getEmployeeCode() != null) ? emp.getEmployeeCode() : "";
                String status = item.getStatus() != null ? item.getStatus() : "";

                if (name.toLowerCase().contains(lower) ||
                        email.toLowerCase().contains(lower) ||
                        code.toLowerCase().contains(lower) ||
                        status.toLowerCase().contains(lower)) {
                    displayedItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByDateRange(String startDate, String endDate, String employeeName, String status) {
        displayedItems.clear();
        Date start = parseFilterDate(startDate);
        Date end = parseFilterDate(endDate);

        Calendar startCal = null;
        if (start != null) {
            startCal = Calendar.getInstance();
            startCal.setTime(start);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
        }

        Calendar endCal = null;
        if (end != null) {
            endCal = Calendar.getInstance();
            endCal.setTime(end);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
        }

        for (AddAttendanceRegularizeAppliedItem item : allItems) {
            if (item == null) continue;

            boolean matchesDate = true;
            boolean matchesEmp = true;
            boolean matchesStatus = true;

            String reqDateStr = item.getPunchDate();
            if (TextUtils.isEmpty(reqDateStr)) {
                reqDateStr = item.getAppliedDate();
            }
            if (TextUtils.isEmpty(reqDateStr)) {
                reqDateStr = item.getCreatedAt();
            }

            Date reqDate = parseFilterDate(reqDateStr);
            if (startCal != null && endCal != null && reqDate != null) {
                if (reqDate.before(startCal.getTime()) || reqDate.after(endCal.getTime())) {
                    matchesDate = false;
                }
            }

            if (!TextUtils.isEmpty(employeeName) && !"All".equalsIgnoreCase(employeeName.trim())) {
                Employee emp = item.getEmployee();
                String firstName = (emp != null && emp.getFirstname() != null) ? emp.getFirstname() : "";
                String lastName = (emp != null && emp.getLastname() != null) ? emp.getLastname() : "";
                String name = (firstName + " " + lastName).trim();

                if (!name.toLowerCase().contains(employeeName.toLowerCase().trim())) {
                    matchesEmp = false;
                }
            }

            if (!TextUtils.isEmpty(status) && !"All".equalsIgnoreCase(status.trim())) {
                String st = item.getStatus() != null ? item.getStatus() : "";
                if (!st.equalsIgnoreCase(status.trim())) {
                    matchesStatus = false;
                }
            }

            if (matchesDate && matchesEmp && matchesStatus) {
                displayedItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    private Date parseFilterDate(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) return null;
        String cleanStr = dateStr.trim();
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "dd-MM-yyyy",
                "MM/dd/yyyy"
        };
        for (String fmt : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.getDefault());
                return sdf.parse(cleanStr);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_attendance_pending_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= displayedItems.size()) return;
        AddAttendanceRegularizeAppliedItem item = displayedItems.get(position);
        if (item == null) return;

        Employee emp = item.getEmployee();
        Shift shift = item.getShift();

        // 1. Name (Summary)
        String firstName = (emp != null && emp.getFirstname() != null) ? emp.getFirstname() : "";
        String lastName = (emp != null && emp.getLastname() != null) ? emp.getLastname() : "";
        String name = (firstName + " " + lastName).trim();
        if (holder.tvName != null) {
            holder.tvName.setText(TextUtils.isEmpty(name) ? "--" : name);
        }

        // 2. Email
        if (holder.tvEmail != null) {
            String email = (emp != null && !TextUtils.isEmpty(emp.getEmail())) ? emp.getEmail() : "--";
            holder.tvEmail.setText(email);
        }

        // 3. Contact
        if (holder.tvContact != null) {
            String contact = (emp != null && !TextUtils.isEmpty(emp.getContact())) ? emp.getContact() : "--";
            holder.tvContact.setText(contact);
        }

        // 5. Shift
        if (holder.tvShift != null) {
            String shiftName = (shift != null && !TextUtils.isEmpty(shift.getName())) ? shift.getName() : "--";
            holder.tvShift.setText(shiftName);
        }

        // 6. Punch Date
        String rawPunchDate = item.getPunchDate();
        if (holder.tvPunchDate != null) {
            holder.tvPunchDate.setText(formatIsoDate(rawPunchDate));
        }

        // 7. Punch-in Time
        String rawPunchIn = item.getPunchIn();
        if (holder.tvPunchInTime != null) {
            holder.tvPunchInTime.setText(formatIsoTime(rawPunchIn));
        }

        // 8. Punch-out Time
        String rawPunchOut = item.getPunchOut();
        if (holder.tvPunchOutTime != null) {
            holder.tvPunchOutTime.setText(formatIsoTime(rawPunchOut));
        }

        // 9. Total Time
        if (holder.tvTotalTime != null) {
            String totalTime = DateTimeUtils.calculateTotalTime(rawPunchIn, rawPunchOut);
            holder.tvTotalTime.setText(TextUtils.isEmpty(totalTime) ? "--" : totalTime);
        }

        // 10. Punch-in Address
        if (holder.tvPunchInAddress != null) {
            String address = !TextUtils.isEmpty(item.getPunchInAddress()) ? item.getPunchInAddress() : "--";
            holder.tvPunchInAddress.setText(address);
        }

        // 11. Punch-out Address
        if (holder.tvPunchOutAddress != null) {
            String address = !TextUtils.isEmpty(item.getPunchOutAddress()) ? item.getPunchOutAddress() : "--";
            holder.tvPunchOutAddress.setText(address);
        }

        // 12. Requested Date
        if (holder.tvRequestedDate != null) {
            String rawAppliedDate = item.getAppliedDate() != null ? item.getAppliedDate() : item.getCreatedAt();
            holder.tvRequestedDate.setText(formatIsoDate(rawAppliedDate));
        }

        // 13. Status
        String status = item.getStatus() != null ? item.getStatus() : "unapproved";
        if (holder.tvStatus != null) {
            holder.tvStatus.setText(status.toUpperCase(Locale.ROOT));
            applyStatusBadgeStyle(holder.tvStatus, status);
        }

        // 14. Status Updated By
        if (holder.tvStatusUpdatedBy != null) {
            String approvedBy = !TextUtils.isEmpty(item.getApprovedByName()) ? item.getApprovedByName() : "--";
            holder.tvStatusUpdatedBy.setText(approvedBy);
        }

        // 15. Status Updated Date
        if (holder.tvStatusUpdatedDate != null) {
            String rawApprovedDate = item.getApprovedDate();
            holder.tvStatusUpdatedDate.setText(formatIsoDate(rawApprovedDate));
        }

        // 16. Remarks
        if (holder.tvRemarks != null) {
            String remark = item.getRemark();
            holder.tvRemarks.setText(remark != null && !remark.trim().isEmpty() ? remark.trim() : "--");
        }

        // 17. Actions Visibility
        if (holder.layoutActions != null) {
            if ("unapproved".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
                holder.layoutActions.setVisibility(View.VISIBLE);
            } else {
                holder.layoutActions.setVisibility(View.GONE);
            }
        }

        // 18. Action Button Click Listeners
        if (holder.btnApprove != null) {
            holder.btnApprove.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onApprove(item);
                }
            });
        }

        if (holder.btnReject != null) {
            holder.btnReject.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onReject(item);
                }
            });
        }

        // 19. Expand / Collapse State
        final String itemKey = item.getId() != null ? item.getId() : String.valueOf(position);
        boolean isExpanded = expandedItemKeys.contains(itemKey);

        if (holder.layoutDetails != null) {
            holder.layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

        if (holder.btnSeeMore != null) {
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
    }

    private String formatIsoDate(String rawDateStr) {
        if (TextUtils.isEmpty(rawDateStr) || "1900-01-01T00:00:00.000Z".equalsIgnoreCase(rawDateStr.trim())) {
            return "--";
        }
        String trimmed = rawDateStr.trim();
        String result = DateTimeUtils.formatToReadableDate(trimmed);
        if (!TextUtils.isEmpty(result) && !result.equalsIgnoreCase(trimmed)) {
            return result;
        }
        try {
            if (trimmed.contains("T")) {
                String datePart = trimmed.split("T")[0];
                SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                Date date = inFormat.parse(datePart);
                if (date != null) return outFormat.format(date);
            } else {
                SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                Date date = inFormat.parse(trimmed);
                if (date != null) return outFormat.format(date);
            }
        } catch (Exception ignored) {
        }
        return trimmed;
    }

    private String formatIsoTime(String rawTimeStr) {
        if (TextUtils.isEmpty(rawTimeStr) || "1900-01-01T00:00:00.000Z".equalsIgnoreCase(rawTimeStr.trim())) {
            return "--";
        }
        String trimmed = rawTimeStr.trim();
        String formatted = DateTimeUtils.formatTime(trimmed);
        if (!"N/A".equalsIgnoreCase(formatted) && !TextUtils.isEmpty(formatted)) {
            return formatted;
        }
        try {
            if (trimmed.contains("T")) {
                Instant instant = Instant.parse(trimmed);
                ZonedDateTime zdt = instant.atZone(ZoneId.of("Asia/Kolkata"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
                return zdt.format(formatter);
            }
        } catch (Exception ignored) {
        }
        return "--";
    }

    private void applyStatusBadgeStyle(TextView tvStatus, String status) {
        if (tvStatus == null) return;
        if (status == null) status = "";

        if ("APPROVED".equalsIgnoreCase(status)) {
            tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
            tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("REJECTED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
            tvStatus.setTextColor(Color.parseColor("#C62828"));
        } else {
            // Default unapproved / pending
            tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
            tvStatus.setTextColor(Color.parseColor("#E65100"));
        }
    }

    @Override
    public int getItemCount() {
        return displayedItems != null ? displayedItems.size() : 0;
    }

    public interface OnActionListener {
        void onApprove(AddAttendanceRegularizeAppliedItem item);

        void onReject(AddAttendanceRegularizeAppliedItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvContact, tvShift;
        TextView tvPunchDate, tvPunchInTime, tvPunchOutTime, tvTotalTime;
        TextView tvPunchInAddress, tvPunchOutAddress;
        TextView tvRequestedDate, tvStatus, tvStatusUpdatedBy, tvStatusUpdatedDate, tvRemarks;
        LinearLayout layoutDetails, layoutActions;
        MaterialButton btnSeeMore, btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvShift = itemView.findViewById(R.id.tvShift);
            tvPunchDate = itemView.findViewById(R.id.tvPunchDate);
            tvPunchInTime = itemView.findViewById(R.id.tvPunchInTime);
            tvPunchOutTime = itemView.findViewById(R.id.tvPunchOutTime);
            tvTotalTime = itemView.findViewById(R.id.tvTotalTime);
            tvPunchInAddress = itemView.findViewById(R.id.tvPunchInAddress);
            tvPunchOutAddress = itemView.findViewById(R.id.tvPunchOutAddress);
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
        }
    }
}