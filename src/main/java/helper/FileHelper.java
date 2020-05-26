package helper;

import com.google.common.io.Files;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;
import service.Holder;

import java.io.*;
import java.util.ArrayList;

public class FileHelper {
    private static final Logger LOG = Logger.getInstance(FileHelper.class);

    //TODO data folder may be removed?
    public static File createDataFolder() {
        try {
            File dataDir = Files.createTempDir();
            if (dataDir.exists()) {
                dataDir.delete();
            }
            dataDir.mkdirs();
            return dataDir;
        } catch (Exception ex) {
            LOG.warn(ex.getMessage());
        }
        return null;
    }

    public static boolean isKotlinOrJavaOrXmlFile(PsiFile file) {
        boolean isKotlinOrJavaOrXmlFile = false;
        if (file instanceof PsiJavaFile || file instanceof KtFile || file instanceof XmlFile)
            isKotlinOrJavaOrXmlFile = true;
        return isKotlinOrJavaOrXmlFile;
    }

    public static boolean isKotlinOrJavaOrXmlFile(@NotNull VirtualFileEvent event){
        boolean isKotlinOrJavaOrXmlFile = false;
        //TODO with .xml files, we need to check whether these files belong to app/main/res/ folder within Android project; otherwise it decrease performance significantly because there are a lot of redundant .xml files
//        if (event.getFile().getName().endsWith(".java") || event.getFile().getName().endsWith(".kt") || event.getFile().getName().endsWith(".xml"))
        if (event.getFile().getName().endsWith(".java") || event.getFile().getName().endsWith(".kt"))
            isKotlinOrJavaOrXmlFile = true;
        return isKotlinOrJavaOrXmlFile;
    }

}
