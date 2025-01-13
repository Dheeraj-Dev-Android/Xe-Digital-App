package app.xedigital.ai.ui.claim_management;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.ApproveClaimAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.approveClaim.ApproveClaimResponse;
import app.xedigital.ai.model.approveClaim.Data;
import app.xedigital.ai.model.approveClaim.EmployeeClaimdataItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApproveClaimFragment extends Fragment implements ApproveClaimAdapter.OnClaimClickListener {
    private final List<EmployeeClaimdataItem> originalClaimList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ApproveClaimAdapter approveClaimAdapter;
    private DatePickerDialog datePickerDialog;
    private ImageButton filterButton;
    private ProgressBar loadingProgress;
    private LinearLayout emptyStateContainer;
    private EditText fromDateEditText;
    private EditText toDateEditText;
    private APIInterface apiInterface;
    private List<EmployeeClaimdataItem> claimList = new ArrayList<>();

    public ApproveClaimFragment() {
        // Required empty public constructor
    }

    public static ApproveClaimFragment newInstance(String param1, String param2) {
        return new ApproveClaimFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = APIClient.getInstance().ApproveClaim();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_approve_claim, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.claimsApproveRecyclerView);
        filterButton = view.findViewById(R.id.filterButton);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        fromDateEditText = view.findViewById(R.id.fromDateEditText);
        toDateEditText = view.findViewById(R.id.toDateEditText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        approveClaimAdapter = new ApproveClaimAdapter(claimList);
        approveClaimAdapter.setOnClaimClickListener(this);
        recyclerView.setAdapter(approveClaimAdapter);

        fromDateEditText.setOnClickListener(v -> showDatePickerDialog(fromDateEditText));

        toDateEditText.setOnClickListener(v -> showDatePickerDialog(toDateEditText));

        filterButton.setOnClickListener(v -> {
            String fromDate = fromDateEditText.getText().toString();
            String toDate = toDateEditText.getText().toString();

            if (!fromDate.isEmpty() && !toDate.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date fromDateObj = dateFormat.parse(fromDate);
                    Date toDateObj = dateFormat.parse(toDate);

                    assert toDateObj != null;
                    if (toDateObj.before(fromDateObj)) {

                        Toast.makeText(requireContext(), "To date cannot be before from date", Toast.LENGTH_SHORT).show();
                    } else {
                        // Filter claims by date range
//                        filterClaimsByDateRange(fromDate, toDate);
                        new Handler().postDelayed(() -> {
                            loadingProgress.setVisibility(View.VISIBLE);
                            filterClaimsByDateRange(fromDate, toDate);
                        }, 300);
                    }
                } catch (androidx.core.net.ParseException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(requireContext(), "Please select both dates", Toast.LENGTH_SHORT).show();
            }
        });
        getClaimsForApproval();
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
            editText.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void getClaimsForApproval() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        Call<ApproveClaimResponse> call = apiInterface.getClaimsForApproval("jwt " + authToken);
        call.enqueue(new Callback<ApproveClaimResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApproveClaimResponse> call, @NonNull Response<ApproveClaimResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    ApproveClaimResponse approveClaimResponse = response.body();
                    assert approveClaimResponse != null;
                    Data data = approveClaimResponse.getData();
                    if (data != null && data.getEmployeeClaimdata() != null) {
                        claimList = data.getEmployeeClaimdata();
                        originalClaimList.clear();
                        originalClaimList.addAll(claimList);
                        if (claimList.isEmpty()) {
                            emptyStateContainer.setVisibility(View.VISIBLE);
                            Log.d("API Response", "No data available");
                        } else {
                            approveClaimAdapter.updateData(claimList);
                            emptyStateContainer.setVisibility(View.GONE);
//                            Log.d("API Response", "Data fetched successfully");
                        }
                        approveClaimAdapter = new ApproveClaimAdapter(approveClaimResponse.getData().getEmployeeClaimdata());
                        approveClaimAdapter.setOnClaimClickListener(ApproveClaimFragment.this);
                        recyclerView.setAdapter(approveClaimAdapter);
//                        Log.d("ApproveClaimFragment", "Response: " + approveClaimResponse);
                    } else {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                        Log.e("ApproveClaimFragment", "Data is null or empty in response");
                    }

                } else {
                    // Handle API error
                    Log.e("ApproveClaimFragment", "API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApproveClaimResponse> call, @NonNull Throwable t) {
                // Handle network or other exceptions
                loadingProgress.setVisibility(View.GONE);
                Log.e("ApproveClaimFragment", "Error: " + t.getMessage());
            }
        });
    }

    private void filterClaimsByDateRange(String fromDate, String toDate) {
        List<EmployeeClaimdataItem> filteredList = new ArrayList<>();
        for (EmployeeClaimdataItem claim : originalClaimList) {
            String claimDateString = claim.getClaimDate();

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date claimDate = dateFormat.parse(claimDateString);
                SimpleDateFormat filterDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedClaimDate = filterDateFormat.format(claimDate);

                // Check if the claim date is within the selected range
                if (formattedClaimDate.compareTo(fromDate) >= 0 && formattedClaimDate.compareTo(toDate) <= 0) {
                    filteredList.add(claim);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
//        claimsAdapter.updateData(filteredList);
        requireActivity().runOnUiThread(() -> {
            approveClaimAdapter.updateData(filteredList);
            loadingProgress.setVisibility(View.GONE);
        });
    }

    @Override
    public void onClaimClick(EmployeeClaimdataItem currentClaim) {
        // Create a bundle to pass data to the new fragment
        Bundle bundle = new Bundle();
        bundle.putSerializable("claimData", currentClaim);

        ApproveClaimDetailsFragment fragment = new ApproveClaimDetailsFragment();
        fragment.setArguments(bundle);

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_nav_approve_claim_to_nav_approve_claim_details, bundle);
    }
}