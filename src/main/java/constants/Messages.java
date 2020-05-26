package constants;

import java.util.ArrayList;

public class Messages {
    public static final String MISMATCH_HANDLER_UI = "There is one permission usage mismatch between this listener handler code (uses %s) and its associated layout (infers to %s).";
    public static final String MISSING_PERMISSION_IN_ANDROID_MANIFEST = "<html><body>This code requires permission %s to function properly. Consider using our quickfix to add these permissions into AndroidManifest.xml file.</body></html>";
    public static final String REDUNDANT_REQUEST = "This permission request (uses %s) does not match the purpose of its associated layout, consider using a clear request rationale to explain it or moving it to a proper Activity or Fragment.";
    public static final String MISMATCH_REQUEST = "This permission request (uses %s) does not match the purpose of its associated layout (infers to %s), consider using a clear request rationale to explain it or moving it to a proper Activity or Fragment.";
    public static final String NON_PRIVACY_PRESERVING = "This permission request does not ensure data integrity for users compared with %s, consider changing to %s while it still ensures the app functionality to work properly.";
    public static final String NON_PRIVACY_PRESERVING_LOCATION = "This permission request %s (~10m accuracy) is less privacy-preserving for users compared with %s (~100m accuracy), consider changing to %s while it still ensures the app functionality to work properly.";
    public static final String UNCLEAR_RATIONALE = "This permission request rationale is not clear, consider updating it to avoid user confusion and increase the chance to be granted.";

    public static String getMessage(String requestedPermission, String suggestedPermission, ArrayList<String> usedPermissions, ArrayList<String> advertisedPermissions, WarningType warningType) {
        switch (warningType) {
            case REDUNDANT_REQUEST:
                return String.format(Messages.REDUNDANT_REQUEST, requestedPermission.split("\\.")[2]);
            case MISMATCH_REQUEST:
                return String.format(Messages.MISMATCH_REQUEST, requestedPermission.split("\\.")[2], retrievePermissionsText(advertisedPermissions));
            case MISMATCH_HANDLER_UI:
                return String.format(Messages.MISMATCH_HANDLER_UI, retrievePermissionsText(usedPermissions), retrievePermissionsText(advertisedPermissions));
            case MISSING_PERMISSION:
                return String.format(Messages.MISSING_PERMISSION_IN_ANDROID_MANIFEST, usedPermissions.toString());
            case UNCLEAR_RATIONALE:
                return UNCLEAR_RATIONALE;
            case NON_PRIVACY_PRESERVING:
                return String.format(Messages.NON_PRIVACY_PRESERVING, suggestedPermission.split("\\.")[2], suggestedPermission.split("\\.")[2]);
            case NON_PRIVACY_PRESERVING_LOCATION:
                return String.format(Messages.NON_PRIVACY_PRESERVING_LOCATION, requestedPermission.split("\\.")[2], suggestedPermission.split("\\.")[2], suggestedPermission.split("\\.")[2]);
            default:
                return null;
        }
    }

    /**
     *
     * @param permissions
     * @return a message concatenates all the advertised permissions
     */
    private static String retrievePermissionsText(ArrayList<String> permissions) {
        StringBuilder builder = new StringBuilder();
        for (String permissionGroup : permissions) {
            if (builder.length() == 0) {
                builder.append(permissionGroup.split("\\.")[2]);
            } else {
                builder.append(", " + permissionGroup.split("\\.")[2]);
            }
        }

        return builder.toString();
    }
}
