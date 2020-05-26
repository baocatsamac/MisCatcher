package helper;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.packageDependencies.DependenciesBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.GlobalSearchScopesCore;
import object.Method;
import object.UIElement;

import java.util.*;

//TODO combine this one with LibraryUsageHelper.java
public class PermissionDependenciesBuilder extends DependenciesBuilder {
    private HashMap<String, ArrayList<PsiElement>> libDependencies = null;


    private HashMap<String, Set<PsiFile>> usedLibFiles = null;
    private final Project mproject;
    private UIElement uiElement;
    private Method method;
    private boolean isLayoutDependencyOnly;
    private static HashMap<Project, PermissionDependenciesBuilder> instances = new HashMap<>();

    public static PermissionDependenciesBuilder getInstance(Project project) {
        if (instances.containsKey(project)) {
            return instances.get(project);
        } else {
            PermissionDependenciesBuilder instance = new PermissionDependenciesBuilder(project);
            instances.put(project, instance);
            return instance;
        }
    }

    public PermissionDependenciesBuilder(Project project) {
        super(project, new AnalysisScope(GlobalSearchScopesCore.projectProductionScope(project), project));
        this.mproject = project;
    }

    public void setDependenciesBuilderAttributes(UIElement uiElement, Method method, boolean isLayoutDependencyOnly){
        this.uiElement = uiElement;
        this.method = method;
        this.isLayoutDependencyOnly = isLayoutDependencyOnly;
    }


    @Override
    public String getRootNodeNameInUsageView() {
        if (this.isLayoutDependencyOnly){
            return String.format("Detailed permission usage inferred from its associated UIs");
        } else {
            return String.format("Detailed permission usage mismatch between selected listener handler and its associated UIs");
        }

    }

    @Override
    public String getInitialUsagesPosition() {
        if (this.isLayoutDependencyOnly){
            return String.format("Select a scope to view the associated layout details");
        } else {
            return String.format("Select a scope to view the mismatch details");
        }

    }

    @Override
    public boolean isBackward() {
        return false;
    }   


    @Override
    public void analyze() {
        final PsiManager psiManager = PsiManager.getInstance(getProject());
        psiManager.startBatchFilesProcessingMode();
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(getProject()).getFileIndex();
        try {
            getScope().accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitFile(final PsiFile file) {

                }
            });
        } catch (Exception ex) {
        } finally {
            psiManager.finishBatchFilesProcessingMode();
        }
    }

}
