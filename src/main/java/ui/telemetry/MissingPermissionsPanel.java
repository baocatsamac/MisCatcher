package ui.telemetry;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.MultiLineLabelUI;

import javax.swing.*;
import java.util.Set;

public class MissingPermissionsPanel extends JPanel implements Disposable {

    private JLabel myDescriptionLabel;

    public MissingPermissionsPanel(Set<String> missingPermission){
        myDescriptionLabel = new JLabel();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("You should declare the following missing permissions into AndroidManifest.xml to avoid app crash:\n");
        int i = 0;
        for (String permission : missingPermission){
            i++;
            stringBuilder.append("\n" + i + ". " + permission);
        }
        myDescriptionLabel.setText(stringBuilder.toString());
        myDescriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        myDescriptionLabel.setUI(new MultiLineLabelUI());
        add(myDescriptionLabel);
    }

    @Override
    public void dispose() {

    }
}
