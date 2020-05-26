package listener;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
//import service.localService.LocalDataService;
//import telemetry.PluginHelper;
//import telemetry.RemoteTelemetry;
//import telemetry.TelemetryHelper;
//import telemetry.entity.QuickFixInfo;

import java.util.Date;
import java.util.Set;

public class QuickFixListener implements IQuickFixListener {



    @Override
    public void applied(PsiElement element, String name) {
        String content = element.getText() + "\t" + name;
        Project project = element.getProject();
        String projectName = project.getName();
//        final LocalDataService service = ServiceManager.getService(LocalDataService.class);
//        Set<String> excludedProjects = service.getExcludedProjects();
//        if (excludedProjects.contains(projectName)) {
//            return;
//        }
//        String pluginId = PluginHelper.getPluginId();
//
//        Date now = new Date();
//        projectName = TelemetryHelper.secureHash(projectName);
//        int type = 0;
//        QuickFixInfo quickFixInfo = new QuickFixInfo(pluginId, content, projectName, now, type);
//        RemoteTelemetry.sendLogToServer(quickFixInfo);
//        int countQuickFix = service.getCountQuickFix();
//        countQuickFix += 1;
//        service.setCountQuickFix(countQuickFix);
//
//        TelemetryHelper.checkToAskSurvey(project);


    }
}
