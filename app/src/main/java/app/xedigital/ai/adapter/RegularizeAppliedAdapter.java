package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import app.xedigital.ai.R;
import app.xedigital.ai.model.regularizeApplied.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.DateTimeUtils;

import java.util.List;

public class RegularizeAppliedAdapter extends RecyclerView.Adapter<RegularizeAppliedAdapter.ViewHolder> {

    private final List<AttendanceRegularizeAppliedItem> items;

    public RegularizeAppliedAdapter(List<AttendanceRegularizeAppliedItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.regularize_applied, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRegularizeAppliedItem item = items.get(position);

        holder.empName.setText(item.getEmployee().getFullname());
        holder.empEmail.setText(item.getEmployee().getEmail());

        String formattedPunchDate = DateTimeUtils.getDayOfWeekAndDate(item.getPunchDate());
        holder.empPunchDate.setText(formattedPunchDate);

        holder.empShift.setText(item.getShift().getName() + " (" + item.getShift().getStartTime() + " - " + item.getShift().getEndTime() + ")");

        String formattedPunchIn = DateTimeUtils.formatTime(item.getPunchIn());
        holder.empPunchIn.setText(formattedPunchIn);

        String formattedPunchOut = DateTimeUtils.formatTime(item.getPunchOut());
        holder.empPunchOut.setText(formattedPunchOut);

        String totalTime = DateTimeUtils.calculateTotalTime(formattedPunchIn, formattedPunchOut);
        holder.empTotalTime.setText(totalTime);

        String lateTime = DateTimeUtils.calculateLateTime(item.getPunchIn(), item.getShift().getStartTime());
        holder.empLateTime.setText(lateTime);

        holder.empPunchInAddress.setText(item.getPunchInAddress());
        holder.empPunchOutAddress.setText(item.getPunchOutAddress());

        String formattedAppliedPunchIn = DateTimeUtils.formatTime(item.getPunchInUpdated());
        holder.appliedPunchIn.setText(formattedAppliedPunchIn);

        String formattedAppliedPunchOut = DateTimeUtils.formatTime(item.getPunchOutUpdated());
        holder.appliedPunchOut.setText(formattedAppliedPunchOut);

        holder.appliedPunchInAddress.setText(item.getPunchInAddressUpdated());
        holder.appliedPunchOutAddress.setText(item.getPunchOutAddressUpdated());

        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
        holder.appliedDate.setText(formattedAppliedDate);

        holder.appliedStatus.setText(item.getStatus());
        holder.appliedStatusUpdateBy.setText(item.getApprovedByName());

        String formattedUpdatedDate = DateTimeUtils.getDayOfWeekAndDate(item.getApprovedDate());
        holder.appliedStatusUpdateDate.setText(formattedUpdatedDate);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView empName;
        public TextView empEmail;
        public TextView empShift;
        public TextView empPunchDate;
        public TextView empPunchIn;
        public TextView empPunchOut;
        public TextView empTotalTime;
        public TextView empLateTime;
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

        public ViewHolder(View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
            empEmail = itemView.findViewById(R.id.empEmail);
            empPunchDate = itemView.findViewById(R.id.empPunchDate);
            empShift = itemView.findViewById(R.id.empShift);
            empPunchIn = itemView.findViewById(R.id.empPunchIn);
            empPunchOut = itemView.findViewById(R.id.empPunchOut);
            empTotalTime = itemView.findViewById(R.id.empTotalTime);
            empLateTime = itemView.findViewById(R.id.empLateTime);
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

        }
    }
}