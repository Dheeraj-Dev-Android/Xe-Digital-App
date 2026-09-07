package app.xedigital.ai.ui.attendance;

import static app.xedigital.ai.ui.regularize_attendance.RegularizeFragment.ARG_ATTENDANCE_ITEM;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.AttendanceAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentAttendanceBinding;
import app.xedigital.ai.model.attendance.Data;
import app.xedigital.ai.model.attendance.EmployeeAttendanceResponse;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.model.regularizeApplied.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.model.regularizeApplied.RegularizeAppliedResponse;
import app.xedigital.ai.model.regularizeLimit.RegularizeLimitResponse;
import app.xedigital.ai.ui.timesheet.FilterAppliedListener;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.FilterBottomSheetDialogFragment;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceFragment extends Fragment implements FilterAppliedListener, AttendanceAdapter.OnAttendanceActionListener {
    private static final String TAG = "AttendanceFragment";
    private FragmentAttendanceBinding binding;
    private AttendanceViewModel attendanceViewModel;
    private RecyclerView recyclerViewAttendance;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private String currentStartDate = "";
    private String currentEndDate = "";
    private String authToken = "";
    private AlertDialog customLoadingDialog;

    public static String getDayOfWeek(String dateString) {
        if (dateString == null || dateString.equals("1900-01-01T00:00:00.000Z")) {
            return "N/A";
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                return dayOfWeekFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "N/A";
    }

    @Override
    public void onFilterApplied(String startDate, String endDate) {
        currentStartDate = startDate;
        currentEndDate = endDate;
        attendanceViewModel.fetchAttendance(startDate, endDate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        setHasOptionsMenu(true);

        binding = FragmentAttendanceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        attendanceViewModel.storeLoginData(authToken);

        if (currentStartDate.isEmpty() && currentEndDate.isEmpty()) {
            attendanceViewModel.fetchAttendance("", "");
        } else {
            attendanceViewModel.fetchAttendance(currentStartDate, currentEndDate);
        }

        recyclerViewAttendance = binding.recyclerViewAttendance;
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadingProgress = binding.loadingProgress;
        emptyStateText = binding.emptyStateText;

        attendanceViewModel.attendance.observe(getViewLifecycleOwner(), attendanceList -> {
            if (attendanceList != null) {
                loadingProgress.setVisibility(View.GONE);
                List<EmployeePunchDataItem> attendance = parseAttendanceData(attendanceList);
                if (attendance.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    recyclerViewAttendance.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    recyclerViewAttendance.setVisibility(View.VISIBLE);
                    AttendanceAdapter adapter = new AttendanceAdapter(attendance, this);
                    recyclerViewAttendance.setAdapter(adapter);
                }
            } else {
                Log.d(TAG, "Attendance List is null");
                loadingProgress.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
                recyclerViewAttendance.setVisibility(View.GONE);
            }
        });

        attendanceViewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                attendanceViewModel.showToastMessage(null);
            }
        });

        attendanceViewModel.getShowNoDataAlert().observe(getViewLifecycleOwner(), showAlert -> {
            if (showAlert) {
                showNoDataAlert();
            }
        });

        return root;
    }

    /**
     * Intercepts regularize button click and validates monthly limit before navigating.
     */
    @Override
    public void onRegularizeClick(EmployeePunchDataItem attendanceItem) {
        if (!isAdded() || getContext() == null) return;

        showCustomLoader("Checking regularization limit...");

        APIInterface apiInterface = APIClient.getInstance().getRegularizeApplied();
        String authHeader = "jwt " + authToken;

        // Step 1: Check regularization limit rule
        apiInterface.getAttendenceRegularizationLimit(authHeader).enqueue(new Callback<RegularizeLimitResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegularizeLimitResponse> call, @NonNull Response<RegularizeLimitResponse> response) {
                if (!isAdded() || getContext() == null) {
                    dismissCustomLoader();
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    boolean isRestricted = response.body().getData().isAttendenceRegularization();
                    int maxAllowedLimit = response.body().getData().getAttendenceRegularizationCount();

                    // If false -> Unlimited applications allowed
                    if (!isRestricted) {
                        dismissCustomLoader();
                        navigateToRegularize(attendanceItem);
                        return;
                    }

                    // If true -> Check applied regularizations for this punch date's month
                    checkAppliedCountAndProceed(apiInterface, authHeader, attendanceItem, maxAllowedLimit);
                } else {
                    dismissCustomLoader();
                    CustomDialogHelper.showErrorDialog(requireContext(), "Error", "Unable to verify regularization limit.", "Close", null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegularizeLimitResponse> call, @NonNull Throwable t) {
                dismissCustomLoader();
                if (isAdded() && getContext() != null) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Network Error", "Unable to connect to the server. Please check your internet connection.", "Close", null);
                }
            }
        });
    }

    /**
     * Step 2: Fetch history and count applied records for the target punch month.
     */
    private void checkAppliedCountAndProceed(APIInterface apiInterface, String authHeader,
                                             EmployeePunchDataItem attendanceItem, int maxAllowedLimit) {
        apiInterface.getRegularizeApplied(authHeader).enqueue(new Callback<RegularizeAppliedResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegularizeAppliedResponse> call, @NonNull Response<RegularizeAppliedResponse> response) {
                dismissCustomLoader();
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<AttendanceRegularizeAppliedItem> appliedList = response.body().getData().getAttendanceRegularizeApplied();

                    // Extract year-month of the punch record being regularized (e.g. "2025-02")
                    String targetDate = attendanceItem.getPunchDateFormat() != null ? attendanceItem.getPunchDateFormat() : attendanceItem.getPunchDate();
                    String targetYearMonth = extractYearMonthKey(targetDate);
                    String readableMonth = getReadableMonthName(targetDate);

                    int appliedCountThisMonth = 0;

                    if (appliedList != null && !appliedList.isEmpty()) {
                        for (AttendanceRegularizeAppliedItem item : appliedList) {
                            if (item != null) {
                                // Prefer punchDate, fallback to appliedDate or createdAt
                                String itemDate = item.getPunchDate();
                                if (itemDate == null || itemDate.isEmpty()) {
                                    itemDate = item.getAppliedDate();
                                }
                                if (itemDate == null || itemDate.isEmpty()) {
                                    itemDate = item.getCreatedAt();
                                }

                                String itemYearMonth = extractYearMonthKey(itemDate);
                                if (!targetYearMonth.isEmpty() && targetYearMonth.equals(itemYearMonth)) {
                                    appliedCountThisMonth++;
                                }
                            }
                        }
                    }

                    // Compare count with limit
                    if (appliedCountThisMonth >= maxAllowedLimit) {
                        showLimitExceededDialog(readableMonth, maxAllowedLimit, appliedCountThisMonth);
                    } else {
                        navigateToRegularize(attendanceItem);
                    }
                } else {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Error", "Failed to retrieve regularization history.", "Close", null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegularizeAppliedResponse> call, @NonNull Throwable t) {
                dismissCustomLoader();
                if (isAdded() && getContext() != null) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Network Error", "Unable to connect to the server. Please check your internet connection.", "Close", null);
                }
            }
        });
    }

    private void navigateToRegularize(EmployeePunchDataItem attendanceItem) {
        if (getView() == null) return;
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ATTENDANCE_ITEM, attendanceItem);
        Navigation.findNavController(getView()).navigate(R.id.action_nav_attendance_to_regularizeFragment, bundle);
    }

    private void showLimitExceededDialog(String monthName, int maxLimit, int currentCount) {
        String message = "You have already applied <b>" + currentCount + " time(s)</b> for <b>" + monthName +
                "</b>.<br><br>The maximum allowed limit is <b>" + maxLimit + "</b> per month.";
        CustomDialogHelper.showErrorDialogHtml(requireContext(), "Limit Reached", message, "Close");
    }

    private void showCustomLoader(String message) {
        if (getContext() == null) return;
        dismissCustomLoader();
        customLoadingDialog = CustomDialogHelper.showLoadingDialog(requireContext(), message);
    }

    private void dismissCustomLoader() {
        if (customLoadingDialog != null && customLoadingDialog.isShowing()) {
            customLoadingDialog.dismiss();
            customLoadingDialog = null;
        }
    }

    private String extractYearMonthKey(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return "";

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format, Locale.getDefault());
                Date parsedDate = parser.parse(rawDate);
                if (parsedDate != null) {
                    return new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(parsedDate);
                }
            } catch (Exception ignored) {
            }
        }

        if (rawDate.length() >= 7 && rawDate.charAt(4) == '-') {
            return rawDate.substring(0, 7);
        }
        return "";
    }

    private String getReadableMonthName(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return "this month";

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format, Locale.getDefault());
                Date parsedDate = parser.parse(rawDate);
                if (parsedDate != null) {
                    return new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(parsedDate);
                }
            } catch (Exception ignored) {
            }
        }
        return "this month";
    }

    private void showNoDataAlert() {
        CustomDialogHelper.showInfoDialog(
                requireContext(),
                "Attendance",
                "No Attendance data available for this month. Use filter to get previous months attendance.",
                "Okay",
                null
        );
    }

    private List<EmployeePunchDataItem> parseAttendanceData(EmployeeAttendanceResponse attendanceResponse) {
        List<EmployeePunchDataItem> attendanceDataList = new ArrayList<>();

        Data data = attendanceResponse.getData();
        if (data != null) {
            List<EmployeePunchDataItem> punchDataList = data.getEmployeePunchData();
            if (punchDataList != null) {
                for (EmployeePunchDataItem punchData : punchDataList) {
                    String date = punchData.getPunchDateFormat();
                    String day = getDayOfWeek(date);
                    punchData.setDayOfWeek(day);

                    String rawPunchIn = punchData.getPunchIn();
                    String rawPunchOut = punchData.getPunchOut();

                    String totalTime = DateTimeUtils.calculateTotalTime(rawPunchIn, rawPunchOut);
                    punchData.setTotalTime(totalTime);

                    String shiftStart = "09:00";
                    String shiftEnd = "18:00";

                    if (punchData.getShift() != null) {
                        if (punchData.getShift().getStartTime() != null && !punchData.getShift().getStartTime().isEmpty()) {
                            shiftStart = punchData.getShift().getStartTime();
                        }
                        if (punchData.getShift().getEndTime() != null && !punchData.getShift().getEndTime().isEmpty()) {
                            shiftEnd = punchData.getShift().getEndTime();
                        }
                    }

                    String lateTime = DateTimeUtils.calculateLateTime(rawPunchIn, shiftStart);
                    punchData.setLateTime(lateTime);

                    String overtime = DateTimeUtils.calculateOvertime(totalTime, shiftStart, shiftEnd);
                    punchData.setOvertime(overtime);

                    attendanceDataList.add(punchData);
                }
            }
        }
        return attendanceDataList;
    }

    @Override
    public void onDestroyView() {
        dismissCustomLoader();
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_attendance_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_filter_attendance) {
            FilterBottomSheetDialogFragment filterBottomSheetDialogFragment = new FilterBottomSheetDialogFragment();
            filterBottomSheetDialogFragment.setFilterAppliedListener(AttendanceFragment.this);
            filterBottomSheetDialogFragment.show(getParentFragmentManager(), filterBottomSheetDialogFragment.getTag());

            return true;
        }
        return false;
    }
}