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
        id = "us.physion.ovation.ui.jumpto.impl.JumpForwardAction")
@ActionRegistration(
        iconBase = "us/physion/ovation/ui/jumpto/impl/forward.png",
        displayName = "#CTL_JumpForwardAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/JumpTo", position = 310),
    @ActionReference(path = "Shortcuts", name = "M-CLOSE_BRACKET")
})
@Messages("CTL_JumpForwardAction=Jump Forward")
public final class JumpForwardAction extends AbstractJumpAction {

    @Override
    protected String getIconResource() {
        return "us/physion/ovation/ui/jumpto/impl/forward.png"; //NOI18N
    }

    @Override
    protected Item getItem() {
        JumpHistory h = getJumpHistory();

        if (h != null) {
            return h.goForward();
        }

        return null;
    }

    @Override
    protected boolean hasItem() {
        JumpHistory h = getJumpHistory();

        if (h != null) {
            return h.hasForward();
        }

        return false;
    }

    @Override
    protected String getTooltip() {
        return Bundle.CTL_JumpForwardAction();
    }
}
