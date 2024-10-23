package app.xedigital.ai.ui.regularize_attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;

import app.xedigital.ai.R;
import app.xedigital.ai.model.regularizeApplied.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class RegularizeViewFragment extends Fragment {

    public static final String ARG_REGULARIZE_APPLIED_ITEM = "regularize_applied_item";

    private AttendanceRegularizeAppliedItem regularizeAppliedItem;

    public RegularizeViewFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            regularizeAppliedItem = (AttendanceRegularizeAppliedItem) getArguments().getSerializable(ARG_REGULARIZE_APPLIED_ITEM);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_regularize, container, false);
        if (regularizeAppliedItem != null) {
            AttendanceRegularizeAppliedItem item = regularizeAppliedItem;

            TextView empName = view.findViewById(R.id.empName);
            empName.setText("Name : " + item.getEmployee().getFullname());
            TextView empEmail = view.findViewById(R.id.empEmail);
            empEmail.setText("Email : " + item.getEmployee().getEmail());

            TextView empPunchDate = view.findViewById(R.id.empPunchDate);
            String punchDate = item.getPunchDate();
            String formattedPunchDate = DateTimeUtils.getDayOfWeekAndDate(punchDate);
            empPunchDate.setText("Punch Date : " + formattedPunchDate);

            TextView empShift = view.findViewById(R.id.empShift);
            empShift.setText(item.getShift().getName() + " (" + item.getShift().getStartTime() + " - " + item.getShift().getEndTime() + ")");

            TextView empPunchIn = view.findViewById(R.id.empPunchIn);
            String punchIn = item.getPunchIn();
            String formattedPunchIn = DateTimeUtils.formatTime(punchIn);
            empPunchIn.setText(formattedPunchIn);

            TextView empPunchOut = view.findViewById(R.id.empPunchOut);
            String punchOut = item.getPunchOut();
            String formattedPunchOut = DateTimeUtils.formatTime(punchOut);
            empPunchOut.setText(formattedPunchOut);

//            TextView empTotalTime = view.findViewById(R.id.empTotalTime);
//            String totalTime = DateTimeUtils.calculateTotalTime(formattedPunchIn, formattedPunchOut);
//            empTotalTime.setText(totalTime);
//
//            String shiftStartTime = item.getShift().getStartTime();
//            TextView empLateTime = view.findViewById(R.id.empLateTime);
//            String lateTime = LateTime(formattedPunchIn, shiftStartTime);
//            empLateTime.setText(lateTime);


            TextView empPunchInAddress = view.findViewById(R.id.empPunchInAddress);
            empPunchInAddress.setText(item.getPunchInAddress());

            TextView empPunchOutAddress = view.findViewById(R.id.empPunchOutAddress);
            empPunchOutAddress.setText(item.getPunchOutAddress());

            TextView appliedPunchIn = view.findViewById(R.id.appliedPunchIn);
            String punchInUpdated = item.getPunchInUpdated();
            String formattedPunchInUpdated = DateTimeUtils.formatTime(punchInUpdated);
            appliedPunchIn.setText(formattedPunchInUpdated);

            TextView appliedPunchOut = view.findViewById(R.id.appliedPunchOut);
            String punchOutUpdated = item.getPunchOutUpdated();
            String formattedPunchOutUpdated = DateTimeUtils.formatTime(punchOutUpdated);
            appliedPunchOut.setText(formattedPunchOutUpdated);

            TextView appliedPunchInAddress = view.findViewById(R.id.appliedPunchInAddress);
            appliedPunchInAddress.setText(item.getPunchInAddressUpdated());

            TextView appliedPunchOutAddress = view.findViewById(R.id.appliedPunchOutAddress);
            appliedPunchOutAddress.setText(item.getPunchOutAddressUpdated());

            TextView appliedDate = view.findViewById(R.id.appliedDate);
            String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
            appliedDate.setText(formattedAppliedDate);

            Chip appliedStatusChip = view.findViewById(R.id.appliedStatus);
            appliedStatusChip.setText(item.getStatus());

            String status = item.getStatus();
            int chipColor;

            if (status.equalsIgnoreCase("Approved")) {
                chipColor = R.color.status_approved;
            } else if (status.equalsIgnoreCase("UnApproved")) {
                chipColor = R.color.status_pending;
            } else if (status.equalsIgnoreCase("Rejected")) {
                chipColor = R.color.status_rejected;
            } else if (status.equalsIgnoreCase("Cancelled")) {
                chipColor = R.color.status_rejected;
            } else {
                chipColor = R.color.status_pending;
            }

            appliedStatusChip.setChipBackgroundColorResource(chipColor);
            appliedStatusChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            TextView appliedStatusUpdateBy = view.findViewById(R.id.appliedStatusUpdateBy);
            appliedStatusUpdateBy.setText(item.getApprovedByName());

            TextView appliedStatusUpdateDate = view.findViewById(R.id.appliedStatusUpdateDate);
            String formattedUpdatedDate = DateTimeUtils.getDayOfWeekAndDate(item.getApprovedDate());
            appliedStatusUpdateDate.setText(formattedUpdatedDate);

        }
        return view;
    }
}
//    public static String LateTime(String punchInTime, String shiftStartTime) {
//        if (punchInTime == null || shiftStartTime == null || punchInTime.equals("N/A") || shiftStartTime.equals("N/A")) {
//            return "";
//        }
//
//        SimpleDateFormat shiftFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
//        SimpleDateFormat time12Format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//
//        try {
//            Date shiftStart = shiftFormat.parse(shiftStartTime);
//            Date punchIn = time12Format.parse(punchInTime);
//
//            if (punchIn != null && punchIn.after(shiftStart)) {
//                if (shiftStart != null) {
//                    long lateInMillis = punchIn.getTime() - shiftStart.getTime();
//                    long lateHours = lateInMillis / (60 * 60 * 1000);
//                    long lateMinutes = (lateInMillis % (60 * 60 * 1000)) / (60 * 1000);
//                    return String.format(Locale.getDefault(), "%02d:%02d Hrs", lateHours, lateMinutes);
//                }
//            } else {
//                return "0";
//            }
//        } catch (ParseException e) {
//            Log.e("DateTimeUtils", "Error calculating late time: " + e.getMessage());
//        }
//        return "N/A";
//    }
