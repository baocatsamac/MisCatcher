package ui.welcome;

import constants.Server;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class DialogTest {
    private final String message = "By installing Up2Dep you agree to our data privacy policy, \nplease click on Data Privacy button to get more details. \n\nTo see how Up2Dep works in an exemplary project, please click \non Exemplary Project button.";
    private final String TITLE = "Welcome to Up2Dep";
    public DialogTest(Component parent){
        Object[] options = new Object[]{"Close", "Exemplary Project", "Data Privacy"};
        int option = JOptionPane.showOptionDialog(parent,message, TITLE, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null, options, options[1]);
        switch (option){
            case 0:
                //close
                break;
            case 1:
                //set up project
//                ImportHelper.setupSampleProject();
                break;
            case 2:
                //open website
//                openDataPrivacy();
                break;
        }

    }

}
