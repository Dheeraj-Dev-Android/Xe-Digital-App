package app.xedigital.ai.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.shiftApprovalList.EmployeeShiftdataItem;


public class ShiftApprovalListAdapter extends RecyclerView.Adapter<ShiftApprovalListAdapter.ViewHolder> {

    private final Context context;
    private final List<EmployeeShiftdataItem> shiftApprovalDataList;

    public ShiftApprovalListAdapter(Context context, List<EmployeeShiftdataItem> shiftApprovalDataList) {
        this.context = context;
        this.shiftApprovalDataList = shiftApprovalDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shift_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeShiftdataItem shiftApprovalData = shiftApprovalDataList.get(position);

        holder.tvName.setText(shiftApprovalData.getEmployee().getFirstname() + " " + shiftApprovalData.getEmployee().getLastname());

        Object profileImageUrl = shiftApprovalData.getEmployee().getProfileImageUrl();
        ImageView profileImage = holder.ivProfile;
        if (profileImageUrl != null) {
            Glide.with(holder.itemView.getContext()).load(profileImageUrl).apply(RequestOptions.circleCropTransform()).placeholder(R.mipmap.ic_default_profile).error(R.mipmap.ic_default_profile).into(profileImage);
        } else {
            profileImage.setImageResource(R.mipmap.ic_default_profile);
        }
        holder.tvEmail.setText(shiftApprovalData.getEmployee().getEmail());
        holder.tvContactNumber.setText(shiftApprovalData.getEmployee().getContact());

        holder.shiftTimeCurrent.setText("Shift : " + shiftApprovalData.getShift().getName() + " (" + shiftApprovalData.getShift().getStartTime() + " - " + shiftApprovalData.getShift().getEndTime() + ")");
        holder.tvShiftType.setText("Shift Type : " + shiftApprovalData.getShiftType().getShifttypeName());
        holder.tvRequestedDate.setText("Requested Date : " + formatDate(shiftApprovalData.getAppliedDate()));
        holder.tvApprovedDate.setText("Approved Date : " + formatDate(shiftApprovalData.getApprovedDate()));
        holder.tvShiftUpdate.setText("Shift Updated : " + shiftApprovalData.getShiftUpdate().getName() + " (" + shiftApprovalData.getShiftUpdate().getStartTime() + " - " + shiftApprovalData.getShiftUpdate().getEndTime() + ")");
        holder.tvApprovedBy.setText("Approved by: " + shiftApprovalData.getApprovedByName());

        if (shiftApprovalData.getStatus().equals("unapproved")) {
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.pending_status_color)));
        } else if (shiftApprovalData.getStatus().equals("approved")) {
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved)));
        } else if (shiftApprovalData.getStatus().equals("cancel")) {
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected)));
        }
        holder.chipStatus.setText(shiftApprovalData.getStatus());
        if (shiftApprovalData.getStatus().equals("unapproved")) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> {
            showActionDialog(holder, shiftApprovalDataList.get(position).getId(), "approve");
        });

        holder.btnCancel.setOnClickListener(v -> {
            showActionDialog(holder, shiftApprovalDataList.get(position).getId(), "reject");
        });
    }


    private void showActionDialog(ViewHolder holder, String shiftId, String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_shift_action, null);
        builder.setView(dialogView);

        TextView tvShiftId = dialogView.findViewById(R.id.tvShiftId);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        MaterialButton btnApproveDialog = dialogView.findViewById(R.id.btnApproveDialog);
        MaterialButton btnRejectDialog = dialogView.findViewById(R.id.btnRejectDialog);

        tvShiftId.setText(shiftId);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnApproveDialog.setOnClickListener(v -> {
            String comment = etComment.getText().toString();
            // Handle approve action with comment and shiftId
            dialog.dismiss();
        });

        btnRejectDialog.setOnClickListener(v -> {
            String comment = etComment.getText().toString();
            // Handle reject action with comment and shiftId
            dialog.dismiss();
        });
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
        return shiftApprovalDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        MaterialButton btnApprove;
        MaterialButton btnCancel;

        public ViewHolder(@NonNull View itemView) {
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
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}