package ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.impl.ContentManagerWatcher;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;


public class PermissionDependenciesWindow {
    private final Project mProject;
    private ContentManager mContentManager;
    private Content currentContent;

    public static PermissionDependenciesWindow getInstance(Project project) {
        return ServiceManager.getService(project, PermissionDependenciesWindow.class);
    }

    public PermissionDependenciesWindow(final Project project) {
        mProject = project;

        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(mProject);
            if (toolWindowManager == null) return;
            ToolWindow toolWindow = toolWindowManager.registerToolWindow(Constants.DEPENDENCIES_WINDOW_ID, true,
                    ToolWindowAnchor.BOTTOM,
                    project);
            toolWindow.getComponent().putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true");
            mContentManager = toolWindow.getContentManager();

            toolWindow.setIcon(AllIcons.Toolwindows.ToolWindowInspection);
            new ContentManagerWatcher(toolWindow, mContentManager);
        });
    }

    public void addContent(final Content content) {
        final Runnable runnable = () -> {
            if(currentContent != null){
                mContentManager.removeContent(currentContent, true);
            }

            currentContent = content;
            mContentManager.addContent(content);
            mContentManager.setSelectedContent(content);
            ToolWindowManager.getInstance(mProject).getToolWindow(Constants.DEPENDENCIES_WINDOW_ID).activate(null);
        };
        StartupManager.getInstance(mProject).runWhenProjectIsInitialized(runnable);
    }
}
