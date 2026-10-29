package app.xedigital.ai.ui.leaves;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
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
import app.xedigital.ai.model.cmLeaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.model.leaveUpdateStatus.LeaveUpdateRequest;
import app.xedigital.ai.model.usedLeave.UsedLeaveRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PendingCMLeaveApprovalFragment extends Fragment {

    public static final String ARG_LEAVE_ID = "leave_id";
    private static final String TAG = "PendingCMLeaveApproval";
    private static final String FALLBACK = "-";
    private static final String SPECIAL_LEAVE_TYPE_ID = "615418abc2432d4d14990ecc";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private APIInterface apiInterface;
    private AppliedLeavesItem item;
    private String currentUserName = "";
    private String managerId = "";
    private double totalDays;
    private double finalUsedDays;
    private String userId, authToken;
    private ProfileViewModel profileViewModel;
    private OnLeaveApprovalActionListener listener;
    private Button leaveBtnApprove, leaveBtnReject, leaveBtnCancel;
    private SecurePrefManager securePrefManager;

    // UI references for instant refresh
    private Chip empStatusChip;
    private TextView empStatusUpdatedBy;
    private TextView empStatusUpdatedDate;
    private TextView empComments;
    private TextView empTotalDays;


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
        SimpleDateFormat dateTimeFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateTimeFormat.format(currentDateTime);
    }

    private static String safe(String value) {
        return (value == null || value.trim().isEmpty()) ? FALLBACK : value;
    }

    private static String safeFormatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return FALLBACK;
        try {
            String formatted = DateTimeUtils.getDayOfWeekAndDate(rawDate);
            return (formatted == null || formatted.trim().isEmpty()) ? FALLBACK : formatted;
        } catch (Exception e) {
            Log.e(TAG, "safeFormatDate error: " + e.getMessage());
            return FALLBACK;
        }
    }

    private static String buildInitials(String first, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.trim().isEmpty()) {
            sb.append(Character.toUpperCase(first.trim().charAt(0)));
        }
        if (last != null && !last.trim().isEmpty()) {
            sb.append(Character.toUpperCase(last.trim().charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    private static String buildFullName(String first, String last) {
        String f = (first == null) ? "" : first.trim();
        String l = (last == null) ? "" : last.trim();
        String combined = (f + " " + l).trim();
        return combined.isEmpty() ? FALLBACK : combined;
    }

    public void setListener(OnLeaveApprovalActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                item = getArguments().getSerializable(ARG_LEAVE_ID, AppliedLeavesItem.class);
            } else {
                @SuppressWarnings("deprecation")
                AppliedLeavesItem oldItem = (AppliedLeavesItem) getArguments().getSerializable(ARG_LEAVE_ID);
                item = oldItem;
            }
        }

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        apiInterface = APIClient.getInstance().UpdateLeaveListApproval();
        securePrefManager = SecurePrefManager.getInstance(requireContext());

        userId = securePrefManager.getString("userId", "");
        authToken = securePrefManager.getString("authToken", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_c_m_leave_approval, container, false);

        TextView empName = view.findViewById(R.id.empName);
        TextView empEmail = view.findViewById(R.id.empEmail);
        TextView empDesignation = view.findViewById(R.id.empDesignation);
        TextView empAvatarInitials = view.findViewById(R.id.empAvatarInitials);
        TextView empLeaveType = view.findViewById(R.id.empLeaveType);
        TextView empFromDate = view.findViewById(R.id.empFromDate);
        TextView empSelectTypeFrom = view.findViewById(R.id.empSelectTypeFrom);
        TextView empToDate = view.findViewById(R.id.empToDate);
        TextView empSelectTypeTo = view.findViewById(R.id.empSelectTypeTo);
        TextView empAppliedDate = view.findViewById(R.id.empAppliedDate);
        empTotalDays = view.findViewById(R.id.empTotalDays);
        TextView empReason = view.findViewById(R.id.empReason);
        TextView empContactNumber = view.findViewById(R.id.empContactNumber);
        TextView empAddress = view.findViewById(R.id.empAddress);
        TextView empVacationAddress = view.findViewById(R.id.empVacationAddress);
        TextView empLeavingStation = view.findViewById(R.id.empLeavingStation);
        empStatusUpdatedBy = view.findViewById(R.id.empStatusUpdatedBy);
        empComments = view.findViewById(R.id.empComments);
        empStatusUpdatedDate = view.findViewById(R.id.empStatusUpdatedDate);
        empStatusChip = view.findViewById(R.id.empStatusChip);

        leaveBtnApprove = view.findViewById(R.id.leaveBtnApprove);
        leaveBtnReject = view.findViewById(R.id.leaveBtnReject);
        leaveBtnCancel = view.findViewById(R.id.leaveBtnCancel);

        if (item == null) {
            Toast.makeText(getContext(), "Leave data details missing", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Calculate days before populating UI
        calculateTotalDaysAndCheckLeaveLimit();
        String formattedDays = (totalDays % 1 == 0)
                ? String.valueOf((int) totalDays)
                : String.valueOf(totalDays);

        // Populate UI
        empName.setText(buildFullName(item.getEmpFirstName(), item.getEmpLastName()));
        empAvatarInitials.setText(buildInitials(item.getEmpFirstName(), item.getEmpLastName()));
        empEmail.setText(safe(item.getEmpEmail()));
        empDesignation.setText(safe(item.getDepartment()));
        empLeaveType.setText(safe(item.getLeaveName()));

        empFromDate.setText(safeFormatDate(item.getFromDate()));
        empSelectTypeFrom.setText(safe(item.getSelectTypeFrom()));
        empToDate.setText(safeFormatDate(item.getToDate()));
        empSelectTypeTo.setText(safe(item.getSelectTypeTo()));
        empAppliedDate.setText(safeFormatDate(item.getAppliedDate()));
        empTotalDays.setText(formattedDays);

        empReason.setText(safe(item.getReason()));
        empContactNumber.setText(safe(item.getContactNumber()));
        empAddress.setText(FALLBACK);
        empVacationAddress.setText(safe(item.getVacationAddress()));
        empLeavingStation.setText(safe(item.getLeavingStation()));

        empStatusUpdatedBy.setText(safe(item.getApprovedByName()));
        empStatusUpdatedDate.setText(safeFormatDate(item.getApprovedDate()));
        empComments.setText(safe(item.getComment()));

        // Status chip — null-safe, case-insensitive
        String status = item.getStatus();
        empStatusChip.setText(safe(status));
        applyChipColor(status);

        // Show action buttons only for unapproved
        if ("unapproved".equalsIgnoreCase(status)) {
            showActionButtons();

            leaveBtnApprove.setOnClickListener(v -> {
                if (!ensureManagerLoaded()) return;
                if (listener != null) listener.onApprove(item);
                handleApprove(item.getId());
            });

            leaveBtnReject.setOnClickListener(v -> {
                if (!ensureManagerLoaded()) return;
                if (listener != null) listener.onReject(item);
                showCommentPopup("reject", item.getId());
            });

            leaveBtnCancel.setOnClickListener(v -> {
                if (!ensureManagerLoaded()) return;
                if (listener != null) listener.onCancel(item);
                showCommentPopup("cancel", item.getId());
            });
        } else {
            hideActionButtons();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe profile — use current user (not cross manager) for approvedBy
        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null
                    && userProfile.getData() != null
                    && userProfile.getData().getEmployee() != null) {

                managerId = userProfile.getData().getEmployee().getId();

                String firstName = userProfile.getData().getEmployee().getFirstname();
                String lastName = userProfile.getData().getEmployee().getLastname();
                currentUserName = buildFullName(firstName, lastName);
            }
        });
    }

    // ======================== DAY CALCULATION (ported from Manager) ========================

    private long calculateTotalDays(String startDate, String endDate) {
        if (startDate == null || endDate == null) return 0;
        try {
            if (startDate.contains("T")) startDate = startDate.split("T")[0];
            if (endDate.contains("T")) endDate = endDate.split("T")[0];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDateObj = LocalDate.parse(startDate, formatter);
            LocalDate endDateObj = LocalDate.parse(endDate, formatter);

            long diff = Math.abs(ChronoUnit.DAYS.between(startDateObj, endDateObj));
            return diff + 1;
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing dates: " + e.getMessage());
            return 0;
        }
    }

    private void calculateTotalDaysAndCheckLeaveLimit() {
        if (item == null) return;

        String fromDate = item.getFromDate();
        String toDate = item.getToDate();
        String selectTypeFrom = item.getSelectTypeFrom();
        String selectTypeTo = item.getSelectTypeTo();

        if (fromDate == null || fromDate.isEmpty() || toDate == null || toDate.isEmpty()) {
            totalDays = 0;
            finalUsedDays = 0;
            return;
        }

        long tDays = calculateTotalDays(fromDate, toDate);
        totalDays = tDays;
        finalUsedDays = computeFinalUsedDays(tDays, selectTypeFrom, selectTypeTo);

        Log.d(TAG, "totalDays=" + totalDays + ", finalUsedDays=" + finalUsedDays);
    }

    private double computeFinalUsedDays(long tDays, String selectTypeFrom, String selectTypeTo) {
        double fUsedDays = tDays;

        if ("First Half Day".equalsIgnoreCase(selectTypeFrom)
                && "First Half Day".equalsIgnoreCase(selectTypeTo)) {
            fUsedDays -= 0.5;
        } else if ("First Half Day".equalsIgnoreCase(selectTypeFrom)
                && "Second Half Day".equalsIgnoreCase(selectTypeTo)) {
            fUsedDays = tDays;
        } else if ("Second Half Day".equalsIgnoreCase(selectTypeFrom)
                && "Second Half Day".equalsIgnoreCase(selectTypeTo)) {
            fUsedDays -= 0.5;
        }

        return fUsedDays;
    }

    // ======================== CHIP COLOR ========================

    private void applyChipColor(String status) {
        if (empStatusChip == null) return;
        if ("approved".equalsIgnoreCase(status)) {
            empStatusChip.setChipBackgroundColorResource(R.color.approved_color);
        } else if ("rejected".equalsIgnoreCase(status)) {
            empStatusChip.setChipBackgroundColorResource(R.color.rejected_color);
        } else if ("cancelled".equalsIgnoreCase(status)) {
            empStatusChip.setChipBackgroundColorResource(R.color.rejected_color);
        } else {
            // pending / unapproved / unknown
            empStatusChip.setChipBackgroundColorResource(R.color.pending_color);
        }
    }

    // ======================== INSTANT UI REFRESH ========================

    private void refreshUIAfterAction(String newStatus, String comment) {
        if (item == null) return;

        // Update local model
        item.setStatus(newStatus);

        // Update chip
        if (empStatusChip != null) {
            empStatusChip.setText(safe(newStatus));
            applyChipColor(newStatus);
        }

        // Update status details
        if (empStatusUpdatedBy != null) {
            empStatusUpdatedBy.setText(safe(currentUserName));
        }
        if (empStatusUpdatedDate != null) {
            empStatusUpdatedDate.setText(safeFormatDate(getCurrentDateTimeInUTC()));
        }
        if (empComments != null) {
            empComments.setText(safe(comment));
        }

        // Hide buttons immediately
        hideActionButtons();
    }

    // ======================== ACTION BUTTONS ========================

    private void showActionButtons() {
        if (leaveBtnApprove != null) leaveBtnApprove.setVisibility(View.VISIBLE);
        if (leaveBtnReject != null) leaveBtnReject.setVisibility(View.VISIBLE);
        if (leaveBtnCancel != null) leaveBtnCancel.setVisibility(View.VISIBLE);
    }

    private void hideActionButtons() {
        if (leaveBtnApprove != null) leaveBtnApprove.setVisibility(View.GONE);
        if (leaveBtnReject != null) leaveBtnReject.setVisibility(View.GONE);
        if (leaveBtnCancel != null) leaveBtnCancel.setVisibility(View.GONE);
    }

    // ======================== GUARDS ========================

    private boolean ensureManagerLoaded() {
        if (TextUtils.isEmpty(managerId)) {
            if (isAdded()) {
                Toast.makeText(requireContext(),
                        "Please wait, loading your details...",
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    // ======================== COMMENT POPUP ========================

    private void showCommentPopup(final String action, final String leaveId) {
        if (!isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.comment_popup, null);
        final TextInputEditText commentEditText = dialogView.findViewById(R.id.commentEditText);
        final TextView characterCount = dialogView.findViewById(R.id.characterCount);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(
                    AppCompatResources.getDrawable(requireContext(), R.drawable.rounded_popup_background));
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
            String comment = commentEditText.getText() == null
                    ? "" : commentEditText.getText().toString().trim();

            if ("reject".equals(action)) {
                handleReject(leaveId, comment);
            } else if ("cancel".equals(action)) {
                handleCancel(leaveId, comment);
            }
            alertDialog.dismiss();
        });

        Button backButton = dialogView.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> alertDialog.dismiss());
    }

    // ======================== APPROVE (single-step, matches Manager) ========================

    public void handleApprove(String leaveId) {
        if (item == null) return;

        LeaveUpdateRequest requestBody = new LeaveUpdateRequest();
        requestBody.setStatus("approved");
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(currentUserName);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        requestBody.setComment("");

        Call<ResponseBody> call = apiInterface.LeavesStatus("jwt " + authToken, leaveId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    refreshUIAfterAction("approved", "");
                    if (listener != null) listener.onApprove(item);
                    Toast.makeText(requireContext(),
                            "Leave Approved Successfully", Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    showErrorMessage(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Leave Approval Failed", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "handleApprove onFailure: " + throwable.getMessage());
            }
        });
    }

    // ======================== REJECT (two-step, matches Manager) ========================

    public void handleReject(String leaveId, String comment) {
        if (item == null) return;

        // Comment validation (matches Manager)
        if (comment.isEmpty()) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Comment is required", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        UsedLeaveRequest usedLeaveRequest = buildUsedLeaveRequest("rejected", comment);

        String leaveTypeId = item.getLeavetype();
        if (leaveTypeId != null && !SPECIAL_LEAVE_TYPE_ID.equals(leaveTypeId)) {
            // Step 1: LeavesUsedCount
            Call<ResponseBody> usedCountCall = apiInterface.LeavesUsedCount("jwt " + authToken, usedLeaveRequest);
            usedCountCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful()) {
                        // Step 2: LeavesStatus
                        callStatusUpdateAPI(leaveId, "rejected", comment);
                    } else {
                        showErrorMessage(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Reject failed: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Skip LeavesUsedCount for special leave type
            callStatusUpdateAPI(leaveId, "rejected", comment);
        }
    }

    // ======================== CANCEL (two-step, matches Manager) ========================

    public void handleCancel(String leaveId, String comment) {
        if (item == null) return;

        // Comment validation (matches Manager)
        if (comment.isEmpty()) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Comment is required", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        UsedLeaveRequest usedLeaveRequest = buildUsedLeaveRequest("cancelled", comment);

        String leaveTypeId = item.getLeavetype();
        if (leaveTypeId != null && !SPECIAL_LEAVE_TYPE_ID.equals(leaveTypeId)) {
            // Step 1: LeavesUsedCount
            Call<ResponseBody> usedCountCall = apiInterface.LeavesUsedCount("jwt " + authToken, usedLeaveRequest);
            usedCountCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful()) {
                        // Step 2: LeavesStatus
                        callStatusUpdateAPI(leaveId, "cancelled", comment);
                    } else {
                        showErrorMessage(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Cancel failed: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Skip LeavesUsedCount for special leave type
            callStatusUpdateAPI(leaveId, "cancelled", comment);
        }
    }

    // ======================== SHARED STATUS UPDATE (Step 2 for Reject/Cancel) ========================

    private void callStatusUpdateAPI(String leaveId, String status, String comment) {
        LeaveUpdateRequest statusUpdateBody = new LeaveUpdateRequest();
        statusUpdateBody.setStatus(status);
        statusUpdateBody.setApprovedBy(managerId);
        statusUpdateBody.setApprovedByName(currentUserName);
        statusUpdateBody.setApprovedDate(getCurrentDateTimeInUTC());
        statusUpdateBody.setComment(comment);

        Call<ResponseBody> statusUpdateCall = apiInterface.LeavesStatus("jwt " + authToken, leaveId, statusUpdateBody);
        statusUpdateCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    refreshUIAfterAction(status, comment);

                    if (listener != null) {
                        if ("cancelled".equalsIgnoreCase(status)) {
                            listener.onCancel(item);
                        } else if ("rejected".equalsIgnoreCase(status)) {
                            listener.onReject(item);
                        }
                    }

                    String displayStatus = status.substring(0, 1).toUpperCase() + status.substring(1);
                    Toast.makeText(requireContext(),
                            "Leave " + displayStatus + " Successfully", Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    showErrorMessage(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Status update failed: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ======================== BUILD USED LEAVE REQUEST ========================

    private UsedLeaveRequest buildUsedLeaveRequest(String status, String comment) {
        UsedLeaveRequest requestBody = new UsedLeaveRequest();
        requestBody.setId(item.getId());
        requestBody.setEmployeeCode(""); // Not available in CM model
        requestBody.setDesignation(item.getDepartment());
        requestBody.setFirstname(item.getEmpFirstName());
        requestBody.setLastname(item.getEmpLastName());
        requestBody.setEmail(item.getEmpEmail());
        requestBody.setContact(item.getContactNumber());
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

        // Build minimal Leavetype object from CM string fields
        if (item.getLeavetype() != null) {
            app.xedigital.ai.model.usedLeave.Leavetype targetLeaveType =
                    new app.xedigital.ai.model.usedLeave.Leavetype();
            targetLeaveType.setId(item.getLeavetype());
            targetLeaveType.setLeavetypeName(item.getLeaveName());
            requestBody.setLeavetype(targetLeaveType);
        }

        requestBody.setCreatedAt(item.getCreatedAt());
        requestBody.setUpdatedAt(item.getUpdatedAt());
        requestBody.setStatus(status);
        requestBody.setApprovedBy(managerId);
        requestBody.setApprovedByName(currentUserName);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        requestBody.setComment(comment);
        requestBody.setTDays((int) Math.ceil(totalDays));
        requestBody.setFUsedDays(finalUsedDays);

        return requestBody;
    }

    // ======================== NAVIGATION & ERROR ========================

    private void navigateBack() {
        try {
            if (getView() != null) {
                Navigation.findNavController(getView())
                        .navigate(R.id.action_nav_pendingCMLeaveApproval_to_nav_cross_approval_leave);
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation error: " + e.getMessage());
        }
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
        if (isAdded()) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, "Error: " + errorMessage);
    }

    // ======================== INTERFACE ========================

    public interface OnLeaveApprovalActionListener {
        void onApprove(AppliedLeavesItem item);

        void onReject(AppliedLeavesItem item);

        void onCancel(AppliedLeavesItem item);
    }
}