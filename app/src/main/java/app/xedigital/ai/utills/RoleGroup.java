package app.xedigital.ai.utills;

/**
 * Central classification of user roles returned by the backend.
 * Backend currently sends:
 * - "employee"         → Employee tier (uses MainActivity + FaceLogin)
 * - "branchadmin"      → Admin tier (uses AdminMainActivity, skips FaceLogin)
 * - "humanresource"    → Admin tier (uses AdminMainActivity, skips FaceLogin)
 */
public class RoleGroup {

    public static boolean isEmployeeTier(String roleName) {
        return "employee".equalsIgnoreCase(roleName);
    }

    public static boolean isAdminTier(String roleName) {
        return "branchadmin".equalsIgnoreCase(roleName)
                || "humanresource".equalsIgnoreCase(roleName);
    }
}