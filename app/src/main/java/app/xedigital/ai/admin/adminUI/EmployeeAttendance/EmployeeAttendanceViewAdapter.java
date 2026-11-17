package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.empAttendance.Employee;
import app.xedigital.ai.admin.adminModal.empAttendance.EmployeePunchDataItem;
import app.xedigital.ai.admin.adminModal.empAttendance.Shift;
import app.xedigital.ai.utills.DateTimeUtils;

public class EmployeeAttendanceViewAdapter
        extends RecyclerView.Adapter<EmployeeAttendanceViewAdapter.ViewHolder> {

    private final List<EmployeePunchDataItem> attendanceList;

    public EmployeeAttendanceViewAdapter(List<EmployeePunchDataItem> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeePunchDataItem item = attendanceList.get(position);

        // ── Employee Name ──
        bindEmployeeName(holder, item.getEmployee());

        // ── Date ──
        String dateStr = item.getPunchDateFormat();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            dateStr = item.getPunchDate() != null
                    ? DateTimeUtils.formatToReadableDate(String.valueOf(item.getPunchDate()))
                    : "—";
        }
        holder.attendaceDateView.setText(dateStr);

        // ── Leave / Holiday ──
        bindLeaveOrHoliday(holder, item);

        // ── Shift times (for late / OT) ──
        String[] shiftTimes = extractShiftTimes(item.getShift());
        String shiftStart = shiftTimes[0]; // e.g. "09:00"
        String shiftEnd = shiftTimes[1]; // e.g. "18:00"

        // ── Raw punch values ──
        String punchInRaw = objToStr(item.getPunchIn());
        String punchOutRaw = objToStr(item.getPunchOut());

        // ── Punch In / Out (formatted via DateTimeUtils) ──
        holder.punchInTextView.setText(DateTimeUtils.formatTime(punchInRaw));
        holder.punchOutTextView.setText(DateTimeUtils.formatTime(punchOutRaw));

        // ── Total Time (cleaned) ──
        String totalTime = formatTotalTime(item.getDailyTotalWorkingHour(), punchInRaw, punchOutRaw);
        holder.totalTimeTextView.setText(totalTime);

        // ── Late Time ──
        holder.lateTimeTextView.setText(
                DateTimeUtils.calculateLateTime(punchInRaw, shiftStart));

        // ── Over Time ──
        holder.overTimeTextView.setText(
                DateTimeUtils.calculateOvertime(totalTime, shiftStart, shiftEnd));

        // ── Action Buttons ──
        holder.btnViewAttendance.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "View Attendance", Toast.LENGTH_SHORT).show());

        holder.btnRegularize.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Regularize", Toast.LENGTH_SHORT).show());
    }

    // ══════════════════════════════════════════════
    //  TOTAL TIME CLEANUP
    // ══════════════════════════════════════════════

    private String formatTotalTime(Object apiTotal, String punchInRaw, String punchOutRaw) {
        if (apiTotal != null) {
            String str = String.valueOf(apiTotal).trim();
            if (!str.isEmpty() && !"null".equalsIgnoreCase(str) && !"—".equals(str)) {

                // "In Progress"
                if (str.equalsIgnoreCase("In Progress")) {
                    return "In Progress";
                }

                // Decimal format like "0 hours 0.379517 minutes"
                if (str.contains("hours") || str.contains("minutes")) {
                    try {
                        int hrs = 0;
                        int mins = 0;

                        if (str.contains("hours")) {
                            String hStr = str.substring(0, str.indexOf("hours")).trim();
                            hrs = (int) Double.parseDouble(hStr);
                        }
                        if (str.contains("minutes")) {
                            int minStart = str.contains("hours") ? str.indexOf("hours") + 5 : 0;
                            String mStr = str.substring(minStart, str.indexOf("minutes")).trim();
                            mins = (int) Math.round(Double.parseDouble(mStr));
                        }
                        return String.format(Locale.getDefault(), "%02d:%02d Hrs", hrs, mins);
                    } catch (Exception ignored) {
                        return str;
                    }
                }

                // Already formatted with Hrs / Min
                if (str.toLowerCase(Locale.getDefault()).contains("hrs")
                        || str.toLowerCase(Locale.getDefault()).contains("min")) {
                    return str;
                }

                // HH:mm format
                if (str.contains(":")) {
                    return str + " Hrs";
                }

                return str;
            }
        }

        // Fallback: calculate
        return DateTimeUtils.calculateTotalTime(punchInRaw, punchOutRaw);
    }

    // ══════════════════════════════════════════════
    //  SHIFT EXTRACTION
    // ══════════════════════════════════════════════

    private String[] extractShiftTimes(Object shiftObj) {
        String start = "09:00";
        String end = "18:00";

        if (shiftObj == null) return new String[]{start, end};

        try {
            if (shiftObj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) shiftObj;

                if (map.get("startTime") != null) {
                    start = normalizeShiftTime(String.valueOf(map.get("startTime")));
                } else if (map.get("fromHour") != null) {
                    int h = (int) Double.parseDouble(String.valueOf(map.get("fromHour")));
                    int m = map.get("fromMinutes") != null
                            ? (int) Double.parseDouble(String.valueOf(map.get("fromMinutes"))) : 0;
                    start = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                }

                if (map.get("endTime") != null) {
                    end = normalizeShiftTime(String.valueOf(map.get("endTime")));
                } else if (map.get("toHour") != null) {
                    int h = (int) Double.parseDouble(String.valueOf(map.get("toHour")));
                    int m = map.get("toMinutes") != null
                            ? (int) Double.parseDouble(String.valueOf(map.get("toMinutes"))) : 0;
                    end = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                }

            } else if (shiftObj instanceof Shift) {
                Shift shift = (Shift) shiftObj;

                if (shift.getStartTime() != null && !shift.getStartTime().trim().isEmpty()) {
                    start = normalizeShiftTime(shift.getStartTime());
                } else {
                    start = String.format(Locale.getDefault(), "%02d:%02d",
                            shift.getFromHour(), shift.getFromMinutes());
                }

                if (shift.getEndTime() != null && !shift.getEndTime().trim().isEmpty()) {
                    end = normalizeShiftTime(shift.getEndTime());
                } else {
                    end = String.format(Locale.getDefault(), "%02d:%02d",
                            shift.getToHour(), shift.getToMinutes());
                }
            }
        } catch (Exception ignored) {
            // keep defaults
        }

        return new String[]{start, end};
    }

    private String normalizeShiftTime(String raw) {
        if (raw == null) return "09:00";
        String s = raw.trim();
        if (s.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
            String[] p = s.split(":");
            return String.format(Locale.getDefault(), "%02d:%02d",
                    Integer.parseInt(p[0]), Integer.parseInt(p[1]));
        }
        return s;
    }

    // ══════════════════════════════════════════════
    //  BIND HELPERS
    // ══════════════════════════════════════════════

    private void bindEmployeeName(ViewHolder holder, Employee emp) {
        if (emp == null) {
            holder.empNameTextView.setText("Unknown Employee");
            return;
        }
        String name = emp.getFullname();
        if (name == null || name.trim().isEmpty()) {
            String first = emp.getFirstname() != null ? emp.getFirstname() : "";
            String last = emp.getLastname() != null ? emp.getLastname() : "";
            name = (first + " " + last).trim();
            if (name.isEmpty()) name = "Unknown Employee";
        }
        holder.empNameTextView.setText(name);
    }

    private void bindLeaveOrHoliday(ViewHolder holder, EmployeePunchDataItem item) {
        String leave = item.getLeaveName();
        String holiday = item.getHolidayName();

        if (leave != null && !leave.trim().isEmpty()) {
            holder.leaveNameTextView.setText(leave);
            holder.leaveNameTextView.setVisibility(View.VISIBLE);
        } else if (holiday != null && !holiday.trim().isEmpty()) {
            holder.leaveNameTextView.setText(holiday);
            holder.leaveNameTextView.setVisibility(View.VISIBLE);
        } else {
            holder.leaveNameTextView.setVisibility(View.GONE);
        }
    }

    private String objToStr(Object obj) {
        if (obj == null) return null;
        String s = String.valueOf(obj).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        return s;
    }

    @Override
    public int getItemCount() {
        return attendanceList != null ? attendanceList.size() : 0;
    }

    // ── ViewHolder ──

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView empNameTextView;
        TextView attendaceDateView;
        TextView leaveNameTextView;
        TextView punchInTextView, punchOutTextView;
        TextView totalTimeTextView, overTimeTextView, lateTimeTextView;
        ImageButton btnViewAttendance, btnRegularize;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            empNameTextView = itemView.findViewById(R.id.empNameTextView);
            attendaceDateView = itemView.findViewById(R.id.attendaceDateView);
            leaveNameTextView = itemView.findViewById(R.id.leaveNameTextView);
            punchInTextView = itemView.findViewById(R.id.punchInTextView);
            punchOutTextView = itemView.findViewById(R.id.punchOutTextView);
            totalTimeTextView = itemView.findViewById(R.id.totalTimeTextView);
            overTimeTextView = itemView.findViewById(R.id.overTimeTextView);
            lateTimeTextView = itemView.findViewById(R.id.lateTimeTextView);
            btnViewAttendance = itemView.findViewById(R.id.btn_viewAttendance);
            btnRegularize = itemView.findViewById(R.id.btn_regularize);
        }
    }
}