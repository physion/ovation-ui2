package us.physion.ovation.ui.jumpto.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import us.physion.ovation.ui.editor.OpenNodeInBrowserAction;
import us.physion.ovation.ui.jumpto.api.JumpHistory;

public abstract class AbstractJumpAction extends AbstractAction implements Presenter.Toolbar {

    private PropertyChangeListener listener;
    private JButton button;

    protected abstract String getIconResource();

    protected abstract String getTooltip();

    protected abstract JumpHistory.Item getItem();

    protected abstract boolean hasItem();

    public AbstractJumpAction() {
        JumpHistory h = getJumpHistory();

        if (h != null) {
            listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (button != null) {
                        button.setEnabled(hasItem());
                    }
                }
            };
            h.addPropertyChangeListener(WeakListeners.propertyChange(listener, h));
        }
    }

    protected JumpHistory getJumpHistory() {
        return Lookup.getDefault().lookup(JumpHistory.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JumpHistory h = getJumpHistory();

        if (h != null) {
            JumpHistory.Item item = getItem();
            if (item != null) {
                new OpenNodeInBrowserAction(item.getProjectNavigatorTreePath(), item.getDisplayName()).actionPerformed(null);
            }
        }
    }

    @Override
    public Component getToolbarPresenter() {
        button = new JButton(ImageUtilities.loadImageIcon(getIconResource(), true));

        button.setToolTipText(getTooltip());
        button.addActionListener(this);
        button.setEnabled(hasItem());
        //XXX: add popup

        return button;
    }
}
