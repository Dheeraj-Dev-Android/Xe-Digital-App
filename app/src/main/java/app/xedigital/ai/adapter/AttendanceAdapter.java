package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.regularize_attendance.RegularizeFragment.ARG_ATTENDANCE_ITEM;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.DateTimeUtils;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<EmployeePunchDataItem> attendanceList;
    private final OnAttendanceActionListener listener;

    public AttendanceAdapter(List<EmployeePunchDataItem> attendanceList, OnAttendanceActionListener listener) {
        this.attendanceList = attendanceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_list_item, parent, false);
        return new AttendanceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        EmployeePunchDataItem attendanceItem = attendanceList.get(position);

        String dayOfWeek = attendanceItem.getDayOfWeek();
        String punchIn = attendanceItem.getPunchIn();
        String punchOut = attendanceItem.getPunchOut();
        holder.leaveTypeName.setVisibility(View.GONE);

        if ((dayOfWeek != null && (dayOfWeek.equals("Sat") || dayOfWeek.equals("Sun"))) && (punchIn == null || punchOut == null)) {
            holder.dateTextView.setText("Date: " + attendanceItem.getPunchDateFormat() + "  (" + dayOfWeek + ")");

            holder.punchInTextView.setVisibility(View.GONE);
            holder.punchOutTextView.setVisibility(View.GONE);
            holder.totalTimeTextView.setVisibility(View.GONE);
            holder.lateTimeTextView.setVisibility(View.GONE);
            holder.overTimeTextView.setVisibility(View.GONE);
            holder.btnViewAttendance.setVisibility(View.GONE);
            holder.btnRegularize.setVisibility(View.GONE);
            holder.leaveTypeName.setVisibility(View.GONE);

        } else if (attendanceItem.getLeaveName() != null && !attendanceItem.getLeaveName().isEmpty()) {
            holder.dateTextView.setText("Date: " + attendanceItem.getPunchDateFormat() + "  (" + dayOfWeek + ")");
            holder.leaveTypeName.setVisibility(View.VISIBLE);
            holder.leaveTypeName.setText("Leave Name: " + attendanceItem.getLeaveName());

            holder.punchInTextView.setVisibility(View.GONE);
            holder.punchOutTextView.setVisibility(View.GONE);
            holder.totalTimeTextView.setVisibility(View.GONE);
            holder.lateTimeTextView.setVisibility(View.GONE);
            holder.overTimeTextView.setVisibility(View.GONE);
            holder.btnViewAttendance.setVisibility(View.GONE);
            holder.btnRegularize.setVisibility(View.GONE);

        } else if (attendanceItem.getHolidayName() != null && !attendanceItem.getHolidayName().isEmpty()) {
            holder.dateTextView.setText("Date: " + attendanceItem.getPunchDateFormat() + "  (" + dayOfWeek + ")");
            holder.leaveTypeName.setVisibility(View.VISIBLE);
            holder.leaveTypeName.setText("Holiday Name: " + attendanceItem.getHolidayName());

            holder.punchInTextView.setVisibility(View.GONE);
            holder.punchOutTextView.setVisibility(View.GONE);
            holder.totalTimeTextView.setVisibility(View.GONE);
            holder.lateTimeTextView.setVisibility(View.GONE);
            holder.overTimeTextView.setVisibility(View.GONE);
            holder.btnViewAttendance.setVisibility(View.GONE);
            holder.btnRegularize.setVisibility(View.GONE);

        } else if (punchIn != null && !punchIn.isEmpty() && punchOut != null && !punchOut.isEmpty()) {
            holder.dateTextView.setText("Date: " + attendanceItem.getPunchDateFormat() + "  (" + dayOfWeek + ")");
            holder.punchInTextView.setVisibility(View.VISIBLE);
            holder.punchOutTextView.setVisibility(View.VISIBLE);
            holder.totalTimeTextView.setVisibility(View.VISIBLE);
            holder.lateTimeTextView.setVisibility(View.VISIBLE);
            holder.overTimeTextView.setVisibility(View.VISIBLE);
            holder.btnViewAttendance.setVisibility(View.VISIBLE);
            holder.btnRegularize.setVisibility(View.VISIBLE);
            holder.leaveTypeName.setVisibility(View.GONE);

            holder.punchInTextView.setText(DateTimeUtils.formatTime(punchIn));
            holder.punchOutTextView.setText(DateTimeUtils.formatTime(punchOut));

            String shiftStart = "09:00";
            String shiftEnd = "18:00";

            if (attendanceItem.getShift() != null) {
                if (attendanceItem.getShift().getStartTime() != null && !attendanceItem.getShift().getStartTime().isEmpty()) {
                    shiftStart = attendanceItem.getShift().getStartTime();
                }
                if (attendanceItem.getShift().getEndTime() != null && !attendanceItem.getShift().getEndTime().isEmpty()) {
                    shiftEnd = attendanceItem.getShift().getEndTime();
                }
            }

            String totalTime = attendanceItem.getTotalTime();
            if (isInvalidValue(totalTime)) {
                totalTime = DateTimeUtils.calculateTotalTime(punchIn, punchOut);
            }
            holder.totalTimeTextView.setText(totalTime);

            String lateTime = attendanceItem.getLateTime();
            if (isInvalidValue(lateTime)) {
                lateTime = DateTimeUtils.calculateLateTime(punchIn, shiftStart);
            }
            holder.lateTimeTextView.setText(lateTime);

            String overTime = attendanceItem.getOvertime();
            if (isInvalidValue(overTime)) {
                overTime = DateTimeUtils.calculateOvertime(totalTime, shiftStart, shiftEnd);
            }
            holder.overTimeTextView.setText(overTime);

        } else {
            holder.dateTextView.setText("Date: " + attendanceItem.getPunchDateFormat() + "  (" + dayOfWeek + ")");
            holder.leaveTypeName.setVisibility(View.VISIBLE);
            holder.leaveTypeName.setText("Leave Name : Loss of Pay (LOP) / Leave Without Pay (LWP)");

            holder.punchInTextView.setVisibility(View.GONE);
            holder.punchOutTextView.setVisibility(View.GONE);
            holder.totalTimeTextView.setVisibility(View.GONE);
            holder.lateTimeTextView.setVisibility(View.GONE);
            holder.overTimeTextView.setVisibility(View.GONE);
            holder.btnViewAttendance.setVisibility(View.GONE);
            holder.btnRegularize.setVisibility(View.GONE);
        }
    }

    private boolean isInvalidValue(String val) {
        if (val == null) return true;
        String trimmed = val.trim();
        return trimmed.isEmpty() || trimmed.equalsIgnoreCase("0") || trimmed.equalsIgnoreCase("00:00 Hrs") || trimmed.equalsIgnoreCase("0 Min's") || trimmed.equalsIgnoreCase("N/A");
    }

    @Override
    public int getItemCount() {
        return attendanceList != null ? attendanceList.size() : 0;
    }

    public interface OnAttendanceActionListener {
        void onRegularizeClick(EmployeePunchDataItem attendanceItem);
    }

    public class AttendanceViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView, punchInTextView, punchOutTextView, totalTimeTextView, lateTimeTextView, overTimeTextView, leaveTypeName;
        public ImageButton btnViewAttendance, btnRegularize;

        public AttendanceViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            punchInTextView = itemView.findViewById(R.id.punchInTextView);
            punchOutTextView = itemView.findViewById(R.id.punchOutTextView);
            totalTimeTextView = itemView.findViewById(R.id.totalTimeTextView);
            lateTimeTextView = itemView.findViewById(R.id.lateTimeTextView);
            overTimeTextView = itemView.findViewById(R.id.overTimeTextView);
            leaveTypeName = itemView.findViewById(R.id.leaveNameTextView);
            btnViewAttendance = itemView.findViewById(R.id.btn_viewAttendance);
            btnRegularize = itemView.findViewById(R.id.btn_regularize);

            btnViewAttendance.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    EmployeePunchDataItem attendanceItem = attendanceList.get(position);
                    if (attendanceItem != null && attendanceItem.getId() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ARG_ATTENDANCE_ITEM, attendanceItem);
                        Navigation.findNavController(v).navigate(R.id.action_nav_attendance_to_nav_viewAttendanceFragment, bundle);
                    } else {
                        CustomDialogHelper.showInfoDialog(v.getContext(), "Attendance Data", "Attendance data not available.");
                    }
                }
            });

            btnRegularize.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    EmployeePunchDataItem attendanceItem = attendanceList.get(position);
                    if (attendanceItem != null) {
                        if (listener != null) {
                            listener.onRegularizeClick(attendanceItem);
                        }
                    } else {
                        CustomDialogHelper.showInfoDialog(v.getContext(), "Attendance Data", "Attendance data not available.");
                    }
                }
            });
        }
    }
}