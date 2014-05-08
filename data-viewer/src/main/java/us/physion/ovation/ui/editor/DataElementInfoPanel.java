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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyleConstants;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;

/**
 *
 * @author barry
 */
public class DataElementInfoPanel extends javax.swing.JPanel {

    private static final String ELEM = AbstractDocument.ElementNameAttribute;
    private static final String ICON = StyleConstants.IconElementName;
    private static final String COMP = StyleConstants.ComponentElementName;

    private final Set<? extends OvationEntity> elements;

    /**
     * Creates new form DataElementInfoPanel
     */
    public DataElementInfoPanel(Iterable<? extends OvationEntity> elements) {
        this.elements = ImmutableSet.copyOf(elements);

        initComponents();

        final DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();

        addSourcesTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Iterable<String> sourceIds = Splitter.on(",")
                        .omitEmptyStrings()
                        .trimResults()
                        .split(addSourcesTextField.getText());

                for (String sourceId : sourceIds) {
                    Set<Source> sources = Sets.newHashSet(ctx.getSourcesWithIdentifier(sourceId));

                    for (Measurement m : getMeasurements()) {
                        Set<String> sourceNames = Sets.newHashSet(m.getSourceNames());

                        for (Source s : sources) {
                            Epoch epoch = m.getEpoch();
                            String epochId = s.getURI().toString();
                            if (!epoch.getInputSources().containsValue(s)) {
                                epoch.addInputSource(epochId, s);
                            }

                            sourceNames.add(epochId);
                        }

                        m.setSourceNames(sourceNames);
                    }
                }

                addSourcesTextField.setText("");

                updateSources();
            }
        });

        updateSources();
    }

    public Set<? extends OvationEntity> getElements() {
        return elements;
    }

    <T extends OvationEntity> Iterable<T> getEntities(Class<T> cls) {
        return Iterables.transform(Iterables.filter(getElements(), new Predicate<OvationEntity>() {
            @Override
            public boolean apply(OvationEntity t) {
                if (t == null) {
                    return false;
                }

                return Measurement.class.isAssignableFrom(t.getClass());
            }
        }), new Function<OvationEntity, T>() {

            @Override
            public T apply(OvationEntity f) {
                return (T) f;
            }
        });
    }

    Set<Measurement> getMeasurements() {
        return ImmutableSet.copyOf(getEntities(Measurement.class));
    }

    Set<Resource> getResources() {
        return ImmutableSet.copyOf(getEntities(Resource.class));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        addSourcesTextField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourcesTextPane = new javax.swing.JTextPane();

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.jLabel1.text")); // NOI18N

        addSourcesTextField.setText(org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.addSourcesTextField.text")); // NOI18N
        addSourcesTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.addSourcesTextField.toolTipText")); // NOI18N
        addSourcesTextField.setBorder(new javax.swing.border.LineBorder(javax.swing.UIManager.getDefaults().getColor("InternalFrame.background"), 1, true));

        jScrollPane1.setBorder(new javax.swing.border.LineBorder(javax.swing.UIManager.getDefaults().getColor("InternalFrame.inactiveTitleBackground"), 1, true));
        jScrollPane1.setViewportView(sourcesTextPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addSourcesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(addSourcesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addSourcesTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane sourcesTextPane;
    // End of variables declaration//GEN-END:variables

    Logger logger = LoggerFactory.getLogger(DataElementInfoPanel.class);

    private final Map<JComponent, String> components = Maps.newHashMap();

    private void updateSources() {
        final Multimap<String, Source> sources = HashMultimap.create();

        for (Measurement m : getMeasurements()) {
            for (String s : m.getSourceNames()) {
                sources.put(s, m.getEpoch().getInputSources().get(s));
            }
        }

        EventQueueUtilities.runOnEDT(new Runnable() {

            @Override
            public void run() {
                sourcesTextPane.setText("");

                for (final Source s : sources.values()) {
                    JPanel sourcePanel = makeSourcePanel(s);

                    sourcesTextPane.setCaretPosition(sourcesTextPane.getDocument().getLength());
                    sourcesTextPane.insertComponent(sourcePanel);

                }
            }
        });

    }

    private JPanel makeSourcePanel(final Source s) {

        JPanel sourcePanel = new JPanel();
        sourcePanel.setBackground(getBackground());
        sourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));

        JButton sourceButton = new JButton(s.getLabel() + " (" + s.getIdentifier() + ")");

        sourceButton.setOpaque(true);
        sourceButton.setBackground(Color.white);
        sourceButton.setForeground(new Color(161, 37, 127)); //TODO Source colors?
        sourceButton.setBorder(null);
        final List<String> uriStrings = Lists.newArrayList();
        final List<URI> elementUris = Lists.newArrayList();
        for (OvationEntity e : getElements()) {
            elementUris.add(e.getURI());
            uriStrings.add(e.getURI().toString());
        }
        sourceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        new OpenNodeInBrowserAction(Lists.newArrayList(s.getURI()),
                                Joiner.on(",").join(uriStrings),
                                false,
                                elementUris,
                                OpenNodeInBrowserAction.SOURCE_BROWSER_ID).actionPerformed(e);
                    }
                });

            }
        });

        sourcePanel.add(sourceButton);
        JButton removeButton = new JButton("x");
        removeButton.setPreferredSize(new Dimension(15, 15));
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Measurement m : getMeasurements()) {
                    Set<String> sourceNames = ImmutableSet.copyOf(m.getSourceNames());
                    Set<String> modifiedNames = Sets.newHashSet(sourceNames);

                    for (String n : sourceNames) {
                        if (m.getEpoch().getInputSources().get(n).equals(s)) {
                            modifiedNames.remove(n);
                        }
                    }

                    m.setSourceNames(modifiedNames);
                }

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        updateSources();
                    }

                });
            }
        });
        sourcePanel.add(removeButton);
        sourcePanel.setBorder(new RoundedCornerBorder());
        sourcePanel.revalidate();
        sourcePanel.setMaximumSize(sourcePanel.getPreferredSize());

        sourcePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return sourcePanel;
    }

    class RoundedCornerBorder extends AbstractBorder {

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int r = height - 1;
            RoundRectangle2D round = new RoundRectangle2D.Float(x, y, width - 1, height - 1, r, r);
            Container parent = c.getParent();
            if (parent != null) {
                g2.setColor(parent.getBackground());
                Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
                corner.subtract(new Area(round));
                g2.fill(corner);
            }
            g2.setColor(Color.GRAY);
            g2.draw(round);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 8;
            insets.top = insets.bottom = 4;
            return insets;
        }
    }
}
