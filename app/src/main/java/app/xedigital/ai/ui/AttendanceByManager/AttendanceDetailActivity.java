package app.xedigital.ai.ui.AttendanceByManager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import app.xedigital.ai.R;
import app.xedigital.ai.model.AttandanceByManager.EmployeePunchDataItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AttendanceDetailActivity extends AppCompatActivity {

    private TextView dateTextView, punchInTime, punchOutTime;
    private TextView totalTime, overtimeTime, lateTime;
    private TextView shiftDetail, addressDetailIn, addressDetailOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_detail);

        initViews();

        // Retrieve the data passed from the Adapter
        EmployeePunchDataItem item = (EmployeePunchDataItem) getIntent().getSerializableExtra("attendance_item");

        if (item != null) {
            bindData(item);
        }
    }

    private void initViews() {
        dateTextView = findViewById(R.id.dateTextView);
        punchInTime = findViewById(R.id.punchInTime);
        punchOutTime = findViewById(R.id.punchOutTime);
        totalTime = findViewById(R.id.totalTime);
        overtimeTime = findViewById(R.id.overtimeTime);
        lateTime = findViewById(R.id.lateTime);
        shiftDetail = findViewById(R.id.shiftDetail);
        addressDetailIn = findViewById(R.id.addressDetailIn);
        addressDetailOut = findViewById(R.id.addressDetailOut);
    }

    private void bindData(EmployeePunchDataItem item) {
        // 1. Header Date
        dateTextView.setText(item.getPunchDateFormat());

        // 2. Punch Timings - Format time and handle nulls
        punchInTime.setText(DateTimeUtils.formatTime(item.getPunchIn()));

        String pOut = item.getPunchOut();
        punchOutTime.setText((pOut != null && !pOut.isEmpty()) ? DateTimeUtils.formatTime(pOut) : "N/A");

        // 3. Calculated Stats
        totalTime.setText(item.getTotalTime() != null ? item.getTotalTime() : "00:00");
        overtimeTime.setText(item.getOvertime() != null ? item.getOvertime() : "00:00");
        lateTime.setText(item.getLateTime() != null ? item.getLateTime() : "00:00");

        // 4. Shift Info
        if (item.getShift() != null) {
            String shiftRange = item.getShift().getStartTime() + " - " + item.getShift().getEndTime();
            shiftDetail.setText(shiftRange);
        }

        // 5. Addresses
        addressDetailIn.setText(getValidString(item.getPunchInAddress()));
        addressDetailOut.setText(getValidString(item.getPunchOutAddress()));
    }

    // Helper to prevent "null" text showing in UI
    private String getValidString(String val) {
        return (val == null || val.isEmpty()) ? "Not Available" : val;
    }

    // Optional: Support back button in header if you add one
    public void onBackClick(View view) {
        finish();
    }
}