/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
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
        displayName = "#CTL_SearchAction")
@ActionReference(path = "Menu/File", position = 1300)
@Messages("CTL_SearchAction=Search")
public final class SearchAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        /*JFrame f = new JFrame();
        f.add(new JLabel("Search by a tag"));
        JComboBox box = new JComboBox();
        box.setEditable(true);
        f.add(box);
        f.pack();
        f.setVisible(true);*/
        
        EventQueueUtilities.runOffEDT(new Runnable() {
            @Override
            public void run() {
                DataContext context = Lookup.getDefault().lookup(ConnectionProvider.class).getNewContext();
                QuerySet querySet = new QuerySet();
                for (Taggable entity: context.getObjectsWithTag("tag"))
                {
                    querySet.add((OvationEntity)entity);
                }
            }
        });
    }
    
    
}
