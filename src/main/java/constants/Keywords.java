package constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Keywords {

    private static String[] calendar_keywords = {"calendar", "schedule", "agenda", "appointment"};
    private static String[] contact_keywords = {"contact", "meeting", "association", "friends", "family", "address book", "share"};
    private static String[] camera_keywords = {"picture", "photo", "take picture", "camera", "capture", "scan"};
    private static String[] location_keywords = {"location", "area", "track", "gps", "find nearest",
            "locate", "near me", "direction", "locale", "neighborhood", "region", "position", "spot", "station", "venue", "whereabouts", "nearby", "search"};
    private static String[] microphone_keywords = {"microphone", "record", "audio", "voice", "mic", "mike"};
    private static String[] phone_keywords = {"call", "phone number", "outgoing call", "manage call", "phone state",
            "call log", "call's log", "log", "sip", "hangup", "missed call", "imei", "dialer"};
    private static String[] sms_keywords = {"sms", "mms", "message"};
    private static String[] storage_keywords = {"storage", "sd card", "file", "save", "gallery",
            "folder", "picture", "from library", "backup", "restore", "download", "upload", "import", "export", "explorer", "image", "media", "photo", "video", "album", "favorite", "screenshot", "story", "stories"};

    public static HashMap<String, Set<String>> permissionKeywordsMapping = new HashMap<String, Set<String>>() {{
        put("android.permission-group.CALENDAR",new HashSet<>(Arrays.asList(calendar_keywords)));
        put("android.permission-group.CONTACTS",new HashSet<>(Arrays.asList(contact_keywords)));
        put("android.permission-group.CAMERA",new HashSet<>(Arrays.asList(camera_keywords)));
        put("android.permission-group.LOCATION",new HashSet<>(Arrays.asList(location_keywords)));
        put("android.permission-group.MICROPHONE",new HashSet<>(Arrays.asList(microphone_keywords)));
        put("android.permission-group.PHONE_CALLS", new HashSet<>(Arrays.asList(phone_keywords)));
        put("android.permission-group.MESSAGES",new HashSet<>(Arrays.asList(sms_keywords)));
        put("android.permission-group.STORAGE",new HashSet<>(Arrays.asList(storage_keywords)));
    }};


}
