package service;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import object.UIElement;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Holder {

    public static final String APP_CATEGORY = "app_category";
    public static final String APP_DESCRIPTION = "app_description";

    private static MultiLayerNetwork imageClassificationModel = null;

    private static ArrayList<UIElement> uiElements = new ArrayList<>();

    // Android Project information
    private static HashMap<String, String> appProjectData = new HashMap<>();

    // for each permission entry record the permission group
    // as a key and the permission as a value
    private static HashMap<String, HashSet<String>> permissionGroupToPermissionsMapping = new HashMap<>();

    // list of all dangerous permissions belonging to all 8 groups
    private static HashSet<String> dangerousPermissions = null;

    // for each permission entry record the permission
    // as a key and the protection level as a value
    private static HashMap<String, HashSet<String>> protectionLevelToPermissionMapping = new HashMap<>();

    // list of permissions associated with one particular layout file
    private static HashMap<String, ArrayList<String>> layoutAdvertisedPermissionsMapping = new HashMap<>();

    // list of keywords (retrieved from text & image) associated with one particular layout file
    private static HashMap<XmlTag, ArrayList<String>> tagKeywordsMapping = new HashMap<>();

    // list of permissions associated with list of Content Provider URIs
    private static HashMap<String, HashMap<String, String>> contentProviderPermissionsMapping = new HashMap<>();

    // list of requested permissions associated with one java/kotlin file
    private static HashMap<String, ArrayList<PsiElement>> requestedPermissionFileMapping = new HashMap<>();

    // a map of specific permission associated permission rationale along with its position on code file
    private static HashMap<PsiElement, Pair<String, String>> permissionRationaleMapping = new HashMap<>();

    // list of requested permissions associated with its permission check and shouldShowRationale check
    private static HashMap<String, ArrayList<PsiElement>> requestedCheckedShouldShowPermissionMapping = new HashMap<>();

    private static PsiFile stringsXMLFile = null;

    private static PsiFile androidManifestXMLFile = null;

    public static MultiLayerNetwork getImageClassificationModel() {
        return imageClassificationModel;
    }

    public static void setImageClassificationModel(MultiLayerNetwork imageClassificationModel) {
        Holder.imageClassificationModel = imageClassificationModel;
    }

    public static void register(@NotNull UIElement uiElement) {
        uiElements.add(uiElement);
    }

    public static ArrayList<UIElement> getUiElements() {
        return uiElements;
    }

    public static HashMap<String, HashSet<String>> getPermissionGroupToPermissionsMapping() {
        return permissionGroupToPermissionsMapping;
    }

    public static HashMap<String, HashSet<String>> getProtectionLevelToPermissionMapping() {
        return protectionLevelToPermissionMapping;
    }

    private static ArrayList<String> androidManifestPermissions = new ArrayList<>();

    public static ArrayList<String> getAndroidManifestPermissions() {
        return androidManifestPermissions;
    }

    public static void setAndroidManifestPermissions(ArrayList<String> androidManifestPermissions) {
        Holder.androidManifestPermissions = androidManifestPermissions;
    }

    public static HashMap<String, ArrayList<String>> getLayoutAdvertisedPermissionsMapping() {
        return layoutAdvertisedPermissionsMapping;
    }

    public static void setLayoutAdvertisedPermissionsMapping(HashMap<String, ArrayList<String>> layoutAdvertisedPermissionsMapping) {
        Holder.layoutAdvertisedPermissionsMapping = layoutAdvertisedPermissionsMapping;
    }

    public static HashMap<XmlTag, ArrayList<String>> getTagKeywordsMapping() {
        return tagKeywordsMapping;
    }

    public static void setTagKeywordsMapping(HashMap<XmlTag, ArrayList<String>> tagKeywordsMapping) {
        Holder.tagKeywordsMapping = tagKeywordsMapping;
    }

    public static HashMap<String, HashMap<String, String>> getContentProviderPermissionsMapping() {
        return contentProviderPermissionsMapping;
    }

    public static void setContentProviderPermissionsMapping(HashMap<String, HashMap<String, String>> contentProviderPermissionsMapping) {
        Holder.contentProviderPermissionsMapping = contentProviderPermissionsMapping;
    }

    public static HashMap<String, ArrayList<PsiElement>> getRequestedPermissionFileMapping() {
        return requestedPermissionFileMapping;
    }

    public static void setRequestedPermissionFileMapping(HashMap<String, ArrayList<PsiElement>> requestedPermissionFileMapping) {
        Holder.requestedPermissionFileMapping = requestedPermissionFileMapping;
    }

    public static HashMap<PsiElement, Pair<String, String>> getPermissionRationaleMapping() {
        return permissionRationaleMapping;
    }

    public static void setPermissionRationaleMapping(HashMap<PsiElement, Pair<String, String>> permissionRationaleMapping) {
        Holder.permissionRationaleMapping = permissionRationaleMapping;
    }

    public static HashMap<String, ArrayList<PsiElement>> getRequestedCheckedShouldShowPermissionMapping() {
        return requestedCheckedShouldShowPermissionMapping;
    }

    public static void setRequestedCheckedShouldShowPermissionMapping(HashMap<String, ArrayList<PsiElement>> requestedCheckedShouldShowPermissionMapping) {
        Holder.requestedCheckedShouldShowPermissionMapping = requestedCheckedShouldShowPermissionMapping;
    }

    public static HashMap<String, String> getAppProjectData() {
        return appProjectData;
    }

    public static PsiFile getStringsXMLFile() {
        return stringsXMLFile;
    }

    public static void setStringsXMLFile(PsiFile stringsXMLFile) {
        Holder.stringsXMLFile = stringsXMLFile;
    }

    public static PsiFile getAndroidManifestXMLFile() {
        return androidManifestXMLFile;
    }

    public static void setAndroidManifestXMLFile(PsiFile androidManifestXMLFile) {
        Holder.androidManifestXMLFile = androidManifestXMLFile;
    }

    public static UIElement getUIElementById(String id){
        for (UIElement uiElement : uiElements){
            if (uiElement.getId().equals(id)){
                return uiElement;
            }
        }
        return null;
    }

    public static HashSet<String> getAllDangerousPermissions(){
        if (dangerousPermissions == null){
            dangerousPermissions = new HashSet<>();
            for (HashSet<String> oneGroupPermissions : permissionGroupToPermissionsMapping.values()){
                dangerousPermissions.addAll(oneGroupPermissions);
            }
        }

        return  dangerousPermissions;
    }

}
