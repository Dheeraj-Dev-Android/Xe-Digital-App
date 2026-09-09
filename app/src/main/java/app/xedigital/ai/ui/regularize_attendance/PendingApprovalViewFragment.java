package app.xedigital.ai.ui.regularize_attendance;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import app.xedigital.ai.model.regularizeUpdateStatus.Attendance;
import app.xedigital.ai.model.regularizeUpdateStatus.Employee;
import app.xedigital.ai.model.regularizeUpdateStatus.RegularizeUpdateRequest;
import app.xedigital.ai.model.regularizeUpdateStatus.Shift;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingApprovalViewFragment extends Fragment {

    public static final String ARG_ATTENDANCE_ID = "attendance_id";
    private static final String TAG = "PendingApprovalViewFrag";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private AttendanceRegularizeAppliedItem item;
    private APIInterface apiInterface;
    private ProfileViewModel profileViewModel;
    private AttendanceApprovalBinding binding;
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
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateTimeFormat.format(new Date());
    }

    public void setListener(OnRegularizeApprovalActionListener listener) {
        this.listener = listener;
    }

    // ─────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (AttendanceRegularizeAppliedItem) getArguments().getSerializable(ARG_ATTENDANCE_ID);
        }

        // Init SecurePrefManager
        prefManager = SecurePrefManager.getInstance(requireContext());
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        // Init API
        apiInterface = APIClient.getInstance().UpdateRegularizeListApproval();
        // Fetch profile to resolve approver name
        String userId = prefManager.getString("userId", "");
        String authToken = prefManager.getString("authToken", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = AttendanceApprovalBinding.inflate(inflater, container, false);

        if (item != null) {
            populateUI();
        } else {
            Log.e(TAG, "onCreateView: item is null, nothing to display");
        }

        // Observe profile to resolve approver name
        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null) {

                String firstName = userProfile.getData().getEmployee().getFirstname();
                String lastName = userProfile.getData().getEmployee().getLastname();

                if (firstName != null && lastName != null) {
                    approverName = firstName.trim() + " " + lastName.trim();
                }
            } else {
                Log.w(TAG, "userProfile Observer: null or incomplete profile data");
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void populateUI() {

        // Employee details
        if (item.getEmployee() != null) {
            binding.empName.setText(safeText(item.getEmployee().getFullname()));
            binding.empEmail.setText(safeText(item.getEmployee().getEmail()));
            binding.empContact.setText(safeText(item.getEmployee().getContact()));
        } else {
            binding.empName.setText("N/A");
            binding.empEmail.setText("N/A");
            binding.empContact.setText("N/A");
        }

        // Punch Date
        binding.empPunchDate.setText(formatOrFallbackDate(item.getPunchDate()));

        // Shift
        if (item.getShift() != null) {
            String shiftName = safeText(item.getShift().getName());
            String startTime = safeText(item.getShift().getStartTime());
            String endTime = safeText(item.getShift().getEndTime());

            if (shiftName.equals("N/A") && startTime.equals("N/A") && endTime.equals("N/A")) {
                binding.empShift.setText("N/A");
            } else {
                binding.empShift.setText(String.format(Locale.US, "%s (%s - %s)", shiftName, startTime, endTime));
            }
        } else {
            binding.empShift.setText("N/A");
        }

        // Original Punch In/Out
        binding.empPunchIn.setText(formatOrFallbackTime(item.getPunchIn()));
        binding.empPunchOut.setText(formatOrFallbackTime(item.getPunchOut()));
        binding.empPunchInAddress.setText(safeText(item.getPunchInAddress()));
        binding.empPunchOutAddress.setText(safeText(item.getPunchOutAddress()));

        // Applied (Updated) Punch In/Out
        binding.appliedPunchIn.setText(formatOrFallbackTime(item.getPunchInUpdated()));
        binding.appliedPunchOut.setText(formatOrFallbackTime(item.getPunchOutUpdated()));
        binding.appliedPunchInAddress.setText(safeText(item.getPunchInAddressUpdated()));
        binding.appliedPunchOutAddress.setText(safeText(item.getPunchOutAddressUpdated()));
        binding.appliedAttendanceRemarks.setText(safeText(item.getAttendenceRegularizationRemark()));

        // Applied Date
        binding.appliedDate.setText(formatOrFallbackDate(item.getAppliedDate()));

        // Status Update Metadata
        binding.appliedStatusUpdateBy.setText(safeText(item.getApprovedByName()));
        binding.appliedStatusUpdateDate.setText(safeText(DateTimeUtils.getDayOfWeekAndDate(item.getApprovedDate())));

        // Status Chip
        String status = item.getStatus();
        binding.appliedStatus.setText(safeText(status));
        applyStatusChipColor(status);

        // Action Buttons — only visible when status is unapproved
        if (status != null && status.equalsIgnoreCase("unapproved")) {
            binding.actionButtonsCard.setVisibility(View.VISIBLE);

            binding.approveButton.setOnClickListener(v -> {
                String attendanceId = item.getId();
                handleApprove(attendanceId);
            });

            binding.rejectButton.setOnClickListener(v -> {
                String attendanceId = item.getId();
                handleReject(attendanceId);
            });

        } else {
            binding.actionButtonsCard.setVisibility(View.GONE);
        }
    }

    private void applyStatusChipColor(String status) {
        if (status == null) {
            binding.appliedStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.black)));
            return;
        }

        int colorRes;
        if (status.equalsIgnoreCase("unapproved")) {
            colorRes = R.color.pending_status_color;
        } else if (status.equalsIgnoreCase("approved")) {
            colorRes = R.color.status_approved;
        } else if (status.equalsIgnoreCase("rejected")) {
            colorRes = R.color.status_rejected;
        } else {
            colorRes = android.R.color.black;
        }

        binding.appliedStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorRes)));
    }

    // ─────────────────────────────────────────────────────────────────
    // Build Request Payload
    // ─────────────────────────────────────────────────────────────────

    private RegularizeUpdateRequest buildRequestBody(String status, String approverName) {
        RegularizeUpdateRequest requestBody = new RegularizeUpdateRequest();

        // Top-level ID
        requestBody.setId(item.getId());

        // Punch updated fields
        requestBody.setPunchInUpdated(item.getPunchInUpdated());
        requestBody.setPunchOutUpdated(item.getPunchOutUpdated());
        requestBody.setPunchInAddressUpdated(item.getPunchInAddressUpdated());
        requestBody.setPunchOutAddressUpdated(item.getPunchOutAddressUpdated());

        // Remark
        requestBody.setAttendenceRegularizationRemark(item.getAttendenceRegularizationRemark());

        // Dates
        requestBody.setAppliedDate(item.getAppliedDate());
        requestBody.setPunchDate(item.getPunchDate());
        requestBody.setPunchIn(item.getPunchIn());
        requestBody.setPunchOut(item.getPunchOut());
        requestBody.setPunchInAddress(item.getPunchInAddress());
        requestBody.setPunchOutAddress(item.getPunchOutAddress());
        requestBody.setCreatedAt(item.getCreatedAt());

        // Date-only fields extracted from ISO datetime
        requestBody.setPunchOutUpdatedDate(extractDateOnly(item.getPunchOutUpdated()));
        requestBody.setPunchOutDate(extractDateOnly(item.getPunchOut()));

        // Map nested Attendance object
        if (item.getAttendance() != null) {
            Attendance att = new Attendance();
            att.setId(item.getAttendance().getId());
            att.setEmployee(item.getAttendance().getEmployee());
            att.setPunchDate(item.getAttendance().getPunchDate());
            att.setV(item.getAttendance().getV());
            att.setCreatedAt(item.getAttendance().getCreatedAt());
            att.setPunchIn(item.getAttendance().getPunchIn());
            att.setPunchInAddress(item.getAttendance().getPunchInAddress());
            att.setPunchOut(item.getAttendance().getPunchOut());
            att.setPunchOutAddress(item.getAttendance().getPunchOutAddress());
            att.setUpdatedAt(item.getAttendance().getUpdatedAt());
            requestBody.setAttendance(att);
        }

        // Map nested Employee object
        if (item.getEmployee() != null) {
            Employee emp = new Employee();
            emp.setId(item.getEmployee().getId());
            emp.setEmployeeCode(item.getEmployee().getEmployeeCode());
            emp.setDateOfBirth(item.getEmployee().getDateOfBirth());
            emp.setJoiningDate(item.getEmployee().getJoiningDate());
            emp.setJoiningType(item.getEmployee().getJoiningType());
            emp.setReportingManager(item.getEmployee().getReportingManager());
            emp.setProfileImageUrl(item.getEmployee().getProfileImageUrl());
            emp.setDesignation(item.getEmployee().getDesignation());
            emp.setActive(item.getEmployee().isActive());
            emp.setIsVerified(item.getEmployee().isIsVerified());
            emp.setIsHROrAdmin(item.getEmployee().isIsHROrAdmin());
            emp.setEmployeeType(item.getEmployee().getEmployeeType());
            emp.setFirstname(item.getEmployee().getFirstname());
            emp.setLastname(item.getEmployee().getLastname());
            emp.setEmail(item.getEmployee().getEmail());
            emp.setContact(item.getEmployee().getContact());
            emp.setCompany(item.getEmployee().getCompany());
            emp.setDepartment(item.getEmployee().getDepartment());
            emp.setPartner(item.getEmployee().getPartner());
            emp.setShift(item.getEmployee().getShift());
            emp.setCreatedAt(item.getEmployee().getCreatedAt());
            emp.setUpdatedAt(item.getEmployee().getUpdatedAt());
            emp.setV(item.getEmployee().getV());
            emp.setGrade(item.getEmployee().getGrade());
            emp.setLevel(item.getEmployee().getLevel());
            emp.setAddpayroll(item.getEmployee().isAddpayroll());
            emp.setAddress(item.getEmployee().getAddress());
            emp.setAdharNo(item.getEmployee().getAdharNo());
            emp.setCrossmanager(item.getEmployee().getCrossmanager());
            emp.setCtc(item.getEmployee().getCtc());
            emp.setDifferentlyAbled(item.getEmployee().getDifferentlyAbled());
            emp.setEpf(item.getEmployee().isEpf());
            emp.setEsi(item.getEmployee().isEsi());
            emp.setFatherName(item.getEmployee().getFatherName());
            emp.setPanNo(item.getEmployee().getPanNo());
            emp.setPfAccountNo(item.getEmployee().getPfAccountNo());
            emp.setPincode(item.getEmployee().getPincode());
            emp.setState(item.getEmployee().getState());
            emp.setTotalMonthlySalary(item.getEmployee().getTotalMonthlySalary());
            emp.setTotalYearlySalary(item.getEmployee().getTotalYearlySalary());
            emp.setUanno(item.getEmployee().getUanno());
            emp.setBu(item.getEmployee().getBu());
            emp.setFullname(item.getEmployee().getFullname());
            requestBody.setEmployee(emp);
        }

        // Map nested Shift object
        if (item.getShift() != null) {
            Shift shft = new Shift();
            shft.setId(item.getShift().getId());
            shft.setStartTime(item.getShift().getStartTime());
            shft.setEndTime(item.getShift().getEndTime());
            shft.setFormat(item.getShift().getFormat());
            shft.setComment(item.getShift().getComment());
            shft.setActive(item.getShift().isActive());
            shft.setName(item.getShift().getName());
            shft.setShiftType(item.getShift().getShiftType());
            shft.setFromHour(item.getShift().getFromHour());
            shft.setFromMinutes(item.getShift().getFromMinutes());
            shft.setToHour(item.getShift().getToHour());
            shft.setToMinutes(item.getShift().getToMinutes());
            shft.setTimeWaiver(item.getShift().getTimeWaiver());
            shft.setCompany(item.getShift().getCompany());
            shft.setCreatedBy(item.getShift().getCreatedBy());
            shft.setCreatedAt(item.getShift().getCreatedAt());
            shft.setUpdatedAt(item.getShift().getUpdatedAt());
            shft.setV(item.getShift().getV());
            requestBody.setShift(shft);
        }

        // ✅ Only these two fields differ between approve and reject
        requestBody.setStatus(status);
        requestBody.setApprovedByName(approverName);
        requestBody.setApprovedDate(getCurrentDateTimeInUTC());

        return requestBody;
    }

    // ─────────────────────────────────────────────────────────────────
    // Approve
    // ─────────────────────────────────────────────────────────────────

    public void handleApprove(String attendanceId) {
        String finalApprover = resolveApproverIdentity();

        if (finalApprover == null || finalApprover.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Error: Approver Identity missing. Request aborted.", Toast.LENGTH_LONG).show();
            return;
        }

        RegularizeUpdateRequest requestBody = buildRequestBody("approved", finalApprover);
        String authToken = prefManager.getString("authToken", "");

        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (binding == null) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Attendance Approved", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onApprove(item);
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Failed to approve: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (binding == null) return;
                Toast.makeText(requireContext(), "Network error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────
    // Reject
    // ─────────────────────────────────────────────────────────────────

    public void handleReject(String attendanceId) {
        String finalApprover = resolveApproverIdentity();

        if (finalApprover == null || finalApprover.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Error: Approver Identity missing. Request aborted.", Toast.LENGTH_LONG).show();
            return;
        }

        RegularizeUpdateRequest requestBody = buildRequestBody("rejected", finalApprover);
        String authToken = prefManager.getString("authToken", "");
        Call<ResponseBody> call = apiInterface.RegularizeAttendanceStatus("jwt " + authToken, attendanceId, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (binding == null) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Attendance Rejected", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onReject(item);
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Failed to reject: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (binding == null) return;
                Toast.makeText(requireContext(), "Network error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String resolveApproverIdentity() {
        if (approverName != null && !approverName.trim().isEmpty()) {
            return approverName;
        } else if (item != null && item.getApprovedByName() != null && !item.getApprovedByName().trim().isEmpty()) {
            return item.getApprovedByName();
        } else {
            String savedName = prefManager.getString("username", "");
            return savedName;
        }
    }

    private String extractDateOnly(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.length() < 10) return null;
        return isoDateTime.substring(0, 10);
    }

    private String safeText(String input) {
        return (input != null && !input.trim().isEmpty()) ? input : "N/A";
    }

    private String formatOrFallbackDate(String rawDate) {
        if (rawDate != null && !rawDate.trim().isEmpty()) {
            String formatted = DateTimeUtils.getDayOfWeekAndDate(rawDate);
            return (formatted != null && !formatted.trim().isEmpty()) ? formatted : "N/A";
        }
        return "N/A";
    }

    private String formatOrFallbackTime(String rawTime) {
        if (rawTime != null && !rawTime.trim().isEmpty()) {
            String formatted = DateTimeUtils.formatTime(rawTime);
            return (formatted != null && !formatted.trim().isEmpty()) ? formatted : "N/A";
        }
        return "N/A";
    }

    public interface OnRegularizeApprovalActionListener {
        void onApprove(AttendanceRegularizeAppliedItem item);

        void onReject(AttendanceRegularizeAppliedItem item);
    }
}