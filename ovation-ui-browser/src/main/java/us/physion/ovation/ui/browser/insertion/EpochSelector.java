/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.explorer.view.BeanTreeView;

/**
 *
 * @author jackie
 */
public class EpochSelector extends JPanel{

    private JButton refreshButton;
    private JButton queryButton;
    private BeanTreeView browserTree;
    void initComponents()
    {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
    }
}
