package app.xedigital.ai.ui.onboarding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.ui.onboarding.model.OtherDocument;

public class OtherDocumentAdapter extends
        RecyclerView.Adapter<OtherDocumentAdapter.ViewHolder> {

    private final Context context;
    private final List<OtherDocument> list;
    private final OnFilePickListener listener;
    // Track submission state for validation highlighting
    private boolean submitted = false;

    public OtherDocumentAdapter(Context context,
                                List<OtherDocument> list,
                                OnFilePickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_other_document, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        OtherDocument doc = list.get(position);

        h.etDocumentName.setText(doc.getDocumentName());
        h.tvDocumentFileName.setText(doc.getDocumentFileName());

        // Show existing URL indicator
        if (doc.getDocumentFileURL() != null
                && !doc.getDocumentFileURL().isEmpty()) {
            h.tvDocumentFileName.setText("File uploaded ✓");
        }

        // Validation highlight – matches web is-invalid behaviour
        boolean rowHasValue = !doc.getDocumentName().isEmpty()
                || doc.getDocumentFile() != null
                || !doc.getDocumentFileURL().isEmpty();

        if (submitted && rowHasValue && doc.getDocumentName().isEmpty()) {
            h.tilDocumentName.setError("Document name is required");
        } else {
            h.tilDocumentName.setError(null);
        }

        if (submitted && rowHasValue
                && doc.getDocumentFile() == null
                && doc.getDocumentFileURL().isEmpty()) {
            h.tvDocumentFileName.setTextColor(
                    context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            h.tvDocumentFileName.setTextColor(
                    context.getResources().getColor(android.R.color.darker_gray));
        }

        h.etDocumentName.setOnFocusChangeListener((v, f) -> {
            if (!f) doc.setDocumentName(str(h.etDocumentName));
        });

        h.btnChooseFile.setOnClickListener(v ->
                listener.onFilePick(h.getAdapterPosition()));

        // Remove – matches web removeDocument(i)
        // Web: *ngIf="documents.length > 0" — always show remove
        h.btnRemove.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) {
                list.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, list.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateFile(int pos, String fileName) {
        if (pos >= 0 && pos < list.size()) {
            list.get(pos).setDocumentFileName(fileName);
            notifyItemChanged(pos);
        }
    }

    private String str(TextInputEditText et) {
        return et.getText() != null
                ? et.getText().toString().trim() : "";
    }

    public interface OnFilePickListener {
        void onFilePick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextInputLayout tilDocumentName;
        TextInputEditText etDocumentName;
        Button btnChooseFile;
        TextView tvDocumentFileName;
        ImageButton btnRemove;

        ViewHolder(@NonNull View v) {
            super(v);
            tilDocumentName = v.findViewById(R.id.til_doc_name);
            etDocumentName = v.findViewById(R.id.et_doc_name);
            btnChooseFile = v.findViewById(R.id.btn_choose_doc_file);
            tvDocumentFileName = v.findViewById(R.id.tv_doc_file_name);
            btnRemove = v.findViewById(R.id.btn_remove_document);
        }
    }
}