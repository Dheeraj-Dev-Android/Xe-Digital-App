package app.xedigital.ai.adminActivity;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomEmployeeAdapter extends ArrayAdapter<EmployeeDropdownItem> {

    private final Context context;
    private final List<EmployeeDropdownItem> originalList;
    private final List<EmployeeDropdownItem> filteredList;
    private final Set<String> selectedEmployeeIds;

    public CustomEmployeeAdapter(Context context, List<EmployeeDropdownItem> list, Set<String> selectedIds) {
        super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(list));
        this.context = context;
        this.originalList = new ArrayList<>(list); // Deep copy
        this.filteredList = new ArrayList<>(list);
        this.selectedEmployeeIds = selectedIds;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public EmployeeDropdownItem getItem(int position) {
        return filteredList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view;

        EmployeeDropdownItem item = getItem(position);
        String fullName = item.getFirstName() + " " + item.getLastName();
        textView.setText(fullName);

        if (selectedEmployeeIds.contains(item.getId())) {
            textView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
        } else {
            textView.setBackgroundColor(Color.TRANSPARENT);
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<EmployeeDropdownItem> resultsList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    resultsList.addAll(originalList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (EmployeeDropdownItem item : originalList) {
                        String fullName = (item.getFirstName() + " " + item.getLastName()).toLowerCase();
                        if (fullName.contains(filterPattern)) {
                            resultsList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = resultsList;
                results.count = resultsList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results.values != null) {
                    filteredList.addAll((List<EmployeeDropdownItem>) results.values);
                }
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof EmployeeDropdownItem) {
                    EmployeeDropdownItem item = (EmployeeDropdownItem) resultValue;
                    return item.getFirstName() + " " + item.getLastName();
                }
                return super.convertResultToString(resultValue);
            }
        };
    }
}
