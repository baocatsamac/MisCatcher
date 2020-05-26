package helper;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import handler.FileManager;
import service.Holder;

import java.util.ArrayList;
import java.util.List;

public class XMLHelper {

    public static void retrieveStringsXMLFile(Project project) {
        // crawl to take strings.xml file
        PsiFile stringsXMLFile = null;
        List<PsiFile> files = FileManager.getFiles(project, StdFileTypes.XML);
        for (PsiFile file : files) {
            XmlFile xmlFile = (XmlFile) file;
            if (xmlFile.getName().equals("strings.xml")) {
                stringsXMLFile = xmlFile;
                break;
            }
        }
        // save this strings.xml file into static Holder variable for later use
        Holder.setStringsXMLFile(stringsXMLFile);
    }

    public static void retrieveAndroidManifestFile(Project project) {
        // crawl to take strings.xml file
        PsiFile androidManifestFile = null;
        List<PsiFile> files = FileManager.getFiles(project, StdFileTypes.XML);
        for (PsiFile file : files) {
            XmlFile xmlFile = (XmlFile) file;
            if (xmlFile.getName().equals("AndroidManifest.xml")) {
                androidManifestFile = xmlFile;
                break;
            }
        }
        // save this strings.xml file into static Holder variable for later use
        Holder.setAndroidManifestXMLFile(androidManifestFile);
    }

    /**
     * @param stringLabel label from string resource, i.e. app_name from @string/app_name
     * @return string content from strings.xml file
     */
    public static String retrieveStringContentFromResource(String stringLabel) {
        PsiFile stringsXMLFile = Holder.getStringsXMLFile();
        final String[] content = {null};
        if (stringsXMLFile != null) {
            stringsXMLFile.accept(new XmlRecursiveElementVisitor() {

                @Override
                public void visitXmlTag(XmlTag tag) {
                    super.visitXmlTag(tag);
                    XmlAttribute nameAttribute = tag.getAttribute("name");
                    if (nameAttribute != null && nameAttribute.getValue().equals(stringLabel)) {
                        content[0] = tag.getValue().getText();
                    }
                }
            });
        }

        return content[0];
    }

    /**
     * @param project
     * @param layoutFile
     * @return list of all text elements belonging to the layoutFile
     */
    public static ArrayList<String> getAllTextElementsFromUI(Project project, PsiFile layoutFile) {
        ArrayList<String> textElements = new ArrayList<>();

        ArrayList<String> includeLayouts = new ArrayList<>();
        if (layoutFile != null) {
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
                        // normal XML tag --> retrieve all text content belong to this tag
                        ArrayList<String> allTextContentPerTag = getAllTextContentPerTag(tag);
                        textElements.addAll(allTextContentPerTag);
                    }
                }
            });
        }


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
                        ArrayList<String> allTextContentPerTag = getAllTextContentPerTag(tag);
                        textElements.addAll(allTextContentPerTag);
                    }
                });
            }
        }
        return MethodHelper.removeDuplicates(textElements);
    }

    /**
     * @param tag
     * @return all possible text content can be declared inside tag element
     */
    private static ArrayList<String> getAllTextContentPerTag(XmlTag tag) {
        ArrayList<String> textItems = new ArrayList<>();
        // get TEXT content
        XmlAttribute textAttribute = tag.getAttribute("android:text");
        if (textAttribute != null) {
            String text = textAttribute.getValue();
            if (text != null && text.contains("@string")) {
                text = XMLHelper.retrieveStringContentFromResource(text.split("/")[1]);
            }
            textItems.add(text);
        }

        // get HINT content
        XmlAttribute hintAttribute = tag.getAttribute("android:hint");
        if (hintAttribute != null) {
            String text = hintAttribute.getValue();
            if (text != null && text.contains("@string")) {
                text = XMLHelper.retrieveStringContentFromResource(text.split("/")[1]);
            }
            textItems.add(text);
        }

        // get CONTENT DESCRIPTION content
        XmlAttribute contentDescriptionAttribute = tag.getAttribute("android:contentDescription");
        if (contentDescriptionAttribute != null) {
            String text = contentDescriptionAttribute.getValue();
            if (text != null && text.contains("@string")) {
                text = XMLHelper.retrieveStringContentFromResource(text.split("/")[1]);
            }
            textItems.add(text);
        }

        // get TOOLTIP content
        XmlAttribute tooltipAttribute = tag.getAttribute("android:tooltipText");
        if (tooltipAttribute != null) {
            String text = tooltipAttribute.getValue();
            if (text != null && text.contains("@string")) {
                text = XMLHelper.retrieveStringContentFromResource(text.split("/")[1]);
            }
            textItems.add(text);
        }

        return textItems;
    }

    /**
     *
     * @param drawableRelativePath i.e. @drawable/app_icon
     * @return absolute path for the trained model to load & classify
     */
//    public static String getAbsoluteDrawablePath(Project project, Path drawableRelativePath){
//
//        // crawl to take app_icon.png/jpg or whatever tail part
//        String absoluteImgPath = null;
//        List<PsiFile> files = FileManager.getFiles(project, StdFileTypes.XML);
//        ResourceFolderManager.getInstance().getFolders()
//        for (PsiFile file : files) {
//            XmlFile xmlFile = (XmlFile) file;
//            if (xmlFile.getName().equals("AndroidManifest.xml")) {
//                androidManifestFile = xmlFile;
//                break;
//            }
//        }
//
//    }
}
