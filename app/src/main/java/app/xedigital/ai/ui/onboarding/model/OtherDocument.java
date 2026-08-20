package app.xedigital.ai.ui.onboarding.model;

import android.net.Uri;

/**
 * Field names match EXACTLY with web formArrayName "documents"
 */
public class OtherDocument {

    private String documentName = "";
    private Uri documentFile;
    private String documentFileName = "No file chosen";
    private String documentFileURL = "";
    private String documentFileURLKey = "";

    public OtherDocument() {
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String v) {
        documentName = v;
    }

    public Uri getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(Uri v) {
        documentFile = v;
    }

    public String getDocumentFileName() {
        return documentFileName;
    }

    public void setDocumentFileName(String v) {
        documentFileName = v;
    }

    public String getDocumentFileURL() {
        return documentFileURL;
    }

    public void setDocumentFileURL(String v) {
        documentFileURL = v;
    }

    public String getDocumentFileURLKey() {
        return documentFileURLKey;
    }

    public void setDocumentFileURLKey(String v) {
        documentFileURLKey = v;
    }
}