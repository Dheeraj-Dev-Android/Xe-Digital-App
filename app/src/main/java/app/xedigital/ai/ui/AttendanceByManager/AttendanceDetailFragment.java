package app.xedigital.ai.ui.AttendanceByManager;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import app.xedigital.ai.R;
import app.xedigital.ai.model.AttandanceByManager.EmployeePunchDataItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AttendanceDetailFragment extends Fragment {

    private static final String TAG = "AttendanceDetail";
    private static final String ARG_ATTENDANCE_ITEM = "attendance_item";

    private TextView nameTextView, dateTextView, punchInTime, punchOutTime;
    private TextView totalTime, overtimeTime, lateTime;
    private TextView shiftDetail, addressDetailIn, addressDetailOut;

    public AttendanceDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendance_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        EmployeePunchDataItem item = null;
        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item = getArguments().getSerializable(ARG_ATTENDANCE_ITEM, EmployeePunchDataItem.class);
            } else {
                item = (EmployeePunchDataItem) getArguments().getSerializable(ARG_ATTENDANCE_ITEM);
            }
        }

        if (item != null) {
            bindData(item);
        } else {
            Navigation.findNavController(view).navigateUp();
        }
    }

    private void initViews(View view) {
        nameTextView = view.findViewById(R.id.nameTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        punchInTime = view.findViewById(R.id.punchInTime);
        punchOutTime = view.findViewById(R.id.punchOutTime);
        totalTime = view.findViewById(R.id.totalTime);
        overtimeTime = view.findViewById(R.id.overtimeTime);
        lateTime = view.findViewById(R.id.lateTime);
        shiftDetail = view.findViewById(R.id.shiftDetail);
        addressDetailIn = view.findViewById(R.id.addressDetailIn);
        addressDetailOut = view.findViewById(R.id.addressDetailOut);
    }

    private void bindData(EmployeePunchDataItem item) {
        // Employee name — same pattern as adapter
        if (item.getEmployee() != null) {
            String fName = item.getEmployee().getFirstname() != null ? item.getEmployee().getFirstname().trim() : "";
            String lName = item.getEmployee().getLastname() != null ? item.getEmployee().getLastname().trim() : "";
            String fullName = (fName + " " + lName).trim();
            nameTextView.setText(fullName.isEmpty() ? "Employee" : fullName);
        } else {
            nameTextView.setText("Employee");
        }

        // Date
        dateTextView.setText(item.getPunchDateFormat());

        // Punch times
        punchInTime.setText(DateTimeUtils.formatTime(item.getPunchIn()));

        String pOut = item.getPunchOut();
        punchOutTime.setText((pOut != null && !pOut.isEmpty()) ? DateTimeUtils.formatTime(pOut) : "N/A");

        // Calculated stats
        totalTime.setText(item.getTotalTime() != null ? item.getTotalTime() : "00:00");
        overtimeTime.setText(item.getOvertime() != null ? item.getOvertime() : "00:00");
        lateTime.setText(item.getLateTime() != null ? item.getLateTime() : "00:00");

        // Shift
        if (item.getShift() != null) {
            String shiftRange = item.getShift().getStartTime() + " - " + item.getShift().getEndTime();
            shiftDetail.setText(shiftRange);
        }

        // Addresses
        addressDetailIn.setText(getValidString(item.getPunchInAddress()));
        addressDetailOut.setText(getValidString(item.getPunchOutAddress()));
    }

    private String getValidString(String val) {
        return (val == null || val.isEmpty()) ? "Not Available" : val;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        nameTextView = null;
        dateTextView = null;
        punchInTime = null;
        punchOutTime = null;
        totalTime = null;
        overtimeTime = null;
        lateTime = null;
        shiftDetail = null;
        addressDetailIn = null;
        addressDetailOut = null;
    }
}