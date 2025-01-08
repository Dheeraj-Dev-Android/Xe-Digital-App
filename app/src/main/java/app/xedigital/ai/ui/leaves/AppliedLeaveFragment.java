package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import app.xedigital.ai.adapter.AppliedLeaveAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesItem;
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AppliedLeaveFragment extends Fragment {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private AppliedLeavesResponse appliedLeavesResponse;
    private AppliedLeaveAdapter AppliedLeaveAdapter;

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
        loadingProgress.setVisibility(View.VISIBLE);

        APIInterface apiInterface = APIClient.getInstance().AppliedLeave();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        retrofit2.Call<AppliedLeavesResponse> appliedLeaves = apiInterface.getAppliedLeaves("jwt " + authToken);
        appliedLeaves.enqueue(new Callback<AppliedLeavesResponse>() {
            @Override
            public void onResponse(@NonNull Call<AppliedLeavesResponse> call, @NonNull Response<AppliedLeavesResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    appliedLeavesResponse = response.body();
//                    Log.d("AppliedLeavesResponse", gson.toJson(appliedLeavesResponse));
                    List<AppliedLeavesItem> leavesList = appliedLeavesResponse.getData().getAppliedLeaves();
//                    AppliedLeaveAdapter adapter = new AppliedLeaveAdapter(leavesList);
//                    recyclerView.setAdapter(adapter);

                    if (leavesList.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        AppliedLeaveAdapter = new AppliedLeaveAdapter(leavesList);
                        recyclerView.setAdapter(AppliedLeaveAdapter);
                    }
                } else {
                    Log.e("API Error", "Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppliedLeavesResponse> call, @NonNull Throwable throwable) {
                loadingProgress.setVisibility(View.GONE);
                Log.e("API Error", "Request failed: " + throwable.getMessage());
            }
        });
        ChipGroup chipGroup = view.findViewById(R.id.statusChipGroup);
        chipGroup.setSingleSelection(true);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Handle chip selection
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
        // Get the ChipGroup and the clicked chip
        ChipGroup chipGroup = requireView().findViewById(R.id.statusChipGroup);
        Chip clickedChip = (Chip) view;

        // Check the clicked chip
        chipGroup.check(clickedChip.getId());
    }

    private void filterLeaves(String status) {
        if (appliedLeavesResponse != null && appliedLeavesResponse.getData() != null && appliedLeavesResponse.getData().getAppliedLeaves() != null) {
            List<AppliedLeavesItem> originalList = appliedLeavesResponse.getData().getAppliedLeaves();
            List<AppliedLeavesItem> filteredList = new ArrayList<>();

            if (status.equals("All")) {
                filteredList.addAll(originalList);
            } else {
                for (AppliedLeavesItem item : originalList) {
                    if (item.getStatus().equalsIgnoreCase(status)) {
                        filteredList.add(item);
                    }
                }
            }
            AppliedLeaveAdapter.updateList(filteredList);
        }
    }
}