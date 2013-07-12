/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import org.openide.actions.CopyAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import us.physion.ovation.ui.interfaces.IEntityWrapper;

/**
 *
 * @author huecotanks
 */
@ActionID(category = "Edit",
id = "us.physion.ovation.ui.browser.BrowserCopyAction")
@ActionRegistration(displayName = "#CTL_BrowserCopyAction")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "M-C")
})
@NbBundle.Messages("CTL_BrowserCopyAction=Copy Entity")
public class BrowserCopyAction implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Lookup.Result global = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class);
        Collection<? extends IEntityWrapper> entities = global.allInstances();
        String selection = "";
        if (entities.size() == 1)
        {
            selection += entities.iterator().next().getURI();
        }
        else{
            for (IEntityWrapper ew : entities)
            {
                selection += ew.getURI() + ", "; 
            }
            selection = selection.substring(0, selection.length() - 2);
        }
       
        StringSelection data = new StringSelection(selection);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(data, data);
    }

}
