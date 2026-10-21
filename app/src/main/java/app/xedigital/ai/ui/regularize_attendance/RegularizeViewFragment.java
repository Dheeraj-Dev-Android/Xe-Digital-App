package app.xedigital.ai.ui.regularize_attendance;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.regularizeApplied.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.model.regularizeUpdateStatus.RegularizeUpdateRequest;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class RegularizeViewFragment extends Fragment {

    public static final String ARG_REGULARIZE_APPLIED_ITEM = "regularize_applied_item";

    private AttendanceRegularizeAppliedItem regularizeAppliedItem;
    private Button cancelButton;

    public RegularizeViewFragment() {
        // Required empty public constructor
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateTimeFormat.format(currentDateTime);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            regularizeAppliedItem = (AttendanceRegularizeAppliedItem) getArguments().getSerializable(ARG_REGULARIZE_APPLIED_ITEM);
        }
    }

    private String getSafeString(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_regularize, container, false);
        cancelButton = view.findViewById(R.id.cancel_button);

        if (regularizeAppliedItem != null) {
            AttendanceRegularizeAppliedItem item = regularizeAppliedItem;

            // Employee Name
            TextView empName = view.findViewById(R.id.empName);
            if (item.getEmployee() != null) {
                empName.setText(getSafeString(item.getEmployee().getFullname()));
            } else {
                empName.setText("N/A");
            }

            // Employee Email
            TextView empEmail = view.findViewById(R.id.empEmail);
            if (item.getEmployee() != null) {
                empEmail.setText("Email : " + getSafeString(item.getEmployee().getEmail()));
            } else {
                empEmail.setText("Email : N/A");
            }

            // Punch Date
            TextView empPunchDate = view.findViewById(R.id.empPunchDate);
            String punchDate = item.getPunchDate();
            empPunchDate.setText(punchDate != null ? DateTimeUtils.getDayOfWeekAndDate(punchDate) : "N/A");

            // Shift
            TextView empShift = view.findViewById(R.id.empShift);
            if (item.getShift() != null) {
                String shiftName = getSafeString(item.getShift().getName());
                String startTime = getSafeString(item.getShift().getStartTime());
                String endTime = getSafeString(item.getShift().getEndTime());
                empShift.setText(shiftName + " (" + startTime + " - " + endTime + ")");
            } else {
                empShift.setText("N/A");
            }

            // Punch In
            TextView empPunchIn = view.findViewById(R.id.empPunchIn);
            String punchIn = item.getPunchIn();
            empPunchIn.setText(punchIn != null ? DateTimeUtils.formatTime(punchIn) : "N/A");

            // Punch Out
            TextView empPunchOut = view.findViewById(R.id.empPunchOut);
            String punchOut = item.getPunchOut();
            empPunchOut.setText(punchOut != null ? DateTimeUtils.formatTime(punchOut) : "N/A");

            // Punch In Address
            TextView empPunchInAddress = view.findViewById(R.id.empPunchInAddress);
            empPunchInAddress.setText(getSafeString(item.getPunchInAddress()));

            // Punch Out Address
            TextView empPunchOutAddress = view.findViewById(R.id.empPunchOutAddress);
            empPunchOutAddress.setText(getSafeString(item.getPunchOutAddress()));

            // Applied Punch In
            TextView appliedPunchIn = view.findViewById(R.id.appliedPunchIn);
            String punchInUpdated = item.getPunchInUpdated();
            appliedPunchIn.setText(punchInUpdated != null ? DateTimeUtils.formatTime(punchInUpdated) : "N/A");

            // Applied Punch Out
            TextView appliedPunchOut = view.findViewById(R.id.appliedPunchOut);
            String punchOutUpdated = item.getPunchOutUpdated();
            appliedPunchOut.setText(punchOutUpdated != null ? DateTimeUtils.formatTime(punchOutUpdated) : "N/A");

            // Applied Punch In Address
            TextView appliedPunchInAddress = view.findViewById(R.id.appliedPunchInAddress);
            appliedPunchInAddress.setText(getSafeString(item.getPunchInAddressUpdated()));

            // Applied Punch Out Address
            TextView appliedPunchOutAddress = view.findViewById(R.id.appliedPunchOutAddress);
            appliedPunchOutAddress.setText(getSafeString(item.getPunchOutAddressUpdated()));

            // Applied Date
            TextView appliedDate = view.findViewById(R.id.appliedDate);
            String appliedDateStr = item.getAppliedDate();
            appliedDate.setText(appliedDateStr != null ? DateTimeUtils.getDayOfWeekAndDate(appliedDateStr) : "N/A");

            // Status Chip
            Chip appliedStatusChip = view.findViewById(R.id.appliedStatus);
            String status = getSafeString(item.getStatus());
            appliedStatusChip.setText(status);

            int chipColor;
            if (status.equalsIgnoreCase("Approved")) {
                chipColor = R.color.status_approved;
                appliedStatusChip.setChipIconResource(R.drawable.ic_check_circle);
            } else if (status.equalsIgnoreCase("Rejected")) {
                chipColor = R.color.status_rejected;
                appliedStatusChip.setChipIcon(null);
            } else if (status.equalsIgnoreCase("Cancelled")) {
                chipColor = R.color.status_rejected;
                appliedStatusChip.setChipIcon(null);
            } else {
                chipColor = R.color.status_pending;
                appliedStatusChip.setChipIcon(null);
            }

            appliedStatusChip.setChipBackgroundColorResource(chipColor);
            appliedStatusChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            // Hide cancel button if already cancelled, rejected, or approved
            if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Approved")) {
                if (cancelButton != null) {
                    cancelButton.setVisibility(View.GONE);
                }
            }

            // Updated By
            TextView appliedStatusUpdateBy = view.findViewById(R.id.appliedStatusUpdateBy);
            appliedStatusUpdateBy.setText(getSafeString(item.getApprovedByName()));

            // Updated Date
            TextView appliedStatusUpdateDate = view.findViewById(R.id.appliedStatusUpdateDate);
            String approvedDate = item.getApprovedDate();
            appliedStatusUpdateDate.setText(approvedDate != null ? DateTimeUtils.getDayOfWeekAndDate(approvedDate) : "N/A");
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> cancelRegularizeRequest());
        }

        return view;
    }

    private void cancelRegularizeRequest() {
        if (!isAdded() || regularizeAppliedItem == null) return;

        String attendanceId = regularizeAppliedItem.getId();
        if (attendanceId == null) return;

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");

        // Build request body with status changed to "cancelled"
        RegularizeUpdateRequest requestBody = new RegularizeUpdateRequest();
        requestBody.setStatus("cancelled");
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());

        final String approverName;
        if (regularizeAppliedItem.getEmployee() != null) {
            approverName = getSafeString(regularizeAppliedItem.getEmployee().getFullname());
        } else {
            approverName = "N/A";
        }
        requestBody.setApprovedByName(approverName);

        APIInterface apiService = APIClient.getInstance().getRegularizeApplied();
        Call<ResponseBody> call = apiService.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Request cancelled successfully", Toast.LENGTH_SHORT).show();

                    // Update UI in-place
                    if (cancelButton != null) {
                        cancelButton.setVisibility(View.GONE);
                    }

                    View view = getView();
                    if (view != null) {
                        Chip appliedStatusChip = view.findViewById(R.id.appliedStatus);
                        if (appliedStatusChip != null) {
                            appliedStatusChip.setText("Cancelled");
                            appliedStatusChip.setChipBackgroundColorResource(R.color.status_rejected);
                            appliedStatusChip.setChipIcon(null);
                        }

                        TextView appliedStatusUpdateBy = view.findViewById(R.id.appliedStatusUpdateBy);
                        if (appliedStatusUpdateBy != null) {
                            appliedStatusUpdateBy.setText(approverName);
                        }

                        TextView appliedStatusUpdateDate = view.findViewById(R.id.appliedStatusUpdateDate);
                        if (appliedStatusUpdateDate != null) {
                            appliedStatusUpdateDate.setText(
                                    DateTimeUtils.getDayOfWeekAndDate(getCurrentDateTimeInUTC()));
                        }
                    }
                } else {
                    Log.e("CancelRegularize", "Error: " + response.message());
                    Toast.makeText(requireContext(), "Error cancelling request", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("CancelRegularize", "Error: " + t.getMessage());
                Toast.makeText(requireContext(), "Error cancelling request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelButton = null;
    }
}