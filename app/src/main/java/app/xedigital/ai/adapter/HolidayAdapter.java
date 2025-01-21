package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.holiday.HolidaysItem;

public class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.HolidayViewHolder> {

    private List<HolidaysItem> holidays;

    public HolidayAdapter(List<HolidaysItem> holidays) {
        this.holidays = holidays;
    }

    public HolidayAdapter() {
        this.holidays = new ArrayList<>();
    }

    @NonNull
    @Override
    public HolidayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_holiday, parent, false);
        return new HolidayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolidayViewHolder holder, int position) {

        HolidaysItem holiday = holidays.get(position);

        // Set holiday name
        holder.holidayName.setText(holiday.getHolidayName());

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        SimpleDateFormat outputDayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        try {
            Date date = inputFormat.parse(holiday.getHolidayDate());
            if (date != null) {
                String formattedDate = outputDateFormat.format(date);
                String dayOfWeek = outputDayFormat.format(date);
                String dateText = formattedDate + " (" + dayOfWeek + ")";

                holder.holidayDate.setText(dateText);
//                Log.d("HolidayAdapter", "Setting holiday date: " + dateText);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set restricted holiday text
        if (holiday.isIsOptional()) {
            holder.restrictedHoliday.setText(R.string.restricted);
//            holder.restrictedHoliday.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.restrictedHoliday.setVisibility(View.VISIBLE);
        } else {
            holder.restrictedHoliday.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return holidays != null ? holidays.size() : 0;
    }

    public void updateHolidays(List<HolidaysItem> newHolidays) {
        this.holidays = newHolidays;
        notifyDataSetChanged();
    }

    static class HolidayViewHolder extends RecyclerView.ViewHolder {
        TextView holidayName;
        TextView holidayDate;
        TextView restrictedHoliday;

        HolidayViewHolder(View itemView) {
            super(itemView);
            holidayName = itemView.findViewById(R.id.holiday_name);
            holidayDate = itemView.findViewById(R.id.holiday_date);
            restrictedHoliday = itemView.findViewById(R.id.holiday_restricted);
        }
    }
}