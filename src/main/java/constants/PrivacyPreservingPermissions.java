package constants;

import java.util.HashMap;
import java.util.jar.Manifest;

public class PrivacyPreservingPermissions {
    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";

    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";

    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";


    private static final HashMap<String, String> privacyPreservingPermissionMappings = new HashMap<String, String>() {
        {
            put(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION);
            put(WRITE_CONTACTS, READ_CONTACTS);
            put(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);
        }
    };

    public static HashMap<String, String> getPrivacyPreservingPermissionMappings() {
        return privacyPreservingPermissionMappings;
    }
}
