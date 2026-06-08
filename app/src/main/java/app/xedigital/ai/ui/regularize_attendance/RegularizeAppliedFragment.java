package app.xedigital.ai.ui.regularize_attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.RegularizeAppliedAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.regularizeApplied.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.model.regularizeApplied.RegularizeAppliedResponse;
import retrofit2.Call;
import retrofit2.Callback;

public class RegularizeAppliedFragment extends Fragment {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private LinearLayout emptyStateContainer;
    private RegularizeAppliedAdapter regularizeAppliedAdapter;
    private RegularizeAppliedResponse apiResponse;
    private String currentFilterStatus = "All";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regularize_applied, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.regularize_applied_recycler_view);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        loadingProgress.setVisibility(View.VISIBLE);
        APIInterface apiInterface = APIClient.getInstance().getRegularizeApplied();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        retrofit2.Call<RegularizeAppliedResponse> call = apiInterface.getRegularizeApplied("jwt " + authToken);
        call.enqueue(new Callback<RegularizeAppliedResponse>() {

            @Override
            public void onResponse(@NonNull Call<RegularizeAppliedResponse> call, @NonNull retrofit2.Response<RegularizeAppliedResponse> response) {
                // LIFECYCLE GUARD: If Xiaomi OS destroyed the fragment/view context while network was in-flight, exit gracefully.
                if (!isAdded() || getContext() == null || getView() == null) {
                    return;
                }

                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    apiResponse = response.body();
                    List<AttendanceRegularizeAppliedItem> items = apiResponse.getData().getAttendanceRegularizeApplied();

                    if (items == null || items.isEmpty()) {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                    } else {
                        emptyStateContainer.setVisibility(View.GONE);
                        regularizeAppliedAdapter = new RegularizeAppliedAdapter(items);
                        recyclerView.setAdapter(regularizeAppliedAdapter);

                        // Apply whatever chip filter the user selected while the network request was executing
                        filterLeaves(currentFilterStatus);
                    }
                } else {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegularizeAppliedResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                loadingProgress.setVisibility(View.GONE);
            }
        });

        ChipGroup chipGroup = view.findViewById(R.id.statusChipGroup);
        chipGroup.setSingleSelection(true);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipAll)) currentFilterStatus = "All";
            else if (checkedIds.contains(R.id.chipApproved)) currentFilterStatus = "Approved";
            else if (checkedIds.contains(R.id.chipUnapproved)) currentFilterStatus = "Unapproved";
            else if (checkedIds.contains(R.id.chipRejected)) currentFilterStatus = "Rejected";
            else if (checkedIds.contains(R.id.chipCancelled)) currentFilterStatus = "Cancelled";
            else currentFilterStatus = "All";

            // RACE CONDITION GUARD: Only execute filtering logic if the API response has successfully populated the adapter
            if (regularizeAppliedAdapter != null) {
                filterLeaves(currentFilterStatus);
            }
        });

        // Attach click listeners to chips
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                child.setOnClickListener(this::onChipClicked);
            }
        }
        return view;
    }

    public void onChipClicked(View view) {
        if (getView() == null) return;
        ChipGroup chipGroup = getView().findViewById(R.id.statusChipGroup);
        Chip clickedChip = (Chip) view;
        if (chipGroup != null && clickedChip != null) {
            chipGroup.check(clickedChip.getId());
        }
    }

    private void filterLeaves(String status) {
        if (regularizeAppliedAdapter == null) {
            return;
        }
        if (apiResponse == null || apiResponse.getData() == null || apiResponse.getData().getAttendanceRegularizeApplied() == null) {
            return;
        }

        List<AttendanceRegularizeAppliedItem> originalList = apiResponse.getData().getAttendanceRegularizeApplied();
        List<AttendanceRegularizeAppliedItem> filteredList = new ArrayList<>();

        if ("All".equalsIgnoreCase(status)) {
            for (AttendanceRegularizeAppliedItem item : originalList) {
                if (item != null) filteredList.add(item);
            }
        } else {
            for (AttendanceRegularizeAppliedItem item : originalList) {
                if (item != null && item.getStatus() != null && item.getStatus().equalsIgnoreCase(status)) {
                    filteredList.add(item);
                }
            }
        }
        regularizeAppliedAdapter.updateList(filteredList);
    }
}