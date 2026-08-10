package app.xedigital.ai.ui.leaves;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesItem;

public class ApproveLeaveViewModal extends ViewModel {
    public final MutableLiveData<List<AppliedLeavesItem>> leaveList = new MutableLiveData<List<app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesItem>>(new ArrayList<>());
    public boolean isDataLoaded = false;
    public String currentStartDate = "";
    public String currentEndDate = "";
}
