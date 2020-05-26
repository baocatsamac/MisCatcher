package quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import helper.LogHelper;
import helper.MismatchUsageHelper;
import object.Method;
import object.UIElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ShowMismatchQuickFix extends GeneralQuickFix {
    private UIElement uiElement;
    private Method method;
    private boolean isLayoutDependencyOnly;
    private PsiFile layoutFile;
    private String requestedPermissionGroup;

    public ShowMismatchQuickFix(PsiFile layoutFile, String requestedPermissionGroup, UIElement uiElement, Method method, boolean isLayoutDependencyOnly) {
        this.uiElement = uiElement;
        this.method = method;
        this.layoutFile = layoutFile;
        this.requestedPermissionGroup = requestedPermissionGroup;
        this.isLayoutDependencyOnly = isLayoutDependencyOnly;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Show detailed mismatch";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        if (this.isLayoutDependencyOnly){
            return "Show me detailed mismatch in associated layout";
        } else {
            return "Show me permission usage mismatch";
        }
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        super.applyFix(project, problemDescriptor);
        MismatchUsageHelper.showDependencyWindow(project, this.layoutFile, this.requestedPermissionGroup, this.uiElement, this.method, isLayoutDependencyOnly);
    }

}
