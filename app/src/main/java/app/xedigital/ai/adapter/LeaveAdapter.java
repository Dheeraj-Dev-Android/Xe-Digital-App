package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.leaves.LeavesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.LeaveViewHolder> {
    private final List<LeavesItem> leaveList;

    public LeaveAdapter(List<LeavesItem> leaveList) {
        this.leaveList = leaveList;
    }

    @NonNull
    @Override
    public LeaveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leave_item, parent, false);
        return new LeaveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveViewHolder holder, int position) {
        LeavesItem leave = leaveList.get(position);
        holder.typeTextView.setText(leave.getLeavetype());
        holder.durationTextView.setText("Credit: " + leave.getCreditLeave() + ", Debit: " + leave.getDebitLeave());
        holder.reasonTextView.setText("Used: " + leave.getUsedLeave());
        String assignDate = leave.getAssignDate();
        holder.statusTextView.setText("Assigned: " + DateTimeUtils.getDayOfWeekAndDate(assignDate));


    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    static class LeaveViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView;
        TextView durationTextView;
        TextView reasonTextView;
        TextView statusTextView;

        public LeaveViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
}
