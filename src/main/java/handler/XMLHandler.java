package handler;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import constants.XMLTags;
import helper.MethodHelper;
import helper.PermissionHelper;
import object.Method;
import object.TextData;
import object.UIElement;
import service.Holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XMLHandler {

    public static void updateDataFromLayout(Project project) {
        List<PsiFile> files = FileManager.getFiles(project, StdFileTypes.XML);
        for (PsiFile file : files) {
            XmlFile xmlFile = (XmlFile) file;
            // inspect only layout files having Top-Level Interactive Function (i.e. setContentView or LayoutInflater.inflate())
            if (isProperLayout(xmlFile.getName())) {
                updateUIElementDetails(xmlFile, project);
            }

        }
    }

    private static boolean isProperLayout(String xmlFileName) {
        boolean isProperLayout = false;
        for (UIElement uiElement : Holder.getUiElements()) {
            if (uiElement.getXmlFile().getName().equals(xmlFileName)) {
                isProperLayout = true;
                break;
            }
        }
        return isProperLayout;
    }


    /**
     * Maybe deleted in th future, and replaced by above function
     *
     * @param xmlFile
     * @param project
     */
    private static void updateUIElementDetails(XmlFile xmlFile, Project project) {

        xmlFile.accept(new XmlRecursiveElementVisitor() {

            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);
                XmlAttribute idAttr = tag.getAttribute(XMLTags.ID);
                String id = null;
                if (idAttr != null) {
                    id = idAttr.getValue().split("/")[1];
                }
                if (null == id) {
                    return;
                }
                XmlAttribute onClickMethodNameXMLAttr = tag.getAttribute(XMLTags.XML_ON_CLICK);
                String onClickMethodNameXML = null;
                if (onClickMethodNameXMLAttr != null) {
                    onClickMethodNameXML = onClickMethodNameXMLAttr.getValue();
                }


                XmlAttribute textAttr = tag.getAttribute(XMLTags.BUTTON_TXT);
                String text = null;
                if (textAttr != null) {
                    text = textAttr.getValue();
                }

                XmlAttribute hintAttr = tag.getAttribute(XMLTags.BUTTON_HINT);
                String hint = null;
                if (hintAttr != null) {
                    hint = hintAttr.getValue();
                }
                XmlAttribute contentDescriptionAttr = tag.getAttribute(XMLTags.BUTTON_CONTENT_DESCRIPTION);
                String contentDescription = null;
                if (contentDescriptionAttr != null) {
                    contentDescription = contentDescriptionAttr.getValue();
                }
                XmlAttribute tooltipAttr = tag.getAttribute(XMLTags.BUTTON_TOOLTIP);
                String tooltip = null;
                if (tooltipAttr != null) {
                    tooltip = tooltipAttr.getValue();
                }
                TextData textData = new TextData(text, hint, contentDescription, tooltip);


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
                for (UIElement element : Holder.getUiElements()) {
                    if (element.getXmlFile().getName().equals(xmlFile.getName()) && element.getId().equals(id)) {
                        element.setTextData(textData);
                        element.setImagePaths(MethodHelper.removeDuplicates(imgPaths));
                        element.setOnClickXMLMethod(onClickMethodNameXML);
                        if (onClickMethodNameXML != null) {
                            // analyze onClick method being specified in XML file
                            List<PsiFile> javaFiles = FileManager.getFiles(project, StdFileTypes.JAVA);
                            for (PsiFile file : javaFiles) {
                                if (file.getName().equals(element.getCodeFile())) {
                                    String finalOnClickMethodNameXML = onClickMethodNameXML;
                                    file.accept(new JavaRecursiveElementVisitor() {
                                        @Override
                                        public void visitMethod(PsiMethod method) {
                                            if (method.getName().equals(finalOnClickMethodNameXML)) {
                                                String methodSignature = MethodHelper.getMethodSignature(method, true);
                                                // add a new top-level interactive method for specific OnClickListener with custom name from XML file (i.e. signUp(), logIn())
                                                if (!isExistingOnClickMethod(methodSignature, element)) {
                                                    HashMap<PsiElement, ArrayList<String>> permissions = PermissionHelper.retrievePermissionsRecursively(method);
                                                    Method uiInteractiveMethod = new Method(MethodHelper.getMethodSignature(method, true), method, permissions);
                                                    element.getUiInteractiveMethods().add(uiInteractiveMethod);
                                                }
                                            }
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }


    private static boolean isExistingOnClickMethod(String methodSignature, UIElement element) {
        boolean isExistingOnClickMethod = false;
        for (Method method : element.getUiInteractiveMethods()) {
            if (method.getSignature().equals(methodSignature)) {
                isExistingOnClickMethod = true;
                break;
            }
        }
        return isExistingOnClickMethod;
    }
}
