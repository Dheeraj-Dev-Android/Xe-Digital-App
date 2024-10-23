package app.xedigital.ai.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import app.xedigital.ai.R;
import app.xedigital.ai.activity.PunchActivity;
import app.xedigital.ai.databinding.FragmentDashboardBinding;
import app.xedigital.ai.ui.profile.ProfileViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    public String employeeName;
    public String employeeEmail;
    public String employeeLastName;
    public String empContact;
    public String empDesignation;
    //    public String empShift;
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
        getContext();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            employeeName = userprofileResponse.getData().getEmployee().getFirstname();
            employeeEmail = userprofileResponse.getData().getEmployee().getEmail();
            employeeLastName = userprofileResponse.getData().getEmployee().getLastname();
            empContact = userprofileResponse.getData().getEmployee().getContact();
//            empShift = userprofileResponse.getData().getEmployee().getShift().getStartTime() + " - " + userprofileResponse.getData().getEmployee().getShift().getEndTime();
            empDesignation = userprofileResponse.getData().getEmployee().getDesignation();

            Object profileImageUrl = userprofileResponse.getData().getEmployee().getProfileImageUrl();
            ImageView profileImage = binding.ivEmployeeProfile;

            if (profileImageUrl != null) {
                Glide.with(requireContext()).load(profileImageUrl).circleCrop().into(profileImage)
                        .onLoadFailed(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_default_profile, null));
            } else {
                Toast.makeText(requireContext(), "Profile image not found", Toast.LENGTH_SHORT).show();
                profileImage.setImageResource(R.mipmap.ic_default_profile);
            }

            binding.tvEmployeeName.setText("Name: " + employeeName + " " + employeeLastName);
            binding.tvEmployeeDesignation.setText("Designation: " + empDesignation);

            String startTime = userprofileResponse.getData().getEmployee().getShift().getStartTime();
            String endTime = userprofileResponse.getData().getEmployee().getShift().getEndTime();

            if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                String shiftTimeString = startTime + " - " + endTime;
                binding.tvEmployeeShift.setText("Shift: " + formatShiftTime(shiftTimeString));
                Log.d("ShiftTime", "Shift Time: " + shiftTimeString);
            } else {
                binding.tvEmployeeShift.setText("Shift time not available");
            }
//            binding.tvEmployeeShift.setText("Shift: " + formatShiftTime(empShift));
            binding.tvEmployeeContact.setText("Contact: " + empContact);
            binding.tvEmployeeEmail.setText("Email: " + employeeEmail);
        });
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
