package app.xedigital.ai.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import app.xedigital.ai.R;
import app.xedigital.ai.databinding.FragmentProfileBinding;
import app.xedigital.ai.model.profile.Employee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        String authToken = sharedPreferences.getString("authToken", null);

        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null) {

//                TableLayout profileDataContainer = binding.profileDataContainer;
//                profileDataContainer.removeAllViews();

                Employee employee = userProfile.getData().getEmployee();

                if (employee != null) {
                    Object profileImageUrl = employee.getProfileImageUrl();
                    ImageView profileImage = binding.profileImage;

                    if (profileImageUrl != null) {
                        Glide.with(requireContext()).load(profileImageUrl).circleCrop().into(profileImage);
                    } else {
                        profileImage.setImageResource(R.mipmap.ic_default_profile);
                    }

                    binding.employeeCodeTitle.setText("Employee Code: ");
                    binding.employeeCodeValue.setText(employee.getEmployeeCode());
                    binding.nameTitle.setText("Name: ");
                    binding.nameValue.setText(employee.getFirstname() + " " + employee.getLastname());
                    binding.emailTitle.setText("Email: ");
                    binding.emailValue.setText(employee.getEmail());
                    binding.contactTitle.setText("Contact: ");
                    binding.contactValue.setText(employee.getContact());
                    if (employee.getDepartment() != null && employee.getDesignation() != null) {
                        binding.departmentTitle.setText("Department: ");
                        binding.departmentValue.setText(employee.getDepartment().getName());
                        binding.designationTitle.setText("Designation: ");
                        binding.designationValue.setText(employee.getDesignation());
                    }
                    binding.levelGradeTitle.setText("Level / Grade: ");
                    binding.levelGradeValue.setText(employee.getLevel() + " / " + employee.getGrade());
                    if (employee.getShift() != null) {
                        binding.shiftTitle.setText("Shift: ");
                        binding.shiftValue.setText(employee.getShift().getName());
                        binding.shiftTimingTitle.setText("Timing: ");
                        binding.shiftTimingValue.setText(employee.getShift().getStartTime() + " - " + employee.getShift().getEndTime());
                    }
                    if (employee.getReportingManager() != null) {
                        binding.reportingManagerTitle.setText("Reporting Manager: ");
                        binding.reportingManagerName.setText(employee.getReportingManager().getFirstname() + " " + employee.getReportingManager().getLastname());
                        binding.reportingManagerEmail.setText("Email: ");
                        binding.reportingManagerEmailValue.setText(employee.getReportingManager().getEmail());
                    }


//                    addProfileDataToCard("Employee Code", employee.getEmployeeCode(), profileDataContainer);
//                    addProfileDataToCard("Name", employee.getFirstname() + " " + employee.getLastname(), profileDataContainer);
//                    addProfileDataToCard("Email", employee.getEmail(), profileDataContainer);
//                    addProfileDataToCard("Contact", employee.getContact(), profileDataContainer);
//
//
//                    // Extract string values from Department
//                    if (employee.getDepartment() != null) {
//                        addProfileDataToCard("Department", employee.getDepartment().getName(), profileDataContainer);
//                        addProfileDataToCard("Designation", employee.getDesignation(), profileDataContainer);
//                        addProfileDataToCard("Level / Grade", employee.getLevel() + " / " + employee.getGrade(), profileDataContainer);
//                    }
//
//                    // Extract string values from ReportingManager
//                    if (employee.getReportingManager() != null) {
//                        addSectionTitle("Reporting Manager", profileDataContainer);
//                        addProfileDataToCard("Name", employee.getReportingManager().getFirstname() + " " + employee.getReportingManager().getLastname(), profileDataContainer);
//                        addProfileDataToCard("Email", employee.getReportingManager().getEmail(), profileDataContainer);
//                    }
//                    // Extract string values from Shift
//                    if (employee.getShift() != null) {
//                        addSectionTitle("Shift", profileDataContainer);
//                        addProfileDataToCard("Shift", employee.getShift().getName(), profileDataContainer);
////                    addProfileDataToCard("Timing", employee.getShift().getStartTime() + " -  " + employee.getShift().getEndTime(), profileDataContainer);
//
//                        String startTime12Hr = convert24HourTo12Hour(employee.getShift().getStartTime());
//                        String endTime12Hr = convert24HourTo12Hour(employee.getShift().getEndTime());
//                        addProfileDataToCard("Timing", startTime12Hr + " - " + endTime12Hr, profileDataContainer);
//                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch profile. Please check your network connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return binding.getRoot();
    }

    private String convert24HourTo12Hour(String time24Hr) {

        try {
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date _24HourDate = _24HourSDF.parse(time24Hr);
            return _12HourSDF.format(_24HourDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return time24Hr;
        }
    }

    private void addProfileDataToCard(String title, String value, TableLayout container) {
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.quicksand_regular);

        TableRow row = new TableRow(requireContext());

        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText(title + ": ");
        titleTextView.setTypeface(typeface, Typeface.BOLD);
        titleTextView.setTextSize(16f);
        titleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        TextView valueTextView = new TextView(requireContext());
        valueTextView.setText(value);
        valueTextView.setTextSize(14f);
        valueTextView.setTypeface(typeface, Typeface.BOLD);
        valueTextView.setGravity(Gravity.END);
        valueTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.OccasionalLeave));

        row.addView(titleTextView);
        row.addView(valueTextView);

        container.addView(row);
    }

    private void addSectionTitle(String title, LinearLayout container) {
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.quicksand_regular);
        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText(title);
        titleTextView.setTypeface(typeface, Typeface.BOLD);
        titleTextView.setTextSize(18f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 8);
        titleTextView.setLayoutParams(params);
        container.addView(titleTextView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}