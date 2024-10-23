package app.xedigital.ai.ui.attendance;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import app.xedigital.ai.databinding.FragmentAttendanceBinding;
import app.xedigital.ai.model.attendance.Data;
import app.xedigital.ai.model.attendance.EmployeeAttendanceResponse;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.ui.timesheet.FilterAppliedListener;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.FilterBottomSheetDialogFragment;

public class AttendanceFragment extends Fragment implements FilterAppliedListener {
    private static final String TAG = "AttendanceFragment";
    private FragmentAttendanceBinding binding;
    private AttendanceViewModel attendanceViewModel;
    private RecyclerView recyclerViewAttendance;

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
        attendanceViewModel.fetchAttendance(startDate, endDate);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        attendanceViewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        setHasOptionsMenu(true);

        binding = FragmentAttendanceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);
        attendanceViewModel.storeLoginData(authToken);

        String startDate = "";
        String endDate = "";
        attendanceViewModel.fetchAttendance(startDate, endDate);

        recyclerViewAttendance = binding.recyclerViewAttendance;
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));

        attendanceViewModel.attendance.observe(getViewLifecycleOwner(), attendanceList -> {
            if (attendanceList != null) {
                Log.d(TAG, "Attendance List:\n " + attendanceList);
                List<EmployeePunchDataItem> attendance = parseAttendanceData(attendanceList);
//                AttendanceAdapter adapter = new AttendanceAdapter(attendance, attendanceViewModel);
//                recyclerViewAttendance.setAdapter(adapter);
                if (attendance.isEmpty()) {
                    showNoDataAlert();
                } else {
                    AttendanceAdapter adapter = new AttendanceAdapter(attendance, attendanceViewModel);
                    recyclerViewAttendance.setAdapter(adapter);
                }
            } else {
                Log.d(TAG, "Attendance List is null");
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

    private void showNoDataAlert() {
        new AlertDialog.Builder(requireContext()).setTitle("Attendance").setMessage("NO Attendance Data Available").setPositiveButton("OK", null).show();
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
                    String punchInTime = DateTimeUtils.formatTime(punchData.getPunchIn());
                    String punchOutTime = DateTimeUtils.formatTime(punchData.getPunchOut());
                    String totalTime = DateTimeUtils.calculateTotalTime(punchInTime, punchOutTime);

                    punchData.setTotalTime(totalTime);
                    attendanceDataList.add(punchData);
                    String overtime = DateTimeUtils.calculateOvertime(totalTime);
                    punchData.setOvertime(overtime);

                    if (punchData.getShift() != null) {
                        String lateTime = DateTimeUtils.calculateLateTime(punchData.getPunchIn(), punchData.getShift().getStartTime());
                        punchData.setLateTime(lateTime);
                    }
                }
            }
        }
        return attendanceDataList;
    }

    public void onDestroyView() {
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