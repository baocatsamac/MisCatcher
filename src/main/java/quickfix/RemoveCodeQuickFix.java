package quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.DeleteHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import helper.PsiElementHelper;
import object.Method;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import helper.LogHelper;
import org.nd4j.shade.guava.collect.Iterables;

import java.util.ArrayList;
import java.util.Arrays;

public class RemoveCodeQuickFix extends GeneralQuickFix {
    private final String NAME = "Remove permission mismatch code";
    private ArrayList<PsiElement> mismatchPermissionUICodeLines;

    public RemoveCodeQuickFix(ArrayList<PsiElement> mismatchPermissionUICodeLines) {
        this.mismatchPermissionUICodeLines = mismatchPermissionUICodeLines;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Remove permission mismatch code";
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
        LogHelper.logInfo("QuickFix applied!!!");
        PsiElement[] elementsToDelete = mismatchPermissionUICodeLines.toArray(new PsiElement[mismatchPermissionUICodeLines.size()]);
        DeleteHandler.deletePsiElement(elementsToDelete, project, true);
    }

}
