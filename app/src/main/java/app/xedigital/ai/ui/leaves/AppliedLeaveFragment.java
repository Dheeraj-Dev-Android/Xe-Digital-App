package app.xedigital.ai.ui.leaves;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.AppliedLeaveAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesItem;
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesResponse;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AppliedLeaveFragment extends Fragment {

    private static final String TAG = "AppliedLeaveFragment";

    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private AppliedLeavesResponse appliedLeavesResponse;
    private AppliedLeaveAdapter appliedLeaveAdapter;

    public AppliedLeaveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applied_leave, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.appliedLeavesRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");

        ChipGroup chipGroup = view.findViewById(R.id.statusChipGroup);
        chipGroup.setSingleSelection(true);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipAll)) {
                filterLeaves("All");
            } else if (checkedIds.contains(R.id.chipApproved)) {
                filterLeaves("Approved");
            } else if (checkedIds.contains(R.id.chipUnapproved)) {
                filterLeaves("Unapproved");
            } else if (checkedIds.contains(R.id.chipRejected)) {
                filterLeaves("Rejected");
            } else if (checkedIds.contains(R.id.chipCancelled)) {
                filterLeaves("Cancelled");
            } else {
                filterLeaves("All");
            }
        });

        fetchAppliedLeaves(recyclerView, authToken);

        return view;
    }

    // ======================== FETCH DATA ========================

    private void fetchAppliedLeaves(RecyclerView recyclerView, String authToken) {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        APIInterface apiInterface = APIClient.getInstance().AppliedLeave();
        Call<AppliedLeavesResponse> call = apiInterface.getAppliedLeaves("jwt " + authToken);

        call.enqueue(new Callback<AppliedLeavesResponse>() {
            @Override
            public void onResponse(@NonNull Call<AppliedLeavesResponse> call,
                                   @NonNull Response<AppliedLeavesResponse> response) {
                if (!isAdded()) return;

                loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    appliedLeavesResponse = response.body();

                    List<AppliedLeavesItem> leavesList = null;
                    if (appliedLeavesResponse.getData() != null) {
                        leavesList = appliedLeavesResponse.getData().getAppliedLeaves();
                    }

                    if (leavesList != null && !leavesList.isEmpty()) {
                        emptyStateText.setVisibility(View.GONE);
                        appliedLeaveAdapter = new AppliedLeaveAdapter(leavesList);
                        recyclerView.setAdapter(appliedLeaveAdapter);
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                    Toast.makeText(requireContext(),
                            "Failed to retrieve applied leaves", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppliedLeavesResponse> call,
                                  @NonNull Throwable throwable) {
                if (!isAdded()) return;

                loadingProgress.setVisibility(View.GONE);
                Log.e(TAG, "Fetch failed: " + throwable.getMessage());
                showEmptyState();
                Toast.makeText(requireContext(),
                        "Network Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ======================== FILTER ========================

    private void filterLeaves(String status) {
        if (appliedLeavesResponse == null
                || appliedLeavesResponse.getData() == null
                || appliedLeavesResponse.getData().getAppliedLeaves() == null) {
            return;
        }

        List<AppliedLeavesItem> originalList = appliedLeavesResponse.getData().getAppliedLeaves();
        List<AppliedLeavesItem> filteredList = new ArrayList<>();

        if ("All".equals(status)) {
            filteredList.addAll(originalList);
        } else {
            for (AppliedLeavesItem item : originalList) {
                if (item != null && item.getStatus() != null
                        && item.getStatus().equalsIgnoreCase(status)) {
                    filteredList.add(item);
                }
            }
        }

        if (appliedLeaveAdapter != null) {
            appliedLeaveAdapter.updateList(filteredList);

            if (filteredList.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
            } else {
                emptyStateText.setVisibility(View.GONE);
            }
        }
    }

    // ======================== HELPERS ========================

    private void showEmptyState() {
        emptyStateText.setVisibility(View.VISIBLE);
    }
}