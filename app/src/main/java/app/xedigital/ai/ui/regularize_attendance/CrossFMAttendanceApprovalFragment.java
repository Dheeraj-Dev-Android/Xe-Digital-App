package app.xedigital.ai.ui.regularize_attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import app.xedigital.ai.adapter.CFregularizeAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.cfRegularizeApproval.AttendanceRegItem;
import app.xedigital.ai.model.cfRegularizeApproval.CfRegularizeApprovalResponse;
import app.xedigital.ai.model.cfRegularizeApproval.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CrossFMAttendanceApprovalFragment extends Fragment {

    private static final String TAG = "CrossFMAttendanceApprovalFragment";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String authToken;
    private String userId;
    private RecyclerView recyclerView;
    private CFregularizeAdapter adapter;
    private APIInterface apiInterface;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private LinearLayout emptyStateContainer;
    private CfRegularizeApprovalResponse regularizeApprovalResponse;
    private Button crossManager;

    public CrossFMAttendanceApprovalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cross_f_m_attendance_approval, container, false);

        recyclerView = view.findViewById(R.id.cross_approval_recycler_view);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        loadingProgress.setVisibility(View.VISIBLE);
        apiInterface = APIClient.getInstance().getAttendance();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        getRegularizeApproval();

        ChipGroup chipGroup = view.findViewById(R.id.statusChipGroup);
        chipGroup.setSingleSelection(false);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Handle chip selection
            if (checkedIds.contains(R.id.chipAll)) {
                filterRequests("All");
            } else if (checkedIds.contains(R.id.chipApproved)) {
                filterRequests("Approved");
            } else if (checkedIds.contains(R.id.chipUnapproved)) {
                filterRequests("Unapproved");
            } else if (checkedIds.contains(R.id.chipRejected)) {
                filterRequests("Rejected");
            } else if (checkedIds.contains(R.id.chipCancelled)) {
                filterRequests("Cancelled");
            } else {
                filterRequests("All");
            }
        });
        // Attach click listeners to chips
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                child.setOnClickListener(this::onChipClicked);
            }
        }
//        callApi();
        return view;
    }

    public void onChipClicked(View view) {
        // Get the ChipGroup and the clicked chip
        ChipGroup chipGroup = requireView().findViewById(R.id.statusChipGroup);
        Chip clickedChip = (Chip) view;

        // Check the clicked chip
        chipGroup.check(clickedChip.getId());
    }

    private void filterRequests(String status) {
        if (regularizeApprovalResponse != null && regularizeApprovalResponse.getData() != null) {
            List<AttendanceRegItem> originalList = (List<AttendanceRegItem>) regularizeApprovalResponse.getData();
            List<AttendanceRegItem> filteredList = new ArrayList<>();

            if (status.equals("All")) {
                filteredList.addAll(originalList);
            } else {
                for (AttendanceRegItem item : originalList) {
                    if (item.getStatus().equalsIgnoreCase(status)) {
                        filteredList.add(item);
                    }
                }
            }
            adapter.updateList(filteredList);
        }
    }

    private void getRegularizeApproval() {
//        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
        // Make API call
        Call<CfRegularizeApprovalResponse> call = apiInterface.getCFMRegularize("jwt " + authToken, userId);

        call.enqueue(new Callback<CfRegularizeApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<CfRegularizeApprovalResponse> call, @NonNull Response<CfRegularizeApprovalResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    CfRegularizeApprovalResponse regularizeApprovalResponse = response.body();
                    if (regularizeApprovalResponse != null) { // CHECK IF BODY IS NULL
                        // Access the Data object correctly
                        Data data = regularizeApprovalResponse.getData();

                        if (data != null) {
                            // Correctly get the List from the Data object
                            List<AttendanceRegItem> items = data.getAttendanceReg();

                            if (items.isEmpty()) {
                                emptyStateContainer.setVisibility(View.VISIBLE);
                                Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                                new AlertDialog.Builder(requireContext()).setTitle("Attendance Regularization Approval List").setMessage("No Records Found.").setPositiveButton("OK", null).show();
                            } else {
                                emptyStateContainer.setVisibility(View.GONE);
                                adapter = new CFregularizeAdapter(items, authToken, userId, getContext());
                                recyclerView.setAdapter(adapter);
                            }
                        } else {
                            Log.d("Approval pending List", "Data is null");
                            Toast.makeText(getContext(), "Data is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Approval pending List", "response.body() is null");
                        Toast.makeText(getContext(), "response.body() is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Approval pending List", "Failed");
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CfRegularizeApprovalResponse> call, @NonNull Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.VISIBLE);
                Log.e("Approval pending List", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

}