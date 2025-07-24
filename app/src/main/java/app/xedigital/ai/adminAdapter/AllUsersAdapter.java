package app.xedigital.ai.adminAdapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.Admin.AdminUsers.UsersItem;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.UserViewHolder> {

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
            // Avatar initials
            String initials = "";
            if (!TextUtils.isEmpty(user.getFirstname()) && !TextUtils.isEmpty(user.getLastname())) {
                initials = user.getFirstname().substring(0, 1).toUpperCase() + user.getLastname().substring(0, 1).toUpperCase();
            }

            avatarText.setText(initials);

            // User Info
            userNameText.setText(user.getFirstname() + " " + user.getLastname());
            emailText.setText(user.getEmail());
            roleText.setText(user.getRole() != null ? user.getRole().getDisplayName() : "N/A");
            companyText.setText(user.getCompany() != null ? user.getCompany().getName() : "N/A");
            branchText.setText(user.getBranch() != null ? user.getBranch().getName() : "N/A");

            // Status
            if (user.isActive()) {
                statusChip.setText("Active");
                statusChip.setTextColor(itemView.getResources().getColor(R.color.success));
                statusChip.setChipBackgroundColorResource(R.color.success_light);
            } else {
                statusChip.setText("Inactive");
                statusChip.setTextColor(itemView.getResources().getColor(R.color.error));
                statusChip.setChipBackgroundColorResource(R.color.error_light);
            }

            // Created date
            createdDateText.setText(formatDate(user.getCreatedAt()));

            // Action click
            actionButton.setOnClickListener(v -> {
                if (listener != null) listener.onUserClicked(user);
            });
        }

        private String formatDate(String isoDate) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date date = input.parse(isoDate);
                SimpleDateFormat output = new SimpleDateFormat("MMMM dd, yyyy");
                return output.format(date);
            } catch (ParseException e) {
                return "Invalid Date";
            }
        }
    }
}


