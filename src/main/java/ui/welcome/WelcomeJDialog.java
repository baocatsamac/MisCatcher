package ui.welcome;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.WindowManager;
import constants.Server;
import helper.ImportHelper;
import helper.LogHelper;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import service.Holder;
import ui.telemetry.FeedbackJDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class WelcomeJDialog extends JDialog{
    private final String message = "By installing UIMis you agree to our data privacy policy, \nplease click on Data Privacy button to get more details. \n\nTo see how UIMis works in an exemplary project, please click \non Exemplary Project button.";
    private static final String TITLE = "Welcome to UIMis";
    public WelcomeJDialog(JFrame frame){
        super(frame, TITLE);

//        frame.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(message);
        panel.add(label);

        JLabel category_selection_label = new JLabel("Please choose your app project category");
        panel.add(category_selection_label);


        // array of string contating cities
        String s1[] = { "Weather", "Restaurant", "Pictures", "Radio", "File Management", "Email", "Maps", "Musics/Videos", "Social"};

        // create checkbox
        ComboBox c1 = new ComboBox(s1);
        AutoCompleteDecorator.decorate(c1);

        final String[] selectedCategory = {""};
        c1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // if the state combobox is changed
                if (e.getSource() == c1) {
                    selectedCategory[0] = (String) c1.getSelectedItem();
                    LogHelper.logInfo(c1.getSelectedItem() + " selected");
                }
            }
        });
        panel.add(c1);

        JLabel app_description_lb = new JLabel("Please describe briefly your app description");
        panel.add(app_description_lb);

        final String hint = "i.e. My app is a Restaurant-related app allowing users to search nearby restaurants, coffee shops and make an order.";
        final JTextField app_description_txt = new JTextField(hint);
        panel.add(app_description_txt);
        app_description_txt.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                app_description_txt.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (app_description_txt.getText().isEmpty()){
                    app_description_txt.setText(hint);
                }

            }
        });

        Button button = new Button("Submit");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LogHelper.logInfo(app_description_txt.getText() + " as app description");
                Holder.getAppProjectData().put(Holder.APP_DESCRIPTION, app_description_txt.getText());
                Holder.getAppProjectData().put(Holder.APP_CATEGORY, selectedCategory[0]);
            }
        });
        panel.add(button);
        getContentPane().add(panel);
//        setSize(new Dimension(100, 100));
//        this.setLocationRelativeTo(frame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);

    }

    private void openDataPrivacy(){
        boolean desktopSupported = Desktop.isDesktopSupported();
        if (desktopSupported) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(Server.HOME_SITE + "telemetry.html"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        new WelcomeJDialog(null);
    }
}
