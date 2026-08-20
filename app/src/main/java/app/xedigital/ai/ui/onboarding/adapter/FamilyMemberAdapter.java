package app.xedigital.ai.ui.onboarding.adapter;

import android.app.DatePickerDialog;
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

import java.util.Calendar;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.ui.onboarding.model.FamilyMember;

public class FamilyMemberAdapter extends
        RecyclerView.Adapter<FamilyMemberAdapter.ViewHolder> {

    private final Context context;
    private final List<FamilyMember> list;
    private final OnFilePickListener listener;

    public FamilyMemberAdapter(Context context,
                               List<FamilyMember> list,
                               OnFilePickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_family_member, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        FamilyMember m = list.get(position);

        // Restore saved values
        h.etFamilyMemberName.setText(m.getFamilyMemberName());
        h.etFamilyRelationship.setText(m.getFamilyRelationship());
        h.etFamilyDob.setText(m.getFamilyDob());
        h.etFamilyMobile.setText(m.getFamilyMobile());
        h.tvAddressProofFileName.setText(m.getAddressProofFileName());

        // Show existing URL indicator
        if (m.getAddressProofFileURL() != null
                && !m.getAddressProofFileURL().isEmpty()) {
            h.tvAddressProofFileName.setText("File uploaded ✓");
        }

        // Save on focus lost – matches web onblur behaviour
        h.etFamilyMemberName.setOnFocusChangeListener((v, f) -> {
            if (!f) m.setFamilyMemberName(str(h.etFamilyMemberName));
        });
        h.etFamilyRelationship.setOnFocusChangeListener((v, f) -> {
            if (!f) m.setFamilyRelationship(str(h.etFamilyRelationship));
        });
        h.etFamilyMobile.setOnFocusChangeListener((v, f) -> {
            if (!f) m.setFamilyMobile(str(h.etFamilyMobile));
        });

        // Date picker for familyDob
        h.etFamilyDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(context, (dp, y, mo, d) -> {
                // Store as yyyy-MM-dd to match web formatDate()
                String date = String.format("%04d-%02d-%02d", y, mo + 1, d);
                h.etFamilyDob.setText(date);
                m.setFamilyDob(date);
            }, c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // File pick
        h.btnChooseFile.setOnClickListener(v ->
                listener.onFilePick(h.getAdapterPosition()));

        // Remove row – matches web removeFamilyMember(i)
        // NOTE: web only shows Remove button if familyDetails.length > 1
        h.btnRemove.setVisibility(
                list.size() > 1 ? View.VISIBLE : View.GONE);

        h.btnRemove.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID && list.size() > 1) {
                list.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, list.size());
                // Refresh all items to update Remove button visibility
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateFile(int pos, String fileName) {
        if (pos >= 0 && pos < list.size()) {
            list.get(pos).setAddressProofFileName(fileName);
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
        TextInputEditText etFamilyMemberName, etFamilyRelationship,
                etFamilyDob, etFamilyMobile;
        Button btnChooseFile;
        TextView tvAddressProofFileName;
        ImageButton btnRemove;

        ViewHolder(@NonNull View v) {
            super(v);
            etFamilyMemberName = v.findViewById(R.id.et_family_member_name);
            etFamilyRelationship = v.findViewById(R.id.et_family_relationship);
            etFamilyDob = v.findViewById(R.id.et_family_dob);
            etFamilyMobile = v.findViewById(R.id.et_family_mobile);
            btnChooseFile = v.findViewById(R.id.btn_family_choose_file);
            tvAddressProofFileName = v.findViewById(R.id.tv_address_proof_file_name);
            btnRemove = v.findViewById(R.id.btn_remove_family);
        }
    }
}