package app.xedigital.ai.ui.leaves;

import android.content.Context;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.LeaveApprovalBinding;
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesApproveItem;
import app.xedigital.ai.model.leaveUpdateStatus.LeaveUpdateRequest;
import app.xedigital.ai.model.usedLeave.UsedLeaveRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingLeaveApproveFragment extends Fragment {
    public static final String ARG_LEAVE_ID = "leave_id";
    private static final String TAG = "PendingLeaveApprove";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private APIInterface apiInterface;
    private AppliedLeavesApproveItem item;
    private String reportingManager = "";
    private String managerId = "";
    private double totalDays;
    private double finalUsedDays;
    private String userId, authToken;
    private LeaveApprovalBinding binding;
    private ProfileViewModel profileViewModel;
    private OnLeaveApprovalActionListener listener;
    private SecurePrefManager securePrefManager;

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
        return dateTimeFormat.format(currentDateTime);
    }

    public void setListener(OnLeaveApprovalActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                item = getArguments().getSerializable(ARG_LEAVE_ID, AppliedLeavesApproveItem.class);
            } else {
                @SuppressWarnings("deprecation") AppliedLeavesApproveItem oldItem = (AppliedLeavesApproveItem) getArguments().getSerializable(ARG_LEAVE_ID);
                item = oldItem;
            }
        }

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        apiInterface = APIClient.getInstance().UpdateLeaveListApproval();

        Context context = getContext();
        if (context != null) {
//            SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            securePrefManager = SecurePrefManager.getInstance(requireContext());
            userId = securePrefManager.getString("userId", "");
            authToken = securePrefManager.getString("authToken", "");
            profileViewModel.storeLoginData(userId, authToken);
            profileViewModel.fetchUserProfile();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LeaveApprovalBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (item == null) {
            Toast.makeText(getContext(), "Leave data details missing", Toast.LENGTH_SHORT).show();
            return view;
        }

        // UI Binding Data Setup with Null Safety
        binding.empName.setText(String.format("%s %s", item.getFirstname() != null ? item.getFirstname() : "", item.getLastname() != null ? item.getLastname() : "").trim());
        binding.empEmail.setText(item.getEmail());
        binding.empDesignation.setText(item.getDesignation());
        binding.empLeaveType.setText(item.getLeavetype() != null && item.getLeavetype().getLeavetypeName() != null ? item.getLeavetype().getLeavetypeName() : "");
        binding.empFromDate.setText(DateTimeUtils.getDayOfWeekAndDate(item.getFromDate()));
        binding.empSelectTypeFrom.setText(item.getSelectTypeFrom());
        binding.empToDate.setText(DateTimeUtils.getDayOfWeekAndDate(item.getToDate()));
        binding.empSelectTypeTo.setText(item.getSelectTypeTo());
        binding.empAppliedDate.setText(DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate()));
        binding.empReason.setText(item.getReason());
        binding.empContactNumber.setText(item.getContactNumber());
        binding.empAddress.setText(item.getVacationAddress());
        binding.empVacationAddress.setText(item.getVacationAddress());
        binding.empLeavingStation.setText(item.getLeavingStation());
        binding.empStatusUpdatedBy.setText(item.getApprovedByName());
        binding.empStatusUpdatedDate.setText(DateTimeUtils.getDayOfWeekAndDate(item.getApprovedDate()));
        binding.empComments.setText(item.getComment());

        calculateTotalDaysAndCheckLeaveLimit();

        String status = item.getStatus() != null ? item.getStatus().toLowerCase() : "";
        Context context = binding.empStatusChip.getContext();
        binding.empStatusChip.setText(status);

        int chipBackgroundColor;
        if (status.equals("approved")) {
            chipBackgroundColor = ContextCompat.getColor(context, R.color.status_approved);
        } else if (status.equals("cancelled") || status.equals("rejected")) {
            chipBackgroundColor = ContextCompat.getColor(context, R.color.status_rejected);
        } else {
            chipBackgroundColor = ContextCompat.getColor(context, R.color.status_pending);
        }
        binding.empStatusChip.setChipBackgroundColor(ColorStateList.valueOf(chipBackgroundColor));

        if ("unapproved".equals(item.getStatus())) {
            binding.leaveBtnApprove.setVisibility(View.VISIBLE);
            binding.leaveBtnReject.setVisibility(View.VISIBLE);
            binding.leaveBtnCancel.setVisibility(View.VISIBLE);

            binding.leaveBtnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApprove(item);
                handleApprove(item.getId());
            });

            binding.leaveBtnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(item);
                showCommentPopup("Reject", item.getId());
            });

            binding.leaveBtnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onCancel(item);
                showCommentPopup("Cancel", item.getId());
            });
        } else {
            hideActionButtons();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null) {
                managerId = userProfile.getData().getEmployee().getId();
                String firstName = userProfile.getData().getEmployee().getFirstname() != null ? userProfile.getData().getEmployee().getFirstname() : "";
                String lastName = userProfile.getData().getEmployee().getLastname() != null ? userProfile.getData().getEmployee().getLastname() : "";
                reportingManager = (firstName + " " + lastName).trim();
            }
        });
    }

    private long calculateTotalDays(String startDate, String endDate) {
        if (startDate == null || endDate == null) return 0;
        try {
            // Handle full ISO timestamp (e.g., "2026-05-30T00:00:00.000Z")
            if (startDate.contains("T")) {
                startDate = startDate.split("T")[0];
            }
            if (endDate.contains("T")) {
                endDate = endDate.split("T")[0];
            }

            // Now both strings are guaranteed to be in "yyyy-MM-dd" format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDateObj = LocalDate.parse(startDate, formatter);
            LocalDate endDateObj = LocalDate.parse(endDate, formatter);

            return ChronoUnit.DAYS.between(startDateObj, endDateObj) + 1;
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing dates after sanitization: " + e.getMessage());
            return 0;
        }
    }

    private void calculateTotalDaysAndCheckLeaveLimit() {
        if (item == null) return;
        String fromDate = item.getFromDate();
        String toDate = item.getToDate();
        String leaveCategoryFrom = item.getSelectTypeFrom();
        String leaveCategoryTo = item.getSelectTypeTo();

        if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
            long totalDaysInDateRange = calculateTotalDays(fromDate, toDate);

            if ("First Half Day".equals(leaveCategoryFrom) && "First Half Day".equals(leaveCategoryTo)) {
                totalDays = 0.5;
            } else if ("Second Half Day".equals(leaveCategoryFrom) && "Second Half Day".equals(leaveCategoryTo)) {
                totalDays = 0.5;
            } else {
                totalDays = totalDaysInDateRange;
            }
            finalUsedDays(totalDays);
        }
    }

    public void finalUsedDays(double totalDays) {
        if (item == null) return;
        String leaveCategoryFrom = item.getSelectTypeFrom();
        String leaveCategoryTo = item.getSelectTypeTo();
        finalUsedDays = totalDays;

        if ("First Half Day".equals(leaveCategoryFrom) && "First Half Day".equals(leaveCategoryTo)) {
            finalUsedDays = 0.5;
        } else if ("Second Half Day".equals(leaveCategoryFrom) && "Second Half Day".equals(leaveCategoryTo)) {
            finalUsedDays = 0.5;
        }
    }

    private void showCommentPopup(final String action, final String leaveId) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.comment_popup, null);
        final TextInputEditText commentEditText = dialogView.findViewById(R.id.commentEditText);
        final TextView characterCount = dialogView.findViewById(R.id.characterCount);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.rounded_popup_background));
        }
        dialogView.setBackgroundColor(Color.TRANSPARENT);

        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();
                characterCount.setText(String.format(Locale.getDefault(), "%d/250", currentLength));
                if (currentLength > 250) {
                    commentEditText.setError("Maximum 250 characters");
                } else {
                    commentEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button submitButton = dialogView.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            Editable text = commentEditText.getText();
            String comment = text != null ? text.toString() : "";
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

    public void handleApprove(String leaveId) {
        if (item == null) return;

        LeaveUpdateRequest requestBody = new LeaveUpdateRequest();
        requestBody.setStatus("Approved");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        requestBody.setComment("");

        if (getContext() == null) return;
        Call<ResponseBody> leaveApprove = apiInterface.LeavesStatus("jwt " + authToken, leaveId, requestBody);
        leaveApprove.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded() || getView() == null) return;

                if (response.isSuccessful()) {
                    hideActionButtons();
                    if (listener != null) listener.onApprove(item);
                    Navigation.findNavController(getView()).navigate(R.id.action_nav_approve_leave_data_to_nav_approve_leaves);
                } else {
                    showErrorMessage(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (!isAdded()) return;
                Log.e(TAG, "Approval onFailure: " + throwable.getMessage());
            }
        });
    }

    public void handleCancel(String leaveId, String comment) {
        if (item == null) return;
        if (comment == null || comment.trim().isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Comment is required", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        UsedLeaveRequest requestBody = new UsedLeaveRequest();
        requestBody.setId(item.getId());
        requestBody.setEmployeeCode(item.getEmployeeCode());
        requestBody.setDesignation(item.getDesignation());
        requestBody.setFirstname(item.getFirstname());
        requestBody.setLastname(item.getLastname());
        requestBody.setEmail(item.getEmail());
        requestBody.setContact(item.getContact());
        requestBody.setFromDate(item.getFromDate());
        requestBody.setSelectTypeFrom(item.getSelectTypeFrom());
        requestBody.setToDate(item.getToDate());
        requestBody.setSelectTypeTo(item.getSelectTypeTo());
        requestBody.setReason(item.getReason());
        requestBody.setLeavingStation(item.getLeavingStation());
        requestBody.setVacationAddress(item.getVacationAddress());
        requestBody.setContactNumber(item.getContactNumber());
        requestBody.setAppliedDate(item.getAppliedDate());
        requestBody.setAppliedBy(item.getAppliedBy());

        if (item.getLeavetype() != null) {
            app.xedigital.ai.model.usedLeave.Leavetype targetLeaveType = new app.xedigital.ai.model.usedLeave.Leavetype();
            targetLeaveType.setId(item.getLeavetype().getId());
            targetLeaveType.setLeavetypeName(item.getLeavetype().getLeavetypeName());
            requestBody.setLeavetype(targetLeaveType);
        }

        requestBody.setCreatedAt(item.getCreatedAt());
        requestBody.setUpdatedAt(item.getUpdatedAt());
        requestBody.setStatus("cancelled");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        requestBody.setComment(comment.trim());
        requestBody.setTDays((int) totalDays);
        requestBody.setFUsedDays(finalUsedDays);

        if (getContext() == null) return;

        // Web Rule Check: special leave id condition
        String leaveTypeId = (item.getLeavetype() != null) ? item.getLeavetype().getId() : "";
        if (leaveTypeId != null && !"615418abc2432d4d14990ecc".equals(leaveTypeId)) {
            // Sequential API Chain Step 1: Update Count/Debit
            Call<ResponseBody> leaveCancel = apiInterface.LeavesUsedCount("jwt " + authToken, requestBody);
            leaveCancel.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (!isAdded() || getView() == null) return;

                    if (response.isSuccessful()) {
                        // Sequential API Chain Step 2: Update Application Status
                        callStatusUpdateAPI(leaveId, "Cancelled", comment);
                    } else {
                        showErrorMessage(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Bypass Count update directly update Application Status if id matches
            callStatusUpdateAPI(leaveId, "Cancelled", comment);
        }
    }

    public void handleReject(String leaveId, String comment) {
        if (item == null) return;
        if (comment == null || comment.trim().isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Comment is required", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        UsedLeaveRequest requestBody = new UsedLeaveRequest();
        requestBody.setId(item.getId());
        requestBody.setEmployeeCode(item.getEmployeeCode());
        requestBody.setDesignation(item.getDesignation());
        requestBody.setFirstname(item.getFirstname());
        requestBody.setLastname(item.getLastname());
        requestBody.setEmail(item.getEmail());
        requestBody.setContact(item.getContact());
        requestBody.setFromDate(item.getFromDate());
        requestBody.setSelectTypeFrom(item.getSelectTypeFrom());
        requestBody.setToDate(item.getToDate());
        requestBody.setSelectTypeTo(item.getSelectTypeTo());
        requestBody.setReason(item.getReason());
        requestBody.setLeavingStation(item.getLeavingStation());
        requestBody.setVacationAddress(item.getVacationAddress());
        requestBody.setContactNumber(item.getContactNumber());
        requestBody.setAppliedDate(item.getAppliedDate());
        requestBody.setAppliedBy(item.getAppliedBy());

        if (item.getLeavetype() != null) {
            app.xedigital.ai.model.usedLeave.Leavetype targetLeaveType = new app.xedigital.ai.model.usedLeave.Leavetype();
            targetLeaveType.setId(item.getLeavetype().getId());
            targetLeaveType.setLeavetypeName(item.getLeavetype().getLeavetypeName());
            targetLeaveType.setActive(item.getLeavetype().isActive());
            targetLeaveType.setCreatedAt(item.getLeavetype().getCreatedAt());
            targetLeaveType.setUpdatedAt(item.getLeavetype().getUpdatedAt());
            targetLeaveType.setCreatedBy(item.getLeavetype().getCreatedBy());
            requestBody.setLeavetype(targetLeaveType);
        }

        requestBody.setStatus("rejected");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        requestBody.setComment(comment.trim());
        requestBody.setTDays((int) totalDays);
        requestBody.setFUsedDays(finalUsedDays);

        if (getContext() == null) return;

        // Web Rule Check: special leave id condition
        String leaveTypeId = (item.getLeavetype() != null) ? item.getLeavetype().getId() : "";
        if (leaveTypeId != null && !"615418abc2432d4d14990ecc".equals(leaveTypeId)) {
            // Sequential API Chain Step 1: Update Count/Debit
            Call<ResponseBody> leaveReject = apiInterface.LeavesUsedCount("jwt " + authToken, requestBody);
            leaveReject.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (!isAdded() || getView() == null) return;

                    if (response.isSuccessful()) {
                        // Sequential API Chain Step 2: Update Application Status
                        callStatusUpdateAPI(leaveId, "Rejected", comment);
                    } else {
                        showErrorMessage(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Bypass Count update directly update Application Status if id matches
            callStatusUpdateAPI(leaveId, "Rejected", comment);
        }
    }

    private void callStatusUpdateAPI(String leaveId, final String status, String comment) {
        LeaveUpdateRequest statusUpdateBody = new LeaveUpdateRequest();
        statusUpdateBody.setStatus(status);
        statusUpdateBody.setApprovedBy(managerId);
        statusUpdateBody.setApprovedByName(reportingManager);
        statusUpdateBody.setApprovedDate(getCurrentDateTimeInUTC());
        statusUpdateBody.setComment(comment.trim());

        Call<ResponseBody> statusUpdateCall = apiInterface.LeavesStatus("jwt " + authToken, leaveId, statusUpdateBody);
        statusUpdateCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded() || getView() == null) return;

                if (response.isSuccessful()) {
                    hideActionButtons();
                    if (listener != null) {
                        if ("Cancelled".equalsIgnoreCase(status)) {
                            listener.onCancel(item);
                        } else if ("Rejected".equalsIgnoreCase(status)) {
                            listener.onReject(item);
                        }
                    }
                    Toast.makeText(requireContext(), "Leave " + status + " Successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).navigate(R.id.action_nav_approve_leave_data_to_nav_approve_leaves);
                } else {
                    showErrorMessage(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Status update failed: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showErrorMessage(Response<ResponseBody> response) {
        String errorMessage = "Operation Failed";
        try {
            if (response.errorBody() != null) {
                errorMessage = response.errorBody().string();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error body", e);
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error : " + errorMessage);
    }

    private void hideActionButtons() {
        if (binding != null) {
            binding.leaveBtnApprove.setVisibility(View.GONE);
            binding.leaveBtnReject.setVisibility(View.GONE);
            binding.leaveBtnCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Nullify binding reference to prevent memory leaks
        binding = null;
    }

    public interface OnLeaveApprovalActionListener {
        void onApprove(AppliedLeavesApproveItem item);

        void onReject(AppliedLeavesApproveItem item);

        void onCancel(AppliedLeavesApproveItem item);
    }
}