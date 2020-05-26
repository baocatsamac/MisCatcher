package helper;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.xml.XmlTag;
import constants.PermissionGroupsDangerous;
import constants.Signatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RationaleHelper {

    private static final String[] RATIONALE_KEYWORDS = {"access", "_access", "_access_", "access_", "permission", "grant", "perm_", "_perm_", "perm", "_perm",
            "perms_", "_perms_", "_perms", "perms", "explain", "explanation", "hint", "rationale", "toast"};

    // list of keywords to detect whether a rationale message has a clear intention
    private static final String[] GENERAL_CLEAR_RATIONALE_KEYWORDS = {"required", "require", "please", "without", "to use", "otherwise", "if", "function properly", "not work"};
    private static final String[] LOCATION_CLEAR_RATIONALE_KEYWORDS = {"to function", "to work", "to determine", "to validate", "to locate", "to carry out", "to query", "to notify", "to query", "to detect", "to verify", "to transmit", "position", "to search nearby"};
    private static final String[] STORAGE_CLEAR_RATIONALE_KEYWORDS = {"to save", "to upload", "to load", "to write", "to access", "to view", "to download", "to read", "to store", "to play", "to change", "to display", "to create"};
    private static final String[] PHONE_CLEAR_RATIONALE_KEYWORDS = {"to pause music", "to make call", "to call", "to sign in via imei", "to pause radio", "to read phone status", "to display", "to diagnose issue", "to backup", "to restore", "to improve", "to earn", "to redeem", "to show info", "to read sms stats", "to secure your account", "to sync sessions"};
    private static final String[] CONTACTS_CLEAR_RATIONALE_KEYWORDS = {"to choose recipients", "to check if", "to access your account", "to sign in", "google account", "to add", "to save", "to access your address book", "to import", "for planning trips", "to read", "to login", "to signup", "to provide email completions", "to invite friends", "to auto complete", "to auto correct", "google signin", "to load", "to call", "to verify", "to set a contact ringtone", "to connect", "to share", "to assign a ringtone", "to find friends", "to send invitations", "to display", "to follow", "completions", "to sync", "to add a number", "ringtone", "to connect", "to create groups"};
    private static final String[] CAMERA_CLEAR_RATIONALE_KEYWORDS = {"to take photo", "to make a cover", "to scan", "to make a photo", "to record video", "video recording", "barcode scanning", "augmented reality", "to login via qr code", "flashlight", "ocr", "qr code", "to make a scan", "take picture"};
    private static final String[] MICROPHONE_CLEAR_RATIONALE_KEYWORDS = {"to record audio", "voice messages", "player", "to record video", "video call", "calling experience", "audio recording", "audio recorder", "voicemail", "video recording", "video shooting", "audio content", "to record your voice"};
    private static final String[] SMS_CLEAR_RATIONALE_KEYWORDS = {"authentication codes", "to send", "to scan", "to read", "to notify", "otp", "incoming sms", "to write", "to backup", "to restore", "to set", "to invite", "sms code", "to receive", "verification", "notification tone", "mms", "to edit", "to display", "to reply", "to find phishing links", "to translate", "to avoid", "to verify", "to block", "to insert", "to view", "to share", "to inform", "to pay", "to catch", "notification", "invitation", "emergency"};
    private static final String[] CALENDAR_CLEAR_RATIONALE_KEYWORDS = {"appointment", "trip", "to sync", "reservation", "holidays", "to add", "events", "to share", "to display", "reminder", "to import", "to create", "to pick", "places", "to confirm", "to view", "invitation", "to show", "to receive", "notification", "to manage", "to read", "to retrieve"};

    // list of good/clear rationale message for each particular type
    private static final String CLEAR_LOCATION_RATIONALE = "Location permission is required for app %s. Please grant the permission.";
    private static final String CLEAR_STORAGE_RATIONALE = "Storage permission is required for app  %s. Please grant the permission.";
    private static final String CLEAR_PHONE_RATIONALE = "Phone permission is required for app %s. Please grant the permission.";
    private static final String CLEAR_CONTACTS_RATIONALE = "Contacts permission is required for app %s. Please grant the permission.";
    private static final String CLEAR_CAMERA_RATIONALE = "Camera permission is required for app %s. Please grant the permission.";
    private static final String CLEAR_MIRCROPHONE_RATIONALE = "Microphone permission is required for app %s. Please grant the permission.";
    private static final String CLEAR_SMS_RATIONALE = "Sms permission is required for app %s. Please grant the permission.";
    private static final String CLEAR_CALENDAR_RATIONALE = "Calendar permission is required for app %s. Please grant the permission.";
    private static final String RATIONALE_HINT = "[please specify your permission request purpose here]";

    private static final HashMap<String, String[]> permissionGroupsWithCorrespondingClearKeywords = new HashMap<String, String[]>() {
        {
            put(PermissionGroupsDangerous.LOCATION, LOCATION_CLEAR_RATIONALE_KEYWORDS);
            put(PermissionGroupsDangerous.PHONE, PHONE_CLEAR_RATIONALE_KEYWORDS);
            put(PermissionGroupsDangerous.MICROPHONE, MICROPHONE_CLEAR_RATIONALE_KEYWORDS);
            put(PermissionGroupsDangerous.CALENDAR, CALENDAR_CLEAR_RATIONALE_KEYWORDS);
            put(PermissionGroupsDangerous.SMS, SMS_CLEAR_RATIONALE_KEYWORDS);
            put(PermissionGroupsDangerous.STORAGE, STORAGE_CLEAR_RATIONALE_KEYWORDS);
            put(PermissionGroupsDangerous.CAMERA, CAMERA_CLEAR_RATIONALE_KEYWORDS);
            put(PermissionGroupsDangerous.CONTACTS, CONTACTS_CLEAR_RATIONALE_KEYWORDS);
        }
    };


    /**
     * Design a keyword matching technique to annotate whether a string variable contains mentions of a permission group.
     * More specifically, we assign a binary label to each string variable by matching the variable’s name or content
     * against 18 keywords referring to permission groups, including "permission", "rationale", and "toast", etc.
     * https://sites.google.com/view/runtimepermissionproject/supplementary-material?authuser=0
     *
     * @param rationaleMessage
     * @return whether the passed message is an actual permission rationale message or not
     */
    public static boolean isActualPermissionRationale(String rationaleMessage) {
        boolean isActualPermissionRationale = false;
        for (String keyword : RATIONALE_KEYWORDS) {
            if (rationaleMessage.contains(keyword)) {
                isActualPermissionRationale = true;
                break;
            }
        }
        return isActualPermissionRationale;
    }

    /**
     * @param rationaleMessage
     * @return whether the permission request rationale content explains its intention clearly to users
     */
    public static boolean isClearRationale(String rationaleMessage, String permissionGroup) {
        boolean isClearRationale = false;
        ArrayList<String> keywords = new ArrayList<>(Arrays.asList(GENERAL_CLEAR_RATIONALE_KEYWORDS));
        switch (permissionGroup) {
            case PermissionGroupsDangerous.LOCATION:

                keywords.addAll(Arrays.asList(LOCATION_CLEAR_RATIONALE_KEYWORDS));
                break;
            case PermissionGroupsDangerous.STORAGE:
                keywords.addAll(Arrays.asList(STORAGE_CLEAR_RATIONALE_KEYWORDS));
                break;
            case PermissionGroupsDangerous.PHONE:
                keywords.addAll(Arrays.asList(PHONE_CLEAR_RATIONALE_KEYWORDS));
                break;
            case PermissionGroupsDangerous.CONTACTS:
                keywords.addAll(Arrays.asList(CONTACTS_CLEAR_RATIONALE_KEYWORDS));
                break;
            case PermissionGroupsDangerous.CAMERA:
                keywords.addAll(Arrays.asList(CAMERA_CLEAR_RATIONALE_KEYWORDS));
                break;
            case PermissionGroupsDangerous.MICROPHONE:
                keywords.addAll(Arrays.asList(MICROPHONE_CLEAR_RATIONALE_KEYWORDS));
                break;
            case PermissionGroupsDangerous.SMS:
                keywords.addAll(Arrays.asList(SMS_CLEAR_RATIONALE_KEYWORDS));
                break;
            case PermissionGroupsDangerous.CALENDAR:
                keywords.addAll(Arrays.asList(CALENDAR_CLEAR_RATIONALE_KEYWORDS));
                break;
        }

        for (String keyword : keywords) {
            if (rationaleMessage.contains(keyword)) {
                isClearRationale = true;
                break;
            }
        }
        return isClearRationale;
    }

    /**
     * Retrieve one suggested permission rationale content to apply in quickfix to avoid non-transparency
     * First criteria is based on rationale type, then later based on permission group
     *
     * @param permissionGroup
     * @param rationaleType
     * @return corresponding suggested clear rationale to replace
     */
    public static String getClearRationale(String permissionGroup, String rationaleType, HashMap<XmlTag, ArrayList<String>> keywordsFromLayout) {
        String clearRationale = null;
        String properActions;
        String checkedCriteria = rationaleType; // prioritize checking according to rationale type first, then permissionGroup
        if (rationaleType == null) {
            checkedCriteria = permissionGroup;
        }
        switch (checkedCriteria) {
            case PermissionGroupsDangerous.LOCATION:
                properActions = drawProperActionFromKeywords(keywordsFromLayout, PermissionGroupsDangerous.LOCATION);
                if (properActions != null){
                    clearRationale = String.format(CLEAR_LOCATION_RATIONALE, properActions);
                } else {
                    // use default message if we cannot draw out anything from the associated layout
                    clearRationale = String.format(CLEAR_LOCATION_RATIONALE, RATIONALE_HINT);
                }

                break;
            case PermissionGroupsDangerous.STORAGE:
                properActions = drawProperActionFromKeywords(keywordsFromLayout, PermissionGroupsDangerous.STORAGE);
                if (properActions != null){
                    clearRationale = String.format(CLEAR_STORAGE_RATIONALE, properActions);
                } else {
                    // use default message if we cannot draw out anything from the associated layout
                    clearRationale = String.format(CLEAR_STORAGE_RATIONALE, RATIONALE_HINT);
                }
                break;
            case PermissionGroupsDangerous.PHONE:
                properActions = drawProperActionFromKeywords(keywordsFromLayout, PermissionGroupsDangerous.PHONE);
                if (properActions != null){
                    clearRationale = String.format(CLEAR_PHONE_RATIONALE, properActions);
                } else {
                    // use default message if we cannot draw out anything from the associated layout
                    clearRationale = String.format(CLEAR_PHONE_RATIONALE, RATIONALE_HINT);
                }
                break;
            case PermissionGroupsDangerous.CONTACTS:
                properActions = drawProperActionFromKeywords(keywordsFromLayout, PermissionGroupsDangerous.CONTACTS);
                if (properActions != null){
                    clearRationale = String.format(CLEAR_CONTACTS_RATIONALE, properActions);
                } else {
                    // use default message if we cannot draw out anything from the associated layout
                    clearRationale = String.format(CLEAR_CONTACTS_RATIONALE, RATIONALE_HINT);
                }
                break;
            case PermissionGroupsDangerous.CAMERA:
                properActions = drawProperActionFromKeywords(keywordsFromLayout, PermissionGroupsDangerous.CAMERA);
                if (properActions != null){
                    clearRationale = String.format(CLEAR_CAMERA_RATIONALE, properActions);
                } else {
                    // use default message if we cannot draw out anything from the associated layout
                    clearRationale = String.format(CLEAR_CAMERA_RATIONALE, RATIONALE_HINT);
                }
                break;
            case PermissionGroupsDangerous.MICROPHONE:
                properActions = drawProperActionFromKeywords(keywordsFromLayout, PermissionGroupsDangerous.MICROPHONE);
                if (properActions != null){
                    clearRationale = String.format(CLEAR_MIRCROPHONE_RATIONALE, properActions);
                } else {
                    // use default message if we cannot draw out anything from the associated layout
                    clearRationale = String.format(CLEAR_MIRCROPHONE_RATIONALE, RATIONALE_HINT);
                }
                break;
            case PermissionGroupsDangerous.SMS:
                properActions = drawProperActionFromKeywords(keywordsFromLayout, PermissionGroupsDangerous.SMS);
                if (properActions != null){
                    clearRationale = String.format(CLEAR_SMS_RATIONALE, properActions);
                } else {
                    // use default message if we cannot draw out anything from the associated layout
                    clearRationale = String.format(CLEAR_SMS_RATIONALE, RATIONALE_HINT);
                }
                break;
            case PermissionGroupsDangerous.CALENDAR:
                properActions = drawProperActionFromKeywords(keywordsFromLayout, PermissionGroupsDangerous.CALENDAR);
                if (properActions != null){
                    clearRationale = String.format(CLEAR_CALENDAR_RATIONALE, properActions);
                } else {
                    // use default message if we cannot draw out anything from the associated layout
                    clearRationale = String.format(CLEAR_CALENDAR_RATIONALE, RATIONALE_HINT);
                }
                break;
        }


        return clearRationale;
    }

    /**
     * @param expression as shouldShowRequestPermissionRationale() function
     * @return String containing one requested permission as "android.permission.READ_CONTACTS" to ask for showing rationale
     */
    public static PsiElement retrieveRequestedPermissions(PsiMethodCallExpression expression) {
        PsiElement rationalePermission = null;
        String signature = MethodHelper.getCallSignature(expression, true);
        if (signature.contains(Signatures.SHOULD_SHOW_PERMISSION_RATIONALE)) {
            PsiExpression[] argumentList = expression.getArgumentList().getExpressions();
            if (argumentList.length > 0) {
                // take the 2nd argument in shouldShowRequestPermissionRationale(), i.e. ActivityCompat.shouldShowRequestPermissionRationale(this,
                //                Manifest.permission.READ_CONTACTS)
                rationalePermission = argumentList[1];
            }
        }

        return rationalePermission;
    }

    /**
     * classify rationale message into one specific permission group via keyword matching
     * it is useful in case we have multiple permission requests at the same time
     *
     * @param rationaleContent
     * @return
     */
    public static String classifyRationale(String rationaleContent) {
        String type = null;
        rationaleContent = rationaleContent.toLowerCase();
        if (rationaleContent.contains("phone")) {
            type = PermissionGroupsDangerous.PHONE;
        } else if (rationaleContent.contains("camera")) {
            type = PermissionGroupsDangerous.CAMERA;
        } else if (rationaleContent.contains("calendar")) {
            type = PermissionGroupsDangerous.CALENDAR;
        } else if (rationaleContent.contains("microphone")) {
            type = PermissionGroupsDangerous.MICROPHONE;
        } else if (rationaleContent.contains("storage")) {
            type = PermissionGroupsDangerous.STORAGE;
        } else if (rationaleContent.contains("sms")) {
            type = PermissionGroupsDangerous.SMS;
        } else if (rationaleContent.contains("location")) {
            type = PermissionGroupsDangerous.LOCATION;
        } else if (rationaleContent.contains("contact")) {
            type = PermissionGroupsDangerous.CONTACTS;
        }
        return type;
    }

    /**
     *
     * @param keywordsFromLayout list of all keywords being retrieved from associated layout including text and image elements
     * @param permissionGroup
     * @return proper actions can be inferred from these keywords above to put into the suggested rationale message
     * TODO need to use ""string similarity" approach to infer proper actions rather than just using "contains()" check
     * TODO Also, properActions should be multiple possible actions, currently only one action
     */
    private static String drawProperActionFromKeywords(HashMap<XmlTag, ArrayList<String>> keywordsFromLayout, String permissionGroup) {
        String properAction;
        if (keywordsFromLayout == null){
            return null;
        }
        for (ArrayList<String> keywords : keywordsFromLayout.values()){
            for (String keyword : keywords) {
                for (String clearKeyword : permissionGroupsWithCorrespondingClearKeywords.get(permissionGroup)) {
                    if (clearKeyword.contains(keyword)) {
                        properAction = clearKeyword;
                        return properAction;
                    }
                }
            }
        }

        return null;
    }
}
