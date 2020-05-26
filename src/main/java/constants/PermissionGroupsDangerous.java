package constants;

import java.util.Arrays;
import java.util.HashSet;

public class PermissionGroupsDangerous {

    // 8 security-privacy dangerous permission group
    public static final String SMS = "android.permission-group.SMS";
    public static final String CONTACTS = "android.permission-group.CONTACTS";
    public static final String CALENDAR = "android.permission-group.CALENDAR";
    public static final String LOCATION = "android.permission-group.LOCATION";
    public static final String MICROPHONE = "android.permission-group.MICROPHONE";
    public static final String CAMERA = "android.permission-group.CAMERA";
    public static final String PHONE = "android.permission-group.PHONE";
    public static final String STORAGE = "android.permission-group.STORAGE";

    public static HashSet<String> permissionGroupsDangerous = new HashSet<>(Arrays.asList(SMS, CONTACTS, CALENDAR, LOCATION, MICROPHONE, CAMERA, PHONE, STORAGE));
}
