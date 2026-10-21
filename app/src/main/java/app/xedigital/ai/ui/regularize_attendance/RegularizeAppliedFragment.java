package app.xedigital.ai.ui.regularize_attendance;

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
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;

public class RegularizeAppliedFragment extends Fragment {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private RecyclerView recyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private LinearLayout emptyStateContainer;
    private RegularizeAppliedAdapter regularizeAppliedAdapter;
    private RegularizeAppliedResponse apiResponse;
    private String currentFilterStatus = "All";
    private SecurePrefManager prefManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regularize_applied, container, false);
        recyclerView = view.findViewById(R.id.regularize_applied_recycler_view);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        loadingProgress.setVisibility(View.VISIBLE);
        APIInterface apiInterface = APIClient.getInstance().getRegularizeApplied();
        prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");

        Call<RegularizeAppliedResponse> call = apiInterface.getRegularizeApplied("jwt " + authToken);
        call.enqueue(new Callback<RegularizeAppliedResponse>() {

            @Override
            public void onResponse(@NonNull Call<RegularizeAppliedResponse> call, @NonNull retrofit2.Response<RegularizeAppliedResponse> response) {
                if (!isAdded() || getContext() == null || getView() == null) {
                    return;
                }

                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    apiResponse = response.body();
                    List<AttendanceRegularizeAppliedItem> items = apiResponse.getData().getAttendanceRegularizeApplied();

                    if (items == null || items.isEmpty()) {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateContainer.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        regularizeAppliedAdapter = new RegularizeAppliedAdapter(items);
                        recyclerView.setAdapter(regularizeAppliedAdapter);

                        // Apply whatever chip filter the user selected while the network request was executing
                        filterLeaves(currentFilterStatus);
                    }
                } else {
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    emptyStateText.setText("Failed to load data. Please try again.");
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegularizeAppliedResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                loadingProgress.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setText("Failed to load data. Please try again.");
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
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

            if (regularizeAppliedAdapter != null) {
                filterLeaves(currentFilterStatus);
            }
        });

        return view;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        loadingProgress = null;
        emptyStateText = null;
        emptyStateContainer = null;
    }
}