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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;

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
    public String employeeName, employeeLastName, employeeEmail, empContact, empDesignation;
    public String punchIn, punchOut;
    public AttendanceViewModel attendanceViewModel;
    public LeavesViewModel leavesViewModel;
    private Handler handler;
    private Runnable runnable;
    private ProgressBar loader;
    private View blurOverlay;
    private PieChart leavePieChart;
    private FragmentDashboardBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String startDateString, endDateString;
    private boolean isProfileDataLoaded = false;
    private boolean isAttendanceDataLoaded = false;
    private boolean isLeavesDataLoaded = false;
    private Button crashButton;

    // Shimmer layouts
    private ShimmerFrameLayout punchCardShimmer;
    private ShimmerFrameLayout employeeCardShimmer;
    private ShimmerFrameLayout leavePieChartShimmer;

    // Real layouts
    private MaterialCardView punchCardView;
    private MaterialCardView employeeCard;
    private MaterialCardView leavePieChartContainer;

    //Real layouts values
    private TextView todayDate;
    private Button punchButton;
    private TextView tvEmployeeNameValue;
    private TextView tvEmployeeDesignationValue;
    private TextView tvEmployeeShiftValue;
    private TextView tvEmployeeContactValue;
    private TextView tvEmployeeEmailValue;
    private ImageView ivEmployeeProfile;
    private TextView tvPunchInTime;
    private TextView tvPunchOutTime;
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
        PieDataSet dataSet = new PieDataSet(entries, "Leave Types");
        // If all entries have 0 balance, show a single "0 leaves" slice
        if (entries.stream().allMatch(e -> e.getValue() == 0f)) {
            entries.clear();
            entries.add(new PieEntry(1f, "0 leaves"));

            // Set the custom ValueFormatter to hide the value
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });
        } else {
            // Use default PercentFormatter for other cases
            dataSet.setValueFormatter(new PercentFormatter(pieChart));
        }

        Map<String, Integer> leaveTypeColors = new HashMap<>();
        leaveTypeColors.put("Casual Leave", Color.rgb(51, 206, 255));
        leaveTypeColors.put("Sick Leave", Color.rgb(125, 206, 160));
        leaveTypeColors.put("Privilege Leave", Color.rgb(255, 165, 0));
        leaveTypeColors.put("Restricted Holidays", Color.rgb(199, 0, 57));

        List<Integer> colors = new ArrayList<>();
        for (PieEntry entry : entries) {
            String leaveType = entry.getLabel();
            int color = leaveTypeColors.getOrDefault(leaveType, Color.GRAY);
            colors.add(color);
        }

//        PieDataSet dataSet = new PieDataSet(entries, "Leave Types");
        dataSet.setColors(colors);
//        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(8f);

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

    private void showLoaderWithBlur() {
        blurOverlay.setVisibility(View.VISIBLE);
        loader.setVisibility(View.VISIBLE);
    }

    private void hideLoaderWithBlur() {
        blurOverlay.setVisibility(View.GONE);
        loader.setVisibility(View.GONE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchData);

        initializeViews(root);

        // Creates a button that mimics a crash when pressed

//        binding.crashButton.setOnClickListener(v -> {
//            throw new RuntimeException("Test Crash"); // Force a crash
//        });

        binding.punchButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", "");
            String authToken = sharedPreferences.getString("authToken", "");
            String name = sharedPreferences.getString("name", "");

            Intent intent = new Intent(requireContext(), PunchActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("authToken", authToken);
            intent.putExtra("name", name);

            Drawable[] drawables = binding.punchButton.getCompoundDrawablesRelative();
            if (drawables != null && drawables.length > 2) {
                Drawable drawable = drawables[2];
                if (drawable instanceof AnimatedVectorDrawable) {
                    AnimatedVectorDrawable animatedVector = (AnimatedVectorDrawable) drawable;
                    animatedVector.start();
                }
            }
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize blur overlay and loader
        blurOverlay = view.findViewById(R.id.blurOverlay);
        loader = view.findViewById(R.id.loader);

        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        endDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = calendar.getTime();
        startDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate);
        attendanceViewModel.storeLoginData(authToken);
        attendanceViewModel.fetchEmployeeAttendance(startDateString, endDateString);
        leavesViewModel.setUserId(authToken);
        leavesViewModel.fetchLeavesData();

        showLoaderWithBlur();
        startShimmerAnimations();

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                Date currentDate = new Date();
                String todayDateString = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault()).format(currentDate);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                String currentTimeString = timeFormat.format(currentDate);
                binding.todayDate.setText(todayDateString + " - " + currentTimeString);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            if (userprofileResponse != null && userprofileResponse.getData() != null && userprofileResponse.getData().getEmployee() != null) {
                Employee employee = userprofileResponse.getData().getEmployee();

                employeeName = employee.getFirstname();
                employeeEmail = employee.getEmail();
                employeeLastName = employee.getLastname();
                empContact = employee.getContact();
                empDesignation = employee.getDesignation();
                binding.tvEmployeeNameValue.setText(employeeName + " " + employeeLastName);
                binding.tvEmployeeDesignationValue.setText(empDesignation);

                String startTime = employee.getShift().getStartTime();
                String endTime = employee.getShift().getEndTime();
                if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                    String shiftTimeString = startTime + " - " + endTime;
                    binding.tvEmployeeShiftValue.setText(formatShiftTime(shiftTimeString));
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

                Object profileImageUrl = employee.getProfileImageUrl();
                ImageView profileImage = binding.ivEmployeeProfile;

                if (profileImageUrl != null) {
                    Glide.with(requireContext()).load(profileImageUrl).into(profileImage);
                } else {
                    profileImage.setImageResource(R.mipmap.ic_default_profile);
                }

            } else {
                binding.tvEmployeeNameValue.setText("N/A");
                binding.tvEmployeeDesignationValue.setText("N/A");
                binding.tvEmployeeShiftValue.setText("N/A");
                binding.tvEmployeeContactValue.setText("N/A");
                binding.tvEmployeeEmailValue.setText("N/A");
                binding.ivEmployeeProfile.setImageResource(R.mipmap.ic_default_profile);
            }

            isProfileDataLoaded = true;
            checkAllDataLoaded();
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
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }).collect(Collectors.toList());

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
                punchCardView.setVisibility(View.VISIBLE);
            }
            punchCardShimmer.stopShimmer();
            punchCardShimmer.setVisibility(View.GONE);
            isAttendanceDataLoaded = true;
            checkAllDataLoaded();
        });

        leavesViewModel.leavesData.observe(getViewLifecycleOwner(), leavesData -> {
            if (leavesData != null && leavesData.getData() != null) {
                List<LeavesItem> leaves = leavesData.getData().getLeaves();
                if (leaves.isEmpty()) {
                    Log.e("DashboardFragment", "No leaves data available");
                } else {
                    binding.leavePieChart.setVisibility(View.VISIBLE);
                    updatePieChartData(binding.leavePieChart, leaves);
                }
            } else {
                Log.e("DashboardFragment", "No leaves data available");
            }

            isLeavesDataLoaded = true;
            checkAllDataLoaded();
            employeeCardShimmer.stopShimmer();
            employeeCardShimmer.setVisibility(View.GONE);
            employeeCard.setVisibility(View.VISIBLE);
        });
    }

    private void fetchData() {
        showLoaderWithBlur();
        attendanceViewModel.fetchEmployeeAttendance(startDateString, endDateString);
        leavesViewModel.fetchLeavesData();
        swipeRefreshLayout.setRefreshing(false);
    }

    // Check if all data is loaded and update the loader visibility
    private void checkAllDataLoaded() {
        if (isProfileDataLoaded && isAttendanceDataLoaded && isLeavesDataLoaded) {
            stopShimmerAnimations();
            hideLoaderWithBlur();
        }
    }

    private void startShimmerAnimations() {
        punchCardShimmer.startShimmer();
        employeeCardShimmer.startShimmer();
        leavePieChartShimmer.startShimmer();
    }

    private void stopShimmerAnimations() {
        punchCardShimmer.stopShimmer();
        employeeCardShimmer.stopShimmer();
        leavePieChartShimmer.stopShimmer();
    }

    private void initializeViews(View root) {
        // Shimmer layouts
        punchCardShimmer = root.findViewById(R.id.punchCardShimmer);
        employeeCardShimmer = root.findViewById(R.id.employeeCardShimmer);
        leavePieChartShimmer = root.findViewById(R.id.leavePieChartShimmer);

        // Real layouts
        punchCardView = root.findViewById(R.id.punchCardView);
        employeeCard = root.findViewById(R.id.employeeCard);
        leavePieChartContainer = root.findViewById(R.id.leavePieChartContainer);

        // Real layouts values
        todayDate = root.findViewById(R.id.todayDate);
//        punchButton = root.findViewById(R.id.punchButton);
        tvEmployeeNameValue = root.findViewById(R.id.tvEmployeeNameValue);
        tvEmployeeDesignationValue = root.findViewById(R.id.tvEmployeeDesignationValue);
        tvEmployeeShiftValue = root.findViewById(R.id.tvEmployeeShiftValue);
        tvEmployeeContactValue = root.findViewById(R.id.tvEmployeeContactValue);
        tvEmployeeEmailValue = root.findViewById(R.id.tvEmployeeEmailValue);
        ivEmployeeProfile = root.findViewById(R.id.ivEmployeeProfile);
        tvPunchInTime = root.findViewById(R.id.tvPunchInTime);
        tvPunchOutTime = root.findViewById(R.id.tvPunchOutTime);
        leavePieChart = root.findViewById(R.id.leavePieChart);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }
}
