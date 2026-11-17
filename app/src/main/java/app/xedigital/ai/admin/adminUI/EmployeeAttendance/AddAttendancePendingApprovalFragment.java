package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import java.util.Calendar;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.AddAttendanceApproval.AddAttendanceRegularizeAppliedItem;

public class AddAttendancePendingApprovalFragment extends Fragment implements AddAttendancePendingApprovalAdapter.OnActionListener, RegularizationFilterBottomSheet.FilterListener {

    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());

    private EditText etSearch;
    private ImageButton btnFilter;
    private TextView tvFilterSummary;
    private RecyclerView recyclerViewAddAttendance;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private AddAttendancePendingApprovalViewModel viewModel;
    private AddAttendancePendingApprovalAdapter adapter;

    private String currentStartDate = "";
    private String currentEndDate = "";

    public static AddAttendancePendingApprovalFragment newInstance() {
        return new AddAttendancePendingApprovalFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_attendance_pending_approval, container, false);
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
        recyclerViewAddAttendance = view.findViewById(R.id.recyclerViewAddAttendance);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(AddAttendancePendingApprovalViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new AddAttendancePendingApprovalAdapter(null, this);
        recyclerViewAddAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewAddAttendance.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s != null ? s.toString() : "");
                    checkEmptyState();
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
        sheet.show(getChildFragmentManager(), "RegularizationFilterBottomSheet");
    }

    @Override
    public void onFilterApplied(String startDate, String endDate) {
        currentStartDate = startDate;
        currentEndDate = endDate;
        applyFilters();
    }

    @Override
    public void onFilterReset() {
        setDefaultDates();
        applyFilters();
    }

    private void observeLiveData() {
        viewModel.getAttendanceList().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                adapter.updateList(items);
                applyFilters();
            } else {
                adapter.updateList(null);
                checkEmptyState();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerViewAddAttendance.setVisibility(View.GONE);
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
        updateFilterSummary();
        viewModel.fetchAddAttendancePendingApprovalList();
    }

    private void applyFilters() {
        updateFilterSummary();
        if (adapter != null) {
            adapter.filterByDateRange(currentStartDate, currentEndDate, "", "");
            if (etSearch != null && etSearch.getText() != null) {
                adapter.filter(etSearch.getText().toString());
            }
        }
        checkEmptyState();
    }

    private void updateFilterSummary() {
        if (tvFilterSummary == null) return;
        if (TextUtils.isEmpty(currentStartDate) || TextUtils.isEmpty(currentEndDate)) {
            tvFilterSummary.setText("Showing all add attendance requests");
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

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerViewAddAttendance.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerViewAddAttendance.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onApprove(AddAttendanceRegularizeAppliedItem item) {
        String empName = (item.getEmployee() != null && !TextUtils.isEmpty(item.getEmployee().getFullname())) ? item.getEmployee().getFullname() : "";
        Toast.makeText(requireContext(), "Approve clicked for " + empName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReject(AddAttendanceRegularizeAppliedItem item) {
        String empName = (item.getEmployee() != null && !TextUtils.isEmpty(item.getEmployee().getFullname())) ? item.getEmployee().getFullname() : "";
        Toast.makeText(requireContext(), "Reject clicked for " + empName, Toast.LENGTH_SHORT).show();
    }
}