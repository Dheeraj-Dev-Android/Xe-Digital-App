package app.xedigital.ai.ui.shifts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.ShiftApprovalListAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.shiftApprovalList.EmployeeApproveShiftdataItem;
import app.xedigital.ai.model.shiftApprovalList.ShiftApproveListResponse;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShiftApproveFragment extends Fragment {

    private static final String TAG = "ShiftApproveFragment";

    // Views
    private RecyclerView shiftApproveRecyclerView;
    private CircularProgressIndicator loadingProgress;  // ✅ Correct type
    private LinearLayout emptyStateContainer;           // ✅ Correct type
    private TextView emptyStateText;

    // ViewModel & Prefs
    private ProfileViewModel profileViewModel;
    private SecurePrefManager prefManager;

    public ShiftApproveFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_shift_approve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ---- Init ViewModel ----
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // ---- Bind Views ----
        shiftApproveRecyclerView = view.findViewById(R.id.ShiftApproveRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // ---- RecyclerView Setup ----
        shiftApproveRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        // ---- Auth Token ----
        prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = "jwt " + prefManager.getString("authToken", "");

        // ---- Fetch Data ----
        fetchShiftApprovalList(authToken);
    }

    private void fetchShiftApprovalList(String authToken) {
        showLoading();

        APIInterface api = APIClient.getInstance().getShiftTypes();
        api.getShiftApprovalList(authToken).enqueue(new Callback<ShiftApproveListResponse>() {

            @Override
            public void onResponse(
                    @NonNull Call<ShiftApproveListResponse> call,
                    @NonNull Response<ShiftApproveListResponse> response
            ) {
                hideLoading();

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getEmployeeShiftdata() != null
                        && !response.body().getData().getEmployeeShiftdata().isEmpty()
                ) {
                    List<EmployeeApproveShiftdataItem> list =
                            response.body().getData().getEmployeeShiftdata();
                    showList(list);
                } else {
                    Log.e(TAG, "Empty or error response: "
                            + response.code() + " - " + response.message());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<ShiftApproveListResponse> call,
                    @NonNull Throwable t
            ) {
                hideLoading();
                Log.e(TAG, "Network error: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    // ----------------------------------------------------------------
    // UI State helpers — single place to control visibility
    // ----------------------------------------------------------------

    private void showLoading() {
        loadingProgress.setVisibility(View.VISIBLE);
        shiftApproveRecyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingProgress.setVisibility(View.GONE);
    }

    private void showList(List<EmployeeApproveShiftdataItem> list) {
        shiftApproveRecyclerView.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);

        ShiftApprovalListAdapter adapter = new ShiftApprovalListAdapter(
                getContext(),
                list,
                getViewLifecycleOwner(),
                profileViewModel
        );
        shiftApproveRecyclerView.setAdapter(adapter);
    }

    private void showEmptyState() {
        shiftApproveRecyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
    }
}