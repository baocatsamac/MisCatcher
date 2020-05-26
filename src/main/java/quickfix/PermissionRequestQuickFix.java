package quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.DeleteHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import helper.LogHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class PermissionRequestQuickFix extends GeneralQuickFix {
    private final String NAME = "Remove redundant permission request";
    private PsiElement redundantRequest;

    public PermissionRequestQuickFix(PsiElement redundantRequest) {
        this.redundantRequest = redundantRequest;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Remove redundant permission request";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {

        return NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        super.applyFix(project, problemDescriptor);
        LogHelper.logInfo("QuickFix removing redundant permission request applied!!!");
        PsiElement[] elementsToDelete = {problemDescriptor.getPsiElement()};
        DeleteHandler.deletePsiElement(elementsToDelete, project, true);
    }

}
