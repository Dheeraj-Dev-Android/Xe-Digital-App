package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewTreeLifecycleOwner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.LeaveApprovalBinding;
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesApproveItem;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;


public class PendingLeaveApproveFragment extends Fragment {
    public static final String ARG_LEAVE_ID = "leave_id";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private APIInterface apiInterface;
    private AppliedLeavesApproveItem item;
    private String reportingManager;
    private String leaveId;
    private LeaveApprovalBinding binding;
    private ProfileViewModel profileViewModel;
    private OnLeaveApprovalActionListener listener;

    public PendingLeaveApproveFragment() {
        // Required empty public constructor
    }

    public static PendingLeaveApproveFragment newInstance(AppliedLeavesApproveItem item) {
        PendingLeaveApproveFragment fragment = new PendingLeaveApproveFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LEAVE_ID, item);
        fragment.setArguments(args);
        return fragment;
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDateTime = dateTimeFormat.format(currentDateTime);
        Log.d("LeaveApprovalAdapter", "getCurrentDateTimeInUTC: " + formattedDateTime);
        return formattedDateTime;

    }

    public void setListener(OnLeaveApprovalActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (AppliedLeavesApproveItem) getArguments().getSerializable(ARG_LEAVE_ID);
        }
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        apiInterface = APIClient.getInstance().UpdateLeaveListApproval();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        String authToken = sharedPreferences.getString("authToken", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LeaveApprovalBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.empCode.setText(item.getEmployeeCode());
        binding.empDesignation.setText(item.getDesignation());
        binding.empName.setText(item.getFirstname() + " " + item.getLastname());
        binding.empLeaveType.setText(item.getLeavetype().getLeavetypeName());
        String formattedFromDate = DateTimeUtils.getDayOfWeekAndDate(item.getFromDate());
        binding.empFromDate.setText(formattedFromDate + " " + item.getSelectTypeFrom());
        String formattedToDate = DateTimeUtils.getDayOfWeekAndDate(item.getToDate());
        binding.empToDate.setText(formattedToDate + " " + item.getSelectTypeTo());
        binding.empReason.setText(item.getReason());
        binding.empContactNumber.setText(item.getContactNumber());
        binding.empAddress.setText(item.getVacationAddress());
        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
        binding.empAppliedDate.setText(formattedAppliedDate);


        if (item.getStatus().equals("unapproved")) {
            binding.leaveBtnApprove.setVisibility(View.VISIBLE);
            binding.leaveBtnReject.setVisibility(View.VISIBLE);
            binding.leaveBtnCancel.setVisibility(View.VISIBLE);

            binding.leaveBtnApprove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(item);
                }
                String attendanceId = item.getId();
                handleApprove(attendanceId);
            });

            binding.leaveBtnReject.setOnClickListener(v -> {
                String attendanceId = item.getId();
                if (listener != null) {
                    listener.onReject(item);
                }
                handleReject(attendanceId);
            });
            binding.leaveBtnCancel.setOnClickListener(v -> {
                String attendanceId = item.getId();
                if (listener != null) {
                    listener.onReject(item);
                }
                handleCancel(attendanceId);
            });


        } else {
            binding.leaveBtnApprove.setVisibility(View.GONE);
            binding.leaveBtnReject.setVisibility(View.GONE);
            binding.leaveBtnCancel.setVisibility(View.GONE);
        }
        if (requireContext() instanceof FragmentActivity) {
            profileViewModel = new ViewModelProvider((FragmentActivity) requireContext()).get(ProfileViewModel.class);
        }

        if (ViewTreeLifecycleOwner.get(binding.getRoot()) != null) {
            profileViewModel.userProfile.observe(Objects.requireNonNull(ViewTreeLifecycleOwner.get(binding.getRoot())), userProfile -> {
                if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null && userProfile.getData().getEmployee().getReportingManager() != null) {
                    String reportingManagerFirstName = userProfile.getData().getEmployee().getReportingManager().getFirstname();
                    String reportingManagerLastName = userProfile.getData().getEmployee().getReportingManager().getLastname();
                    if (reportingManagerFirstName != null && reportingManagerLastName != null) {
                        reportingManager = reportingManagerFirstName + " " + reportingManagerLastName;
                        Log.e("RegularizeApprovalAdapter", "reportingManager: " + reportingManager);
                    }
                }
            });
        }
        return view;
    }

    void handleCancel(String leaveId) {

    }

    public void handleApprove(String leaveId) {
    }

    public void handleReject(String leaveId) {
    }


    public interface OnLeaveApprovalActionListener {
        void onApprove(AppliedLeavesApproveItem item);

        void onReject(AppliedLeavesApproveItem item);

        void onCancel(AppliedLeavesApproveItem item);
    }
}