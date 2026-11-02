package app.xedigital.ai.model.employeeClaim;

import java.io.Serializable;

public class Department implements Serializable {
    private String createdAt;
    private boolean jsonMemberDefault;
    private String createdBy;
    private int v;
    private String name;
    private String description;
    private boolean active;
    private String company;
    private String id;
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isJsonMemberDefault() {
        return jsonMemberDefault;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getV() {
        return v;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public String getCompany() {
        return company;
    }

    public String getId() {
        return id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
