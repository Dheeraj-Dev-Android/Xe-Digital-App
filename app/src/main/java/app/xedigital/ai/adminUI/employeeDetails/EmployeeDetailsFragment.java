package app.xedigital.ai.adminUI.employeeDetails;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeeDetailResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDetailsFragment extends Fragment {

    private EmployeeDetailsViewModel mViewModel;
    private String token;

    public static EmployeeDetailsFragment newInstance() {
        return new EmployeeDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_details, container, false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", null);

        fetchEmployees(token);
        visitorsCategories(token);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EmployeeDetailsViewModel.class);
        // TODO: Use the ViewModel
    }


    private void fetchEmployees(String token) {


        if (token == null) {
            Toast.makeText(getContext(), "Token not found", Toast.LENGTH_SHORT).show();
            return;
        }

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();

        Call<EmployeeDetailResponse> call = apiService.getEmployees("jwt " + token);
        call.enqueue(new Callback<EmployeeDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeDetailResponse> call, @NonNull Response<EmployeeDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
//                        String responseString = String.valueOf(response.body());

                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        String json = gson.toJson(response.body());
                        Log.e("EMPLOYEES_RESPONSE", json);
                        Toast.makeText(getContext(), "API Response: " + json, LENGTH_SHORT).show();

                    } catch (JsonSyntaxException e) {
                        Log.e("EMPLOYEES_RESPONSE", "Invalid JSON format");
                    }
                } else {
                    Toast.makeText(getContext(), "API Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeDetailResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void visitorsCategories(String token) {
        if (token == null) {
            Log.e("Token", "Token not found");
            return;
        }

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();

        Call<ResponseBody> call = apiService.getVisitorCategories("jwt " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
//                        String responseString = String.valueOf(response.body());

                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        String json = gson.toJson(response.body());
                        Toast.makeText(getContext(), "Visitors Categories API Response: " + json, LENGTH_SHORT).show();

                    } catch (JsonSyntaxException e) {
                        Log.e("Visitors Categories", "Invalid JSON format");
                    }
                } else {
                    Toast.makeText(getContext(), "API Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}