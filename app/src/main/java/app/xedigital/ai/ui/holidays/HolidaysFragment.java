package app.xedigital.ai.ui.holidays;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import app.xedigital.ai.adapter.HolidayAdapter;
import app.xedigital.ai.databinding.FragmentHolidaysBinding;


public class HolidaysFragment extends Fragment {

    private FragmentHolidaysBinding binding;
    private HolidayAdapter holidayAdapter;
    private ProgressBar progressBar;
    private HolidaysViewModel holidaysViewModel;
    private LinearLayout emptyStateView;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHolidaysBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        holidaysViewModel = new ViewModelProvider(this).get(HolidaysViewModel.class);

        progressBar = binding.progressBar;
        emptyStateView = binding.emptyStateView;
        swipeRefreshLayout = binding.swipeRefreshLayout;
        MaterialButton retryButton = binding.retryButton;


        // Initialize RecyclerView and adapter
        holidayAdapter = new HolidayAdapter();
        binding.recyclerViewHolidays.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewHolidays.setAdapter(holidayAdapter);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        loadHolidays(authToken);


        // Observe ViewModel data
        holidaysViewModel.getHolidaysList().observe(getViewLifecycleOwner(), holidays -> {
            if (holidays == null || holidays.isEmpty()) {
                binding.recyclerViewHolidays.setVisibility(View.GONE);
                emptyStateView.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewHolidays.setVisibility(View.VISIBLE);
                emptyStateView.setVisibility(View.GONE);
                holidayAdapter.updateHolidays(holidays);
            }
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        });
        progressBar.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setOnRefreshListener(() -> loadHolidays(authToken));

        retryButton.setOnClickListener(v -> loadHolidays(authToken));
        return root;
    }


    private void loadHolidays(String authToken) {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
        binding.recyclerViewHolidays.setVisibility(View.GONE);
        holidaysViewModel.loadHolidays(authToken);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}