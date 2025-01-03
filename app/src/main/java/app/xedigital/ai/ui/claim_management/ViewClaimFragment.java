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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import retrofit2.Call;
import retrofit2.Callback;

public class ViewClaimFragment extends Fragment implements ClaimsAdapter.OnClaimClickListener {

    private final List<EmployeeClaimdataItem> originalClaimList = new ArrayList<>();
    private RecyclerView claimsRecyclerView;
    private ClaimsAdapter claimsAdapter;
    private DatePickerDialog datePickerDialog;
    private ImageButton filterButton;
    private ProgressBar loadingProgress;
    private LinearLayout emptyStateContainer;
    private EditText fromDateEditText;
    private EditText toDateEditText;
    private List<EmployeeClaimdataItem> claimList = new ArrayList<>();
    private APIInterface apiInterface;

    public ViewClaimFragment() {
        // Required empty public constructor
    }

    public static ViewClaimFragment newInstance(String param1, String param2) {
        ViewClaimFragment fragment = new ViewClaimFragment();
        return fragment;
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

        // Initialize views
        claimsRecyclerView = view.findViewById(R.id.claimsRecyclerView);
        filterButton = view.findViewById(R.id.filterButton);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        fromDateEditText = view.findViewById(R.id.fromDateEditText);
        toDateEditText = view.findViewById(R.id.toDateEditText);

        // Setup RecyclerView
        claimsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        claimsAdapter = new ClaimsAdapter(claimList);
        claimsAdapter.setOnClaimClickListener(this);
        claimsRecyclerView.setAdapter(claimsAdapter);


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

                    if (toDateObj.before(fromDateObj)) {

                        Toast.makeText(requireContext(), "To date cannot be before from date", Toast.LENGTH_SHORT).show();
                    } else {
                        // Filter claims by date range
//                        filterClaimsByDateRange(fromDate, toDate);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingProgress.setVisibility(View.VISIBLE);
                                filterClaimsByDateRange(fromDate, toDate);
                            }
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
        // Make API call
        fetchClaims();
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                editText.setText(selectedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void fetchClaims() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String token = "jwt " + authToken;

        Call<EmployeeClaimResponse> call = apiInterface.getClaims(token);
        call.enqueue(new Callback<EmployeeClaimResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeClaimResponse> call, @NonNull retrofit2.Response<EmployeeClaimResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {

                    EmployeeClaimResponse employeeClaimResponse = response.body();
                    Data data = employeeClaimResponse.getData();
                    if (data != null && data.getEmployeeClaimdata() != null) {
                        claimList = data.getEmployeeClaimdata();
                        originalClaimList.clear();
                        originalClaimList.addAll(claimList);

                        if (claimList.isEmpty()) {
                            emptyStateContainer.setVisibility(View.VISIBLE);
                            Log.d("API Response", "No data available");
                        } else {
                            claimsAdapter.updateData(claimList);
                            emptyStateContainer.setVisibility(View.GONE);
                            Log.d("API Response", "Data fetched successfully");
                        }
                    } else {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                        Log.e("API Error", "Data or employeeClaimdata is null in response");
                    }
                } else {
                    Log.e("API Error", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeClaimResponse> call, @NonNull Throwable throwable) {
                loadingProgress.setVisibility(View.GONE);
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
            claimsAdapter.updateData(filteredList);
            loadingProgress.setVisibility(View.GONE);
        });
    }

    @Override
    public void onClaimClick(EmployeeClaimdataItem claim) {
        // Create a bundle to pass data to the new fragment
        Bundle bundle = new Bundle();
        bundle.putSerializable("claimData", claim);

        ClaimDetailsFragment fragment = new ClaimDetailsFragment();
        fragment.setArguments(bundle);

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_nav_view_claim_to_nav_claim_details, bundle);
    }

}