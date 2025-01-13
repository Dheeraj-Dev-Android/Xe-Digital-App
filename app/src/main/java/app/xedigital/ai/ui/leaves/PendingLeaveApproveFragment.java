package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.LeaveApprovalBinding;
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesApproveItem;
import app.xedigital.ai.model.leaveUpdateStatus.LeaveUpdateRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PendingLeaveApproveFragment extends Fragment {
    public static final String ARG_LEAVE_ID = "leave_id";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private APIInterface apiInterface;
    private AppliedLeavesApproveItem item;
    private String reportingManager;
    private String managerId;
    private String leaveId;
    private LeaveApprovalBinding binding;
    private ProfileViewModel profileViewModel;
    private OnLeaveApprovalActionListener listener;

    public PendingLeaveApproveFragment() {
        // Required empty public constructor
    }

    public static PendingLeaveApproveFragment newInstance(AppliedLeavesApproveItem item) {
        PendingLeaveApproveFragment fragment = new PendingLeaveApproveFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LEAVE_ID, item);
        fragment.setArguments(args);
        return fragment;
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        //        Log.d("LeaveApprovalAdapter", "getCurrentDateTimeInUTC: " + formattedDateTime);
        return dateTimeFormat.format(currentDateTime);

    }

    public void setListener(OnLeaveApprovalActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (AppliedLeavesApproveItem) getArguments().getSerializable(ARG_LEAVE_ID);
        }
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        apiInterface = APIClient.getInstance().UpdateLeaveListApproval();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        String authToken = sharedPreferences.getString("authToken", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LeaveApprovalBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.empName.setText(item.getFirstname() + " " + item.getLastname());
        binding.empEmail.setText(item.getEmail());
        binding.empDesignation.setText(item.getDesignation());
        binding.empLeaveType.setText(item.getLeavetype().getLeavetypeName());
        String formattedFromDate = DateTimeUtils.getDayOfWeekAndDate(item.getFromDate());
        binding.empFromDate.setText(formattedFromDate);
        binding.empSelectTypeFrom.setText(item.getSelectTypeFrom());
        String formattedToDate = DateTimeUtils.getDayOfWeekAndDate(item.getToDate());
        binding.empToDate.setText(formattedToDate);
        binding.empSelectTypeTo.setText(item.getSelectTypeTo());
        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
        binding.empAppliedDate.setText(formattedAppliedDate);
        binding.empReason.setText(item.getReason());
        binding.empContactNumber.setText(item.getContactNumber());
        binding.empAddress.setText(item.getVacationAddress());
        binding.empVacationAddress.setText(item.getVacationAddress());
        binding.empLeavingStation.setText(item.getLeavingStation());
        binding.empStatusUpdatedBy.setText(item.getApprovedByName());
        String formattedApprovedDate = DateTimeUtils.getDayOfWeekAndDate(item.getApprovedDate());
        binding.empStatusUpdatedDate.setText(formattedApprovedDate);
        binding.empComments.setText(item.getComment());
//        binding.empTotalDays.setText();

        String status = item.getStatus().toLowerCase();
        Context context = binding.empStatusChip.getContext();

        binding.empStatusChip.setText(status);

        int chipBackgroundColor;

        if (status.equals("approved")) {
            chipBackgroundColor = ContextCompat.getColor(context, R.color.status_approved);
        } else if (status.equals("cancelled")) {
            chipBackgroundColor = ContextCompat.getColor(context, R.color.status_rejected);
        } else if (status.equals("rejected")) {
            chipBackgroundColor = ContextCompat.getColor(context, R.color.status_rejected);
        } else if (status.equals("unapproved")) {
            chipBackgroundColor = ContextCompat.getColor(context, R.color.status_pending);
        } else {
            chipBackgroundColor = ContextCompat.getColor(context, R.color.status_pending);
        }

        binding.empStatusChip.setChipBackgroundColor(ColorStateList.valueOf(chipBackgroundColor));

        if (item.getStatus().equals("unapproved")) {
            binding.leaveBtnApprove.setVisibility(View.VISIBLE);
            binding.leaveBtnReject.setVisibility(View.VISIBLE);
            binding.leaveBtnCancel.setVisibility(View.VISIBLE);

            binding.leaveBtnApprove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(item);
                }
                String leaveId = item.getId();
                handleApprove(leaveId);
            });

            binding.leaveBtnReject.setOnClickListener(v -> {
                String leaveId = item.getId();
                if (listener != null) {
                    listener.onReject(item);
                }
                showCommentPopup("Reject", leaveId);
            });
            binding.leaveBtnCancel.setOnClickListener(v -> {
                String leaveId = item.getId();
                if (listener != null) {
                    listener.onReject(item);
                }
                showCommentPopup("Cancel", leaveId);
            });

        } else {
            binding.leaveBtnApprove.setVisibility(View.GONE);
            binding.leaveBtnReject.setVisibility(View.GONE);
            binding.leaveBtnCancel.setVisibility(View.GONE);
        }
        if (requireContext() instanceof FragmentActivity) {
            profileViewModel = new ViewModelProvider((FragmentActivity) requireContext()).get(ProfileViewModel.class);
        }

        if (ViewTreeLifecycleOwner.get(binding.getRoot()) != null) {
            profileViewModel.userProfile.observe(Objects.requireNonNull(ViewTreeLifecycleOwner.get(binding.getRoot())), userProfile -> {
                if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null && userProfile.getData().getEmployee().getReportingManager() != null) {
                    String reportingManagerFirstName = userProfile.getData().getEmployee().getReportingManager().getFirstname();
                    String reportingManagerLastName = userProfile.getData().getEmployee().getReportingManager().getLastname();
                    String reportingManagerId = userProfile.getData().getEmployee().getReportingManager().getId();
                    if (reportingManagerFirstName != null && reportingManagerLastName != null && reportingManagerId != null) {
                        managerId = reportingManagerId;
                        reportingManager = reportingManagerFirstName + " " + reportingManagerLastName;
//                        Log.e("RegularizeApprovalAdapter", "reportingManager: " + reportingManager);
                    }
                }
            });
        }
        return view;
    }

    private void showCommentPopup(final String action, final String leaveId) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.comment_popup, null);
        final TextInputEditText commentEditText = dialogView.findViewById(R.id.commentEditText);
        final TextView characterCount = dialogView.findViewById(R.id.characterCount);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.rounded_popup_background));
        dialogView.setBackgroundColor(Color.TRANSPARENT);

        // TextWatcher for character count
        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for this implementation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();
                characterCount.setText(currentLength + "/250");

                if (currentLength > 250) {
                    commentEditText.setError("Maximum 250 characters");
                } else {
                    commentEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed for this implementation
            }
        });

        Button submitButton = dialogView.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            String comment = commentEditText.getText().toString();

            if (action.equals("Reject")) {
                handleReject(leaveId, comment);
            } else if (action.equals("Cancel")) {
                handleCancel(leaveId, comment);
            }
            alertDialog.dismiss();
        });

        Button backButton = dialogView.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> alertDialog.dismiss());
    }

    void handleCancel(String leaveId, String comment) {
//        Log.e("LeaveCancel", "handleCancel: " + leaveId);
        LeaveUpdateRequest requestBody = new LeaveUpdateRequest();
        requestBody.setStatus("Cancelled");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        requestBody.setComment(comment);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        Call<ResponseBody> leaveCancel = apiInterface.LeavesStatus("jwt " + authToken, leaveId, requestBody);
        leaveCancel.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    updateUIBasedOnStatus();
//                    Log.d("LeaveCancel", "onResponse: " + response.body());
                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingLeaveApproveFragment.this).attach(PendingLeaveApproveFragment.this).commit();
                    if (listener != null) {
                        listener.onCancel(item);
                    }
                    // Navigate to the destination fragment
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_approve_leave_data_to_nav_approve_leaves);
                    Toast.makeText(requireContext(), "Leave Cancelled", Toast.LENGTH_SHORT).show();
//                    Log.d("LeaveCancel", "onResponse: " + gson.toJson(response.body()));
                } else {
                    Log.e("LeaveCancel", "onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Toast.makeText(requireContext(), "Leave Cancelled Failed", Toast.LENGTH_SHORT).show();
                Log.e("LeaveCancel", "onFailure: " + throwable.getMessage());
            }
        });

    }

    public void handleApprove(String leaveId) {
//        Log.e("LeaveApproval", "handleApprove: " + leaveId);
        LeaveUpdateRequest requestBody = new LeaveUpdateRequest();
        requestBody.setStatus("Approved");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        requestBody.setComment("");

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        Call<ResponseBody> leaveCancel = apiInterface.LeavesStatus("jwt " + authToken, leaveId, requestBody);
        leaveCancel.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    updateUIBasedOnStatus();
//                    Log.d("LeaveApproval", "onResponse: " + response.body());
                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingLeaveApproveFragment.this).attach(PendingLeaveApproveFragment.this).commit();
                    if (listener != null) {
                        listener.onCancel(item);
                    }
                    // Navigate to the destination fragment
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_approve_leave_data_to_nav_approve_leaves);
                    Toast.makeText(requireContext(), "Leave Approved Successfully", Toast.LENGTH_SHORT).show();
//                    Log.d("LeaveApproval", "onResponse: " + gson.toJson(response.body()));
                } else {
                    Log.e("LeaveApproval", "onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e("LeaveApproval", "onFailure: " + throwable.getMessage());
            }
        });
    }

    public void handleReject(String leaveId, String comment) {
//        Log.e("LeaveRejected", "handleReject: " + leaveId);
        LeaveUpdateRequest requestBody = new LeaveUpdateRequest();
        requestBody.setStatus("Rejected");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        requestBody.setComment(comment);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        Call<ResponseBody> leaveCancel = apiInterface.LeavesStatus("jwt " + authToken, leaveId, requestBody);
        leaveCancel.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    updateUIBasedOnStatus();
//                    Log.e("LeaveApproval", "onResponse: " + response.body());
                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingLeaveApproveFragment.this).attach(PendingLeaveApproveFragment.this).commit();
                    if (listener != null) {
                        listener.onCancel(item);
                    }
                    // Navigate to the destination fragment
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_approve_leave_data_to_nav_approve_leaves);
                    Toast.makeText(requireContext(), "Leave Rejected Successfully", Toast.LENGTH_SHORT).show();
//                    Log.d("LeaveRejected", "onResponse: " + gson.toJson(response.body()));
                } else {
                    Log.e("LeaveRejected", "onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Toast.makeText(requireContext(), "Leave Rejected Failed", Toast.LENGTH_SHORT).show();
                Log.e("LeaveRejected", "onFailure: " + throwable.getMessage());
            }
        });
    }

    private void updateUIBasedOnStatus() {
        // Hide/show buttons based on the current status of 'item'
        if (item.getStatus().equals("Cancelled") || item.getStatus().equals("Rejected") || item.getStatus().equals("Approved")) {
            binding.leaveBtnApprove.setVisibility(View.GONE);
            binding.leaveBtnReject.setVisibility(View.GONE);
            binding.leaveBtnCancel.setVisibility(View.GONE);
        } else {
            binding.leaveBtnApprove.setVisibility(View.VISIBLE);
            binding.leaveBtnReject.setVisibility(View.VISIBLE);
            binding.leaveBtnCancel.setVisibility(View.VISIBLE);
        }
    }

    public interface OnLeaveApprovalActionListener {
        void onApprove(AppliedLeavesApproveItem item);

        void onReject(AppliedLeavesApproveItem item);

        void onCancel(AppliedLeavesApproveItem item);
    }
}