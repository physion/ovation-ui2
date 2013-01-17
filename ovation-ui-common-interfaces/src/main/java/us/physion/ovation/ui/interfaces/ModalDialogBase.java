/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import javax.swing.JDialog;
import javax.swing.JFrame;
/**
 *
 * @author jackie
 */
public class ModalDialogBase extends JDialog {

    public ModalDialogBase() {
        super(new JFrame(), true);
    }

    public void showDialog() {
        try {
            EventQueueUtilities.runAndWaitOnEDT(new Runnable() {

                @Override
                public void run() {
                    setLocationRelativeTo(null);
                    pack();
                    setVisible(true);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    public void disposeOnEDT() {
        EventQueueUtilities.runOnEDT(new Runnable() {

            @Override
            public void run() {
                ModalDialogBase.this.dispose();
            }
        });
    }
}
