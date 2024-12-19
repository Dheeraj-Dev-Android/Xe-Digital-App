package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.regularize_attendance.RegularizeViewFragment.ARG_REGULARIZE_APPLIED_ITEM;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.addedAttendanceList.AddAttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AddedAttendanceAdapter extends RecyclerView.Adapter<AddedAttendanceAdapter.AttendanceViewHolder> {

    private List<AddAttendanceRegularizeAppliedItem> attendanceDataList;


    public AddedAttendanceAdapter(List<AddAttendanceRegularizeAppliedItem> attendanceDataList) {
        this.attendanceDataList = attendanceDataList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_added_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AddAttendanceRegularizeAppliedItem attendanceData = attendanceDataList.get(position);

        holder.empName.setText(attendanceData.getEmployee().getFirstname() + " " + attendanceData.getEmployee().getLastname());
        holder.appliedDate.setText("Applied Date : " + DateTimeUtils.getDayOfWeekAndDate(attendanceData.getAppliedDate()));
        holder.empPunchDate.setText("Punch Date : " + DateTimeUtils.getDayOfWeekAndDate(attendanceData.getPunchDate()));
        holder.statusChip.setText(attendanceData.getStatus());


        if (attendanceData.getStatus().equalsIgnoreCase("Approved")) {
            holder.statusChip.setChipBackgroundColorResource(R.color.status_approved);
        } else if (attendanceData.getStatus().equalsIgnoreCase("unapproved")) {
            holder.statusChip.setChipBackgroundColorResource(R.color.status_pending);
        } else if (attendanceData.getStatus().equalsIgnoreCase("Cancel")) {
            holder.statusChip.setChipBackgroundColorResource(R.color.status_rejected);
        }

        holder.btnAppliedAddAttendance.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                AddAttendanceRegularizeAppliedItem addAttendanceAppliedItem = attendanceDataList.get(adapterPosition);
                if (addAttendanceAppliedItem != null && addAttendanceAppliedItem.getId() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_REGULARIZE_APPLIED_ITEM, addAttendanceAppliedItem);
                    Navigation.findNavController(v).navigate(R.id.action_nav_View_add_attendance_fragment_to_nav_detail_view_added_attendanceFragment, bundle);
                } else {
                    Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return attendanceDataList.size();
    }

    public void updateAttendanceData(List<AddAttendanceRegularizeAppliedItem> newAttendanceData) {
        this.attendanceDataList = newAttendanceData;
        notifyDataSetChanged();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        public ShapeableImageView btnAppliedAddAttendance;
        TextView empName;
        TextView appliedDate;
        TextView empPunchDate;
        Chip statusChip;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            empPunchDate = itemView.findViewById(R.id.empPunchDate);
            statusChip = itemView.findViewById(R.id.statusChip);
            btnAppliedAddAttendance = itemView.findViewById(R.id.btn_viewAppliedAddAttendance);
        }
    }
}