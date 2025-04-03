package app.xedigital.ai.adapter;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.cfRegularizeApproval.AttendanceRegItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class CFregularizeAdapter extends RecyclerView.Adapter<CFregularizeAdapter.ViewHolder> {
    public static final String ARG_ATTENDANCE_REG_ITEM = "attendanceRegItem";
    private final String authToken;
    private final String userId;
    private final Context context;
    private List<AttendanceRegItem> attendanceRegItem;

    public CFregularizeAdapter(List<AttendanceRegItem> attendanceRegItem, String authToken, String userId, Context context) {
        this.attendanceRegItem = attendanceRegItem;
        this.authToken = authToken;
        this.userId = userId;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cf_regularize_approval_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRegItem item = attendanceRegItem.get(position);
        holder.empName.setText(item.getEmployee().getFullname());
        holder.appliedDate.setText("Applied Date : " + DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate()));
        holder.empPunchDate.setText("Punch Date : " + DateTimeUtils.getDayOfWeekAndDate(item.getPunchDate()));
        holder.statusChip.setText(item.getStatus());
        setStatusChipColor(holder.statusChip, item.getStatus());

        holder.attendancePendingCard.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                AttendanceRegItem selectedItem = attendanceRegItem.get(position);
                String attendanceRegId = selectedItem.getId();

                if (attendanceRegId != null && attendanceRegItem != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ATTENDANCE_REG_ITEM, selectedItem);
                    Navigation.findNavController(v).navigate(R.id.action_nav_cross_approval_attendance_to_nav_pendingCFMAttendanceApprovalFragment, bundle);
                } else {
                    Toast.makeText(context, "Leave ID is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return attendanceRegItem.size();
    }

    public void updateList(List<AttendanceRegItem> newList) {
        attendanceRegItem = newList;
        notifyDataSetChanged();
    }

    private void setStatusChipColor(Chip chip, String status) {
        switch (status.toLowerCase()) {
            case "approved":
                chip.setChipBackgroundColorResource(R.color.status_approved);
                break;
            case "unapproved":
                chip.setChipBackgroundColorResource(R.color.status_pending);
                break;
            case "rejected":
            case "cancelled":
                chip.setChipBackgroundColorResource(R.color.status_rejected);
                break;
            default:
                chip.setChipBackgroundColorResource(R.color.icon_tint);
                break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView empName;
        public TextView appliedDate;
        public TextView empPunchDate;
        public Chip statusChip;
        ShapeableImageView btnViewDetailLeave;
        MaterialCardView attendancePendingCard;

        public ViewHolder(View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            empPunchDate = itemView.findViewById(R.id.empPunchDate);
            statusChip = itemView.findViewById(R.id.statusChip);
            btnViewDetailLeave = itemView.findViewById(R.id.btn_viewAppliedAttendance);
            attendancePendingCard = itemView.findViewById(R.id.attendancePendingCard);
        }
    }
}