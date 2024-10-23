package app.xedigital.ai.ui.document_upload;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import app.xedigital.ai.databinding.FragmentDocumentUploadBinding;

public class DocumentUploadFragment extends Fragment {

    private FragmentDocumentUploadBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        DocumentUploadViewModel documentUploadViewModel =
                new ViewModelProvider(this).get(DocumentUploadViewModel.class);

        binding = FragmentDocumentUploadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDocumentUpload;
        documentUploadViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}