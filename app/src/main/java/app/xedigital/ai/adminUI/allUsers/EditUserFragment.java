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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.AdminUsers.UsersItem;
import app.xedigital.ai.model.Admin.Role.RolesItem;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserFragment extends Fragment {

    private EditUserViewModel mViewModel;
    private UsersItem selectedUser;

    private TextInputEditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private AutoCompleteTextView spinnerRole, spinnerStatus;
    private ProgressBar progressBar;
    private Button btnSubmit, btnClear;

    private List<RolesItem> allRoles = new ArrayList<>();

//    public static EditUserFragment newInstance() {
//        return new EditUserFragment();
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditUserViewModel.class);

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
        btnSubmit = view.findViewById(R.id.buttonUpdate);
        btnClear = view.findViewById(R.id.clearForm);

        if (selectedUser != null) {
            editTextFirstName.setText(selectedUser.getFirstname());
            editTextLastName.setText(selectedUser.getLastname());
            editTextEmail.setText(selectedUser.getEmail());
        } else {
            Toast.makeText(requireContext(), "User data not available", Toast.LENGTH_SHORT).show();
        }

        // Observe roles
        mViewModel.getRolesLiveData().observe(getViewLifecycleOwner(), roles -> {
            if (roles != null && !roles.isEmpty()) {
                allRoles = roles;

                List<String> roleNames = new ArrayList<>();
                for (RolesItem role : roles) {
                    roleNames.add(role.getDisplayName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, roleNames);
                spinnerRole.setAdapter(adapter);

                if (selectedUser != null && selectedUser.getRole() != null) {
                    String currentRole = selectedUser.getRole().getDisplayName();
                    spinnerRole.setText(currentRole, false);
                }
            }
        });

        // Status dropdown
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Active");
        statusOptions.add("Deactive");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions);
        spinnerStatus.setAdapter(statusAdapter);

        if (selectedUser != null) {
            String currentStatus = selectedUser.isActive() ? "Active" : "Deactive";
            spinnerStatus.setText(currentStatus, false);
        }

        // Button click
        btnSubmit.setOnClickListener(v -> sendFormDataToApi());
        btnClear.setOnClickListener(v -> {
            spinnerRole.setText("");
            spinnerStatus.setText("");
            editTextFirstName.setText("");
            editTextLastName.setText("");
            editTextEmail.setText("");
            editTextPassword.setText("");
            editTextConfirmPassword.setText("");
        });

        return view;
    }

    private void sendFormDataToApi() {
        String firstname = Objects.requireNonNull(editTextFirstName.getText()).toString().trim();
        String lastname = Objects.requireNonNull(editTextLastName.getText()).toString().trim();
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(editTextConfirmPassword.getText()).toString().trim();
        String selectedRoleName = spinnerRole.getText().toString().trim();
        String status = spinnerStatus.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isActive = status.equalsIgnoreCase("Active");

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        String authToken = "jwt " + sharedPreferences.getString("authToken", "");

        // Map role name to ID
        String roleId = null;
        for (RolesItem role : allRoles) {
            if (role.getDisplayName().equalsIgnoreCase(selectedRoleName)) {
                roleId = role.getId();
                break;
            }
        }

        if (roleId == null) {
            Toast.makeText(getContext(), "Invalid role selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String companyId = selectedUser.getCompany().getId();
        String branchId = selectedUser.getBranch().getId();

        // Build JSON
        JSONObject json = new JSONObject();
        try {
            json.put("company", companyId);
            json.put("branch", branchId);
            json.put("role", roleId);
            json.put("firstname", firstname);
            json.put("lastname", lastname);
            json.put("email", email);
            json.put("active", isActive);
            json.put("password", password);
            json.put("confirmPassword", confirmPassword);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "JSON Error", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json"));

        // Call API
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<ResponseBody> call = apiService.updateUser(authToken, selectedUser.getId(), requestBody);

        progressBar.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "User updated successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(requireContext(), "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditUserViewModel.class);
    }
}
