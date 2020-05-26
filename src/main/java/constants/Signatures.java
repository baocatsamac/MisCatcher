package constants;


import javaslang.collection.Map;

import java.util.Arrays;
import java.util.HashMap;

public class Signatures {




    // Listener Handler Signatures
    public static final String ON_CLICK = "onClick(android.view.View)";
    public static final String ON_TOUCH = "onTouch(android.view.View, android.view.MotionEvent)";
    public static final String ON_LONG_CLICK = "onLongClick(android.view.View)";
    public static final String ON_DRAG = "onDrag(android.view.View, android.view.DragEvent)";
    public static final String ON_FOCUS_CHANGE = "onFocusChange(android.view.View, boolean)";
    public static final String ON_KEY = "onKey(android.view.View, int, android.view.KeyEvent)";
    public static final String ON_CREATE_CONTEXT_MENU = "onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu$ContextMenuInfo)";
    public static final String ON_HOVER = "onHover(android.view.View, android.view.MotionEvent)";
    public static final String ON_APPLY_WINDOW_INSETS = "onApplyWindowInsets(android.view.View, android.view.WindowInsets)";
    public static final String ON_CAPTURE_POINTER = "onCapturedPointer(android.view.View, android.view.MotionEvent)";
    public static final String ON_CONTEXT_CLICK = "onContextClick(android.view.View)";
    public static final String ON_GENERIC_MOTION = "onGenericMotion(android.view.View, android.view.MotionEvent)";
    public static final String ON_SCROLL_CHANGE = "onScrollChange(android.view.View, int, int, int, int)";
    public static final String ON_SYSTEM_VISIBILITY_CHANGE = "onSystemUiVisibilityChange(int)";
    public static final String ON_EDITOR_ACTION = "onEditorAction(android.widget.TextView, int, android.view.KeyEvent)";








    // Callback Register Signatures
    public static final String SET_ON_CLICK_LISTENER = "android.view.View.setOnClickListener(android.view.View$OnClickListener)";
    public static final String SET_ON_TOUCH_LISTENER = "android.view.View.setOnTouchListener(android.view.View$OnTouchListener)";
    public static final String SET_ON_LONG_CLICK_LISTENER = "android.view.View.setOnLongClickListener(android.view.View$OnLongClickListener)";
    public static final String SET_ON_DRAG_LISTENER = "android.view.View.setOnDragListener(android.view.View$OnDragListener)";
    public static final String SET_ON_FOCUS_CHANGE_LISTENER = "android.view.View.setOnFocusChangeListener(android.view.View$OnFocusChangeListener)";
    public static final String SET_ON_KEY_LISTENER = "android.view.View.setOnKeyListener(android.view.View$OnKeyListener)";
    public static final String SET_ON_CREATE_CONTEXT_MENU_LISTENER = "android.view.View.setOnCreateContextMenuListener(android.view.View$OnCreateContextMenuListener)";
    public static final String SET_ON_HOVER_LISTENER = "android.view.View.setOnHoverListener(android.view.View$OnCreateContextMenuListener)";
    public static final String SET_ON_APPLY_WINDOW_INSETS_LISTENER = "android.view.View.setOnApplyWindowInsetsListener(android.view.View$OnApplyWindowInsetsListener)";
    public static final String SET_ON_CAPTURE_POINTER_LISTENER = "android.view.View.setOnCapturedPointerListener(android.view.View$OnCapturedPointerListener)";
    public static final String SET_ON_CONTEXT_CLICK_LISTENER = "android.view.View.setOnContextClickListener(android.view.View$OnContextClickListener)";
    public static final String SET_ON_EDITOR_ACTION_LISTENER = "android.widget.TextView.setOnEditorActionListener(android.widget.TextView$OnEditorActionListener)";
    public static final String SET_ON_GENERIC_MOTION_LISTENER = "android.view.View.setOnGenericMotionListener(android.view.View$OnGenericMotionListener)";
    public static final String SET_ON_SCROLL_CHANGE_LISTENER = "android.view.View.setOnScrollChangeListener(android.view.View$OnScrollChangeListener)";
    public static final String SET_ON_SYSTEM_UI_VISIBILITY_CHANGE_LISTENER = "android.view.View.setOnSystemUiVisibilityChangeListener(android.view.View$OnSystemUiVisibilityChangeListener)";


    public static final HashMap<String, String> listenerHandlers = new HashMap<String, String>(){
        {
            put(SET_ON_CLICK_LISTENER, ON_CLICK);
            put(SET_ON_TOUCH_LISTENER, ON_TOUCH);
            put(SET_ON_LONG_CLICK_LISTENER, ON_LONG_CLICK);
            put(SET_ON_DRAG_LISTENER, ON_DRAG);
            put(SET_ON_FOCUS_CHANGE_LISTENER, ON_FOCUS_CHANGE);
            put(SET_ON_KEY_LISTENER, ON_KEY);
            put(SET_ON_CREATE_CONTEXT_MENU_LISTENER, ON_CREATE_CONTEXT_MENU);
            put(SET_ON_HOVER_LISTENER, ON_HOVER);
            put(SET_ON_APPLY_WINDOW_INSETS_LISTENER, ON_APPLY_WINDOW_INSETS);
            put(SET_ON_CAPTURE_POINTER_LISTENER, ON_CAPTURE_POINTER);
            put(SET_ON_CONTEXT_CLICK_LISTENER, ON_CONTEXT_CLICK);
            put(SET_ON_EDITOR_ACTION_LISTENER, ON_EDITOR_ACTION);
            put(SET_ON_GENERIC_MOTION_LISTENER, ON_GENERIC_MOTION);
            put(SET_ON_SCROLL_CHANGE_LISTENER, ON_SCROLL_CHANGE);
            put(SET_ON_SYSTEM_UI_VISIBILITY_CHANGE_LISTENER, ON_SYSTEM_VISIBILITY_CHANGE);
        }

    };




    public static final String SET_CONTENT_VIEW = "setContentView(int)";
    public static final String INFLATE_LAYOUT_1 = "android.view.LayoutInflater.inflate(int, android.view.ViewGroup)";
    public static final String INFLATE_LAYOUT_2 = "android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean)";
    public static final String FIND_VIEW_BY_ID = "findViewById(int)";

    public static final String CONTENT_RESOLVER_QUERY = "android.content.ContentResolver.query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)";
    public static final String CONTENT_RESOLVER_UPDATE = "android.content.ContentResolver.query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)";
    public static final String CONTENT_RESOLVER_INSERT = "android.content.ContentResolver.insert(android.net.Uri, android.content.ContentValues)";
    public static final String CONTENT_RESOLVER_DELETE = "android.content.ContentResolver.query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)";

    // Permission Request Detection
    public static final String SHOULD_SHOW_PERMISSION_RATIONALE = "shouldShowRequestPermissionRationale(android.app.Activity, java.lang.String)";
    public static final String REQUEST_PERMISSION_FIRST_TYPE = "requestPermissions(android.app.Activity, java.lang.String[], int)";
    public static final String REQUEST_PERMISSION_SECOND_TYPE = "requestPermissions(java.lang.String[], int)";
    public static final String CHECK_SELF_PERMISSION = "checkSelfPermission(android.content.Context, java.lang.String)";
}
