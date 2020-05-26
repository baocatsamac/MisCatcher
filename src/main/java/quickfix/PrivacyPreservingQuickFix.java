package quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import service.Holder;

public class PrivacyPreservingQuickFix extends GeneralQuickFix {
    private final String NAME = "Change permission to %s";
    private String suggestedPermission;

    public PrivacyPreservingQuickFix(String suggestedPermission) {
        this.suggestedPermission = suggestedPermission.replace("android", "Manifest");
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Change permission";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return String.format(NAME, suggestedPermission.split("\\.")[2]);
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        super.applyFix(project, problemDescriptor);
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        PsiElement currentPermission = problemDescriptor.getPsiElement();
        String[] parts = currentPermission.getText().split("\\.");
        String permissionName = parts[parts.length - 1];
        PsiFile file = currentPermission.getContainingFile();
        Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document != null) {
            // change in requestPermissions() function
            TextRange range = currentPermission.getTextRange();
            if (range != null) {
                int startOffset = range.getStartOffset();
                int endOffset = range.getEndOffset();
                document.replaceString(startOffset, endOffset, suggestedPermission);
                // commit code change
                documentManager.commitAllDocuments();

            }
            if (Holder.getRequestedCheckedShouldShowPermissionMapping().containsKey(permissionName)){
                for (PsiElement permissionElement : Holder.getRequestedCheckedShouldShowPermissionMapping().get(permissionName)){
                    if (!permissionElement.equals(currentPermission)){
                        // change in requestPermissions() and shouldShowRequestRationale() function
                        range = permissionElement.getTextRange();
                        if (range != null) {
                            int startOffset = range.getStartOffset();
                            int endOffset = range.getEndOffset();
                            document.replaceString(startOffset, endOffset, suggestedPermission);
                            // commit code change
                            documentManager.commitAllDocuments();
                        }
                    }
                }
            }
        }
        // clear caches
        file.clearCaches();
    }
}