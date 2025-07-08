package app.xedigital.ai.adminUI.adminDashboard;

import static app.xedigital.ai.utills.BirthdayUtils.formatBirthdayDate;
import static app.xedigital.ai.utills.BirthdayUtils.isBirthdayToday;

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

    private List<EmployeesItem> birthdayEmployees;

    public BirthdayEmployeesAdapter() {
        this.birthdayEmployees = new ArrayList<>();
    }

    // Method to set up horizontal scrolling for the RecyclerView
    public static void setupHorizontalScrolling(RecyclerView recyclerView) {
        // Set horizontal LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                recyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerView.setLayoutManager(layoutManager);

        // Add item decoration for spacing between items
        recyclerView.addItemDecoration(new HorizontalSpaceItemDecoration(
                recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.birthday_item_spacing)
        ));
    }

    public void updateBirthdayEmployees(List<EmployeesItem> employees) {
        this.birthdayEmployees = employees != null ? employees : new ArrayList<>();
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
        EmployeesItem employee = birthdayEmployees.get(position);
        holder.bind(employee);
    }

    @Override
    public int getItemCount() {
        return birthdayEmployees.size();
    }

    // Item decoration for horizontal spacing
    public static class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public HorizontalSpaceItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            // Add spacing to the right of each item except the last one
            if (position < parent.getAdapter().getItemCount() - 1) {
                outRect.right = spacing;
            }

            // Add spacing to the left of the first item
            if (position == 0) {
                outRect.left = spacing / 2;
            }

            // Add spacing to the right of the last item
            if (position == parent.getAdapter().getItemCount() - 1) {
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
            // Set employee name - Fixed null check logic
            String fullName = "";
            if (employee.getFirstname() != null && employee.getLastname() != null) {
                fullName = employee.getFirstname() + " " + employee.getLastname();
            } else if (employee.getFirstname() != null) {
                fullName = employee.getFirstname();
            } else if (employee.getLastname() != null) {
                fullName = employee.getLastname();
            } else {
                fullName = "Unknown";
            }
            employeeName.setText(fullName);

            // Set employee code
            employeeCode.setText(employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "N/A");

            // Set department - Added null check for department object
            if (employee.getDepartment() != null && employee.getDepartment().getName() != null) {
                department.setText(employee.getDepartment().getName());
            } else {
                department.setText("N/A");
            }

            // Set email
            email.setText(employee.getEmail() != null ? employee.getEmail() : "N/A");

            // Set contact
            contact.setText(employee.getContact() != null ? employee.getContact() : "N/A");

            // Set birthday date
            setBirthdayDate(employee.getDateOfBirth());

            // Load profile image
            loadProfileImage(employee);
        }

        private void setBirthdayDate(String dateOfBirth) {
            String formattedDate = formatBirthdayDate(dateOfBirth);
            birthdayDate.setText(formattedDate);

            // Add special styling for today's birthday
            if (isBirthdayToday(dateOfBirth)) {
                birthdayDate.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
                // You can add more special styling here like background, animation, etc.
            } else {
                // Reset to default color for recycled views
                birthdayDate.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
            }
        }

        private void loadProfileImage(EmployeesItem employee) {
            if (employee.getProfileImageUrl() != null) {
                Glide.with(itemView.getContext())
                        .load(employee.getProfileImageUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .centerCrop())
                        .into(profileImage);
            } else {
                // Set default profile image
                profileImage.setImageResource(R.drawable.ic_person);
            }
        }
    }
}