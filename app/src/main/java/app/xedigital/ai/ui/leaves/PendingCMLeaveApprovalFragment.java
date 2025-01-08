package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
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
import app.xedigital.ai.model.cmLeaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.model.leaveUpdateStatus.LeaveUpdateRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PendingCMLeaveApprovalFragment extends Fragment {

    public static final String ARG_LEAVE_ID = "leave_id";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private APIInterface apiInterface;
    private AppliedLeavesItem item;
    private String crossManager;
    private String managerId;
    private String leaveId;
    private ProfileViewModel profileViewModel;
    private OnLeaveApprovalActionListener listener;
    private Button leaveBtnApprove, leaveBtnReject, leaveBtnCancel;


    public PendingCMLeaveApprovalFragment() {
        // Required empty public constructor
    }

    public static PendingCMLeaveApprovalFragment newInstance(AppliedLeavesItem item) {
        PendingCMLeaveApprovalFragment fragment = new PendingCMLeaveApprovalFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LEAVE_ID, item);
        fragment.setArguments(args);
        return fragment;
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateTimeFormat.format(currentDateTime);

    }

    public void setListener(OnLeaveApprovalActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (AppliedLeavesItem) getArguments().getSerializable(ARG_LEAVE_ID);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pending_c_m_leave_approval, container, false);
        TextView empName = view.findViewById(R.id.empName);
        TextView empEmail = view.findViewById(R.id.empEmail);
        TextView empDesignation = view.findViewById(R.id.empDesignation);
        TextView empLeaveType = view.findViewById(R.id.empLeaveType);
        TextView empFromDate = view.findViewById(R.id.empFromDate);
        TextView empSelectTypeFrom = view.findViewById(R.id.empSelectTypeFrom);
        TextView empToDate = view.findViewById(R.id.empToDate);
        TextView empSelectTypeTo = view.findViewById(R.id.empSelectTypeTo);
        TextView empAppliedDate = view.findViewById(R.id.empAppliedDate);
        TextView empReason = view.findViewById(R.id.empReason);
        TextView empContactNumber = view.findViewById(R.id.empContactNumber);
        TextView empVacationAddress = view.findViewById(R.id.empVacationAddress);
        TextView empLeaveingStation = view.findViewById(R.id.empLeavingStation);
        TextView empStatusUpdatedBy = view.findViewById(R.id.empStatusUpdatedBy);
        TextView empComments = view.findViewById(R.id.empComments);
        TextView empApprovedDate = view.findViewById(R.id.empStatusUpdatedDate);
        Chip empStatusChip = view.findViewById(R.id.empStatusChip);

        leaveBtnApprove = view.findViewById(R.id.leaveBtnApprove);
        leaveBtnReject = view.findViewById(R.id.leaveBtnReject);
        leaveBtnCancel = view.findViewById(R.id.leaveBtnCancel);

        // Populate data
        if (item != null) {
            empName.setText(item.getEmpFirstName() + " " + item.getEmpLastName());
            empEmail.setText(item.getEmpEmail());
            empDesignation.setText(item.getDepartment());
            empLeaveType.setText(item.getLeaveName());
            String formattedFromDate = DateTimeUtils.getDayOfWeekAndDate(item.getFromDate());
            empFromDate.setText(formattedFromDate);
            empSelectTypeFrom.setText(item.getSelectTypeFrom());
            String formattedToDate = DateTimeUtils.getDayOfWeekAndDate(item.getToDate());
            empToDate.setText(formattedToDate);
            empSelectTypeTo.setText(item.getSelectTypeTo());
            String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
            empAppliedDate.setText(formattedAppliedDate);
            empReason.setText(item.getReason());
            empContactNumber.setText(item.getContactNumber());
            empVacationAddress.setText(item.getVacationAddress());
            empLeaveingStation.setText(item.getLeavingStation());
            String formattedAprrovedDate = DateTimeUtils.getDayOfWeekAndDate(item.getApprovedDate());
            empApprovedDate.setText(formattedAprrovedDate);
            empStatusUpdatedBy.setText(item.getApprovedByName());
            empComments.setText(item.getComment());
            empStatusChip.setText(item.getStatus());
            // Set chip color based on status using if-else
            if (item.getStatus().equalsIgnoreCase("Pending")) {
                empStatusChip.setChipBackgroundColorResource(R.color.pending_color);
            } else if (item.getStatus().equalsIgnoreCase("Approved")) {
                empStatusChip.setChipBackgroundColorResource(R.color.approved_color);
            } else if (item.getStatus().equalsIgnoreCase("Rejected")) {
                empStatusChip.setChipBackgroundColorResource(R.color.rejected_color);
            }

            if (item.getStatus().equals("unapproved")) {
                leaveBtnApprove.setVisibility(View.VISIBLE);
                leaveBtnReject.setVisibility(View.VISIBLE);
                leaveBtnCancel.setVisibility(View.VISIBLE);

                leaveBtnApprove.setOnClickListener(v -> {
                    String leaveId = item.getId();
                    if (listener != null) {
                        listener.onApprove(item);
                    }
                    handleApprove(leaveId);
                });

                leaveBtnReject.setOnClickListener(v -> {
                    String leaveId = item.getId();
                    if (listener != null) {
                        listener.onReject(item);
                    }
                    showCommentPopup("Reject", leaveId);
                });

                leaveBtnCancel.setOnClickListener(v -> {
                    String leaveId = item.getId();
                    if (listener != null) {
                        listener.onCancel(item);
                    }
                    showCommentPopup("Cancel", leaveId);
                });

            } else {
                leaveBtnApprove.setVisibility(View.GONE);
                leaveBtnReject.setVisibility(View.GONE);
                leaveBtnCancel.setVisibility(View.GONE);
            }

        }
        if (requireContext() instanceof FragmentActivity) {
            profileViewModel = new ViewModelProvider((FragmentActivity) requireContext()).get(ProfileViewModel.class);
        }

        if (ViewTreeLifecycleOwner.get(view) != null) {
            profileViewModel.userProfile.observe(Objects.requireNonNull(ViewTreeLifecycleOwner.get(view)), userProfile -> {
                if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null && userProfile.getData().getEmployee().getReportingManager() != null) {
                    String crossManagerFirstName = userProfile.getData().getEmployee().getCrossmanager().getFirstname();
                    String crossManagerLastName = userProfile.getData().getEmployee().getCrossmanager().getLastname();
                    String crossManagerId = userProfile.getData().getEmployee().getCrossmanager().getId();
                    if (crossManagerFirstName != null && crossManagerLastName != null && crossManagerId != null) {
                        managerId = crossManagerId;
                        crossManager = crossManagerFirstName + " " + crossManagerLastName;
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
            String comment = Objects.requireNonNull(commentEditText.getText()).toString();

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


    public void handleReject(String leaveId, String comment) {
//        Log.e("CMLeaveRejected", "handleReject: " + leaveId);
        LeaveUpdateRequest requestBody = new LeaveUpdateRequest();
        requestBody.setStatus("Rejected");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(crossManager);
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
//                    Log.e("CMLeaveRejected", "onResponse: " + response.body());
                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingCMLeaveApprovalFragment.this).attach(PendingCMLeaveApprovalFragment.this).commit();
                    if (listener != null) {
                        listener.onCancel(item);
                    }
                    // Navigate to the destination fragment
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_pendingCMLeaveApproval_to_nav_cross_approval_leave);
                    Toast.makeText(requireContext(), "Leave Rejected Successfully", Toast.LENGTH_SHORT).show();
//                    Log.d("CMLeaveRejected", "onResponse: " + gson.toJson(response.body()));
                } else {
                    Log.e("CMLeaveRejected", "onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Toast.makeText(requireContext(), "Leave Rejected Failed", Toast.LENGTH_SHORT).show();
                Log.e("CMLeaveRejected", "onFailure: " + throwable.getMessage());
            }
        });
    }

    void handleCancel(String leaveId, String comment) {
//        Log.e("CMLeaveCancel", "handleCancel: " + leaveId);
        LeaveUpdateRequest requestBody = new LeaveUpdateRequest();
        requestBody.setStatus("Cancelled");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(crossManager);
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
//                    Log.d("CMLeaveCancel", "onResponse: " + response.body());
                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingCMLeaveApprovalFragment.this).attach(PendingCMLeaveApprovalFragment.this).commit();
                    if (listener != null) {
                        listener.onCancel(item);
                    }
                    // Navigate to the destination fragment
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_pendingCMLeaveApproval_to_nav_cross_approval_leave);
                    Toast.makeText(requireContext(), "Leave Cancelled", Toast.LENGTH_SHORT).show();
                    Log.d("CMLeaveCancel", "onResponse: " + gson.toJson(response.body()));
                } else {
                    Log.e("CMLeaveCancel", "onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Toast.makeText(requireContext(), "Leave Cancelled Failed", Toast.LENGTH_SHORT).show();
                Log.e("CMLeaveCancel", "onFailure: " + throwable.getMessage());
            }
        });

    }

    public void handleApprove(String leaveId) {
//        Log.e("CMLeaveApproval", "handleApprove: " + leaveId);
        LeaveUpdateRequest requestBody = new LeaveUpdateRequest();
        requestBody.setStatus("Approved");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(crossManager);
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
//                    Log.d("CMLeaveApproval", "onResponse: " + response.body());
                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingCMLeaveApprovalFragment.this).attach(PendingCMLeaveApprovalFragment.this).commit();
                    if (listener != null) {
                        listener.onCancel(item);
                    }
                    // Navigate to the destination fragment
                    Navigation.findNavController(requireView()).navigate(R.id.action_nav_pendingCMLeaveApproval_to_nav_cross_approval_leave);
                    Toast.makeText(requireContext(), "Leave Approved Successfully", Toast.LENGTH_SHORT).show();
                    Log.d("CMLeaveApproval", "onResponse: " + gson.toJson(response.body()));
                } else {
                    Log.e("CMLeaveApproval", "onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e("LeaveApproval", "onFailure: " + throwable.getMessage());
            }
        });
    }

    private void updateUIBasedOnStatus() {
        // Hide/show buttons based on the current status of 'item'
        if (item.getStatus().equals("Cancelled") || item.getStatus().equals("Rejected") || item.getStatus().equals("Approved")) {
            leaveBtnApprove.setVisibility(View.GONE);
            leaveBtnReject.setVisibility(View.GONE);
            leaveBtnCancel.setVisibility(View.GONE);
        } else {
            leaveBtnApprove.setVisibility(View.VISIBLE);
            leaveBtnReject.setVisibility(View.VISIBLE);
            leaveBtnCancel.setVisibility(View.VISIBLE);
        }
    }

    public interface OnLeaveApprovalActionListener {
        void onApprove(AppliedLeavesItem item);

        void onReject(AppliedLeavesItem item);

        void onCancel(AppliedLeavesItem item);
    }
}