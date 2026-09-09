package app.xedigital.ai.ui.regularize_attendance;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.CFregularizeAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentPendingCMFAttendanceApproveBinding;
import app.xedigital.ai.model.cfRegularizeApproval.AttendanceRegItem;
import app.xedigital.ai.model.regularizeUpdateStatus.RegularizeUpdateRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingCMFAttendanceApproveFragment extends Fragment {

    public static final String ARG_ATTENDANCE_REG_ITEM = CFregularizeAdapter.ARG_ATTENDANCE_REG_ITEM;
    public static final String REQUEST_KEY_REGULARIZE_REFRESH = "request_key_regularize_refresh";
    public static final String BUNDLE_KEY_IS_UPDATED = "is_updated";

    private static final String TAG = "PendingCMFApprovalFrag";

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private FragmentPendingCMFAttendanceApproveBinding binding;
    private AttendanceRegItem item;
    private APIInterface apiInterface;
    private ProfileViewModel profileViewModel;
    private String approverName;
    private SecurePrefManager prefManager;
    private OnCMFRegularizeApprovalActionListener listener;

    public PendingCMFAttendanceApproveFragment() {
        // Required empty public constructor
    }

    public static PendingCMFAttendanceApproveFragment newInstance(AttendanceRegItem item) {
        PendingCMFAttendanceApproveFragment fragment = new PendingCMFAttendanceApproveFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ATTENDANCE_REG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    public static String getCurrentDateTimeInUTC() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateTimeFormat.format(new Date());
    }

    public void setListener(OnCMFRegularizeApprovalActionListener listener) {
        this.listener = listener;
    }

    // ─────────────────────────────────────────────────────────────────
    // Lifecycle 
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (AttendanceRegItem) getArguments().getSerializable(ARG_ATTENDANCE_REG_ITEM);
        }

        prefManager = SecurePrefManager.getInstance(requireContext());
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        apiInterface = APIClient.getInstance().UpdateRegularizeListApproval();

        String userId = prefManager.getString("userId", "");
        String authToken = prefManager.getString("authToken", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPendingCMFAttendanceApproveBinding.inflate(inflater, container, false);

        if (item != null) {
            populateUI();
        } else {
            Log.e(TAG, "onCreateView: AttendanceRegItem is null");
        }

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null) {
                String firstName = userProfile.getData().getEmployee().getFirstname();
                String lastName = userProfile.getData().getEmployee().getLastname();

                if (firstName != null && lastName != null) {
                    approverName = firstName.trim() + " " + lastName.trim();
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ─────────────────────────────────────────────────────────────────
    // UI Auto-Refresh Renderer
    // ─────────────────────────────────────────────────────────────────

    private void populateUI() {
        if (item == null || binding == null) return;

        // Employee Details
        if (item.getEmployee() != null) {
            binding.empName.setText(safeText(item.getEmployee().getFullname()));
            binding.empEmail.setText(safeText(item.getEmployee().getEmail()));
            binding.empContact.setText(safeText(item.getEmployee().getContact()));
        } else {
            binding.empName.setText("N/A");
            binding.empEmail.setText("N/A");
            binding.empContact.setText("N/A");
        }

        // Punch Date
        binding.empPunchDate.setText(formatOrFallbackDate(item.getPunchDate()));

        // Shift Details
        if (item.getShift() != null) {
            String shiftName = safeText(item.getShift().getName());
            String startTime = safeText(item.getShift().getStartTime());
            String endTime = safeText(item.getShift().getEndTime());

            if (shiftName.equals("N/A") && startTime.equals("N/A") && endTime.equals("N/A")) {
                binding.empShift.setText("N/A");
            } else if (!shiftName.equals("N/A")) {
                binding.empShift.setText(String.format(Locale.US, "%s (%s - %s)", shiftName, startTime, endTime));
            } else {
                binding.empShift.setText(String.format(Locale.US, "%s - %s", startTime, endTime));
            }
        } else {
            binding.empShift.setText("N/A");
        }

        // Original Punch Timings & Locations
        binding.empPunchIn.setText(formatOrFallbackTime(item.getPunchIn()));
        binding.empPunchOut.setText(formatOrFallbackTime(item.getPunchOut()));
        binding.empPunchInAddress.setText(safeText(item.getPunchInAddress()));
        binding.empPunchOutAddress.setText(safeText(item.getPunchOutAddress()));

        // Requested Timings & Locations
        binding.appliedPunchIn.setText(formatOrFallbackTime(item.getPunchInUpdated()));
        binding.appliedPunchOut.setText(formatOrFallbackTime(item.getPunchOutUpdated()));
        binding.appliedPunchInAddress.setText(safeText(item.getPunchInAddressUpdated()));
        binding.appliedPunchOutAddress.setText(safeText(item.getPunchOutAddressUpdated()));
        binding.appliedAttendanceRemarks.setText(safeText(item.getAttendenceRegularizationRemark()));

        // Auto-Refreshed Approval Metadata Log
        binding.appliedDate.setText(formatOrFallbackDate(item.getAppliedDate()));
        binding.appliedStatusUpdateBy.setText(safeText(item.getApprovedByName()));
        binding.appliedStatusUpdateDate.setText(formatOrFallbackDate(item.getApprovedDate()));

        // Status Text & Color
        String status = item.getStatus();
        binding.appliedStatus.setText(safeText(status));
        applyStatusColor(status);

        // Action Buttons Card Visibility: Automatically hides once approved/rejected
        if (status != null && status.equalsIgnoreCase("unapproved")) {
            binding.actionButtonsCard.setVisibility(View.VISIBLE);
            binding.approveButton.setOnClickListener(v -> submitApprovalStatus("approved", "Attendance Approved Successfully"));
            binding.rejectButton.setOnClickListener(v -> submitApprovalStatus("rejected", "Attendance Rejected Successfully"));
        } else {
            binding.actionButtonsCard.setVisibility(View.GONE);
        }
    }

    private void applyStatusColor(String status) {
        if (status == null || getContext() == null) return;

        int colorRes;
        if (status.equalsIgnoreCase("unapproved")) {
            colorRes = R.color.pending_status_color;
        } else if (status.equalsIgnoreCase("approved")) {
            colorRes = R.color.status_approved;
        } else if (status.equalsIgnoreCase("rejected")) {
            colorRes = R.color.status_rejected;
        } else {
            colorRes = android.R.color.black;
        }

        binding.appliedStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    // ─────────────────────────────────────────────────────────────────
    // Build Exact Web Payload
    // ─────────────────────────────────────────────────────────────────

    private RegularizeUpdateRequest buildRequestBody(String status, String approverName, String approvalDate) {
        JsonObject jsonObject = gson.toJsonTree(item).getAsJsonObject();

        jsonObject.addProperty("status", status);
        jsonObject.addProperty("approvedByName", approverName);
        jsonObject.addProperty("approvedDate", approvalDate);

        if (item.getPunchOutUpdated() != null) {
            jsonObject.addProperty("punchOutUpdatedDate", extractDateOnly(item.getPunchOutUpdated()));
        }
        if (item.getPunchOut() != null) {
            jsonObject.addProperty("punchOutDate", extractDateOnly(item.getPunchOut()));
        }

        return gson.fromJson(jsonObject, RegularizeUpdateRequest.class);
    }

    // ─────────────────────────────────────────────────────────────────
    // Submission Execution & Auto-Refresh Logic
    // ─────────────────────────────────────────────────────────────────

    private void submitApprovalStatus(String status, String successMessage) {
        String finalApprover = resolveApproverIdentity();

        if (finalApprover == null || finalApprover.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Error: Approver Identity missing. Request aborted.", Toast.LENGTH_LONG).show();
            return;
        }

        toggleButtonsInteraction(false);

        String approvalTimestamp = getCurrentDateTimeInUTC();
        RegularizeUpdateRequest requestBody = buildRequestBody(status, finalApprover, approvalTimestamp);
        String authToken = prefManager.getString("authToken", "");

        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, item.getId(), requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();

                    // 1. AUTO-REFRESH LOCAL STATE
                    item.setStatus(status);
                    item.setApprovedByName(finalApprover);
                    item.setApprovedDate(approvalTimestamp);

                    // 2. RE-RENDER UI INSTANTLY
                    populateUI();

                    // 3. BROADCAST RESULT TO PARENT SCREEN TO AUTO-REFRESH LIST
                    Bundle result = new Bundle();
                    result.putBoolean(BUNDLE_KEY_IS_UPDATED, true);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY_REGULARIZE_REFRESH, result);

                    if (listener != null) {
                        if ("approved".equalsIgnoreCase(status)) {
                            listener.onApprove(item);
                        } else {
                            listener.onReject(item);
                        }
                    }

                    // 4. (OPTIONAL) AUTO NAVIGATE BACK AFTER 1 SECOND
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded() && getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        }
                    }, 1000);

                } else {
                    toggleButtonsInteraction(true);
                    Toast.makeText(requireContext(), "Processing Failed: Code " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (!isAdded() || binding == null) return;
                toggleButtonsInteraction(true);
                Toast.makeText(requireContext(), "Network Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleButtonsInteraction(boolean interactable) {
        if (binding != null) {
            binding.approveButton.setEnabled(interactable);
            binding.rejectButton.setEnabled(interactable);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    private String resolveApproverIdentity() {
        if (approverName != null && !approverName.trim().isEmpty()) {
            return approverName;
        } else if (item != null && item.getApprovedByName() != null && !item.getApprovedByName().trim().isEmpty()) {
            return item.getApprovedByName();
        } else {
            return prefManager.getString("username", "");
        }
    }

    private String extractDateOnly(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.length() < 10) return null;
        return isoDateTime.substring(0, 10);
    }

    private String safeText(String input) {
        return (input != null && !input.trim().isEmpty() && !input.equalsIgnoreCase("null")) ? input : "N/A";
    }

    private String formatOrFallbackDate(String rawDate) {
        if (rawDate != null && !rawDate.trim().isEmpty() && !rawDate.equalsIgnoreCase("null")) {
            String formatted = DateTimeUtils.getDayOfWeekAndDate(rawDate);
            return (formatted != null && !formatted.trim().isEmpty()) ? formatted : "N/A";
        }
        return "N/A";
    }

    private String formatOrFallbackTime(String rawTime) {
        if (rawTime != null && !rawTime.trim().isEmpty() && !rawTime.equalsIgnoreCase("null")) {
            String formatted = DateTimeUtils.formatTime(rawTime);
            return (formatted != null && !formatted.trim().isEmpty()) ? formatted : "N/A";
        }
        return "N/A";
    }

    public interface OnCMFRegularizeApprovalActionListener {
        void onApprove(AttendanceRegItem item);

        void onReject(AttendanceRegItem item);
    }
}