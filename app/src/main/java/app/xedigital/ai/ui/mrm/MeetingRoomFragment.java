package app.xedigital.ai.ui.mrm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

import app.xedigital.ai.R;

public class MeetingRoomFragment extends Fragment {

    private static final String TAG = "MeetingRoomFragment";
    private MeetingRoomViewModel mViewModel;
    private TextView meetingsTextView;
    private TextView emptyTextView;
    private View emptyStateContainer;
    private ProgressBar progressBar;

    public static MeetingRoomFragment newInstance() {
        return new MeetingRoomFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting_room, container, false);

        // Bind Views
        meetingsTextView = view.findViewById(R.id.meetingsTextView);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        emptyTextView = view.findViewById(R.id.emptyStateText);
        progressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(MeetingRoomViewModel.class);
        setupObservers();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);

        if (authToken != null) {
            mViewModel.fetchMeetings(authToken);
        } else {
            Log.e(TAG, "Auth token not found in SharedPreferences");
            Toast.makeText(getContext(), "Authentication token is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            showEmptyState("Please login to see meetings.");
        }
    }

    private void setupObservers() {
        // Observe loading state
        mViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                meetingsTextView.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        // Observe successful API response
        // Observe successful API response
        mViewModel.getMeetingsLiveData().observe(getViewLifecycleOwner(), responseBody -> {
            try {
                String jsonResponse = responseBody.string();
                Log.d(TAG, "Meetings JSON Data: " + jsonResponse);

                // Check for empty conditions, including your backend's custom "No record found" structures
                if (jsonResponse.trim().isEmpty() || jsonResponse.equals("[]") || jsonResponse.equals("{}") || jsonResponse.contains("\"roomData\":[]") || jsonResponse.contains("No record found!!!")) {

                    showEmptyState("No meetings rooms found");
                } else {
                    emptyStateContainer.setVisibility(View.GONE);
                    meetingsTextView.setVisibility(View.VISIBLE);
                    meetingsTextView.setText(jsonResponse);
                }

            } catch (IOException e) {
                Log.e(TAG, "Error reading response body", e);
                showEmptyState("Error handling dynamic response data");
            }
        });

        // Observe network or API errors
        mViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            Log.e(TAG, "API Error: " + errorMessage);
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            showEmptyState("Error loading meetings: " + errorMessage);
        });
    }

    private void showEmptyState(String message) {
        if (meetingsTextView != null && emptyTextView != null) {
            meetingsTextView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(message);
        }
    }
}