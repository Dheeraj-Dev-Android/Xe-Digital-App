package app.xedigital.ai.ui.claim_management;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.CurrencyArrayAdapter;
import app.xedigital.ai.databinding.FragmentClaimManagementBinding;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.SecurePrefManager;

public class ClaimManagementFragment extends Fragment {

    private FragmentClaimManagementBinding binding;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private ClaimManagementViewModel viewModel;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private int uploadedCount = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", null);
        String userId = prefManager.getString("userId", null);

        viewModel = new ViewModelProvider(this).get(ClaimManagementViewModel.class);
        viewModel.initAuth(authToken);
        viewModel.fetchBranchDetails(userId);

        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
        profileViewModel.userProfile.observe(this, response -> {
            if (response != null && response.getData() != null && response.getData().getEmployee() != null) {
                viewModel.cacheEmployeeProfile(response.getData().getEmployee());
            }
        });

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                processPickedFiles(result.getData());
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClaimManagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupPickersAndButtons();
        setupDropdownObservers();
        setupLayoutVisibilityLogic();

        // Listen to ViewModel actions
        viewModel.toastMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.operationSuccess.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                viewModel.clearFormFields();
                binding.selectedFilesContainer.removeAllViews();
                binding.selectedFileText.setVisibility(View.GONE);

                uploadedCount = 0;
            }
        });

        return binding.getRoot();
    }

    private void setupPickersAndButtons() {
        binding.claimDateInput.setOnClickListener(v -> new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            String formattedDate = dateFormatter.format(calendar.getTime());

            // 1. Update ViewModel state
            viewModel.claimDate.setValue(formattedDate);

            // 2. Explicitly update view text to force layout refresh
            binding.claimDateInput.setText(formattedDate);

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());

        binding.uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            filePickerLauncher.launch(intent);
        });

        binding.saveButton.setOnClickListener(v -> performAction(false));
        binding.submitButton.setOnClickListener(v -> performAction(true));

        binding.viewClaimChip.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_nav_claim_management_to_nav_view_claim));
        binding.approveClaimChip.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_nav_claim_management_to_nav_approve_claim));
    }

    private void performAction(boolean isSubmit) {
        // 1. Manually pull inputs from View Binding components and copy them into the ViewModel tracking states
        viewModel.projectName.setValue(binding.projectName.getText() != null ? binding.projectName.getText().toString() : "");
        viewModel.purposeOfMeeting.setValue(binding.purposeInput.getText() != null ? binding.purposeInput.getText().toString() : "");
        viewModel.totalAmount.setValue(binding.amountInput.getText() != null ? binding.amountInput.getText().toString() : "");

        // 2. Map other form fields so they don't get sent as blank/null in your API payload configuration
        viewModel.startLocation.setValue(binding.startLocationInput.getText() != null ? binding.startLocationInput.getText().toString() : "");
        viewModel.endLocation.setValue(binding.endLocationInput.getText() != null ? binding.endLocationInput.getText().toString() : "");
        viewModel.remarks.setValue(binding.remarksInput.getText() != null ? binding.remarksInput.getText().toString() : "");
        viewModel.customTransportInput.setValue(binding.EnterTransportInput.getText() != null ? binding.EnterTransportInput.getText().toString() : "");

        // 3. Now run your validation execution loop safely as before
        viewModel.validateAndExecute(isSubmit, binding.meetingTypeDropdown.getSelectedItem().toString(), binding.claimCategoryDropdown.getSelectedItem().toString(), binding.travelCategoryDropdown.getSelectedItem().toString(), binding.transportModeDropdown.getSelectedItem().toString(), binding.transportModeShared.getSelectedItem().toString(), binding.transportModeDedicated.getSelectedItem().toString(), binding.currencyDropdown.getText().toString());
    }

    private void setupDropdownObservers() {
        viewModel.meetingTypes.observe(getViewLifecycleOwner(), list -> binding.meetingTypeDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, list)));
        viewModel.claimCategories.observe(getViewLifecycleOwner(), list -> binding.claimCategoryDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, list)));
        viewModel.travelCategories.observe(getViewLifecycleOwner(), list -> binding.travelCategoryDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, list)));
        viewModel.transportModes.observe(getViewLifecycleOwner(), list -> binding.transportModeDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, list)));
        viewModel.sharedTransportModes.observe(getViewLifecycleOwner(), list -> binding.transportModeShared.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, list)));
        viewModel.dedicatedTransportModes.observe(getViewLifecycleOwner(), list -> binding.transportModeDedicated.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, list)));
        viewModel.currencyDropdown.observe(getViewLifecycleOwner(), list -> binding.currencyDropdown.setAdapter(new CurrencyArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, list)));
    }

    private void setupLayoutVisibilityLogic() {
        // Safe execution checks via Selection Listener
        binding.claimCategoryDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String selected = p.getItemAtPosition(pos).toString();
                if ("Standard".equals(selected)) {
                    Toast.makeText(requireContext(), "Standard category is not available.", Toast.LENGTH_SHORT).show();
                    binding.claimCategoryDropdown.setSelection(0);
                }
                viewModel.isTravelSelected.setValue("Travel".equals(binding.claimCategoryDropdown.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });

        binding.transportModeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String mode = p.getItemAtPosition(pos).toString();
                viewModel.transportLayoutType.setValue("Shared".equals(mode) ? 1 : ("Dedicated".equals(mode) ? 2 : 0));
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });

        AdapterView.OnItemSelectedListener othersListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                viewModel.showCustomTransportInput.setValue("Others".equals(p.getItemAtPosition(pos).toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        };
        binding.transportModeShared.setOnItemSelectedListener(othersListener);
        binding.transportModeDedicated.setOnItemSelectedListener(othersListener);

        binding.expenseTypeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            StringBuilder sb = new StringBuilder();
            boolean travelSelected = false;
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    String text = chip.getText().toString();
                    if ("Travel".equals(text)) travelSelected = true;
                    else sb.append(text).append(" - Under Process\n");
                }
            }
            viewModel.underProcessTextState.setValue(sb.toString().trim());
        });
    }

    private void processPickedFiles(Intent data) {
        if (uploadedCount >= 10) {
            Toast.makeText(requireContext(), "Maximum limit of 10 files reached.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount() && uploadedCount < 10; i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                if (validateAndDisplayFile(uri)) {
                    viewModel.uploadFileAtIndex(uri, uploadedCount++);
                }
            }
        } else if (data.getData() != null) {
            Uri uri = data.getData();
            if (validateAndDisplayFile(uri)) {
                viewModel.uploadFileAtIndex(uri, uploadedCount++);
            }
        }

        if (uploadedCount > 0) {
            binding.selectedFileText.setVisibility(View.VISIBLE);
            binding.selectedFileText.setText("Selected " + uploadedCount + " files:");
        }
    }

    private boolean validateAndDisplayFile(Uri uri) {
        long fileSize = 0;
        String name = "Unknown File";
        try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE));
                name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            return false;
        }

        if (fileSize > 1024 * 1024) {
            Toast.makeText(requireContext(), "File size exceeds 1MB: " + name, Toast.LENGTH_LONG).show();
            return false;
        }

        TextView tv = new TextView(requireContext());
        tv.setText(name);
        binding.selectedFilesContainer.addView(tv);
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}