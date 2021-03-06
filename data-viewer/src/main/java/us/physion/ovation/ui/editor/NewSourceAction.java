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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.slf4j.LoggerFactory;
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
public final class NewSourceAction extends AbstractNewEntityAction<Source> {

    @Override
    public void actionPerformed(final ActionEvent e) {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();

        final Source s = ctx.insertSource(Bundle.CTL_NewSourceLabel(), "");

        ListenableFuture<Void> reset = BrowserUtilities.reloadView(BrowserUtilities.SOURCE_BROWSER_ID);
        Futures.addCallback(reset, new FutureCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                selectNode(s, BrowserUtilities.SOURCE_BROWSER_ID);
            }

            @Override
            public void onFailure(Throwable t) {
                LoggerFactory.getLogger(NewProjectAction.class).error("Unable to reset view", t);
            }
        });
    }
}
