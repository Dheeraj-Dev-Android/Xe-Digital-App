package app.xedigital.ai.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import app.xedigital.ai.model.shiftApprovalList.EmployeeShiftdataItem;
import app.xedigital.ai.model.shiftApprove.ShiftApproveRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ShiftApprovalListAdapter extends RecyclerView.Adapter<ShiftApprovalListAdapter.ViewHolder> {

    private final Context context;
    private final List<EmployeeShiftdataItem> shiftApprovalDataList;
    private final ProfileViewModel profileViewModel;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String fName;
    private String lName;
    private String empId;
    private final LifecycleOwner lifecycleOwner;

    public ShiftApprovalListAdapter(Context context, List<EmployeeShiftdataItem> shiftApprovalDataList, LifecycleOwner lifecycleOwner, ProfileViewModel profileViewModel) {
        this.context = context;
        this.shiftApprovalDataList = shiftApprovalDataList;
        this.lifecycleOwner = lifecycleOwner;
        this.profileViewModel = profileViewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shift_approval, parent, false);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        profileViewModel.fetchUserProfile();
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.userProfile.observe(lifecycleOwner, userProfile -> {
            if (userProfile != null) {
                Log.d("ShiftsFragment", "User profile data: " + userProfile.getData());
                fName = userProfile.getData().getEmployee().getFirstname();
                lName = userProfile.getData().getEmployee().getLastname();
                empId = userProfile.getData().getEmployee().getId();
            }
        });
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
            String shiftID = shiftApprovalData.getId();
//            profileViewModel.fetchUserProfile();
            updateShiftStatus(shiftID, "approved", "", shiftApprovalData);

        });

        holder.btnCancel.setOnClickListener(v -> {
//            profileViewModel.fetchUserProfile();
            showRejectDialog(holder, shiftApprovalData.getId(), shiftApprovalData);
        });
    }

    private void showRejectDialog(ViewHolder holder, final String shiftID, EmployeeShiftdataItem shiftApprovalData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_shift_action, null);
        builder.setView(dialogView);

        EditText etComment = dialogView.findViewById(R.id.etComment);
        MaterialButton btnRejectDialog = dialogView.findViewById(R.id.btnRejectDialog);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnRejectDialog.setOnClickListener(v -> {
            String comment = etComment.getText().toString();
            updateShiftStatus(shiftID, "cancel", comment, shiftApprovalData);
            dialog.dismiss();
        });
    }

    private void updateShiftStatus(String shiftId, String status, String comment, EmployeeShiftdataItem originalPayload) {
        Log.d("ShiftStatus", "Shift ID: " + shiftId + ", Status: " + status + ", Comment: " + comment);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String token = "jwt " + authToken;
        ShiftApproveRequest requestBody = new ShiftApproveRequest();

        requestBody.setStatus(status);
        requestBody.setComment(comment);
        requestBody.setReportingManager(empId);

        // Get current date and time
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);

        requestBody.setApprovedDate(formattedDate);
        requestBody.setApprovedByName(fName + " " + lName);

//        requestBody.setId(originalPayload.getId());
        requestBody.setAppliedDate(originalPayload.getAppliedDate());
//        requestBody.setShiftType(originalPayload.getShiftType());
//        requestBody.setShiftUpdate(originalPayload.getShiftUpdate());
//        requestBody.setReportingManager(originalPayload.getReportingManager());
//        requestBody.setShift(originalPayload.getShift());
//        requestBody.setEmployee(originalPayload.getEmployee());


        APIInterface apiInterface = APIClient.getInstance().getShiftTypes();
        Call<ResponseBody> call = apiInterface.UpdateShiftStatus(token, shiftId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (response.isSuccessful() && responseBody != null) {
                        String responseJson = gson.toJson(response.body());
                        Log.d("ShiftStatus", "Response JSON: " + responseJson);
                    } else {
                        // Log error response
                        assert response.errorBody() != null;
                        String errorBody = response.errorBody().string();
                        Log.d("ShiftStatus", "Error Response: " + errorBody);
                    }
                } catch (IOException e) {
                    Log.e("ShiftStatus", "Error reading response body", e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("ShiftStatus", "Failed to update shift status: " + t.getMessage());
                Toast.makeText(context, "Failed to update shift status: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("ShiftApprovalListAdapter", "Error parsing date", e);
            return "";
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