package us.physion.ovation.ui.database;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Future;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.api.Ovation;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author jackie
 */
@Messages({
    "Login_Window_Title=Ovation",
    "Login_Window_Remember_Me=Remember me",
    "Login_Window_Email=Email: ",
    "Login_Window_Password=Password: ",
    "Login_Window_Invalid_Password=Invalid password",
    "Login_Window_Login_Button=Login",
    "Login_Window_Authenticating_Progress=Authenticating..."
})
public class LoginWindow {
    private final static Color SPINNER_BACKGROUND = Color.WHITE;
    private final static Color ERROR_BACKGROUND = Color.YELLOW;
    
    LoginModel model;
    private CardLayout loginLayout;
    private JPanel loginWithSpinnerPanel;
    private JTextField emailTB;
    private JTextField passwordTB;
    private JPanel errorPanel;
    JDialog dialog;
    JLabel errorMsg;
    JLabel spinner;
    
    public LoginModel showLoginDialog() {

        if (dialog == null)
            initWindow();
        
        dialog.setVisible(true);
        
        return model;
    }

    private void authenticateInBackgroundThread(final LoginModel m, final Dialog d) {
        //start timer
        
        spinner.setVisible(true);
        loginLayout.last(loginWithSpinnerPanel);
        emailTB.setEditable(false);
        passwordTB.setEditable(false);
        
        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Login_Window_Authenticating_Progress());
                
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //spinner.setVisible(true);
                DataStoreCoordinator dsc = Ovation.newDataStoreCoordinator();
                try {
                    boolean success = dsc.authenticateUser(m.getEmail(), m.getPassword().toCharArray(), m.rememberMe()).get();
                    if (success) {
                        m.cancelled = false;
                        m.setDSC(dsc);
                        EventQueueUtilities.runOnEDT(new Runnable() {
                            @Override
                            public void run() {
                                ph.start();
                            }
                        });
                        d.dispose();
                    }else{
                        displayError(Bundle.Login_Window_Invalid_Password());
                    }
                } catch (Exception ex) {
                    displayError(ex.getLocalizedMessage());
                } finally{
                    loginLayout.first(loginWithSpinnerPanel);
                    emailTB.setEditable(true);
                    passwordTB.setEditable(true);
                }
            }
        };
        Future f = EventQueueUtilities.runOffEDT(r);
        if (f == null)
        {
            EventQueueUtilities.runOnEDT(new Runnable() {

                        @Override
                        public void run() {
                            ph.finish();
                        }
                    });
        }else if (f instanceof ListenableFuture){
            Futures.addCallback((ListenableFuture)f, new FutureCallback() {
                @Override
                public void onSuccess(Object result) {
                    EventQueueUtilities.runOnEDT(new Runnable() {

                        @Override
                        public void run() {
                            ph.finish();
                        }
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    EventQueueUtilities.runOnEDT(new Runnable() {

                        @Override
                        public void run() {
                            ph.finish();
                        }
                    });
                }
            });
        }
    }
    
    private void displayError(final String error)
    {
        EventQueueUtilities.runOnEDT(new Runnable() {

            @Override
            public void run() {
                boolean visible;
                if (error == null) {
                    errorMsg.setText(""); //NOI18N
                    visible = false;
                } else {
                    errorMsg.setText(error);
                    visible = true;
                }
                
                if(errorPanel.isVisible() != visible){
                    errorPanel.setVisible(visible);
                    dialog.pack();
                }
            }
        });
    }
    
    private void initWindow()
    {
        model = new LoginModel();
        dialog = new JDialog(new JFrame(), Bundle.Login_Window_Title(), true);
        spinner = new JLabel(new ImageIcon(LoginWindow.class.getResource("ajax-loader.gif"))); //NOI18N
        spinner.setVisible(false);
        
        errorMsg = new JLabel();
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel login = new JPanel();
        login.setBackground(SPINNER_BACKGROUND);
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.setBorder(new EmptyBorder(15, 15, 15, 15));
        login.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 15, 15);
        
        //two text fields
        emailTB = addField(login, Bundle.Login_Window_Email(), 1, false);
        passwordTB = addField(login, Bundle.Login_Window_Password(), 2, true);

        //Cancel/Ok buttons
        //JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton(Bundle.Login_Window_Login_Button());
        //okButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(15, 0, 0, 0);
        
        loginWithSpinnerPanel = new JPanel(loginLayout = new CardLayout());
        loginWithSpinnerPanel.setBackground(SPINNER_BACKGROUND);
        loginWithSpinnerPanel.add(okButton, "login"); //NOI18N
        loginWithSpinnerPanel.add(spinner, "spinner"); //NOI18N
        
        login.add(loginWithSpinnerPanel, c);

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                model.setEmail(emailTB.getText());
                model.setPassword(passwordTB.getText());
                displayError(null);
                authenticateInBackgroundThread(model, dialog);
            }
            
        });
        
        c.gridx = 0;
        c.gridwidth = 1;
        final JCheckBox cb = new JCheckBox();
        final Preferences prefs = NbPreferences.forModule(LoginWindow.class);
        boolean save = prefs.getBoolean(SAVE_LOGIN_FOR_OFFLINE, false);
        cb.setSelected(save);
        cb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                boolean checked = ((JCheckBox)(ae.getSource())).isSelected();
                model.setRememberMe(checked);
                prefs.putBoolean(SAVE_LOGIN_FOR_OFFLINE, checked);
            }
        });
        
        prefs.addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if(evt.getKey().equals(SAVE_LOGIN_FOR_OFFLINE)) {
                    boolean b = Boolean.valueOf(evt.getNewValue());
                    cb.setSelected(b);
                    model.setRememberMe(b);
                }
            }
        });
        
        login.add(cb, c);
        c.gridwidth = 1;
        c.gridx = 1;
        JLabel rememberMe = new JLabel(Bundle.Login_Window_Remember_Me());
        login.add(rememberMe, c);
        
        
        errorPanel = new JPanel(new BorderLayout()) {
            @Override
            //not resizing the UI if the error text is too long
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                Dimension ldim = login.getPreferredSize();
                if (d.width > ldim.width) {
                    return new Dimension(ldim.width, d.height);
                } else {
                    return d;
                }
            }
        };
        errorPanel.setBackground(ERROR_BACKGROUND);
        errorMsg.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        errorPanel.add(errorMsg, BorderLayout.CENTER);
        errorPanel.setVisible(false);
        
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBackground(SPINNER_BACKGROUND);
        mainPane.add(login, BorderLayout.CENTER);
        mainPane.add(errorPanel, BorderLayout.NORTH);
        
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.getContentPane().add(mainPane);
        login.getRootPane().setDefaultButton(okButton);
        //show dialog
        dialog.pack();
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocationRelativeTo(null);
    }
    private static final String SAVE_LOGIN_FOR_OFFLINE = "save-login-for-offline";
    
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
