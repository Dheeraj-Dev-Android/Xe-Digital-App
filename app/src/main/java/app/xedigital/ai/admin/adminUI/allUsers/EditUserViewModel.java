package app.xedigital.ai.admin.adminUI.allUsers;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.AdminUsers.UsersItem;
import app.xedigital.ai.admin.adminModal.Role.RolesItem;
import app.xedigital.ai.admin.adminModal.Role.UserRoleResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserViewModel extends ViewModel {

    private final MutableLiveData<List<RolesItem>> rolesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<UpdateResult> updateResult = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();

    private List<RolesItem> cachedRoles = new ArrayList<>();

    public LiveData<List<RolesItem>> getRolesLiveData() {
        return rolesLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<UpdateResult> getUpdateResult() {
        return updateResult;
    }

    public LiveData<String> getValidationError() {
        return validationError;
    }

    public List<RolesItem> getCachedRoles() {
        return cachedRoles;
    }

    public void fetchRoles(String authToken) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        apiService.getRoles(authToken).enqueue(new Callback<UserRoleResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserRoleResponse> call, @NonNull Response<UserRoleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cachedRoles = response.body().getData().getRoles();
                    rolesLiveData.postValue(cachedRoles);
                } else {
                    rolesLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoleResponse> call, @NonNull Throwable t) {
                rolesLiveData.postValue(null);
            }
        });
    }

    public PasswordRuleStatus evaluatePasswordRules(String password, String firstName, String lastName) {
        PasswordRuleStatus s = new PasswordRuleStatus();

        s.lengthOk = password.length() >= 16 && password.length() <= 18;
        s.upperOk = password.matches(".*[A-Z].*");
        s.lowerOk = password.matches(".*[a-z].*");
        s.numberOk = password.matches(".*[0-9].*");
        s.specialOk = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|.<>/?`~].*");
        s.noSpaceCommaOk = !password.contains(" ") && !password.contains(",");

        String fn = firstName == null ? "" : firstName.trim().toLowerCase();
        String ln = lastName == null ? "" : lastName.trim().toLowerCase();
        String pwLower = password.toLowerCase();

        boolean nameValid = fn.isEmpty() || !pwLower.contains(fn);
        if (!ln.isEmpty() && pwLower.contains(ln)) nameValid = false;
        s.noNameOk = nameValid;

        s.noSequentialOk = !hasSequentialChars(password, 4);
        s.noRepeatedOk = !hasRepeatedChars(password, 4);

        return s;
    }

    public boolean hasSequentialChars(String password, int sequenceLength) {
        if (password.length() < sequenceLength) return false;
        String lower = password.toLowerCase();
        for (int i = 0; i <= lower.length() - sequenceLength; i++) {
            boolean asc = true, desc = true;
            for (int j = 0; j < sequenceLength - 1; j++) {
                char c = lower.charAt(i + j);
                char n = lower.charAt(i + j + 1);
                if (n - c != 1) asc = false;
                if (c - n != 1) desc = false;
            }
            if (asc || desc) return true;
        }
        return false;
    }

    public boolean hasRepeatedChars(String password, int repeatLength) {
        if (password.length() < repeatLength) return false;
        String lower = password.toLowerCase();
        for (int i = 0; i <= lower.length() - repeatLength; i++) {
            boolean allSame = true;
            char first = lower.charAt(i);
            for (int j = 1; j < repeatLength; j++) {
                if (lower.charAt(i + j) != first) {
                    allSame = false;
                    break;
                }
            }
            if (allSame) return true;
        }
        return false;
    }

    public boolean validateForm(String firstName, String lastName, String email, String roleName, String status, String password, String confirmPassword) {

        if (firstName.isEmpty()) {
            validationError.setValue("First name is required.");
            return false;
        }
        if (lastName.isEmpty()) {
            validationError.setValue("Last name is required.");
            return false;
        }
        if (email.isEmpty()) {
            validationError.setValue("Email is required.");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            validationError.setValue("Please enter a valid email address.");
            return false;
        }
        if (roleName.isEmpty()) {
            validationError.setValue("Please select a role.");
            return false;
        }
        if (status.isEmpty()) {
            validationError.setValue("Please select a status.");
            return false;
        }

        // Password required for admin update (admin always sets password)
        if (password.isEmpty()) {
            validationError.setValue("Password is required.");
            return false;
        }

        if (password.length() < 16) {
            validationError.setValue("Password must be at least 16 characters.");
            return false;
        }
        if (password.length() > 18) {
            validationError.setValue("Password cannot exceed 18 characters.");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            validationError.setValue("Password must contain at least one uppercase letter.");
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            validationError.setValue("Password must contain at least one lowercase letter.");
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            validationError.setValue("Password must contain at least one number.");
            return false;
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|.<>/?`~].*")) {
            validationError.setValue("Password must contain at least one special character.");
            return false;
        }
        if (password.contains(" ") || password.contains(",")) {
            validationError.setValue("Password must not contain spaces or commas.");
            return false;
        }

        String fn = firstName.toLowerCase();
        String ln = lastName.toLowerCase();
        if (!fn.isEmpty() && password.toLowerCase().contains(fn)) {
            validationError.setValue("Password must not contain the user's first name.");
            return false;
        }
        if (!ln.isEmpty() && password.toLowerCase().contains(ln)) {
            validationError.setValue("Password must not contain the user's last name.");
            return false;
        }
        if (hasSequentialChars(password, 4)) {
            validationError.setValue("Password must not contain sequential characters (e.g., 1234, abcd).");
            return false;
        }
        if (hasRepeatedChars(password, 4)) {
            validationError.setValue("Password must not contain repeated characters (e.g., aaaa, 1111).");
            return false;
        }
        if (confirmPassword.isEmpty()) {
            validationError.setValue("Please confirm the password.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            validationError.setValue("Passwords do not match.");
            return false;
        }

        return true;
    }

    public String resolveRoleId(String roleDisplayName) {
        if (cachedRoles == null) return null;
        for (RolesItem r : cachedRoles) {
            if (r.getDisplayName().equalsIgnoreCase(roleDisplayName)) {
                return r.getId();
            }
        }
        return null;
    }

    public void updateUser(String authToken, UsersItem selectedUser, String firstName, String lastName, String email, String roleName, String status, String password, String confirmPassword) {

        String roleId = resolveRoleId(roleName);
        if (roleId == null) {
            updateResult.setValue(new UpdateResult(false, "Invalid role selected."));
            return;
        }

        if (selectedUser == null || selectedUser.getCompany() == null || selectedUser.getBranch() == null) {
            updateResult.setValue(new UpdateResult(false, "User data is incomplete. Cannot proceed."));
            return;
        }

        boolean isActive = status.equalsIgnoreCase("Active");

        JSONObject json = new JSONObject();
        try {
            json.put("company", selectedUser.getCompany().getId());
            json.put("branch", selectedUser.getBranch().getId());
            json.put("role", roleId);
            json.put("firstname", firstName);
            json.put("lastname", lastName);
            json.put("email", email);
            json.put("active", isActive);
            json.put("password", password);
            json.put("confirmPassword", confirmPassword);
        } catch (JSONException e) {
            updateResult.setValue(new UpdateResult(false, "Error building request. Please try again."));
            return;
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        isLoading.setValue(true);

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<ResponseBody> call = apiService.updateUser(authToken, selectedUser.getId(), body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    updateResult.postValue(new UpdateResult(true, "User updated successfully."));
                } else {
                    updateResult.postValue(new UpdateResult(false, "Update failed. Server returned code " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                isLoading.postValue(false);
                updateResult.postValue(new UpdateResult(false, "Network error. Please try again."));
            }
        });
    }

    public void clearUpdateResult() {
        updateResult.setValue(null);
    }

    public void clearValidationError() {
        validationError.setValue(null);
    }

    public static class PasswordRuleStatus {
        public boolean lengthOk;
        public boolean upperOk;
        public boolean lowerOk;
        public boolean numberOk;
        public boolean specialOk;
        public boolean noSpaceCommaOk;
        public boolean noNameOk;
        public boolean noSequentialOk;
        public boolean noRepeatedOk;

        public boolean allPassed() {
            return lengthOk && upperOk && lowerOk && numberOk && specialOk && noSpaceCommaOk && noNameOk && noSequentialOk && noRepeatedOk;
        }
    }

    public static class UpdateResult {
        public final boolean success;
        public final String message;

        public UpdateResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}