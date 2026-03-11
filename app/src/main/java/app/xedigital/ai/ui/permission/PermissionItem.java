package app.xedigital.ai.ui.permission;

public class PermissionItem {
    private final String title;
    private final String description;
    private final String manifestPermission;
    private final boolean isMandatory;
    private final String tag;
    private boolean isGranted;

    public PermissionItem(String title, String description, String manifestPermission, boolean isMandatory, String tag) {
        this.title = title;
        this.description = description;
        this.manifestPermission = manifestPermission;
        this.isMandatory = isMandatory;
        this.tag = tag;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getManifestPermission() {
        return manifestPermission;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean isGranted() {
        return isGranted;
    }

    public void setGranted(boolean granted) {
        isGranted = granted;
    }

    public String getTag() {
        return tag;
    }
}
