package ui.usages;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlFile;
import com.intellij.usageView.UsageInfo;
import helper.PermissionDependenciesBuilder;

import java.util.*;

public class PermissionDependenciesUsagePanel extends UsagesPanel {
    private final PermissionDependenciesBuilder builder;

    public PermissionDependenciesUsagePanel(Project project, final PermissionDependenciesBuilder builder) {
        super(project);
        this.builder = builder;
        setToInitialPosition();
    }

    @Override
    public String getInitialPositionText() {
        return builder.getInitialUsagesPosition();
    }


    @Override
    public String getCodeUsagesString() {
        return builder.getRootNodeNameInUsageView();
    }

    public void showUsages(PsiJavaFile codeFile, XmlFile layoutFile, ArrayList<PsiElement> layoutPositions, HashMap<PsiElement, ArrayList<String>> mismatchCodeLines){
        Set<PsiFile> elementsToSearch = new HashSet<>();
        if (codeFile != null){
            elementsToSearch.add(codeFile);
        }
        if (layoutFile != null){
            elementsToSearch.add(layoutFile);
        }

        ArrayList<UsageInfo> usages = new ArrayList<>();
        for (PsiElement layoutPos : layoutPositions){
            if (layoutPos != null){
                usages.add(new UsageInfo(layoutPos));
            }

        }

        if (mismatchCodeLines != null){
            for (Map.Entry<PsiElement, ArrayList<String>> entry : mismatchCodeLines.entrySet()){
                usages.add(new UsageInfo(entry.getKey()));
            }
        }
        final UsageInfo[] finalUsages = usages.toArray(new UsageInfo[usages.size()]);
        final PsiElement[] _elementsToSearch = PsiUtilCore.toPsiElementArray(elementsToSearch);
        ApplicationManager.getApplication().invokeLater(() -> showUsages(_elementsToSearch, finalUsages), ModalityState.stateForComponent(
                this));
    }
}