package quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.DeleteHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import helper.LogHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class MovePermissionRequestQuickFix extends GeneralQuickFix {
    private final String NAME = "Move this permission request %s to %s for right contextual usage";
    private PsiElement redundantRequest;
    private PsiFile sourceCodeFile;
    private PsiFile destinationCodeFile;

    public MovePermissionRequestQuickFix(PsiElement redundantRequest, PsiFile sourceCodeFile, PsiFile destinationCodeFile) {
        this.redundantRequest = redundantRequest;
        this.sourceCodeFile = sourceCodeFile;
        this.destinationCodeFile = destinationCodeFile;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Move permission request";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {

        return String.format(NAME,redundantRequest.getText(), destinationCodeFile.getName());
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        super.applyFix(project, problemDescriptor);
        LogHelper.logInfo("QuickFix moving redundant permission request applied!!!");
        PsiElement[] elementsToDelete = {problemDescriptor.getPsiElement()};
        DeleteHandler.deletePsiElement(elementsToDelete, project, false);
        // clear caches
        problemDescriptor.getPsiElement().getContainingFile().clearCaches();
    }

}
