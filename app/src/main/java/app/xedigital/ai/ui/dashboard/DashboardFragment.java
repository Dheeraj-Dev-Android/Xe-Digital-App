package app.xedigital.ai.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.activity.PunchActivity;
import app.xedigital.ai.databinding.FragmentDashboardBinding;
import app.xedigital.ai.ui.attendance.AttendanceViewModel;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;

public class DashboardFragment extends Fragment {
    public String employeeName;
    public String employeeEmail;
    public String employeeLastName;
    public String empContact;
    public String empDesignation;
    public String punchIn;
    public String punchOut;
    private Handler handler;
    private Runnable runnable;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.punchButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);
            String authToken = sharedPreferences.getString("authToken", null);
            String name = sharedPreferences.getString("name", null);

            Intent intent = new Intent(requireContext(), PunchActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("authToken", authToken);
            intent.putExtra("name", name);

            Drawable[] drawables = binding.punchButton.getCompoundDrawablesRelative();
            AnimatedVectorDrawable animatedVector = (AnimatedVectorDrawable) drawables[2];
            if (animatedVector != null) {
                animatedVector.start();
            }
            startActivity(intent);
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        AttendanceViewModel attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        getContext();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        Calendar calendar = Calendar.getInstance();

// Get current date for endDate
        Date endDate = calendar.getTime();
        String endDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate);

// Calculate startDate (30 days before endDate)
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = calendar.getTime();
        String startDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate);
        attendanceViewModel.storeLoginData(authToken);
        attendanceViewModel.fetchEmployeeAttendance(startDateString, endDateString);

//        Date currentDate = new Date();
//        String todayDateString = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()).format(currentDate);
//        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//        String currentTimeString = timeFormat.format(currentDate);
//        binding.todayDate.setText(todayDateString + " - " + currentTimeString);


        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {

                Date currentDate = new Date();
                String todayDateString = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()).format(currentDate);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm ss a", Locale.getDefault());
                String currentTimeString = timeFormat.format(currentDate);
                binding.todayDate.setText(todayDateString + " - " + currentTimeString);

                // Schedule the Runnable to run again after 1 second
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(runnable);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            employeeName = userprofileResponse.getData().getEmployee().getFirstname();
            employeeEmail = userprofileResponse.getData().getEmployee().getEmail();
            employeeLastName = userprofileResponse.getData().getEmployee().getLastname();
            empContact = userprofileResponse.getData().getEmployee().getContact();
            empDesignation = userprofileResponse.getData().getEmployee().getDesignation();

            Object profileImageUrl = userprofileResponse.getData().getEmployee().getProfileImageUrl();
            ImageView profileImage = binding.ivEmployeeProfile;

            if (profileImageUrl != null) {
                Glide.with(requireContext()).load(profileImageUrl).circleCrop().into(profileImage).onLoadFailed(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_default_profile, null));
            } else {
//                Toast.makeText(requireContext(), "Profile image not found", Toast.LENGTH_SHORT).show();
                profileImage.setImageResource(R.mipmap.ic_default_profile);
            }

            binding.tvEmployeeNameValue.setText(employeeName + " " + employeeLastName);
            binding.tvEmployeeDesignationValue.setText(empDesignation);

            String startTime = userprofileResponse.getData().getEmployee().getShift().getStartTime();
            String endTime = userprofileResponse.getData().getEmployee().getShift().getEndTime();

            if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                String shiftTimeString = startTime + " - " + endTime;
                binding.tvEmployeeShiftValue.setText(formatShiftTime(shiftTimeString));
                Log.d("ShiftTime", "Shift Time: " + shiftTimeString);
            } else {
                binding.tvEmployeeShift.setText("Shift time not available");
            }

            binding.tvEmployeeContactValue.setText(empContact);
            binding.tvEmployeeEmailValue.setText(employeeEmail);

        });
        attendanceViewModel.attendance.observe(getViewLifecycleOwner(), attendanceResponse -> {
            if (attendanceResponse.getData() != null && attendanceResponse.getData().getEmployeePunchData() != null && !attendanceResponse.getData().getEmployeePunchData().isEmpty()) {

                punchIn = DateTimeUtils.formatTime(attendanceResponse.getData().getEmployeePunchData().get(0).getPunchIn());
                punchOut = DateTimeUtils.formatTime(attendanceResponse.getData().getEmployeePunchData().get(0).getPunchOut());
                binding.tvPunchInTime.setText("Punch In: " + punchIn);
                binding.tvPunchOutTime.setText("Punch Out: " + punchOut);
            } else {
                binding.tvPunchInTime.setText("Punch In: --:--");
                binding.tvPunchOutTime.setText("Punch Out: --:--");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
//        binding = null;
    }

    private String formatShiftTime(String shiftTime) {
        if (shiftTime == null || shiftTime.isEmpty()) {
            return "";
        }

        String[] parts = shiftTime.split("-");
        if (parts.length == 2) {
            try {
                SimpleDateFormat inputFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat outputFormatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                Date startTime = inputFormatter.parse(parts[0].trim());
                Date endTime = inputFormatter.parse(parts[1].trim());

                return outputFormatter.format(startTime) + " - " + outputFormatter.format(endTime);

            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }
}
