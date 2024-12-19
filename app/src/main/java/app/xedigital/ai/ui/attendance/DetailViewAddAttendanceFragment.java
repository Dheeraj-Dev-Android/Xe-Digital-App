package app.xedigital.ai.ui.attendance;

import android.content.Context;
import android.content.SharedPreferences;
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
        DetailViewAddAttendanceFragment fragment = new DetailViewAddAttendanceFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDateTime = dateTimeFormat.format(currentDateTime);
        Log.d("RegularizeApprovalAdapter", "getCurrentDateTimeInUTC: " + formattedDateTime);
        return formattedDateTime;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail_view_add_attendance, container, false);

        actionButtonsCard = view.findViewById(R.id.actionButtonsCard);
        if (getArguments() != null) {
            attendanceItem = (AddAttendanceRegularizeAppliedItem) getArguments().getSerializable(RegularizeViewFragment.ARG_REGULARIZE_APPLIED_ITEM);
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

                TextView remarks = view.findViewById(R.id.remarksValue);
                remarks.setText(attendanceItem.getRemark());

                // Punch Details Card
                TextView appliedStatusChip = view.findViewById(R.id.appliedStatus);
                appliedStatusChip.setText(attendanceItem.getStatus());

                String status = attendanceItem.getStatus();

                if (status.equalsIgnoreCase("approved")) {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.approved_color));
                } else if (status.equalsIgnoreCase("unapproved")) {
                    appliedStatusChip.setTextColor(getResources().getColor(R.color.pending_status_color));
                } else if (status.equalsIgnoreCase("Cancelled")) {
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

        Button cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> updateAttendanceStatus());
        return view;
    }

    private void refreshFragment() {
        Log.w("RegularizeApprovalAdapter", "refreshFragment: ");
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(this);
        fragmentTransaction.attach(this);
        fragmentTransaction.commit();
    }

    private void updateAttendanceStatus() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String attendanceId = attendanceItem != null ? attendanceItem.getId() : null;
        if (attendanceId == null) {
            return;
        }

        AddedAttendanceCancelRequest requestBody = new AddedAttendanceCancelRequest();
        requestBody.setStatus("Cancelled");
        requestBody.setApprovedByName(attendanceItem.getEmployee().getFirstname() + " " + attendanceItem.getEmployee().getLastname());
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());

        APIInterface apiService = APIClient.getInstance().AddAttendance();
        Call<ResponseBody> call = apiService.AddedAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    refreshFragment();
                    Toast.makeText(requireContext(), "Attendance status updated", Toast.LENGTH_SHORT).show();
                    requireActivity().runOnUiThread(() -> actionButtonsCard.setVisibility(View.GONE));
                } else {
                    Log.e("UpdateAttendance", "Error updating status: " + response.message());
                    Toast.makeText(requireContext(), "Error updating status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                Log.e("UpdateAttendance", "Error updating status: " + t.getMessage());
                Toast.makeText(requireContext(), "Error updating status", Toast.LENGTH_SHORT).show();
            }
        });
    }


}