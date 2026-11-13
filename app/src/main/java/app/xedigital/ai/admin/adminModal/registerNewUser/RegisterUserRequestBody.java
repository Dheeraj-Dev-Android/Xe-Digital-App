package app.xedigital.ai.admin.adminModal.registerNewUser;

import com.google.gson.annotations.SerializedName;

public class RegisterUserRequestBody {

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;

    @SerializedName("active")
    private boolean active;

    @SerializedName("confirmPassword")
    private String confirmPassword;

    @SerializedName("company")
    private String company;

    @SerializedName("branch")
    private String branch;

    @SerializedName("email")
    private String email;

    @SerializedName("lastname")
    private String lastname;

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}