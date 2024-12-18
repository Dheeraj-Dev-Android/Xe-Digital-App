package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.regularize_attendance.PendingApprovalViewFragment.ARG_ATTENDANCE_ID;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.regularizeList.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.ui.regularize_attendance.PendingApprovalAttendance;
import app.xedigital.ai.utills.DateTimeUtils;


public class RegularizeApprovalAdapter extends RecyclerView.Adapter<RegularizeApprovalAdapter.ViewHolder> {

    private final List<AttendanceRegularizeAppliedItem> items;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String authToken;
    private final String userId;
    private final Context context;


    public RegularizeApprovalAdapter(List<AttendanceRegularizeAppliedItem> items, String authToken, String userId, PendingApprovalAttendance pendingApprovalAttendance, Context context) {
        this.items = items;
        this.userId = userId;
        this.authToken = authToken;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_pending_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRegularizeAppliedItem item = items.get(position);

        holder.empName.setText(item.getEmployee().getFullname());

        String formattedPunchDate = DateTimeUtils.getDayOfWeekAndDate(item.getPunchDate());
        holder.empPunchDate.setText("Punch Date : " + formattedPunchDate);

        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
        holder.appliedDate.setText("Applied Date : " + formattedAppliedDate);

        Chip statusChip = holder.itemView.findViewById(R.id.statusChip);
        statusChip.setText(item.getStatus());

        if (item.getStatus().equalsIgnoreCase("Approved")) {
            statusChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved)));
        } else if (item.getStatus().equalsIgnoreCase("Unapproved")) {
            statusChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending)));
        } else if (item.getStatus().equalsIgnoreCase("Rejected")) {
            statusChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected)));
        }

        holder.btn_viewAppliedAttendance.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                AttendanceRegularizeAppliedItem appliedAttendanceItem = items.get(position);
                String attendanceId = appliedAttendanceItem.getId();

                if (appliedAttendanceItem != null && attendanceId != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_ATTENDANCE_ID, appliedAttendanceItem);
                    Navigation.findNavController(v).navigate(R.id.action_nav_pendingApprovalFragment_to_nav_pendingApprovalViewFragment, bundle);

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
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView empName;
        public TextView empEmail;
        public TextView empShift;
        public TextView empContact;
        public TextView empPunchDate;
        public TextView empPunchIn;
        public TextView empPunchOut;
        public TextView empPunchInAddress;
        public TextView empPunchOutAddress;
        public TextView appliedPunchIn;
        public TextView appliedPunchOut;
        public TextView appliedPunchInAddress;
        public TextView appliedPunchOutAddress;
        public TextView appliedDate;
        public TextView appliedStatus;
        public TextView appliedStatusUpdateBy;
        public TextView appliedStatusUpdateDate;
        public Button approveButton;
        public Button rejectButton;

        public ShapeableImageView btn_viewAppliedAttendance;

        public ViewHolder(View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
            empEmail = itemView.findViewById(R.id.empEmail);
            empContact = itemView.findViewById(R.id.empContact);
            empPunchDate = itemView.findViewById(R.id.empPunchDate);
            empShift = itemView.findViewById(R.id.empShift);
            empPunchIn = itemView.findViewById(R.id.empPunchIn);
            empPunchOut = itemView.findViewById(R.id.empPunchOut);
            empPunchInAddress = itemView.findViewById(R.id.empPunchInAddress);
            empPunchOutAddress = itemView.findViewById(R.id.empPunchOutAddress);
            appliedPunchIn = itemView.findViewById(R.id.appliedPunchIn);
            appliedPunchOut = itemView.findViewById(R.id.appliedPunchOut);
            appliedPunchInAddress = itemView.findViewById(R.id.appliedPunchInAddress);
            appliedPunchOutAddress = itemView.findViewById(R.id.appliedPunchOutAddress);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            appliedStatus = itemView.findViewById(R.id.appliedStatus);
            appliedStatusUpdateBy = itemView.findViewById(R.id.appliedStatusUpdateBy);
            appliedStatusUpdateDate = itemView.findViewById(R.id.appliedStatusUpdateDate);

            approveButton = itemView.findViewById(R.id.approve_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
            btn_viewAppliedAttendance = itemView.findViewById(R.id.btn_viewAppliedAttendance);
        }
    }
}