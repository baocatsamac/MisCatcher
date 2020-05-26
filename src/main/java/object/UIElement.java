package object;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;

public class UIElement {

    public static final UIElement Dummy = new UIElement("Dummy", null, null, null, null, "Dummy", null, null);
    private String id;
    private PsiElement layoutPosition; // position of UI element in XML file
    private TextData textData;
    private ArrayList<String> imagePaths;
    private ArrayList<Method> uiInteractiveMethods = new ArrayList<>();
    private PsiFile codeFile;
    private PsiFile xmlFile;
    private String onClickXMLMethod; // special XML attribute (i.e. android:onClick="signIn") to declare the OnClickListener callback from XML, and ONLY onClick can be defined, other methods like onTouch, onDrag are impossible.

    public PsiElement getLayoutPosition() {
        return layoutPosition;
    }

    public TextData getTextData() {
        return textData;
    }

    public String getId() {
        return id;
    }

    public ArrayList<String> getImagePaths() {
        return imagePaths;
    }

    public ArrayList<Method> getUiInteractiveMethods() {
        return uiInteractiveMethods;
    }

    public PsiFile getCodeFile() {
        return codeFile;
    }

    public PsiFile getXmlFile() {
        return xmlFile;
    }

    public String getOnClickXMLMethod() {
        return onClickXMLMethod;
    }

    public void setOnClickXMLMethod(String onClickXMLMethod) {
        this.onClickXMLMethod = onClickXMLMethod;
    }

    public void setLayoutPosition(PsiElement layoutPosition) {
        this.layoutPosition = layoutPosition;
    }


    public void setTextData(TextData textData) {
        this.textData = textData;
    }

    public void setImagePaths(ArrayList<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public void setUiInteractiveMethods(ArrayList<Method> uiInteractiveMethods) {
        this.uiInteractiveMethods = uiInteractiveMethods;
    }

    public void setCodeFile(PsiFile codeFile) {
        this.codeFile = codeFile;
    }

    public void setXmlFile(PsiFile xmlFile) {
        this.xmlFile = xmlFile;
    }

    public UIElement(String id, TextData textData, PsiElement layoutPosition, ArrayList<String> imagePaths, ArrayList<Method> uiInteractiveMethods, String onClickXMLMethod, PsiFile xmlFile, PsiFile codeFile) {
        this.id = id;
        this.textData = textData;
        this.layoutPosition = layoutPosition;
        this.imagePaths = imagePaths;
        this.uiInteractiveMethods = uiInteractiveMethods;
        this.onClickXMLMethod = onClickXMLMethod;
        this.codeFile = codeFile;
        this.xmlFile = xmlFile;
    }

}
