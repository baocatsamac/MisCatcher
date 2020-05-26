package inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import constants.Messages;
import constants.PrivacyPreservingPermissions;
import constants.Signatures;
import constants.WarningType;
import helper.*;
import object.Method;
import object.UIElement;
import org.jetbrains.annotations.NotNull;
import quickfix.*;
import service.Holder;

import java.util.*;

public class ApiDependencyInspection extends PerMisJavaLocalInspection {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                String currentSignature = MethodHelper.getCallSignature(expression, true);
                PsiFile layoutFile = MethodHelper.retrieveAssociatedLayout(expression.getContainingFile().getName());
                Pair<UIElement, Method> existingData = checkExistingListenerCallExpression(expression);
                if (existingData != null) {
                    Method existingMethod = existingData.getSecond();
                    UIElement existingUIElement = existingData.getFirst();
                    // generate quick fixes
                    ArrayList<LocalQuickFix> quickFixes = new ArrayList<>();
                    // given the all the surrounding UI elements referring PHONE permission
                    ArrayList<String> advertisedPermissions = Holder.getLayoutAdvertisedPermissionsMapping().get(existingUIElement.getXmlFile().getName());
//                            advertisedPermissions.add("android.permission-group.AFFECTS_BATTERY");

                    ArrayList<PsiElement> mismatchPermissionUICodeLines= MismatchChecker.retrieveMismatchPermissionUICodeLines(existingUIElement, existingMethod, advertisedPermissions);
                    if (mismatchPermissionUICodeLines.size() != 0) {
//                        quickFixes.add(new SendFeedback(existingUIElement.getId()));
                        existingMethod.removeNotMismatchCodeLines(advertisedPermissions);
                        quickFixes.add(new ShowMismatchQuickFix(null, null, Holder.getUIElementById(existingUIElement.getId()), existingMethod, false));
                        quickFixes.add(new RemoveCodeQuickFix(mismatchPermissionUICodeLines));
                        //"There is one Permission-UI mismatch here!"
                        // Need to be updated
                        String message = Messages.getMessage(null, null, existingMethod.getActualPermissionGroups(), advertisedPermissions, WarningType.MISMATCH_HANDLER_UI);
                        int index = 0;
                        LocalQuickFix[] finalQuickFixes = new LocalQuickFix[quickFixes.size()];
                        for (LocalQuickFix quickFix : quickFixes) {
                            finalQuickFixes[index] = quickFix;
                            index++;
                        }
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if (MethodHelper.isLamdaExpressionIncluded(expression)) {
                                // try to highlight shortened version of expression if there is Lamda expression
                                holder.registerProblem(expression.getMethodExpression(), message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, finalQuickFixes);
                            } else {
                                holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, finalQuickFixes);
                            }
                        });
                    }
                }

                // check whether there is any not clear permission rationale
                PsiExpression[] argumentExpressions;
                PsiExpression psiExpression = expression.getMethodExpression().getQualifierExpression();
                if (psiExpression instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) psiExpression;
                    argumentExpressions = psiMethodCallExpression.getArgumentList().getExpressions();
                } else {
                    argumentExpressions = expression.getArgumentList().getExpressions();

                }
                HashMap<XmlTag, ArrayList<String>> keywordsFromLayout = new HashMap<>();
                if (layoutFile != null){
                    for (Map.Entry<XmlTag,ArrayList<String>> map : Holder.getTagKeywordsMapping().entrySet()){
                        if (map.getKey().getContainingFile().equals(layoutFile)){
                            keywordsFromLayout.put(map.getKey(), map.getValue());
                        }
                    }
                }
                for (PsiExpression argumentExp : argumentExpressions) {
                    if (Holder.getPermissionRationaleMapping().containsKey(argumentExp)) {
                        Pair<String, String> permissionRationale = Holder.getPermissionRationaleMapping().get(argumentExp);

                        String permissionGroup = permissionRationale.getFirst();
                        String rationaleContent = permissionRationale.getSecond();
                        String rationaleType = RationaleHelper.classifyRationale(rationaleContent);
                        String message = Messages.getMessage(null, null, null, null, WarningType.UNCLEAR_RATIONALE);
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if (!isExistingProblem(argumentExp, holder)) {
                                holder.registerProblem(argumentExp, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new UpdateRationaleQuickFix(permissionGroup, rationaleType, keywordsFromLayout));
                            }
                        });

                    }
                }

                // check all requested permissions
                if (currentSignature != null && (currentSignature.contains(Signatures.REQUEST_PERMISSION_FIRST_TYPE) || currentSignature.contains(Signatures.REQUEST_PERMISSION_SECOND_TYPE))) {
                    ArrayList<PsiElement> requestedPermissions = PermissionHelper.retrieveRequestedPermissions(expression);
                    for (PsiElement requestedPerm : requestedPermissions) {
                        String permissionGroup;
                        String permission = PermissionHelper.parsePermission(requestedPerm);
                        if (permission.contains("Manifest")) {
                            permission = permission.replace("Manifest", "android");
                        }
                        // request permission group (i.e. "android.permission-group.CONTACTS") instead of a particular permission (i.e. "android.permission.READ_CONTACTS")
                        if (Holder.getPermissionGroupToPermissionsMapping().containsKey(permission)) {
                            permissionGroup = permission;
                        } else {
                            permissionGroup = PermissionHelper.inferPermissionGroup(permission);
                        }

                        // check whether there is any overuse or mismatch between permissions being requested and advertised permissions in the corresponding layout
                        if (permissionGroup != null && layoutFile != null) {
                            ArrayList<LocalQuickFix> quickFixes = new ArrayList<>();
                            quickFixes.add(new ShowMismatchQuickFix(layoutFile, permissionGroup,null,null, true));
                            quickFixes.add(new PermissionRequestQuickFix(requestedPerm));

                            int index = 0;
                            LocalQuickFix[] finalQuickFixes = new LocalQuickFix[quickFixes.size()];
                            for (LocalQuickFix quickFix : quickFixes) {
                                finalQuickFixes[index] = quickFix;
                                index++;
                            }
                            // also check if the request rationale of that permission is clear or not
                            // if it is clear to explain its non-transparent request, no need to show warning
                            String message;
                            ArrayList<String> advertisedPermissions = Holder.getLayoutAdvertisedPermissionsMapping().get(layoutFile.getName());
                            if (advertisedPermissions.size() == 0) {
                                message = Messages.getMessage(permissionGroup, null, null, advertisedPermissions, WarningType.REDUNDANT_REQUEST);
                            } else {
                                message = Messages.getMessage(permissionGroup, null, null, advertisedPermissions, WarningType.MISMATCH_REQUEST);
                            }

                            if (!advertisedPermissions.contains(permissionGroup) && isUnclearPermissionRequest(permissionGroup)) {
                                ApplicationManager.getApplication().runReadAction(() -> {
                                        holder.registerProblem(requestedPerm, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, finalQuickFixes);
                                });
                            }
                        }

                        // check whether the requested permission is privacy-preserving or not, i.e. WRITE_CONTACTS or WRITE_EXTERNAL_STORAGE is not privacy-preserving
                        if (permission.equals(PrivacyPreservingPermissions.ACCESS_FINE_LOCATION)) {
                            boolean isCoarseLocationMode = PrivacyPreservingHelper.isCoarseLocationMode(XMLHelper.getAllTextElementsFromUI(expression.getProject(), layoutFile));
                            if (isCoarseLocationMode && PrivacyPreservingPermissions.getPrivacyPreservingPermissionMappings().containsKey(permission)) {
                                String suggestedPermission = PrivacyPreservingPermissions.getPrivacyPreservingPermissionMappings().get(permission);
                                String message = Messages.getMessage(permission, suggestedPermission, null, null, WarningType.NON_PRIVACY_PRESERVING_LOCATION);
                                ApplicationManager.getApplication().runReadAction(() -> {
                                        holder.registerProblem(requestedPerm, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new PrivacyPreservingQuickFix(suggestedPermission));
                                });
                            }
                        } else {
                            //  WRITE_CONTACTS or WRITE_EXTERNAL_STORAGE
                            boolean isReadingMode = PrivacyPreservingHelper.isReadingMode(XMLHelper.getAllTextElementsFromUI(expression.getProject(), layoutFile));
                            if (isReadingMode && PrivacyPreservingPermissions.getPrivacyPreservingPermissionMappings().containsKey(permission)) {
                                String suggestedPermission = PrivacyPreservingPermissions.getPrivacyPreservingPermissionMappings().get(permission);
                                String message = Messages.getMessage(permissionGroup, suggestedPermission, null, null, WarningType.NON_PRIVACY_PRESERVING);
                                ApplicationManager.getApplication().runReadAction(() -> {
                                        holder.registerProblem(requestedPerm, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new PrivacyPreservingQuickFix(suggestedPermission));
                                });
                            }

                        }
                    }
                }
                super.visitMethodCallExpression(expression);
            }
        };
    }


    /**
     * Check whether the argumentExp is being registered into problem descriptors or not
     *
     * @param argumentExp
     * @param holder
     * @return
     */
    private boolean isExistingProblem(PsiElement argumentExp, ProblemsHolder holder) {
        boolean isExistingProblem = false;
        for (ProblemDescriptor problem : holder.getResults()) {
            if (problem.getPsiElement().equals(argumentExp)) {
                isExistingProblem = true;
                break;
            }
        }
        return isExistingProblem;
    }

    /**
     * @param callExpression
     * @return pair of UI element and its corresponding method with callExpression which is inspected already
     */
    private Pair<UIElement, Method> checkExistingListenerCallExpression(PsiMethodCallExpression callExpression) {
        Pair<UIElement, Method> existingData = null;

        for (UIElement uiElement : Holder.getUiElements()) {
            if (uiElement.getCodeFile().equals(callExpression.getContainingFile())) {
                for (Method method : uiElement.getUiInteractiveMethods()) {
                    if (method.getMethod().equals(callExpression)) {
                        existingData = new Pair<>(uiElement, method);
                        break;
                    }
                }
            }
        }
        return existingData;
    }

    private Method retrieveExistingMethod(String uiElementId, String methodSignature) {
        for (UIElement uiElement : Holder.getUiElements()) {
            if (uiElement.getId().equals(uiElementId)) {
                for (Method method : uiElement.getUiInteractiveMethods()) {
                    if (method.getSignature().equals(methodSignature)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param usedPermissions list of permissions being used by a specific listener handler
     * @return list of used permissions but not being declared in AndroidManifest.xml file
     */
    private Set<String> retrieveMissingPermissions(HashMap<PsiElement, String> usedPermissions) {
        Set<String> missingPermissions = null;
        for (String usedPermission : usedPermissions.values()) {
            if (!Holder.getAndroidManifestPermissions().contains(usedPermission)) {
                if (missingPermissions == null) {
                    missingPermissions = new HashSet<>();
                }
                missingPermissions.add(usedPermission);
            }
        }

        return missingPermissions;

    }


    /**
     * @param permissionGroup
     * @return true if the permission request has unclear request rationale
     */
    private boolean isUnclearPermissionRequest(String permissionGroup) {
        boolean isUnclearPermissionRequest = false;
        for (Pair<String, String> map : Holder.getPermissionRationaleMapping().values()) {
            if (map.getFirst().equals(permissionGroup)) {
                isUnclearPermissionRequest = true;
                break;
            }
        }
        return isUnclearPermissionRequest;
    }
}
