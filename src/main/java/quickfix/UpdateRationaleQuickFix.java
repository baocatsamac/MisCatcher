package quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import helper.RationaleHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import service.Holder;

import java.util.ArrayList;
import java.util.HashMap;

public class UpdateRationaleQuickFix extends GeneralQuickFix {
    private final String NAME = "Update permission rationale content for clear intention";
    private String suggestedRationale;

    public UpdateRationaleQuickFix(String permissionGroup, String rationaleType, HashMap<XmlTag, ArrayList<String>> keywordsFromLayout) {
        this.suggestedRationale = RationaleHelper.getClearRationale(permissionGroup, rationaleType, keywordsFromLayout);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Update permission rationale";
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
        PsiElement currentRationale = problemDescriptor.getPsiElement();

        if (currentRationale instanceof PsiReferenceExpression && currentRationale.getText().contains("R.string.")) {
            // replace rationale content in strings.xml file
            PsiFile stringsXMLFile = Holder.getStringsXMLFile();
            Document document = FileDocumentManager.getInstance().getDocument(stringsXMLFile.getVirtualFile());
            if (document != null) {
                // in case R.string.permission_rationale being declared in strings.xml file
                String stringLabel = currentRationale.getText().split("\\.")[2];
                // look up string content in strings.xml file
                XmlTag rationaleStringTag = retrieveStringTagFromResource(stringLabel);
                XmlTagValue rationaleStringTagValue = rationaleStringTag.getValue();
                TextRange range = rationaleStringTagValue.getTextRange();
                int startOffset = range.getStartOffset();
                int endOffset = range.getEndOffset();
                document.replaceString(startOffset, endOffset, suggestedRationale);
            }

        } else if (currentRationale instanceof PsiLiteralExpression) {
            // replace rationale content right inside Java or Kotlin code
            PsiFile file = currentRationale.getContainingFile();
            Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
            if (document != null) {
                TextRange range = currentRationale.getTextRange();
                if (range != null) {
                    int startOffset = range.getStartOffset();
                    int endOffset = range.getEndOffset();
                    document.replaceString(startOffset + 1, endOffset - 1, suggestedRationale);

                }
            }
        }
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        documentManager.commitAllDocuments();
        // clear caches
        currentRationale.getContainingFile().clearCaches();
    }

    public static XmlTag retrieveStringTagFromResource(String stringLabel) {
        PsiFile stringsXMLFile = Holder.getStringsXMLFile();
        final XmlTag[] content = {null};
        if (stringsXMLFile != null) {
            stringsXMLFile.accept(new XmlRecursiveElementVisitor() {

                @Override
                public void visitXmlTag(XmlTag tag) {
                    super.visitXmlTag(tag);
                    XmlAttribute nameAttribute = tag.getAttribute("name");
                    if (nameAttribute != null && nameAttribute.getValue().equals(stringLabel)) {
                        content[0] = tag;
                    }
                }
            });
        }

        return content[0];
    }
}