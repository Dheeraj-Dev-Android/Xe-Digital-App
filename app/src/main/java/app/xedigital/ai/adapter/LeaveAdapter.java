package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.progressindicator.LinearProgressIndicator;

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

        // Set Leave Type
        holder.leaveTypeText.setText(leave.getLeavetype());

        // Calculate and set numeric values
        float creditedLeaves = leave.getCreditLeave();
        float debitedLeaves = leave.getDebitLeave();
        float usedLeaves = leave.getUsedLeave();
        float balanceLeaves = creditedLeaves - usedLeaves - debitedLeaves;

        // Set numeric text views
        holder.creditedDaysText.setText(String.format("%.1f", creditedLeaves));
        holder.debitedDaysText.setText(String.format("%.1f", debitedLeaves));
        holder.usedDaysText.setText(String.format("%.1f", usedLeaves));
        holder.balanceDaysText.setText(String.format("%.1f", balanceLeaves));
        holder.totalDaysText.setText(String.format("%.1f", creditedLeaves));

        // Calculate and set progress
        if (creditedLeaves > 0) {
            float progressPercentage = Math.min((usedLeaves * 100 / creditedLeaves), 100);
            holder.leaveProgress.setProgress((int) progressPercentage);
            holder.progressText.setText(String.format("%.1f%%", progressPercentage));
        } else {
            holder.leaveProgress.setProgress(0);
            holder.progressText.setText("0%");
        }

        // Setup Pie Chart
//        setupPieChart(holder.pieChart, creditedLeaves, usedLeaves, balanceLeaves,debitedLeaves);
    }

//    private void setupPieChart(PieChart pieChart, float creditedLeaves, float usedLeaves, float balanceLeaves,float debitedLeaves) {
//        // Prepare chart entries
//        List<PieEntry> entries = new ArrayList<>();
//        List<Integer> colors = new ArrayList<>();
//
//        // Add Used Leaves
//        if (usedLeaves > 0) {
//            entries.add(new PieEntry(usedLeaves, "Used"));
//            colors.add(Color.RED);
//        }
//        // Add Balance Leaves
//        if (balanceLeaves > 0) {
//            entries.add(new PieEntry(balanceLeaves, "Balance"));
//            colors.add(Color.GREEN);
//        }
//        // Add Credited Leaves
//        if (creditedLeaves > 0) {
//            entries.add(new PieEntry(creditedLeaves, "Credited"));
//            colors.add(Color.BLUE);
//        }
//        // Add Debited Leaves
//        if (debitedLeaves > 0) {
//            entries.add(new PieEntry(debitedLeaves, "Debited"));
//            colors.add(Color.CYAN);
//        }
//
//        // Create dataset
//        PieDataSet dataSet = new PieDataSet(entries, "");
//        dataSet.setColors(colors);
//        dataSet.setValueTextColor(Color.WHITE);
//        dataSet.setValueTextSize(8f);
//
//
//        // Create pie data object
//        PieData pieData = new PieData(dataSet);
//
//        // Customize chart
//        pieChart.setData(pieData);
//        pieChart.getDescription().setEnabled(false);
//        pieChart.setDrawHoleEnabled(true);
//        pieChart.setHoleRadius(35f);
//        pieChart.setTransparentCircleRadius(45f);
//        pieChart.animateXY(1000, 1000);
//        pieChart.invalidate();
//    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    static class LeaveViewHolder extends RecyclerView.ViewHolder {
        TextView leaveTypeText;
        TextView creditedDaysText;
        TextView debitedDaysText;
        TextView usedDaysText;
        TextView balanceDaysText;
        TextView totalDaysText;
        TextView progressText;
        LinearProgressIndicator leaveProgress;
        PieChart pieChart;

        public LeaveViewHolder(@NonNull View itemView) {
            super(itemView);
            leaveTypeText = itemView.findViewById(R.id.leaveTypeText);
            creditedDaysText = itemView.findViewById(R.id.creditedDaysText);
            debitedDaysText = itemView.findViewById(R.id.debitedDaysText);
            usedDaysText = itemView.findViewById(R.id.usedDaysText);
            balanceDaysText = itemView.findViewById(R.id.balanceDaysText);
            totalDaysText = itemView.findViewById(R.id.totalDaysText);
            progressText = itemView.findViewById(R.id.progressText);
            leaveProgress = itemView.findViewById(R.id.leaveProgress);
            pieChart = itemView.findViewById(R.id.leavePieChart);
        }
    }
}