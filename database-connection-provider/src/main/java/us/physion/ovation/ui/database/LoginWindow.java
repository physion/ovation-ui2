/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.api.Ovation;
import static us.physion.ovation.ui.database.DatabaseConnectionProvider.logger;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author jackie
 */
public class LoginWindow {
    LoginModel model;
    JDialog dialog;
    JLabel errorMsg;
    OVTimer spinner;
    
    public LoginModel showLoginDialog() {

        if (dialog == null)
            initWindow();
        
        dialog.setVisible(true);
        
        return model;
    }

    private void authenticateInBackgroundThread(final LoginModel m, final Dialog d) {
        //start timer
        
        spinner.setVisible(true);
        spinner.start();

        Runnable r = new Runnable() {
            public void run() {
                DataStoreCoordinator dsc = Ovation.newDataStoreCoordinator();
                try {
                    boolean success = dsc.authenticateUser(m.getEmail(), m.getPassword().toCharArray()).get();
                    if (success) {
                        m.cancelled = false;
                        m.setDSC(dsc);
                        d.dispose();
                    }
                } catch (Exception ex) {
                    displayError(ex);
                    spinner.stop();
                    return;
                }
            }
        };
        EventQueueUtilities.runOffEDT(r);
    }
    
    private void displayError(final Exception e)
    {
        EventQueueUtilities.runOnEDT(new Runnable() {

            @Override
            public void run() {
                if (e == null)
                    errorMsg.setText("");
                else
                    errorMsg.setText(e.getLocalizedMessage());
            }
        });
    }
    
    private void initWindow()
    {
        model = new LoginModel();
        dialog = new JDialog(new JFrame(), "Ovation", true);
        spinner = new OVTimer();
        errorMsg = new JLabel();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel login = new JPanel();
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.setBorder(new EmptyBorder(15, 15, 15, 15));
        login.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 15, 15);
            login.add(spinner, c);
        
        /*BufferedImage physionIcon;
        File f = null;
        try {
            f = new File ("installer/ovation_48x48.png");
            physionIcon = ImageIO.read(f);
            JLabel image = new JLabel(new ImageIcon( physionIcon ));
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 15, 15);
            login.add(image, c);
            image.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        catch (IOException ex) {
            String s = "";
            if (f != null)
                s = " at '" + f.getAbsolutePath() + "'";
            logger.error("Could not find Physion icon" + s);
        }*/

        //tabs.addTab("Login", login);

        //JPanel signUp = new JPanel();
        //tabs.addTab("Sign up", signUp);

        //LOGIN
        //------------------------------------------------------
        //TODO: header if the error is not null

        //two text fields
        final JTextField emailTB = addField(login, "Email: ", 1, false);
        final JTextField passwordTB = addField(login, "Password: ", 2, true);

        //Cancel/Ok buttons
        //JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("Login");
        //okButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(15, 0, 0, 0);
        login.add(okButton, c);

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                model.setEmail(emailTB.getText());
                model.setPassword(passwordTB.getText());
                authenticateInBackgroundThread(model, dialog);
            }
            
        });
        
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 3;
        errorMsg.setForeground(Color.RED);
        login.add(errorMsg, c);

        //SIGN UP
        //-----------------------------------------------------------
       /* JLabel header = new JLabel("New to Ovation? Sign up");

        //two text fields
        JPanel s_form = new JPanel(new GridBagLayout());
        final JTextField nameTB = addField(s_form, "Name: ", 0, false);
        final JTextField s_emailTB = addField(s_form, "Email: ", 1, false);
        final JTextField s_passwordTB = addField(s_form, "Password: ", 2, true);

        JPanel s_buttonPane = new JPanel();
        JButton signUpButton = new JButton("Sign Up");
        s_buttonPane.setLayout(new BoxLayout(s_buttonPane, BoxLayout.LINE_AXIS));
        s_buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        s_buttonPane.add(signUpButton);

        signUpButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                //sign up through the website
                //when that's completed,
                model.setEmail(s_emailTB.getText());
                model.setPassword(s_passwordTB.getText());
                model.cancelled = false;
                d.dispose();
            }
        });
        signUp.add(header, BorderLayout.PAGE_START);
        signUp.add(s_form, BorderLayout.CENTER);
        signUp.add(s_buttonPane, BorderLayout.PAGE_END);

        d.getContentPane().add(tabs);
        login.getRootPane().setDefaultButton(okButton);
        */
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.getContentPane().add(login);
        login.getRootPane().setDefaultButton(okButton);
        //show dialog
        dialog.pack();
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocationRelativeTo(null);
    }
    
    private JTextField addField(JPanel form, String name, int row, boolean passwordField) {
        JTextField f;
        if (passwordField)
        {
            f = new JPasswordField();
        }else{
            f = new JTextField();
        }
        JLabel l = new JLabel(name);
        f.setPreferredSize(new Dimension(250, 25));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridy = row;
        form.add(l, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        form.add(f, c);

        return f;
    }
}
