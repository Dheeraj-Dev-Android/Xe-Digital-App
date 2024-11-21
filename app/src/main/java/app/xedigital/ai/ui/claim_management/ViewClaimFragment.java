package app.xedigital.ai.ui.claim_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import app.xedigital.ai.R;


public class ViewClaimFragment extends Fragment {


    public ViewClaimFragment() {
        // Required empty public constructor
    }


    public static ViewClaimFragment newInstance(String param1, String param2) {
        ViewClaimFragment fragment = new ViewClaimFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_claim, container, false);
    }
}