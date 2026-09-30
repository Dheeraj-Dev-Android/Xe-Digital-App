package app.xedigital.ai.ui.mrm;

import android.app.DatePickerDialog;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.MeetingRoomBookedSlotsResponse.DataByIdItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class MeetingRoomBookedSlotsFragment extends Fragment {

    private static final String TAG = "BookedSlotsFragment";
    private static final String ARG_ROOM_ID = "arg_room_id";
    private static final String ARG_ROOM_NAME = "arg_room_name";

    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private MeetingRoomBookedSlotsViewModel mViewModel;
    private BookedSlotsAdapter adapter;

    private RecyclerView recyclerView;
    private View emptyStateContainer;
    private TextView emptyStateText;
    private ProgressBar progressBar;

    private MaterialButton btnBookMeetingRoom;
    private MaterialCardView btnFilter;
    private View filterActiveDot;
    private Chip chipActiveFilter;

    private String roomId;
    private String roomName;
    private String authToken;

    // Filter state
    private Date fromDate = null;
    private Date toDate = null;

    // Full list from API
    private List<DataByIdItem> allSlots = new ArrayList<>();

    public static MeetingRoomBookedSlotsFragment newInstance(String roomId, String roomName) {
        MeetingRoomBookedSlotsFragment f = new MeetingRoomBookedSlotsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        args.putString(ARG_ROOM_NAME, roomName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomId = getArguments().getString(ARG_ROOM_ID);
            roomName = getArguments().getString(ARG_ROOM_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting_room_booked_slots, container, false);

        recyclerView = view.findViewById(R.id.bookedSlotsRecyclerView);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        progressBar = view.findViewById(R.id.progressBar);

        btnBookMeetingRoom = view.findViewById(R.id.btnBookMeetingRoom);
        btnFilter = view.findViewById(R.id.btnFilter);
        filterActiveDot = view.findViewById(R.id.filterActiveDot);
        chipActiveFilter = view.findViewById(R.id.chipActiveFilter);

        adapter = new BookedSlotsAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(MeetingRoomBookedSlotsViewModel.class);
        setupObservers();
        setupClickListeners();

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", null);

        if (authToken == null) {
            Toast.makeText(getContext(), "Authentication token missing. Please log in again.", Toast.LENGTH_SHORT).show();
            showEmptyState("Please log in to see booked slots.");
            return;
        }

        if (roomId == null || roomId.trim().isEmpty()) {
            showEmptyState("Invalid room selected.");
            return;
        }

        mViewModel.fetchBookedSlots(authToken, roomId);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh list when user returns from booking form
        if (authToken != null && roomId != null) {
            mViewModel.fetchBookedSlots(authToken, roomId);
        }
    }

    private void setupClickListeners() {
        // Navigate to the Book Meeting Room Fragment
        btnBookMeetingRoom.setOnClickListener(v -> navigateToBookingForm());

        btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        chipActiveFilter.setOnCloseIconClickListener(v -> clearFilter());
    }

    // ─────────────────────────────────────────────
    // Navigate to Booking Form
    // ─────────────────────────────────────────────
    private void navigateToBookingForm() {
        if (roomId == null || roomId.trim().isEmpty()) {
            Toast.makeText(getContext(), "Room information missing", Toast.LENGTH_SHORT).show();
            return;
        }

        BookMeetingRoomFragment fragment = BookMeetingRoomFragment.newInstance(roomId, roomName);

        // Replace with the container ID used in your Activity
        // Common: R.id.nav_host_fragment_content_main OR R.id.fragment_container
        getParentFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right).replace(((ViewGroup) requireView().getParent()).getId(), fragment).addToBackStack("BookMeetingRoom").commit();
    }

    private void setupObservers() {
        mViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        mViewModel.getBookedSlotsLiveData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null && response.getData().getDataById() != null) {
                allSlots = response.getData().getDataById();
                applyFilterAndRender();
            } else {
                allSlots = new ArrayList<>();
                showEmptyState("No booked slots found.");
            }
        });

        mViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            Log.e(TAG, "API Error: " + errorMessage);
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            showEmptyState("Error loading slots: " + errorMessage);
        });
    }

    // ─────────────────────────────────────────────
    // Filter BottomSheet
    // ─────────────────────────────────────────────
    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_filter_meeting, null);
        dialog.setContentView(sheet);

        TextInputEditText etFrom = sheet.findViewById(R.id.etFromDate);
        TextInputEditText etTo = sheet.findViewById(R.id.etToDate);
        ChipGroup chipGroup = sheet.findViewById(R.id.chipGroupQuickFilter);
        Chip chipToday = sheet.findViewById(R.id.chipToday);
        Chip chipWeek = sheet.findViewById(R.id.chipWeek);
        Chip chipMonth = sheet.findViewById(R.id.chipMonth);
        MaterialButton btnClear = sheet.findViewById(R.id.btnClearFilter);
        MaterialButton btnApply = sheet.findViewById(R.id.btnApplyFilter);
        View btnClose = sheet.findViewById(R.id.btnCloseSheet);

        final Date[] tempFrom = {fromDate};
        final Date[] tempTo = {toDate};

        if (tempFrom[0] != null) etFrom.setText(displayFormat.format(tempFrom[0]));
        if (tempTo[0] != null) etTo.setText(displayFormat.format(tempTo[0]));

        etFrom.setOnClickListener(v -> pickDate(tempFrom[0], date -> {
            tempFrom[0] = date;
            etFrom.setText(displayFormat.format(date));
            chipGroup.clearCheck();
        }));

        etTo.setOnClickListener(v -> pickDate(tempTo[0], date -> {
            tempTo[0] = date;
            etTo.setText(displayFormat.format(date));
            chipGroup.clearCheck();
        }));

        chipToday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            Date d = c.getTime();
            tempFrom[0] = d;
            tempTo[0] = d;
            etFrom.setText(displayFormat.format(d));
            etTo.setText(displayFormat.format(d));
        });

        chipWeek.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
            Date start = c.getTime();
            c.add(Calendar.DAY_OF_MONTH, 6);
            Date end = c.getTime();
            tempFrom[0] = start;
            tempTo[0] = end;
            etFrom.setText(displayFormat.format(start));
            etTo.setText(displayFormat.format(end));
        });

        chipMonth.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            Date start = c.getTime();
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date end = c.getTime();
            tempFrom[0] = start;
            tempTo[0] = end;
            etFrom.setText(displayFormat.format(start));
            etTo.setText(displayFormat.format(end));
        });

        btnClear.setOnClickListener(v -> {
            tempFrom[0] = null;
            tempTo[0] = null;
            etFrom.setText("");
            etTo.setText("");
            chipGroup.clearCheck();
        });

        btnApply.setOnClickListener(v -> {
            if (tempFrom[0] != null && tempTo[0] != null && tempFrom[0].after(tempTo[0])) {
                Toast.makeText(getContext(), "From date cannot be after To date", Toast.LENGTH_SHORT).show();
                return;
            }
            fromDate = tempFrom[0];
            toDate = tempTo[0];
            applyFilterAndRender();
            updateFilterIndicator();
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void pickDate(@Nullable Date initial, DateSelectedCallback cb) {
        Calendar c = Calendar.getInstance();
        if (initial != null) c.setTime(initial);

        DatePickerDialog dp = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day, 0, 0, 0);
            picked.set(Calendar.MILLISECOND, 0);
            cb.onDate(picked.getTime());
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void clearFilter() {
        fromDate = null;
        toDate = null;
        updateFilterIndicator();
        applyFilterAndRender();
    }

    private void updateFilterIndicator() {
        boolean active = fromDate != null || toDate != null;
        filterActiveDot.setVisibility(active ? View.VISIBLE : View.GONE);
        chipActiveFilter.setVisibility(active ? View.VISIBLE : View.GONE);

        if (active) {
            String from = fromDate != null ? displayFormat.format(fromDate) : "Any";
            String to = toDate != null ? displayFormat.format(toDate) : "Any";
            chipActiveFilter.setText(from + " → " + to);
        }
    }

    // ─────────────────────────────────────────────
    // Filtering Logic
    // ─────────────────────────────────────────────
    private void applyFilterAndRender() {
        if (allSlots == null || allSlots.isEmpty()) {
            showEmptyState("No booked slots found.");
            return;
        }

        List<DataByIdItem> filtered = new ArrayList<>();
        for (DataByIdItem item : allSlots) {
            Date d = parseSlotDate(item.getSelectDate());
            if (isInRange(d)) filtered.add(item);
        }

        if (filtered.isEmpty()) {
            showEmptyState("No bookings found in the selected date range.");
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setItems(filtered);
        }
    }

    private boolean isInRange(Date d) {
        if (fromDate == null && toDate == null) return true;
        if (d == null) return false;

        Date start = fromDate != null ? startOfDay(fromDate) : null;
        Date end = toDate != null ? endOfDay(toDate) : null;

        if (start != null && d.before(start)) return false;
        return end == null || !d.after(end);
    }

    private Date startOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private Date endOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    private Date parseSlotDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                Date d = sdf.parse(raw);
                if (d != null) return d;
            } catch (Exception ignored) {
            }
        }
        Log.w(TAG, "Unparseable date: " + raw);
        return null;
    }

    private void showEmptyState(String message) {
        if (recyclerView != null && emptyStateContainer != null) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
            emptyStateText.setText(message);
        }
    }

    private interface DateSelectedCallback {
        void onDate(Date date);
    }
}