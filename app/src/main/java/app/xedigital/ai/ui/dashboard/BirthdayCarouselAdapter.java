//package app.xedigital.ai.ui.dashboard;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.google.android.material.imageview.ShapeableImageView;
//
//import java.util.List;
//
//import app.xedigital.ai.R;
//
//public class BirthdayCarouselAdapter extends RecyclerView.Adapter<BirthdayCarouselAdapter.ViewHolder> {
//
//    private final List<EmployeesItem> list;
//
//    public BirthdayCarouselAdapter(List<EmployeesItem> list) {
//        this.list = list;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_birthday_carousel, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        EmployeesItem item = list.get(position);
//
//        String fullName = item.getFirstname() + " " + item.getLastname();
//        holder.tvEmpName.setText(fullName);
//        holder.tvEmpCode.setText("Employee Code: " + item.getEmployeeCode());
//        holder.tvEmpDOB.setText("DOB: " + DateTimeUtils.formatToReadableDate(item.getDateOfBirth()));
//        holder.tvEmpEmail.setText(item.getEmail());
//        holder.tvEmpContact.setText(item.getContact());
//
//        if (item.getDepartment() != null) {
//            holder.tvEmpDept.setText(item.getDepartment().getName());
//        } else {
//            holder.tvEmpDept.setText("N/A");
//        }
//
//        Glide.with(holder.itemView.getContext())
//                .load(item.getProfileImageUrl())
//                .placeholder(R.mipmap.ic_launcher_round)
//                .error(R.mipmap.ic_launcher_round)
//                .into(holder.ivEmpProfile);
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//        ShapeableImageView ivEmpProfile;
//        TextView tvEmpName, tvEmpCode, tvEmpDOB, tvEmpDept, tvEmpEmail, tvEmpContact;
//
//        ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ivEmpProfile = itemView.findViewById(R.id.ivEmpProfile);
//            tvEmpName = itemView.findViewById(R.id.tvEmpName);
//            tvEmpCode = itemView.findViewById(R.id.tvEmpCode);
//            tvEmpDOB = itemView.findViewById(R.id.tvEmpDOB);
//            tvEmpDept = itemView.findViewById(R.id.tvEmpDept);
//            tvEmpEmail = itemView.findViewById(R.id.tvEmpEmail);
//            tvEmpContact = itemView.findViewById(R.id.tvEmpContact);
//        }
//    }
//}