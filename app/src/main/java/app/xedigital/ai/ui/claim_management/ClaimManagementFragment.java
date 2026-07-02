package app.xedigital.ai.ui.claim_management;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    // Track uploaded URIs locally to handle flexible deletions easily
    private final List<Uri> selectedFileUris = new ArrayList<>();

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

        setupPickersAndButtons();
        setupDropdownObservers();
        setupLayoutVisibilityLogic();

        viewModel.toastMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.operationSuccess.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                viewModel.clearFormFields();
                binding.selectedFilesContainer.removeAllViews();
                binding.selectedFileText.setVisibility(View.GONE);
                binding.forGuestEmployeesCheckbox.setChecked(false);
                selectedFileUris.clear();
            }
        });

        return binding.getRoot();
    }

    private void setupPickersAndButtons() {
        binding.claimDateInput.setOnClickListener(v -> showDatePicker(binding.claimDateInput, true));

        binding.accommodationCheckInDate.setOnClickListener(v -> showDatePicker(binding.accommodationCheckInDate, false));
        binding.accommodationCheckOutDate.setOnClickListener(v -> showDatePicker(binding.accommodationCheckOutDate, false));
        binding.internetBillingPeriodInput.setOnClickListener(v -> showDatePicker(binding.internetBillingPeriodInput, false));
        binding.parkingDateInput.setOnClickListener(v -> showDatePicker(binding.parkingDateInput, false));

        binding.uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // Accept images and PDF
            intent.setType("*/*");
            String[] mimeTypes = {"image/jpeg", "application/pdf"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            filePickerLauncher.launch(intent);
        });

        binding.saveButton.setOnClickListener(v -> performAction(false));
        binding.submitButton.setOnClickListener(v -> performAction(true));

        binding.viewClaimChip.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_nav_claim_management_to_nav_view_claim));
        binding.approveClaimChip.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_nav_claim_management_to_nav_approve_claim));
    }

    private void showDatePicker(EditText targetEditText, boolean updateViewModel) {
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            String formattedDate = dateFormatter.format(calendar.getTime());
            if (updateViewModel) {
                viewModel.claimDate.setValue(formattedDate);
            }
            targetEditText.setText(formattedDate);

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void performAction(boolean isSubmit) {
        viewModel.projectName.setValue(binding.projectName.getText() != null ? binding.projectName.getText().toString() : "");
        viewModel.purposeOfMeeting.setValue(binding.purposeInput.getText() != null ? binding.purposeInput.getText().toString() : "");
        viewModel.totalAmount.setValue(binding.amountInput.getText() != null ? binding.amountInput.getText().toString() : "");

        viewModel.startLocation.setValue(binding.startLocationInput.getText() != null ? binding.startLocationInput.getText().toString() : "");
        viewModel.endLocation.setValue(binding.endLocationInput.getText() != null ? binding.endLocationInput.getText().toString() : "");
        viewModel.remarks.setValue(binding.remarksInput.getText() != null ? binding.remarksInput.getText().toString() : "");
        viewModel.customTransportInput.setValue(binding.EnterTransportInput.getText() != null ? binding.EnterTransportInput.getText().toString() : "");

        String travelCategory = binding.travelCategoryDropdown.getSelectedItem() != null ? binding.travelCategoryDropdown.getSelectedItem().toString() : "";
        String transportMode = binding.transportModeDropdown.getSelectedItem() != null ? binding.transportModeDropdown.getSelectedItem().toString() : "";
        String transportShared = binding.transportModeShared.getSelectedItem() != null ? binding.transportModeShared.getSelectedItem().toString() : "";
        String transportDedicated = binding.transportModeDedicated.getSelectedItem() != null ? binding.transportModeDedicated.getSelectedItem().toString() : "";

        viewModel.validateAndExecute(isSubmit,
                binding.meetingTypeDropdown.getSelectedItem().toString(),
                binding.claimCategoryDropdown.getSelectedItem().toString(),
                travelCategory,
                transportMode,
                transportShared,
                transportDedicated,
                binding.currencyDropdown.getText().toString());
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
        binding.forGuestEmployeesCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.guestEmployeesFieldsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        String[] businessUnits = {"Please Select", "BU 1", "BU 2", "BU 3"};
        binding.businessUnitDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, businessUnits));

        String[] employeesList = {"Please Select", "Employee A", "Employee B", "Employee C"};
        binding.requestedEmployeeDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, employeesList));

        String[] mealTypes = {"Please Select", "Dinner", "Breakfast", "lunch", "snacks", "refreshments", "late night meals", "Others"};
        binding.mealTypeDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, mealTypes));

        String[] accommodationTypes = {"Please Select", "Hotel", "Guest House", "Rental", "Company Accommodation"};
        binding.accommodationTypeDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, accommodationTypes));

        String[] miscCategories = {"Please Select", "Internet Expense", "Fuel Expense", "Parking Expense", "Toll Expense"};
        binding.miscExpenseCategoryDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, miscCategories));

        String[] fuelTypes = {"Please Select", "Petrol", "Diesel"};
        binding.fuelTypeDropdown.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, fuelTypes));

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

        // Handles Chip Selection & explicit unselection states safely
//        binding.expenseTypeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
//            StringBuilder sb = new StringBuilder();
//            boolean travelChecked = false;
//            boolean foodChecked = false;
//            boolean accommodationChecked = false;
//            boolean miscChecked = false;
//
//            for (int id : checkedIds) {
//                Chip chip = group.findViewById(id);
//                if (chip != null) {
//                    String text = chip.getText().toString();
//                    sb.append(text).append(" - Expense Type\n");
//
//                    if ("Travel".equals(text)) travelChecked = true;
//                    if ("Food".equals(text)) foodChecked = true;
//                    if ("Accommodation".equals(text)) accommodationChecked = true;
//                    if ("Miscellaneous".equals(text)) miscChecked = true;
//                }
//            }
//
//            if (checkedIds.isEmpty()) {
//                binding.underProcessText.setVisibility(View.GONE);
//            } else {
//                binding.underProcessText.setVisibility(View.VISIBLE);
//                viewModel.underProcessTextState.setValue(sb.toString().trim());
//            }
//
//            // Exclusively toggle views. If unselected, it falls back to View.GONE gracefully
//            binding.travelFieldsContainer.setVisibility(travelChecked ? View.VISIBLE : View.GONE);
//            binding.foodFieldsContainer.setVisibility(foodChecked ? View.VISIBLE : View.GONE);
//            binding.accommodationFieldsContainer.setVisibility(accommodationChecked ? View.VISIBLE : View.GONE);
//            binding.miscellaneousFieldsContainer.setVisibility(miscChecked ? View.VISIBLE : View.GONE);
//        });

        binding.expenseTypeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            StringBuilder sb = new StringBuilder();
            boolean travelChecked = false;
            boolean foodChecked = false;
            boolean accommodationChecked = false;
            boolean miscChecked = false;

            // Get the single checked ID since app:singleSelection="true" is configured in XML
            int checkedChipId = checkedIds.isEmpty() ? View.NO_ID : checkedIds.get(0);

            // Loop through all chips inside the group to apply correct unique state backgrounds
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;

                    chip.setBackgroundTintList(null);

                    // Match against the single checked ID
                    if (chip.getId() == checkedChipId) {
                        // Checked chip style
                        chip.setChipBackgroundColorResource(R.color.avatar_text);
                        chip.setTextColor(getResources().getColor(R.color.approved_color));

                        String text = chip.getText().toString();
                        sb.append(text).append(" - Expense Type\n");

                        if ("Travel".equals(text)) travelChecked = true;
                        if ("Food".equals(text)) foodChecked = true;
                        if ("Accommodation".equals(text)) accommodationChecked = true;
                        if ("Miscellaneous".equals(text)) miscChecked = true;
                    } else {
                        // Revert all unselected chips to original defaults
                        chip.setChipBackgroundColorResource(android.R.color.white);
                        chip.setTextColor(getResources().getColor(R.color.colorPrimary, requireContext().getTheme()));
                    }
                }
            }

            if (checkedIds.isEmpty()) {
                binding.underProcessText.setVisibility(View.GONE);
            } else {
                binding.underProcessText.setVisibility(View.VISIBLE);
                viewModel.underProcessTextState.setValue(sb.toString().trim());
            }

            binding.travelFieldsContainer.setVisibility(travelChecked ? View.VISIBLE : View.GONE);
            binding.foodFieldsContainer.setVisibility(foodChecked ? View.VISIBLE : View.GONE);
            binding.accommodationFieldsContainer.setVisibility(accommodationChecked ? View.VISIBLE : View.GONE);
            binding.miscellaneousFieldsContainer.setVisibility(miscChecked ? View.VISIBLE : View.GONE);
        });

        binding.miscExpenseCategoryDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = parent.getItemAtPosition(position).toString();
                binding.internetExpenseFieldsContainer.setVisibility("Internet Expense".equals(selection) ? View.VISIBLE : View.GONE);
                binding.fuelExpenseFieldsContainer.setVisibility("Fuel Expense".equals(selection) ? View.VISIBLE : View.GONE);
                binding.parkingExpenseFieldsContainer.setVisibility("Parking Expense".equals(selection) ? View.VISIBLE : View.GONE);
                binding.tollChargesFieldsContainer.setVisibility("Toll Expense".equals(selection) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        viewModel.transportLayoutType.observe(getViewLifecycleOwner(), type -> {
            binding.transportModeSharedLayout.setVisibility(type == 1 ? View.VISIBLE : View.GONE);
            binding.transportModeDedicatedLayout.setVisibility(type == 2 ? View.VISIBLE : View.GONE);
        });

        viewModel.showCustomTransportInput.observe(getViewLifecycleOwner(), show ->
                binding.EnterTransportInputLayout.setVisibility(show ? View.VISIBLE : View.GONE));

        viewModel.underProcessTextState.observe(getViewLifecycleOwner(), text -> {
            binding.underProcessText.setText(text);
        });
    }

    private void processPickedFiles(Intent data) {
        if (selectedFileUris.size() >= 10) {
            Toast.makeText(requireContext(), "Maximum limit of 10 files reached.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount() && selectedFileUris.size() < 10; i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                validateAndProcessFile(uri);
            }
        } else if (data.getData() != null) {
            Uri uri = data.getData();
            validateAndProcessFile(uri);
        }

        updateFileCountHeader();
    }

    private void validateAndProcessFile(Uri uri) {
        long fileSize = 0;
        String name = "Unknown File";

        try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE));
                name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            return;
        }

        // 1MB Size Check Alert Dialog Requirement
        if (fileSize > 1024 * 1024) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("File Too Large")
                    .setMessage("The file \"" + name + "\" exceeds the 1MB limit and will not be uploaded.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        selectedFileUris.add(uri);
        int listIndex = selectedFileUris.size() - 1;

        // Pass index reference to the ViewModel file processor
        viewModel.uploadFileAtIndex(uri, listIndex);

        // Render dynamic removable layout element
        addFileViewToContainer(uri, name);
    }

    private void addFileViewToContainer(Uri uri, String filename) {
        LinearLayout rowLayout = new LinearLayout(requireContext());
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setPadding(0, 8, 0, 8);

        TextView tv = new TextView(requireContext());
        tv.setText(filename);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(textParams);

        TextView removeButton = new TextView(requireContext());
        removeButton.setText(" [Remove]");
        removeButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        removeButton.setPadding(16, 0, 16, 0);

        removeButton.setOnClickListener(v -> {
            int index = selectedFileUris.indexOf(uri);
            if (index != -1) {
                selectedFileUris.remove(index);
                // Note: If your ViewModel retains indexed state mapping, verify your viewmodel API context handling here
                binding.selectedFilesContainer.removeView(rowLayout);
                updateFileCountHeader();
            }
        });

        rowLayout.addView(tv);
        rowLayout.addView(removeButton);
        binding.selectedFilesContainer.addView(rowLayout);
    }

    private void updateFileCountHeader() {
        if (!selectedFileUris.isEmpty()) {
            binding.selectedFileText.setVisibility(View.VISIBLE);
            binding.selectedFileText.setText("Selected " + selectedFileUris.size() + " files:");
        } else {
            binding.selectedFileText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}