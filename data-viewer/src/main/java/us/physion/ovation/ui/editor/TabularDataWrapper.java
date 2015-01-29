package us.physion.ovation.ui.editor;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import javax.swing.JComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;
import us.physion.ovation.exceptions.OvationException;

public class TabularDataWrapper extends AbstractDataVisualization {

    static Logger logger = LoggerFactory.getLogger(TabularDataWrapper.class);

    private final Resource entity;
    private final TabularData data;

    TabularDataWrapper(Resource r)
    {
        entity = r;

        try {
            File file = r.getData().get();

            CSVReader reader = new CSVReader(new FileReader(r.getData().get()));
            List<String[]> entries = reader.readAll();
            String[] columnNames = entries.remove(0);
            
            data = new TabularData(entries, columnNames, file);

        } catch (Exception ex) {
            String rId = "";
            if (r != null)
            {
                rId = "'" + r.getUuid() + "'";
            }
            logger.debug("Error parsing tabular data file " + rId + ":" + ex.getLocalizedMessage());
            throw new OvationException(ex);
        }
    }

    @Override
    public JComponent generatePanel() {
        return new TabularPanel(data);
    }

    @Override
    public boolean shouldAdd(Resource r) {
        return false;
    }

    @Override
    public void add(Resource r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterable<? extends OvationEntity> getEntities() {
        return Sets.newHashSet(entity);
    }
}
