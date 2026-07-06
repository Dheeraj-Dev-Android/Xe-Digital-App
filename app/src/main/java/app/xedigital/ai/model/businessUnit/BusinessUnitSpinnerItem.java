package app.xedigital.ai.model.businessUnit;

import androidx.annotation.NonNull;

public class BusinessUnitSpinnerItem {
    private final String id;
    private final String name;

    public BusinessUnitSpinnerItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
