package helper;

import java.util.ArrayList;

public class PrivacyPreservingHelper {
    private static final String[] READ_VIEW_RELATED_KEYWORDS = {"access", "load", "read", "view", "watch", "see", "scan", "spot", "explore", "look", "observe", "identify", "detect", "inspect", "glance", "note"};
    private static final String[] COARSE_LOCATION_RELATED_KEYWORDS = {"nearby", "weather", "forecast", "near area", "close area", "proximate", "neighbor"};
    private static final String[] FINE_LOCATION_RELATED_KEYWORDS = {"exact location", "correct location", "exact position", "correct position", "precise location", "precise position"};
    private static final String[] WRITE_EDIT_RELATED_KEYWORDS = {"write", "create", "commit", "edit", "change", "save", "store", "compose", "record", "generate", "construct", "produce", "recover", "alter", "adjust", "modify", "merge", "transform", "substitute", "replace", "convert", "publish"};

    public static boolean isReadingMode(ArrayList<String> textElements){
        boolean isReadingMode = true;
        for (String text : textElements){
            for (String keyword : WRITE_EDIT_RELATED_KEYWORDS){
                if (text != null && text.contains(keyword)){
                    isReadingMode = false;
                    break;
                }
            }
        }
        return isReadingMode;
    }

    public static boolean isCoarseLocationMode(ArrayList<String> textElements){
        boolean isCoarseLocationMode = false;
        for (String text : textElements){
            for (String keyword : COARSE_LOCATION_RELATED_KEYWORDS){
                if (text != null && text.contains(keyword)){
                    isCoarseLocationMode = true;
                    break;
                }
            }
        }
        return isCoarseLocationMode;
    }
}
