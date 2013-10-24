/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import us.physion.ovation.DataStoreCoordinator;

/**
 *
 * @author jackie
 */
public class LoginModel {
        String email;
        String password;
        boolean cancelled = true;
        DataStoreCoordinator dsc;

        void setDSC(DataStoreCoordinator dsc)
        {
            this.dsc = dsc;
        }
        
        DataStoreCoordinator getDSC()
        {
            return dsc;
        }
        
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
