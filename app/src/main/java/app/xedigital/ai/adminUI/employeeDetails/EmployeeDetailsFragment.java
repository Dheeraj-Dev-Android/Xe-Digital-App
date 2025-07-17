package app.xedigital.ai.adminUI.employeeDetails;

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

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adminAdapter.EmployeeAdapter;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;

public class EmployeeDetailsFragment extends Fragment {

    private final List<EmployeesItem> employeeList = new ArrayList<>();
    private final List<EmployeesItem> fullEmployeeList = new ArrayList<>();
    private EmployeeDetailsViewModel mViewModel;
    private String token;
    private RecyclerView rvEmployees;
    private EmployeeAdapter adapter;

    public static EmployeeDetailsFragment newInstance() {
        return new EmployeeDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_details, container, false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", null);

        rvEmployees = view.findViewById(R.id.rvEmployees);
        rvEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmployeeAdapter(getContext(), employeeList);
        rvEmployees.setAdapter(adapter);
        Chip chipAll = view.findViewById(R.id.chipAll);
        Chip chipActive = view.findViewById(R.id.chipActive);
        Chip chipInactive = view.findViewById(R.id.chipInactive);

// Set default selection to "All"
        chipAll.setChecked(true);

// Chip click listeners
        chipAll.setOnClickListener(v -> filterEmployees("ALL"));
        chipActive.setOnClickListener(v -> filterEmployees("ACTIVE"));
        chipInactive.setOnClickListener(v -> filterEmployees("INACTIVE"));

        EditText etSearch = view.findViewById(R.id.etSearchEmployee);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void filterByName(String query) {
        employeeList.clear();

        if (query.isEmpty()) {
            employeeList.addAll(fullEmployeeList);
        } else {
            for (EmployeesItem employee : fullEmployeeList) {
                if (employee.getFirstname() != null && employee.getFirstname().toLowerCase().contains(query.toLowerCase())) {
                    employeeList.add(employee);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EmployeeDetailsViewModel.class);

        mViewModel.getEmployeeList().observe(getViewLifecycleOwner(), employees -> {
            fullEmployeeList.clear();
            employeeList.clear();
            fullEmployeeList.addAll(employees);
            employeeList.addAll(employees);
            adapter.notifyDataSetChanged();
        });


        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());

        mViewModel.fetchEmployees(token);
    }

    private void filterEmployees(String filterType) {
        employeeList.clear();

        switch (filterType) {
            case "ALL":
                employeeList.addAll(fullEmployeeList);
                break;

            case "ACTIVE":
                for (EmployeesItem employee : fullEmployeeList) {
                    if (employee.isActive()) {
                        employeeList.add(employee);
                    }
                }
                break;

            case "INACTIVE":
                for (EmployeesItem employee : fullEmployeeList) {
                    if (!employee.isActive()) {
                        employeeList.add(employee);
                    }
                }
                break;
        }

        adapter.notifyDataSetChanged();
    }

}