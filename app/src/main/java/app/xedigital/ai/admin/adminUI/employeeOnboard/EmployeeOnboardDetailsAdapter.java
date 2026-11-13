package app.xedigital.ai.admin.adminUI.employeeOnboard;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.employeeOnboardDetails.EmployeeOnBoardDetailsItem;

public class EmployeeOnboardDetailsAdapter extends RecyclerView.Adapter<EmployeeOnboardDetailsAdapter.ViewHolder> {

    private static final int[] AVATAR_COLORS = {
            0xFF1E88E5, 0xFF43A047, 0xFFE53935, 0xFF8E24AA, 0xFFFB8C00,
            0xFF00ACC1, 0xFF3949AB, 0xFFD81B60, 0xFF00897B, 0xFF6D4C41
    };

    private final List<EmployeeOnBoardDetailsItem> fullEmployeeList = new ArrayList<>();
    private final List<EmployeeOnBoardDetailsItem> filteredList = new ArrayList<>();
    private final OnOnboardActionListener listener;

    public EmployeeOnboardDetailsAdapter(OnOnboardActionListener listener) {
        this.listener = listener;
    }

    public void updateData(List<EmployeeOnBoardDetailsItem> newList) {
        fullEmployeeList.clear();
        filteredList.clear();
        if (newList != null) {
            fullEmployeeList.addAll(newList);
            filteredList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    /**
     * Real-time filter by full name
     */
    public void filterByName(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullEmployeeList);
        } else {
            String lowerQuery = query.trim().toLowerCase(Locale.getDefault());
            for (EmployeeOnBoardDetailsItem item : fullEmployeeList) {
                if (item == null) continue;
                String name = item.getFullName();
                if (name != null && name.toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean isFilteredEmpty() {
        return filteredList.isEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emp_onboard_details_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= filteredList.size()) return;
        EmployeeOnBoardDetailsItem item = filteredList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public interface OnOnboardActionListener {
        void onViewDetails(EmployeeOnBoardDetailsItem item);

        void onEditDetails(EmployeeOnBoardDetailsItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView avatarText, tvName, tvEmail, tvMobile, tvEmpId, tvDepartment, tvJoinDate;
        ShapeableImageView btnView, btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarText = itemView.findViewById(R.id.avatarText);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvEmpId = itemView.findViewById(R.id.tvEmpId);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvJoinDate = itemView.findViewById(R.id.tvJoinDate);
            btnView = itemView.findViewById(R.id.btnView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }

        public void bind(final EmployeeOnBoardDetailsItem item, final OnOnboardActionListener listener) {
            if (item == null) return;

            String name = safeString(item.getFullName());
            tvName.setText(name);

            String email = safeString(item.getPersonalEmail());
            tvEmail.setText(email);

            tvMobile.setText(safeString(item.getPersonalMobile()));
            tvEmpId.setText(safeString(item.getEmployeeId()));
            tvDepartment.setText(safeString(item.getDepartment()));

            String joinDateRaw = item.getDateOfJoining();
            tvJoinDate.setText(
                    (joinDateRaw != null && !joinDateRaw.trim().isEmpty())
                            ? formatDate(joinDateRaw)
                            : "N/A"
            );

            String displayName = !name.equals("N/A") ? name : (!email.equals("N/A") ? email : "?");
            avatarText.setText(getInitials(displayName));

            GradientDrawable circularBg = new GradientDrawable();
            circularBg.setShape(GradientDrawable.OVAL);
            circularBg.setColor(getAvatarColor(displayName));
            avatarText.setBackground(circularBg);

            btnView.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetails(item);
            });
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditDetails(item);
            });
        }

        private String safeString(String value) {
            return (value != null && !value.trim().isEmpty()) ? value.trim() : "N/A";
        }

        private String getInitials(String name) {
            if (name == null || name.trim().isEmpty() || name.equals("?") || name.equals("N/A")) {
                return "?";
            }
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 1) {
                return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.getDefault());
            }
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase(Locale.getDefault());
        }

        private int getAvatarColor(String name) {
            if (name == null || name.trim().isEmpty() || name.equals("N/A")) {
                return AVATAR_COLORS[0];
            }
            return AVATAR_COLORS[Math.abs(name.trim().hashCode()) % AVATAR_COLORS.length];
        }

        private String formatDate(String isoDate) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = input.parse(isoDate);
                if (date == null) return isoDate;
                return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
            } catch (ParseException e) {
                try {
                    SimpleDateFormat input2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date2 = input2.parse(isoDate);
                    if (date2 != null) {
                        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date2);
                    }
                } catch (ParseException ignored) {
                }
                return isoDate;
            }
        }
    }
}