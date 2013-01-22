/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Component;

/**
 *
 * @author huecotanks
 */
public class ResponsePanel {
    Component panel;
    ResponsePanel(Component p)
    {
        panel = p;
    }
    
    public Component getPanel()
    {
        return panel;
    }
}
