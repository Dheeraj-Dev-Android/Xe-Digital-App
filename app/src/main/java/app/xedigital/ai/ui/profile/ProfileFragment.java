package app.xedigital.ai.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.FragmentProfileBinding;
import app.xedigital.ai.model.profile.Employee;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        binding.profileLoader.setVisibility(View.VISIBLE);
        binding.emptyStateText.setVisibility(View.GONE);
        binding.punchCardView.setVisibility(View.GONE);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        String authToken = sharedPreferences.getString("authToken", null);

        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
        // Show the ProgressBar initially

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            binding.profileLoader.setVisibility(View.GONE);
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
                        binding.reportingManagerTitle.setText("Name");
                        binding.reportingManagerName.setText(employee.getReportingManager().getFirstname() + " " + employee.getReportingManager().getLastname());
                        binding.reportingManagerEmail.setText("Email");
                        binding.reportingManagerEmailValue.setText(employee.getReportingManager().getEmail());
                    } else {
                        binding.reportingManagerName.setText("Not Assigned");
                        binding.reportingManagerEmailValue.setText("");
                    }
                    if (employee.getCrossmanager() != null) {
                        binding.crossManagerTitle.setText("Name");
                        binding.crossManagerName.setText(employee.getCrossmanager().getFirstname() + " " + employee.getCrossmanager().getLastname());
                        binding.crossManagerEmail.setText("Email");
                        binding.crossManagerEmailValue.setText(employee.getCrossmanager().getEmail());
                    } else {
                        binding.crossManagerName.setText("Not Assigned");
                        binding.crossManagerEmailValue.setText("");
                    }

                } else {
                    Toast.makeText(requireContext(), "Failed to fetch profile. Please check your network connection.", Toast.LENGTH_SHORT).show();
                }
                binding.punchCardView.setVisibility(View.VISIBLE);
                binding.emptyStateText.setVisibility(View.GONE);
            } else {
                binding.punchCardView.setVisibility(View.GONE);
                binding.emptyStateText.setVisibility(View.VISIBLE);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_edit_profile) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_nav_profile_to_nav_edit_profile);
            Toast.makeText(requireContext(), "Edit Profile", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}