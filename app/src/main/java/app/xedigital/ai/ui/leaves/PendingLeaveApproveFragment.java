package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.ui.profile.ProfileViewModel;


public class PendingLeaveApproveFragment extends Fragment {
    public static final String ARG_LEAVE_ID = "leave_id";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private APIInterface apiInterface;
    private AppliedLeavesItem item;
    private String leaveId;
    //    private LeaveApprovalBinding binding;
    private ProfileViewModel profileViewModel;

    public PendingLeaveApproveFragment() {
        // Required empty public constructor
    }

    public static PendingLeaveApproveFragment newInstance(AppliedLeavesItem item) {
        PendingLeaveApproveFragment fragment = new PendingLeaveApproveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LEAVE_ID, String.valueOf(item));
        fragment.setArguments(args);

        return fragment;
    }

    public static String getCurrentDateTimeInUTC() {
        Date currentDateTime = new Date();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDateTime = dateTimeFormat.format(currentDateTime);
        Log.d("LeaveApprovalAdapter", "getCurrentDateTimeInUTC: " + formattedDateTime);
        return formattedDateTime;

    }
//    public void setListener(PendingApprovalViewFragment.OnRegularizeApprovalActionListener listener) {
//        this.listener = listener;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            leaveId = getArguments().getString(ARG_LEAVE_ID);
            item = (AppliedLeavesItem) getArguments().getSerializable(ARG_LEAVE_ID);
        }
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        apiInterface = APIClient.getInstance().UpdateLeaveListApproval();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        String authToken = sharedPreferences.getString("authToken", "");

        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pending_leave_approve, container, false);
    }

    public void handleApprove(String id) {
    }

    public void handleReject(String id) {
    }
}