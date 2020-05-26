package inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import helper.LogHelper;
import helper.MethodHelper;

public abstract class PerMisJavaLocalInspection extends LocalInspectionTool {

    protected String getClassName(PsiNewExpression newExpression) {
        String className = null;
        try {
            if (!newExpression.isValid()) {
                return null;
            }

            PsiJavaCodeReferenceElement reference = newExpression.getClassOrAnonymousClassReference();
            if (reference == null) {
                return null;
            }
            className = reference.getCanonicalText();
        } catch (Exception ex) {
            LogHelper.logInfo(ex.getMessage());
        }
        return className;
    }

    protected static String getMethodClassName(PsiMethodCallExpression methodCallExpression) {
        if (!methodCallExpression.isValid()) {
            return null;
        }

        return MethodHelper.getMethodType(methodCallExpression);
    }

    protected static String getMethodName(PsiMethodCallExpression methodCallExpression) {
        return MethodHelper.getMethodName(methodCallExpression);
    }

    protected String getClassName(PsiDeclarationStatement declarationStatement) {
        String className = null;
        try {
            if (!declarationStatement.isValid()) {
                return null;
            }

            PsiElement[] elements = declarationStatement.getDeclaredElements();
            if (elements.length != 0) {
                if (elements[0] instanceof PsiLocalVariable) {
                    className = ((PsiLocalVariable) elements[0]).getType().getCanonicalText();
                }
                //todo consider elements[0] instance of PsiClass
            }
        } catch (Exception ex) {
            LogHelper.logInfo(ex.getMessage());
        }
        return className;
    }

    protected String getClassName(PsiAssignmentExpression assignmentExpression) {
        try {
            if (!assignmentExpression.isValid()) {
                return null;
            }

            final PsiExpression lExpression = assignmentExpression.getLExpression();

            if (!(lExpression instanceof PsiReferenceExpression))
                return null;
            final PsiReferenceExpression reference = ((PsiReferenceExpression) lExpression);

            PsiType type = reference.getType();
            if (type == null)
                return null;

            return type.getCanonicalText();
        } catch (Exception ex) {
            LogHelper.logInfo(ex.getMessage());
        }
        return null;
    }
}
