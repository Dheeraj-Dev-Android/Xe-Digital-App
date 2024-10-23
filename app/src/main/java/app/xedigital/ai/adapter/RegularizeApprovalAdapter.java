package app.xedigital.ai.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.regularizeList.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.model.regularizeUpdateStatus.RegularizeUpdateRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegularizeApprovalAdapter extends RecyclerView.Adapter<RegularizeApprovalAdapter.ViewHolder> {

    private final List<AttendanceRegularizeAppliedItem> items;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String authToken;
    private final String userId;
    APIInterface apiInterface = APIClient.getInstance().UpdateRegularizeListApproval();
    private final Context context;
    private final OnRegularizeApprovalActionListener listener;
    private ProfileViewModel profileViewModel;
    private String reportingManager;

    public RegularizeApprovalAdapter(List<AttendanceRegularizeAppliedItem> items, String authToken, String userId, OnRegularizeApprovalActionListener listener, Context context) {
        this.items = items;
        this.userId = userId;
        this.authToken = authToken;
        this.listener = listener;
        this.context = context;
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDateTime = dateTimeFormat.format(currentDateTime);
        Log.d("RegularizeApprovalAdapter", "getCurrentDateTimeInUTC: " + formattedDateTime);
        return formattedDateTime;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRegularizeAppliedItem item = items.get(position);

        holder.empName.setText(item.getEmployee().getFullname());
        holder.appliedDate.setText(item.getAppliedDate());
        holder.empName.setText(item.getEmployee().getFullname());
        holder.empEmail.setText(item.getEmployee().getEmail() + ",  " + item.getEmployee().getContact());

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


        if (item.getStatus().equals("unapproved")) {
            holder.approveButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);

            holder.approveButton.setOnClickListener(v -> {
                listener.onApprove(item);
                String attendanceId = item.getId();
                handleApprove(attendanceId);
            });

            holder.rejectButton.setOnClickListener(v -> {
                listener.onReject(item);
                String attendanceId = item.getId();

                handleReject(attendanceId);
            });
        } else {
            holder.approveButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
        }

        if (context instanceof FragmentActivity) {
            profileViewModel = new ViewModelProvider((FragmentActivity) context).get(ProfileViewModel.class);
        }

        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
        if (ViewTreeLifecycleOwner.get(holder.itemView) != null) {
            profileViewModel.userProfile.observe(Objects.requireNonNull(ViewTreeLifecycleOwner.get(holder.itemView)), userProfile -> {
                if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null && userProfile.getData().getEmployee().getReportingManager() != null) {
                    String reportingManagerFirstName = userProfile.getData().getEmployee().getReportingManager().getFirstname();
                    String reportingManagerLastName = userProfile.getData().getEmployee().getReportingManager().getLastname();
                    if (reportingManagerFirstName != null && reportingManagerLastName != null) {
                        reportingManager = reportingManagerFirstName + " " + reportingManagerLastName;
                        Log.e("RegularizeApprovalAdapter", "reportingManager: " + reportingManager);
                    }
                }
            });
        }
    }

    public void handleApprove(String attendanceId) {
        Log.d("RegularizeApprovalAdapter", "handleApprove: " + attendanceId);
        RegularizeUpdateRequest requestBody = new RegularizeUpdateRequest();
        requestBody.setStatus("Approved");
        requestBody.setApprovedByName(reportingManager);
        Log.d("RegularizeApprovalAdapter", "handleApprove: " + reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());

        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
//                    Log.d("RegularizeApprovalAdapter", "onResponse: " + gson.toJson(response.body()));
                    Toast.makeText(context, "Attendance approved successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to approve attendance.", Toast.LENGTH_SHORT).show();
                    Log.d("RegularizeApprovalAdapter", "onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.d("RegularizeApprovalAdapter", "onFailure: " + throwable.getMessage());
                Toast.makeText(context, "Failed to approve attendance.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void handleReject(String attendanceId) {

        RegularizeUpdateRequest requestBody = new RegularizeUpdateRequest();
        requestBody.setStatus("Rejected");
        requestBody.setApprovedByName(reportingManager);
        Log.d("RegularizeApprovalAdapter", "handleReject: " + reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());

        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
//                    Log.d("RegularizeApprovalAdapter", "onResponse: " + gson.toJson(response.body()));
                    Toast.makeText(context, "Attendance rejected successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("RegularizeApprovalAdapter", "onResponse: " + response.errorBody());
                    Toast.makeText(context, "Failed to reject attendance.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.d("RegularizeApprovalAdapter", "onFailure: " + throwable.getMessage());
                Toast.makeText(context, "Failed to reject attendance.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnRegularizeApprovalActionListener {
        void onApprove(AttendanceRegularizeAppliedItem item);

        void onReject(AttendanceRegularizeAppliedItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView empName;
        public TextView empEmail;
        public TextView empShift;
        public TextView empContact;
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
        public Button approveButton;
        public Button rejectButton;

        public ViewHolder(View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
            empEmail = itemView.findViewById(R.id.empEmail);
            empContact = itemView.findViewById(R.id.empContact);
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

            approveButton = itemView.findViewById(R.id.approve_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}