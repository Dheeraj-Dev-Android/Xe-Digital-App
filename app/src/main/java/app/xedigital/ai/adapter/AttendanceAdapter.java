package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.regularize_attendance.RegularizeFragment.ARG_ATTENDANCE_ITEM;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<EmployeePunchDataItem> attendanceList;

    public AttendanceAdapter(List<EmployeePunchDataItem> attendanceList) {
        this.attendanceList = attendanceList;
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

        if ((dayOfWeek.equals("Sat") || dayOfWeek.equals("Sun")) && (punchIn == null || punchOut == null)) {
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
            // Leave data
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
            // Holiday data
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
            // Attendance data
            holder.dateTextView.setText("Date: " + attendanceItem.getPunchDateFormat() + "  (" + dayOfWeek + ")");
            holder.punchInTextView.setVisibility(View.VISIBLE);
            holder.punchOutTextView.setVisibility(View.VISIBLE);
            holder.totalTimeTextView.setVisibility(View.VISIBLE);
            holder.lateTimeTextView.setVisibility(View.VISIBLE);
            holder.overTimeTextView.setVisibility(View.VISIBLE);
            holder.btnViewAttendance.setVisibility(View.VISIBLE);
            holder.btnRegularize.setVisibility(View.VISIBLE);
            holder.leaveTypeName.setVisibility(View.GONE);

            holder.punchInTextView.setText(DateTimeUtils.formatTime(attendanceItem.getPunchIn()));
            holder.punchOutTextView.setText(DateTimeUtils.formatTime(attendanceItem.getPunchOut()));
            holder.totalTimeTextView.setText(attendanceList.get(position).getTotalTime());
            holder.overTimeTextView.setText(attendanceList.get(position).getOvertime());
            holder.lateTimeTextView.setText(attendanceList.get(position).getLateTime());
        } else {
            //  No leave, holiday, or punch data - show LOP/LWP
            holder.dateTextView.setText("Date: " + attendanceItem.getPunchDateFormat() + "  (" + dayOfWeek + ")");
            holder.leaveTypeName.setVisibility(View.VISIBLE);
            holder.leaveTypeName.setText("Loss of Pay (LOP) / Leave Without Pay (LWP)");

            holder.punchInTextView.setVisibility(View.GONE);
            holder.punchOutTextView.setVisibility(View.GONE);
            holder.totalTimeTextView.setVisibility(View.GONE);
            holder.lateTimeTextView.setVisibility(View.GONE);
            holder.overTimeTextView.setVisibility(View.GONE);
            holder.btnViewAttendance.setVisibility(View.GONE);
            holder.btnRegularize.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public class AttendanceViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView punchInTextView;
        public TextView punchOutTextView;
        public TextView totalTimeTextView;
        public TextView lateTimeTextView;
        public TextView overTimeTextView;
        public TextView leaveTypeName;
        public ImageButton btnViewAttendance;
        public ImageButton btnRegularize;

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
                        new AlertDialog.Builder(v.getContext()).setTitle("Attendance Data").setMessage("Attendance data not available.").setPositiveButton(android.R.string.ok, null).show();
                    }
                }
            });

            btnRegularize.setOnClickListener(v -> {
                try {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        EmployeePunchDataItem attendanceItem = attendanceList.get(position);
                        if (attendanceItem != null && attendanceItem.getId() != null) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(ARG_ATTENDANCE_ITEM, attendanceItem);
                            Navigation.findNavController(v).navigate(R.id.action_nav_attendance_to_regularizeFragment, bundle);
                        } else {
                            new AlertDialog.Builder(v.getContext()).setTitle("Attendance Data").setMessage("Attendance data not available.").setPositiveButton(android.R.string.ok, null).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
