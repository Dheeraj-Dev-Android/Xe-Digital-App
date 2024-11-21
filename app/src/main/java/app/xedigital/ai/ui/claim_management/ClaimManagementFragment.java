package app.xedigital.ai.ui.claim_management;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.FragmentClaimManagementBinding;

public class ClaimManagementFragment extends Fragment {

    private static final long MAX_FILE_SIZE_BYTES = 1024 * 1024;
    private FragmentClaimManagementBinding binding;
    private ClaimManagementViewModel claimManagementViewModel;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private List<Uri> selectedFiles = new ArrayList<>();
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private LinearLayout selectedFilesContainer;
    private TextView underProcessText;
    private ChipGroup expenseTypeChipGroup;
    private TextInputLayout travelCategoryDropdownLayout;
    private MaterialCardView transportModeDropdownLayout;
    private LinearLayout locationDetailsContainer;

    public static ClaimManagementFragment newInstance() {
        return new ClaimManagementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        claimManagementViewModel = new ViewModelProvider(this).get(ClaimManagementViewModel.class);
        binding = FragmentClaimManagementBinding.inflate(inflater, container, false);

        initializeCalendar();
        setupDropdowns();
        setupDatePicker();
        setupButtons();

        underProcessText = binding.underProcessText;
        expenseTypeChipGroup = binding.expenseTypeChipGroup;
        travelCategoryDropdownLayout = binding.travelCategoryDropdownLayout;
        transportModeDropdownLayout = binding.transportModeDropdownLayout;
        locationDetailsContainer = binding.locationDetailsContainer;
        selectedFilesContainer = binding.selectedFilesContainer;

        travelCategoryDropdownLayout.setVisibility(View.GONE);
        transportModeDropdownLayout.setVisibility(View.GONE);
        locationDetailsContainer.setVisibility(View.GONE);

        binding.transportModeSharedLayout.setVisibility(View.GONE);
        binding.transportModeDedicatedLayout.setVisibility(View.GONE);
        binding.EnterTransportInputLayout.setVisibility(View.GONE);


        // Set ChipGroup listener with deprecation suppression
        expenseTypeChipGroup.setOnCheckedStateChangeListener(((chipGroup, checkedIds) -> {
//                Chip selectedChip = chipGroup.findViewById(checkedId.get(0));
            Chip selectedChip = null;
            for (Integer checkedId : checkedIds) {
                selectedChip = chipGroup.findViewById(checkedId);
                if (selectedChip != null) break;
            }

            // Show/hide travel fields and Location Details
            boolean isTravelSelected = selectedChip != null && selectedChip.getId() == R.id.travelChip;
            travelCategoryDropdownLayout.setVisibility(isTravelSelected ? View.VISIBLE : View.GONE);
            transportModeDropdownLayout.setVisibility(isTravelSelected ? View.VISIBLE : View.GONE);
            locationDetailsContainer.setVisibility(isTravelSelected ? View.VISIBLE : View.GONE);

            // Show "Under Process" for specific chips
            if (selectedChip != null) {
                String chipText = selectedChip.getText().toString();

                // Check if any "Under Process" chips are selected
                boolean isUnderProcessChipSelected = chipText.equals("Food") || chipText.equals("Accommodation") || chipText.equals("Miscellaneous");
                // Show "Under Process" with multiple lines if a relevant chip is selected or Travel is selected along with an "Under Process" chip
                if (isUnderProcessChipSelected || (isTravelSelected && isAnyUnderProcessChipSelected(chipGroup))) {
                    underProcessText.setVisibility(View.VISIBLE);
                    // Build the multiline text
                    StringBuilder multilineText = new StringBuilder();
                    for (int i = 0; i < chipGroup.getChildCount(); i++) {
                        View child = chipGroup.getChildAt(i);
                        if (child instanceof Chip) {
                            Chip chip = (Chip) child;
                            if (chip.isChecked() && !chip.getText().toString().equals("Travel")) {
                                multilineText.append(chip.getText().toString()).append(" - Under Process\n");
                            }
                        }
                    }
                    underProcessText.setText(multilineText.toString().trim());
                } else {
                    underProcessText.setVisibility(View.GONE);
                }
            } else {
                underProcessText.setVisibility(View.GONE);
            }
        }));

        selectedFilesContainer = binding.selectedFilesContainer;
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    // Handle multiple files
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            Uri uri = item.getUri();
                            if (isValidFileSize(uri)) {
                                selectedFiles.add(uri);
                            } else {
                                Toast.makeText(requireContext(), "File size exceeds 1MB", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else if (data.getData() != null) { // Handle single file
                        Uri uri = data.getData();
                        if (isValidFileSize(uri)) {
                            selectedFiles.add(uri);
                        } else {
                            Toast.makeText(requireContext(), "File size exceeds 1MB", Toast.LENGTH_SHORT).show();
                        }
                    }
                    updateSelectedFileText();
                }
            }
        });

//        binding.transportModeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedMode = parent.getItemAtPosition(position).toString();
//                Log.e("Selected Mode", selectedMode);
//
//                if (selectedMode.equals("Shared")) {
//                    Log.d("Shared", "Shared");
//                    Toast.makeText(requireContext(), "Shared", Toast.LENGTH_SHORT).show();
//                    binding.transportModeSharedLayout.setVisibility(View.VISIBLE);
//                    binding.transportModeDedicatedLayout.setVisibility(View.GONE);
//                    binding.EnterTransportInputLayout.setVisibility(View.GONE);
//                } else if (selectedMode.equals("Dedicated")) {
//                    Log.d("Dedicated", "Dedicated");
//                    Toast.makeText(requireContext(), "Dedicated", Toast.LENGTH_SHORT).show();
//                    binding.transportModeSharedLayout.setVisibility(View.GONE);
//                    binding.transportModeDedicatedLayout.setVisibility(View.VISIBLE);
//                    binding.EnterTransportInputLayout.setVisibility(View.GONE);
//                } else {
//                    binding.transportModeSharedLayout.setVisibility(View.GONE);
//                    binding.transportModeDedicatedLayout.setVisibility(View.GONE);
//                    binding.EnterTransportInputLayout.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Handle case when nothing is selected if needed
//                binding.transportModeSharedLayout.setVisibility(View.GONE);
//                binding.transportModeDedicatedLayout.setVisibility(View.GONE);
//                binding.EnterTransportInputLayout.setVisibility(View.GONE);
//            }
//        });

        binding.transportModeDropdown.post(() -> binding.transportModeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMode = parent.getItemAtPosition(position).toString();
                Log.e("Selected Mode", selectedMode);
                if (selectedMode.equals("Select Mode")) {
                    return;
                }

                if (selectedMode.equals("Shared")) {
                    Log.d("Shared", "Shared");
                    Toast.makeText(requireContext(), "Shared", Toast.LENGTH_SHORT).show();
                    binding.transportModeSharedLayout.setVisibility(View.VISIBLE);
                    binding.transportModeDedicatedLayout.setVisibility(View.GONE);
                    binding.EnterTransportInputLayout.setVisibility(View.GONE);
                } else if (selectedMode.equals("Dedicated")) {
                    Log.d("Dedicated", "Dedicated");
                    Toast.makeText(requireContext(), "Dedicated", Toast.LENGTH_SHORT).show();
                    binding.transportModeSharedLayout.setVisibility(View.GONE);
                    binding.transportModeDedicatedLayout.setVisibility(View.VISIBLE);
                    binding.EnterTransportInputLayout.setVisibility(View.GONE);
                } else {
                    binding.transportModeSharedLayout.setVisibility(View.GONE);
                    binding.transportModeDedicatedLayout.setVisibility(View.GONE);
                    binding.EnterTransportInputLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected if needed
                binding.transportModeSharedLayout.setVisibility(View.GONE);
                binding.transportModeDedicatedLayout.setVisibility(View.GONE);
                binding.EnterTransportInputLayout.setVisibility(View.GONE);
            }
        }));

        binding.transportModeShared.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMode = parent.getItemAtPosition(position).toString();
                if (selectedMode.equals("Others")) {
                    binding.EnterTransportInputLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.EnterTransportInputLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.EnterTransportInputLayout.setVisibility(View.GONE);
            }
        });

        binding.transportModeDedicated.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMode = parent.getItemAtPosition(position).toString();
                if (selectedMode.equals("Others")) {
                    binding.EnterTransportInputLayout.setVisibility(View.VISIBLE);
                } else {
                    binding.EnterTransportInputLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.EnterTransportInputLayout.setVisibility(View.GONE);
            }
        });

        return binding.getRoot();
    }

    // Helper method to check if any "Under Process" chips are selected
    private boolean isAnyUnderProcessChipSelected(ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked() && (chip.getText().toString().equals("Food") || chip.getText().toString().equals("Accommodation") || chip.getText().toString().equals("Miscellaneous"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initializeCalendar() {
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    }


    private void setupDropdowns() {
        // Meeting Type Dropdown
        String[] meetingTypes = {"Business", "Project", "Pre Sales"};
        ArrayAdapter<String> meetingAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, meetingTypes);
        binding.meetingTypeDropdown.setAdapter(meetingAdapter);

        //Claim Category Dropdown
        String[] claimCategories = {"General", "Standard"};
        ArrayAdapter<String> claimCategoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, claimCategories);
        binding.claimCategoryDropdown.setAdapter(claimCategoryAdapter);
        // Travel Category Dropdown
        String[] travelCategories = {"Local", "Domestic", "International"};
        ArrayAdapter<String> travelCategoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, travelCategories);
        binding.travelCategoryDropdown.setAdapter(travelCategoryAdapter);
        // Transport Mode Dropdown
        String[] transportModes = {"Select Mode", "Shared", "Dedicated"};
        ArrayAdapter<String> transportAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, transportModes);
        binding.transportModeDropdown.setAdapter(transportAdapter);
        binding.transportModeDropdown.setSelection(0);
        // Shared Transport Mode Dropdown
        String[] shared = {"Auto", "Car", "E-Rickshaw", "Metro", "Others"};
        ArrayAdapter<String> sharedAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, shared);
        binding.transportModeShared.setAdapter(sharedAdapter);
        // Dedicated Transport Mode Dropdown
        String[] dedicated = {"Two-Wheeler", "Three-Wheeler", "Others"};
        ArrayAdapter<String> dedicatedAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, dedicated);
        binding.transportModeDedicated.setAdapter(dedicatedAdapter);
        // Currency Dropdown
        String[] currencies = {"INR", "USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF", "HKD", "SEK", "NZD"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies);
        binding.currencyDropdown.setAdapter(currencyAdapter);
    }

    private void setupDatePicker() {
        binding.claimDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.claimDateInput.setText(dateFormatter.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*"); // Only allow images
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"}); // Filter to JPG and PNG
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(intent);
    }

    private void updateSelectedFileText() {
        selectedFilesContainer.removeAllViews(); // Clear previous file names

        for (Uri uri : selectedFiles) {
            String fileName = getFileNameFromUri(uri);
            TextView textView = new TextView(requireContext());
            textView.setText(fileName);
            selectedFilesContainer.addView(textView);
        }

        if (selectedFiles.isEmpty()) {
            binding.selectedFileText.setVisibility(View.GONE); // Hide "Selected files" text if no files selected
        } else {
            binding.selectedFileText.setVisibility(View.VISIBLE);
            binding.selectedFileText.setText("Selected " + selectedFiles.size() + " files:");
        }
    }

    // Helper method to get file name from URI
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Use getColumnIndexOrThrow() to avoid potential errors
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    // Method to validate file size
    private boolean isValidFileSize(Uri fileUri) {
        try (Cursor cursor = requireContext().getContentResolver().query(fileUri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // Use getColumnIndexOrThrow() to avoid potential errors
                int sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);
                if (!cursor.isNull(sizeIndex)) {
                    long fileSize = cursor.getLong(sizeIndex);
                    return fileSize <= MAX_FILE_SIZE_BYTES;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Assume invalid if size cannot be determined
    }

    private void setupButtons() {
        binding.uploadButton.setOnClickListener(v -> {
            openFilePicker();
            // Implement file upload logic
            Toast.makeText(requireContext(), "Select Files to Upload", Toast.LENGTH_SHORT).show();
        });

        binding.saveButton.setOnClickListener(v -> {
            // Implement save logic
            if (validateForm()) {
                saveClaimData();
                Toast.makeText(requireContext(), "Claim saved successfully", Toast.LENGTH_SHORT).show();
            }
        });

        binding.submitButton.setOnClickListener(v -> {
            // Implement submit logic
            if (validateForm()) {
                submitClaimData();
                Toast.makeText(requireContext(), "Claim submitted successfully", Toast.LENGTH_SHORT).show();
            }
        });
//        binding.transportModeDropdown.setOnClickListener(v -> {
//            Log.d("DropdownClick", "transportModeDropdown clicked");
//            Toast.makeText(requireContext(), "Dropdown Clicked", Toast.LENGTH_SHORT).show();
//        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate Claim Date
        if (binding.claimDateInput.getText().toString().trim().isEmpty()) {
            binding.claimDateInput.setError("Please select claim date");
            isValid = false;
        }

        // Validate Project Name
        if (binding.projectName.getText().toString().trim().isEmpty()) {
            binding.projectName.setError("Please enter project name");
            isValid = false;
        }

        // Validate Meeting Type
        if (binding.meetingTypeDropdown.getText().toString().trim().isEmpty()) {
            binding.meetingTypeDropdown.setError("Please select meeting type");
            isValid = false;
        }

        // Validate Purpose
        if (binding.purposeInput.getText().toString().trim().isEmpty()) {
            binding.purposeInput.setError("Please enter purpose of meeting");
            isValid = false;
        }

        // Validate Amount
        String amount = binding.amountInput.getText().toString().trim();
        if (amount.isEmpty()) {
            binding.amountInput.setError("Please enter amount");
            isValid = false;
        } else {
            try {
                double amountValue = Double.parseDouble(amount);
                if (amountValue <= 0) {
                    binding.amountInput.setError("Amount must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.amountInput.setError("Please enter valid amount");
                isValid = false;
            }
        }

        return isValid;
    }

    private void saveClaimData() {
        Toast.makeText(requireContext(), "Claim saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void submitClaimData() {
        Toast.makeText(requireContext(), "Claim submitted successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}