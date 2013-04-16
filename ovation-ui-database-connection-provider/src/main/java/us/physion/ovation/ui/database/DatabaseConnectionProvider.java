/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.api.Ovation;
import us.physion.ovation.ui.interfaces.ConnectionListener;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.exceptions.AuthenticationException;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author jackie
 */
@ServiceProvider(service = ConnectionProvider.class)
public class DatabaseConnectionProvider implements ConnectionProvider{

    private JTextField addField(JPanel form, String name, int row) {
        JTextField f = new JTextField();
        JLabel l = new JLabel(name);
        f.setPreferredSize(new Dimension(250, 25));
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        form.add(l, c);
        c.gridwidth = 2;
        form.add(f, c);
        
        return f;
    }

    private static class LoginModel {
        String email; 
        String password; 
        boolean cancelled = true;

        void setEmail(String email)
        {
            this.email = email;
        }
        void setPassword(String pw)
        {
            this.password = pw;
        }
        String getPassword()
        {
            return password;
        }
        String getEmail()
        {
            return email;
        }
        boolean isCancelled()
        {
            return cancelled;
        }
    }
    private DataStoreCoordinator dsc = null;
    private Set<ConnectionListener> connectionListeners;
    private boolean waitingForDSC = false;

    public DatabaseConnectionProvider() {
        connectionListeners = Collections.synchronizedSet(new HashSet());
    }

    public synchronized void resetConnection()
    {
        dsc = null;
        getConnection();
    }

    @Override
    public DataStoreCoordinator getConnection() {

        synchronized (this) {
            if (waitingForDSC || dsc != null) {
                return dsc;
            }
            setWaitingFlag(true);
        }

        final ConnectionListener[] listeners = connectionListeners.toArray(new ConnectionListener[0]);

        Runnable r = new Runnable() {

            public void run() {

                try {
                    DataStoreCoordinator toAuthenticate = Ovation.newDataStoreCoordinator();
                    boolean succeeded = authenticateUser(toAuthenticate, null);
                    if (succeeded)
                    {
                        setDsc(toAuthenticate);
                        setWaitingFlag(false);

                        for (ConnectionListener l : listeners)
                        {
                            l.propertyChange(new PropertyChangeEvent(toAuthenticate, "ovation.connectionChanged", 0, 1));
                            //l.fire(); TODO: implement
                        }
                    }
                } finally {
                    setWaitingFlag(false);
                }
            }
        };

        EventQueueUtilities.runOnEDT(r);

        return dsc;
    }
    
    private LoginModel showLoginDialog(String error) {
        
        final LoginModel model = new LoginModel();
        
        final JDialog d = new JDialog(new JFrame(), true);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        JPanel login = new JPanel();
        tabs.addTab("Login", login);
        
        JPanel signUp = new JPanel();
        tabs.addTab("Sign up", signUp);
        
        //LOGIN
        //------------------------------------------------------
        //TODO: header if the error is not null
        
        //two text fields
        JPanel form = new JPanel(new GridBagLayout());
        final JTextField emailTB = addField(form, "Email: ", 0);
        final JTextField passwordTB = addField(form, "Password: ", 1);
        
        //Cancel/Ok buttons
        JPanel buttonPane = new JPanel();
        //JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("Login");
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //buttonPane.add(Box.createHorizontalGlue());
        //buttonPane.add(cancelButton);
        //buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(okButton);
        /*cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                model.cancelled = true;
                d.dispose();
            }
        });
        *
        */
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                model.setEmail(emailTB.getText());
                model.setPassword(passwordTB.getText());
                model.cancelled = false;
                d.dispose();
            }
        });
               
        login.add(form, BorderLayout.CENTER);
        login.add(buttonPane, BorderLayout.PAGE_END);
        
        //SIGN UP
        //-----------------------------------------------------------
        JLabel header = new JLabel("New to Ovation? Sign up");
        
        //two text fields
        JPanel s_form = new JPanel(new GridBagLayout());
        final JTextField nameTB = addField(s_form, "Name: ", 0);
        final JTextField s_emailTB = addField(s_form, "Email: ", 1);
        final JTextField s_passwordTB = addField(s_form, "Password: ", 2);
        
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
        signUp.getRootPane().setDefaultButton(signUpButton);

        //show dialog
        d.pack();
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        //d.setLocation((dim.width - d.getWidth())/2, (dim.height - d.getHeight())/2);
        d.setVisible(true);
        return model;
    }
    private boolean authenticateUser(DataStoreCoordinator dsc, String error) {
        LoginModel m = showLoginDialog(error);
        if (!m.isCancelled()) {
            try {
                dsc.authenticateUser(m.getEmail(), m.getPassword().toCharArray());
                return true;
            } catch (AuthenticationException e) {
                return authenticateUser(dsc, e.getLocalizedMessage());
            } catch (NullPointerException e){
                //pass for now
            }
            //TODO: add other common errors here
        }
        return false;
    }

    private synchronized void setDsc(DataStoreCoordinator the_dsc) {
        dsc = the_dsc;
    }

    private synchronized void setWaitingFlag(boolean b) {
        waitingForDSC = b;
    }

    @Override
    public void addConnectionListener(ConnectionListener cl) {
        connectionListeners.add(cl);
    }

    @Override
    public void removeConnectionListener(ConnectionListener cl) {
        connectionListeners.remove(cl);
    }
    
}
