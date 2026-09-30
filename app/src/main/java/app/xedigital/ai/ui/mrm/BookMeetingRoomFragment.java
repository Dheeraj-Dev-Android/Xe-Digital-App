package app.xedigital.ai.ui.mrm;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.MeetingRoomBookedSlotsResponse.DataByIdItem;
import app.xedigital.ai.model.meetingRoom.MeetingRoomBookingRequest;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.utills.SecurePrefManager;

public class BookMeetingRoomFragment extends Fragment {

    private static final String TAG = "BookMeetingRoomFragment";
    private static final String ARG_ROOM_ID = "arg_room_id";
    private static final String ARG_ROOM_NAME = "arg_room_name";

    // Full-day slot configuration
    private static final int WORK_START_HOUR = 0;
    private static final int WORK_END_HOUR = 24;
    private static final int SLOT_INTERVAL_MIN = 30;

    // ─────────────────────────────────────────────
    // Static formatters — instantiated ONCE for entire app lifecycle.
    // Prevents heavy SimpleDateFormat re-creation on every date/time click.
    // ─────────────────────────────────────────────
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT =
            new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private static final SimpleDateFormat API_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final List<int[]> bookedRanges = new ArrayList<>();

    // Args
    private String roomId;
    private String roomName;

    // Auth
    private String authToken;
    private String userId;

    // Host details
    private String hostId, hostName, hostEmail, hostContact;

    // Time selection state
    private int selectedStartMinutes = -1;
    private int selectedEndMinutes = -1;

    // ViewModel
    private BookMeetingRoomViewModel mViewModel;

    // Views
    private TextInputEditText etMeetingRoom, etSelectDate, etStartTime, etEndTime,
            etHost, etVisitorName, etVisitorCount, etVisitorContact,
            etVisitorCompany, etPurpose;

    private TextInputLayout tilSelectDate, tilStartTime, tilEndTime,
            tilHost, tilVisitorName, tilVisitorCount, tilVisitorContact,
            tilVisitorCompany, tilPurpose;

    private View loadingOverlay;
    private TextView textLoadingMsg;
    private MaterialButton btnCancel, btnSubmit;

    public static BookMeetingRoomFragment newInstance(String roomId, String roomName) {
        BookMeetingRoomFragment fragment = new BookMeetingRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        args.putString(ARG_ROOM_NAME, roomName);
        fragment.setArguments(args);
        return fragment;
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_meeting_room, container, false);
        bindViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", null);
        userId = prefManager.getString("userId", null);

        mViewModel = new ViewModelProvider(this).get(BookMeetingRoomViewModel.class);
        setupObservers();

        prefillReadonlyFields();
        setupClickListeners();
        setupTextWatchers();

        loadHostProfile();
    }

    // ─────────────────────────────────────────────
    // View Binding
    // ─────────────────────────────────────────────
    private void bindViews(View view) {
        etMeetingRoom = view.findViewById(R.id.etMeetingRoom);
        etSelectDate = view.findViewById(R.id.etSelectDate);
        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        etHost = view.findViewById(R.id.etHost);
        etVisitorName = view.findViewById(R.id.etVisitorName);
        etVisitorCount = view.findViewById(R.id.etVisitorCount);
        etVisitorContact = view.findViewById(R.id.etVisitorContact);
        etVisitorCompany = view.findViewById(R.id.etVisitorCompany);
        etPurpose = view.findViewById(R.id.etPurpose);

        tilSelectDate = view.findViewById(R.id.tilSelectDate);
        tilStartTime = view.findViewById(R.id.tilStartTime);
        tilEndTime = view.findViewById(R.id.tilEndTime);
        tilHost = view.findViewById(R.id.tilHost);
        tilVisitorName = view.findViewById(R.id.tilVisitorName);
        tilVisitorCount = view.findViewById(R.id.tilVisitorCount);
        tilVisitorContact = view.findViewById(R.id.tilVisitorContact);
        tilVisitorCompany = view.findViewById(R.id.tilVisitorCompany);
        tilPurpose = view.findViewById(R.id.tilPurpose);

        btnCancel = view.findViewById(R.id.btnCancelBooking);
        btnSubmit = view.findViewById(R.id.btnSubmitBooking);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        textLoadingMsg = view.findViewById(R.id.textLoadingMsg);
    }

    private void prefillReadonlyFields() {
        if (etMeetingRoom != null) {
            etMeetingRoom.setText(roomName != null ? roomName : "Meeting Room");
        }
        if (etHost != null) {
            etHost.setText("Loading host...");
        }
    }

    // ─────────────────────────────────────────────
    // Observers
    // ─────────────────────────────────────────────
    private void setupObservers() {
        mViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                showOverlayLoading(isLoading, "Loading form...");
            }
        });

        mViewModel.getIsBookingSubmittingLiveData().observe(getViewLifecycleOwner(), isSubmitting -> {
            if (isSubmitting != null) {
                showOverlayLoading(isSubmitting, "Reserving Room...");
            }
        });

        mViewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null
                    && response.getData().getEmployee() != null) {
                Employee e = response.getData().getEmployee();
                hostId = safe(e.getId());
                hostName = buildFullName(e.getFirstname(), e.getLastname());
                hostEmail = safe(e.getEmail());
                hostContact = safe(e.getContact());
                if (etHost != null) etHost.setText(hostName);
                if (tilHost != null) tilHost.setError(null);
            } else {
                if (etHost != null) etHost.setText("");
                if (tilHost != null) tilHost.setError("Failed to load host details");
            }
        });

        mViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            Log.e(TAG, "Host API Error: " + errorMessage);
            if (etHost != null) etHost.setText("Tap sync icon to retry");
            if (tilHost != null) tilHost.setError(errorMessage);
        });

        mViewModel.getBookedSlotsLiveData().observe(getViewLifecycleOwner(), response -> {
            processBookedSlots(response == null ? null
                    : (response.getData() == null ? null
                    : response.getData().getDataById()));
        });

        mViewModel.getBookedSlotsErrorLiveData().observe(getViewLifecycleOwner(), err -> {
            Log.w(TAG, "Booked slots fetch error: " + err);
            bookedRanges.clear();
        });

        mViewModel.getBookingSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(),
                        "Room reserved successfully!",
                        Toast.LENGTH_LONG).show();
                mViewModel.resetBookingStatus();
                navigateBack();
            }
        });

        mViewModel.getBookingErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Log.e(TAG, "Booking API Error: " + errorMessage);
                Toast.makeText(getContext(),
                        "Booking failed: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                mViewModel.resetBookingStatus();
            }
        });
    }

    private void loadHostProfile() {
        if (authToken == null || userId == null) {
            if (etHost != null) etHost.setText("Login required");
            if (tilHost != null) tilHost.setError("User not authenticated");
            return;
        }
        if (etHost != null) etHost.setText("Loading host...");
        if (tilHost != null) tilHost.setError(null);
        mViewModel.fetchUserProfile(userId, authToken);
    }

    // ─────────────────────────────────────────────
    // Click Listeners
    // ─────────────────────────────────────────────
    private void setupClickListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> navigateBack());
        }
        if (etSelectDate != null) {
            etSelectDate.setOnClickListener(v -> showDatePicker());
        }
        if (etStartTime != null) {
            etStartTime.setOnClickListener(v -> openStartTimePicker());
        }
        if (etEndTime != null) {
            etEndTime.setOnClickListener(v -> openEndTimePicker());
        }
        if (tilHost != null) {
            tilHost.setEndIconOnClickListener(v -> loadHostProfile());
        }
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                if (validateForm()) submitBooking();
            });
        }
    }

    private void setupTextWatchers() {
        if (etVisitorName != null) {
            etVisitorName.addTextChangedListener(new SimpleTextWatcher(
                    () -> tilVisitorName.setError(null)));
        }
        if (etVisitorCount != null) {
            etVisitorCount.addTextChangedListener(new SimpleTextWatcher(
                    () -> tilVisitorCount.setError(null)));
        }
        if (etVisitorContact != null) {
            etVisitorContact.addTextChangedListener(new SimpleTextWatcher(
                    () -> tilVisitorContact.setError(null)));
        }
        if (etVisitorCompany != null) {
            etVisitorCompany.addTextChangedListener(new SimpleTextWatcher(
                    () -> tilVisitorCompany.setError(null)));
        }
        if (etPurpose != null) {
            etPurpose.addTextChangedListener(new SimpleTextWatcher(
                    () -> tilPurpose.setError(null)));
        }
    }

    // ─────────────────────────────────────────────
    // Date Picker
    // ─────────────────────────────────────────────
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, day);
                    if (etSelectDate != null) {
                        etSelectDate.setText(DISPLAY_DATE_FORMAT.format(picked.getTime()));
                    }
                    if (tilSelectDate != null) tilSelectDate.setError(null);

                    resetTimeSelections();

                    if (authToken != null && roomId != null) {
                        mViewModel.fetchBookedSlots(authToken, roomId);
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dp.show();
    }

    private void resetTimeSelections() {
        selectedStartMinutes = -1;
        selectedEndMinutes = -1;
        if (etStartTime != null) etStartTime.setText("");
        if (etEndTime != null) etEndTime.setText("");
        if (tilStartTime != null) tilStartTime.setError(null);
        if (tilEndTime != null) tilEndTime.setError(null);
    }

    // ─────────────────────────────────────────────
    // Time Slot Pickers
    // ─────────────────────────────────────────────
    private void openStartTimePicker() {
        if (isEmpty(etSelectDate)) {
            Toast.makeText(getContext(),
                    "Please select a date first", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TimeSlot> slots = generateTimeSlots(WORK_START_HOUR * 60,
                (WORK_END_HOUR * 60) - SLOT_INTERVAL_MIN);
        markBookedSlots(slots);

        TimeSlotBottomSheet sheet = TimeSlotBottomSheet.newInstance(
                "Select Start Time", slots, (time, totalMinutes) -> {
                    selectedStartMinutes = totalMinutes;
                    if (etStartTime != null) etStartTime.setText(time);
                    if (tilStartTime != null) tilStartTime.setError(null);

                    if (selectedEndMinutes != -1
                            && selectedEndMinutes <= selectedStartMinutes) {
                        selectedEndMinutes = -1;
                        if (etEndTime != null) etEndTime.setText("");
                    }
                });
        sheet.show(getParentFragmentManager(), "StartTimePicker");
    }

    private void openEndTimePicker() {
        if (isEmpty(etSelectDate)) {
            Toast.makeText(getContext(),
                    "Please select a date first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedStartMinutes == -1) {
            Toast.makeText(getContext(),
                    "Please select a start time first", Toast.LENGTH_SHORT).show();
            return;
        }

        int rangeStart = selectedStartMinutes + SLOT_INTERVAL_MIN;
        int rangeEnd = WORK_END_HOUR * 60;

        List<TimeSlot> slots = generateTimeSlotsForEnd(rangeStart, rangeEnd);
        markBookedSlotsForEnd(slots, selectedStartMinutes);

        TimeSlotBottomSheet sheet = TimeSlotBottomSheet.newInstance(
                "Select End Time", slots, (time, totalMinutes) -> {
                    selectedEndMinutes = totalMinutes;
                    if (etEndTime != null) etEndTime.setText(time);
                    if (tilEndTime != null) tilEndTime.setError(null);
                });
        sheet.show(getParentFragmentManager(), "EndTimePicker");
    }

    // ─────────────────────────────────────────────
    // Slot Generation
    // ─────────────────────────────────────────────
    private List<TimeSlot> generateTimeSlots(int fromMinutes, int toMinutes) {
        List<TimeSlot> list = new ArrayList<>();
        for (int m = fromMinutes; m <= toMinutes; m += SLOT_INTERVAL_MIN) {
            int hour = m / 60;
            int minute = m % 60;
            list.add(new TimeSlot(hour, minute));
        }
        return list;
    }

    private List<TimeSlot> generateTimeSlotsForEnd(int fromMinutes, int toMinutes) {
        List<TimeSlot> list = new ArrayList<>();
        int maxCapped = Math.min(toMinutes, 23 * 60 + 30);
        for (int m = fromMinutes; m <= maxCapped; m += SLOT_INTERVAL_MIN) {
            int hour = m / 60;
            int minute = m % 60;
            list.add(new TimeSlot(hour, minute));
        }
        return list;
    }

    // ─────────────────────────────────────────────
    // Booked Slot Marking
    // ─────────────────────────────────────────────
    private void markBookedSlots(List<TimeSlot> slots) {
        for (TimeSlot slot : slots) {
            for (int[] range : bookedRanges) {
                if (slot.getTotalMinutes() >= range[0]
                        && slot.getTotalMinutes() < range[1]) {
                    slot.setBooked(true);
                    break;
                }
            }
        }
    }

    private void markBookedSlotsForEnd(List<TimeSlot> slots, int startMin) {
        int firstBookedStart = Integer.MAX_VALUE;
        for (int[] range : bookedRanges) {
            if (range[0] > startMin && range[0] < firstBookedStart) {
                firstBookedStart = range[0];
            }
        }

        for (TimeSlot slot : slots) {
            for (int[] range : bookedRanges) {
                if (slot.getTotalMinutes() > range[0]
                        && slot.getTotalMinutes() <= range[1]) {
                    slot.setBooked(true);
                    break;
                }
            }
            if (firstBookedStart != Integer.MAX_VALUE
                    && slot.getTotalMinutes() > firstBookedStart) {
                slot.setEnabled(false);
            }
        }
    }

    private void processBookedSlots(@Nullable List<DataByIdItem> items) {
        bookedRanges.clear();
        if (items == null || items.isEmpty()) return;

        String selectedDate = etSelectDate.getText() == null ? ""
                : etSelectDate.getText().toString().trim();
        if (selectedDate.isEmpty()) return;

        String selectedDateApiFormat = convertDateToApiFormat(selectedDate);
        if (selectedDateApiFormat == null) return;

        for (DataByIdItem item : items) {
            String apiDate = item.getSelectDate();
            if (apiDate == null) continue;

            String apiDateOnly = apiDate.length() >= 10
                    ? apiDate.substring(0, 10) : apiDate;

            if (apiDateOnly.equals(selectedDateApiFormat)) {
                int startMin = parseTimeToMinutes(item.getStartTime());
                int endMin = parseTimeToMinutes(item.getEndTime());
                if (startMin >= 0 && endMin > startMin) {
                    bookedRanges.add(new int[]{startMin, endMin});
                }
            }
        }
    }

    /**
     * OPTIMIZED — reuses static class-level SimpleDateFormats.
     * No allocation per call.
     */
    private String convertDateToApiFormat(String ddMMyyyy) {
        if (ddMMyyyy == null || ddMMyyyy.trim().isEmpty()) return null;
        try {
            Date d = DISPLAY_DATE_FORMAT.parse(ddMMyyyy);
            return d != null ? API_DATE_FORMAT.format(d) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private int parseTimeToMinutes(String time) {
        if (time == null || time.trim().isEmpty()) return -1;
        try {
            String[] parts = time.trim().split(":");
            if (parts.length < 2) return -1;
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            return h * 60 + m;
        } catch (Exception e) {
            return -1;
        }
    }

    // ─────────────────────────────────────────────
    // Form Validation
    // ─────────────────────────────────────────────
    private boolean validateForm() {
        boolean isValid = true;

        if (hostId == null || hostName == null || hostName.isEmpty()) {
            Toast.makeText(getContext(),
                    "Host details not loaded. Please retry.",
                    Toast.LENGTH_SHORT).show();
            if (tilHost != null) tilHost.setError("Host not loaded");
            return false;
        }

        if (isEmpty(etSelectDate)) {
            if (tilSelectDate != null) tilSelectDate.setError("Please select a date");
            isValid = false;
        }
        if (isEmpty(etStartTime) || selectedStartMinutes == -1) {
            if (tilStartTime != null) tilStartTime.setError("Required");
            isValid = false;
        }
        if (isEmpty(etEndTime) || selectedEndMinutes == -1) {
            if (tilEndTime != null) tilEndTime.setError("Required");
            isValid = false;
        }

        if (selectedStartMinutes != -1 && selectedEndMinutes != -1) {
            if (selectedEndMinutes <= selectedStartMinutes) {
                if (tilEndTime != null)
                    tilEndTime.setError("End time must be after start");
                isValid = false;
            }

            for (int[] range : bookedRanges) {
                boolean overlaps = selectedStartMinutes < range[1]
                        && selectedEndMinutes > range[0];
                if (overlaps) {
                    if (tilEndTime != null)
                        tilEndTime.setError("Time overlaps a booked slot");
                    isValid = false;
                    break;
                }
            }
        }

        if (isEmpty(etVisitorName)) {
            if (tilVisitorName != null)
                tilVisitorName.setError("Enter visitor name(s)");
            isValid = false;
        }

        if (isEmpty(etVisitorCount)) {
            if (tilVisitorCount != null) tilVisitorCount.setError("Required");
            isValid = false;
        } else {
            try {
                int count = Integer.parseInt(etVisitorCount.getText().toString().trim());
                if (count < 1) {
                    if (tilVisitorCount != null)
                        tilVisitorCount.setError("Min 1 visitor");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                if (tilVisitorCount != null)
                    tilVisitorCount.setError("Invalid number");
                isValid = false;
            }
        }

        if (isEmpty(etVisitorContact)) {
            if (tilVisitorContact != null)
                tilVisitorContact.setError("Enter contact number");
            isValid = false;
        } else if (etVisitorContact.getText().toString().trim().length() < 10) {
            if (tilVisitorContact != null)
                tilVisitorContact.setError("Enter valid 10-digit number");
            isValid = false;
        }

        if (isEmpty(etVisitorCompany)) {
            if (tilVisitorCompany != null)
                tilVisitorCompany.setError("Enter company name");
            isValid = false;
        }

        if (isEmpty(etPurpose)) {
            if (tilPurpose != null)
                tilPurpose.setError("Describe the meeting purpose");
            isValid = false;
        }

        return isValid;
    }

    // ─────────────────────────────────────────────
    // Submit Booking
    // ─────────────────────────────────────────────
    private void submitBooking() {
        String inputDate = etSelectDate.getText().toString().trim();
        String apiFormattedDate = convertDateToApiFormat(inputDate);
        if (apiFormattedDate == null) {
            Toast.makeText(getContext(),
                    "Invalid date format selected.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String visitorName = etVisitorName.getText().toString().trim();

        int parsedVisitorCount = 0;
        try {
            parsedVisitorCount = Integer.parseInt(etVisitorCount.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }

        String visitorContact = etVisitorContact.getText().toString().trim();
        String visitorCompany = etVisitorCompany.getText().toString().trim();
        String purpose = etPurpose.getText().toString().trim();

        // Build request payload
        MeetingRoomBookingRequest requestBody = new MeetingRoomBookingRequest();
        requestBody.setSelectDate(apiFormattedDate);
        requestBody.setStartTime(startTime);
        requestBody.setEndTime(endTime);
        requestBody.setRoomName(roomId);
        requestBody.setMeetingRoom(roomName);
        requestBody.setHost(hostId);
        requestBody.setHostName(hostName);
        requestBody.setHostEmail(hostEmail);
        requestBody.setHostContact(hostContact);
        requestBody.setVisitorName(visitorName);
        requestBody.setVisitorCount(parsedVisitorCount);
        requestBody.setVisitorContact(visitorContact);
        requestBody.setVisitorCompany(visitorCompany);
        requestBody.setMeetingPurpose(purpose);

        Log.d(TAG, "Submitting Booking request");
        mViewModel.executeBooking(authToken, requestBody);
    }

    // ─────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────
    private void showOverlayLoading(boolean show, String message) {
        if (loadingOverlay != null) {
            if (show) {
                if (textLoadingMsg != null) textLoadingMsg.setText(message);
                loadingOverlay.setVisibility(View.VISIBLE);
            } else {
                loadingOverlay.setVisibility(View.GONE);
            }
        }
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }

    private boolean isEmpty(TextInputEditText et) {
        return et == null || et.getText() == null
                || et.getText().toString().trim().isEmpty();
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String buildFullName(String first, String last) {
        String f = safe(first);
        String l = safe(last);
        String full = (f + " " + l).trim();
        return full.isEmpty() ? "Unknown Host" : full;
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int a, int b, int c) {
        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
            onChange.run();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}