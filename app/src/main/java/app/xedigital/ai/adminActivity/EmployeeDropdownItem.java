package app.xedigital.ai.adminActivity;

import androidx.annotation.NonNull;

public class EmployeeDropdownItem {
    private final String id;
    private final String firstName;
    private final String lastName;

    public EmployeeDropdownItem(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName == null ? "" : lastName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @NonNull
    @Override
    public String toString() {
        // This string is what the dropdown shows
        return lastName.isEmpty()
                ? firstName
                : firstName + " " + lastName;
    }
}
