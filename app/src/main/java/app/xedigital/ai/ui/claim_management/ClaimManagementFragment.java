package app.xedigital.ai.ui.claim_management;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.CurrencyArrayAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentClaimManagementBinding;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private TextInputLayout transportModeDropdownLayout;
    private LinearLayout locationDetailsContainer;
    //    private void updateSelectedFileText() {
//        selectedFilesContainer.removeAllViews();
//
//        for (Uri uri : selectedFiles) {
//            String fileName = getFileNameFromUri(uri);
//            TextView textView = new TextView(requireContext());
//            textView.setText(fileName);
//            selectedFilesContainer.addView(textView);
//        }
//
//        if (selectedFiles.isEmpty()) {
//            binding.selectedFileText.setVisibility(View.GONE);
//        } else {
//            binding.selectedFileText.setVisibility(View.VISIBLE);
//            binding.selectedFileText.setText("Selected " + selectedFiles.size() + " files:");
//        }
//    }
    private final List<Uri> processedFiles = new ArrayList<>();
    private String authToken;

    public static ClaimManagementFragment newInstance() {
        return new ClaimManagementFragment();
    }
    private String authTokenHeader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        authTokenHeader = "jwt " + authToken;

        claimManagementViewModel = new ViewModelProvider(this).get(ClaimManagementViewModel.class);
        claimManagementViewModel.getClaimLength(authToken);
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
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(intent);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        claimManagementViewModel = new ViewModelProvider(this).get(ClaimManagementViewModel.class);
        binding = FragmentClaimManagementBinding.inflate(inflater, container, false);

        initializeCalendar();
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

        // Observe dropdown data from ViewModel
        claimManagementViewModel.getMeetingTypes().observe(getViewLifecycleOwner(), meetingTypes -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, meetingTypes);
            binding.meetingTypeDropdown.setAdapter(adapter);
        });

        claimManagementViewModel.getClaimCategories().observe(getViewLifecycleOwner(), claimCategories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, claimCategories);
            binding.claimCategoryDropdown.setAdapter(adapter);
        });

        claimManagementViewModel.getTravelCategories().observe(getViewLifecycleOwner(), travelCategories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, travelCategories);
            binding.travelCategoryDropdown.setAdapter(adapter);
        });

        claimManagementViewModel.getTransportModes().observe(getViewLifecycleOwner(), transportModes -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, transportModes);
            binding.transportModeDropdown.setAdapter(adapter);
        });

        claimManagementViewModel.getSharedTransportModes().observe(getViewLifecycleOwner(), sharedTransportModes -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sharedTransportModes);
            binding.transportModeShared.setAdapter(adapter);
        });

        claimManagementViewModel.getDedicatedTransportModes().observe(getViewLifecycleOwner(), dedicatedTransportModes -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, dedicatedTransportModes);
            binding.transportModeDedicated.setAdapter(adapter);
        });

        claimManagementViewModel.getCurrencyDropdown().observe(getViewLifecycleOwner(), currencies -> {
            CurrencyArrayAdapter currencyAdapter = new CurrencyArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies);
            binding.currencyDropdown.setAdapter(currencyAdapter);
        });

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
//        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == Activity.RESULT_OK) {
//                Intent data = result.getData();
//                if (data != null) {
//                    // Handle multiple files
//                    if (data.getClipData() != null) {
//                        ClipData clipData = data.getClipData();
//                        for (int i = 0; i < clipData.getItemCount(); i++) {
//                            ClipData.Item item = clipData.getItemAt(i);
//                            Uri uri = item.getUri();
//                            if (isValidFileSize(uri)) {
//                                selectedFiles.add(uri);
//                            } else {
//                                Toast.makeText(requireContext(), "File size exceeds 1MB", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    } else if (data.getData() != null) {
//                        Uri uri = data.getData();
//                        if (isValidFileSize(uri)) {
//                            selectedFiles.add(uri);
//                        } else {
//                            Toast.makeText(requireContext(), "File size exceeds 1MB", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    updateSelectedFileText();
//                }
//            }
//        });
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
                                // Throw an error or display an error message
                                Toast.makeText(requireContext(), "File size exceeds 1MB: " + uri.toString(), Toast.LENGTH_LONG).show();
                                throw new IllegalStateException("File size exceeds 1MB: " + uri.toString());
                                // or
                                // Toast.makeText(requireContext(), "File size exceeds 1MB: " + uri.toString(), Toast.LENGTH_LONG).show();
                                // return; // Stop further processing
                            }
                        }
                    } else if (data.getData() != null) {
                        Uri uri = data.getData();
                        if (isValidFileSize(uri)) {
                            selectedFiles.add(uri);
                        } else {
                            // Throw an error or display an error message
                            Toast.makeText(requireContext(), "File size exceeds 1MB: " + uri.toString(), Toast.LENGTH_LONG).show();
                            throw new IllegalStateException("File size exceeds 1MB: " + uri.toString());
                            // or

                            // return; // Stop further processing
                        }
                    }
                    updateSelectedFileText();
                }
            }
        });

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

                    binding.transportModeSharedLayout.post(() -> {
                        binding.transportModeSharedLayout.requestLayout();
                        binding.transportModeSharedLayout.invalidate();
                    });
                } else if (selectedMode.equals("Dedicated")) {
                    Log.d("Dedicated", "Dedicated");
                    Toast.makeText(requireContext(), "Dedicated", Toast.LENGTH_SHORT).show();
                    binding.transportModeSharedLayout.setVisibility(View.GONE);
                    binding.transportModeDedicatedLayout.setVisibility(View.VISIBLE);
                    binding.EnterTransportInputLayout.setVisibility(View.GONE);

                    binding.transportModeSharedLayout.post(() -> {
                        binding.transportModeSharedLayout.requestLayout();
                        binding.transportModeSharedLayout.invalidate();
                    });
                } else {
                    binding.transportModeSharedLayout.setVisibility(View.GONE);
                    binding.transportModeDedicatedLayout.setVisibility(View.GONE);
                    binding.EnterTransportInputLayout.setVisibility(View.GONE);

                    binding.transportModeSharedLayout.post(() -> {
                        binding.transportModeSharedLayout.requestLayout();
                        binding.transportModeSharedLayout.invalidate();
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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

    private void updateSelectedFileText() {
        selectedFilesContainer.removeAllViews();

        // Declare Uri variables for up to 10 files
        Uri file1Uri = null, file2Uri = null, file3Uri = null, file4Uri = null, file5Uri = null, file6Uri = null, file7Uri = null, file8Uri = null, file9Uri = null, file10Uri = null;

        // Extract URIs from selectedFiles list and handle them individually
        int fileCount = selectedFiles.size();

        if (fileCount > 0) {
            file1Uri = selectedFiles.get(0);
            displayFileName(file1Uri);
//            logFileInfo(file1Uri);
            handleFile1(file1Uri);

        }
        if (fileCount > 1) {
            file2Uri = selectedFiles.get(1);
            displayFileName(file2Uri);
//            logFileInfo(file2Uri);
            handleFile2(file2Uri);
        }
        if (fileCount > 2) {
            file3Uri = selectedFiles.get(2);
            displayFileName(file3Uri);
            handleFile3(file3Uri);
//            logFileInfo(file3Uri);// Handle file3Uri
        }
        if (fileCount > 3) {
            file4Uri = selectedFiles.get(3);
            displayFileName(file4Uri);
            handleFile4(file4Uri);
//            logFileInfo(file4Uri);// Handle file4Uri
        }
        if (fileCount > 4) {
            file5Uri = selectedFiles.get(4);
            displayFileName(file5Uri);
            handleFile5(file5Uri);
//            logFileInfo(file5Uri);// Handle file5Uri
        }
        if (fileCount > 5) {
            file6Uri = selectedFiles.get(5);
            displayFileName(file6Uri);
            handleFile6(file6Uri);
//            logFileInfo(file6Uri);// Handle file6Uri
        }
        if (fileCount > 6) {
            file7Uri = selectedFiles.get(6);
            displayFileName(file7Uri);
            handleFile7(file7Uri);
//            logFileInfo(file7Uri);// Handle file7Uri
        }
        if (fileCount > 7) {
            file8Uri = selectedFiles.get(7);
            displayFileName(file8Uri);
            handleFile8(file8Uri);
//            logFileInfo(file8Uri);// Handle file8Uri
        }
        if (fileCount > 8) {
            file9Uri = selectedFiles.get(8);
            displayFileName(file9Uri);
            handleFile9(file9Uri);
//            logFileInfo(file9Uri);// Handle file9Uri
        }
        if (fileCount > 9) {
            file10Uri = selectedFiles.get(9);
            displayFileName(file10Uri);
            handleFile10(file10Uri);
//            logFileInfo(file10Uri);
        }

        if (selectedFiles.isEmpty()) {
            binding.selectedFileText.setVisibility(View.GONE);
        } else {
            binding.selectedFileText.setVisibility(View.VISIBLE);
            binding.selectedFileText.setText("Selected " + selectedFiles.size() + " files:");
        }
    }

    private void handleFile1(Uri file1Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file1Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("image", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile1 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile1 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile1 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile1 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile1 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void handleFile2(Uri file2Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file2Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageOne", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage1(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile2 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile2 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile2 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile2 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile2 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void handleFile3(Uri file3Uri) {

        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file3Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageTwo", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage2(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) { // Use try-with-resources
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile3 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile3 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile3 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile3 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile3 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void handleFile4(Uri file4Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file4Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageThree", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage3(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) { // Use try-with-resources
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile4 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile4 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile4 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile4 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile4 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    private void handleFile5(Uri file5Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file5Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageFour", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage4(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile5 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile5 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile5 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile5 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile5 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    private void handleFile6(Uri file6Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file6Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageFive", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage5(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile6 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile6 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile6 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile6 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile6 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void handleFile7(Uri file7Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file7Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageSix", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage6(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile7 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile7 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile7 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile7 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile7 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    private void handleFile8(Uri file8Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file8Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageSeven", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage7(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile8 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile8 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile8 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile8 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile8 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    private void handleFile9(Uri file9Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file9Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageEight", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage8(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile9 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile9 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile9 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile9 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("handleFile9 Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    private void handleFile10(Uri file10Uri) {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            String base64String = getBase64StringFromFile(file10Uri);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageNine", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = apiInterface.uploadImage9(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                Log.d("handleFile10 Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("handleFile10 Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("handleFile10 Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("handleFile10 Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("API Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (IOException | JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String getBase64StringFromFile(Uri fileUri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
        byte[] bytes = getBytes(inputStream);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Helper function to get bytes from InputStream
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void logFileInfo(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        String fileType = requireContext().getContentResolver().getType(uri);

        Log.d("FileInfo", "File Name: " + fileName + ", File Type: " + fileType + ", File URI: " + uri.toString());
    }

    // Helper method to display the file name
    private void displayFileName(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        TextView textView = new TextView(requireContext());
        textView.setText(fileName);
        selectedFilesContainer.addView(textView);
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
//    private boolean isValidFileSize(Uri fileUri) {
//        try (Cursor cursor = requireContext().getContentResolver().query(fileUri, null, null, null, null)) {
//            if (cursor != null && cursor.moveToFirst()) {
//                // Use getColumnIndexOrThrow() to avoid potential errors
//                int sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);
//                if (!cursor.isNull(sizeIndex)) {
//                    long fileSize = cursor.getLong(sizeIndex);
//                    return fileSize <= MAX_FILE_SIZE_BYTES;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
    // Helper function to check file size
    private boolean isValidFileSize(Uri uri) {
        try {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                long fileSize = cursor.getLong(sizeIndex);
                cursor.close();
                return fileSize <= 1024 * 1024; // 1MB in bytes
            }
        } catch (Exception e) {
            Log.e("FileSizeCheck", "Error checking file size: " + e.getMessage());
        }
        return false;
    }

    private void setupButtons() {
        binding.uploadButton.setOnClickListener(v -> {
            openFilePicker();
            Toast.makeText(requireContext(), "Select Files to Upload", Toast.LENGTH_SHORT).show();
        });

        binding.saveButton.setOnClickListener(v -> {
            if (validateForm()) {
                saveClaimData();
                Toast.makeText(requireContext(), "Claim saved successfully", Toast.LENGTH_SHORT).show();
            }
        });

        binding.submitButton.setOnClickListener(v -> {
            if (validateForm()) {
                submitClaimData();
                Toast.makeText(requireContext(), "Claim submitted successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean validateForm() {
        boolean isValid = true;
        if (binding.claimDateInput.getText().toString().trim().isEmpty()) {
            binding.claimDateInput.setError("Please enter claim date");
            isValid = false;
        } else {
            binding.claimDateInput.setError(null);
        }
        if (binding.projectName.getText().toString().trim().isEmpty()) {
            binding.projectName.setError("Please enter project name");
            isValid = false;
        } else {
            binding.projectName.setError(null);
        }
        if (binding.purposeInput.getText().toString().trim().isEmpty()) {
            binding.purposeInput.setError("Please enter purpose of meeting");
            isValid = false;
        } else {
            binding.purposeInput.setError(null);
        }
        if (binding.meetingTypeDropdown.getSelectedItemPosition() == 0) {
            binding.meetingTypeDropdownLayout.setError("Please select meeting type");
            isValid = false;
        } else {
            binding.meetingTypeDropdownLayout.setError(null);
        }

        if (binding.claimCategoryDropdown.getSelectedItemPosition() == 0) {
            binding.claimCategoryDropdownLayout.setError("Please select claim category");
            isValid = false;
        } else {
            binding.claimCategoryDropdownLayout.setError(null);
        }

        if (binding.claimCategoryDropdown.getSelectedItem().toString().equals("Travel") && binding.travelCategoryDropdown.getSelectedItemPosition() == 0) {
            binding.travelCategoryDropdownLayout.setError("Please select travel category");
            isValid = false;
        } else {
            binding.travelCategoryDropdownLayout.setError(null);
        }

        if (binding.travelCategoryDropdown.getSelectedItem().toString().equals("Local") && (binding.transportModeDropdown.getSelectedItemPosition() == 0)) {
            binding.transportModeDropdownLayout.setError("Please select transport mode");
            isValid = false;
        } else {
            binding.transportModeDropdownLayout.setError(null);
        }

        if (binding.transportModeDropdown.getSelectedItem().toString().equals("Shared") && (binding.transportModeShared.getSelectedItemPosition() == 0)) {
            binding.transportModeSharedLayout.setError("Please select transport mode Shared");
            isValid = false;
        } else {
            binding.transportModeSharedLayout.setError(null);
        }

        if (binding.transportModeDropdown.getSelectedItem().toString().equals("Dedicated") && (binding.transportModeDedicated.getSelectedItemPosition() == 0)) {
            binding.transportModeDedicatedLayout.setError("Please select transport mode dedicated");
            isValid = false;
        } else {
            binding.transportModeDedicatedLayout.setError(null);
        }
        // Validate Enter Transport Input
        if (Objects.requireNonNull(binding.EnterTransportInput.getText()).toString().trim().isEmpty()) {
            binding.EnterTransportInputLayout.setError("Please enter transport details");
            isValid = false;
        } else {
            binding.EnterTransportInputLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.startLocationInput.getText()).toString().trim().isEmpty()) {
            binding.startLocationInputLayout.setError("Please enter start location");
            isValid = false;
        } else {
            binding.startLocationInputLayout.setError(null);
        }
        if (Objects.requireNonNull(binding.endLocationInput.getText()).toString().trim().isEmpty()) {
            binding.endLocationInputLayout.setError("Please enter end location");
            isValid = false;
        } else {
            binding.endLocationInputLayout.setError(null);
        }
        // Validate Amount
        String amountString = Objects.requireNonNull(binding.amountInput.getText()).toString().trim();
        if (amountString.isEmpty()) {
            binding.amountInput.setError("Please enter the amount");
            isValid = false;
        } else if (!amountString.matches("^[0-9]+(?:\\.[0-9]{0,2})?$")) {
            binding.amountInput.setError("Invalid amount format for INR");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountString);
                if (amount <= 0) {
                    binding.amountInput.setError("Amount must be positive");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.amountInput.setError("Invalid amount format");
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