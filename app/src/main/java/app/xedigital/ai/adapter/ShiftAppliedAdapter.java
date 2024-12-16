package app.xedigital.ai.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.shiftApplied.EmployeeShiftdataItem;

public class ShiftAppliedAdapter extends RecyclerView.Adapter<ShiftAppliedAdapter.ShiftViewHolder> {

    private List<EmployeeShiftdataItem> shiftList;

    public ShiftAppliedAdapter(List<EmployeeShiftdataItem> shiftList) {
        this.shiftList = shiftList;
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shift_applied, parent, false);
        return new ShiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        EmployeeShiftdataItem shift = shiftList.get(position);

        holder.tvName.setText(shift.getEmployee().getFirstname() + " " + shift.getEmployee().getLastname());

        Object profileImageUrl = shift.getEmployee().getProfileImageUrl();
        ImageView profileImage = holder.ivProfile;
        if (profileImageUrl != null) {
            Glide.with(holder.itemView.getContext()).load(profileImageUrl).apply(RequestOptions.circleCropTransform()).placeholder(R.mipmap.ic_default_profile).error(R.mipmap.ic_default_profile).into(profileImage);
        } else {
            profileImage.setImageResource(R.mipmap.ic_default_profile);
        }
        holder.tvEmail.setText(shift.getEmployee().getEmail());
        holder.tvContactNumber.setText(shift.getEmployee().getContact());

        holder.shiftTimeCurrent.setText("Shift : " + shift.getShift().getName() + " (" + shift.getShift().getStartTime() + " - " + shift.getShift().getEndTime() + ")");
        holder.tvShiftType.setText("Shift Type : " + shift.getShiftType().getShifttypeName());
        holder.tvRequestedDate.setText("Requested Date : " + formatDate(shift.getAppliedDate()));
        holder.tvApprovedDate.setText("Approved Date : " + formatDate(shift.getApprovedDate()));
        holder.tvShiftUpdate.setText("Shift Updated : " + shift.getShiftUpdate().getName() + " (" + shift.getShiftUpdate().getStartTime() + " - " + shift.getShiftUpdate().getEndTime() + ")");
        holder.tvApprovedBy.setText("Approved by: " + shift.getApprovedByName());

        if (shift.getStatus().equals("unapproved")) {
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.pending_status_color)));
        } else if (shift.getStatus().equals("approved")) {
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved)));
        } else if (shift.getStatus().equals("cancelled")) {
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected)));
        }
        holder.chipStatus.setText(shift.getStatus());

    }

    private String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputDate;
        }
    }

    @Override
    public int getItemCount() {
        return shiftList.size();
    }

    static class ShiftViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;
        TextView tvContactNumber;
        TextView shiftTimeCurrent;
        TextView tvShiftType;
        TextView tvRequestedDate;
        TextView tvApprovedDate;
        TextView tvShiftUpdate;
        TextView tvApprovedBy;
        Chip chipStatus;
        ImageView ivProfile;

        public ShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            shiftTimeCurrent = itemView.findViewById(R.id.shiftTimeCurrent);
            tvShiftType = itemView.findViewById(R.id.tvShiftType);
            tvRequestedDate = itemView.findViewById(R.id.tvRequestedDate);
            tvApprovedDate = itemView.findViewById(R.id.tvApprovedDate);
            tvShiftUpdate = itemView.findViewById(R.id.tvShiftUpdate);
            tvApprovedBy = itemView.findViewById(R.id.tvApprovedBy);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }
    }
}