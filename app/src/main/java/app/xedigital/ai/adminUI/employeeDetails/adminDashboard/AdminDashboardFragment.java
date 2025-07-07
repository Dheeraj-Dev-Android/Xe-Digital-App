package app.xedigital.ai.adminUI.employeeDetails.adminDashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import app.xedigital.ai.R;


public class AdminDashboardFragment extends Fragment {

    private AdminDashboardViewModel mViewModel;

    private String token;
    private TextView totalSignin, totalSignout, totalEmployees, totalBranches;


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
        totalSignin = view.findViewById(R.id.signinCount);
        totalSignout = view.findViewById(R.id.signoutCount);
        totalEmployees = view.findViewById(R.id.employeesCount);
        totalBranches = view.findViewById(R.id.branchesCount);

        mViewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

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


        String authToken = "jwt " + token;
        mViewModel.fetchDashboardData(authToken);
    }
}
