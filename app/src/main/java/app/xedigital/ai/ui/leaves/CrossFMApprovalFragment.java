package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.leaveApprovalPending.LeavePendingApprovalResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CrossFMApprovalFragment extends Fragment {

    private String authToken;
    private String userId;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyStateView;
//    private LeavePendingApprovalAdapter leavePendingApprovalAdapter;
//    private List<LeavePendingApprovalResponseData> leavePendingApprovalResponseDataList;

    public CrossFMApprovalFragment() {
        // Required empty public constructor
    }


    public static CrossFMApprovalFragment newInstance(String param1, String param2) {
        CrossFMApprovalFragment fragment = new CrossFMApprovalFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cross_f_m_approval, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateView = view.findViewById(R.id.emptyStateView);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        fetchPendingLeaves();

        return view;
    }

    private void fetchPendingLeaves() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        String authHeader = "jwt " + authToken;

        APIInterface apiService = APIClient.getInstance().getPendingApprovalLeaves();

        Call<LeavePendingApprovalResponse> call = apiService.getPendingApprovalLeavesCR(authHeader, userId);
        call.enqueue(new Callback<LeavePendingApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Response<LeavePendingApprovalResponse> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful()) {
                    LeavePendingApprovalResponse leavePendingApprovalResponse = response.body();
                    if (leavePendingApprovalResponse != null && !leavePendingApprovalResponse.getData().getAppliedLeaves().isEmpty()) {
                        // Data is available, update RecyclerView
                        // ... (your code to update RecyclerView adapter)
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        // Data is empty, show empty state view
                        emptyStateView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE); // Hide RecyclerView if empty
                    }
                } else {
                    // Handle API error
                    // ... (your error handling code)
                    emptyStateView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE); // Hide RecyclerView in case of error
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Log.e("Pending Approval Error", Objects.requireNonNull(throwable.getMessage()));
                emptyStateView.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Network Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}