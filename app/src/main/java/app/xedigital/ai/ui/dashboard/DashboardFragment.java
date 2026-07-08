package app.xedigital.ai.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
import app.xedigital.ai.model.allEmployee.EmployeesItem;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.model.leaves.LeavesItem;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.model.profile.Shift;
import app.xedigital.ai.ui.attendance.AttendanceViewModel;
import app.xedigital.ai.ui.leaves.LeavesViewModel;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;

public class DashboardFragment extends Fragment {
    private static final SimpleDateFormat DATE_FORMAT_YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT_DD_MMMM_YYYY = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT_HH_MM_SS_A = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
    private static final SimpleDateFormat INPUT_SHIFT_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat OUTPUT_SHIFT_FORMAT = new SimpleDateFormat("hh:mm a", Locale.getDefault());
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

    // Shimmer layouts
    private ShimmerFrameLayout punchCardShimmer;
    private ShimmerFrameLayout employeeCardShimmer;
    private ShimmerFrameLayout leavePieChartShimmer;

    // Real layouts
    private MaterialCardView punchCardView;
    private MaterialCardView employeeCard;
    private RecyclerView rvBirthdayCarousel;
    private View birthdayCardView;
    private MaterialCardView leavePieChartContainer;

    // Real layouts values
    private TextView todayDate;
    private TextView tvEmployeeNameValue;
    private TextView tvEmployeeDesignationValue;
    private TextView tvEmployeeShiftValue;
    private TextView tvEmployeeContactValue;
    private TextView tvEmployeeEmailValue;
    private ImageView ivEmployeeProfile;
    private TextView tvPunchInTime;
    private TextView tvPunchOutTime;
    private TextView tvEmployeeCodeValue;
    private DashboardViewModel viewModal;

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
        if (entries.stream().allMatch(e -> e.getValue() == 0f)) {
            entries.clear();
            entries.add(new PieEntry(1f, "0 leaves"));
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });
        } else {
            dataSet.setValueFormatter(new PercentFormatter(pieChart));
        }
        Map<String, Integer> leaveTypeColors = new HashMap<>();
        leaveTypeColors.put("Casual Leave", Color.rgb(51, 206, 255));
        leaveTypeColors.put("Sick Leave", Color.rgb(125, 206, 160));
        leaveTypeColors.put("Privilege Leave", Color.rgb(255, 165, 0));
        leaveTypeColors.put("Restricted Holidays", Color.rgb(199, 0, 57));
        leaveTypeColors.put("Occasional Leave", Color.rgb(161, 16, 76));
        List<Integer> colors = new ArrayList<>();
        for (PieEntry entry : entries) {
            String leaveType = entry.getLabel();
            int color = leaveTypeColors.getOrDefault(leaveType, Color.rgb(102, 0, 102));
            colors.add(color);
        }
        dataSet.setColors(colors);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(8f);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateXY(1000, 1000);
        pieChart.setDrawEntryLabels(false);

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedTotalBalanceLeaves = decimalFormat.format(totalBalanceLeaves);

        pieChart.setCenterText("Balance:\n" + formattedTotalBalanceLeaves);
        pieChart.setCenterTextColor(Color.rgb(161, 16, 7));
        pieChart.setCenterTextSize(9f);
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchData);
        initializeViews(root);
        updateGreetingText();

        binding.punchButton.setOnClickListener(v -> {
//            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
            String userId = prefManager.getString("userId", "");
            String authToken = prefManager.getString("authToken", "");
            String name = prefManager.getString("name", "");
            Intent intent = new Intent(requireContext(), PunchActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("authToken", authToken);
            intent.putExtra("name", name);
            startActivity(intent);
        });


        // --- QUICK ACTION NAVIGATIONS ---

        binding.quickActionsCard.findViewById(R.id.btnQuickAttendance).setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_attendance);
        });

        binding.quickActionsCard.findViewById(R.id.btnQuickApplyLeave).setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_leaves);
        });

        binding.quickActionsCard.findViewById(R.id.btnQuickTimesheet).setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_dcr_form);
        });

        binding.quickActionsCard.findViewById(R.id.btnQuickBookMeeting).setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_meeting_room);
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        blurOverlay = view.findViewById(R.id.blurOverlay);
        loader = view.findViewById(R.id.loader);

        viewModal = new ViewModelProvider(this).get(DashboardViewModel.class);
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);

//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");
        String userId = prefManager.getString("userId", "");
        viewModal.fetchEmployeeBirthdays(authToken);

        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        endDateString = DATE_FORMAT_YYYY_MM_DD.format(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = calendar.getTime();
        startDateString = DATE_FORMAT_YYYY_MM_DD.format(startDate);

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
                String todayDateString = DATE_FORMAT_DD_MMMM_YYYY.format(currentDate);
                String currentTimeString = TIME_FORMAT_HH_MM_SS_A.format(currentDate);
                binding.todayDate.setText(todayDateString + " - " + currentTimeString);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            if (userprofileResponse != null && userprofileResponse.getData() != null && userprofileResponse.getData().getEmployee() != null) {
                Employee employee = userprofileResponse.getData().getEmployee();
                if (employee != null) {
                    String employeeName = employee.getFirstname() != null ? employee.getFirstname() : "";
                    String employeeLastName = employee.getLastname() != null ? employee.getLastname() : "";
                    String employeeEmail = employee.getEmail() != null ? employee.getEmail() : "";
                    String empContact = employee.getContact() != null ? employee.getContact() : "";
                    String empDesignation = employee.getDesignation() != null ? employee.getDesignation() : "";

                    binding.tvEmployeeNameValue.setText((!employeeName.isEmpty() || !employeeLastName.isEmpty()) ? employeeName + " " + employeeLastName : "N/A");
                    binding.tvHeaderEmployeeName.setText((!employeeName.isEmpty() ? employeeName : "N/A"));
                    binding.tvEmployeeDesignationValue.setText(!empDesignation.isEmpty() ? empDesignation : "N/A");
                    binding.tvEmployeeCodeValue.setText(!employee.getEmployeeCode().isEmpty() ? employee.getEmployeeCode() : "N/A");

                    Shift shift = employee.getShift();
                    String startTime = shift != null && shift.getStartTime() != null ? shift.getStartTime() : "";
                    String endTime = shift != null && shift.getEndTime() != null ? shift.getEndTime() : "";

                    if (!startTime.isEmpty() && !endTime.isEmpty()) {
                        String shiftTimeString = startTime + " - " + endTime;
                        binding.tvEmployeeShiftValue.setText(formatShiftTime(shiftTimeString));
                    } else {
                        binding.tvEmployeeShiftValue.setText("N/A");
                    }

                    binding.tvEmployeeContactValue.setText(!empContact.isEmpty() ? empContact : "N/A");
                    binding.tvEmployeeEmailValue.setText(!employeeEmail.isEmpty() ? employeeEmail : "N/A");

                    Object profileImageUrl = employee.getProfileImageUrl();
                    ImageView profileImage = binding.ivEmployeeProfile;
                    if (profileImageUrl != null) {
                        Glide.with(requireContext()).load(profileImageUrl).placeholder(R.drawable.ic_profile_placeholder).error(R.drawable.ic_profile_placeholder).into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } else {
                    binding.tvEmployeeNameValue.setText("N/A");
                    binding.tvEmployeeDesignationValue.setText("N/A");
                    binding.tvEmployeeShiftValue.setText("N/A");
                    binding.tvEmployeeContactValue.setText("N/A");
                    binding.tvEmployeeEmailValue.setText("N/A");
                    binding.ivEmployeeProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                binding.tvEmployeeNameValue.setText("N/A");
                binding.tvEmployeeDesignationValue.setText("N/A");
                binding.tvEmployeeShiftValue.setText("N/A");
                binding.tvEmployeeContactValue.setText("N/A");
                binding.tvEmployeeEmailValue.setText("N/A");
                binding.ivEmployeeProfile.setImageResource(R.drawable.ic_profile_placeholder);
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
                            Date punchDate = DATE_FORMAT_YYYY_MM_DD.parse(punchData.getPunchDateFormat());
                            String formattedPunchDate = DATE_FORMAT_YYYY_MM_DD.format(punchDate);
                            return formattedPunchDate.equals(currentDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }).collect(Collectors.toList());
                    requireActivity().runOnUiThread(() -> {
                        if (binding != null) {
                            boolean isDataAvailable = !currentPunchData.isEmpty();
                            boolean isToday = false;

                            if (isDataAvailable) {
                                String punchDateStr = currentPunchData.get(0).getPunchDateFormat();

                                if (punchDateStr != null && !punchDateStr.trim().isEmpty()) {
                                    try {
                                        LocalDate localPunchDate = LocalDate.parse(punchDateStr);
                                        LocalDate today = LocalDate.now();
                                        isToday = localPunchDate.equals(today);

                                    } catch (DateTimeParseException e) {
                                        e.printStackTrace();
                                        isToday = false;
                                    }
                                }
                            }


                            if (isDataAvailable && isToday) {
                                punchIn = DateTimeUtils.formatTime(currentPunchData.get(0).getPunchIn());
                                punchOut = DateTimeUtils.formatTime(currentPunchData.get(0).getPunchOut());

                                binding.tvPunchInTime.setText("Punch In: " + punchIn);
                                binding.tvPunchOutTime.setText("Punch Out: " + punchOut);

                                if (punchIn != null && !punchIn.trim().isEmpty() && !punchIn.equals("-")) {
                                    binding.tvPunchStatusValue.setText("Punched In");
                                    binding.tvPunchStatusValue.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));

                                    binding.tvPunchButtonLabel.setText("Punch Out");
                                    binding.punchButton.setCardBackgroundColor(getResources().getColor(R.color.rejected_color, requireContext().getTheme()));
                                } else {
                                    resetPunchUI();
                                }
                            } else {
                                resetPunchUI();
                            }
                        }
                    });
                }).start();
            } else {
                if (binding != null) {
                    binding.tvPunchInTime.setText("Punch In: --:--");
                    binding.tvPunchOutTime.setText("Punch Out: --:--");
                    punchCardView.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(requireContext(), "No attendance data available", Toast.LENGTH_SHORT).show();
                }
            }
            punchCardShimmer.stopShimmer();
            punchCardShimmer.setVisibility(View.GONE);
            isAttendanceDataLoaded = true;
            checkAllDataLoaded();
        });

        leavesViewModel.leavesData.observe(getViewLifecycleOwner(), leavesData -> {
            if (leavesData != null && leavesData.getData() != null) {
                List<LeavesItem> leaves = leavesData.getData().getLeaves();
                if (leaves == null || leaves.isEmpty()) {
                    Toast.makeText(requireContext(), "No leaves data available", Toast.LENGTH_SHORT).show();
                } else {
                    binding.leavePieChart.setVisibility(View.VISIBLE);
                    updatePieChartData(binding.leavePieChart, leaves);
                }
            } else {
                Toast.makeText(requireContext(), "No leaves data available", Toast.LENGTH_SHORT).show();
            }

            isLeavesDataLoaded = true;
            checkAllDataLoaded();
            employeeCardShimmer.stopShimmer();
            employeeCardShimmer.setVisibility(View.GONE);
            employeeCard.setVisibility(View.VISIBLE);
        });

        viewModal.getEmployeeBirthdayData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null && response.getData().getEmployees() != null) {
                filterAndProcessBirthdays(response.getData().getEmployees());
            }
        });
    }

    private void updateGreetingText() {
        if (binding == null) return;

        int hour = LocalTime.now().getHour();
        String greeting;

        if (hour >= 4 && hour < 12) {
            greeting = "Good Morning ! ";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon ! ";
        } else if (hour >= 17 && hour < 22) {
            greeting = "Good Evening ! ";
        } else {
            greeting = "Good Night ! "; // Covers 10 PM to 3:59 AM
        }

        binding.tvGreeting.setText(greeting);
    }

    private void resetPunchUI() {
        if (binding == null) return;

        // Reset Times
        binding.tvPunchInTime.setText("Punch In: --:--");
        binding.tvPunchOutTime.setText("Punch Out: --:--");

        // Reset Status Value
        binding.tvPunchStatusValue.setText("Not Punched Yet");
        binding.tvPunchStatusValue.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));

        // Reset Button Label & Text Color
        binding.tvPunchButtonLabel.setText("Punch In");
        binding.tvPunchButtonLabel.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme())); // Change to your default label color

        // Reset Card Background
        binding.punchButton.setCardBackgroundColor(getResources().getColor(R.color._0000, requireContext().getTheme()));
    }

    private void fetchData() {
        showLoaderWithBlur();
        attendanceViewModel.fetchEmployeeAttendance(startDateString, endDateString);
        leavesViewModel.fetchLeavesData();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void checkAllDataLoaded() {
        if (isProfileDataLoaded && isAttendanceDataLoaded && isLeavesDataLoaded) {
            stopShimmerAnimations();
            hideLoaderWithBlur();
        }
    }

    private void startShimmerAnimations() {
        punchCardShimmer.setVisibility(View.VISIBLE);
        punchCardShimmer.startShimmer();

        employeeCardShimmer.setVisibility(View.VISIBLE);
        employeeCardShimmer.startShimmer();

        leavePieChartShimmer.setVisibility(View.VISIBLE);
        leavePieChartShimmer.startShimmer();

        punchCardView.setVisibility(View.GONE);
        employeeCard.setVisibility(View.GONE);
        leavePieChartContainer.setVisibility(View.GONE);

        binding.quickActionsShimmer.setVisibility(View.VISIBLE);
        binding.quickActionsShimmer.startShimmer();
    }

    private void stopShimmerAnimations() {
        punchCardShimmer.stopShimmer();
        punchCardShimmer.setVisibility(View.GONE);

        employeeCardShimmer.stopShimmer();
        employeeCardShimmer.setVisibility(View.GONE);

        leavePieChartShimmer.stopShimmer();
        leavePieChartShimmer.setVisibility(View.GONE);

        punchCardView.setVisibility(View.VISIBLE);
        employeeCard.setVisibility(View.VISIBLE);
        leavePieChartContainer.setVisibility(View.VISIBLE);

        binding.quickActionsShimmer.stopShimmer();
        binding.quickActionsShimmer.setVisibility(View.GONE);
    }

    private void initializeViews(View root) {
        punchCardShimmer = root.findViewById(R.id.punchCardShimmer);
        employeeCardShimmer = root.findViewById(R.id.employeeCardShimmer);
        leavePieChartShimmer = root.findViewById(R.id.leavePieChartShimmer);

        punchCardView = root.findViewById(R.id.punchCardView);
        employeeCard = root.findViewById(R.id.employeeCard);
        leavePieChartContainer = root.findViewById(R.id.leavePieChartContainer);

        todayDate = root.findViewById(R.id.todayDate);
        tvEmployeeNameValue = root.findViewById(R.id.tvEmployeeNameValue);
        tvEmployeeCodeValue = root.findViewById(R.id.tvEmployeeCodeValue);
        tvEmployeeDesignationValue = root.findViewById(R.id.tvEmployeeDesignationValue);
        tvEmployeeShiftValue = root.findViewById(R.id.tvEmployeeShiftValue);
        tvEmployeeContactValue = root.findViewById(R.id.tvEmployeeContactValue);
        tvEmployeeEmailValue = root.findViewById(R.id.tvEmployeeEmailValue);
        ivEmployeeProfile = root.findViewById(R.id.ivEmployeeProfile);
        tvPunchInTime = root.findViewById(R.id.tvPunchInTime);
        tvPunchOutTime = root.findViewById(R.id.tvPunchOutTime);
        birthdayCardView = root.findViewById(R.id.includedBirthdayCarousel);
        if (birthdayCardView != null) {
            rvBirthdayCarousel = birthdayCardView.findViewById(R.id.rvBirthdayCarousel);
            rvBirthdayCarousel.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

            java.util.ArrayList<EmployeesItem> emptyList = new java.util.ArrayList<>();
            BirthdayCarouselAdapter initialAdapter = new BirthdayCarouselAdapter(emptyList);
            rvBirthdayCarousel.setAdapter(initialAdapter);

            LinearSnapHelper snapHelper = new LinearSnapHelper();
            snapHelper.attachToRecyclerView(rvBirthdayCarousel);
        }
    }

    private String formatShiftTime(String shiftTime) {
        if (shiftTime == null || shiftTime.isEmpty()) {
            return "";
        }

        String[] parts = shiftTime.split("-");
        if (parts.length == 2) {
            try {
                Date startTime = INPUT_SHIFT_FORMAT.parse(parts[0].trim());
                Date endTime = INPUT_SHIFT_FORMAT.parse(parts[1].trim());
                return OUTPUT_SHIFT_FORMAT.format(startTime) + " - " + OUTPUT_SHIFT_FORMAT.format(endTime);
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

    private void filterAndProcessBirthdays(List<EmployeesItem> employees) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentMonthNum = calendar.get(java.util.Calendar.MONTH) + 1;
        String currentMonthString = String.format(java.util.Locale.getDefault(), "%02d", currentMonthNum);

        List<EmployeesItem> currentMonthBirthdays = new ArrayList<>();

        for (EmployeesItem employee : employees) {
            String dob = employee.getDateOfBirth();
            if (dob != null && !dob.isEmpty()) {
                if (dob.contains("T")) {
                    dob = dob.split("T")[0];
                }

                String[] segments = dob.split("[-/]");
                if (segments.length >= 2) {
                    String monthSegment = "";
                    if (segments[0].length() == 4) {
                        monthSegment = segments[1];
                    } else if (segments[2].length() == 4) {
                        monthSegment = segments[1];
                    }

                    if (monthSegment.equals(currentMonthString)) {
                        currentMonthBirthdays.add(employee);
                    }
                }
            }
        }

        // Sort the filtered birthdays in ascending order (Day 1 to Day 31)
        currentMonthBirthdays.sort((emp1, emp2) -> {
            int day1 = getDayFromDob(emp1.getDateOfBirth());
            int day2 = getDayFromDob(emp2.getDateOfBirth());
            return Integer.compare(day1, day2);
        });

        displayBirthdayList(currentMonthBirthdays);
    }

    private int getDayFromDob(String dob) {
        if (dob == null || dob.isEmpty()) return 0;
        try {
            if (dob.contains("T")) {
                dob = dob.split("T")[0];
            }

            String[] segments = dob.split("[-/]");
            if (segments.length >= 3) {
                if (segments[0].length() == 4) {
                    return Integer.parseInt(segments[2]);
                } else if (segments[2].length() == 4) {
                    // Format: dd-MM-yyyy -> Day is at index 0 (e.g. 16-08-1998 -> 16)
                    return Integer.parseInt(segments[0]);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void displayBirthdayList(List<EmployeesItem> birthdayList) {
        if (birthdayCardView == null || rvBirthdayCarousel == null) return;

        if (birthdayList == null || birthdayList.isEmpty()) {
            birthdayCardView.setVisibility(View.GONE);
        } else {
            birthdayCardView.setVisibility(View.VISIBLE);
            BirthdayCarouselAdapter adapter = new BirthdayCarouselAdapter(birthdayList);
            rvBirthdayCarousel.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}