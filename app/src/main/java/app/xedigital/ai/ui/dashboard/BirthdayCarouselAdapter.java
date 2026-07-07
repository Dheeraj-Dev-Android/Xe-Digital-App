package app.xedigital.ai.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.allEmployee.EmployeesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class BirthdayCarouselAdapter extends RecyclerView.Adapter<BirthdayCarouselAdapter.ViewHolder> {

    private final List<EmployeesItem> list;

    public BirthdayCarouselAdapter(List<EmployeesItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_birthday_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeesItem item = list.get(position);

        String fullName = item.getFirstname() + " " + item.getLastname();
        holder.tvEmpName.setText(fullName);
        holder.tvEmpDOB.setText(DateTimeUtils.formatToReadableDate(item.getDateOfBirth()));
        holder.tvEmpEmail.setText(item.getEmail());

        if (item.getDepartment() != null) {
            holder.tvEmpDept.setText(item.getDesignation());
        } else {
            holder.tvEmpDept.setText("N/A");
        }

        Glide.with(holder.itemView.getContext())
                .load(item.getProfileImageUrl())
                .placeholder(R.mipmap.ic_default_profile)
                .error(R.mipmap.ic_default_profile)
                .into(holder.ivEmpProfile);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivEmpProfile;
        TextView tvEmpName, tvEmpDOB, tvEmpDept, tvEmpEmail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEmpProfile = itemView.findViewById(R.id.ivEmpProfile);
            tvEmpName = itemView.findViewById(R.id.tvEmpName);
            tvEmpDOB = itemView.findViewById(R.id.tvEmpDOB);
            tvEmpDept = itemView.findViewById(R.id.tvEmpDept);
            tvEmpEmail = itemView.findViewById(R.id.tvEmpEmail);
        }
    }
}