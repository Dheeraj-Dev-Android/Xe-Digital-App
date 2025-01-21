package app.xedigital.ai.ui.holidays;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.adapter.HolidayAdapter;
import app.xedigital.ai.databinding.FragmentHolidaysBinding;
import app.xedigital.ai.model.holiday.HolidaysItem;


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
                List<HolidaysItem> sortedHolidays = sortHolidaysByMonth(holidays);
                Gson gson = new Gson();
                String sortedHolidaysJsonString = gson.toJson(sortedHolidays);

                // Log the JSON string
                Log.d("SortedHolidays", "Sorted Holidays (JSON): " + sortedHolidaysJsonString);
                holidayAdapter.updateHolidays(sortedHolidays);
            }
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        });
        progressBar.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setOnRefreshListener(() -> loadHolidays(authToken));

        retryButton.setOnClickListener(v -> loadHolidays(authToken));
        return root;
    }

    private List<HolidaysItem> sortHolidaysByMonth(List<HolidaysItem> holidays) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        List<Pair<Date, HolidaysItem>> holidaysWithMonth = new ArrayList<>();
        for (HolidaysItem holiday : holidays) {
            try {
                Date date = dateFormat.parse(holiday.getHolidayDate());
                holidaysWithMonth.add(new Pair<>(date, holiday));
            } catch (ParseException e) {
                Log.e("HolidaysFragment", "Error parsing date", e);
                e.printStackTrace();
            }
        }

        // Sort by Month and then by Day
        holidaysWithMonth.sort((a, b) -> {
            // Get the month for each date
            Calendar calA = Calendar.getInstance();
            calA.setTime(a.first);
            int monthA = calA.get(Calendar.MONTH);

            Calendar calB = Calendar.getInstance();
            calB.setTime(b.first);
            int monthB = calB.get(Calendar.MONTH);

            // Compare by month, then
            if (monthA != monthB) {
                return Integer.compare(monthA, monthB);
            } else {
                // If months are the same, compare by day
                int dayA = calA.get(Calendar.DAY_OF_MONTH);
                int dayB = calB.get(Calendar.DAY_OF_MONTH);
                return Integer.compare(dayA, dayB);
            }
        });

        List<HolidaysItem> sortedHolidays = new ArrayList<>();
        for (Pair<Date, HolidaysItem> item : holidaysWithMonth) {
            sortedHolidays.add(item.second);
        }

        return sortedHolidays;
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