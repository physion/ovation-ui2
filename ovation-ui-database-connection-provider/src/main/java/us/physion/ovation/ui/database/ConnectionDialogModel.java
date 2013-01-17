/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author barry
 */
public class ConnectionDialogModel {

    public boolean isComplete() {
        return (getConnection() != null &&
                !getConnection().isEmpty() &&
                getUsername() != null &&
                !getUsername().isEmpty() &&
                getPassword() != null &&
                !getPassword().isEmpty());
    }

    /**
     * @return the connection
     */
    public String getConnection() {
        return connection;
    }

    /**
     * @param connection the connection to set
     */
    public void setConnection(String connection) {
        boolean ready = isComplete();
        String oldValue = getConnection();

        this.connection = connection;

        support.firePropertyChange("connection", oldValue, connection);

        if (!ready && isComplete()) {
            support.firePropertyChange("complete", ready, isComplete());
        }
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        boolean ready = isComplete();

        String oldValue = getUsername();
        this.username = username;
        support.firePropertyChange("username", oldValue, username);
        if (!ready && isComplete()) {
            support.firePropertyChange("complete", ready, isComplete());
        }
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        boolean ready = isComplete();

        String oldValue = getPassword();
        this.password = password;
        support.firePropertyChange("password", oldValue, password);
        if (!ready && isComplete()) {
            support.firePropertyChange("complete", ready, isComplete());
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private String connection;
    private String username;
    private String password;
}
