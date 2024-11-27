package app.xedigital.ai.ui.claim_management;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.ClaimsAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimResponse;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimdataItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewClaimFragment extends Fragment {

    private RecyclerView claimsRecyclerView;
    private ClaimsAdapter claimsAdapter;
    private EditText searchEditText;
    private ImageButton filterButton;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;

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
        searchEditText = view.findViewById(R.id.searchEditText);
        filterButton = view.findViewById(R.id.filterButton);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Setup RecyclerView
        claimsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        claimsAdapter = new ClaimsAdapter(claimList);
        claimsRecyclerView.setAdapter(claimsAdapter);

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClaims(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Filter button (optional)
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement your filtering logic here
            }
        });

        // Make API call
        fetchClaims();
    }

    private void fetchClaims() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        String authToken = "your_auth_token"; // Get the auth token
        Call<EmployeeClaimResponse> call = apiInterface.getClaims(authToken); // Use EmployeeClaimResponse
        call.enqueue(new Callback<EmployeeClaimResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeClaimResponse> call, @NonNull Response<EmployeeClaimResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeClaimResponse employeeClaimResponse = response.body();
                    claimList = employeeClaimResponse.getEmployeeClaimdata();
                    if (claimList.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        claimsAdapter.updateData(claimList);
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


    private void filterClaims(String query) {
        List<EmployeeClaimdataItem> filteredList = new ArrayList<>();
        for (EmployeeClaimdataItem claim : claimList) {
            String claimDateString = claim.getClaimDate();

            try {
                // Use ISO 8601 date format
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date claimDate = dateFormat.parse(claimDateString);

                // Format the date for filtering (e.g., to "yyyy-MM-dd")
                SimpleDateFormat filterDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String filterDateString = filterDateFormat.format(claimDate);

                // Check if the formatted date matches the query
                if (filterDateString.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(claim);
                }
            } catch (ParseException e) {
                // Handle date parsing error
                e.printStackTrace();
            }
        }
        claimsAdapter.updateData(filteredList);
    }
}