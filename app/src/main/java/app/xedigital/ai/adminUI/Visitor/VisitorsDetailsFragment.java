package app.xedigital.ai.adminUI.Visitor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adminAdapter.VisitorAdapter;
import app.xedigital.ai.model.Admin.VisitorsAdminDetails.VisitorsItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class VisitorsDetailsFragment extends Fragment {

    private final List<VisitorsItem> fullVisitorList = new ArrayList<>();
    private final List<VisitorsItem> filteredVisitorList = new ArrayList<>();
    private VisitorsDetailsViewModel mViewModel;
    private VisitorAdapter adapter;
    private RecyclerView rvVisitors;
    private EditText vsSearchEmployee;
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visitors_details, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvVisitors = view.findViewById(R.id.rvVisitors);
        vsSearchEmployee = view.findViewById(R.id.vsSearchEmployee);
        rvVisitors.setLayoutManager(new LinearLayoutManager(requireContext()));

        mViewModel = new ViewModelProvider(this).get(VisitorsDetailsViewModel.class);
        // This code runs when the button is clicked
        adapter = new VisitorAdapter(requireContext(), filteredVisitorList, new VisitorAdapter.OnVisitorActionListener() {
            @Override
            public void onCheckInClick(VisitorsItem visitor, int position) {
                handleCheckIn(visitor, position);
            }

            @Override
            public void onCheckOutClick(VisitorsItem visitor, int position) {
                handleCheckOut(visitor, position);
            }
        });
        rvVisitors.setAdapter(adapter);

        String authToken = getAuthToken();
        if (authToken != null) {
            mViewModel.getVisitors("jwt " + authToken).observe(getViewLifecycleOwner(), visitors -> {
                if (visitors != null && !visitors.isEmpty()) {
                    fullVisitorList.clear();
                    filteredVisitorList.clear();
                    fullVisitorList.addAll(visitors);
                    filteredVisitorList.addAll(visitors);
//                    adapter = new VisitorAdapter(requireContext(), filteredVisitorList);
                    adapter.notifyDataSetChanged();
                    rvVisitors.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(), "No Visitors Found", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show();
        }

        vsSearchEmployee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVisitorsByName(s.toString());
            }
        });
    }

    private void handleCheckIn(VisitorsItem visitor, int position) {
        String token = "jwt " + getAuthToken();
        String visitorId = visitor.getId();

        visitor.setApprovalStatus("approved");
        visitor.setApprovalDate(DateTimeUtils.getCurrentDateInISOFormat());
        // Show a small Toast so the user knows the request started
        showLoading(true);
        Toast.makeText(getContext(), "Processing Sign Out...", Toast.LENGTH_SHORT).show();

        mViewModel.checkInVisitor(token, visitorId, visitor).observe(getViewLifecycleOwner(), success -> {
            showLoading(false);
            if (success) {
                Toast.makeText(getContext(), "Visitor Signed Out Successfully", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
                // RELOAD THE PAGE DATA
                refreshData();
            } else {
                Toast.makeText(getContext(), "Failed to sign out visitor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCheckOut(VisitorsItem visitor, int position) {
        String token = "jwt " + getAuthToken();
        String visitorId = visitor.getId();

        // Update the local object with the required payload values
        visitor.setMeetingOverStatus("Done");
        visitor.setMeetingOverDate(DateTimeUtils.getCurrentDateInISOFormat());
        showLoading(true);

        // Call your ViewModel
        mViewModel.checkOutVisitor(token, visitorId, visitor).observe(getViewLifecycleOwner(), success -> {
            showLoading(false);
            if (success) {
                Toast.makeText(getContext(), "Sign-out successful", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
                // RELOAD THE PAGE DATA
                refreshData();
            } else {
                Toast.makeText(getContext(), "Sign-out failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void filterVisitorsByName(String query) {
        filteredVisitorList.clear();
        if (query.isEmpty()) {
            filteredVisitorList.addAll(fullVisitorList);
        } else {
            for (VisitorsItem visitor : fullVisitorList) {
                if (visitor.getName() != null && visitor.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredVisitorList.add(visitor);
                }
            }
        }
//        adapter.notifyDataSetChanged();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private String getAuthToken() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        return sharedPreferences.getString("authToken", null);
    }

    private void refreshData() {
        String authToken = getAuthToken();
        if (authToken != null) {
            // We call the same ViewModel method used in onViewCreated
            mViewModel.getVisitors("jwt " + authToken).observe(getViewLifecycleOwner(), visitors -> {
                if (visitors != null) {
                    fullVisitorList.clear();
                    filteredVisitorList.clear();
                    fullVisitorList.addAll(visitors);
                    filteredVisitorList.addAll(visitors);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            if (loadingDialog == null) {
                android.widget.ProgressBar progressBar = new android.widget.ProgressBar(requireContext());

                progressBar.setPadding(20, 20, 20, 20);

                loadingDialog = new android.app.AlertDialog.Builder(requireContext())
                        .setView(progressBar)
                        .setCancelable(false)
                        .create();

                if (loadingDialog.getWindow() != null) {
                    loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }
            }
            loadingDialog.show();
        } else {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }
}
