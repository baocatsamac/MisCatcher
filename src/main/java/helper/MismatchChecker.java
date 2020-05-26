package helper;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import constants.XMLTags;
import handler.FileManager;
import object.Method;
import object.UIElement;
import service.Holder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MismatchChecker {

    /**
     * Check whether there is any mismatch between actual used permissions in listener handler and advertised permissions from associated layout
     *
     * @param uiElement
     * @param method
     * @param advertisedPermissions
     * @return
     */
    public static ArrayList<PsiElement> retrieveMismatchPermissionUICodeLines(UIElement uiElement, Method method, ArrayList<String> advertisedPermissions) {
        ArrayList<PsiElement> mismatchPermissionUICodeLines = new ArrayList<>();
        // firstly check text content to see whether there is any mismatch
        HashMap<PsiElement, ArrayList<String>> codeLinePermissions = method.getCodeLinePermissionMappings();
        for (Map.Entry<PsiElement, ArrayList<String>> me : codeLinePermissions.entrySet()) {
            PsiElement codeLine = me.getKey();
            ArrayList<String> actualUsedPermissionGroups = me.getValue();
            // check whether there is a permission mismatch here (when at least one actual used permission is not involved in advertised permissions                              // inferred from contextual UIs)
            for (String actualUsedPerm : actualUsedPermissionGroups) {
                if (!advertisedPermissions.contains(actualUsedPerm)) {
                    mismatchPermissionUICodeLines.add(codeLine);
                }
            }
        }


        // TODO secondly check Image content as if any mismatch

        return MethodHelper.removeDuplicates(mismatchPermissionUICodeLines);
    }


    /**
     * Retrieve all the mismatch code lines along with its used permissions inside the listener handler code
     *
     * @param method
     * @param advertisedPermissions
     * @return
     */
    public static HashMap<PsiElement, ArrayList<String>> retrieveMismatchCodeLines(Method method, ArrayList<String> advertisedPermissions) {
        HashMap<PsiElement, ArrayList<String>> mismatches = new HashMap<>();
        // firstly check text content to see whether there is any mismatch
        HashMap<PsiElement, ArrayList<String>> codeLinePermissions = method.getCodeLinePermissionMappings();
        for (Map.Entry<PsiElement, ArrayList<String>> me : codeLinePermissions.entrySet()) {
            PsiElement codeLine = me.getKey();
            ArrayList<String> actualUsedPermissionGroups = me.getValue();
            // check whether there is a permission mismatch here (when at least one actual used permission is not involved in advertised permissions                              // inferred from contextual UIs)
            for (String actualUsedPerm : actualUsedPermissionGroups) {
                if (!advertisedPermissions.contains(actualUsedPerm)) {
                    mismatches.put(codeLine, actualUsedPermissionGroups);
                }
            }
        }


        // TODO secondly check Image content as if any mismatch

        return mismatches;
    }

    /**
     * @param project
     * @param layoutFile
     * @return list of Permission Groups that are being inferred from all the UI elements in this layout file
     */
    public static ArrayList<String> inferPermissionFromUI(Project project, PsiFile layoutFile) {
        ArrayList<String> permissionGroups = new ArrayList<>();

        // retrieve all the text elements and images within layout file
        HashMap<XmlTag, ArrayList<String>> xmlTagKeywordList = retrieveKeywordsFromLayout(project, layoutFile);
        for (ArrayList<String> keywords : xmlTagKeywordList.values()){
            for (String text : keywords){
                if (text != null) {
                    ArrayList<String> permissionGroupsFromText = PermissionHelper.inferPermissionsFromText(text.toLowerCase());
                    permissionGroups.addAll(permissionGroupsFromText);
                }
            }
        }
        return MethodHelper.removeDuplicates(permissionGroups);
    }

    public static HashMap<XmlTag, ArrayList<String>> retrieveKeywordsFromLayout(Project project, PsiFile layoutFile){
        HashMap<XmlTag, ArrayList<String>> xmlTagKeywordList = new HashMap<>();
        ArrayList<String> includeLayouts = new ArrayList<>();
        layoutFile.accept(new XmlRecursiveElementVisitor() {

            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);

                if (tag.getName().equals("include") || tag.getName().equals("merge")) {
                    // special XML tag as include or merge layout --> retrieve include/merge layouts
                    XmlAttribute layoutAttribute = tag.getAttribute("layout");
                    if (layoutAttribute != null) {
                        String[] splits = layoutAttribute.getValue().split("/");
                        if (splits.length >= 2) {
                            includeLayouts.add(splits[1] + ".xml");
                        }
                    }
                } else {
                    // normal XML tag --> retrieve keywords
                    ArrayList<String> keywordsPerTags = inferKeywordsPerTag(tag);
                    xmlTagKeywordList.put(tag, MethodHelper.removeDuplicates(keywordsPerTags));
                }
            }
        });

        if (includeLayouts.size() != 0){
            List<PsiFile> files = FileManager.getFiles(project, StdFileTypes.XML);
            for (String includeLayout : includeLayouts) {
                PsiFile includeLayoutFile = null;
                for (PsiFile file : files) {
                    XmlFile xmlFile = (XmlFile) file;
                    // find the layout file based on its name
                    if (xmlFile.getName().equals(includeLayout)) {
                        includeLayoutFile = xmlFile;
                        break;
                    }
                }
                if (includeLayoutFile != null) {
                    includeLayoutFile.accept(new XmlRecursiveElementVisitor() {

                        @Override
                        public void visitXmlTag(XmlTag tag) {
                            super.visitXmlTag(tag);
                            ArrayList<String> keywordsPerTag = inferKeywordsPerTag(tag);
                            xmlTagKeywordList.put(tag, MethodHelper.removeDuplicates(keywordsPerTag));
                        }
                    });
                }
            }
        }

        return xmlTagKeywordList;
    }


    /**
     * @param tag, i.e. Button, ImageView
     * @return list of keywords being inferred from all text attributes and classified images of that tag
     */
    private static ArrayList<String> inferKeywordsPerTag(XmlTag tag) {
        ArrayList<String> keywords = new ArrayList<>();
        // get TEXT content
        XmlAttribute textAttribute = tag.getAttribute("android:text");
        if (textAttribute != null) {
            String text = textAttribute.getValue();
            if (text != null){
                keywords.add(text.toLowerCase());
            }
        }

        // get HINT content
        XmlAttribute hintAttribute = tag.getAttribute("android:hint");
        if (hintAttribute != null) {
            String text = hintAttribute.getValue();
            if (text != null){
                keywords.add(text.toLowerCase());
            }
        }

        // get CONTENT DESCRIPTION content
        XmlAttribute contentDescriptionAttribute = tag.getAttribute("android:contentDescription");
        if (contentDescriptionAttribute != null) {
            String text = contentDescriptionAttribute.getValue();
            if (text != null){
                keywords.add(text.toLowerCase());
            }
        }

        // get TOOLTIP content
        XmlAttribute tooltipAttribute = tag.getAttribute("android:tooltipText");
        if (tooltipAttribute != null) {
            String text = tooltipAttribute.getValue();
            if (text != null){
                keywords.add(text.toLowerCase());
            }
        }

        XmlAttribute imgAttr = tag.getAttribute(XMLTags.DRAWABLE_LEFT);
        ArrayList<String> imgPaths = new ArrayList<>();
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.DRAWABLE_RIGHT);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.DRAWABLE_TOP);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.DRAWABLE_BOTTOM);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.DRAWABLE_START);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.DRAWABLE_END);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.IMG_SRC);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.IMG_FLOATING_BUTTON);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.IMG_BACKGROUND);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }
        imgAttr = tag.getAttribute(XMLTags.IMG_FOREGROUND);
        if (null != imgAttr && imgAttr.getValue() != null) {
            imgPaths.add(imgAttr.getValue());
        }

        // retrieve text content from strings.xml if any
        // then defer the permission group via keyword matching technique
        for (int i = 0; i < keywords.size(); i++) {
            if (keywords.get(i).contains("@string/")) {
                String text = XMLHelper.retrieveStringContentFromResource(keywords.get(i).split("/")[1]);
                if (text != null){
                    keywords.set(i, text.toLowerCase());
                }

            }
    }

        // then defer the permission group from image via trained classification model
        VirtualFile resDir = tag.getContainingFile().getVirtualFile().getParent().getParent();

        for (String imageVar : imgPaths) {
            String androidDrawablePath = null;
            if (imageVar.startsWith("@drawable/")) {
                // imagePath as @drawable/btn_share
                androidDrawablePath = getAndroidImageDrawablePath(resDir.getCanonicalPath(), imageVar);

            } else if (imageVar.startsWith("@mipmap/")) {
                // imagePath as @mipmap/btn_share
                androidDrawablePath = getAndroidImageMipmapPath(resDir.getCanonicalPath(), imageVar);
            }
            if (androidDrawablePath != null) {
                keywords.add(ImageClassificationHelper.classifyImage(androidDrawablePath));
            }
        }

        return MethodHelper.removeDuplicates(keywords);
    }

    /**
     *
     * @param resPath as project_absolute_path/app/src/main/res
     * @param drawableVar as @drawable/btn_share
     * @return
     */
    private static String getAndroidImageDrawablePath(String resPath, String drawableVar) {
        String androidDrawablePath = null;
        List<String> imageFormats = ImmutableList.of(".jpg", ".png");
        String imageName = drawableVar.split("/")[1];
        List<String> drawableDirs = ImmutableList.of("drawable", "drawable-ldpi", "drawable-mdpi", "drawable-hdpi", "drawable-xhdpi", "drawable-xxhdpi", "drawable-xxxhdpi");
        for (String drawable : drawableDirs) {
            for (String imgFormat : imageFormats) {
                String tempPath = resPath + "/" + drawable + "/" + imageName + imgFormat;
                File file = new File(tempPath);
                if (file.exists()) {
                    androidDrawablePath = tempPath;
                    return androidDrawablePath;
                }
            }
        }
        return androidDrawablePath;
    }

    /**
     *
     * @param resPath as project_absolute_path/app/src/main/res
     * @param mipmapVar as @mipmap/btn_share
     * @return
     */
    private static String getAndroidImageMipmapPath(String resPath, String mipmapVar) {
        String androidDrawablePath = null;
        List<String> imageFormats = ImmutableList.of(".jpg", ".png");
        String imageName = mipmapVar.split("/")[1];
        List<String> mipmapDirs = ImmutableList.of("mipmap", "mipmap-ldpi", "mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi");
        for (String mipmap : mipmapDirs) {
            for (String imgFormat : imageFormats) {
                String tempPath = resPath + "/" + mipmap + "/" + imageName + imgFormat;
                File file = new File(tempPath);
                if (file.exists()) {
                    androidDrawablePath = tempPath;
                    return androidDrawablePath;
                }
            }
        }
        return androidDrawablePath;
    }


    private static ArrayList<String> inferPermissionsInAndroidManifest(PsiFile codeFile) {
        ArrayList<String> permissionGroups = new ArrayList<>();
        ArrayList<String> textItems = new ArrayList<>();
        if (Holder.getAndroidManifestXMLFile() != null) {
            Holder.getAndroidManifestXMLFile().accept(new XmlRecursiveElementVisitor() {
                @Override
                public void visitXmlTag(XmlTag tag) {
                    super.visitXmlTag(tag);

                    // get TEXT content
                    XmlAttribute textAttribute = tag.getAttribute("android:text");
                    if (textAttribute != null) {
                        String text = textAttribute.getValue();
                        textItems.add(text);
                    }
                }

                @Override
                public void visitXmlElement(XmlElement element) {
                    super.visitXmlElement(element);
                }
            });
        }


        // retrieve text content from strings.xml if any
        // then defer the Permission Group via keyword matching technique
        for (String text : textItems) {
            if (text.contains("@string")) {
                text = XMLHelper.retrieveStringContentFromResource(text.split("/")[1]);
            }
            if (text != null) {
                ArrayList<String> permissionGroupsFromText = PermissionHelper.inferPermissionsFromText(text.toLowerCase());
                permissionGroups.addAll(permissionGroupsFromText);
            }
        }
        return MethodHelper.removeDuplicates(permissionGroups);
    }
}
