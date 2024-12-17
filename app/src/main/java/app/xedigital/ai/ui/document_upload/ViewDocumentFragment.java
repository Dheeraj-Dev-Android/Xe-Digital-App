package app.xedigital.ai.ui.document_upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.DocumentListAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentViewDocumentBinding;
import app.xedigital.ai.model.getDocuments.DocumentListResponse;
import app.xedigital.ai.model.getDocuments.DocumentsItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewDocumentFragment extends Fragment {

    private FragmentViewDocumentBinding binding;
    private String authToken;
    private String userId;
    private RecyclerView recyclerView;
    private DocumentListAdapter adapter;

    public ViewDocumentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentViewDocumentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.viewUploadedDocumentRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        fetchDocuments();

        binding.uploadDocumentsChip.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_documents_to_nav_document_upload);
        });

        return root;
    }

    private void fetchDocuments() {
        APIInterface getDocuments = APIClient.getInstance().getDocumentList();
        String authHeader = "jwt " + authToken;
        Call<DocumentListResponse> call = getDocuments.getDocList(authHeader, userId);
        call.enqueue(new Callback<DocumentListResponse>() {

            @Override
            public void onResponse(@NonNull Call<DocumentListResponse> call, @NonNull Response<DocumentListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().toString();
                        Log.d("TAG", "onResponse: " + responseBody);
                        List<DocumentsItem> documents = response.body().getData().getDocuments();
                        adapter = new DocumentListAdapter(documents);
                        recyclerView.setAdapter(adapter);

                        // Show/hide emptyStateText based on data
                        if (documents.isEmpty()) {
                            binding.emptyStateText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            binding.emptyStateText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    Log.d("TAG", "onResponse: " + response.message());
                }

            }

            @Override
            public void onFailure(@NonNull Call<DocumentListResponse> call, @NonNull Throwable throwable) {

            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}