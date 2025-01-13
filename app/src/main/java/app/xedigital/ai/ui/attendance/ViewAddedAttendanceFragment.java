package app.xedigital.ai.ui.attendance;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.AddedAttendanceAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentViewAddedAttendanceBinding;
import app.xedigital.ai.model.addedAttendanceList.AddAttendanceRegularizeAppliedItem;
import app.xedigital.ai.model.addedAttendanceList.AddedAttendanceListResponse;
import retrofit2.Call;
import retrofit2.Response;

public class ViewAddedAttendanceFragment extends Fragment {

    List<AddAttendanceRegularizeAppliedItem> attendanceData = new ArrayList<>();
    private FragmentViewAddedAttendanceBinding binding;
    private RecyclerView recyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private AddedAttendanceListResponse addedAttendanceListResponse;
    private AddedAttendanceAdapter addedAttendanceAdapter;

    public ViewAddedAttendanceFragment() {
        // Required empty public constructor
    }

    public static ViewAddedAttendanceFragment newInstance() {
        return new ViewAddedAttendanceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentViewAddedAttendanceBinding.inflate(inflater, container, false);

        ChipGroup chipGroup = binding.statusChipGroup;
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
        return binding.getRoot();
    }

    public void onChipClicked(View view) {
        // Get the ChipGroup and the clicked chip
        ChipGroup chipGroup = requireView().findViewById(R.id.statusChipGroup);
        Chip clickedChip = (Chip) view;

        // Check the clicked chip
        chipGroup.check(clickedChip.getId());
    }

    private void filterLeaves(String status) {
        if (addedAttendanceListResponse != null && addedAttendanceListResponse.getData() != null && addedAttendanceListResponse.getData().getAddAttendanceRegularizeApplied() != null) {
            List<AddAttendanceRegularizeAppliedItem> originalList = addedAttendanceListResponse.getData().getAddAttendanceRegularizeApplied();
            List<AddAttendanceRegularizeAppliedItem> filteredList = new ArrayList<>();
            if (status.equals("All")) {
                filteredList.addAll(originalList);
            } else {
                for (AddAttendanceRegularizeAppliedItem item : originalList) {
                    if (item.getStatus().equalsIgnoreCase(status)) {
                        filteredList.add(item);
                    }
                }
            }
            addedAttendanceAdapter.updateAttendanceData(filteredList);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");

        recyclerView = binding.AddAttendanceRecyclerView;
        loadingProgress = binding.loadingProgress;
        emptyStateText = binding.emptyStateText;

        addedAttendanceAdapter = new AddedAttendanceAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(addedAttendanceAdapter);

        loadingProgress.setVisibility(View.VISIBLE);
        getAddedAttendance(authToken);

    }

    private void getAddedAttendance(String authToken) {
        String authHeader = "jwt " + authToken;
        APIInterface getAddedAttendance = APIClient.getInstance().getAttendance();
        Call<AddedAttendanceListResponse> call = getAddedAttendance.getAddedAttendance(authHeader);
        call.enqueue(new retrofit2.Callback<AddedAttendanceListResponse>() {

            @Override
            public void onResponse(@NonNull Call<AddedAttendanceListResponse> call, @NonNull Response<AddedAttendanceListResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    addedAttendanceListResponse = response.body();
                    if (addedAttendanceListResponse.getData() != null && !addedAttendanceListResponse.getData().getAddAttendanceRegularizeApplied().isEmpty()) {
                        recyclerView.setAdapter(new AddedAttendanceAdapter(addedAttendanceListResponse.getData().getAddAttendanceRegularizeApplied()));
                        addedAttendanceAdapter = (AddedAttendanceAdapter) recyclerView.getAdapter();
                        if (addedAttendanceAdapter != null) {
                            addedAttendanceAdapter.updateAttendanceData(addedAttendanceListResponse.getData().getAddAttendanceRegularizeApplied());
                        }
                    } else {
                        emptyStateText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    // Handle API error, show error message
                    emptyStateText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    emptyStateText.setText("Failed to load data. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddedAttendanceListResponse> call, @NonNull Throwable throwable) {
                loadingProgress.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
                emptyStateText.setText("Failed to load data. Please try again.");
                Log.e("API Error", "API call failed: " + throwable.getMessage());
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}