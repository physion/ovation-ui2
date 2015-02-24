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

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.apache.commons.io.IOUtils;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.exceptions.ResourceNotFoundException;

@ServiceProvider(service = VisualizationFactory.class)
@Messages({
    "# {0} - resource name",
    "LBL_TextLoadingFailed=Loading {0} failed :-(",
    "LBL_TextLoading=Loading",
    "LBL_LineWrap=Line wrap"
})
public class PlainTextVisualizationFactory implements VisualizationFactory {

    private final static String PLAIN_TEXT_MIMETYPE = "text/plain"; //NOI18N
    private final static Logger log = LoggerFactory.getLogger(PlainTextVisualizationFactory.class);
    private ExecutorService loadFileExecutors = Executors.newSingleThreadExecutor();

    @Override
    public DataVisualization createVisualization(final Resource r) {
        return new AbstractDataVisualization() {
            @Override
            public JComponent generatePanel() {
                class PlainTextArea extends JTextArea {

                    private boolean scrollableTracksViewportWidth = false;

                    @Override
                    public boolean getScrollableTracksViewportWidth() {
                        return scrollableTracksViewportWidth;
                    }

                    private void setScrollableTracksViewportWidth(boolean b) {
                        if (b == scrollableTracksViewportWidth) {
                            return;
                        }
                        scrollableTracksViewportWidth = b;
                    }

                    private void failed() {
                        setText(Bundle.LBL_TextLoadingFailed(r.getName()));
                    }

                    @Override
                    public void addNotify() {
                        super.addNotify();

                        ListenableFuture<File> data;

                        try {
                            data = r.getData();
                        } catch (ResourceNotFoundException ex) {
                            log.warn("Resource not found", ex);
                            failed();

                            return;
                        }

                        Futures.addCallback(data, new FutureCallback<File>() {
                            @Override
                            public void onSuccess(File f) {
                                try {
                                    FileReader fr = new FileReader(f);
                                    try {
                                        final String text = IOUtils.toString(fr);
                                        EventQueue.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                setText(text);
                                                setCaretPosition(0);
                                                repaint();
                                            }
                                        });
                                    } finally {
                                        IOUtils.closeQuietly(fr);
                                    }
                                } catch (IOException ex) {
                                    log.warn("Could not load text", ex);
                                    failed();
                                }
                            }

                            @Override
                            public void onFailure(Throwable ex) {
                                log.warn("Could not get file", ex);
                                failed();
                            }
                        }, loadFileExecutors);
                    }
                }

                final PlainTextArea t = new PlainTextArea();

                t.setEditable(false);
                t.setText(Bundle.LBL_TextLoading());

                ParentWidthPanel panel = new ParentWidthPanel();

                panel.add(new JScrollPane(t), BorderLayout.CENTER);

                {
                    JToolBar toolbar = new JToolBar(SwingConstants.HORIZONTAL);
                    toolbar.setBackground(Color.WHITE);

                    toolbar.add(new JToggleButton(new AbstractAction(Bundle.LBL_LineWrap()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            boolean selected = ((JToggleButton) e.getSource()).isSelected();

                            t.setLineWrap(selected);
                            t.setScrollableTracksViewportWidth(selected);
                            t.setCaretPosition(0);

                            t.revalidate();
                            t.repaint();
                        }
                    }));

                    panel.add(toolbar, BorderLayout.NORTH);
                }

                return panel;
            }

            @Override
            public boolean shouldAdd(Resource r) {
                return false;
            }

            @Override
            public void add(Resource r) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterable<? extends OvationEntity> getEntities() {
                return Sets.newHashSet(r);
            }
        };
    }

    @Override
    public int getPreferenceForDataContentType(String contentType) {
        if (PLAIN_TEXT_MIMETYPE.equals(contentType)) {
            return 100;
        }
        return -1;
    }

    private static class ParentWidthPanel extends JPanel {

        ParentWidthPanel() {
            super(new BorderLayout());
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = new Dimension(super.getPreferredSize());

            Container parent = getParent();
            if (parent != null) {
                Dimension parentSize = parent.getSize();
                if (getScrollableTracksViewportWidth()) {
                    d.width = parentSize.width;
                }
                if (getScrollableTracksViewportHeight()) {
                    d.height = parentSize.height;
                }
            }

            return d;
        }
    }

}
