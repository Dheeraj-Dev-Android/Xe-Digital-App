package app.xedigital.ai.adminUI.allUsers;

import static app.xedigital.ai.ui.timesheet.SelectedTimesheetFragment.ARG_SELECTED_ITEM;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.Admin.AdminUsers.UsersItem;
import app.xedigital.ai.model.Admin.Role.RolesItem;

public class EditUserFragment extends Fragment {

    private EditUserViewModel mViewModel;
    private UsersItem selectedUser;

    private TextInputEditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword;

    private AutoCompleteTextView spinnerRole, spinnerStatus;
    private ProgressBar progressBar;

    public static EditUserFragment newInstance() {
        return new EditUserFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditUserViewModel.class);

        // Retrieve selected user from bundle
        if (getArguments() != null && getArguments().containsKey(ARG_SELECTED_ITEM)) {
            selectedUser = (UsersItem) getArguments().getSerializable(ARG_SELECTED_ITEM);
        }

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String token = "jwt " + authToken;
        mViewModel.fetchRoles(token);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_user, container, false);

        // Initialize views
        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        spinnerRole = view.findViewById(R.id.spinnerRole);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        progressBar = view.findViewById(R.id.progress_bar);

        // Populate fields with selected user data
        if (selectedUser != null) {
            editTextFirstName.setText(selectedUser.getFirstname());
            editTextLastName.setText(selectedUser.getLastname());
            editTextEmail.setText(selectedUser.getEmail());

        } else {
            Toast.makeText(requireContext(), "User data not available", Toast.LENGTH_SHORT).show();
        }
        mViewModel.getRolesLiveData().observe(getViewLifecycleOwner(), roles -> {
            if (roles != null && !roles.isEmpty()) {
                List<String> roleNames = new ArrayList<>();
                for (RolesItem role : roles) {
                    roleNames.add(role.getDisplayName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, roleNames);
                spinnerRole.setAdapter(adapter);

                // Set current role if editing existing user
                if (selectedUser != null && selectedUser.getRole() != null) {
                    String currentRole = selectedUser.getRole().getDisplayName();
                    spinnerRole.setText(currentRole, false);
                }
            }
        });

        // Setup spinnerStatus with "Active" and "Deactive" options
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Active");
        statusOptions.add("Deactive");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions);
        spinnerStatus.setAdapter(statusAdapter);

// Set default selection based on isActive() boolean
        if (selectedUser != null) {
            String currentStatus = selectedUser.isActive() ? "Active" : "Deactive";
            spinnerStatus.setText(currentStatus, false);
        }


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditUserViewModel.class);
    }
}
