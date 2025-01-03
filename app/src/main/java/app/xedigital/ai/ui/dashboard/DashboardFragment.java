package app.xedigital.ai.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import app.xedigital.ai.R;
import app.xedigital.ai.activity.PunchActivity;
import app.xedigital.ai.databinding.FragmentDashboardBinding;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.model.leaves.LeavesItem;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.ui.attendance.AttendanceViewModel;
import app.xedigital.ai.ui.leaves.LeavesViewModel;
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
    public AttendanceViewModel attendanceViewModel;
    public LeavesViewModel leavesViewModel;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar loader;
    private PieChart leavePieChart;
    private FragmentDashboardBinding binding;

    public static void updatePieChartData(PieChart pieChart, List<LeavesItem> leaves) {
        Map<String, Float> leaveData = new HashMap<>();
        float totalBalanceLeaves = 0;
        float totalLeaves = 0;

        for (LeavesItem leave : leaves) {
            String leaveType = leave.getLeavetype();
            if (leaveType != null) {
                float creditedLeaves = leave.getCreditLeave();
                float usedLeaves = leave.getUsedLeave();
                float debitedLeaves = leave.getDebitLeave();

                float balanceLeaves = creditedLeaves - usedLeaves - debitedLeaves;

                leaveData.put(leaveType, balanceLeaves);
                totalBalanceLeaves += balanceLeaves;
                totalLeaves += creditedLeaves;
            }
        }

        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Float> entry : leaveData.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        }

        // Create a color map for leave types
        Map<String, Integer> leaveTypeColors = new HashMap<>();
        leaveTypeColors.put("Casual Leave", Color.BLUE);
        leaveTypeColors.put("Sick Leave", Color.RED);
        leaveTypeColors.put("Privilege Leave", Color.rgb(255, 165, 0));
        // Add more leave types and colors as needed

        // Create a list of colors for the pie chart slices
        List<Integer> colors = new ArrayList<>();
        for (PieEntry entry : entries) {
            String leaveType = entry.getLabel();
            int color = leaveTypeColors.getOrDefault(leaveType, Color.GRAY);
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Leave Types");
        dataSet.setColors(colors);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateXY(1000, 1000);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }

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
        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);
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
        leavesViewModel.setUserId(authToken);
        leavesViewModel.fetchLeavesData();

        loader = view.findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                Date currentDate = new Date();
                String todayDateString = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()).format(currentDate);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String currentTimeString = timeFormat.format(currentDate);
                binding.todayDate.setText(todayDateString + " - " + currentTimeString);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
//            loader.setVisibility(View.GONE);
            // Null check before accessing Employee object
            if (userprofileResponse != null && userprofileResponse.getData() != null && userprofileResponse.getData().getEmployee() != null) {
                Employee employee = userprofileResponse.getData().getEmployee();

                employeeName = employee.getFirstname();
                employeeEmail = employee.getEmail();
                employeeLastName = employee.getLastname();
                empContact = employee.getContact();
                empDesignation = employee.getDesignation();


                binding.tvEmployeeNameValue.setText(employeeName + " " + employeeLastName);
                binding.tvEmployeeDesignationValue.setText(empDesignation);

                // Handling shift time
                String startTime = employee.getShift().getStartTime();
                String endTime = employee.getShift().getEndTime();
                if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                    String shiftTimeString = startTime + " - " + endTime;
                    binding.tvEmployeeShiftValue.setText(formatShiftTime(shiftTimeString));
                    Log.d("ShiftTime", "Shift Time: " + shiftTimeString);
                } else {
                    binding.tvEmployeeShift.setText("N/A");
                }

                if (empContact != null && employeeEmail != null) {
                    binding.tvEmployeeContactValue.setText(empContact);
                    binding.tvEmployeeEmailValue.setText(employeeEmail);
                } else {
                    binding.tvEmployeeContactValue.setText("");
                    binding.tvEmployeeEmailValue.setText("");
                }

                // Load Profile image
                Object profileImageUrl = employee.getProfileImageUrl();
                ImageView profileImage = binding.ivEmployeeProfile;

                if (profileImageUrl != null) {
                    Glide.with(requireContext()).load(profileImageUrl).circleCrop().into(profileImage).onLoadFailed(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_default_profile, null));
                } else {
                    profileImage.setImageResource(R.mipmap.ic_default_profile);
                }

            } else {
                // Handle the case where employee data is null
                Log.e("DashboardFragment", "Employee data is null");
                binding.tvEmployeeNameValue.setText("N/A");
                binding.tvEmployeeDesignationValue.setText("N/A");
                binding.tvEmployeeShiftValue.setText("N/A");
                binding.tvEmployeeContactValue.setText("N/A");
                binding.tvEmployeeEmailValue.setText("N/A");
                binding.ivEmployeeProfile.setImageResource(R.mipmap.ic_default_profile);
            }
            // Check if other data is loaded as well
            if (attendanceViewModel.attendance.getValue() != null && leavesViewModel.leavesData.getValue() != null) {
                loader.setVisibility(View.GONE); // Hide the loader when all data is loaded
            }
        });
        attendanceViewModel.attendance.observe(getViewLifecycleOwner(), attendanceResponse -> {

            if (attendanceResponse.getData() != null && attendanceResponse.getData().getEmployeePunchData() != null) {
                new Thread(() -> {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String currentDate = dateFormat.format(new Date());

                    List<EmployeePunchDataItem> currentPunchData = attendanceResponse.getData().getEmployeePunchData().stream().filter(punchData -> {
                        try {
                            SimpleDateFormat punchDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date punchDate = punchDateFormat.parse(punchData.getPunchDateFormat());
                            String formattedPunchDate = dateFormat.format(punchDate);
                            return formattedPunchDate.equals(currentDate);
                        } catch (androidx.core.net.ParseException | ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }).collect(Collectors.toList());

                    // Update UI on the main thread
                    requireActivity().runOnUiThread(() -> {
                        if (!currentPunchData.isEmpty()) {
                            punchIn = DateTimeUtils.formatTime(currentPunchData.get(0).getPunchIn());
                            punchOut = DateTimeUtils.formatTime(currentPunchData.get(0).getPunchOut());
                            binding.tvPunchInTime.setText("Punch In: " + punchIn);
                            binding.tvPunchOutTime.setText("Punch Out: " + punchOut);
                        } else {
                            binding.tvPunchInTime.setText("Punch In: --:--");
                            binding.tvPunchOutTime.setText("Punch Out: --:--");
                        }
                    });
                }).start();
            } else {
                binding.tvPunchInTime.setText("Punch In: --:--");
                binding.tvPunchOutTime.setText("Punch Out: --:--");
            }
            // Check if other data is loaded as well
            if (profileViewModel.userProfile.getValue() != null && leavesViewModel.leavesData.getValue() != null) {
                loader.setVisibility(View.GONE); // Hide the loader when all data is loaded
            }
        });

//        To show leave Chart here
//        loader.setVisibility(View.VISIBLE);
        leavesViewModel.leavesData.observe(getViewLifecycleOwner(), leavesData -> {
//            loader.setVisibility(View.GONE);
            if (leavesData != null && leavesData.getData() != null) {
                List<LeavesItem> leaves = leavesData.getData().getLeaves();
                if (leaves.isEmpty()) {
//                    binding.leavePieChart.setVisibility(View.GONE);
                    Log.e("DashboardFragment", "No leaves data available");
                } else {
                    binding.leavePieChart.setVisibility(View.VISIBLE);
                    updatePieChartData(binding.leavePieChart, leaves);
                }
            } else {
//                binding.leavePieChart.setVisibility(View.GONE);
                Log.e("DashboardFragment", "No leaves data available");
            }
            // Check if other data is loaded as well
            if (profileViewModel.userProfile.getValue() != null && attendanceViewModel.attendance.getValue() != null) {
                loader.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
        binding = null;
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
                Log.e("DashboardFragment", "Error formatting shift time", e);
                return "";

            }
        }
        return "";
    }
}
