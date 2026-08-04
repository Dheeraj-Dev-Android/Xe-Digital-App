package app.xedigital.ai.ui.attendance;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.addAttendanceRequest.AddedAttendanceCancelRequest;
import app.xedigital.ai.model.addedAttendanceList.AddAttendanceRegularizeAppliedItem;
import app.xedigital.ai.ui.regularize_attendance.RegularizeViewFragment;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class DetailViewAddAttendanceFragment extends Fragment {
    private AddAttendanceRegularizeAppliedItem attendanceItem;
    private MaterialCardView actionButtonsCard;

    public DetailViewAddAttendanceFragment() {
        // Required empty public constructor
    }

    public static DetailViewAddAttendanceFragment newInstance(String param1, String param2) {
        return new DetailViewAddAttendanceFragment();
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateTimeFormat.format(currentDateTime);
    }

    /**
     * Helper method to return "N/A" if the input string is null, empty, or contains only whitespace.
     */
    private String getSafeString(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value;
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

        actionButtonsCard = view.findViewById(R.id.actionButtonsCard);

        if (getArguments() != null) {
            attendanceItem = (AddAttendanceRegularizeAppliedItem) getArguments().getSerializable(RegularizeViewFragment.ARG_REGULARIZE_APPLIED_ITEM);

            if (attendanceItem != null) {
                // 1. Employee Name Check
                TextView empNameTextView = view.findViewById(R.id.empName);
                if (attendanceItem.getEmployee() != null) {
                    String firstName = getSafeString(attendanceItem.getEmployee().getFirstname());
                    String lastName = getSafeString(attendanceItem.getEmployee().getLastname());

                    if (firstName.equals("N/A") && lastName.equals("N/A")) {
                        empNameTextView.setText("N/A");
                    } else {
                        String fullName = (firstName.equals("N/A") ? "" : firstName) + " " +
                                (lastName.equals("N/A") ? "" : lastName);
                        empNameTextView.setText(fullName.trim());
                    }
                } else {
                    empNameTextView.setText("N/A");
                }

                // 2. Punch Date
                TextView empPunchDateTextView = view.findViewById(R.id.empPunchDate);
                String rawPunchDate = attendanceItem.getPunchDate();
                String punchDate = rawPunchDate != null ? DateTimeUtils.getDayOfWeekAndDate(rawPunchDate) : null;
                empPunchDateTextView.setText(getSafeString(punchDate));

                // 3. Applied Date
                TextView appliedDateTextView = view.findViewById(R.id.appliedDate);
                String rawAppliedDate = attendanceItem.getAppliedDate();
                String appliedDate = rawAppliedDate != null ? DateTimeUtils.getDayOfWeekAndDate(rawAppliedDate) : null;
                appliedDateTextView.setText(getSafeString(appliedDate));

                // 4. Employee Email
                TextView empEmailTextView = view.findViewById(R.id.empEmail);
                String email = attendanceItem.getEmployee() != null ? attendanceItem.getEmployee().getEmail() : null;
                empEmailTextView.setText(getSafeString(email));

                // 5. Employee Contact
                TextView empContactTextView = view.findViewById(R.id.empContact);
                String contact = attendanceItem.getEmployee() != null ? attendanceItem.getEmployee().getContact() : null;
                empContactTextView.setText(getSafeString(contact));

                // 6. Shift Details Check
                TextView empShift = view.findViewById(R.id.empShift);
                if (attendanceItem.getShift() != null) {
                    String shiftName = getSafeString(attendanceItem.getShift().getName());
                    String startTime = getSafeString(attendanceItem.getShift().getStartTime());
                    String endTime = getSafeString(attendanceItem.getShift().getEndTime());

                    if (shiftName.equals("N/A") && startTime.equals("N/A") && endTime.equals("N/A")) {
                        empShift.setText("N/A");
                    } else {
                        empShift.setText(shiftName + " (" + startTime + " - " + endTime + ")");
                    }
                } else {
                    empShift.setText("N/A");
                }

                // 7. Punch In
                TextView empPunchIn = view.findViewById(R.id.empPunchIn);
                String punchIn = attendanceItem.getPunchIn();
                String formattedPunchIn = punchIn != null ? DateTimeUtils.extractTime(punchIn) : null;
                empPunchIn.setText(getSafeString(formattedPunchIn));

                // 8. Punch Out
                TextView empPunchOut = view.findViewById(R.id.empPunchOut);
                String punchOut = attendanceItem.getPunchOut();
                String formattedPunchOut = punchOut != null ? DateTimeUtils.extractTime(punchOut) : null;
                empPunchOut.setText(getSafeString(formattedPunchOut));

                // 9. Punch In Address
                TextView empPunchInAddress = view.findViewById(R.id.empPunchInAddress);
                empPunchInAddress.setText(getSafeString(attendanceItem.getPunchInAddress()));

                // 10. Punch Out Address
                TextView empPunchOutAddress = view.findViewById(R.id.empPunchOutAddress);
                empPunchOutAddress.setText(getSafeString(attendanceItem.getPunchOutAddress()));

                // 11. Remarks
                TextView remarks = view.findViewById(R.id.remarksValue);
                remarks.setText(getSafeString(attendanceItem.getRemark()));

                // 12. Applied Status
                TextView appliedStatusChip = view.findViewById(R.id.appliedStatus);
                String status = getSafeString(attendanceItem.getStatus());
                appliedStatusChip.setText(status);

                if (status.equalsIgnoreCase("approved")) {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.approved_color));
                } else if (status.equalsIgnoreCase("unapproved")) {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.pending_status_color));
                } else if (status.equalsIgnoreCase("Cancelled")) {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.status_rejected));
                } else {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.status_pending));
                }

                if (status.equalsIgnoreCase("Cancelled")) {
                    if (actionButtonsCard != null) {
                        actionButtonsCard.setVisibility(View.GONE);
                    }
                }

                // 13. Status Updated By
                TextView appliedStatusUpdateBy = view.findViewById(R.id.appliedStatusUpdateBy);
                appliedStatusUpdateBy.setText(getSafeString(attendanceItem.getApprovedByName()));

                // 14. Status Update Date
                TextView appliedStatusUpdateDate = view.findViewById(R.id.appliedStatusUpdateDate);
                String rawApprovedDate = attendanceItem.getApprovedDate();
                String formattedUpdatedDate = rawApprovedDate != null ? DateTimeUtils.getDayOfWeekAndDate(rawApprovedDate) : null;
                appliedStatusUpdateDate.setText(getSafeString(formattedUpdatedDate));

            } else {
                Log.e("AttendanceItem", "Attendance item is null");
            }
        }

        Button cancelButton = view.findViewById(R.id.cancel_button);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> updateAttendanceStatus());
        }

        return view;
    }

    private void refreshFragment() {
        if (isAdded()) {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.detach(this);
            fragmentTransaction.attach(this);
            fragmentTransaction.commit();
        }
    }

    private void updateAttendanceStatus() {
        if (!isAdded()) return;

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");
        String attendanceId = attendanceItem != null ? attendanceItem.getId() : null;

        if (attendanceId == null) {
            return;
        }

        AddedAttendanceCancelRequest requestBody = new AddedAttendanceCancelRequest();
        requestBody.setStatus("Cancelled");

        // Safe check on Employee object during status update
        String approverName = "N/A";
        if (attendanceItem != null && attendanceItem.getEmployee() != null) {
            String firstName = getSafeString(attendanceItem.getEmployee().getFirstname());
            String lastName = getSafeString(attendanceItem.getEmployee().getLastname());
            approverName = (firstName.equals("N/A") ? "" : firstName) + " " + (lastName.equals("N/A") ? "" : lastName);
            approverName = approverName.trim().isEmpty() ? "N/A" : approverName.trim();
        }
        requestBody.setApprovedByName(approverName);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());

        APIInterface apiService = APIClient.getInstance().AddAttendance();
        Call<ResponseBody> call = apiService.AddedAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    refreshFragment();
                    Toast.makeText(requireContext(), "Attendance status updated", Toast.LENGTH_SHORT).show();
                    requireActivity().runOnUiThread(() -> {
                        if (actionButtonsCard != null) {
                            actionButtonsCard.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Log.e("UpdateAttendance", "Error updating status: " + response.message());
                    Toast.makeText(requireContext(), "Error updating status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (!isAdded()) return;

                Log.e("UpdateAttendance", "Error updating status: " + t.getMessage());
                Toast.makeText(requireContext(), "Error updating status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}