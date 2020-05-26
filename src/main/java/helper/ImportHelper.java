package helper;

import com.android.tools.idea.gradle.project.importing.GradleProjectImporter;
import com.android.tools.idea.gradle.project.sync.GradleSyncListener;
import com.android.tools.idea.gradle.util.LocalProperties;
import com.android.tools.idea.sdk.IdeSdks;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import telemetry.LocalLog;

import static com.android.SdkConstants.DOT_GRADLE;
import static com.android.SdkConstants.FN_BUILD_GRADLE;
import static com.google.common.io.Files.createTempDir;
import static com.intellij.openapi.vfs.VfsUtil.findFileByIoFile;
import static com.intellij.openapi.util.io.FileUtil.copy;
import static com.intellij.openapi.util.io.FileUtil.copyDirContent;

import java.io.File;

import static com.intellij.openapi.util.io.FileUtil.ensureExists;
import static com.intellij.openapi.vfs.VfsUtilCore.virtualToIoFile;
import static com.intellij.util.ObjectUtils.assertNotNull;
import static java.lang.String.join;

import com.google.common.io.Files;
import com.google.common.base.Charsets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Matcher;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.intellij.openapi.projectRoots.JdkUtil.checkForJdk;
import static com.intellij.openapi.util.io.FileUtil.filesEqual;
import static org.junit.Assert.fail;

import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;

import javax.swing.*;

public class ImportHelper {
    private final static String BUILD_TOOL_MIN_VERSION = "25.0.2";
    private final static String GRADLE_PLUGIN_RECOMMENDED_VERSION = "3.2.1";
    private static final String MAVEN_URL_PROPERTY = "android.mavenRepoUrl";
    private static final String PROJECT_NAME = "Up2DepExemplary";
    private static LocalLog localLog = LocalLog.getInstance();

    public static void setupSampleProject() {
        ApplicationManager.getApplication().invokeLater(()->init());
//        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(ImportHelper::init));


    }

    private static void init() {
        IdeSdks ideSdks = IdeSdks.getInstance();
        final File androidSdkPath = ideSdks.getAndroidSdkPath();
        if (androidSdkPath == null) {
            JOptionPane.showMessageDialog(null, "Android Sdk is missing, please finish your Android Studio setup and try Exemplary Project again", "Android Sdk is not set", JOptionPane.ERROR_MESSAGE);
        }

        setUpSdks();
        String outputFolder = createTemptProjectFolder(PROJECT_NAME).getAbsolutePath();
        ZipHelper.unZip(PROJECT_NAME, outputFolder);
        File projectPath = setUpProject(outputFolder, true, false, "3.2.1");
        VirtualFile selectedFile = findFileByIoFile(projectPath, true);
        assertNotNull(selectedFile);
//
        createProjectFileForGradleProject(selectedFile);
    }

    private static GradleSyncListener gradleSyncListener = new GradleSyncListener() {


        @Override
        public void syncStarted(@NotNull Project project, boolean b, boolean b1) {

        }

        @Override
        public void setupStarted(@NotNull Project project) {

        }

        @Override
        public void syncSucceeded(@NotNull Project project) {
            ApplicationManager.getApplication().invokeLater(()->{
                String projectFolderPath = project.getBasePath();
                if (projectFolderPath != null) {
                    File buildFile = new File(projectFolderPath, "app/" + FN_BUILD_GRADLE);
                    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
                    final String vfsPath = FileUtil.toSystemIndependentName(buildFile.getAbsolutePath());
                    VirtualFile virtualFile = localFileSystem.refreshAndFindFileByPath(vfsPath);
                    if (virtualFile != null) {
                        FileEditorManager.getInstance(project).openFile(virtualFile, true);
                    }
                }
            });

        }

        @Override
        public void syncFailed(final Project project1, String errorMessage) {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    JOptionPane.showMessageDialog(null, errorMessage, "Set up failed", JOptionPane.ERROR_MESSAGE,
                            AllIcons.General.ErrorDialog);
                    String message = "Failed setup: " + errorMessage;
                    localLog.writeLine(Level.SEVERE, message);

                } catch (Exception e) {
                    localLog.writeLine(e);
                }
            });
        }

        @Override
        public void syncSkipped(@NotNull Project project) {

        }

    };


    private static boolean createProjectFileForGradleProject(VirtualFile selectedFile) {
        boolean isSuccessful = false;
        VirtualFile projectDir = selectedFile.isDirectory() ? selectedFile : selectedFile.getParent();
        File projectDirPath = virtualToIoFile(projectDir);
        try {
//            GradleProjectImporter.getInstance().importProject(projectDir.getName(), projectDirPath, gradleSyncListener);
            isSuccessful = true;
        } catch (Exception e) {
            localLog.writeLine(e);
        }
        return isSuccessful;
    }

    private static File setUpProject(String projectPath,
                                     boolean forOpen,
                                     boolean updateAndroidPluginVersion,
                                     String gradleVersion) {
        final File projectPathFile = new File(projectPath);
        try {
            if (updateAndroidPluginVersion) {
                updateGradleVersions(projectPathFile);
            }

            updateLocalProperties(projectPathFile);

            if (forOpen) {
                File toDotIdea = new File(projectPathFile, Project.DIRECTORY_STORE_FOLDER);
                ensureExists(toDotIdea);
                File fromDotIdea = new File(projectPathFile, join("commonFiles", Project.DIRECTORY_STORE_FOLDER));

                for (File from : fromDotIdea.listFiles()) {
                    if (from.isDirectory()) {
                        File destination = new File(toDotIdea, from.getName());
                        if (!destination.isDirectory()) {
                            copyDirContent(from, destination);
                        }
                        continue;
                    }
                    File to = new File(toDotIdea, from.getName());
                    if (!to.isFile()) {
                        copy(from, to);
                    }
                }
            }
        } catch (Exception ex) {
            localLog.writeLine(ex);
        }
        return projectPathFile;
    }


    private static boolean updateLocalProperties(File projectPath) {
        boolean isSuccessful = false;
        try {
            // Bao update for new Intellij Idea version 171
            IdeSdks ideSdks = IdeSdks.getInstance();
            File androidHomePath = ideSdks.getAndroidSdkPath();
            if (androidHomePath == null) {
                return false;
            }

            LocalProperties localProperties = new LocalProperties(projectPath);
            localProperties.setAndroidSdkPath(androidHomePath);
            localProperties.save();
            isSuccessful = true;
        } catch (Exception ex) {
            localLog.writeLine(ex);
        }
        return isSuccessful;
    }


    private static boolean updateGradleVersions(File file) {
        boolean isSuccessful = false;
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File child : files) {
                        updateGradleVersions(child);
                    }
                }
            } else if (file.getPath().endsWith(DOT_GRADLE) && file.isFile()) {
                String contents = Files.toString(file, Charsets.UTF_8);
//                LogHelper.getInstance().info("updateGradleVersions with contents file as " + contents + " and file input as " + file.toString());

                boolean changed = false;
                Pattern pattern = Pattern.compile("classpath ['\"]com.android.tools.build:gradle:(.+)['\"]");
                Matcher matcher = pattern.matcher(contents);
                if (matcher.find()) {
                    contents = contents.substring(0, matcher.start(1)) + GRADLE_PLUGIN_RECOMMENDED_VERSION +
                            contents.substring(matcher.end(1));
                    changed = true;
                }

                pattern = Pattern.compile("buildToolsVersion ['\"](.+)['\"]");
                matcher = pattern.matcher(contents);
                if (matcher.find()) {
                    contents = contents.substring(0, matcher.start(1)) + BUILD_TOOL_MIN_VERSION +
                            contents.substring(matcher.end(1));
                    changed = true;
                }

                String repository = System.getProperty(MAVEN_URL_PROPERTY);
                if (repository != null) {
                    pattern = Pattern.compile("mavenCentral\\(\\)");
                    matcher = pattern.matcher(contents);
                    if (matcher.find()) {
                        contents = contents.substring(0, matcher.start()) + "maven { url '" + repository + "' };" +
                                contents.substring(matcher.start()); // note: start; not end; we're prepending, not replacing!
                        changed = true;
                    }
                    pattern = Pattern.compile("jcenter\\(\\)");
                    matcher = pattern.matcher(contents);
                    if (matcher.find()) {
                        contents = contents.substring(0, matcher.start()) + "maven { url '" + repository + "' };" +
                                contents.substring(matcher.start()); // note: start; not end; we're prepending, not replacing!
                        changed = true;
                    }
                }

                if (changed) {
                    Files.write(contents, file, Charsets.UTF_8);
                }
            }
            isSuccessful = true;
        } catch (Exception ex) {
            localLog.writeLine(ex);
        }
        return isSuccessful;
    }

    private static File createTemptProjectFolder(String projectName) {
        try {
            File tempDir = null;
            if (SystemUtils.IS_OS_WINDOWS) {
                tempDir = new File(System.getProperty("java.io.tmpdir"));

            } else {
                tempDir = createTempDir();
            }
            return new File(tempDir, projectName).getCanonicalFile();
        } catch (IOException e) {
            localLog.writeLine(e);
        }
        return null;
    }

    public static final String JDK_HOME_FOR_TESTS = "java.home";

    public static void setUpSdks() {
        try {
            // Bao update for new Intellij Idea version 171
            IdeSdks ideSdks = IdeSdks.getInstance();

            final File androidSdkPath = ideSdks.getAndroidSdkPath();

            String jdkHome = getSystemPropertyOrEnvironmentVariable(JDK_HOME_FOR_TESTS);
            if (jdkHome.endsWith("jre")) {
                File jdkHomeFile = new File(jdkHome);
                jdkHome = jdkHomeFile.getParent();
            }
            if (isNullOrEmpty(jdkHome) || !checkForJdk(jdkHome)) {
                fail("Please specify the path to a valid JDK using system property " + JDK_HOME_FOR_TESTS);
            }
            final File jdkPath = new File(jdkHome);

            //TODO put to separate thread

            File currentAndroidSdkPath = ideSdks.getAndroidSdkPath();
            File currentJdkPath = ideSdks.getJdkPath();

            if (!filesEqual(androidSdkPath, currentAndroidSdkPath) || !filesEqual(jdkPath, currentJdkPath)) {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    ideSdks.setAndroidSdkPath(androidSdkPath, null);
                    ideSdks.setJdkPath(jdkPath);
                });
            }
        } catch (Exception ex) {
            localLog.writeLine(ex);
        }
    }

    public static String getSystemPropertyOrEnvironmentVariable(String name) {
        String s = System.getProperty(name);
        return s == null ? System.getenv(name) : s;
    }

}

