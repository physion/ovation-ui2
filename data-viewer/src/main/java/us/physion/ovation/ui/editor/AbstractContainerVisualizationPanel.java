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
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.ui.interfaces.EntityColors;
import us.physion.ovation.ui.interfaces.IEntityNode;

/**
 *
 * @author barry
 */
abstract class AbstractContainerVisualizationPanel extends javax.swing.JLayeredPane {

    public static final String PROP_PROTOCOLS = "protocols";
    public static final String PROP_PROTOCOL = "protocol";

    final IEntityNode node;

    private final DataContext context;
    //private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public AbstractContainerVisualizationPanel(IEntityNode entityNode) {
        node = entityNode;
        context = entityNode.getEntity().getDataContext();

        this.setOpaque(true);
    }

    protected void setEntityBorder(JComponent panel) {
        panel.setBorder(new LineBorder(EntityColors.getEntityColor(getNode().getEntity().getClass()), 2, true));
    }

    Logger logger = LoggerFactory.getLogger(AbstractContainerVisualizationPanel.class);

    public List<Protocol> getProtocols() {
        List<Protocol> result = Lists.newLinkedList(context.getProtocols());
//        result.sort(new Comparator<Protocol>() {
//
//            @Override
//            public int compare(Protocol o1, Protocol o2) {
//                if (o1 == null || o2 == null) {
//                    return 0;
//                }
//
//                return o1.getName().compareTo(o2.getName());
//            }
//        });

        return result;
    }

    protected DataContext getContext() {
        return context;
    }

    protected IEntityNode getNode() {
        return node;
    }

//    @Override
//    public void addPropertyChangeListener(PropertyChangeListener listener) {
//        getPropertyChangeSupport().addPropertyChangeListener(listener);
//    }
//
//    @Override
//    public void removePropertyChangeListener(PropertyChangeListener listener) {
//        getPropertyChangeSupport().removePropertyChangeListener(listener);
//    }
//
//    protected PropertyChangeSupport getPropertyChangeSupport() {
//        return propertyChangeSupport;
//    }
    
    protected Protocol addProtocol()
    {
        Protocol p = getContext().insertProtocol(Bundle.CTL_NewProtocolName(), "");
        firePropertyChange("protocols", null, p);
        return p;
    }

    static class ProtocolCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Protocol) {
                Protocol p = (Protocol) value;
                setText(p.getName()); //TODO getVersion()
            }
            return this;
        }
    }

    public List<String> getAvailableZoneIDs() {
        return Lists.newArrayList(DatePickers.getTimeZoneIDs());
    }

    public static class WindowMoveAdapter extends MouseAdapter {

        private boolean dragging = false;
        private int prevX = -1;
        private int prevY = -1;

        public WindowMoveAdapter() {
            super();
        }

        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                dragging = true;
            }
            prevX = e.getXOnScreen();
            prevY = e.getYOnScreen();
        }

        public void mouseDragged(MouseEvent e) {
            if (prevX != -1 && prevY != -1 && dragging) {
                Window w = SwingUtilities.getWindowAncestor(e.getComponent());
                if (w != null && w.isShowing()) {
                    Rectangle rect = w.getBounds();
                    w.setBounds(rect.x + (e.getXOnScreen() - prevX),
                            rect.y + (e.getYOnScreen() - prevY), rect.width, rect.height);
                }
            }
            prevX = e.getXOnScreen();
            prevY = e.getYOnScreen();
        }

        public void mouseReleased(MouseEvent e) {
            dragging = false;
        }
    }
}
