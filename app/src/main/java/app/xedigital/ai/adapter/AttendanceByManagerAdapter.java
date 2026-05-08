package app.xedigital.ai.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.AttandanceByManager.EmployeePunchDataItem;
import app.xedigital.ai.ui.AttendanceByManager.AttendanceDetailActivity;
import app.xedigital.ai.utills.DateTimeUtils;

public class AttendanceByManagerAdapter extends RecyclerView.Adapter<AttendanceByManagerAdapter.ViewHolder> {

    private List<EmployeePunchDataItem> punchDataList;

    public AttendanceByManagerAdapter(List<EmployeePunchDataItem> punchDataList) {
        this.punchDataList = punchDataList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<EmployeePunchDataItem> newList) {
        this.punchDataList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_manager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeePunchDataItem item = punchDataList.get(position);
        if (item == null) return;

        holder.nameTextView.setText(item.getEmployee() != null ? item.getEmployee().getFirstname() + " " + item.getEmployee().getLastname() : "Unknown");
        holder.dateTextView.setText(item.getPunchDateFormat());

        String status = item.getLeaveName();

        if (item.isFullDayLeave()) {
            holder.leaveNameTextView.setVisibility(View.VISIBLE);
            holder.leaveNameTextView.setText(status);

            // --- WEEK OFF STYLING ---
            if ("Week Off".equalsIgnoreCase(status)) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#EEEEEE")); // Light Grey
                holder.leaveNameTextView.setTextColor(Color.GRAY);
            } else {
                holder.cardView.setCardBackgroundColor(Color.WHITE);
                holder.leaveNameTextView.setTextColor(Color.parseColor("#FF9800")); // Orange for LOP/Leave
            }

            holder.layoutPunchDetails.setVisibility(View.GONE);
            holder.btn_viewAttendance.setVisibility(View.GONE);
            holder.viewDivider.setVisibility(View.GONE);
            holder.layoutTimeStats.setVisibility(View.GONE);
        } else {
            // --- NORMAL PRESENT STYLING ---
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.layoutPunchDetails.setVisibility(View.VISIBLE);
            holder.viewDivider.setVisibility(View.VISIBLE);
            holder.btn_viewAttendance.setVisibility(View.VISIBLE);
            holder.layoutTimeStats.setVisibility(View.VISIBLE);

            // Show leave name only if it's a half-day
            holder.leaveNameTextView.setVisibility((status != null && !status.isEmpty()) ? View.VISIBLE : View.GONE);
            holder.leaveNameTextView.setText(status + " (Half Day)");

            holder.punchInTextView.setText(DateTimeUtils.formatTime(item.getPunchIn()));
            holder.punchOutTextView.setText(DateTimeUtils.formatTime(item.getPunchOut()));
            holder.totalTimeTextView.setText(item.getTotalTime());
            holder.overTimeTextView.setText(item.getOvertime());
            holder.lateTimeTextView.setText(item.getLateTime());
        }
        holder.btn_viewAttendance.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, AttendanceDetailActivity.class);

            // Pass the item data
            intent.putExtra("attendance_item", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return punchDataList != null ? punchDataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, dateTextView, leaveNameTextView;
        TextView punchInTextView, punchOutTextView;
        TextView totalTimeTextView, overTimeTextView, lateTimeTextView;
        ImageButton btn_viewAttendance;
        View layoutPunchDetails, viewDivider, layoutTimeStats;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (com.google.android.material.card.MaterialCardView) itemView;
            nameTextView = itemView.findViewById(R.id.nameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            leaveNameTextView = itemView.findViewById(R.id.leaveNameTextView);
            punchInTextView = itemView.findViewById(R.id.punchInTextView);
            punchOutTextView = itemView.findViewById(R.id.punchOutTextView);
            totalTimeTextView = itemView.findViewById(R.id.totalTimeTextView);
            overTimeTextView = itemView.findViewById(R.id.overTimeTextView);
            lateTimeTextView = itemView.findViewById(R.id.lateTimeTextView);
            btn_viewAttendance = itemView.findViewById(R.id.btn_viewAttendance);
            layoutPunchDetails = itemView.findViewById(R.id.layoutPunchDetails);
            viewDivider = itemView.findViewById(R.id.viewDivider);
            layoutTimeStats = itemView.findViewById(R.id.layoutTimeStats);
        }
    }
}
