package app.xedigital.ai.ui.claim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import app.xedigital.ai.R;

public class ClaimSubmissionFragment extends Fragment {

    private ClaimSubmissionViewModel mViewModel;

    public static ClaimSubmissionFragment newInstance() {
        return new ClaimSubmissionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_claim_submission, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ClaimSubmissionViewModel.class);
        // TODO: Use the ViewModel
    }

}