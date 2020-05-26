package inspection.visitor;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import constants.Signatures;
import handler.FileManager;
import helper.*;
import object.Method;
import object.UIElement;
import service.Holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UIElementVisitor extends JavaRecursiveElementVisitor {

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {

        // only continue inspecting if this Activity is associated with a layout
        String signature = MethodHelper.getCallSignature(expression, true);

        if (signature != null && MethodHelper.isSetLayoutCall(signature)) {
            // retrieve specific layout associated with this Activity
            String layoutName = MethodHelper.retrieveLayout(expression);

            if (layoutName != null) {
                // remove all the inspected UI elements of this Activity because they will be inspected again in this Visitor
                removeInspectedElements(expression.getContainingFile(), layoutName);

                // retrieve Permission Groups being deferred from all UI elements within the layout file
                PsiFile layoutFile = FileManager.retrieveXMLFile(expression.getProject(), layoutName);
                if (layoutFile != null) {
                    ArrayList<String> advertisedPermissions = MismatchChecker.inferPermissionFromUI(expression.getProject(), layoutFile);
                    Holder.getLayoutAdvertisedPermissionsMapping().put(layoutName, advertisedPermissions);

                    // retrieve associated keywords from text & images inside this layout file
                    HashMap<XmlTag, ArrayList<String>> keywordsFromLayout = MismatchChecker.retrieveKeywordsFromLayout(expression.getProject(), layoutFile);
                    Holder.getTagKeywordsMapping().putAll(keywordsFromLayout);

                    expression.getContainingFile().accept(new JavaRecursiveElementVisitor() {
                        @Override
                        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                            String currentSignature = MethodHelper.getCallSignature(expression, true);
                            // retrieve requested permissions if any,
                            // i.e. ActivityCompat.requestPermissions(this, new String[]{
                            // Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            ArrayList<PsiElement> requestedPermissions = null;
                            if (currentSignature != null && (currentSignature.contains(Signatures.REQUEST_PERMISSION_FIRST_TYPE) || currentSignature.contains(Signatures.REQUEST_PERMISSION_SECOND_TYPE))) {
                                requestedPermissions = PermissionHelper.retrieveRequestedPermissions(expression);
                            }
                            if (requestedPermissions != null) {
                                String currentFileName = expression.getContainingFile().getName();
                                if (Holder.getRequestedPermissionFileMapping().containsKey(currentFileName)) {
                                    Holder.getRequestedPermissionFileMapping().get(currentFileName).addAll(MethodHelper.removeDuplicates(requestedPermissions));
                                } else {
                                    Holder.getRequestedPermissionFileMapping().put(expression.getContainingFile().getName(), requestedPermissions);
                                }

                                // update permission requests psi element into Holder list to change along with shouldShowRationale and checkSelfPermission
                                for (PsiElement requestedPerm : requestedPermissions){
                                    updatePermissionRequest(requestedPerm);
                                }

                            }
                            // retrieve Permission Request - Rationale mappings,
                            // i.e. ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS)
                            if (currentSignature != null && currentSignature.contains(Signatures.SHOULD_SHOW_PERMISSION_RATIONALE)) {
                                retrievePermissionRationale(expression);
                            }

                            // retrieve Permission Request - Check Self Permission mappings,
                            // i.e. ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)
                            if (currentSignature != null && currentSignature.contains(Signatures.CHECK_SELF_PERMISSION)) {
                                retrievePermissionRequestCheck(expression);

                            }
                            checkListenerHandler(expression, layoutFile);
                            super.visitMethodCallExpression(expression);
                        }

                    });
                }
            }
        }
        super.visitMethodCallExpression(expression);
    }

    private void checkListenerHandler(PsiMethodCallExpression expression, PsiFile layoutFile) {
        String callSignature = MethodHelper.getCallSignature(expression, true);
        if (callSignature == null) {
            return;
        }
        if (!MethodHelper.isListenerCall(callSignature)) {
            return;
        }

        String xmlId = MethodHelper.getXMLIdInCode(expression);
        if (null == xmlId) {
            return;
        }

        PsiElement psiMethod = MethodHelper.getViewHandlerMethod(expression);
        if (null == psiMethod) {
            return;
        }
        HashMap<PsiElement, ArrayList<String>> permissions = null;
        Method uiInteractiveMethod;
        if (psiMethod instanceof PsiMethod) {
            // without using LAMDA expression
//            psiMethod = (PsiMethod) psiMethod.getOriginalElement();
            // check whether the method call expression is like btnSignin.setOnClickListener(THIS) to define how to retrieve permissions properly
            if (MethodHelper.isMethodCallWithThisArgument(expression)) {
                PsiElement codeBlock = PermissionHelper.retrieveAssociatedIdCodeBlock((PsiMethod) psiMethod, xmlId);
                if (codeBlock instanceof PsiStatement) {
                    permissions = PermissionHelper.retrievePermissionsRecursively((PsiStatement) codeBlock);
                } else {
                    permissions = PermissionHelper.retrievePermissionsRecursively((PsiMethod) psiMethod);
                }


            } else {
                permissions = PermissionHelper.retrievePermissionsRecursively((PsiMethod) psiMethod);

            }
        } else if (psiMethod instanceof PsiCodeBlock) {
            // with using LAMDA expression
            permissions = PermissionHelper.retrievePermissionsRecursively((PsiCodeBlock) psiMethod);
        }
        uiInteractiveMethod = new Method(callSignature, expression, permissions);


        // check whether there is any small layout or view being inflated before
        String inflatedChildViewName = MethodHelper.retrieveInflatedChildView(expression);
        if (!inflatedChildViewName.equals(layoutFile.getName())) {
            layoutFile = FileManager.retrieveXMLFile(expression.getProject(), inflatedChildViewName);
        }
        // retrieve layout file from file name
        PsiElement xmlLayoutDeclaration = MethodHelper.getXMLInLayout(xmlId, layoutFile);

        if (!isUIElementExisting(xmlId, layoutFile.getName(), uiInteractiveMethod)) {
            ArrayList<Method> uiMethods = new ArrayList<>();
            uiMethods.add(uiInteractiveMethod);
            UIElement button = new UIElement(xmlId, null, xmlLayoutDeclaration, null, uiMethods, null, layoutFile, expression.getContainingFile());
            Holder.register(button);
        }
    }


    /**
     * Check to see whether a specific UI Element is existing in our Holder or not, if not update with a new UI method then.
     *
     * @param id
     * @param layoutName
     * @param newUIInteractiveMethod
     * @return
     */
    private boolean isUIElementExisting(String id, String layoutName, Method newUIInteractiveMethod) {
        boolean isUIElementExisting = false;
        for (UIElement uiElement : Holder.getUiElements()) {
            // check for the same layout and the same element ID --> existing UI element
            if (uiElement.getId().equals(id) && uiElement.getXmlFile().getName().equals(layoutName)) {
                // add one more method if it is not existing in the method list belonging to that UI element
                if (!isExistingMethod(uiElement, newUIInteractiveMethod.getSignature())) {
                    uiElement.getUiInteractiveMethods().add(newUIInteractiveMethod);
                }
                isUIElementExisting = true;
                break;
            }
        }
        return isUIElementExisting;
    }

    private boolean isExistingMethod(UIElement uiElement, String methodSignature) {
        boolean isExistingMethod = false;
        for (Method method : uiElement.getUiInteractiveMethods()) {
            if (method.getSignature().equals(methodSignature)) {
                isExistingMethod = true;
                break;
            }
        }
        return isExistingMethod;
    }

    /**
     * remove all the already-inspected UI elements, requested permissions and permission request rationales belonging to this Activity because they will be inspected again in this Visitor
     * when there is a file change listener event
     *
     * @param currentCodeFile current Activity being inspected
     */
    private void removeInspectedElements(PsiFile currentCodeFile, String layoutName) {
        // remove all inspected ui elements
        Iterator itr = Holder.getUiElements().iterator();
        while (itr.hasNext()) {
            UIElement uiElement = (UIElement) itr.next();
            if (uiElement.getCodeFile().equals(currentCodeFile)) {
                itr.remove();
            }
        }

        // remove all inspected permissions inferred from this layout
        Holder.getLayoutAdvertisedPermissionsMapping().remove(layoutName);

        // remove all inspected permission request rationales
        Iterator<PsiElement> itr1 = Holder.getPermissionRationaleMapping().keySet().iterator();
        while (itr1.hasNext()) {
            PsiElement psiElement = itr1.next();
            if (!psiElement.isValid()) {
                itr1.remove();
            } else if (psiElement.getContainingFile().equals(currentCodeFile)) {
                itr1.remove();
            }

        }

        // remove all inspected requested permissions from this Activity
        Iterator<String> itr2 = Holder.getRequestedPermissionFileMapping().keySet().iterator();
        while (itr2.hasNext()) {
            String codeFileName = itr2.next();
            if (codeFileName.equals(currentCodeFile.getName())) {
                itr2.remove();
            }
        }

        // remove all inspected checked and shoudShowRationale permissions from this Activity
        Holder.getRequestedCheckedShouldShowPermissionMapping().clear();
    }

    /**
     * @param expression shouldShowRequestPermissionRationale expression, i.e. ActivityCompat.shouldShowRequestPermissionRationale(this,
     *                   Manifest.permission.READ_CONTACTS)
     */
    private void retrievePermissionRationale(PsiMethodCallExpression expression) {
        // the returned list of permissions contains only one permission to check shouldShowRequestPermissionRationale
        PsiElement rationalePermissionExp = RationaleHelper.retrieveRequestedPermissions(expression);
        if (rationalePermissionExp == null){
            return;
        }
        String rationalePermission = rationalePermissionExp.getText();
        if(rationalePermission.contains("Manifest")){
            rationalePermission = rationalePermission.replace("Manifest", "android");
        }
        PsiIfStatement parentExpression = PsiTreeUtil.getParentOfType(expression, PsiIfStatement.class);
        if (parentExpression != null) {
            PsiBlockStatement body = (PsiBlockStatement) ((PsiIfStatement) parentExpression).getThenBranch();
            if (body != null) {
                PsiCodeBlock codeBlock = body.getCodeBlock();
                String finalRationalePermission = rationalePermission;
                codeBlock.acceptChildren(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitMethodCallExpression(PsiMethodCallExpression childExpression) {
                        PsiExpression[] argumentExpressions;
                        PsiExpression psiExpression = childExpression.getMethodExpression().getQualifierExpression();
                        if (psiExpression instanceof PsiMethodCallExpression) {
                            PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) psiExpression;
                            argumentExpressions = psiMethodCallExpression.getArgumentList().getExpressions();
                        } else {
                            argumentExpressions = childExpression.getArgumentList().getExpressions();

                        }
                        for (PsiExpression rationaleExp : argumentExpressions) {
                            String rationaleMessage = null;
                            if (rationaleExp != null && rationaleExp.getType().equals(PsiType.INT) && rationaleExp.getText().contains("R.string.")) {
                                // in case R.string.permission_rationale being declared in strings.xml file
                                String stringLabel = rationaleExp.getText().split("\\.")[2];
                                // look up string content in strings.xml file
                                rationaleMessage = XMLHelper.retrieveStringContentFromResource(stringLabel);
                            } else if (rationaleExp instanceof PsiLiteralExpression) {
                                // in case hard-coded string for rationale
                                rationaleMessage = rationaleExp.getText();
                            }
                            if (rationaleMessage != null) {
                                // only take the string content considered actual permission rationale and has clear intention
                                String rationalePermissionGroup = PermissionHelper.inferPermissionGroup(finalRationalePermission);
                                if (RationaleHelper.isActualPermissionRationale(rationaleMessage) && !RationaleHelper.isClearRationale(rationaleMessage, rationalePermissionGroup)) {
                                    Pair<String, String> perRationale = new Pair<>(rationalePermissionGroup, rationaleMessage);
                                    // save this mapping into Holder variable for later use
                                    Holder.getPermissionRationaleMapping().put(rationaleExp, perRationale);
                                }
                                // add shouldShowRequestPermissionRationale psi element into permission-related list for concurrent change when applying quickfix
                                String[] permissionShouldShowParts = finalRationalePermission.split("\\."); // rationalePermission such as Manifest.android.permission.ACCESS_FINE_LOCATION
                                String permissionName = permissionShouldShowParts[permissionShouldShowParts.length - 1]; // i.e. ACCESS_FINE_LOCATION
                                if (Holder.getRequestedCheckedShouldShowPermissionMapping().containsKey(permissionName)){
                                    Holder.getRequestedCheckedShouldShowPermissionMapping().get(permissionName).add(rationalePermissionExp);
                                } else {
                                    ArrayList<PsiElement> shouldShowPermList = new ArrayList<>();
                                    shouldShowPermList.add(rationalePermissionExp);
                                    Holder.getRequestedCheckedShouldShowPermissionMapping().put(permissionName, shouldShowPermList);
                                }
                            }
                        }
                    }
                });
            }
        }
    }


    /**
     *
     * @param expression checkSelfPermission expression, i.e. ActivityCompat.checkSelfPermission(this,
     *      *                   Manifest.permission.READ_CONTACTS)
     */
    private void retrievePermissionRequestCheck(PsiMethodCallExpression expression) {
        // the returned list of permissions contains only one permission to check shouldShowRequestPermissionRationale
        PsiElement checkedPermissionPsi = null;
        PsiExpression[] argumentList = expression.getArgumentList().getExpressions();
        if (argumentList.length > 1){
            // take the 2nd argument in ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)
            checkedPermissionPsi = argumentList[1];
        }
        if (checkedPermissionPsi == null) {
            return;
        }

        String checkedPermission = argumentList[1].getText();
        if(checkedPermission.contains("Manifest")){
            checkedPermission = checkedPermission.replace("Manifest", "android");
        }
        // add checkSelfPermission psi element into permission-related list for concurrent change when applying quickfix
        String[] permissionToCheckParts = checkedPermission.split("\\."); // rationalePermission such as Manifest.android.permission.ACCESS_FINE_LOCATION
        String permissionName = permissionToCheckParts[permissionToCheckParts.length - 1]; // i.e. ACCESS_FINE_LOCATION
        if (Holder.getRequestedCheckedShouldShowPermissionMapping().containsKey(permissionName)){
            Holder.getRequestedCheckedShouldShowPermissionMapping().get(permissionName).add(checkedPermissionPsi);
        } else {
            ArrayList<PsiElement> checkedPermList = new ArrayList<>();
            checkedPermList.add(checkedPermissionPsi);
            Holder.getRequestedCheckedShouldShowPermissionMapping().put(permissionName, checkedPermList);
        }
    }

    /**
     *
     * @param permissionRequestExp requestPermissions expression, i.e. ActivityCompat.requestPermissions(this,
     *      *      *                   Manifest.permission.READ_CONTACTS)
     */
    private void updatePermissionRequest(PsiElement permissionRequestExp) {
        if (permissionRequestExp == null) {
            return;
        }

        String requestedPermission = permissionRequestExp.getText();
        if(requestedPermission.contains("Manifest")){
            requestedPermission = requestedPermission.replace("Manifest", "android");
        }
        // add requestPermissions psi element into permission-related list for concurrent change when applying quickfix
        String[] permissionToRequestParts = requestedPermission.split("\\."); // requestPermissions such as Manifest.android.permission.ACCESS_FINE_LOCATION
        String permissionName = permissionToRequestParts[permissionToRequestParts.length - 1]; // i.e. ACCESS_FINE_LOCATION
        if (Holder.getRequestedCheckedShouldShowPermissionMapping().containsKey(permissionName)){
            Holder.getRequestedCheckedShouldShowPermissionMapping().get(permissionName).add(permissionRequestExp);
        } else {
            ArrayList<PsiElement> requestedPermList = new ArrayList<>();
            requestedPermList.add(permissionRequestExp);
            Holder.getRequestedCheckedShouldShowPermissionMapping().put(permissionName, requestedPermList);
        }
    }
}