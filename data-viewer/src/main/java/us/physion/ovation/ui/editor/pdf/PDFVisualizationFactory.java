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
package us.physion.ovation.ui.editor.pdf;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.utility.annotation.AnnotationPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.exceptions.ResourceNotFoundException;
import us.physion.ovation.ui.editor.AbstractDataVisualization;
import us.physion.ovation.ui.editor.DataVisualization;
import us.physion.ovation.ui.editor.VisualizationFactory;

@ServiceProvider(service = VisualizationFactory.class)
@NbBundle.Messages({
    "# {0} - pdf name",
    "LBL_PDFLoadingFailed=Loading {0} failed",
    "LBL_PDFLoading=Loading"
})
public class PDFVisualizationFactory implements VisualizationFactory {

    private final static String PDF_MIMETYPE = "application/pdf"; //NOI18N

    @Override
    public DataVisualization createVisualization(final Resource r) {
        return new AbstractDataVisualization(Collections.singleton(r)) {

            @Override
            public boolean shouldAdd(Resource r) {
                return false;
            }

            @Override
            public void add(Resource r) {
                throw new UnsupportedOperationException();
            }

            @Override
            public JComponent generatePanel() {
                LoadingHelper h = new LoadingHelper(r) {

                    @Override
                    protected void open(File f) {
                        content.add(loadPDF(f.getAbsolutePath()), BorderLayout.CENTER);
                    }

                    @Override
                    protected void failed(Throwable t) {
                        //TODO: Log the exception?
                        content.add(new JLabel(Bundle.LBL_PDFLoadingFailed(r.getName())), BorderLayout.CENTER);
                    }
                };

                return h.load();
            }

            protected JComponent loadPDF(String path) {
                final SwingController controller = new SwingController();

                //excluding the save button and everything annotation-related
                SwingViewBuilder builder = new SwingViewBuilder(controller) {
                    @Override
                    public JToolBar buildCompleteToolBar(boolean embeddableComponent) {
                        //the original toolbar is too wide, restrict it to 100px so only the document width matters
                        JToolBar toolbar = new JToolBar() {

                            @Override
                            public Dimension getMinimumSize() {
                                Dimension d = super.getMinimumSize();

                                //100 is just about 32 * 3 plus some extra pixels, ie 3 toolbar buttons.
                                //declaring 3 toolbar buttons as the minimum width
                                int MIN_WIDTH = 100;

                                if (d.width > MIN_WIDTH) {
                                    return new Dimension(MIN_WIDTH, d.height);
                                } else {
                                    return d;
                                }
                            }

                            @Override
                            public Dimension getPreferredSize() {
                                return getMinimumSize();
                            }

                        };
                        toolbar.setFloatable(false);
                        
                        //move toolbars over
                        JToolBar t = super.buildCompleteToolBar(embeddableComponent);
                        while (t.getComponentCount() > 0) {
                            Component c = t.getComponent(0);
                            t.remove(0);
                            toolbar.add(c);
                        }

                        return toolbar;
                    }
                    
                    private JTabbedPane cloneUtilityTabbedPane(JTabbedPane t) {
                        //making this clone with a better getPreferredSize so the split pane parent won't have such a large width
                        JTabbedPane clone = new JTabbedPane() {
                            @Override
                            public Dimension getPreferredSize() {
                                if (!isVisible()) {
                                    return new Dimension(20, 20);
                                }
                                return super.getPreferredSize();
                            }
                        };

                        while (t.getTabCount() > 0) {
                            String title = t.getTitleAt(0);
                            Component c = t.getComponentAt(0);
                            
                            t.remove(0);
                            
                            if (c == null) {
                                //this would be the annotations tab
                                continue;
                            }

                            clone.addTab(title, c);
                        }
                        
                        return clone;
                    }
                    
                    @Override
                    public JButton buildSaveAsFileButton() {
                        return null;
                    }

                    @Override
                    public JToolBar buildAnnotationlToolBar() {
                        return null;
                    }

                    @Override
                    public JToolBar buildAnnotationUtilityToolBar() {
                        return null;
                    }

                    @Override
                    public AnnotationPanel buildAnnotationPanel() {
                        return null;
                    }

                    @Override
                    public JTabbedPane buildUtilityTabbedPane() {
                        //Hiding the annotation tab:
                        //
                        //XXX: I'm not using PropertiesManager since PropertiesManager displays a message about creating a folder to store the settings (and even without the message, creates the folder)
                        // Ideally:
                        //        PropertiesManager properties = new PropertiesManager(System.getProperties(), ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
                        //        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION, Boolean.FALSE);

                        JTabbedPane t = super.buildUtilityTabbedPane();

                        JTabbedPane clone = cloneUtilityTabbedPane(t);

                        controller.setUtilityTabbedPane(clone);

                        return clone;
                    }
                };

                JPanel pdf = builder.buildViewerPanel();

                ComponentKeyBinding.install(controller, pdf);

                controller.openDocument(path);

                //XXX: This component looks weird in a scrollpane since the document already has its own scrollpane.
                //But it also has a very large 'minimum size' which makes it look odd when multiple nodes are selected...
                return pdf;
            }

        };
    }

    @Override
    public int getPreferenceForDataContentType(String contentType) {
        return PDF_MIMETYPE.equals(contentType) ? 100 : -1;
    }

    public abstract static class LoadingHelper {

        private final Resource r;
        protected final JPanel content;

        public LoadingHelper(Resource r) {
            this.r = r;
            content = new JPanel(new BorderLayout());
            content.setBackground(Color.WHITE);
            content.add(new JLabel(Bundle.LBL_PDFLoading()), BorderLayout.CENTER);
        }

        public JComponent load() {
            ListenableFuture<File> data;

            try {
                data = r.getData();
            } catch (ResourceNotFoundException ex) {
                failed(ex);

                return content;
            }

            Futures.addCallback(data, new FutureCallback<File>() {

                @Override
                public void onSuccess(File f) {
                    open(f);
                }

                @Override
                public void onFailure(Throwable t) {
                    failed(t);
                }

            });

            return content;
        }

        protected abstract void open(File f);

        protected abstract void failed(Throwable t);
    }

}
