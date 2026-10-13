package app.xedigital.ai.admin.adminAdapter;

import static app.xedigital.ai.ui.timesheet.SelectedTimesheetFragment.ARG_SELECTED_ITEM;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.AdminUsers.UsersItem;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.UserViewHolder> {

    private static final int[] AVATAR_COLORS = {
            0xFF1E88E5, // Blue
            0xFF43A047, // Green
            0xFFE53935, // Red
            0xFF8E24AA, // Purple
            0xFFFB8C00, // Orange
            0xFF00ACC1, // Cyan
            0xFF3949AB, // Indigo
            0xFFD81B60, // Pink
            0xFF00897B, // Teal
            0xFF6D4C41  // Brown
    };

    private final OnUserClickListener listener;
    private List<UsersItem> users;

    public AllUsersAdapter(List<UsersItem> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void updateData(List<UsersItem> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UsersItem user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    // Helper: Extract 1 or 2 uppercase initials from a name
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // Helper: Returns a consistent color from the palette based on the name's hash
    private int getAvatarColor(String name) {
        if (name == null || name.trim().isEmpty()) {
            return AVATAR_COLORS[0];
        }
        int hash = Math.abs(name.trim().hashCode());
        return AVATAR_COLORS[hash % AVATAR_COLORS.length];
    }

    public interface OnUserClickListener {
        void onUserClicked(UsersItem user);
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView avatarText, userNameText, emailText, roleText, companyText, branchText, createdDateText;
        Chip statusChip;
        ShapeableImageView actionButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarText = itemView.findViewById(R.id.avatarText);
            userNameText = itemView.findViewById(R.id.userNameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleText = itemView.findViewById(R.id.roleText);
            companyText = itemView.findViewById(R.id.companyText);
            branchText = itemView.findViewById(R.id.branchText);
            createdDateText = itemView.findViewById(R.id.createdDateText);
            statusChip = itemView.findViewById(R.id.statusChip);
            actionButton = itemView.findViewById(R.id.actionButton);
        }

        public void bind(UsersItem user) {
            // Safe Full Name Resolution
            String firstName = user.getFirstname();
            String lastName = user.getLastname();
            StringBuilder nameBuilder = new StringBuilder();
            if (firstName != null && !firstName.trim().isEmpty()) {
                nameBuilder.append(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                if (nameBuilder.length() > 0) nameBuilder.append(" ");
                nameBuilder.append(lastName.trim());
            }
            String displayName = nameBuilder.length() > 0 ? nameBuilder.toString() : "N/A";
            userNameText.setText(displayName);

            // Safe Email Resolution
            String email = user.getEmail();
            emailText.setText((email != null && !email.trim().isEmpty()) ? email : "N/A");

            // Safe Role Resolution
            if (user.getRole() != null && user.getRole().getDisplayName() != null
                    && !user.getRole().getDisplayName().trim().isEmpty()) {
                roleText.setText(user.getRole().getDisplayName());
            } else {
                roleText.setText("N/A");
            }

            // Safe Company Resolution
            if (user.getCompany() != null && user.getCompany().getName() != null
                    && !user.getCompany().getName().trim().isEmpty()) {
                companyText.setText(user.getCompany().getName());
            } else {
                companyText.setText("N/A");
            }

            // Safe Branch Resolution
            if (user.getBranch() != null && user.getBranch().getName() != null
                    && !user.getBranch().getName().trim().isEmpty()) {
                branchText.setText(user.getBranch().getName());
            } else {
                branchText.setText("N/A");
            }

            // Safe Created Date Resolution
            String createdAt = user.getCreatedAt();
            if (createdAt != null && !createdAt.trim().isEmpty()) {
                createdDateText.setText(formatDate(createdAt));
            } else {
                createdDateText.setText("N/A");
            }

            // Dynamic Initials with Colored Background
            String initials = getInitials(displayName);
            int avatarColor = getAvatarColor(displayName);
            avatarText.setText(initials);

            GradientDrawable circularBg = new GradientDrawable();
            circularBg.setShape(GradientDrawable.OVAL);
            circularBg.setColor(avatarColor);
            avatarText.setBackground(circularBg);

            // Status
            if (user.isActive()) {
                statusChip.setText("Active");
                statusChip.setChipBackgroundColorResource(R.color.success_light);
            } else {
                statusChip.setText("Inactive");
                statusChip.setChipBackgroundColorResource(R.color.error_light);
            }

            // Action click (existing navigation logic preserved)
            actionButton.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    UsersItem selectedItem = users.get(adapterPosition);
                    String userId = selectedItem.getId();
                    if (userId != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ARG_SELECTED_ITEM, selectedItem);
                        Navigation.findNavController(v).navigate(R.id.nav_allUsers_to_nav_editUser, bundle);
                    } else {
                        Toast.makeText(v.getContext(), "Selected Employee or EmployeeId is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(v.getContext(), "Invalid position", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private String formatDate(String isoDate) {
            if (isoDate == null || isoDate.trim().isEmpty()) {
                return "N/A";
            }
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date date = input.parse(isoDate);
                if (date == null) return "N/A";
                SimpleDateFormat output = new SimpleDateFormat("MMMM dd, yyyy");
                return output.format(date);
            } catch (ParseException | NullPointerException e) {
                return "N/A";
            }
        }
    }
}