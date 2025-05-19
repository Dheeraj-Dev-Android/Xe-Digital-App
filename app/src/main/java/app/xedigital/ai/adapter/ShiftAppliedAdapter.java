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
import app.xedigital.ai.model.shiftApplied.Employee;
import app.xedigital.ai.model.shiftApplied.EmployeeShiftdataItem;
import app.xedigital.ai.model.shiftApplied.Shift;
import app.xedigital.ai.model.shiftApplied.ShiftType;
import app.xedigital.ai.model.shiftApplied.ShiftUpdate;

public class ShiftAppliedAdapter extends RecyclerView.Adapter<ShiftAppliedAdapter.ShiftViewHolder> {

    private final List<EmployeeShiftdataItem> shiftList;

    public ShiftAppliedAdapter(List<EmployeeShiftdataItem> shiftList) {
        this.shiftList = shiftList;
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shift_applied, parent, false);
        return new ShiftViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
//        EmployeeShiftdataItem shift = shiftList.get(position);
//
//        holder.tvName.setText(shift.getEmployee().getFirstname() + " " + shift.getEmployee().getLastname());
//
//        Object profileImageUrl = shift.getEmployee().getProfileImageUrl();
//        ImageView profileImage = holder.ivProfile;
//        if (profileImageUrl != null) {
//            Glide.with(holder.itemView.getContext()).load(profileImageUrl).apply(RequestOptions.circleCropTransform()).placeholder(R.mipmap.ic_default_profile).error(R.mipmap.ic_default_profile).into(profileImage);
//        } else {
//            profileImage.setImageResource(R.mipmap.ic_default_profile);
//        }
//        holder.tvEmail.setText(shift.getEmployee().getEmail());
//        holder.tvContactNumber.setText(shift.getEmployee().getContact());
//
//        holder.shiftTimeCurrent.setText("Shift : " + shift.getShift().getName() + " (" + shift.getShift().getStartTime() + " - " + shift.getShift().getEndTime() + ")");
//        holder.tvShiftType.setText("Shift Type : " + shift.getShiftType().getShifttypeName());
//        holder.tvRequestedDate.setText("Requested Date : " + formatDate(shift.getAppliedDate()));
//        holder.tvApprovedDate.setText("Approved Date : " + formatDate(shift.getApprovedDate()));
//        holder.tvShiftUpdate.setText("Shift Updated : " + shift.getShiftUpdate().getName() + " (" + shift.getShiftUpdate().getStartTime() + " - " + shift.getShiftUpdate().getEndTime() + ")");
//        holder.tvApprovedBy.setText("Approved by: " + shift.getApprovedByName());
//
//        if (shift.getStatus().equals("unapproved")) {
//            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.pending_status_color)));
//        } else if (shift.getStatus().equals("approved")) {
//            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved)));
//        } else if (shift.getStatus().equals("cancel")) {
//            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected)));
//        }
//        holder.chipStatus.setText(shift.getStatus());
//
//    }

    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        EmployeeShiftdataItem shift = shiftList.get(position);

        // Employee Data
        Employee employee = shift.getEmployee();
        String fullName = "N/A";
        if (employee != null) {
            String firstName = employee.getFirstname();
            String lastName = employee.getLastname();
            fullName = (firstName != null ? firstName : "N/A") + " " + (lastName != null ? lastName : "N/A");
        }
        holder.tvName.setText(fullName);

        // Profile Image
        Object profileImageUrl = employee != null ? employee.getProfileImageUrl() : null;
        ImageView profileImage = holder.ivProfile;
        if (profileImageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.mipmap.ic_default_profile)
                    .error(R.mipmap.ic_default_profile)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.mipmap.ic_default_profile);
        }

        holder.tvEmail.setText(employee != null && employee.getEmail() != null ? employee.getEmail() : "N/A");
        holder.tvContactNumber.setText(employee != null && employee.getContact() != null ? employee.getContact() : "N/A");

        //Shift Data
        Shift shiftData = shift.getShift();
        String shiftTimeText = "Shift : N/A";
        if (shiftData != null) {
            String shiftName = shiftData.getName() != null ? shiftData.getName() : "N/A";
            String shiftStartTime = shiftData.getStartTime() != null ? shiftData.getStartTime() : "N/A";
            String shiftEndTime = shiftData.getEndTime() != null ? shiftData.getEndTime() : "N/A";
            shiftTimeText = "Shift : " + shiftName + " (" + shiftStartTime + " - " + shiftEndTime + ")";

        }
        holder.shiftTimeCurrent.setText(shiftTimeText);

        // Shift type data
        ShiftType shiftType = shift.getShiftType();
        String shiftTypeText = "Shift Type : N/A";
        if (shiftType != null) {
            shiftTypeText = "Shift Type : " + (shiftType.getShifttypeName() != null ? shiftType.getShifttypeName() : "N/A");
        }
        holder.tvShiftType.setText(shiftTypeText);

        // other data
        holder.tvRequestedDate.setText("Requested Date : " + (formatDate(shift.getAppliedDate()) != null ? formatDate(shift.getAppliedDate()) : "N/A"));
        holder.tvApprovedDate.setText("Updated Date : " + (formatDate(shift.getApprovedDate()) != null ? formatDate(shift.getApprovedDate()) : "N/A"));

        //Shift Update data
        ShiftUpdate shiftUpdate = shift.getShiftUpdate();
        String shiftUpdateText = "Shift Updated : N/A";
        if (shiftUpdate != null) {
            String updateName = shiftUpdate.getName() != null ? shiftUpdate.getName() : "N/A";
            String updateStartTime = shiftUpdate.getStartTime() != null ? shiftUpdate.getStartTime() : "N/A";
            String updateEndTime = shiftUpdate.getEndTime() != null ? shiftUpdate.getEndTime() : "N/A";
            shiftUpdateText = "Shift Updated : " + updateName + " (" + updateStartTime + " - " + updateEndTime + ")";

        }
        holder.tvShiftUpdate.setText(shiftUpdateText);

        holder.tvApprovedBy.setText("Updated By: " + (shift.getApprovedByName() != null ? shift.getApprovedByName() : "N/A"));

        // Status and Chip color
        String status = shift.getStatus();
        if (status != null) {
            if (status.equals("unapproved")) {
                holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.pending_status_color)));
            } else if (status.equals("approved")) {
                holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved)));
            } else if (status.equals("cancel")) {
                holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected)));
            } else {
                holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.pending_status_color)));
            }
            holder.chipStatus.setText(status);
        } else {
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.pending_status_color)));
            holder.chipStatus.setText("N/A");
        }
    }

    private String formatDate(String inputDate) {
        if (inputDate == null) {
            return "";
        }

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