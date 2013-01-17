/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.database;

import java.util.List;

/**
 *
 * @author huecotanks
 */
public interface ConnectionHistoryProvider {
    public List<String> getConnectionHistory();
    public void addConnectionFile(String element);
}
