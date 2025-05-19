package app.xedigital.ai.ui.regularize_attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.CFregularizeAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.cfRegularizeApproval.AttendanceRegItem;
import app.xedigital.ai.model.regularizeUpdateStatus.RegularizeUpdateRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingCMFAttendanceApproveFragment extends Fragment {
    private TextView empNameTextView;
    private TextView appliedDateTextView;
    private TextView empPunchDateTextView;
    private TextView empEmailTextView;
    private TextView empContactTextView;
    private TextView empShiftTextView;
    private TextView empPunchInTextView;
    private TextView empPunchOutTextView;
    private TextView empPunchInAddressTextView;
    private TextView empPunchOutAddressTextView;
    private TextView appliedPunchInTextView;
    private TextView appliedPunchOutTextView;
    private TextView appliedPunchInAddressTextView;
    private TextView appliedPunchOutAddressTextView;
    private TextView appliedStatusUpdateByTextView;
    private TextView appliedStatusUpdatedDateTextView;
    private MaterialButton approveButton;
    private MaterialButton rejectButton;
    private View actionButtonsCard;
    private TextView appliedStatus;

    private ProfileViewModel profileViewModel;
    private APIInterface apiInterface;
    private String reportingManager;
    private AttendanceRegItem attendanceRegItem;


    public PendingCMFAttendanceApproveFragment() {
        // Required empty public constructor
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDateTime = dateTimeFormat.format(currentDateTime);
        return formattedDateTime;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        apiInterface = APIClient.getInstance().UpdateRegularizeListApproval();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        String authToken = sharedPreferences.getString("authToken", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_c_m_f_attendance_approve, container, false);
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        empNameTextView = view.findViewById(R.id.empName);
        appliedDateTextView = view.findViewById(R.id.appliedDate);
        empPunchDateTextView = view.findViewById(R.id.empPunchDate);
        empEmailTextView = view.findViewById(R.id.empEmail);
        empContactTextView = view.findViewById(R.id.empContact);
        empShiftTextView = view.findViewById(R.id.empShift);
        empPunchInTextView = view.findViewById(R.id.empPunchIn);
        empPunchOutTextView = view.findViewById(R.id.empPunchOut);
        empPunchInAddressTextView = view.findViewById(R.id.empPunchInAddress);
        empPunchOutAddressTextView = view.findViewById(R.id.empPunchOutAddress);
        appliedPunchInTextView = view.findViewById(R.id.appliedPunchIn);
        appliedPunchOutTextView = view.findViewById(R.id.appliedPunchOut);
        appliedPunchInAddressTextView = view.findViewById(R.id.appliedPunchInAddress);
        appliedPunchOutAddressTextView = view.findViewById(R.id.appliedPunchOutAddress);
        appliedStatusUpdateByTextView = view.findViewById(R.id.appliedStatusUpdateBy);
        appliedStatusUpdatedDateTextView = view.findViewById(R.id.appliedStatusUpdateDate);
        approveButton = view.findViewById(R.id.approve_button);
        rejectButton = view.findViewById(R.id.reject_button);
        actionButtonsCard = view.findViewById(R.id.actionButtonsCard);
        appliedStatus = view.findViewById(R.id.appliedStatus);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        getArguments();
        setupViewModelObservers();

        // Get the arguments
        Bundle args = getArguments();
        if (args != null) {
            AttendanceRegItem attendanceRegItem = (AttendanceRegItem) args.getSerializable(CFregularizeAdapter.ARG_ATTENDANCE_REG_ITEM);
            if (attendanceRegItem != null) {
                populateUI(attendanceRegItem);
            } else {
                Toast.makeText(getContext(), "Data is null", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Arguments are null", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViewModelObservers() {
        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null && userProfile.getData().getEmployee().getCrossmanager() != null) {
                String reportingManagerFirstName = userProfile.getData().getEmployee().getFirstname();
                String reportingManagerLastName = userProfile.getData().getEmployee().getLastname();
                if (reportingManagerFirstName != null && reportingManagerLastName != null) {
                    reportingManager = reportingManagerFirstName + " " + reportingManagerLastName;
                }
            }
        });
    }


    private void populateUI(AttendanceRegItem attendanceRegItem) {
        empNameTextView.setText(attendanceRegItem.getEmployee().getFullname());
        appliedDateTextView.setText("Applied Date: " + DateTimeUtils.getDayOfWeekAndDate(attendanceRegItem.getAppliedDate()));
        empPunchDateTextView.setText("Punch Date: " + DateTimeUtils.getDayOfWeekAndDate(attendanceRegItem.getPunchDate()));
        appliedStatus.setText(attendanceRegItem.getStatus());
        empEmailTextView.setText(attendanceRegItem.getEmployee().getEmail());
        empContactTextView.setText(attendanceRegItem.getEmployee().getContact());
        empShiftTextView.setText(attendanceRegItem.getShift().getStartTime() + " - " + attendanceRegItem.getShift().getEndTime());
        empPunchInTextView.setText(DateTimeUtils.formatTime(attendanceRegItem.getPunchIn()));
        empPunchOutTextView.setText(DateTimeUtils.formatTime(attendanceRegItem.getPunchOut()));
        empPunchInAddressTextView.setText(attendanceRegItem.getPunchInAddress());
        empPunchOutAddressTextView.setText(attendanceRegItem.getPunchOutAddress());
        appliedPunchInTextView.setText(DateTimeUtils.formatTime(attendanceRegItem.getPunchInUpdated()));
        appliedPunchOutTextView.setText(DateTimeUtils.formatTime(attendanceRegItem.getPunchOutUpdated()));
        appliedPunchInAddressTextView.setText(attendanceRegItem.getPunchInAddressUpdated());
        appliedPunchOutAddressTextView.setText(attendanceRegItem.getPunchOutAddressUpdated());
        appliedStatusUpdateByTextView.setText(attendanceRegItem.getApprovedByName());
        appliedStatusUpdatedDateTextView.setText(DateTimeUtils.getDayOfWeekAndDate(attendanceRegItem.getAppliedDate()));

        // Color code the status text based on the status value
        String status = attendanceRegItem.getStatus();
        if (status != null) {
            if (status.equalsIgnoreCase("unapproved")) {
                appliedStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.pending_status_color));
            } else if (status.equalsIgnoreCase("Approved")) {
                appliedStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_approved));
            } else if (status.equalsIgnoreCase("Rejected")) {
                appliedStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_rejected));
            } else {
                appliedStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            }

        }
        if (attendanceRegItem.getStatus().equals("unapproved")) {
            actionButtonsCard.setVisibility(View.VISIBLE);

        } else {
            actionButtonsCard.setVisibility(View.GONE);
        }

        approveButton.setOnClickListener(v -> {
//            Toast.makeText(getContext(), "Approve button clicked", Toast.LENGTH_SHORT).show();
            // Handle approve action
            String attendanceId = attendanceRegItem.getId();
            Log.d("PendingCMFAttendanceApproveFragment", "attendanceId: " + attendanceRegItem);

            handleApprove(attendanceId);
        });

        rejectButton.setOnClickListener(v -> {
//            Toast.makeText(getContext(), "Reject button clicked", Toast.LENGTH_SHORT).show();
            // Handle reject action
            String attendanceId = attendanceRegItem.getId();
            Log.d("PendingCMFAttendanceApproveFragment", "attendanceId: " + attendanceRegItem);

            handleReject(attendanceId);
        });
    }

    public void handleApprove(String attendanceId) {
        Toast.makeText(requireContext(), "Attendance Approved", Toast.LENGTH_SHORT).show();
        RegularizeUpdateRequest requestBody = new RegularizeUpdateRequest();
        requestBody.setStatus("Approved");
        requestBody.setApprovedByName(reportingManager);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Attendance Approved", Toast.LENGTH_SHORT).show();

                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingCMFAttendanceApproveFragment.this).attach(PendingCMFAttendanceApproveFragment.this).commit();

                } else {
                    Log.d("RegularizeApprovalAdapter", "onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.d("RegularizeApprovalAdapter", "onFailure: " + throwable.getMessage());
            }
        });
    }

    public void handleReject(String attendanceId) {
        Toast.makeText(requireContext(), "Attendance Rejected", Toast.LENGTH_SHORT).show();
        RegularizeUpdateRequest requestBody = new RegularizeUpdateRequest();
        requestBody.setStatus("Rejected");
        requestBody.setApprovedByName(reportingManager);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Attendance Rejected", Toast.LENGTH_SHORT).show();

                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingCMFAttendanceApproveFragment.this).attach(PendingCMFAttendanceApproveFragment.this).commit();

                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.d("RegularizeApprovalAdapter", "onFailure: " + throwable.getMessage());
            }
        });
    }
}

