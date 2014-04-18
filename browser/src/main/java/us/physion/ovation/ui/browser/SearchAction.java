/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.exceptions.OvationException;
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
        submit.setMnemonic(KeyEvent.VK_ENTER);
        f.getRootPane().setDefaultButton(submit);
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
                        try {
                            for (OvationEntity entity : context.query(OvationEntity.class, text.getText()).get()) {
                                querySet.add(entity);
                            }
                        } catch (InterruptedException e1) {
                            //pass
                        } catch (ExecutionException e1) {
                            throw new OvationException("Unable to run query", e1);
                        } finally {
                            f.dispose();
                        }
                    }
                });
            }
        });

        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);

    }
}
