package app.xedigital.ai.ui.leaves;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
            binding.leaveNameTextView.setText(appliedLeaveItem.getLeaveName());
            binding.appliedDateTextView.setText("Applied on " + DateTimeUtils.getDayOfWeekAndDate(appliedLeaveItem.getAppliedDate()));
            binding.startDateTextView.setText(DateTimeUtils.getDayOfWeekAndDate(appliedLeaveItem.getFromDate()));
            binding.startDateSelectedType.setText(appliedLeaveItem.getSelectTypeFrom());
            binding.endDateTextView.setText(DateTimeUtils.getDayOfWeekAndDate(appliedLeaveItem.getToDate()));
            binding.endDateSelectedType.setText(appliedLeaveItem.getSelectTypeTo());
            binding.totalDaysTextView.setText("Total Days : " + getTotalDays(appliedLeaveItem));
            binding.reasonTextView.setText(appliedLeaveItem.getReason());
            binding.approvedByTextView.setText(appliedLeaveItem.getApprovedByName());
            binding.approvedDateTextView.setText("Updated on : " + DateTimeUtils.getDayOfWeekAndDate(appliedLeaveItem.getApprovedDate()));
            binding.approvedComment.setText("Comment : " + appliedLeaveItem.getComment());
            binding.leavingStationTextView.setText(appliedLeaveItem.getLeavingStation());
            binding.LeavingStationAddress.setText("Vacation Address : " + appliedLeaveItem.getVacationAddress());

            // Set status chip text and color based on leave status
            String status = appliedLeaveItem.getStatus();
            binding.statusChip.setText(status);
            if (status.equalsIgnoreCase("Approved")) {
                binding.statusChip.setChipBackgroundColorResource(R.color.status_approved);
                binding.statusChip.setTextColor(getResources().getColor(R.color.white));
            } else if (status.equalsIgnoreCase("UnApproved")) {
                binding.statusChip.setChipBackgroundColorResource(R.color.status_pending);
                binding.statusChip.setTextColor(getResources().getColor(R.color.white));
            } else if (status.equalsIgnoreCase("Rejected")) {
                binding.statusChip.setChipBackgroundColorResource(R.color.status_rejected);
                binding.statusChip.setTextColor(getResources().getColor(R.color.white));
            } else if (status.equalsIgnoreCase("Cancelled")) {
                binding.statusChip.setChipBackgroundColorResource(R.color.status_rejected);
                binding.statusChip.setTextColor(getResources().getColor(R.color.white));
            }
        }
        return view;
    }

    private int getTotalDays(AppliedLeavesItem appliedLeave) {
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
}