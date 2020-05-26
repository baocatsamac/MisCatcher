package telemetry;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.IconUtil;
import quickfix.SendFeedback;
import service.localService.LocalDataService;
import ui.telemetry.FeedbackJDialog;
import ui.welcome.DialogTest;
import ui.welcome.WelcomeJDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TelemetryHelper {
    private static final int FILE_SIZE = 2048000;
    private static LocalLog localLog = LocalLog.getInstance();
    private static final int MINUTES = 60000;//60*1000
    private static final int START_SURVEY_AT = 4;
    //in minutes
    private static final double DURATION_BEFORE_ASK_AGAIN = 15;


//    private static boolean shouldAskForExport() {
//
//        boolean shouldAsk = false;
//
//        String logFilePath = localLog.getLogFile();
//        File logFile = new File(logFilePath);
//        if (logFile.length() > FILE_SIZE) {
//            final LocalDataService service = ServiceManager.getService(LocalDataService.class);
//
//            Date lastAlreadySent = service.getLastAlreadySent();
//            if (lastAlreadySent != null) {
//                Date today = new Date();
//                long diff = today.getTime() - lastAlreadySent.getTime();
//                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//                if (days >= Constants.EXPORT_LOG_INTERVAL) {
//                    shouldAsk = true;
//                }
//            } else {
//                shouldAsk = true;
//            }
//        }
//        return shouldAsk;
//    }
//
//    public static void sendUnknownLib(Project project, UnknownLib unknownLib) {
//        final LocalDataService service = ServiceManager.getService(LocalDataService.class);
//        if (service.getExcludedProjects().contains(project.getName())) {
//            return;
//        }
//        RemoteTelemetry.sendMissingLib(unknownLib);
//    }
//
//
//    public static void unsubscribe(PluginProject projectPlugin) {
//        RemoteTelemetry.unSubscribe(projectPlugin);
//    }
//
//    private static Survey getSurvey() {
//        String pluginId = PluginHelper.getPluginId();
//        return RemoteTelemetry.getSurvey(pluginId);
//    }
//
//    public static void checkToAskSurvey(Project project) {
//        try {
//            LocalDataService service = ServiceManager.getService(LocalDataService.class);
//            int countQuickFix = service.getCountQuickFix();
//            if (countQuickFix >= START_SURVEY_AT) {
//                Date lastAsked = service.getLastAsked();
//                Date now = new Date();
//                boolean shouldAsk = false;
//                if (lastAsked != null) {
//                    double diffInMinutes = (float) (now.getTime() - lastAsked.getTime()) / MINUTES;
//                    if (diffInMinutes > DURATION_BEFORE_ASK_AGAIN) {
//                        shouldAsk = true;
//                    }
//                } else {
//                    shouldAsk = true;
//                }
//
//                if (shouldAsk) {
//                    Survey survey = getSurvey();
//                    if (survey != null) {
//                        SurveyNotification exportDataNotificationPanel = new SurveyNotification("Up2Dep", survey);
//                        Notifications.Bus.notify(exportDataNotificationPanel, project);
//                        service.setLastAsked(now);
//                    }
//                }
//
//            }
//        } catch (Exception ex) {
//            localLog.writeLine(ex);
//        }
//    }
//
//    public static void checkToAskExport(Project project) {
//        try {
//            boolean shouldASk = shouldAskForExport();
//            if (shouldASk) {
//                ExportDataNotification exportDataNotificationPanel = new ExportDataNotification("Up2Dep", IconUtil.getAddFolderIcon());
//                Notifications.Bus.notify(exportDataNotificationPanel, project);
//            }
//        } catch (Exception ex) {
//            localLog.writeLine(ex);
//        }
//    }

    public static void checkToShowWelcomeDialog(Project project) {
        try {
//            JFrame frame = WindowManager.getInstance().getFrame(project);
//            WelcomeJDialog welcomeJDialog = new WelcomeJDialog(frame);
//            welcomeJDialog.setVisible(true);

            // TEST
            DialogTest dialogTest = new DialogTest(null);

//            LocalDataService localDataService = ServiceManager.getService(LocalDataService.class);
//            if (!localDataService.getShowedWelcome()) {
//                WelcomeJDialog welcomeJDialog = new WelcomeJDialog(component);
//                localDataService.setShowedWelcome(true);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    public static void sendFeedback(String feedback, String check) {
//        String pluginId = PluginHelper.getPluginId();
//        Feedback feedbackObj = new Feedback(pluginId, feedback, check);
//        boolean success = RemoteTelemetry.sendFeedback(feedbackObj);
//        if (!success) {
//            localLog.writeLine("Fail to send feedback : " + feedback);
//        }
//    }
//
//    public static void sendSurveyParticipation(int surveyId) {
//
//        String pluginId = PluginHelper.getPluginId();
//
//        PluginSurvey pluginSurvey = new PluginSurvey(pluginId, surveyId);
//        boolean success = RemoteTelemetry.sendSurveyParticipation(pluginSurvey);
//        if (!success) {
//            localLog.writeLine("Fail to send survey : " + surveyId + "\t pluginId: " + pluginId);
//        }
//    }


//    public static void sendLibStats(Project project, String libName, ArrayList<LocalQuickFix> quickFixes) {
//        final LocalDataService service = ServiceManager.getService(LocalDataService.class);
//        if (service.getExcludedProjects().contains(project.getName())) {
//            return;
//        }
//
//        String projectName = project.getName();
//        String hashedProjectName = secureHash(projectName);
//        ArrayList<String> quickfixes = new ArrayList<>();
//        for (LocalQuickFix quickFix : quickFixes) {
//            if (!(quickFix instanceof SendFeedback)) {
//                String name = quickFix.getFamilyName();
//                quickfixes.add(name);
//            }
//
//        }
//        String hashedLibName = secureHash(libName);
//        String pluginId = PluginHelper.getPluginId();
//        LibStats libStats = new LibStats(pluginId, hashedProjectName, hashedLibName, quickfixes);
//        RemoteTelemetry.sendLibStats(libStats);
//
//    }

    public static String secureHash(String input) {
        String hashed = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(input.getBytes());
            byte[] digest = messageDigest.digest();
            BigInteger no = new BigInteger(1, digest);

            // Convert message digest into hex value
            hashed = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashed.length() < 32) {
                hashed = "0" + hashed;
            }

        } catch (NoSuchAlgorithmException e) {
            localLog.writeLine(e);
        }

        return hashed;
    }
}

