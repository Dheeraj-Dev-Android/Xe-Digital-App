package app.xedigital.ai.ui.mrm;

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
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.meetingRoom.RoomDataItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class MeetingRoomFragment extends Fragment {

    private static final String TAG = "MeetingRoomFragment";
    private MeetingRoomViewModel mViewModel;

    private RecyclerView recyclerView;
    private MeetingRoomAdapter adapter;

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
        recyclerView = view.findViewById(R.id.meetingsRecyclerView);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        emptyTextView = view.findViewById(R.id.emptyStateText);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        adapter = new MeetingRoomAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(MeetingRoomViewModel.class);
        setupObservers();

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", null);

        if (authToken != null) {
            mViewModel.fetchMeetings(authToken);
        } else {
            Toast.makeText(getContext(), "Authentication token is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            showEmptyState("Please login to see meetings.");
        }
    }

    private void setupObservers() {
        // Observe loading state
        mViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        // Observe successful structural API model objects
        mViewModel.getMeetingsLiveData().observe(getViewLifecycleOwner(), responseBody -> {
            if (responseBody != null && responseBody.getData() != null && responseBody.getData().getRoomData() != null) {
                List<RoomDataItem> rooms = responseBody.getData().getRoomData();

                if (rooms.isEmpty()) {
                    showEmptyState("No meeting rooms found");
                } else {
                    emptyStateContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setRooms(rooms);
                }
            } else {
                showEmptyState("No meeting rooms found");
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
        if (recyclerView != null && emptyTextView != null) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(message);
        }
    }
}