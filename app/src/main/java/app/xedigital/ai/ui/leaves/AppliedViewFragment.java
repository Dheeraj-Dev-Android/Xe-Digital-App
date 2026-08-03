package app.xedigital.ai.ui.leaves;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.AppliedLeaveItemBinding;
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AppliedViewFragment extends Fragment {
    public static final String ARG_APPLIED_LEAVE = "applied_leave_item";
    private AppliedLeavesItem appliedLeaveItem;
    private AppliedLeaveItemBinding binding;

    public AppliedViewFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appliedLeaveItem = (AppliedLeavesItem) getArguments().getSerializable(ARG_APPLIED_LEAVE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AppliedLeaveItemBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (appliedLeaveItem != null) {
            // Text values with simple null checks
            binding.leaveNameTextView.setText(getValueOrNA(appliedLeaveItem.getLeaveName()));
            binding.startDateSelectedType.setText(getValueOrNA(appliedLeaveItem.getSelectTypeFrom()));
            binding.endDateSelectedType.setText(getValueOrNA(appliedLeaveItem.getSelectTypeTo()));
            binding.reasonTextView.setText(getValueOrNA(appliedLeaveItem.getReason()));
            binding.approvedByTextView.setText(getValueOrNA(appliedLeaveItem.getApprovedByName()));
            binding.approvedComment.setText(getValueOrNA(appliedLeaveItem.getComment()));
            binding.leavingStationTextView.setText(getValueOrNA(appliedLeaveItem.getLeavingStation()));
            binding.LeavingStationAddress.setText(getValueOrNA(appliedLeaveItem.getVacationAddress()));
            binding.plannedLeaveTextView.setText(getValueOrNA(appliedLeaveItem.getLeavePlanned()));

            // Date values with null checks
            String appliedDateFormatted = formatDateOrNA(appliedLeaveItem.getAppliedDate());
            binding.appliedDateTextView.setText(appliedDateFormatted.equals("N/A") ? "N/A" : "Applied on " + appliedDateFormatted);

            binding.startDateTextView.setText(formatDateOrNA(appliedLeaveItem.getFromDate()));
            binding.endDateTextView.setText(formatDateOrNA(appliedLeaveItem.getToDate()));
            binding.approvedDateTextView.setText(formatDateOrNA(appliedLeaveItem.getApprovedDate()));

            // Total days with null check
            int totalDays = getTotalDays(appliedLeaveItem);
            binding.totalDaysTextView.setText(totalDays > 0 ? totalDays + " Days" : "N/A");

            // Status Chip binding
            String status = appliedLeaveItem.getStatus();
            binding.statusChip.setText(getValueOrNA(status));

            if (status != null) {
                if (status.equalsIgnoreCase("Approved")) {
                    binding.statusChip.setChipBackgroundColorResource(R.color.status_approved);
                    binding.statusChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                } else if (status.equalsIgnoreCase("UnApproved")) {
                    binding.statusChip.setChipBackgroundColorResource(R.color.status_pending);
                    binding.statusChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                } else if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Cancelled")) {
                    binding.statusChip.setChipBackgroundColorResource(R.color.status_rejected);
                    binding.statusChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                }
            }
        }
        return view;
    }

    private String getValueOrNA(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "N/A";
    }

    private String formatDateOrNA(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "N/A";
        }
        String formatted = DateTimeUtils.getDayOfWeekAndDate(dateStr);
        return (formatted != null && !formatted.trim().isEmpty()) ? formatted : "N/A";
    }

    private int getTotalDays(AppliedLeavesItem appliedLeave) {
        if (appliedLeave == null || appliedLeave.getFromDate() == null || appliedLeave.getToDate() == null) {
            return 0;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = dateFormat.parse(appliedLeave.getFromDate());
            Date endDate = dateFormat.parse(appliedLeave.getToDate());

            if (startDate == null || endDate == null) {
                return 0;
            }

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);

            int totalDays = 0;
            while (!startCal.after(endCal)) {
                int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                    totalDays++;
                }
                startCal.add(Calendar.DATE, 1);
            }

            return totalDays;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}