package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        APIInterface apiInterface = APIClient.getInstance().AppliedLeave();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        retrofit2.Call<AppliedLeavesResponse> appliedLeaves = apiInterface.getAppliedLeaves("jwt " + authToken);
        appliedLeaves.enqueue(new Callback<AppliedLeavesResponse>() {
            @Override
            public void onResponse(@NonNull Call<AppliedLeavesResponse> call, @NonNull Response<AppliedLeavesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppliedLeavesResponse appliedLeavesResponse = response.body();
                    Log.d("AppliedLeavesResponse", gson.toJson(appliedLeavesResponse));
                    List<AppliedLeavesItem> leavesList = appliedLeavesResponse.getData().getAppliedLeaves();
                    AppliedLeaveAdapter adapter = new AppliedLeaveAdapter(leavesList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("API Error", "Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AppliedLeavesResponse> call, @NonNull Throwable throwable) {
                Log.e("API Error", "Request failed: " + throwable.getMessage());
            }
        });

        return view;
    }
}