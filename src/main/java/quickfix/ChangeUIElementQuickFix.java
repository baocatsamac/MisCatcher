package quickfix;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ChangeUIElementQuickFix extends GeneralQuickFix {

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Show dependencies";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Show me dependencies";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        super.applyFix(project, problemDescriptor);
    }
}