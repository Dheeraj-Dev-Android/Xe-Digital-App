package app.xedigital.ai.ui.vms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.VisitorClickListener;
import app.xedigital.ai.adapter.VisitorsAdapter;
import app.xedigital.ai.model.vms.VisitorsItem;

public class VmsFragment extends Fragment implements VisitorClickListener {

    private VmsViewModel mViewModel;
    private RecyclerView recyclerView;
    private VisitorsAdapter visitorsAdapter;
    private CircularProgressIndicator loadingProgress;
    private LinearLayout emptyStateContainer;

    public static VmsFragment newInstance() {
        return new VmsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_v_m_s, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.VisitorsListRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        // Find and setup chips
        Chip preApprovedVisitorChip = view.findViewById(R.id.preApprovedVisitorChip);
        Chip checkVisitorsChip = view.findViewById(R.id.checkVisitorsChip);

        preApprovedVisitorChip.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_nav_vms_to_nav_preApproved_visitors);
            Toast.makeText(requireContext(), "Pre-Approved Visitors", Toast.LENGTH_SHORT).show();
        });

        checkVisitorsChip.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Check Visitors", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://app.xedigital.ai/checkin/home"));
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        mViewModel = new ViewModelProvider(this).get(VmsViewModel.class);

        // Initialize adapter and RecyclerView
//        visitorsAdapter = new VisitorsAdapter((List<VisitorsItem>) null, (VisitorClickListener) this);
        visitorsAdapter = new VisitorsAdapter(null, this);
        recyclerView.setAdapter(visitorsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Observe ViewModel LiveData
        mViewModel.getVisitors().observe(getViewLifecycleOwner(), visitorsItems -> {
            visitorsAdapter.updateVisitors(visitorsItems);
            updateUIState(visitorsItems == null || visitorsItems.isEmpty(), false);
        });

        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> updateUIState(false, isLoading));

        mViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e("VmsFragment", "Error: " + error);
                Toast.makeText(requireContext(), "Error loading visitors", Toast.LENGTH_SHORT).show();
                updateUIState(true, false);
            }
        });

        // Fetch visitors data
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        mViewModel.fetchVisitors(authToken);
    }

    private void updateUIState(boolean isEmpty, boolean isLoading) {
        if (isLoading) {
            // Show loading state
            loadingProgress.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else if (isEmpty) {
            // Show empty state
            loadingProgress.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // Show content
            loadingProgress.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVisitorClicked(VisitorsItem visitor) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("visitor", visitor);

        // Navigate using NavController
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_vms_to_nav_visitor_details, bundle);
    }
}
