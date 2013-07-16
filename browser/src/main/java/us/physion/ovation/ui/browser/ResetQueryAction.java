/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.nodes.Children;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import us.physion.ovation.ui.interfaces.ResetBrowser;

@ActionID(category = "Edit",
id = "us.physion.ovation.ui.browser.ResetQueryAction")
@ActionRegistration(iconBase = "us/physion/ovation/ui/browser/reset-query.png",
displayName = "#CTL_ResetQueryAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 1200),
    //@ActionReference(path = "Toolbars/Find", position = 150),
    @ActionReference(path = "Shortcuts", name = "DS-R")
})
@Messages("CTL_ResetQueryAction=Reset Query")
public final class ResetQueryAction implements ResetBrowser{

    public void actionPerformed(ActionEvent e) {
        BrowserUtilities.resetView();
    }
}
