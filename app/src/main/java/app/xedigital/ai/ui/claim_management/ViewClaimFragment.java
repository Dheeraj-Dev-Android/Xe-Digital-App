package app.xedigital.ai.ui.claim_management;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.ClaimsAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.employeeClaim.Data;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimResponse;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimdataItem;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;

public class ViewClaimFragment extends Fragment implements ClaimsAdapter.OnClaimClickListener {

    private final List<EmployeeClaimdataItem> originalClaimList = new ArrayList<>();
    private RecyclerView claimsRecyclerView;
    private ClaimsAdapter claimsAdapter;
    private DatePickerDialog datePickerDialog;
    private ImageButton filterButton;
    private CircularProgressIndicator loadingProgress;
    private LinearLayout emptyStateContainer;
    private EditText fromDateEditText;
    private EditText toDateEditText;
    private ChipGroup statusChipGroup;
    private String selectedStatusFilter = "all";
    private List<EmployeeClaimdataItem> claimList = new ArrayList<>();
    private APIInterface apiInterface;

    public ViewClaimFragment() {
        // Required empty public constructor
    }

    public static ViewClaimFragment newInstance(String param1, String param2) {
        return new ViewClaimFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = APIClient.getInstance().getEmployeeClaim();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_claim, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        claimsRecyclerView = view.findViewById(R.id.claimsRecyclerView);
        filterButton = view.findViewById(R.id.filterButton);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        fromDateEditText = view.findViewById(R.id.fromDateEditText);
        toDateEditText = view.findViewById(R.id.toDateEditText);
        statusChipGroup = view.findViewById(R.id.statusChipGroup);

        claimsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        claimsAdapter = new ClaimsAdapter(claimList);
        claimsAdapter.setOnClaimClickListener(this);
        claimsRecyclerView.setAdapter(claimsAdapter);

        fromDateEditText.setOnClickListener(v -> showDatePickerDialog(fromDateEditText));
        toDateEditText.setOnClickListener(v -> showDatePickerDialog(toDateEditText));

        filterButton.setOnClickListener(v -> {
            String fromDate = fromDateEditText.getText() != null ? fromDateEditText.getText().toString() : "";
            String toDate = toDateEditText.getText() != null ? toDateEditText.getText().toString() : "";

            if (!fromDate.isEmpty() && !toDate.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date fromDateObj = dateFormat.parse(fromDate);
                    Date toDateObj = dateFormat.parse(toDate);

                    if (fromDateObj == null || toDateObj == null) {
                        Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (toDateObj.before(fromDateObj)) {
                        Toast.makeText(requireContext(), "To date cannot be before from date", Toast.LENGTH_SHORT).show();
                    } else {
                        applyFilters();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(requireContext(), "Please select both dates", Toast.LENGTH_SHORT).show();
            }
        });

        if (statusChipGroup != null) {
            statusChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds == null || checkedIds.isEmpty()) return;
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipAll) {
                    selectedStatusFilter = "all";
                } else if (checkedId == R.id.chipUnapproved) {
                    selectedStatusFilter = "unapproved";
                } else if (checkedId == R.id.chipApproved) {
                    selectedStatusFilter = "approved";
                } else if (checkedId == R.id.chipRejected) {
                    selectedStatusFilter = "rejected";
                } else if (checkedId == R.id.chipCancelled) {
                    selectedStatusFilter = "cancelled";
                }
                applyFilters();
            });
        }

        fetchClaims();
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

    private void fetchClaims() {
        if (loadingProgress != null) loadingProgress.setVisibility(View.VISIBLE);
        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");
        String token = "jwt " + authToken;

        Call<EmployeeClaimResponse> call = apiInterface.getClaims(token);
        call.enqueue(new Callback<EmployeeClaimResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeClaimResponse> call, @NonNull retrofit2.Response<EmployeeClaimResponse> response) {
                if (!isAdded()) return;
                if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    EmployeeClaimResponse employeeClaimResponse = response.body();
                    Data data = employeeClaimResponse.getData();
                    if (data != null && data.getEmployeeClaimdata() != null) {
                        claimList = data.getEmployeeClaimdata();
                        originalClaimList.clear();
                        originalClaimList.addAll(claimList);
                        applyFilters();
                    } else {
                        if (emptyStateContainer != null)
                            emptyStateContainer.setVisibility(View.VISIBLE);
                        Log.e("ViewClaimFragment", "Data or employeeClaimdata is null in response");
                    }
                } else {
                    if (emptyStateContainer != null)
                        emptyStateContainer.setVisibility(View.VISIBLE);
                    Log.e("ViewClaimFragment", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeClaimResponse> call, @NonNull Throwable throwable) {
                if (!isAdded()) return;
                if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
                Log.e("ViewClaimFragment", "Error: " + throwable.getMessage());
            }
        });
    }

    private void applyFilters() {
        if (originalClaimList == null) return;

        List<EmployeeClaimdataItem> filteredList = new ArrayList<>();
        String fromDate = fromDateEditText != null && fromDateEditText.getText() != null
                ? fromDateEditText.getText().toString() : "";
        String toDate = toDateEditText != null && toDateEditText.getText() != null
                ? toDateEditText.getText().toString() : "";
        boolean hasDateFilter = !fromDate.isEmpty() && !toDate.isEmpty();
        boolean hasStatusFilter = selectedStatusFilter != null && !selectedStatusFilter.equals("all");

        for (EmployeeClaimdataItem claim : originalClaimList) {
            if (claim == null) continue;

            // Status filter
            if (hasStatusFilter) {
                String status = claim.getStatusRm() != null ? claim.getStatusRm()
                        : (claim.getStatusHr() != null ? claim.getStatusHr() : "pending");
                if (!status.equalsIgnoreCase(selectedStatusFilter)) {
                    continue;
                }
            }

            // Date filter
            if (hasDateFilter) {
                String claimDateString = claim.getClaimDate();
                if (claimDateString == null || claimDateString.isEmpty()) {
                    continue;
                }
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    Date claimDate = dateFormat.parse(claimDateString);
                    if (claimDate == null) continue;
                    SimpleDateFormat filterDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String formattedClaimDate = filterDateFormat.format(claimDate);

                    if (formattedClaimDate.compareTo(fromDate) < 0 || formattedClaimDate.compareTo(toDate) > 0) {
                        continue;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            filteredList.add(claim);
        }

        claimList = filteredList;
        if (claimsAdapter != null) {
            claimsAdapter.updateData(claimList);
        }

        if (emptyStateContainer != null) {
            if (claimList.isEmpty()) {
                emptyStateContainer.setVisibility(View.VISIBLE);
            } else {
                emptyStateContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClaimClick(EmployeeClaimdataItem claim) {
        if (claim == null) return;
        Bundle bundle = new Bundle();
        bundle.putSerializable("claimData", claim);

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_nav_view_claim_to_nav_claim_details, bundle);
    }
}