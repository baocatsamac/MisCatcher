package handler;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import inspection.visitor.UIElementVisitor;

import java.util.List;

public class JavaHandler  {

    public static void inspectUIElementsWithinCode(Project project) {
        List<PsiFile> files = FileManager.getFiles(project, StdFileTypes.JAVA);
        for (PsiFile file : files) {
            file.accept(new UIElementVisitor());
        }
    }

}
