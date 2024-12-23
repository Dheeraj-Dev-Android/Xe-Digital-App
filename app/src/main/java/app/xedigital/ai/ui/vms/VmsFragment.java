package app.xedigital.ai.ui.vms;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.VisitorsAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.vms.GetVisitorsResponse;
import app.xedigital.ai.model.vms.VisitorsItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VmsFragment extends Fragment {

    private VmsViewModel mViewModel;
    private RecyclerView recyclerView;
    private VisitorsAdapter visitorsAdapter;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;

    public static VmsFragment newInstance() {
        return new VmsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_v_m_s, container, false);
        recyclerView = view.findViewById(R.id.VisitorsListRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        fetchVisitors("jwt " + authToken);
    }

    private void fetchVisitors(String authToken) {
        loadingProgress.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        APIInterface visitorsApi = APIClient.getInstance().getVisitors();
        Call<GetVisitorsResponse> call = visitorsApi.getVisitors(authToken);

        call.enqueue(new Callback<GetVisitorsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetVisitorsResponse> call, @NonNull Response<GetVisitorsResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    GetVisitorsResponse visitorsResponse = response.body();
                    if (visitorsResponse != null) {
                        List<VisitorsItem> visitors = visitorsResponse.getData().getVisitors();
                        visitorsAdapter = new VisitorsAdapter(visitors);
                        recyclerView.setAdapter(visitorsAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        // Check if the adapter has any items
                        if (visitorsAdapter.getItemCount() > 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyStateText.setVisibility(View.GONE);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            emptyStateText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("API Error", "Empty response body");
                        recyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    }

                } else {
                    Log.e("API Error", "Response code: " + response.code());
                    recyclerView.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onFailure(@NonNull Call<GetVisitorsResponse> call, @NonNull Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                t.printStackTrace();
                Log.e("API Error", "Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(VmsViewModel.class);

    }

}