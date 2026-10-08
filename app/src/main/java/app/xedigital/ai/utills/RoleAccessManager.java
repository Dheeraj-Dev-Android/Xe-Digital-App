package app.xedigital.ai.utills;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import app.xedigital.ai.admin.adminModal.UserDetails.UserDetailsResponse;
import app.xedigital.ai.model.user.Data;
import app.xedigital.ai.model.user.ResourcePermissionsItem;

/**
 * Single source of truth for role, resource-level permissions,
 * and branch/company module subscription toggles.
 * <p>
 * Cached in SecurePrefManager so it survives app restarts until logout.
 */
public class RoleAccessManager {

    private static final String TAG = "RoleAccessManager";

    private static final String KEY_ROLE_NAME = "role_name";
    private static final String KEY_ROLE_DISPLAY = "role_display_name";
    private static final String KEY_RESOURCE_MAP_JSON = "resource_access_map_json";
    private static final String KEY_BRANCH_PERMS_JSON = "branch_permissions_json";

    private static RoleAccessManager instance;
    private final SecurePrefManager prefManager;
    private final Gson gson = new Gson();

    // resource_name (lowercase) → set of permission names ("read","create","update","delete","approve")
    private Map<String, Set<String>> resourceAccessMap = new HashMap<>();

    // branch module key (lowercase, e.g. "payroll") → enabled boolean
    private Map<String, Boolean> companyBranchMenuPermissions = new HashMap<>();

    private String roleName = "";
    private String roleDisplayName = "";

    private RoleAccessManager(Context context) {
        prefManager = SecurePrefManager.getInstance(context);
        loadFromCache();
    }

    public static synchronized RoleAccessManager getInstance(Context context) {
        if (instance == null) {
            instance = new RoleAccessManager(context.getApplicationContext());
        }
        return instance;
    }

    // ─── Apply Employee Payload (UserModelResponse.Data) ─────────────
    public void applyUserData(Data data) {
        if (data == null) return;

        resourceAccessMap = new HashMap<>();

        if (data.getRole() != null) {
            roleName = safe(data.getRole().getName());
            roleDisplayName = safe(data.getRole().getDisplayName());
        }

        if (data.getResourcePermissions() != null) {
            for (ResourcePermissionsItem item : data.getResourcePermissions()) {
                if (item == null || item.getResource() == null) continue;
                String resourceName = safe(item.getResource().getName());
                if (resourceName.isEmpty()) continue;

                Set<String> perms = new HashSet<>();
                if (item.getPermissions() != null) {
                    for (Object permObj : item.getPermissions()) {
                        String permName = extractPermissionName(permObj);
                        if (permName != null) perms.add(permName.toLowerCase());
                    }
                }
                resourceAccessMap.put(resourceName.toLowerCase(), perms);
            }
        }

        // Parse branch permissions dynamically (No compile dependency on Branch getters)
        companyBranchMenuPermissions = new HashMap<>();
        if (data.getBranch() != null) {
            extractBranchMenuPermissionsFromObject(data.getBranch());
        }

        persistToCache();
        Log.d(TAG, "Applied EMPLOYEE role=" + roleName + " resources=" + resourceAccessMap.keySet()
                + " branchPerms=" + companyBranchMenuPermissions);
    }

    // ─── Apply Admin Payload (UserDetailsResponse) ───────────────────
    public void applyAdminDetails(UserDetailsResponse adminResponse) {
        if (adminResponse == null || adminResponse.getData() == null) return;

        resourceAccessMap = new HashMap<>();
        app.xedigital.ai.admin.adminModal.UserDetails.Data data = adminResponse.getData();

        if (data.getRole() != null) {
            roleName = safe(data.getRole().getName());
            roleDisplayName = safe(data.getRole().getDisplayName());
        }

        // Admin UserDetailsResponse dynamic resource permission extraction
        try {
            String json = gson.toJson(data);
            Map<String, Object> rawMap = gson.fromJson(json,
                    new TypeToken<Map<String, Object>>() {
                    }.getType());

            if (rawMap != null && rawMap.containsKey("resourcePermissions")) {
                Object resPermsObj = rawMap.get("resourcePermissions");
                if (resPermsObj != null) {
                    String resJson = gson.toJson(resPermsObj);
                    java.util.List<Map<String, Object>> list = gson.fromJson(resJson,
                            new TypeToken<java.util.List<Map<String, Object>>>() {
                            }.getType());

                    if (list != null) {
                        for (Map<String, Object> entry : list) {
                            if (entry == null) continue;
                            Object resObj = entry.get("resource");
                            if (!(resObj instanceof Map)) continue;
                            Object nameObj = ((Map<?, ?>) resObj).get("name");
                            if (nameObj == null) continue;

                            String resName = nameObj.toString().toLowerCase();
                            Set<String> perms = new HashSet<>();
                            Object permsObj = entry.get("permissions");
                            if (permsObj instanceof java.util.List) {
                                for (Object p : (java.util.List<?>) permsObj) {
                                    String pn = extractPermissionName(p);
                                    if (pn != null) perms.add(pn.toLowerCase());
                                }
                            }
                            resourceAccessMap.put(resName, perms);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing admin resourcePermissions: " + e.getMessage());
        }

        // Parse branch permissions dynamically (No compile dependency on Branch getters)
        companyBranchMenuPermissions = new HashMap<>();
        if (data.getBranch() != null) {
            extractBranchMenuPermissionsFromObject(data.getBranch());
        }

        persistToCache();
        Log.d(TAG, "Applied ADMIN role=" + roleName + " resources=" + resourceAccessMap.keySet()
                + " branchPerms=" + companyBranchMenuPermissions);
    }

    // ─── Dynamic Generic Branch Extractor ────────────────────────────
    private void extractBranchMenuPermissionsFromObject(Object branchObj) {
        if (branchObj == null) return;
        try {
            String branchJson = gson.toJson(branchObj);
            Map<String, Object> branchMap = gson.fromJson(branchJson,
                    new TypeToken<Map<String, Object>>() {
                    }.getType());

            if (branchMap != null && branchMap.containsKey("menuPermissions")) {
                Object menuPerms = branchMap.get("menuPermissions");
                if (menuPerms != null) {
                    String json = gson.toJson(menuPerms);
                    Map<String, Map<String, Object>> rawMap = gson.fromJson(json,
                            new TypeToken<Map<String, Map<String, Object>>>() {
                            }.getType());

                    if (rawMap != null) {
                        for (Map.Entry<String, Map<String, Object>> entry : rawMap.entrySet()) {
                            if (entry.getValue() != null && entry.getValue().containsKey("enabled")) {
                                Object val = entry.getValue().get("enabled");
                                boolean enabled = val instanceof Boolean && (Boolean) val;
                                companyBranchMenuPermissions.put(entry.getKey().toLowerCase(), enabled);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed parsing branch menu permissions: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractPermissionName(Object permObj) {
        try {
            if (permObj instanceof Map) {
                Object name = ((Map<String, Object>) permObj).get("name");
                return name != null ? name.toString() : null;
            }
        } catch (Exception e) {
            Log.e(TAG, "extractPermissionName error: " + e.getMessage());
        }
        return null;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // ─── Public Verification APIs ─────────────────────────────────────

    public boolean hasAccess(String resourceName) {
        if (resourceName == null) return false;
        return resourceAccessMap.containsKey(resourceName.toLowerCase());
    }

    public boolean hasPermission(String resourceName, String permissionName) {
        if (resourceName == null || permissionName == null) return false;
        Set<String> perms = resourceAccessMap.get(resourceName.toLowerCase());
        return perms != null && perms.contains(permissionName.toLowerCase());
    }

    public boolean hasAnyAccess(String... resourceNames) {
        for (String r : resourceNames) {
            if (hasAccess(r)) return true;
        }
        return false;
    }

    public boolean isCompanyModuleEnabled(String moduleKey) {
        if (moduleKey == null) return false;
        Boolean enabled = companyBranchMenuPermissions.get(moduleKey.toLowerCase());
        return enabled != null && enabled;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleDisplayName() {
        return roleDisplayName;
    }

    // ─── Cache Operations ─────────────────────────────────────────────

    private void persistToCache() {
        prefManager.putString(KEY_ROLE_NAME, roleName);
        prefManager.putString(KEY_ROLE_DISPLAY, roleDisplayName);
        prefManager.putString(KEY_RESOURCE_MAP_JSON, gson.toJson(resourceAccessMap));
        prefManager.putString(KEY_BRANCH_PERMS_JSON, gson.toJson(companyBranchMenuPermissions));
    }

    private void loadFromCache() {
        roleName = prefManager.getString(KEY_ROLE_NAME, "");
        roleDisplayName = prefManager.getString(KEY_ROLE_DISPLAY, "");

        String jsonResources = prefManager.getString(KEY_RESOURCE_MAP_JSON, null);
        if (jsonResources != null) {
            try {
                Type type = new TypeToken<Map<String, Set<String>>>() {
                }.getType();
                Map<String, Set<String>> map = gson.fromJson(jsonResources, type);
                if (map != null) resourceAccessMap = map;
            } catch (Exception e) {
                Log.e(TAG, "loadFromCache resources parse error: " + e.getMessage());
            }
        }

        String jsonBranch = prefManager.getString(KEY_BRANCH_PERMS_JSON, null);
        if (jsonBranch != null) {
            try {
                Type type = new TypeToken<Map<String, Boolean>>() {
                }.getType();
                Map<String, Boolean> map = gson.fromJson(jsonBranch, type);
                if (map != null) companyBranchMenuPermissions = map;
            } catch (Exception e) {
                Log.e(TAG, "loadFromCache branch parse error: " + e.getMessage());
            }
        }
    }

    public void clear() {
        resourceAccessMap.clear();
        companyBranchMenuPermissions.clear();
        roleName = "";
        roleDisplayName = "";
        prefManager.remove(KEY_ROLE_NAME);
        prefManager.remove(KEY_ROLE_DISPLAY);
        prefManager.remove(KEY_RESOURCE_MAP_JSON);
        prefManager.remove(KEY_BRANCH_PERMS_JSON);
    }
}