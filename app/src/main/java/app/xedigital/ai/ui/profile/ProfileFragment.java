package app.xedigital.ai.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.FragmentProfileBinding;
import app.xedigital.ai.model.profile.Employee;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private CircularProgressIndicator progressIndicator;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        progressIndicator = binding.progressIndicator;

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        String authToken = sharedPreferences.getString("authToken", null);

        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
        // Show the ProgressBar initially
        progressIndicator.setVisibility(View.VISIBLE);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            progressIndicator.setVisibility(View.GONE);
            if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null) {

                Employee employee = userProfile.getData().getEmployee();

                if (employee != null) {
                    Object profileImageUrl = employee.getProfileImageUrl();
                    ImageView profileImage = binding.profileImage;

                    if (profileImageUrl != null) {
                        Glide.with(requireContext()).load(profileImageUrl).circleCrop().into(profileImage);
                    } else {
                        profileImage.setImageResource(R.mipmap.ic_default_profile);
                    }

                    binding.employeeCodeTitle.setText("Emp Code ");
                    binding.employeeCodeValue.setText(employee.getEmployeeCode());
                    binding.nameTitle.setText("Name ");
                    binding.nameValue.setText(employee.getFirstname() + " " + employee.getLastname());
                    binding.emailTitle.setText("Email ");
                    binding.emailValue.setText(employee.getEmail());
                    binding.contactTitle.setText("Contact ");
                    binding.contactValue.setText(employee.getContact());
                    if (employee.getDepartment() != null && employee.getDesignation() != null) {
                        binding.departmentTitle.setText("Department ");
                        binding.departmentValue.setText(employee.getDepartment().getName());
                        binding.designationTitle.setText("Designation ");
                        binding.designationValue.setText(employee.getDesignation());
                    }
                    binding.levelGradeTitle.setText("Level / Grade ");
                    binding.levelGradeValue.setText(employee.getLevel() + " / " + employee.getGrade());
                    if (employee.getShift() != null) {
                        binding.shiftTitle.setText("Shift ");
                        binding.shiftValue.setText(employee.getShift().getName());
                        binding.shiftTimingTitle.setText("Timing ");
                        binding.shiftTimingValue.setText(employee.getShift().getStartTime() + " - " + employee.getShift().getEndTime());
                    }
                    if (employee.getReportingManager() != null) {
                        binding.reportingManagerTitle.setText("Name ");
                        binding.reportingManagerName.setText(employee.getReportingManager().getFirstname() + " " + employee.getReportingManager().getLastname());
                        binding.reportingManagerEmail.setText("Email ");
                        binding.reportingManagerEmailValue.setText(employee.getReportingManager().getEmail());
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch profile. Please check your network connection.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Failed to fetch profile. Please check your network connection.", Toast.LENGTH_SHORT).show();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}