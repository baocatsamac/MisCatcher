package helper;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiNewExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import object.UIElement;
import org.jetbrains.annotations.NotNull;
import constants.Settings;
import constants.Signatures;
import service.Holder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MethodHelper {

    private static Logger LOGGER = Logger.getInstance(MethodHelper.class);

    private static String[] listenerHandlerSignatures = {Signatures.ON_CLICK, Signatures.ON_TOUCH, Signatures.ON_LONG_CLICK, Signatures.ON_DRAG, Signatures.ON_FOCUS_CHANGE, Signatures.ON_KEY, Signatures.ON_CREATE_CONTEXT_MENU, Signatures.ON_HOVER, Signatures.ON_APPLY_WINDOW_INSETS, Signatures.ON_CAPTURE_POINTER, Signatures.ON_CONTEXT_CLICK, Signatures.ON_GENERIC_MOTION, Signatures.ON_SCROLL_CHANGE, Signatures.ON_SYSTEM_VISIBILITY_CHANGE, Signatures.ON_EDITOR_ACTION};

    private static String[] setLayoutSignatures = {Signatures.INFLATE_LAYOUT_1, Signatures.INFLATE_LAYOUT_2};

    private static String[] listenerSignatures = {Signatures.SET_ON_CLICK_LISTENER, Signatures.SET_ON_TOUCH_LISTENER, Signatures.SET_ON_LONG_CLICK_LISTENER, Signatures.SET_ON_DRAG_LISTENER, Signatures.SET_ON_FOCUS_CHANGE_LISTENER, Signatures.SET_ON_KEY_LISTENER, Signatures.SET_ON_CREATE_CONTEXT_MENU_LISTENER, Signatures.SET_ON_HOVER_LISTENER, Signatures.SET_ON_APPLY_WINDOW_INSETS_LISTENER, Signatures.SET_ON_CAPTURE_POINTER_LISTENER, Signatures.SET_ON_CONTEXT_CLICK_LISTENER, Signatures.SET_ON_EDITOR_ACTION_LISTENER, Signatures.SET_ON_GENERIC_MOTION_LISTENER, Signatures.SET_ON_SCROLL_CHANGE_LISTENER, Signatures.SET_ON_SYSTEM_UI_VISIBILITY_CHANGE_LISTENER};

    public static String getMethodType(PsiMethodCallExpression methodCallExpression) {
        try {
            PsiMethod resolvedMethod = methodCallExpression.resolveMethod();
            if (resolvedMethod != null) {
                PsiClass containingClass = resolvedMethod.getContainingClass();
                if (containingClass != null) {
                    return containingClass.getQualifiedName();
                }
            }
        } catch (Exception ex) {
            LogHelper.logInfo(ex.getMessage());
        }
        return null;
    }

    public static PsiElement getCaller(PsiMethodCallExpression methodCallExpression) {
        try {
            PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
            PsiExpression psiExpression = methodExpression.getQualifierExpression();
            if (psiExpression instanceof PsiMethodCallExpression) {
                return getCaller((PsiMethodCallExpression) psiExpression);
            }

            if (psiExpression instanceof PsiReferenceExpression) {
                PsiReferenceExpression referenceExpression = (PsiReferenceExpression) psiExpression;
                return referenceExpression.resolve();
            }
        } catch (Exception ex) {
            LogHelper.logInfo(ex.getMessage());
        }
        return null;
    }

    public static String getMethodName(PsiMethodCallExpression methodCallExpression) {
        try {
            PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();
            if (methodExpression == null) {
                return null;
            }
            String methodName = methodExpression.getReferenceName();

            return methodName;
        } catch (Exception ex) {
            LogHelper.logInfo(ex.getMessage());
        }
        return null;
    }

    public static String getCallSignature(PsiMethodCallExpression methodCall, boolean includePackage) {
        PsiMethod psiMethod = methodCall.resolveMethod();
        if (psiMethod != null){
            return getMethodSignature(psiMethod, includePackage);
        } else {
            return null;
        }

    }

    public static String getMethodSignature(PsiMethod method, boolean includePackage) {
        if (includePackage) {
            return computeSignatureWithPackage(method);
        } else {
            return computeSignatureWithoutPackage(method);
        }
    }

    private static String computeSignatureWithoutPackage(PsiMethod method) {
        try {
            StringBuilder signature = new StringBuilder();
            signature.append(method.getName());
            signature.append('(');
            processParameters(method, signature);
            signature.append(')');
            return signature.toString();
        } catch (NullPointerException e) {
            LOGGER.error(Settings.TAG + "No method provided\n\n--------------------\n\n" + e.getMessage());
            e.printStackTrace();
            return "ERROR: Could not determine Signature";
        }
    }

    private static void processParameters(PsiMethod method, StringBuilder signature) {
        PsiParameterList parameterList = method.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();

        if (parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                PsiParameter parameter = parameters[i];
                String parameterTypeString = "";
                PsiType parameterType = parameter.getType();
                if (parameterType instanceof PsiClassType) {
                    PsiClass parameterClassType = ((PsiClassType) parameterType).resolve();
                    if (parameterClassType != null) {
                        parameterTypeString = getTypeString(parameterClassType);
                    }
                } else {
                    parameterTypeString = parameterType.getCanonicalText();
                }
                if (i != parameters.length - 1) {
                    signature.append(parameterTypeString).append(", ");
                } else {
                    signature.append(parameterTypeString);
                }
            }
        }
    }

    private static String computeSignatureWithPackage(PsiMethod method) {
        StringBuilder methodSignature = new StringBuilder();
        PsiClass containingClass = method.getContainingClass();
        String className = "";
        if (null != containingClass) {
            className = getTypeString(containingClass);
        }

        methodSignature.append(className);
        methodSignature.append(".");
        methodSignature.append(method.getName());
        methodSignature.append("(");

        processParameters(method, methodSignature);
        methodSignature.append(")");
        return methodSignature.toString();
    }

    private static String getTypeString(PsiClass psiClass) {
        PsiFile containingFile = psiClass.getContainingFile();
        if (!(containingFile instanceof PsiClassOwner)) {
            return null;
        }
        PsiClassOwner classOwner = (PsiClassOwner) containingFile;
        String packageName = classOwner.getPackageName();
        String qualifiedName = psiClass.getQualifiedName();

        String className = "";
        if (packageName.length() > 0) {
            className = packageName + ".";
            if (qualifiedName != null) {
                className += qualifiedName.substring(packageName.length() + 1).replace('.', '$');
            }
        } else {
            if (qualifiedName != null) {
                className = qualifiedName.replace('.', '$');
            }
        }

        return className;
    }

    public static boolean isListenerHandler(String signature) {
        boolean isListenerHandler = false;
        Set<String> handlerSignatures = new HashSet<>(Arrays.asList(listenerHandlerSignatures));
        if (handlerSignatures.contains(signature)) {
            isListenerHandler = true;
        }
        return isListenerHandler;
    }

    public static boolean isSetLayoutCall(String signature) {
        boolean isSetLayoutCall = false;
        Set<String> layoutSignatures = new HashSet<>(Arrays.asList(setLayoutSignatures));
        // for the case of setLayout from LayoutInflater, we need to include the package into the signature to differentiate from other inflater like MenuInflater
        // for the case of setContentView, we do not need to include package to compare
        if (layoutSignatures.contains(signature) || signature.contains(Signatures.SET_CONTENT_VIEW)) {
            isSetLayoutCall = true;
        }
        return isSetLayoutCall;
    }

    public static boolean isListenerCall(String callSignature) {
        boolean isListenerCall = false;
        Set<String> signatures = new HashSet<>(Arrays.asList(listenerSignatures));
        if (signatures.contains(callSignature)) {
            isListenerCall = true;
        }
        return isListenerCall;
    }

    /**
     * @param expression i.e. setContentView(R.layout.activity_main.xml)
     * @return layout name (i.e. activity_main)
     * NOTE: need to consider the case of using LayoutInflater???
     */
    public static String retrieveLayout(PsiMethodCallExpression expression) {
        String layoutName = null;
        PsiExpressionList arguments = expression.getArgumentList();
        if (arguments != null && arguments.getExpressions().length > 0){
            PsiElement layout = arguments.getExpressions()[0].getLastChild();
            layoutName = layout.getText() + ".xml";
        }
        return layoutName;
    }

    /**
     *
     * @param expression i.e. Button btnSignIn = findViewById(R.id.btn_signIn) declaration in code
     * @return XML ID element being declared in code btn_SignIn
     */
    public static String getXMLIdInCode(@NotNull PsiMethodCallExpression expression) {
        final String[] xmlId = {null};

        PsiExpression psiExpression = expression.getMethodExpression().getQualifierExpression();
        if (psiExpression instanceof PsiMethodCallExpression){
            // special case as findViewById(R.id.btn_signIn).setOnClickListener()
            PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression)psiExpression;
            String callSignature = MethodHelper.getCallSignature(psiMethodCallExpression, false);
            if (callSignature.equals(Signatures.FIND_VIEW_BY_ID)){
                // get the 1st argument inside the function findViewById as R.id.btn_signIn
                PsiExpression argument = psiMethodCallExpression.getArgumentList().getExpressions()[0];

                if (!(argument instanceof PsiReferenceExpression)) {
                    return null;
                }

                PsiReferenceExpression XMLReference = (PsiReferenceExpression) argument;
                PsiElement xml = XMLReference.getLastChild();
                xmlId[0] = xml.getText();
            }

        } else {
            // normal case as Button btnSignIn = findViewById(R.id.btn_signIn)
            PsiElement expressionFirstChild = expression.getFirstChild();
            PsiReferenceExpression referenceExpression = (PsiReferenceExpression) expressionFirstChild.getFirstChild();
            PsiElement reference = referenceExpression.resolve();
            if (reference instanceof PsiLocalVariable) {
                PsiLocalVariable localVariable = (PsiLocalVariable) reference;
                if (localVariable == null) {
                    return null;
                }

                PsiElement initializer = localVariable.getInitializer();

                if (!(initializer instanceof PsiMethodCallExpression)) {
                    initializer = localVariable.getInitializer().getLastChild();
                }

                PsiMethodCallExpression getViewById = (PsiMethodCallExpression) initializer;
                PsiExpression argument = getViewById.getArgumentList().getExpressions()[0];

                if (!(argument instanceof PsiReferenceExpression)) {
                    return null;
                }

                PsiReferenceExpression XMLReference = (PsiReferenceExpression) argument;
                PsiElement xml = XMLReference.getLastChild();
                xmlId[0] = xml.getText();
            } else {
                // associate UI id via @BindView
                expression.getContainingFile().accept(new JavaRecursiveElementVisitor() {

                    @Override
                    public void visitField(PsiField field) {
                        if (field.getName().contains(reference.getText())) {
                            PsiAnnotation[] annotations = field.getAnnotations();
                            for (PsiAnnotation annotation : annotations) {
                                if (annotation.getQualifiedName().contains("BindView")) {
                                    PsiAnnotationParameterList paramList = annotation.getParameterList();
                                    PsiNameValuePair[] nameValuePairs = paramList.getAttributes();
                                    for (PsiNameValuePair element : nameValuePairs) {
                                        String[] temp = element.getText().split("\\.");
                                        if (temp.length > 2) {
                                            xmlId[0] = temp[2];
                                        }
                                    }
                                }
                            }
                        }
                    }

                });
            }

            if (xmlId[0] == null){
                // try to find the assignment with findViewById for that PsiField
                PsiField localField = (PsiField) reference;
                expression.getContainingFile().accept(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
                        super.visitAssignmentExpression(expression);
                        if (expression.getLExpression().getText().contains(localField.getName())){
                            // assignment as txtTest = findViewById(R.id.btn_test);
                            // take the right expression as findViewById(R.id.btn_test)
                            PsiElement initializer = expression.getRExpression();

                            if (!(initializer instanceof PsiMethodCallExpression)) {
                                initializer = initializer.getLastChild();
                            }

                            String callSignature = MethodHelper.getCallSignature((PsiMethodCallExpression)initializer, false);
                            if (callSignature.equals(Signatures.FIND_VIEW_BY_ID)){
                                // get the 1st argument as "R.id.btn_test"
                                PsiExpression argument = ((PsiMethodCallExpression) initializer).getArgumentList().getExpressions()[0];

                                if (!(argument instanceof PsiReferenceExpression)) {
                                    return;
                                }

                                PsiReferenceExpression XMLReference = (PsiReferenceExpression) argument;
                                PsiElement xml = XMLReference.getLastChild();
                                xmlId[0] = xml.getText();
                            }
                        }
                    }
                });
            }
        }
        return xmlId[0];
    }

    /**
     *
     * @param elementId
     * @param layoutFile
     * @return UIElement being declared in the XML layout file, i.e. Button or EditText
     */
    public static PsiElement getXMLInLayout(String elementId, PsiFile layoutFile){
        PsiElement[] layoutPosition = {null};
        layoutFile.accept(new XmlRecursiveElementVisitor(){

            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);
                XmlAttribute iDAttribute = tag.getAttribute("android:id");
                if(iDAttribute != null){
                    String id = iDAttribute.getValue().split("/")[1];
                    if (id != null && elementId.equals(id)){
                        layoutPosition[0] = tag;
                    }
                }

            }
        });


        return layoutPosition[0];
    }

    public static PsiElement getViewHandlerMethod(PsiMethodCallExpression expression) {
        String callSignature = MethodHelper.getCallSignature(expression, true);
        if (callSignature == null){
            return null;
        }
        PsiExpressionList argumentList = expression.getArgumentList();
        if (argumentList.isEmpty()) {
            return null;
        }
        PsiExpression argument = argumentList.getExpressions()[0];
        final PsiElement[] onListenerHandlerVisitor = {null};

        if (argument instanceof PsiLambdaExpression) {
            PsiCodeBlock psiCodeBlock = (PsiCodeBlock)((PsiLambdaExpression) argument).getBody();
            onListenerHandlerVisitor[0] = psiCodeBlock;
            return onListenerHandlerVisitor[0];

        } else if (argument instanceof PsiConstructorCall) {
            PsiAnonymousClass anonymousClass = ((PsiNewExpressionImpl) argument).getAnonymousClass();
            if (anonymousClass == null){
                // in case of using a concrete instance from a Listener class being defined outside,
                // i.e. View.OnClickListener awesomeOnClickListener = new View.OnClickListener() {
                //        @Override
                //        public void onClick(View v) { }
                onListenerHandlerVisitor[0] = ((PsiNewExpressionImpl) argument).getClassReference().getReference().resolve();
            } else {
                // in case of using Anonymous class,
                // i.e. btnExample.setOnTouchListener(new View.OnTouchListener() {
                //            @Override
                //            public boolean onTouch(View v, MotionEvent event) {}});
                onListenerHandlerVisitor[0] = argument;

            }
        } else if (argument instanceof PsiReferenceExpression) {
            onListenerHandlerVisitor[0] = ((PsiReferenceExpression) argument).resolve();
            if (onListenerHandlerVisitor[0] instanceof PsiField){
                // i.e. btnSignin.setOnClickListener(awesomeListener);
                PsiField listenerHandlerField = (PsiField) onListenerHandlerVisitor[0];
                expression.getContainingFile().accept(new JavaRecursiveElementVisitor() {

                    @Override
                    public void visitField(PsiField field) {
                        if(listenerHandlerField.getName().equals(field.getName())){
                            PsiExpression methodExpression = (PsiExpression) field.getInitializer();
                            onListenerHandlerVisitor[0] = methodExpression;
                        }
                    }

                });
            }
        } else if (argument instanceof  PsiThisExpression){
            // i.e. btnSignin.setOnClickListener(this);
            String listenerHandlerSignature = Signatures.listenerHandlers.get(callSignature);
            PsiClass containingClass = PsiTreeUtil.getParentOfType(expression, PsiClass.class);
            expression.getContainingFile().accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethod(PsiMethod method) {
                    String methodSignature = MethodHelper.getMethodSignature(method, false);
                    PsiClass containingClassOfMethod = PsiTreeUtil.getParentOfType(method, PsiClass.class);
                    if (methodSignature.equals(listenerHandlerSignature) && containingClass.equals(containingClassOfMethod)){
                        onListenerHandlerVisitor[0] = method;
                    }
                }
            });
            return (PsiMethod)onListenerHandlerVisitor[0];
        }
        if (null == onListenerHandlerVisitor[0]) {
            return null;
        }

        final PsiMethod[] psiMethod = new PsiMethod[1];

        onListenerHandlerVisitor[0].acceptChildren(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                String methodSignature = MethodHelper.getMethodSignature(method, false);
                if (MethodHelper.isListenerHandler(methodSignature)) {
                    psiMethod[0] = method;
                }
            }
        });


        return psiMethod[0];
    }

    /**
     *
     * @param expression
     * @return true/false whether the method expression is calling THIS as an argument or not, i.e. btnSignin.setOnClickListener(THIS)
     */
    public static boolean isMethodCallWithThisArgument(PsiMethodCallExpression expression){
        boolean isMethodCallWithThisArgument = false;
        PsiExpressionList argumentList = expression.getArgumentList();
        if (!argumentList.isEmpty()) {
            PsiExpression argument = argumentList.getExpressions()[0];
            if (argument instanceof PsiThisExpression){
                isMethodCallWithThisArgument = true;
            }
        }
        return isMethodCallWithThisArgument;
    }

    /**
     * Function to remove duplicates from an ArrayList
     * @param list
     * @param <T>
     * @return list with unique values
     */
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }


    /**
     *
     * @param mainExpression the expression from where we search upward to see if any layout inflating is being called within the parent function
     * @return layoutName to be inflated if any
     */
    public static String retrieveInflatedChildView(PsiMethodCallExpression mainExpression){
        String inflatedLayoutName[] = {null};
        PsiMethod parentMethod = PsiTreeUtil.getParentOfType(mainExpression, PsiMethod.class);
        parentMethod.acceptChildren(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                String signature = MethodHelper.getCallSignature(expression, true);
                if (signature != null && MethodHelper.isSetLayoutCall(signature)){
                    String layoutName = MethodHelper.retrieveLayout(expression);
                    if (expression.getTextOffset() < mainExpression.getTextOffset()){
                        inflatedLayoutName[0] = layoutName;
                    }
                }
            }
        });
        return inflatedLayoutName[0];
    }

    /**
     *
     * @param codeFileName
     * @return associated layout file name where there are existing UI elements to be considered mismatch
     */
    public static PsiFile retrieveAssociatedLayout(String codeFileName){
        for (UIElement uiElement : Holder.getUiElements()){
            if (uiElement.getCodeFile().getName().equals(codeFileName)){
                return uiElement.getXmlFile();
            }
        }
        return null;
    }

    /**
     *
     * @param expression
     * @return true if the expression like setOnClickListener() include the Lamda expression inside the listener handler onClick()
     */
    public static boolean isLamdaExpressionIncluded(PsiMethodCallExpression expression){
        boolean isLamdaExpressionIncluded = false;
            PsiExpressionList argumentList = expression.getArgumentList();
            if (!argumentList.isEmpty()) {
                PsiExpression argument = argumentList.getExpressions()[0];

                if (argument instanceof PsiLambdaExpression) {
                    isLamdaExpressionIncluded = true;
                }
            }
        return isLamdaExpressionIncluded;
    }

}

