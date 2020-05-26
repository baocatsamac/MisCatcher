package helper;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import constants.Constants;
import constants.Keywords;
import constants.PermissionGroupsDangerous;
import constants.Signatures;
import handler.FileManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import service.Holder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermissionHelper {

    private static String ANDROID_MANIFEST_RESOURCE_PATH = "AndroidManifest.xml";
    private static String CONTENT_PROVIDER_MAPPING_RESOURCE_PATH = "cp-map-25.txt";


    private static HashMap<PsiElement, ArrayList<String>> processCalledMethod(PsiMethodCallExpression expression) {
        PsiMethod method = expression.resolveMethod();

        if (null == method) {
            return new HashMap<>();
        }
        return retrievePermissionsRecursively(method);
    }

    /**
     *
     * @param method i.e. onClick(), onTouch()
     * @return permission list being used within that specific method
     */
    public static HashMap<PsiElement, ArrayList<String>> retrievePermissionsRecursively(PsiMethod method) {
        HashMap<PsiElement, ArrayList<String>> expressionPermissionMapping = new HashMap<>();

        method.acceptChildren(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                expressionPermissionMapping.putAll(retrievePermissionsRecursively(expression));
            }
        });

        return expressionPermissionMapping;
    }

    public static HashMap<PsiElement, ArrayList<String>> retrievePermissionsRecursively(PsiMethodCallExpression mainExpression) {
        HashMap<PsiElement, ArrayList<String>> expressionPermissionMapping = new HashMap<>();

        // list of methods can be inspected from the mainExpression
        ArrayList<PsiMethod> psiMethods = new ArrayList<>();

        // get qualifier expression if any, i.e. getLastLocation() in the main expression fusedLocationClient.getLastLocation().addOnSuccessListener(this)
        PsiReferenceExpression referenceExpression = mainExpression.getMethodExpression();
        PsiExpression psiExpression = referenceExpression.getQualifierExpression();
        if (psiExpression instanceof PsiMethodCallExpression){
            psiMethods.add(((PsiMethodCallExpression)psiExpression).resolveMethod());
        }
        PsiMethod mainMethod = mainExpression.resolveMethod();
        psiMethods.add(mainMethod);

        ArrayList<String> permissionGroups = new ArrayList<>();
        for (PsiMethod psiMethod : psiMethods){
            ArrayList<String> permissions;
            if (psiMethod.getContainingFile().getName().equals("ContentResolver.class")){
                // take the 1st argument as URI from method of ContentResolver,
                // i.e. getContentResolver().query(uri, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC ")
                String uri = parseURI(mainExpression.getArgumentList().getExpressions()[0]);
                // DEBUG
                uri = "content://contacts";
                permissions = getContentProviderPermissionGroups(psiMethod, uri);
            } else {
                permissions = getPermissionGroups(psiMethod, mainExpression.getContainingFile());
            }
            if (permissions != null){
                permissionGroups.addAll(MethodHelper.removeDuplicates(permissions));
            }
        }
        if (permissionGroups.size() != 0){
            expressionPermissionMapping.put(mainExpression, permissionGroups);
        }
        // query more for expressions being declared within mainMethod if any
        if (mainMethod != null){
            mainMethod.acceptChildren(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                    expressionPermissionMapping.putAll(retrievePermissionsRecursively(expression));
                }
            });
        }

        return expressionPermissionMapping;
    }


    /**
     *
     * @param ifStatement
     * @return permission list being used within that If code block
     */
    public static HashMap<PsiElement, ArrayList<String>> retrievePermissionsRecursively(PsiStatement ifStatement) {
        HashMap<PsiElement, ArrayList<String>> expressionPermissionMapping = new HashMap<>();

        PsiBlockStatement body;
        if (ifStatement instanceof PsiBlockStatement){
            body = (PsiBlockStatement) ifStatement;
        } else {
            body = (PsiBlockStatement) ifStatement.getLastChild();
        }
        if (body != null) {
            PsiCodeBlock codeBlock = body.getCodeBlock();
            codeBlock.acceptChildren(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                    expressionPermissionMapping.putAll(retrievePermissionsRecursively(expression));
                }
            });
        }

        return expressionPermissionMapping;
    }

    /**
     *
     * @param psiCodeBlock
     * @return permission list being used within that Lamda code block
     */
    public static HashMap<PsiElement, ArrayList<String>> retrievePermissionsRecursively(PsiCodeBlock psiCodeBlock) {
        HashMap<PsiElement, ArrayList<String>> expressionPermissionMapping = new HashMap<>();

        psiCodeBlock.acceptChildren(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                expressionPermissionMapping.putAll(retrievePermissionsRecursively(expression));
            }
        });

        return expressionPermissionMapping;
    }

    /**
     *
     * @param method      Listener handler method like onClick(), onTouch() which being activated by calling function with THIS argument, i.e. btnSignin.setOnclickListener(this)
     * @param UIElementId is the id of a button or a specific UI element to find the correct code block matching that id
     * @return a PsiMethod (the whole method if we cannot find the specific UIElementId) or a PsiIfStatement (the code block within the method to handle for that specific UIElementId)
     */
    public static PsiElement retrieveAssociatedIdCodeBlock(PsiMethod method, String UIElementId) {
        final PsiElement[] ifStatements = {method};
        method.acceptChildren(new JavaRecursiveElementVisitor() {

            @Override
            public void visitBinaryExpression(PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);
                if(expression.getText().contains(UIElementId)){
                    PsiIfStatement statement = PsiTreeUtil.getParentOfType(expression, PsiIfStatement.class);
                    if (statement != null){
                        PsiStatement thenBranch = statement.getThenBranch();
                        ifStatements[0] = thenBranch;
                    }

                }
            }
        });
        return ifStatements[0];
    }

    private static ArrayList<String> getContentProviderPermissionGroups(PsiMethod method, String uri) {
        ArrayList<String> permissionGroups = new ArrayList<>();
        if (null == method) {
            return null;
        }
        Set<String> permissions = new HashSet<>();

        if (Holder.getContentProviderPermissionsMapping().containsKey(uri)){
            HashMap<String, String> permissionRights = Holder.getContentProviderPermissionsMapping().get(uri);
            ArrayList<PsiAnnotation> psiAnnotations = getParameterAnnotation(method);
            for (PsiAnnotation psiAnnotation : psiAnnotations){
                if (psiAnnotation != null) {
                    String permission = null;
                    if (psiAnnotation.getQualifiedName().equals("RequiresPermission.Read")){
                        permission = permissionRights.get("[R]");
                    } else if (psiAnnotation.getQualifiedName().equals("RequiresPermission.Write")){
                        permission = permissionRights.get("[W]");
                    } else if (psiAnnotation.getQualifiedName().equals("RequiresPermission.ReadWrite")){
                        permission = permissionRights.get("[RW]");
                    }
                    if (permission != null){
                        permissions.add(permission);
                    }
                }
            }
        }


        // infer Permission Group from list of permissions
        for (String permission : permissions){
            String permGroup = inferPermissionGroup(permission);
            if ( permGroup != null){
                permissionGroups.add(permGroup);
            }
        }
        return permissionGroups;
    }


    private static ArrayList<String> getPermissionGroups(PsiMethod method, PsiFile currentCodeFile) {
        ArrayList<String> permissionGroups = new ArrayList<>();
        if (null == method) {
            return null;
        }
        Set<String> permissions = new HashSet<>();
//        checkCallingMethodInSameProject(method, currentCodeFile);
        PsiAnnotation psiAnnotation = getPermissionAnnotation(method);
        if (psiAnnotation != null) {
            PsiNameValuePair[] attributes = psiAnnotation.getParameterList().getAttributes();
            PsiNameValuePair attribute = attributes[0];
            PsiAnnotationMemberValue value = attribute.getValue();

            // in case the method requires more than one permission (i.e. "anyOf=Permission1, Permission2" or "allOf=Permission1,Permission2" )
            if (value instanceof PsiArrayInitializerMemberValue) {
                PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) value).getInitializers();
                for (PsiAnnotationMemberValue initializer : initializers) {
                    String permission = initializer.getText();
                    permissions.add(permission);
                }
            } else {
                // in case there is only one required permission
                String permission = value.getText();
                permissions.add(permission);
            }
        } else {
            // if we cannot find permission in Annotation
            // try to search in the Method comment (i.e. {@link android.Manifest.permission#SEND_SMS} permission.)
            permissions.addAll(retrievePermissionsWithinCodeDescription(method));
        }

        // infer Permission Group from list of permissions
        for (String permission : permissions){
            String permGroup = inferPermissionGroup(permission);
            if ( permGroup != null){
                permissionGroups.add(permGroup);
            }
        }
        return permissionGroups;
    }

    /**
     *
     * @param usedPermission, i.e. android.permission.READ_CONTACTS
     * @return corresponding permission group, i.e. android.permission-group.CONTACTS
     */
    public static String inferPermissionGroup(String usedPermission){
        String permissionGroup = null;

        // remove redundant "\"" chars if any
        usedPermission = usedPermission.replace("\"", "");

        // validate the permissions being retrieved from Annotation (i.e. android.Manifest.permission.VIBRATE --> android.permission.VIBRATE)
        if(usedPermission.contains("Manifest")){
            usedPermission = usedPermission.replace("Manifest.", "");
        }

        for (Map.Entry<String, HashSet<String>> map : Holder.getPermissionGroupToPermissionsMapping().entrySet()){
            HashSet<String> permissionList = map.getValue();
            if(permissionList.contains(usedPermission)){
                permissionGroup = map.getKey();
                break;
            }
        }
        return permissionGroup;
    }

    private static PsiAnnotation getPermissionAnnotation(PsiMethod method) {
        Project project = method.getProject();
        String signature = MethodHelper.getMethodSignature(method, true);

        // start looking at .java source code files for RequiresPermission annotation
        String javaFileName = method.getContainingFile().getName().replace(".class", ".java");
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, javaFileName, GlobalSearchScope.allScope(project));
        for (PsiFile sourceFile : filesByName) {
            final PsiMethod[] javaMethod = {null};
            sourceFile.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethod(PsiMethod method) {
                    if (signature.equals(MethodHelper.getMethodSignature(method, true))) {
                        javaMethod[0] = method;
                    }
                }
            });
            if (null != javaMethod[0]) {
                PsiAnnotation[] annotations = javaMethod[0].getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    if (annotation.getQualifiedName().contains("RequiresPermission")) {
                        return annotation;
                    }
                }
                return null;
            }
        }

        // if not found in .java, then continue finding RequiresPermission annotation inside .class files
        String javaClassFileName = method.getContainingFile().getName().replace(".java", ".class");
        filesByName = FilenameIndex.getFilesByName(project, javaClassFileName, GlobalSearchScope.allScope(project));
        for (PsiFile classFile : filesByName) {
            final PsiMethod[] javaMethod = {null};
            classFile.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethod(PsiMethod method) {
                    if (signature.equals(MethodHelper.getMethodSignature(method, true))) {
                        javaMethod[0] = method;
                    }
                }
            });
            if (null != javaMethod[0]) {
                PsiAnnotation[] annotations = javaMethod[0].getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    if (annotation.getQualifiedName().contains("RequiresPermission")) {
                        return annotation;
                    }
                }
                return null;
            }
        }

        return null;
    }

    private static ArrayList<PsiAnnotation> getParameterAnnotation(PsiMethod method) {
        ArrayList<PsiAnnotation> annotations = new ArrayList<>();
        Project project = method.getProject();
        String signature = MethodHelper.getMethodSignature(method, true);
        String javaFileName = method.getContainingFile().getName().replace(".class", ".java");
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, javaFileName, GlobalSearchScope.allScope(project));
        for (PsiFile sourceFile : filesByName) {
            final PsiMethod[] javaMethod = {null};
            sourceFile.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethod(PsiMethod method) {
                    if (signature.equals(MethodHelper.getMethodSignature(method, true))) {
                        javaMethod[0] = method;
                    }
                }
            });
            if (null != javaMethod[0]) {
                PsiParameter[] psiParameters = javaMethod[0].getParameterList().getParameters();
                if (psiParameters.length > 0){
                    // just take the annotations for the 1st parameter as URI param
                    PsiAnnotation[] psiAnnotations = psiParameters[0].getAnnotations();
                    for (PsiAnnotation annotation : psiAnnotations) {
                        if (annotation.getQualifiedName().contains("RequiresPermission")) {
                            annotations.add(annotation);
                        }
                    }
                }

            }
        }
        return annotations;
    }

    public static void retrievePermissionMappings() {
        try {
            // read the AndroidManifest source code for each platform
            InputStream inputStream = PermissionHelper.class.getResourceAsStream("/" + ANDROID_MANIFEST_RESOURCE_PATH);
            // parse manifest xml file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document androidManifest = dBuilder.parse(inputStream);
            androidManifest.getDocumentElement().normalize();


            // iterate over permissions and collect attributes
            NodeList permissions = androidManifest.getElementsByTagName("permission");
            for (int i = 0; i < permissions.getLength(); i++) {
                Element permission = (Element) permissions.item(i);
                String permissionName = permission.getAttribute("android:name");
                String permissionGroup = permission.getAttribute("android:permissionGroup");
                String protectionLevelString = permission.getAttribute("android:protectionLevel");

                // only add this permission into the list if it is dangerous and belong to one of 8 security & privacy groups
                if (PermissionGroupsDangerous.permissionGroupsDangerous.contains(permissionGroup) && protectionLevelString.contains("dangerous")){
                    // add the permission group mapping for permission
                    if (Holder.getPermissionGroupToPermissionsMapping().containsKey(permissionGroup)) {
                        Holder.getPermissionGroupToPermissionsMapping().get(permissionGroup).add(permissionName);
                    } else {
                        HashSet<String> permissionNames = new HashSet<String>();
                        permissionNames.add(permissionName);
                        Holder.getPermissionGroupToPermissionsMapping().put(permissionGroup, permissionNames);
                    }

                    // add the protection level mapping for permission
                    for (String protectionLevel : protectionLevelString.split("\\|")) {
                        if (Holder.getProtectionLevelToPermissionMapping().containsKey(protectionLevel)) {
                            Holder.getProtectionLevelToPermissionMapping().get(protectionLevel).add(permissionName);
                        } else {
                            HashSet<String> permissionNames = new HashSet<String>();
                            permissionNames.add(permissionName);
                            Holder.getProtectionLevelToPermissionMapping().put(protectionLevel, permissionNames);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static List<String> retrievePermissionsWithinCodeDescription(PsiMethod method) {
        List<String> permissions = new ArrayList<>();

        Project project = method.getProject();
        String signature = MethodHelper.getMethodSignature(method, true);
        String javaFileName = method.getContainingFile().getName().replace(".class", ".java");
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, javaFileName, GlobalSearchScope.allScope(project));
        for (PsiFile sourceFile : filesByName) {
            final PsiMethod[] javaMethod = {null};
            sourceFile.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethod(PsiMethod method) {
                    if (signature.equals(MethodHelper.getMethodSignature(method, true))) {
                        javaMethod[0] = method; // javaMethod[0] here is a PsiMethodImpl instance, not like method input as ClsMethodImpl (which cannot support to retrieve Annotation)
                    }
                }
            });
            if (null != javaMethod[0]) {
                PsiDocComment docComment = javaMethod[0].getDocComment();
                if (docComment != null) {
                    String methodDescription = docComment.getText();
                    Pattern pattern = Pattern.compile(".*\\{@link android.Manifest.permission.*\\}.*");
                    Matcher matcher = pattern.matcher(methodDescription);

                    while (matcher.find()) {
                        String permissionDeclaration = matcher.group(0);
                        String permission = permissionDeclaration.substring(permissionDeclaration.indexOf("android"), permissionDeclaration.indexOf("}"));
                        permissions.add(permission.replace("#", "."));
                    }
                }
            }
        }
        return permissions;
    }

    public static void retrieveAndroidManifestPermissions(Project project){
        // find AndroidManifest.xml file
        PsiFile androidManifestFile = null;
        List<PsiFile> files = FileManager.getFiles(project, StdFileTypes.XML);
        for (PsiFile file : files) {
            if( Constants.ANDROID_MANIFEST_FILE.equals(file.getName())){
                androidManifestFile =  file;
                break;
            }
        }
        if (androidManifestFile != null){
            androidManifestFile.accept(new XmlRecursiveElementVisitor(){
                @Override
                public void visitXmlTag(XmlTag tag) {
                    super.visitXmlTag(tag);
                    // retrieve permission via uses-permission tag, i.e.<uses-permission android:name="android.permission.SEND_SMS" />
                    if (tag.getName().equals(Constants.USE_PERMISSION)){
                        String permission = tag.getAttributeValue("android:name");
                        if (permission != null && !permission.isEmpty()){
                            Holder.getAndroidManifestPermissions().add(permission);
                        }
                    }
                }
            });
        }
    }

    /**
     * load Content Provider Permission Mappings from the generated file in the paper AXPLORER (https://github.com/reddr/axplorer)
     */
    public static void loadContentProviderPermissionMappings(){
        BufferedReader reader;
        try {
            InputStream inputStream = PermissionHelper.class.getResourceAsStream("/" + CONTENT_PROVIDER_MAPPING_RESOURCE_PATH);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();

            while (line != null) {
                String[] parts = line.split("\\s\\s");
                String permission = parts[parts.length - 1];
                String readWritePermission = parts[parts.length - 2]; // can be [R] or [W] or [RW]
                String uri = parts[1];

                HashMap<String, String> permissionMap = new HashMap<>();
                permissionMap.put(readWritePermission, permission);

                // only add into the mappings if the permission belongs to dangerous list
                if (Holder.getAllDangerousPermissions().contains(permission)){
                    if (!Holder.getContentProviderPermissionsMapping().containsKey(uri)){
                        Holder.getContentProviderPermissionsMapping().put(uri, permissionMap);
                    } else {
                        Holder.getContentProviderPermissionsMapping().get(uri).putAll(permissionMap);
                    }
                }

                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }


    private static String parseURI(PsiElement uriVar){
        String uri = null;
        uriVar = ((PsiReferenceExpression)uriVar).resolve();
        if (uriVar instanceof PsiLocalVariable){
            PsiReferenceExpression expression = (PsiReferenceExpression) ((PsiLocalVariable)uriVar).getInitializer();
            PsiField field = (PsiField) expression.resolve();
            field.getContainingFile().accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitReferenceExpression(PsiReferenceExpression expression) {
                    super.visitReferenceExpression(expression);
                }
            });
        }
        return uri;
    }

    /**
     * Parse one PsiElement containing permission definition into string content, i.e. ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS
     * @param permissionVar
     * @return permission string
     */
    public static String parsePermission(PsiElement permissionVar){
        String permission = null;
        if (permissionVar instanceof PsiLiteralExpression){
            permission = permissionVar.getText().replace("\"", "");
        } else if (permissionVar instanceof PsiReferenceExpression){
            permissionVar = ((PsiReferenceExpression)permissionVar).resolve();
            if (permissionVar instanceof PsiField){
                PsiElement initializer = ((PsiField)permissionVar).getInitializer();
                if (initializer instanceof PsiLiteralExpression){
                    permission = initializer.getText().replace("\"", "");
                }
            }
        }
        return permission;
    }


    private static void checkCallingMethodInSameProject(PsiMethod method, PsiFile currentCodeFile){
        Project project = method.getProject();
        String signature = MethodHelper.getMethodSignature(method, true);

        // check whether the retrieved method belongs to the current code file or different class in the same project
        if (method.getContainingFile().equals(currentCodeFile)){
            final PsiMethod[] javaMethod = {null};
            currentCodeFile.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitMethod(PsiMethod method) {
                    if (signature.equals(MethodHelper.getMethodSignature(method, true))) {
                        javaMethod[0] = method;
                    }
                }
            });
            if (null != javaMethod[0]) {

                checkCallingMethodInSameProject(javaMethod[0], currentCodeFile);
            }
        } else if (method.getProject().equals(currentCodeFile.getProject())){
            String javaFileName = method.getContainingFile().getName().replace(".class", ".java");
            PsiFile[] filesByName = FilenameIndex.getFilesByName(project, javaFileName,GlobalSearchScope.projectScope(project));
            for (PsiFile sourceFile : filesByName) {
                final PsiMethod[] javaMethod = {null};
                sourceFile.accept(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitMethod(PsiMethod method) {
                        if (signature.equals(MethodHelper.getMethodSignature(method, true))) {
                            javaMethod[0] = method;
                        }
                    }
                });
                if (null != javaMethod[0]) {

                    break;
                }
            }
        }
    }

    /**
     *
     * @param expression as requestPermissions() function
     * @return String array list containing requested permissions as "android.permission.READ_CONTACTS"
     */
    public static  ArrayList<PsiElement> retrieveRequestedPermissions(PsiMethodCallExpression expression){
        ArrayList<PsiElement> requestedPermissions = new ArrayList<>();
        String signature = MethodHelper.getCallSignature(expression, true);
        if (signature != null) {
            PsiExpression[] argumentList = expression.getArgumentList().getExpressions();
            if (argumentList.length >= 2) {
                PsiNewExpression permissionArrayExp = null;
                if (signature.contains(Signatures.REQUEST_PERMISSION_FIRST_TYPE) && argumentList[1] instanceof PsiNewExpression) {
                    // take the 2nd argument in requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}) function
                    permissionArrayExp = (PsiNewExpression) argumentList[1];
                } else if (signature.contains(Signatures.REQUEST_PERMISSION_SECOND_TYPE) && argumentList[0] instanceof PsiNewExpression) {
                    // take the 1st argument in requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}) function
                    permissionArrayExp = (PsiNewExpression) argumentList[0];
                }
                if (permissionArrayExp != null) {
                    PsiExpression[] requestedExpressions = permissionArrayExp.getArrayInitializer().getInitializers();
                    for (PsiExpression exp : requestedExpressions) {
                        requestedPermissions.add(exp);
                    }
                }
            }
        }
        return requestedPermissions;
    }

    /**
     * @param text text content retrieved from UI layout or from Image classification module
     * @return permission group name being inferred from keyword list associated with each particular group
     */
    public static ArrayList<String> inferPermissionsFromText(String text) {
        ArrayList<String> permissionGroups = new ArrayList<>();
        for (Map.Entry<String, Set<String>> map : Keywords.permissionKeywordsMapping.entrySet()) {
            Set<String> correspondingKeywords = map.getValue();
            for (String keyword : correspondingKeywords) {
                if (text.contains(keyword)) {
                    permissionGroups.add(map.getKey());
                }
            }
        }
        return MethodHelper.removeDuplicates(permissionGroups);
    }

    /**
     * @param textList list of text elements with content retrieved from UI layout or from Image classification module
     * @return permission groups being inferred from keyword list associated with each particular group
     */
    public static ArrayList<String> inferPermissionsFromText(ArrayList<String> textList) {
        ArrayList<String> permissionGroups = new ArrayList<>();
        for (String text : textList){
            for (Map.Entry<String, Set<String>> map : Keywords.permissionKeywordsMapping.entrySet()) {
                Set<String> correspondingKeywords = map.getValue();

                for (String keyword : correspondingKeywords) {
                    if (text.contains(keyword)) {
                        permissionGroups.add(map.getKey());
                    }
                }
            }
        }
        return MethodHelper.removeDuplicates(permissionGroups);
    }
}
