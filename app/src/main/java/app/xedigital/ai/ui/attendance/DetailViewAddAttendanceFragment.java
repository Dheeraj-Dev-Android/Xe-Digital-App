package app.xedigital.ai.ui.attendance;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.xedigital.ai.R;
import app.xedigital.ai.model.addedAttendanceList.AddAttendanceRegularizeAppliedItem;
import app.xedigital.ai.ui.regularize_attendance.RegularizeViewFragment;
import app.xedigital.ai.utills.DateTimeUtils;


public class DetailViewAddAttendanceFragment extends Fragment {

    public DetailViewAddAttendanceFragment() {
        // Required empty public constructor
    }

    public static DetailViewAddAttendanceFragment newInstance(String param1, String param2) {
        DetailViewAddAttendanceFragment fragment = new DetailViewAddAttendanceFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail_view_add_attendance, container, false);

        if (getArguments() != null) {
            AddAttendanceRegularizeAppliedItem attendanceItem = (AddAttendanceRegularizeAppliedItem) getArguments().getSerializable(RegularizeViewFragment.ARG_REGULARIZE_APPLIED_ITEM);
//        AddAttendanceRegularizeAppliedItem attendanceItem = (AddAttendanceRegularizeAppliedItem) getArguments().getSerializable(RegularizeViewFragment.ARG_REGULARIZE_APPLIED_ITEM);

            if (attendanceItem != null) {
                // Populate UI elements with attendance item details
                TextView empNameTextView = view.findViewById(R.id.empName);
                empNameTextView.setText(attendanceItem.getEmployee().getFirstname() + " " + attendanceItem.getEmployee().getLastname());
                TextView empPunchDateTextView = view.findViewById(R.id.empPunchDate);
                empPunchDateTextView.setText("Punch Date : " + DateTimeUtils.getDayOfWeekAndDate(attendanceItem.getPunchDate()));

                TextView appliedDateTextView = view.findViewById(R.id.appliedDate);
                appliedDateTextView.setText("Applied Date : " + DateTimeUtils.getDayOfWeekAndDate(attendanceItem.getAppliedDate()));

                TextView empEmailTextView = view.findViewById(R.id.empEmail);
                empEmailTextView.setText(attendanceItem.getEmployee().getEmail());

                TextView empContactTextView = view.findViewById(R.id.empContact);
                empContactTextView.setText(attendanceItem.getEmployee().getContact());
                TextView empShift = view.findViewById(R.id.empShift);
                empShift.setText(attendanceItem.getShift().getName() + " (" + attendanceItem.getShift().getStartTime() + " - " + attendanceItem.getShift().getEndTime() + ")");

                TextView empPunchIn = view.findViewById(R.id.empPunchIn);
                String punchIn = attendanceItem.getPunchIn();
                String formattedPunchIn = DateTimeUtils.extractTime(punchIn);
                empPunchIn.setText(formattedPunchIn);

                TextView empPunchOut = view.findViewById(R.id.empPunchOut);
                String punchOut = attendanceItem.getPunchOut();
                String formattedPunchOut = DateTimeUtils.extractTime(punchOut);
                empPunchOut.setText(formattedPunchOut);

                TextView empPunchInAddress = view.findViewById(R.id.empPunchInAddress);
                empPunchInAddress.setText(attendanceItem.getPunchInAddress());

                TextView empPunchOutAddress = view.findViewById(R.id.empPunchOutAddress);
                empPunchOutAddress.setText(attendanceItem.getPunchOutAddress());

                // Punch Details Card
                TextView appliedStatusChip = view.findViewById(R.id.appliedStatus);
                appliedStatusChip.setText(attendanceItem.getStatus());

                String status = attendanceItem.getStatus();

                if (status.equalsIgnoreCase("approved")) {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.approved_color));
                } else if (status.equalsIgnoreCase("unapproved")) {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.pending_status_color));
                } else if (status.equalsIgnoreCase("cancel")) {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.status_rejected));
                } else {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.status_pending));
                }

                TextView appliedStatusUpdateBy = view.findViewById(R.id.appliedStatusUpdateBy);
                appliedStatusUpdateBy.setText(attendanceItem.getApprovedByName());

                TextView appliedStatusUpdateDate = view.findViewById(R.id.appliedStatusUpdateDate);
                String formattedUpdatedDate = DateTimeUtils.getDayOfWeekAndDate(attendanceItem.getApprovedDate());
                appliedStatusUpdateDate.setText(formattedUpdatedDate);

            } else {
                Log.e("AttendanceItem", "Attendance item is null");
            }
        }
        return view;
    }
}