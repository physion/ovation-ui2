/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Future;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.mixin.Taggable;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

@ActionID(
        category = "Edit",
        id = "us.physion.ovation.ui.browser.SearchAction")
@ActionRegistration(
        //iconBase = "us/physion/ovation/ui/browser/reset-query.png",
        displayName = "#CTL_SearchAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 1100),
    @ActionReference(path = "Shortcuts", name = "DS-S")
})
@Messages("CTL_SearchAction=Search")
public final class SearchAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        final JTextField text = new JTextField();
        text.setEditable(true);
        text.setPreferredSize(new Dimension(200, 20));
        f.add(text, c);
        JButton submit = new JButton("Search");
        submit.setEnabled(true);
        c.gridx = 1;
        f.add(submit, c);
        submit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Future future = EventQueueUtilities.runOffEDT(new Runnable() {
                    @Override
                    public void run() {
                        DataContext context = Lookup.getDefault().lookup(ConnectionProvider.class).getNewContext();
                        QuerySet querySet = new QuerySet();
                        Lookup.getDefault().lookup(QueryProvider.class).setQuerySet(querySet);
                        for (String tag : getTags(text.getText())) {
                            for (Taggable entity : context.getObjectsWithTag(tag)) {
                                querySet.add((OvationEntity) entity);
                            }
                        }
                        f.dispose();
                    }
                });
            }
        });
        
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        
    }

    private String[] getTags(String text) {
        return text.replaceAll("\\s+","").split(",");
    }
}
