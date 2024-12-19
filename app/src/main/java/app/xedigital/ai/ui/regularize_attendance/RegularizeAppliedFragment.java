package app.xedigital.ai.ui.regularize_attendance;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    RegularizeAppliedResponse apiResponse = response.body();
                    Log.d("RegularizeApplied", gson.toJson(apiResponse));
                    List<AttendanceRegularizeAppliedItem> items = apiResponse.getData().getAttendanceRegularizeApplied();
                    if (items.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        emptyStateText.setVisibility(View.GONE);
                        RegularizeAppliedAdapter adapter = new RegularizeAppliedAdapter(items);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegularizeAppliedResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                loadingProgress.setVisibility(View.GONE);
            }
        });

        return view;
    }

}