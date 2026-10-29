package app.xedigital.ai.ui.leaves;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.AppliedLeaveItemBinding;
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AppliedViewFragment extends Fragment {

    public static final String ARG_APPLIED_LEAVE = "applied_leave_item";
    private static final String TAG = "AppliedViewFragment";

    private AppliedLeavesItem appliedLeaveItem;
    private AppliedLeaveItemBinding binding;

    public AppliedViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                appliedLeaveItem = getArguments().getSerializable(ARG_APPLIED_LEAVE, AppliedLeavesItem.class);
            } else {
                @SuppressWarnings("deprecation")
                AppliedLeavesItem oldItem = (AppliedLeavesItem) getArguments().getSerializable(ARG_APPLIED_LEAVE);
                appliedLeaveItem = oldItem;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AppliedLeaveItemBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (appliedLeaveItem != null) {
            // Text values with null checks
            binding.leaveNameTextView.setText(getValueOrNA(appliedLeaveItem.getLeaveName()));
            binding.startDateSelectedType.setText(getValueOrNA(appliedLeaveItem.getSelectTypeFrom()));
            binding.endDateSelectedType.setText(getValueOrNA(appliedLeaveItem.getSelectTypeTo()));
            binding.reasonTextView.setText(getValueOrNA(appliedLeaveItem.getReason()));
            binding.approvedByTextView.setText(getValueOrNA(appliedLeaveItem.getApprovedByName()));
            binding.approvedComment.setText(getValueOrNA(appliedLeaveItem.getComment()));
            binding.leavingStationTextView.setText(getValueOrNA(appliedLeaveItem.getLeavingStation()));
            binding.LeavingStationAddress.setText(getValueOrNA(appliedLeaveItem.getVacationAddress()));
            binding.plannedLeaveTextView.setText(getValueOrNA(appliedLeaveItem.getLeavePlanned()));

            // Date values
            String appliedDateFormatted = formatDateOrNA(appliedLeaveItem.getAppliedDate());
            binding.appliedDateTextView.setText(
                    "N/A".equals(appliedDateFormatted) ? "N/A" : "Applied on " + appliedDateFormatted);

            binding.startDateTextView.setText(formatDateOrNA(appliedLeaveItem.getFromDate()));
            binding.endDateTextView.setText(formatDateOrNA(appliedLeaveItem.getToDate()));
            binding.approvedDateTextView.setText(formatDateOrNA(appliedLeaveItem.getApprovedDate()));

            // Total days — matches Manager calculation (calendar days + half-day adjustments)
            double totalDays = calculateTotalDays();
            double finalDays = computeFinalUsedDays(totalDays);

            String formattedDays;
            if (finalDays <= 0) {
                formattedDays = "N/A";
            } else if (finalDays % 1 == 0) {
                formattedDays = (int) finalDays + " Days";
            } else {
                formattedDays = finalDays + " Days";
            }
            binding.totalDaysTextView.setText(formattedDays);

            // Status Chip binding
            String status = appliedLeaveItem.getStatus();
            binding.statusChip.setText(getValueOrNA(status));

            if (status != null) {
                String statusLower = status.toLowerCase();
                if ("approved".equals(statusLower)) {
                    binding.statusChip.setChipBackgroundColorResource(R.color.status_approved);
                } else if ("unapproved".equals(statusLower)) {
                    binding.statusChip.setChipBackgroundColorResource(R.color.status_pending);
                } else if ("rejected".equals(statusLower) || "cancelled".equals(statusLower)) {
                    binding.statusChip.setChipBackgroundColorResource(R.color.status_rejected);
                } else {
                    // Fallback for unknown status
                    binding.statusChip.setChipBackgroundColorResource(R.color.status_pending);
                }
                binding.statusChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
        }
        return view;
    }

    // ======================== DAY CALCULATION (matches Manager) ========================

    /**
     * Matches Manager: ChronoUnit.DAYS.between() + 1 (calendar days, includes weekends).
     */
    private double calculateTotalDays() {
        String fromDate = appliedLeaveItem.getFromDate();
        String toDate = appliedLeaveItem.getToDate();

        if (fromDate == null || toDate == null) return 0;

        try {
            if (fromDate.contains("T")) fromDate = fromDate.split("T")[0];
            if (toDate.contains("T")) toDate = toDate.split("T")[0];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(fromDate, formatter);
            LocalDate endDate = LocalDate.parse(toDate, formatter);

            long diff = Math.abs(ChronoUnit.DAYS.between(startDate, endDate));
            return diff + 1;
        } catch (DateTimeParseException e) {
            Log.e(TAG, "calculateTotalDays parse error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Matches Manager: half-day adjustments based on selectTypeFrom/selectTypeTo.
     */
    private double computeFinalUsedDays(double tDays) {
        String selectTypeFrom = appliedLeaveItem.getSelectTypeFrom();
        String selectTypeTo = appliedLeaveItem.getSelectTypeTo();

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

    // ======================== HELPERS ========================

    private String getValueOrNA(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "N/A";
    }

    private String formatDateOrNA(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "N/A";
        try {
            String formatted = DateTimeUtils.getDayOfWeekAndDate(dateStr);
            return (formatted != null && !formatted.trim().isEmpty()) ? formatted : "N/A";
        } catch (Exception e) {
            Log.e(TAG, "formatDateOrNA error: " + e.getMessage());
            return "N/A";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}