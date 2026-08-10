package app.xedigital.ai.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_holiday, parent, false);
        return new HolidayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolidayViewHolder holder, int position) {

        HolidaysItem holiday = holidays.get(position);
        Context context = holder.itemView.getContext();

        // Holiday name
        holder.holidayName.setText(holiday.getHolidayName());

        // Parse & format date
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        SimpleDateFormat outputDayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        Date parsedDate = null;
        try {
            parsedDate = inputFormat.parse(holiday.getHolidayDate());
            if (parsedDate != null) {
                String formattedDate = outputDateFormat.format(parsedDate);
                String dayOfWeek = outputDayFormat.format(parsedDate);
                holder.holidayDate.setText(formattedDate + " (" + dayOfWeek + ")");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Restricted/Optional chip visibility
        boolean isRestricted = holiday.isIsOptional();
        if (isRestricted) {
            holder.restrictedHoliday.setText(R.string.restricted);
            holder.restrictedHoliday.setVisibility(View.VISIBLE);
        } else {
            holder.restrictedHoliday.setVisibility(View.GONE);
        }

        // Accent bar color based on holiday type
        int accentColorRes = isRestricted ? R.color.RestrictedHoliday : R.color.NationalHoliday;
        int accentColor = ContextCompat.getColor(context, accentColorRes);
        holder.typeIndicator.setBackgroundTintList(ColorStateList.valueOf(accentColor));

        // Today badge + countdown pill
        if (parsedDate != null) {
            long daysUntil = calculateDaysUntil(parsedDate);

            boolean isToday = daysUntil == 0;
            holder.todayBadge.setVisibility(isToday ? View.VISIBLE : View.GONE);

            if (!isToday && daysUntil >= 1 && daysUntil <= 30) {
                holder.countdown.setVisibility(View.VISIBLE);
                String label = "IN " + daysUntil + (daysUntil == 1 ? " DAY" : " DAYS");
                holder.countdown.setText(label);
            } else {
                holder.countdown.setVisibility(View.GONE);
            }
        } else {
            holder.todayBadge.setVisibility(View.GONE);
            holder.countdown.setVisibility(View.GONE);
        }
    }

    private long calculateDaysUntil(Date targetDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar target = Calendar.getInstance();
        target.setTime(targetDate);
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long diffMillis = target.getTimeInMillis() - today.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
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
        Chip restrictedHoliday;
        View typeIndicator;
        TextView todayBadge;
        TextView countdown;

        HolidayViewHolder(View itemView) {
            super(itemView);
            holidayName = itemView.findViewById(R.id.holiday_name);
            holidayDate = itemView.findViewById(R.id.holiday_date);
            restrictedHoliday = itemView.findViewById(R.id.holiday_restricted);
            typeIndicator = itemView.findViewById(R.id.holiday_type_indicator);
            todayBadge = itemView.findViewById(R.id.holiday_today_badge);
            countdown = itemView.findViewById(R.id.holiday_countdown);
        }
    }
}