package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.AttendanceRegularizationApproval.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class AttendanceRegularizationApprovalFragment extends Fragment implements RegularizationFilterBottomSheet.FilterListener {

    private final List<AttendanceRegularizeAppliedItem> regularizationList = new ArrayList<>();
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());

    private EditText etSearch;
    private ImageButton btnFilter;
    private TextView tvFilterSummary;
    private RecyclerView recyclerViewRegularization;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private AttendanceRegularizationApprovalViewModel viewModel;
    private AttendanceRegularizationApprovalAdapter adapter;

    private String currentStartDate = "";
    private String currentEndDate = "";

    public static AttendanceRegularizationApprovalFragment newInstance() {
        return new AttendanceRegularizationApprovalFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendance_regularization_approval, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        initViewModel();
        setupRecyclerView();
        setupSearch();
        setupFilterButton();
        observeLiveData();
        loadInitialData();
    }

    private void bindViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        tvFilterSummary = view.findViewById(R.id.tvFilterSummary);
        recyclerViewRegularization = view.findViewById(R.id.recyclerViewRegularization);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(AttendanceRegularizationApprovalViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new AttendanceRegularizationApprovalAdapter(regularizationList);
        recyclerViewRegularization.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewRegularization.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFilterButton() {
        btnFilter.setOnClickListener(v -> openFilterBottomSheet());
    }

    private void openFilterBottomSheet() {
        RegularizationFilterBottomSheet sheet = new RegularizationFilterBottomSheet();
        sheet.setFilterListener(this);
        sheet.show(getChildFragmentManager(), "RegularizationFilter");
    }

    @Override
    public void onFilterApplied(String startDate, String endDate) {
        currentStartDate = startDate;
        currentEndDate = endDate;
        fetchRegularizationData();
    }

    @Override
    public void onFilterReset() {
        setDefaultDates();
        fetchRegularizationData();
    }

    private void observeLiveData() {
        viewModel.getRegularizationData().observe(getViewLifecycleOwner(), data -> {
            regularizationList.clear();
            if (data != null && !data.isEmpty()) {
                regularizationList.addAll(data);
                tvEmpty.setVisibility(View.GONE);
                recyclerViewRegularization.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerViewRegularization.setVisibility(View.GONE);
            }
            adapter.setItems(regularizationList);
            if (etSearch != null && etSearch.getText() != null) {
                adapter.filter(etSearch.getText().toString());
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerViewRegularization.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDefaultDates() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        currentStartDate = apiFormat.format(cal.getTime());

        Calendar today = Calendar.getInstance();
        currentEndDate = apiFormat.format(today.getTime());
    }

    private void loadInitialData() {
        setDefaultDates();
        fetchRegularizationData();
    }

    private void fetchRegularizationData() {
        updateFilterSummary();
        String token = SecurePrefManager.getInstance(requireContext()).getString("authToken", "");
        viewModel.fetchRegularizationRequests(token, currentStartDate, currentEndDate, "", 1, 50, "", "", false);
    }

    private void updateFilterSummary() {
        if (tvFilterSummary == null) return;

        if (currentStartDate == null || currentStartDate.isEmpty() || currentEndDate == null || currentEndDate.isEmpty()) {
            tvFilterSummary.setText("Showing all regularization requests");
            return;
        }

        try {
            String startFormatted = displayFormat.format(apiFormat.parse(currentStartDate));
            String endFormatted = displayFormat.format(apiFormat.parse(currentEndDate));
            tvFilterSummary.setText("Showing requests from " + startFormatted + " to " + endFormatted);
        } catch (Exception e) {
            tvFilterSummary.setText("Showing requests from " + currentStartDate + " to " + currentEndDate);
        }
    }
}