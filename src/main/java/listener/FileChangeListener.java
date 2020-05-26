package listener;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import handler.XMLHandler;
import helper.FileHelper;
import inspection.visitor.UIElementVisitor;
import object.UIElement;
import org.jetbrains.annotations.NotNull;
import service.Holder;

public class FileChangeListener implements VirtualFileListener {
    private Project project;

    public FileChangeListener(Project project) {
        this.project = project;
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        if (FileHelper.isKotlinOrJavaOrXmlFile(event)) {
            updateFileDependency(event.getFile());
        }
    }

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
        if (FileHelper.isKotlinOrJavaOrXmlFile(event)) {
            updateFileDependency(event.getFile());
        }
    }


    private void updateFileDependency(VirtualFile file) {
        PsiFile foundFile = PsiManager.getInstance(project).findFile(file);
        if (foundFile != null && foundFile.isValid() && FileHelper.isKotlinOrJavaOrXmlFile(foundFile)) {
            if (foundFile instanceof XmlFile){
                PsiFile javaOrKotlinFile = retrieveCorrespondingCodeFile((XmlFile) foundFile);
                if (javaOrKotlinFile != null){
                    foundFile = javaOrKotlinFile;
                }
            }
            DumbService dumpService = DumbService.getInstance(project);
            PsiFile finalFoundFile = foundFile;
            dumpService.smartInvokeLater(() -> {
               finalFoundFile.accept(new UIElementVisitor());
                XMLHandler.updateDataFromLayout(project);
            });

        }
    }

    /**
     *
     * @param xmlFile
     * @return java or kotlin need to be inspected correspondingly
     */
    private PsiFile retrieveCorrespondingCodeFile(XmlFile xmlFile) {
        PsiFile javaOrKotlinFile = null;
        for (UIElement uiElement : Holder.getUiElements()){
            if (uiElement.getXmlFile().equals(xmlFile)){
                javaOrKotlinFile = uiElement.getCodeFile();
                break;
            }
        }
        return javaOrKotlinFile;
    }
}
