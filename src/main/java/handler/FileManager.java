package handler;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import helper.MethodHelper;
import object.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileManager {

    public  static List<PsiFile> getFiles(Project project, LanguageFileType languageFileType) {
        PsiManager psiManager = PsiManager.getInstance(project);
        List<PsiFile> psiFiles = new ArrayList<>();
        GlobalSearchScope contentScope = ProjectScope.getContentScope(project);
        Collection<VirtualFile> virtualFilesWithoutLibs = FilenameIndex.getAllFilesByExt(project, languageFileType.getDefaultExtension(), contentScope);
        for (VirtualFile virtualFile : virtualFilesWithoutLibs) {
            if (languageFileType.equals(virtualFile.getFileType())) {
                PsiFile psiFile = psiManager.findFile(virtualFile);
                if (psiFile != null) {
                    psiFiles.add(psiFile);
                }
            }
        }
        return psiFiles;
    }

    public static PsiFile retrieveXMLFile(Project project, String xmlFileName) {
        List<PsiFile> files = FileManager.getFiles(project, StdFileTypes.XML);
        for (PsiFile file : files) {
            // inspect only layout files having Top-Level Interactive Function (i.e. setContentView or LayoutInflater.inflate())
            if( xmlFileName.equals(file.getName())){
                return file;
            }
        }
        return null;
    }

}
