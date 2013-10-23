package us.physion.ovation.ui.database;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
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

    static Logger logger = LoggerFactory.getLogger(DatabaseConnectionProvider.class);
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

    @Override
    public DataContext getNewContext() {
        return context.getCoordinator().getContext();
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
    private DataContext context = null;
    private Set<ConnectionListener> connectionListeners;
    private boolean waitingForContext = false;

    public DatabaseConnectionProvider() {

        connectionListeners = Collections.synchronizedSet(new HashSet());
    }

    public synchronized void resetConnection()
    {
        context = null;
        getDefaultContext();
    }

    @Override
    public DataContext getDefaultContext() {

        synchronized (this) {
            if (waitingForContext || context != null) {
                return context;
            }
            setWaitingFlag(true);
        }

        final ConnectionListener[] listeners = connectionListeners.toArray(new ConnectionListener[0]);

        final Runnable r = new Runnable() {

            public void run() {
                ProgressHandle ph = ProgressHandleFactory.createHandle("Authenticating...");
                try {
                    ph.start();
                    DataStoreCoordinator toAuthenticate = Ovation.newDataStoreCoordinator();
                    boolean succeeded = authenticateUser(toAuthenticate, null);
                    if (succeeded) {
                        setContext(toAuthenticate.getContext());
                        setWaitingFlag(false);

                        for (ConnectionListener l : listeners) {
                            l.propertyChange(new PropertyChangeEvent(context, "ovation.connectionChanged", 0, 1));
                        }
                    }
                } finally {
                    setWaitingFlag(false);
                    ph.finish();
                }
            }
        };

        EventQueueUtilities.runOnEDT(r);

        return context;
    }

    private LoginModel showLoginDialog(String error) {

        final LoginModel model = new LoginModel();

        final JDialog d = new JDialog(new JFrame(), "Ovation", true);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel login = new JPanel();
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.setBorder(new EmptyBorder(15, 15, 15, 15));
        login.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        ImageIcon physionIcon = ImageUtilities.loadImageIcon("/org/netbeans/core/startup/frame48.gif", true);
            JLabel image = new JLabel(physionIcon);
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 15, 15);
            login.add(image, c);
            image.setAlignmentX(Component.LEFT_ALIGNMENT);

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
                model.cancelled = false;
                d.dispose();
            }
        });

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
        d.getContentPane().setBackground(Color.WHITE);
        d.getContentPane().add(login);
        login.getRootPane().setDefaultButton(okButton);
        //show dialog
        d.pack();
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        return model;
    }
    private boolean authenticateUser(DataStoreCoordinator dsc, String error) {
        LoginModel m = showLoginDialog(error);
        if (!m.isCancelled()) {
            try {
                return dsc.authenticateUser(m.getEmail(), m.getPassword().toCharArray()).get();
            } catch (AuthenticationException e) {
                return authenticateUser(dsc, e.getLocalizedMessage());
            } catch (InterruptedException e) {
                return authenticateUser(dsc, e.getLocalizedMessage());
            } catch (ExecutionException e) {
                return authenticateUser(dsc, e.getLocalizedMessage());
            }
            //TODO: add other common errors here
        }
        return false;
    }

    private synchronized void setContext(DataContext context) {
        this.context = context;
    }

    private synchronized void setWaitingFlag(boolean b) {
        waitingForContext = b;
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
