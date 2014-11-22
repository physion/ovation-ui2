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
package us.physion.ovation.ui.editor.xls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.ui.editor.AbstractDataVisualization;
import us.physion.ovation.ui.editor.DataVisualization;
import us.physion.ovation.ui.editor.VisualizationFactory;
import us.physion.ovation.ui.editor.pdf.PDFVisualizationFactory;

@ServiceProvider(service = VisualizationFactory.class)
@NbBundle.Messages({
    "# {0} - xls name",
    "LBL_XLSLoadingFailed=Loading {0} failed"
})
public class XLSVisualizationFactory implements VisualizationFactory {

    private final static String XLSX_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; //NOI18N

    public XLSVisualizationFactory() {
    }

    @Override
    public DataVisualization createVisualization(final DataElement r) {
        return new AbstractDataVisualization(Collections.singleton(r)) {

            @Override
            public boolean shouldAdd(DataElement r) {
                return false;
            }

            @Override
            public void add(DataElement r) {
                throw new UnsupportedOperationException();
            }

            @Override
            public JComponent generatePanel() {
                PDFVisualizationFactory.LoadingHelper h = new PDFVisualizationFactory.LoadingHelper(r) {

                    @Override
                    protected void open(File f) {
                        content.add(loadXLSX(f), BorderLayout.CENTER);
                    }

                    @Override
                    protected void failed(Throwable t) {
                        //TODO: Log the exception?
                        content.add(new JLabel(Bundle.LBL_XLSLoadingFailed(r.getName())), BorderLayout.CENTER);
                    }
                };

                return h.load();
            }
        };
    }

    @Override
    public int getPreferenceForDataContainer(DataElement r) {
        return XLSX_MIMETYPE.equals(r.getDataContentType()) ? 100 : -1;
    }

    private JComponent loadXLSX(File f) {
        final JTabbedPane tab = new JTabbedPane(JTabbedPane.BOTTOM);
        //This is generally redundant. Adding in case some LnF actually respects it.
        tab.setBackground(Color.WHITE);
        new XLSXReader() {
            @Override
            protected void addSheet(String sheetName, JComponent c) {
                tab.addTab(sheetName, c);
                tab.setBackgroundAt(tab.getTabCount() - 1, Color.WHITE);
            }
        }.readAll(f);
        return tab;
    }

}
