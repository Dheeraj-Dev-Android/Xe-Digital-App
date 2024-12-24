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
import android.widget.ProgressBar;
import android.widget.TextView;
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

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.VisitorsAdapter;

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

        // Find the chips by their IDs
        Chip preApprovedVisitorChip = view.findViewById(R.id.preApprovedVisitorChip);
        Chip checkVisitorsChip = view.findViewById(R.id.checkVisitorsChip);

        preApprovedVisitorChip.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_nav_vms_to_nav_preApproved_visitors);
            Toast.makeText(requireContext(), "Pre-Approved Visitors", Toast.LENGTH_SHORT).show();
        });

        checkVisitorsChip.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Check Visitors", Toast.LENGTH_SHORT).show();
            // Create an Intent to open the link in a web browser
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

        // Initialize adapter
        visitorsAdapter = new VisitorsAdapter(null);
        recyclerView.setAdapter(visitorsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Observe ViewModel LiveData
        mViewModel.getVisitors().observe(getViewLifecycleOwner(), visitorsItems -> {
            visitorsAdapter.updateVisitors(visitorsItems);
            updateVisibility(visitorsItems == null || visitorsItems.isEmpty());
        });

        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> loadingProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        mViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e("VmsFragment", "Error: " + error);
            }
        });

        // Fetch visitors data
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        mViewModel.fetchVisitors(authToken);
    }

    private void updateVisibility(boolean isEmpty) {
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}