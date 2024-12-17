package app.xedigital.ai.adapter;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.getDocuments.DocumentsItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class DocumentListAdapter extends RecyclerView.Adapter<DocumentListAdapter.ViewHolder> {

    private List<DocumentsItem> documentList;

    public DocumentListAdapter(List<DocumentsItem> documentList) {
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentsItem document = documentList.get(position);
        holder.documentNameTextView.setText(document.getDocumentName());
        holder.employeeNameTextView.setText(document.getEmpFirstName() + " " + document.getEmpLastName());
        holder.createdAtTextView.setText(DateTimeUtils.getDayOfWeekAndDate(document.getCreatedAt()));
//        holder.documentUrlTextView.setText(document.getDocFileURL());
//        holder.documentIcon.setOnClickListener(v -> {
//            // Open document using the URL
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.parse(document.getDocFileURL()), "application/*");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            try {
//                holder.itemView.getContext().startActivity(intent);
//            } catch (android.content.ActivityNotFoundException e) {
//                Log.e("DocumentListAdapter", "Error opening document: " + e.getMessage());
//            }
//        });
        holder.documentIcon.setOnClickListener(v -> {
            // Show the image in a dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
            View dialogView = inflater.inflate(R.layout.image_viewer_dialog, null);
            builder.setView(dialogView);

            ImageView imageView = dialogView.findViewById(R.id.imageView);

            Glide.with(holder.itemView.getContext()).load(document.getDocFileURL()).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    imageView.setImageDrawable(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    // Handle if the image load is cleared
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("ShiftApprovalListAdapter", "Error parsing date", e);
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView documentNameTextView;
        public TextView employeeNameTextView;
        public TextView createdAtTextView;
        //        public TextView documentUrlTextView;
        public ImageView documentIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            documentNameTextView = itemView.findViewById(R.id.documentNameTextView);
            employeeNameTextView = itemView.findViewById(R.id.employeeNameTextView);
            createdAtTextView = itemView.findViewById(R.id.createdAtTextView);
//            documentUrlTextView = itemView.findViewById(R.id.documentUrlTextView);
            documentIcon = itemView.findViewById(R.id.documentIcon);
        }
    }
}