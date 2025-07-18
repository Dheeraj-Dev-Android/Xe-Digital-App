package app.xedigital.ai.adminUI.Visitor;

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

public class VisitorsDetailsFragment extends Fragment {

    private final List<VisitorsItem> fullVisitorList = new ArrayList<>();
    private final List<VisitorsItem> filteredVisitorList = new ArrayList<>();
    private VisitorsDetailsViewModel mViewModel;
    private VisitorAdapter adapter;
    private RecyclerView rvVisitors;
    private EditText vsSearchEmployee;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visitors_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvVisitors = view.findViewById(R.id.rvVisitors);
        vsSearchEmployee = view.findViewById(R.id.vsSearchEmployee);
        rvVisitors.setLayoutManager(new LinearLayoutManager(requireContext()));

        mViewModel = new ViewModelProvider(this).get(VisitorsDetailsViewModel.class);

        String authToken = getAuthToken();
        if (authToken != null) {
            mViewModel.getVisitors("jwt " + authToken).observe(getViewLifecycleOwner(), visitors -> {
                if (visitors != null && !visitors.isEmpty()) {
                    fullVisitorList.clear();
                    filteredVisitorList.clear();
                    fullVisitorList.addAll(visitors);
                    filteredVisitorList.addAll(visitors);
                    adapter = new VisitorAdapter(requireContext(), filteredVisitorList);
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
        adapter.notifyDataSetChanged();
    }

    private String getAuthToken() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        return sharedPreferences.getString("authToken", null);
    }
}
