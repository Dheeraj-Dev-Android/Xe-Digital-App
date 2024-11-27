package app.xedigital.ai.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

public class CurrencyArrayAdapter extends ArrayAdapter<String> {

    public CurrencyArrayAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        // Enable only the "INR" option (assuming it's at position 1)
        return position == 1;
    }
}