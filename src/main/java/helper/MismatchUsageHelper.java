package helper;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.ContentFactory;
import object.Method;
import object.UIElement;
import ui.PermissionDependenciesPanel;
import ui.PermissionDependenciesWindow;

import com.intellij.ui.content.Content;
import ui.telemetry.MissingPermissionsPanel;

import java.util.Set;

public class MismatchUsageHelper {

    private static final String WINDOW_PREFIX = "Permission usage mismatch";

    public static void showDependencyWindow(Project project, PsiFile layoutFile, String requestedPermissionGroup, UIElement uiElement, Method method, boolean isLayoutDependencyOnly) {
        PermissionDependenciesPanel libDepPanel = new PermissionDependenciesPanel(project, layoutFile, requestedPermissionGroup, uiElement, method, isLayoutDependencyOnly);
        String windowsName;
        if (method != null){
            windowsName = WINDOW_PREFIX + " between " + uiElement.getId() + " and " + method.getSignature();
        } else {
            windowsName = WINDOW_PREFIX + " between this permission request and " + layoutFile.getName();
        }

        Content content = ContentFactory.SERVICE.getInstance().createContent(libDepPanel, windowsName, false);

        content.setDisposer(libDepPanel);
        libDepPanel.setContent(content);

        PermissionDependenciesWindow libDepWindow = PermissionDependenciesWindow.getInstance(project);

        libDepWindow.addContent(content);
    }

    public static void showMissingPermissionWindow(Project project, Set<String> missingPermissions){
        MissingPermissionsPanel missingPerPanel = new MissingPermissionsPanel(missingPermissions);
        String windowsName = " Missing permissions in AndroidManifest.xml";
        Content content = ContentFactory.SERVICE.getInstance().createContent(missingPerPanel, windowsName, false);

        content.setDisposer(missingPerPanel);

        PermissionDependenciesWindow libDepWindow = PermissionDependenciesWindow.getInstance(project);

        libDepWindow.addContent(content);
    }

}
