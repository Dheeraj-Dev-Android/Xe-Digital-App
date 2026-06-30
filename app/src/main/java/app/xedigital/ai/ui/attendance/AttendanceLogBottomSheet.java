package app.xedigital.ai.ui.attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.AttendanceLogAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.AttendanceLog.AttendanceLogResponse;
import app.xedigital.ai.model.AttendanceLog.AttendanceLogsItem;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceLogBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ATTENDANCE_ID = "attendance_id";
    private static final String ARG_PUNCH_DATE = "punch_date";

    private String attendanceId;
    private String punchDate;

    private ProgressBar progressBar;
    private RecyclerView logsRecyclerView;
    private TextView errorTextView;

    private APIInterface apiInterface;

    public static AttendanceLogBottomSheet newInstance(String attendanceId, String punchDate) {
        AttendanceLogBottomSheet fragment = new AttendanceLogBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_ATTENDANCE_ID, attendanceId);
        args.putString(ARG_PUNCH_DATE, punchDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            attendanceId = getArguments().getString(ARG_ATTENDANCE_ID);
            punchDate = getArguments().getString(ARG_PUNCH_DATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_attendance_logs, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        logsRecyclerView = view.findViewById(R.id.logsRecyclerView);
        errorTextView = view.findViewById(R.id.errorTextView);

        logsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchAttendanceLogs();

        return view;
    }

    private void fetchAttendanceLogs() {
        progressBar.setVisibility(View.VISIBLE);
        logsRecyclerView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);

//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", null);
        String userId = prefManager.getString("userId", null);
        String token = "jwt " + authToken;

        // Assuming you have a RetrofitInstance class built for your client setup
        apiInterface = APIClient.getInstance().getApi();

        apiInterface.getAttendanceLogs(token, userId, punchDate).enqueue(new Callback<AttendanceLogResponse>() {
            @Override
            public void onResponse(@NonNull Call<AttendanceLogResponse> call, @NonNull Response<AttendanceLogResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<AttendanceLogsItem> logs = response.body().getData().getAttendanceLogs();

                    if (logs != null && !logs.isEmpty()) {
                        logsRecyclerView.setVisibility(View.VISIBLE);
                        AttendanceLogAdapter adapter = new AttendanceLogAdapter(logs);
                        logsRecyclerView.setAdapter(adapter);
                    } else {
                        showError("No logs recorded for this day.");
                    }
                } else {
                    showError("Failed to fetch logs. Server error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AttendanceLogResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Network connection error: " + t.getLocalizedMessage());
            }
        });
    }

    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

}