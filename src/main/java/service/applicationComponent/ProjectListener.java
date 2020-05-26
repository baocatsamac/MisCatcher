package service.applicationComponent;

import com.intellij.analysis.AnalysisScopeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.profile.codeInspection.InspectionProfileManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import constants.Topics;
import handler.JavaHandler;
import handler.XMLHandler;
import helper.ImageClassificationHelper;
import helper.LogHelper;
import helper.PermissionHelper;
import helper.XMLHelper;
import listener.FileChangeListener;
import listener.QuickFixListener;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;

public class ProjectListener implements ApplicationComponent {
    private MessageBusConnection connection;

    @Override
    public void initComponent() {
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        //listen for project opened event (if this is the first time, then perform lib dependency analysis
        connection = messageBus.connect();
        InspectionProfileManager.getInstance();
        connection.subscribe(Topics.QUICK_FIX_LISTENER_TOPIC, new QuickFixListener());


        connection.subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {

            @Override
            public void projectOpened(@NotNull Project project) {
                try {

                    ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();

                    if (indicator != null) {
                        if (indicator.isCanceled()) {
                            throw new ProcessCanceledException();
                        }
                        indicator.setText(AnalysisScopeBundle.message("analyzing.project"));

//                        indicator.setText2(getRelativeToProjectPath(virtualFile));
//
//                        if (myTotalFileCount > 0) {
//                            indicator.setFraction(((double) ++myFileCount) / myTotalFileCount);
//                        }
                    }

                    // retrieve permissions and permission groups mappings
                    PermissionHelper.retrievePermissionMappings();
                    // retrieve permissions being declared within AndroidManifest.xml
                    PermissionHelper.retrieveAndroidManifestPermissions(project);
                    // retrieve Content Provider permission-uri mappings
                    PermissionHelper.loadContentProviderPermissionMappings();

                    // retrieve strings.xml file for the project
                    XMLHelper.retrieveStringsXMLFile(project);

                    // retrieve AndroidManifest.xml file for the project
                    XMLHelper.retrieveAndroidManifestFile(project);

                    // load keras image classification trained model
                    ImageClassificationHelper.loadModel();

                    JavaHandler.inspectUIElementsWithinCode(project);
                    XMLHandler.updateDataFromLayout(project);

                    VirtualFileManager.getInstance().addVirtualFileListener(new FileChangeListener(project), project);

                } catch (Exception ex) {
                    LogHelper.logInfo(ex.getMessage());
                }
            }

            @Override
            public void projectClosing(Project project) {

            }
        });
//       TelemetryHelper.checkToShowWelcomeDialog(null);
    }
}

