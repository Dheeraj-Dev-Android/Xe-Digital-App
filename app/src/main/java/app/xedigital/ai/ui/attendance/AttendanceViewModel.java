package app.xedigital.ai.ui.attendance;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.attendance.EmployeeAttendanceResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceViewModel extends ViewModel {

    private static final String TAG = "AttendanceViewModel";
    private final MutableLiveData<EmployeeAttendanceResponse> _attendance = new MutableLiveData<>();
    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    private final APIInterface apiInterface;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final MutableLiveData<Boolean> showNoDataAlert = new MutableLiveData<>(false);
    public LiveData<EmployeeAttendanceResponse> attendance = _attendance;
    public LiveData<String> toastMessage = _toastMessage;
    private String authToken;

    public AttendanceViewModel() {
        apiInterface = APIClient.getInstance().getAttendance();
    }

    public void showToastMessage(String message) {
        _toastMessage.setValue(message);
    }

    public void storeLoginData(String authToken) {
        this.authToken = authToken;
    }

    public void fetchAttendance(String startDate, String endDate) {
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

            fetchEmployeeAttendance(startDate, endDate);
        } else {
            fetchEmployeeAttendance(startDate, endDate);
        }
    }

    public LiveData<Boolean> getShowNoDataAlert() {
        return showNoDataAlert;
    }

    public void fetchEmployeeAttendance(String startDate, String endDate) {
        if (authToken != null) {
            new Thread(() -> {
                String authHeaderValue = "jwt " + authToken;
                apiInterface.getAttendance(authHeaderValue, startDate, endDate, "", "", "", "", "", "").enqueue(new Callback<EmployeeAttendanceResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EmployeeAttendanceResponse> call, @NonNull Response<EmployeeAttendanceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EmployeeAttendanceResponse responseBody = response.body();

                            // Post the response body to LiveData safely
                            mainHandler.post(() -> _attendance.setValue(responseBody));

                            // Show the date range toast message
                            mainHandler.post(() -> {
                                String message = "Attendance Showing from " + startDate + " to " + endDate;
                                showToastMessage(message);
                            });

                            // 1. Ensure responseBody.getData() is not null
                            // 2. Ensure responseBody.getData().getEmployeePunchData() is not null
                            if (responseBody.getData() != null && responseBody.getData().getEmployeePunchData() != null) {
                                if (responseBody.getData().getEmployeePunchData().isEmpty()) {
                                    // List is empty -> Alert the user
                                    mainHandler.post(() -> showNoDataAlert.setValue(true));
                                } else {
                                    // List has items -> Dismiss any alerts
                                    mainHandler.post(() -> showNoDataAlert.setValue(false));
                                }
                            } else {
                                // If data or employeePunchData is missing entirely from the payload,
                                // treat it safely as "No Data Available" instead of throwing a crash.
                                mainHandler.post(() -> showNoDataAlert.setValue(true));
                            }

                        } else {
                            Log.e(TAG, "API Error Response: Code " + response.code());
                            System.err.println("API Error : " + response.code());

                            // Trigger no-data alert since the API call failed to return content
                            mainHandler.post(() -> showNoDataAlert.setValue(true));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EmployeeAttendanceResponse> call, @NonNull Throwable throwable) {
                        System.err.println("Error: " + throwable.getMessage());
                        // In case of network drops or timeout errors, trigger the no data alert state
                        mainHandler.post(() -> showNoDataAlert.setValue(true));
                    }
                });
            }).start();
        } else {
            Log.e(TAG, "Error:authToken is null");
            System.err.println("Error:authToken is null");
        }
    }
}