/*
 * Copyright (C) 2014 Physion LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package us.physion.ovation.ui.editor;

import com.google.common.collect.Lists;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.browser.BrowserUtilities;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

@ActionID(
        category = "Edit",
        id = "us.physion.ovation.ui.browser.insertion.NewSourceAction"
)
@ActionRegistration(
        displayName = "#CTL_NewSourceAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File/New", position = 1350, separatorAfter = 1375),
    @ActionReference(path = "Shortcuts", name = "DS-S")
})
@Messages({"CTL_NewSourceAction=Source...",
    "CTL_NewSourceLabel=New Source"})
public final class NewSourceAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();

        Source s = ctx.insertSource(Bundle.CTL_NewSourceLabel(), "");

        BrowserUtilities.resetView();
        new OpenNodeInBrowserAction(Lists.<URI>newArrayList(s.getURI()),
                s.getLabel(),
                false,
                Lists.<URI>newArrayList(),
                "SourceBrowserTopComponent").actionPerformed(e);

        //BrowserUtilities.switchToSourceView();
    }
}
