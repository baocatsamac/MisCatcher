package object;

import com.intellij.psi.PsiElement;
import helper.MethodHelper;
import helper.MismatchChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Listener Handler method like onClick(), onTouch(), etc.
 */
public class Method {

    private String signature;
    private PsiElement method;
    // mapping between each PsiMethodCallExpression and its corresponding permissions
    private HashMap<PsiElement, ArrayList<String>> codeLinePermissionMappings;
    private boolean isMismatchPermissionUI = false;
    public static final Method Dummy = new Method("", null, new HashMap<>());

    public Method(String signature, PsiElement method, HashMap<PsiElement, ArrayList<String>> codeLinePermissionMappings) {

        this.signature = signature;
        this.method = method;
        this.codeLinePermissionMappings = codeLinePermissionMappings;
    }

    public String getSignature() {
        return signature;
    }

    public HashMap<PsiElement, ArrayList<String>> getCodeLinePermissionMappings() {
        return codeLinePermissionMappings;
    }

    public PsiElement getMethod() {
        return method;
    }

    public boolean isMismatchPermissionUI() {
        return isMismatchPermissionUI;
    }

    public void setMismatchPermissionUI(boolean mistmatchPermissionUI) {
        isMismatchPermissionUI = mistmatchPermissionUI;
    }

    // remove non-mismatch code lines, only keep the mismatch cases
    public void removeNotMismatchCodeLines(ArrayList<String> advertisedPermissions){
        this.codeLinePermissionMappings = MismatchChecker.retrieveMismatchCodeLines(this,advertisedPermissions);
    }

    // get all the permissions being used within the listener handler
    public ArrayList<String> getActualPermissionGroups(){
        ArrayList<String> actualUsedPermissionGroups = new ArrayList<>();
        for (Map.Entry<PsiElement, ArrayList<String>> me : codeLinePermissionMappings.entrySet()) {
            actualUsedPermissionGroups.addAll(me.getValue());
        }
        return MethodHelper.removeDuplicates(actualUsedPermissionGroups);
    }
}
