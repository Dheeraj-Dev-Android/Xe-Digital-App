package app.xedigital.ai.ui.mrm;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import app.xedigital.ai.R;

public class TimeSlotBottomSheet extends BottomSheetDialogFragment {

    private String title;
    private List<TimeSlot> slots;
    private OnTimeSelectedListener listener;

    public static TimeSlotBottomSheet newInstance(String title,
                                                  List<TimeSlot> slots,
                                                  OnTimeSelectedListener listener) {
        TimeSlotBottomSheet sheet = new TimeSlotBottomSheet();
        sheet.title = title;
        sheet.slots = slots;
        sheet.listener = listener;
        return sheet;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog)
                super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_time_slot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textTitle = view.findViewById(R.id.textPickerTitle);
        View btnClose = view.findViewById(R.id.btnCloseTimePicker);
        RecyclerView recycler = view.findViewById(R.id.recyclerTimeSlots);
        View emptyContainer = view.findViewById(R.id.emptySlotsContainer);

        textTitle.setText(title != null ? title : "Select Time");
        btnClose.setOnClickListener(v -> dismiss());

        // Check if any slots are available
        boolean anyAvailable = false;
        if (slots != null) {
            for (TimeSlot s : slots) {
                if (s.isEnabled() && !s.isBooked()) {
                    anyAvailable = true;
                    break;
                }
            }
        }

        if (slots == null || slots.isEmpty() || !anyAvailable) {
            recycler.setVisibility(View.GONE);
            emptyContainer.setVisibility(View.VISIBLE);
            return;
        }

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 4));
        TimeSlotAdapter adapter = new TimeSlotAdapter(slot -> {
            if (listener != null) {
                listener.onTimeSelected(slot.getDisplay(), slot.getTotalMinutes());
            }
            dismiss();
        });
        adapter.setSlots(slots);
        recycler.setAdapter(adapter);
    }

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time, int totalMinutes);
    }
}