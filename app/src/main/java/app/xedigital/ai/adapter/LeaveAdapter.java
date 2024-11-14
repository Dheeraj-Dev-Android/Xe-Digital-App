package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.leaves.LeavesItem;

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
        holder.leaveTypeText.setText(leave.getLeavetype());
        holder.creditedDaysText.setText(String.valueOf(leave.getCreditLeave()));
        holder.debitedDaysText.setText(String.valueOf(leave.getDebitLeave()));
        holder.usedDaysText.setText(String.valueOf(leave.getUsedLeave()));
        holder.balanceDaysText.setText(String.valueOf(leave.getCreditLeave() - leave.getUsedLeave() - leave.getDebitLeave()));
        holder.totalDaysText.setText(String.valueOf(leave.getCreditLeave()));

        float creditedDays = leave.getCreditLeave();
        float usedDays = leave.getUsedLeave();


        if (creditedDays > 0) {
            float progress = Math.min((usedDays * 100 / creditedDays), 100);
            holder.leaveProgress.setProgress((int) progress);
            holder.progressText.setText(progress + "%");
        } else {
            holder.leaveProgress.setProgress(0);
            holder.progressText.setText("0%");
        }
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    static class LeaveViewHolder extends RecyclerView.ViewHolder {
        TextView leaveTypeText;
        TextView creditedDaysText;
        TextView debitedDaysText;
        TextView balanceDaysText;
        TextView usedDaysText;
        TextView totalDaysText;
        ProgressBar leaveProgress;
        TextView progressText;

        public LeaveViewHolder(@NonNull View itemView) {
            super(itemView);
            leaveTypeText = itemView.findViewById(R.id.leaveTypeText);
            creditedDaysText = itemView.findViewById(R.id.creditedDaysText);
            debitedDaysText = itemView.findViewById(R.id.debitedDaysText);
            balanceDaysText = itemView.findViewById(R.id.balanceDaysText);
            leaveProgress = itemView.findViewById(R.id.leaveProgress);
            usedDaysText = itemView.findViewById(R.id.usedDaysText);
            totalDaysText = itemView.findViewById(R.id.totalDaysText);
            progressText = itemView.findViewById(R.id.progressText);
        }
    }
}
