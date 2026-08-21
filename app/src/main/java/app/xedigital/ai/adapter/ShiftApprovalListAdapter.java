package app.xedigital.ai.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.shiftApprovalList.EmployeeApproveShiftdataItem;
import app.xedigital.ai.model.shiftApprove.ShiftApproveRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShiftApprovalListAdapter extends RecyclerView.Adapter<ShiftApprovalListAdapter.ViewHolder> {

    private static final String TAG = "ShiftApprovalAdapter";

    private final Context context;
    private final List<EmployeeApproveShiftdataItem> shiftApprovalDataList;
    private final ProfileViewModel profileViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String fName = "";
    private String lName = "";
    private String empId = "";

    public ShiftApprovalListAdapter(Context context, List<EmployeeApproveShiftdataItem> shiftApprovalDataList, LifecycleOwner lifecycleOwner, ProfileViewModel profileViewModel) {
        this.context = context;
        this.shiftApprovalDataList = shiftApprovalDataList;
        this.lifecycleOwner = lifecycleOwner;
        this.profileViewModel = profileViewModel;

        // ✅ Observe profile once in the constructor to avoid memory leaks
        SecurePrefManager prefs = SecurePrefManager.getInstance(context);
        profileViewModel.storeLoginData(prefs.getString("userId", ""), prefs.getString("authToken", ""));
        profileViewModel.fetchUserProfile();
        profileViewModel.userProfile.observe(lifecycleOwner, profile -> {
            if (profile != null && profile.getData() != null) {
                fName = profile.getData().getEmployee().getFirstname();
                lName = profile.getData().getEmployee().getLastname();
                empId = profile.getData().getEmployee().getId();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shift_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeApproveShiftdataItem data = shiftApprovalDataList.get(position);

        // ---- Profile ----
        holder.tvName.setText(data.getEmployee().getFirstname() + " " + data.getEmployee().getLastname());
        holder.tvEmail.setText(data.getEmployee().getEmail());
        holder.tvContactNumber.setText(data.getEmployee().getContact());

        Object profileImageUrl = data.getEmployee().getProfileImageUrl();
        if (profileImageUrl != null) {
            Glide.with(context).load(profileImageUrl).apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.ic_default_profile).error(R.mipmap.ic_default_profile)).into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.mipmap.ic_default_profile);
        }

        // ---- Current Shift ----
        holder.shiftTimeCurrent.setText(data.getShift().getName() + " (" + data.getShift().getStartTime() + " - " + data.getShift().getEndTime() + ")");
        holder.tvShiftType.setText(data.getShiftType().getShifttypeName());

        // ---- Requested Update ----
        holder.tvShiftUpdate.setText(data.getShiftUpdate().getName() + " (" + data.getShiftUpdate().getStartTime() + " - " + data.getShiftUpdate().getEndTime() + ")");
        holder.tvRequestedDate.setText(formatDate(data.getAppliedDate()));

        // ---- Approval info ----
        holder.tvApprovedBy.setText(data.getApprovedByName() != null ? data.getApprovedByName() : "N/A");
        holder.tvApprovedDate.setText(formatDate(data.getApprovedDate()));

        // ---- Status UI Bind ----
        bindStatus(holder, data.getStatus());

        // ---- Action Listeners ----
        holder.btnApprove.setOnClickListener(v -> confirmAndUpdate(data, "approved", ""));
        holder.btnCancel.setOnClickListener(v -> showRejectDialog(data));
    }

    private void bindStatus(@NonNull ViewHolder holder, String rawStatus) {
        if (rawStatus == null) rawStatus = "";

        String label;
        int chipBg;
        int chipText = ContextCompat.getColor(context, R.color.white);
        boolean showStrip = false;
        boolean showActions = false;

        // Reset colors to guard against RecyclerView view recycling
        int green = ContextCompat.getColor(context, R.color.status_approved);
        holder.tvApprovedBy.setTextColor(green);
        holder.tvApprovedDate.setTextColor(green);

        switch (rawStatus.toLowerCase(Locale.getDefault())) {
            case "approved":
                label = "Approved";
                chipBg = ContextCompat.getColor(context, R.color.status_approved);
                showStrip = true;
                break;

            case "cancel":
                label = "Cancelled";
                chipBg = ContextCompat.getColor(context, R.color.status_rejected);
                showStrip = true;
                int red = ContextCompat.getColor(context, R.color.status_rejected);
                holder.tvApprovedBy.setTextColor(red);
                holder.tvApprovedDate.setTextColor(red);
                break;

            case "unapproved":
            default:
                label = "Pending";
                chipBg = ContextCompat.getColor(context, R.color.pending_status_color);
                showActions = true;
                break;
        }

        holder.chipStatus.setText(label);
        holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(chipBg));
        holder.chipStatus.setTextColor(chipText);
        holder.approvalStripContainer.setVisibility(showStrip ? View.VISIBLE : View.GONE);
        holder.actionsContainer.setVisibility(showActions ? View.VISIBLE : View.GONE);
    }

    private void confirmAndUpdate(EmployeeApproveShiftdataItem data, String status, String comment) {
        new AlertDialog.Builder(context).setTitle("Confirm Approval").setMessage("Are you sure you want to approve this shift change request?").setPositiveButton("Approve", (d, w) -> {
            updateShiftStatus(data, status, comment);
            d.dismiss();
        }).setNegativeButton("Cancel", (d, w) -> d.dismiss()).show();
    }

    private void showRejectDialog(EmployeeApproveShiftdataItem data) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_shift_action, null);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        MaterialButton btnReject = dialogView.findViewById(R.id.btnRejectDialog);

        AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).create();
        dialog.show();

        btnReject.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            if (comment.isEmpty()) {
                etComment.setError("Please provide a cancellation reason");
                return;
            }
            updateShiftStatus(data, "cancel", comment);
            dialog.dismiss();
        });
    }

    private void updateShiftStatus(EmployeeApproveShiftdataItem originalPayload, String status, String comment) {
        Log.d(TAG, "Updating status of " + originalPayload.getId() + " to " + status);

        SecurePrefManager prefManager = SecurePrefManager.getInstance(context);
        String token = "jwt " + prefManager.getString("authToken", "");

        String formattedNow = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

        // ---- Create Request Payload ----
        ShiftApproveRequest request = new ShiftApproveRequest();

        request.setId(originalPayload.getId());
        request.setStatus(status);
        request.setApprovedDate(formattedNow);
        request.setApprovedByName(fName + " " + lName);
        request.setComment(comment != null ? comment : "");
        request.setAppliedDate(originalPayload.getAppliedDate());
        request.setReportingManager(originalPayload.getReportingManager());

        // ✅ Bulletproof Conversion: Maps nested JSON models across packages perfectly
        request.setShiftType(convertModel(originalPayload.getShiftType(), app.xedigital.ai.model.shiftApprove.ShiftType.class));
        request.setShift(convertModel(originalPayload.getShift(), app.xedigital.ai.model.shiftApprove.Shift.class));
        request.setShiftUpdate(convertModel(originalPayload.getShiftUpdate(), app.xedigital.ai.model.shiftApprove.ShiftUpdate.class));
        request.setEmployee(convertModel(originalPayload.getEmployee(), app.xedigital.ai.model.shiftApprove.Employee.class));

        APIInterface api = APIClient.getInstance().getShiftTypes();
        api.UpdateShiftStatus(token, originalPayload.getId(), request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // ✅ Dynamic UI Updates (no list re-fetch required)
                    originalPayload.setStatus(status);
                    originalPayload.setApprovedByName(fName + " " + lName);
                    originalPayload.setApprovedDate(formattedNow);

                    int index = shiftApprovalDataList.indexOf(originalPayload);
                    if (index != -1) {
                        notifyItemChanged(index);
                    }

                    String msg = "approved".equals(status) ? "Shift approved successfully!" : "Shift cancelled successfully!";
                    showAlertDialog("Success", msg);
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Response Error: " + error);
                        showAlertDialog("Error", "Action failed:\n" + error);
                    } catch (IOException e) {
                        showAlertDialog("Error", "Failed to parse API error details.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
                showAlertDialog("Network Error", "Could not connect to server.");
            }
        });
    }

    /**
     * Helper strategy to convert matching nested JSON schemas between packages type-safely.
     */
    private <T> T convertModel(Object source, Class<T> targetClass) {
        if (source == null) return null;
        String json = gson.toJson(source);
        return gson.fromJson(json, targetClass);
    }

    private void showAlertDialog(String title, String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton("OK", (d, w) -> d.dismiss()).show());
        }
    }

    private String formatDate(String input) {
        if (input == null || input.isEmpty()) return "N/A";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = in.parse(input);
            return date != null ? out.format(date) : "N/A";
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing issue: " + e.getMessage());
            return input;
        }
    }

    @Override
    public int getItemCount() {
        return shiftApprovalDataList != null ? shiftApprovalDataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvContactNumber;
        TextView shiftTimeCurrent, tvShiftType;
        TextView tvShiftUpdate, tvRequestedDate;
        TextView tvApprovedBy, tvApprovedDate;
        Chip chipStatus;
        ImageView ivProfile;
        MaterialButton btnApprove, btnCancel;
        LinearLayout approvalStripContainer;
        LinearLayout actionsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            shiftTimeCurrent = itemView.findViewById(R.id.shiftTimeCurrent);
            tvShiftType = itemView.findViewById(R.id.tvShiftType);
            tvShiftUpdate = itemView.findViewById(R.id.tvShiftUpdate);
            tvRequestedDate = itemView.findViewById(R.id.tvRequestedDate);
            tvApprovedBy = itemView.findViewById(R.id.tvApprovedBy);
            tvApprovedDate = itemView.findViewById(R.id.tvApprovedDate);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            approvalStripContainer = itemView.findViewById(R.id.approvalStripContainer);
            actionsContainer = itemView.findViewById(R.id.actionsContainer);
        }
    }
}