package app.xedigital.ai.ui.regularize_attendance;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.AttendanceApprovalBinding;
import app.xedigital.ai.model.regularizeList.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.model.regularizeUpdateStatus.RegularizeUpdateRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingApprovalViewFragment extends Fragment {

    public static final String ARG_ATTENDANCE_ID = "attendance_id";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private AttendanceRegularizeAppliedItem item;
    private APIInterface apiInterface;
    private ProfileViewModel profileViewModel;
    private String reportingManager;
    private AttendanceApprovalBinding binding;
    private static final String TAG = "PendingApprovalViewFrag";
    private String approverName;
    private OnRegularizeApprovalActionListener listener;
    private SecurePrefManager prefManager;

    public PendingApprovalViewFragment() {

    }

    public static PendingApprovalViewFragment newInstance(AttendanceRegularizeAppliedItem item) {
        PendingApprovalViewFragment fragment = new PendingApprovalViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ATTENDANCE_ID, item);
        fragment.setArguments(args);
        return fragment;
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDateTime = dateTimeFormat.format(currentDateTime);
        return formattedDateTime;

    }

    public void setListener(OnRegularizeApprovalActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (AttendanceRegularizeAppliedItem) getArguments().getSerializable(ARG_ATTENDANCE_ID);
        }
        prefManager = SecurePrefManager.getInstance(requireContext());

        if (getActivity() != null) {
            profileViewModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);
            Log.d(TAG, "onCreate: ViewModel provided with Activity context scope");
        } else {
            profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
            Log.d(TAG, "onCreate: ViewModel provided with Fragment context scope");
        }

        apiInterface = APIClient.getInstance().UpdateRegularizeListApproval();
        String userId = prefManager.getString("userId", "");
        String authToken = prefManager.getString("authToken", "");

        Log.d(TAG, "onCreate: Fetching user profile for userId: " + userId);
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = AttendanceApprovalBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.empName.setText(item.getEmployee().getFullname());
        binding.empPunchDate.setText(item.getPunchDate());

        binding.empName.setText(item.getEmployee().getFullname());
        binding.empEmail.setText(item.getEmployee().getEmail() + ",  " + item.getEmployee().getContact());

        String formattedPunchDate = DateTimeUtils.getDayOfWeekAndDate(item.getPunchDate());
        binding.empPunchDate.setText("Punch Date : " + formattedPunchDate);

        binding.empShift.setText(item.getShift().getName() + " (" + item.getShift().getStartTime() + " - " + item.getShift().getEndTime() + ")");

        String formattedPunchIn = DateTimeUtils.formatTime(item.getPunchIn());
        binding.empPunchIn.setText(formattedPunchIn);

        String formattedPunchOut = DateTimeUtils.formatTime(item.getPunchOut());
        binding.empPunchOut.setText(formattedPunchOut);

        binding.empPunchInAddress.setText(item.getPunchInAddress());
        binding.empPunchOutAddress.setText(item.getPunchOutAddress());

        String formattedAppliedPunchIn = DateTimeUtils.formatTime(item.getPunchInUpdated());
        binding.appliedPunchIn.setText(formattedAppliedPunchIn);

        String formattedAppliedPunchOut = DateTimeUtils.formatTime(item.getPunchOutUpdated());
        binding.appliedPunchOut.setText(formattedAppliedPunchOut);

        binding.appliedPunchInAddress.setText(item.getPunchInAddressUpdated());
        binding.appliedPunchOutAddress.setText(item.getPunchOutAddressUpdated());

        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
        binding.appliedDate.setText("Applied Date : " + formattedAppliedDate);

        binding.appliedStatusUpdateBy.setText(item.getApprovedByName());

        String formattedUpdatedDate = DateTimeUtils.getDayOfWeekAndDate(item.getApprovedDate());
        binding.appliedStatusUpdateDate.setText(formattedUpdatedDate);

        binding.appliedStatus.setText(item.getStatus());
        String status = item.getStatus();
        binding.appliedStatus.setText(status);


        if (status.equalsIgnoreCase("unapproved")) {
            binding.appliedStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.pending_status_color));
        } else if (status.equalsIgnoreCase("Approved")) {
            binding.appliedStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.status_approved));
        } else if (status.equalsIgnoreCase("Rejected")) {
            binding.appliedStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.status_rejected));
        } else {
            binding.appliedStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), android.R.color.black));
        }

        if (item.getStatus().equals("unapproved")) {
            binding.actionButtonsCard.setVisibility(View.VISIBLE);

            binding.approveButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(item);
                }
                String attendanceId = item.getId();
                handleApprove(attendanceId);
            });

            binding.rejectButton.setOnClickListener(v -> {
                String attendanceId = item.getId();
                if (listener != null) {
                    listener.onReject(item);
                }
                handleReject(attendanceId);
            });
        } else {
            binding.actionButtonsCard.setVisibility(View.GONE);
            binding.actionButtonsCard.setVisibility(View.GONE);
        }


        if (requireContext() instanceof FragmentActivity) {
            profileViewModel = new ViewModelProvider((FragmentActivity) requireContext()).get(ProfileViewModel.class);
        }

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            Log.d(TAG, "userProfile Observer: Received update trigger");
            if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null) {
                String firstName = userProfile.getData().getEmployee().getFirstname();
                String lastName = userProfile.getData().getEmployee().getLastname();
                Log.d(TAG, "userProfile Observer: Parsed name values -> firstname: " + firstName + ", lastname: " + lastName);

                if (firstName != null && lastName != null) {
                    approverName = firstName + " " + lastName;
                    Log.d(TAG, "userProfile Observer: Set approverName to: " + approverName);
                }
            } else {
                Log.w(TAG, "userProfile Observer: profile structural hierarchy returns null elements");
            }
        });

        return view;
    }

    private String resolveApproverIdentity() {
        if (approverName != null && !approverName.trim().isEmpty()) {
            Log.d(TAG, "resolveApproverIdentity: Using runtime approverName variable -> " + approverName);
            return approverName;
        } else if (item != null && item.getApprovedByName() != null && !item.getApprovedByName().trim().isEmpty()) {
            Log.d(TAG, "resolveApproverIdentity: Using metadata value -> " + item.getApprovedByName());
            return item.getApprovedByName();
        } else {
            String savedName = prefManager.getString("username", "");
            Log.d(TAG, "resolveApproverIdentity: Using shared preferences fallback -> " + savedName);
            return savedName;
        }
    }

    public void handleApprove(String attendanceId) {
        String finalApprover = resolveApproverIdentity();

        // Strict Pre-flight structural validate check
        if (finalApprover == null || finalApprover.trim().isEmpty()) {
            Log.e(TAG, "CRITICAL ERROR: Calculated approver identity string is blank! Aborting Approve API hit.");
            Toast.makeText(requireContext(), "Error: Approver Identity missing. Request aborted.", Toast.LENGTH_LONG).show();
            return;
        }
        RegularizeUpdateRequest requestBody = new RegularizeUpdateRequest();
        requestBody.setPunchInUpdated(item.getPunchInUpdated());
        requestBody.setPunchOutUpdated(item.getPunchOutUpdated());
        requestBody.setPunchInAddressUpdated(item.getPunchInAddressUpdated());
        requestBody.setPunchOutAddressUpdated(item.getPunchOutAddressUpdated());
        requestBody.setStatus("Approved");
        requestBody.setApprovedByName(finalApprover);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefManager.getString("authToken", "");
        Log.i(TAG, "handleApprove: Dispatching PUT request. Payload json -> " + gson.toJson(requestBody));

        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Attendance Approved", Toast.LENGTH_SHORT).show();

                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingApprovalViewFragment.this).attach(PendingApprovalViewFragment.this).commit();
                    if (listener != null) {
                        listener.onApprove(item);
                    }
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

        RegularizeUpdateRequest requestBody = new RegularizeUpdateRequest();
        requestBody.setStatus("Rejected");
        requestBody.setApprovedByName(reportingManager);
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefManager.getString("authToken", "");

        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Attendance Rejected", Toast.LENGTH_SHORT).show();

                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    ft.detach(PendingApprovalViewFragment.this).attach(PendingApprovalViewFragment.this).commit();
                    if (listener != null) {
                        listener.onApprove(item);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.d("RegularizeApprovalAdapter", "onFailure: " + throwable.getMessage());
            }
        });
    }

    public interface OnRegularizeApprovalActionListener {
        void onApprove(AttendanceRegularizeAppliedItem item);

        void onReject(AttendanceRegularizeAppliedItem item);
    }
}