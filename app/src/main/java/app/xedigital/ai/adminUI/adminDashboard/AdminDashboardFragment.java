package app.xedigital.ai.adminUI.adminDashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import app.xedigital.ai.R;

public class AdminDashboardFragment extends Fragment {

    private final int SCROLL_INTERVAL = 3000;
    private final android.os.Handler scrollHandler = new android.os.Handler();
    private AdminDashboardViewModel mViewModel;
    private BirthdayEmployeesAdapter birthdayAdapter;
    private String token;
    private TextView totalSignin, totalSignout, totalEmployees, totalBranches;
    private TextView birthdayCount;
    private RecyclerView birthdayRecyclerView;
    private LinearLayout emptyBirthdayState;
    private int currentPosition = 0;
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (birthdayAdapter != null && birthdayAdapter.getItemCount() > 0) {
                currentPosition = (currentPosition + 1) % birthdayAdapter.getItemCount();
                birthdayRecyclerView.smoothScrollToPosition(currentPosition);
                scrollHandler.postDelayed(this, SCROLL_INTERVAL);
            }
        }
    };

    public static AdminDashboardFragment newInstance() {
        return new AdminDashboardFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", "");

        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();

        String authToken = "jwt " + token;
        mViewModel.fetchDashboardData(authToken);
        mViewModel.fetchBirthdayData(authToken);
    }

    private void initializeViews(View view) {
        // Dashboard counters
        totalSignin = view.findViewById(R.id.signinCount);
        totalSignout = view.findViewById(R.id.signoutCount);
        totalEmployees = view.findViewById(R.id.employeesCount);
        totalBranches = view.findViewById(R.id.branchesCount);

        // Birthday components
        birthdayCount = view.findViewById(R.id.birthdayCount);
        birthdayRecyclerView = view.findViewById(R.id.birthdayRecyclerView);
        emptyBirthdayState = view.findViewById(R.id.emptyBirthdayState);
    }

    private void setupRecyclerView() {
        birthdayAdapter = new BirthdayEmployeesAdapter();
//        birthdayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        BirthdayEmployeesAdapter.setupHorizontalScrolling(birthdayRecyclerView);
        birthdayRecyclerView.setAdapter(birthdayAdapter);
        birthdayRecyclerView.setNestedScrollingEnabled(false);
        // Optional: Add snap helper for smooth item-by-item scrolling
        // This makes the scrolling snap to each item like a pager
        androidx.recyclerview.widget.PagerSnapHelper snapHelper = new androidx.recyclerview.widget.PagerSnapHelper();
        snapHelper.attachToRecyclerView(birthdayRecyclerView);
    }

    private void setupViewModel() {
        mViewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        // Observe dashboard data
        mViewModel.getDashboardData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                String totalVisitors = String.valueOf(data.getTotalVisitors());
                String checkInVisitors = String.valueOf(data.getTotalSigninVisitors());
                String checkOutVisitors = String.valueOf(data.getTotalSignoutVisitors());

                totalSignin.setText(checkInVisitors + " / " + totalVisitors);
                totalSignout.setText(checkOutVisitors + " / " + totalVisitors);
                totalEmployees.setText(String.valueOf(data.getTotalEmployees()));
                totalBranches.setText(String.valueOf(data.getTotalBranches()));
            } else {
                totalSignin.setText("N/A");
                totalSignout.setText("N/A");
                totalEmployees.setText("N/A");
                totalBranches.setText("N/A");
            }
        });

        // Observe birthday data
//        mViewModel.getBirthdayData().observe(getViewLifecycleOwner(), birthdayEmployees -> {
//            if (birthdayEmployees != null && !birthdayEmployees.isEmpty()) {
//                // Show birthday data
//                birthdayCount.setText(String.valueOf(birthdayEmployees.size()));
//                birthdayAdapter.updateBirthdayEmployees(birthdayEmployees);
//
//                // Show RecyclerView and hide empty state
//                birthdayRecyclerView.setVisibility(View.VISIBLE);
//                emptyBirthdayState.setVisibility(View.GONE);
//            } else {
//                // Show empty state
//                birthdayCount.setText("0");
//                birthdayAdapter.updateBirthdayEmployees(null);
//
//                // Hide RecyclerView and show empty state
//                birthdayRecyclerView.setVisibility(View.GONE);
//                emptyBirthdayState.setVisibility(View.VISIBLE);
//            }
//        });
        // Observe birthday data
        mViewModel.getBirthdayData().observe(getViewLifecycleOwner(), birthdayEmployees -> {
            if (birthdayEmployees != null && !birthdayEmployees.isEmpty()) {
                // Show birthday data
                birthdayCount.setText(String.valueOf(birthdayEmployees.size()));
                birthdayAdapter.updateBirthdayEmployees(birthdayEmployees);

                // Show RecyclerView and hide empty state
                birthdayRecyclerView.setVisibility(View.VISIBLE);
                emptyBirthdayState.setVisibility(View.GONE);

                // 👉 Start auto-scroll
                scrollHandler.removeCallbacks(scrollRunnable);
                scrollHandler.postDelayed(scrollRunnable, SCROLL_INTERVAL);
            } else {
                // Show empty state
                birthdayCount.setText("0");
                birthdayAdapter.updateBirthdayEmployees(null);

                // Hide RecyclerView and show empty state
                birthdayRecyclerView.setVisibility(View.GONE);
                emptyBirthdayState.setVisibility(View.VISIBLE);

                // 👉 Stop auto-scroll
                scrollHandler.removeCallbacks(scrollRunnable);
            }
        });


        // Observe loading state
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // You can show/hide a progress bar here if needed
            // For now, we'll just handle it silently
        });

        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop auto-scroll when view is destroyed
        scrollHandler.removeCallbacks(scrollRunnable);
    }

}