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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.AnalysisRecord;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.Measurement;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.domain.Source;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.browser.BrowserUtilities;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.EntityColors;
import us.physion.ovation.ui.interfaces.EventQueueUtilities;
import us.physion.ovation.ui.reveal.api.RevealNode;

/**
 *
 * @author barry
 */
@Messages({
    "Adding_source=Adding Source {0}",
    "DataElement_Multiple_Content_Types=<Multiple>"
})
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

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(getAvailableContentTypes().toArray(new String[0]));
        contentTypeComboBox.setModel(model);
        contentTypeComboBox.setSelectedItem(getContentType());

        contentTypeComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selection = (String) e.getItem();
                    setContentType(selection);
                }
            }
        });


        final DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();

        addSourcesTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addSourceFromText(ctx, addSourcesTextField.getText());
            }

        });

        addSourcesTextField.setEnabled(getMeasurements().size() > 0);
        addSourcesTextField.setVisible(getMeasurements().size() > 0);

        List<String> sourceIds = getSourceIds(ctx.getTopLevelSources());
        List<String> sortedIds = Lists.newArrayList(sourceIds);
        Collections.sort(sortedIds);

        AutoCompleteDecorator.decorate(addSourcesTextField, sortedIds, false);


//        addSourcesComboBox.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JComboBox c = (JComboBox) e.getSource();
//                JTextComponent tc = (JTextComponent) c.getEditor().getEditorComponent();
//                addSourceFromText(ctx, tc.getText());
//            }
//        });
//
//        addSourcesComboBox.setModel(new DefaultComboBoxModel<>(sortedIds.toArray(new String[sortedIds.size()])));
//        addSourcesComboBox.setEnabled(getMeasurements().size() > 0);
//        addSourcesComboBox.setVisible(getMeasurements().size() > 0);
//        AutoCompleteDecorator.decorate(addSourcesComboBox);

        updateInputs();
    }

    private List<String> getSourceIds(Iterable<Source> roots) {
        List<String> result = Lists.newLinkedList();
        for (Source s : roots) {
            result.add(s.getIdentifier());
            result.addAll(getSourceIds(s.getChildrenSources()));
        }

        return result;
    }

    public final String getContentType() {
        List<String> contentTypes = Lists.newLinkedList();
        for (DataElement e : getEntities(DataElement.class)) {
            contentTypes.add(e.getDataContentType());
        }

        if (contentTypes.size() == 1) {
            return contentTypes.get(0);
        } else {
            return Bundle.DataElement_Multiple_Content_Types();
        }
    }

    public void setContentType(String contentType) {
        if (Bundle.DataElement_Multiple_Content_Types().equals(contentType)) {
            return;
        }

        for (DataElement e : getEntities(DataElement.class)) {
            if (!e.getDataContentType().equals(contentType)) {
                e.setDataContentType(contentType);
            }
        }
    }

    public Set<? extends OvationEntity> getElements() {
        return elements;
    }

    public final List<String> getAvailableContentTypes() {
        List<String> contentTypes = ContentTypes.getContentTypes();
        contentTypes.add("application/octet-stream");

        if (!contentTypes.contains(getContentType())) {
            contentTypes.add(getContentType());
        }

        Collections.sort(contentTypes);

        return contentTypes;
    }

    <T extends OvationEntity> Iterable<T> getEntities(final Class<T> cls) {
        return Iterables.transform(Iterables.filter(getElements(), new Predicate<OvationEntity>() {
            @Override
            public boolean apply(OvationEntity t) {
                if (t == null) {
                    return false;
                }

                return cls.isAssignableFrom(t.getClass());
            }
        }), new Function<OvationEntity, T>() {

            @Override
            public T apply(OvationEntity f) {
                return (T) f;
            }
        });
    }

    final Set<Measurement> getMeasurements() {
        return ImmutableSet.copyOf(getEntities(Measurement.class));
    }

    final Set<Resource> getResources() {
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
        inputsTextPane = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        contentTypeComboBox = new javax.swing.JComboBox();

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.jLabel1.text")); // NOI18N

        addSourcesTextField.setText(org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.addSourcesTextField.text")); // NOI18N
        addSourcesTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.addSourcesTextField.toolTipText")); // NOI18N
        addSourcesTextField.setBorder(new javax.swing.border.LineBorder(javax.swing.UIManager.getDefaults().getColor("InternalFrame.background"), 1, true));

        jScrollPane1.setBorder(null);

        inputsTextPane.setBorder(null);
        jScrollPane1.setViewportView(inputsTextPane);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DataElementInfoPanel.class, "DataElementInfoPanel.jLabel2.text")); // NOI18N

        contentTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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
                        .addComponent(addSourcesTextField))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contentTypeComboBox, 0, 560, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(contentTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(addSourcesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
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
    private javax.swing.JComboBox contentTypeComboBox;
    private javax.swing.JTextPane inputsTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    Logger logger = LoggerFactory.getLogger(DataElementInfoPanel.class);

    private final Map<JComponent, String> components = Maps.newHashMap();

    private void updateInputs() {
        final Multimap<String, Source> sources = HashMultimap.create();
        final Multimap<String, DataElement> inputResources = HashMultimap.create();

        for (Measurement m : getMeasurements()) {
            for (String s : m.getSourceNames()) {
                sources.put(s, m.getEpoch().getInputSources().get(s));
            }
        }

        for (Resource r : getResources()) {
            if (r.getContainingEntity() instanceof AnalysisRecord) {
                AnalysisRecord record = (AnalysisRecord) r.getContainingEntity();

                for (String s : record.getInputs().keySet()) {
                    inputResources.put(s, ((AnalysisRecord) r.getContainingEntity()).getInputs().get(s));
                }
            }
        }
        EventQueueUtilities.runOnEDT(new Runnable() {

            @Override
            public void run() {
                    inputsTextPane.setText("");

                for (Map.Entry<String, Source> namedSource : sources.entries()) {
                    insertInputsPanel(namedSource.getKey(), namedSource.getValue());

                }

                for (Map.Entry<String, DataElement> namedInput : inputResources.entries()) {
                    insertInputsPanel(namedInput.getKey(), namedInput.getValue());
                }
            }
        });

    }

    private void insertInputsPanel(final String label, final OvationEntity entity) {
        JPanel sourcePanel = makeSourcePanel(label, entity);

        inputsTextPane.setCaretPosition(inputsTextPane.getDocument().getLength());
        inputsTextPane.insertComponent(sourcePanel);
    }

    private JPanel makeSourcePanel(final String label, final OvationEntity entity) {

        JPanel sourcePanel = new JPanel();
        sourcePanel.setBackground(getBackground());
        sourcePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));

        JButton sourceButton = new JButton(label);

        sourceButton.setOpaque(true);
        sourceButton.setBackground(Color.white);
        sourceButton.setForeground(EntityColors.getEntityColor(entity.getClass()));
        sourceButton.setBorder(null);

        sourceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                //TODO find target, then decide where to show it
                if (entity instanceof Source) {
                    RevealNode.forEntity(BrowserUtilities.SOURCE_BROWSER_ID, entity);
                } else {
                    RevealNode.forEntity(BrowserUtilities.PROJECT_BROWSER_ID, entity);
                }
            }
        });

        sourcePanel.add(sourceButton);
        JButton removeButton = new JButton("x");
        removeButton.setPreferredSize(new Dimension(15, 15));
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Measurement m : getMeasurements()) {
                    Set<String> sourceNames = Sets.newHashSet(m.getSourceNames());

                    sourceNames.remove(label);
                    m.setSourceNames(sourceNames);

                    try {
                        m.getEpoch().removeInputSource(label);
                    } catch (IllegalArgumentException ex) {
                        // pass — it's in use by another source
                    }
                }

                for (Resource dataElement : getResources()) {
                    if (e instanceof Measurement) {
                        continue;
                    }

                    ((AnalysisRecord) dataElement.getContainingEntity()).removeInput(label);

                }

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        updateInputs();
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

    private void addSourceFromText(final DataContext ctx, String txt) throws OvationException {

        Iterable<String> sourceIds = Splitter.on(",")
                .omitEmptyStrings()
                .trimResults()
                .split(addSourcesTextField.getText());

        for (final String sourceId : sourceIds) {

            final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Adding_source(sourceId));

            EventQueueUtilities.runOffEDT(new Runnable() {

                @Override
                public void run() {
                    Set<Source> sources = Sets.newHashSet(ctx.getSourcesWithIdentifier(sourceId));
                    if (sources.isEmpty()) {
                        sources = Sets.newHashSet(ctx.insertSource(sourceId, sourceId));
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                BrowserUtilities.resetView(BrowserUtilities.SOURCE_BROWSER_ID);
                            }
                        });
                    }

                    for (Measurement m : getMeasurements()) {
                        Set<String> sourceNames = Sets.newHashSet(m.getSourceNames());
                        Epoch epoch = m.getEpoch();

                        for (Source s : sources) {
                            String epochId = s.getLabel() + " (" + s.getIdentifier() + ")"; //s.getURI().toString();
                            if (!s.equals(epoch.getInputSources().get(epochId))) {
                                epochId = s.getLabel() + " (" + s.getIdentifier() + "; " + s.getURI().toString() + ")";
                            }

                            if (!epoch.getInputSources().containsValue(s)) {
                                ctx.beginTransaction();
                                try {
                                    epoch.addInputSource(epochId, s);
                                    epoch.getDataContext().markModified(s);
                                    ctx.commitTransaction();
                                } catch (Throwable t) {
                                    ctx.abortTransaction();

                                    throw new OvationException("Unable to add input source", t);
                                }

                            }

                            sourceNames.add(epochId);
                        }

                        m.setSourceNames(sourceNames);
                    }

                    EventQueueUtilities.runOnEDT(new Runnable() {

                        @Override
                        public void run() {
                            addSourcesTextField.setText("");
                            updateInputs();
                        }
                    });

                }
            }, ph);
        }
    }
}
