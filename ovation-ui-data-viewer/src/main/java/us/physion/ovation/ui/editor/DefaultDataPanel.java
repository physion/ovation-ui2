/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author jackie
 */
public class DefaultDataPanel extends JPanel{
    
    DataElement d;
    JLabel elementName;
    JLabel messageLabel;
    JButton openButton;
    
    public DefaultDataPanel(DataElement data) {
        d = data;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        elementName = new JLabel(data.getName());
        messageLabel = new JLabel("Cannot display data of type '" + data.getDataContentType() + "'");
        openButton = new JButton("Open in native application...");
        openButton.setEnabled(true);
        
        openButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                EventQueueUtilities.runOffEDT(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File f = d.getData().get();
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().edit(f);
                            } else {
                                throw new OvationException("Java Desktop not supported on this machine");
                            }
                        } catch (InterruptedException ex) {
                            throw new OvationException(ex);
                        } catch (ExecutionException ex) {
                            throw new OvationException(ex);
                        } catch (IOException ex) {
                            throw new OvationException(ex);
                        }
                    }
                });
            }
        });

        c.insets = new Insets(10, 10, 10, 10);
        c.gridy = 0;
        add(elementName, c);
        c.gridy = 1;
        add(messageLabel, c);
        c.gridy = 2;
        add(openButton, c);
    }
}
