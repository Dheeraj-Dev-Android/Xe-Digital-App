package app.xedigital.ai.adminUI.adminDashboard;

import static app.xedigital.ai.utills.BirthdayUtils.formatBirthdayDate;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;
import de.hdodenhof.circleimageview.CircleImageView;

public class BirthdayEmployeesAdapter extends RecyclerView.Adapter<BirthdayEmployeesAdapter.BirthdayViewHolder> {

    private final List<EmployeesItem> birthdayEmployees;

    public BirthdayEmployeesAdapter() {
        this.birthdayEmployees = new ArrayList<>();
    }

    public static void setupHorizontalScrolling(RecyclerView recyclerView) {
        if (recyclerView == null) return;
        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        int spacing = recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.birthday_item_spacing);
        recyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(spacing));
    }

    public void updateBirthdayEmployees(List<EmployeesItem> employees) {
        this.birthdayEmployees.clear();
        if (employees != null) {
            this.birthdayEmployees.addAll(employees);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BirthdayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_birthday_employee, parent, false);
        return new BirthdayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BirthdayViewHolder holder, int position) {
        if (position >= 0 && position < birthdayEmployees.size()) {
            EmployeesItem employee = birthdayEmployees.get(position);
            if (employee != null) {
                holder.bind(employee);
            }
        }
    }

    @Override
    public int getItemCount() {
        return birthdayEmployees.size();
    }

    public static class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public HorizontalSpaceItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (parent.getAdapter() == null) return;
            int totalItems = parent.getAdapter().getItemCount();

            if (position == RecyclerView.NO_POSITION || totalItems == 0) return;

            if (position < (totalItems - 1)) {
                outRect.right = spacing;
            }
            if (position == 0) {
                outRect.left = spacing / 2;
            }
            if (position == totalItems - 1) {
                outRect.right = spacing / 2;
            }
        }
    }

    public static class BirthdayViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImage;
        private final TextView employeeName;
        private final TextView employeeCode;
        private final TextView birthdayDate;
        private final TextView department;
        private final TextView email;
        private final TextView contact;
        private final String fallbackText = "Data Not Available";

        public BirthdayViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            employeeName = itemView.findViewById(R.id.employeeName);
            employeeCode = itemView.findViewById(R.id.employeeCode);
            birthdayDate = itemView.findViewById(R.id.birthdayDate);
            department = itemView.findViewById(R.id.department);
            email = itemView.findViewById(R.id.email);
            contact = itemView.findViewById(R.id.contact);
        }

        public void bind(EmployeesItem employee) {
            String firstName = employee.getFirstname();
            String lastName = employee.getLastname();
            String fullName;

            if (firstName != null && !firstName.trim().isEmpty() && lastName != null && !lastName.trim().isEmpty()) {
                fullName = firstName.trim() + " " + lastName.trim();
            } else if (firstName != null && !firstName.trim().isEmpty()) {
                fullName = firstName.trim();
            } else if (lastName != null && !lastName.trim().isEmpty()) {
                fullName = lastName.trim();
            } else {
                fullName = fallbackText;
            }
            employeeName.setText(fullName);

            employeeCode.setText((employee.getEmployeeCode() != null && !employee.getEmployeeCode().trim().isEmpty())
                    ? employee.getEmployeeCode() : fallbackText);

            if (employee.getDepartment() != null && employee.getDepartment().getName() != null && !employee.getDepartment().getName().trim().isEmpty()) {
                department.setText(employee.getDepartment().getName());
            } else {
                department.setText(fallbackText);
            }

            email.setText((employee.getEmail() != null && !employee.getEmail().trim().isEmpty()) ? employee.getEmail() : fallbackText);
            contact.setText((employee.getContact() != null && !employee.getContact().trim().isEmpty()) ? employee.getContact() : fallbackText);

            setBirthdayDate(employee.getDateOfBirth());
            loadProfileImage(employee);
        }

        private void setBirthdayDate(String dateOfBirth) {
            if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
                birthdayDate.setText(fallbackText);
                return;
            }
            try {
                String formattedDate = formatBirthdayDate(dateOfBirth);
                birthdayDate.setText((formattedDate != null && !formattedDate.trim().isEmpty()) ? formattedDate : fallbackText);
            } catch (Exception e) {
                birthdayDate.setText(fallbackText);
            }
        }

        private void loadProfileImage(EmployeesItem employee) {
            Object rawUrl = employee.getProfileImageUrl();

            if (rawUrl != null) {
                Glide.with(itemView.getContext())
                        .load(rawUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .centerCrop())
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }
    }
}