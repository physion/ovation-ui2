package us.physion.ovation.ui.jumpto.impl;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.ui.jumpto.api.JumpHistory;
import us.physion.ovation.ui.jumpto.api.JumpHistory.Item;

@ActionID(
    category = "JumpTo",
id = "us.physion.ovation.ui.jumpto.impl.JumpBackAction")
@ActionRegistration(
    iconBase = "org/openide/resources/actions/previousTab.gif",
displayName = "#CTL_JumpBackAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/JumpTo", position = 300),
    @ActionReference(path = "Shortcuts", name = "M-OPEN_BRACKET")
})
@Messages("CTL_JumpBackAction=Jump Back")
public final class JumpBackAction extends AbstractJumpAction {

    @Override
    protected String getIconResource() {
        return "org/openide/resources/actions/previousTab.gif"; //NOI18N
    }

    @Override
    protected Item getItem() {
        JumpHistory h = getJumpHistory();

        if (h != null) {
            return h.goBack();
        }

        return null;
    }

    @Override
    protected boolean hasItem() {
        JumpHistory h = getJumpHistory();

        if (h != null) {
            return h.hasBack();
        }

        return false;
    }

    @Override
    protected String getTooltip() {
        return Bundle.CTL_JumpBackAction();
    }
}
