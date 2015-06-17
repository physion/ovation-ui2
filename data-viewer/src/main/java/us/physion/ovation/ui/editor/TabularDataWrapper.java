package us.physion.ovation.ui.editor;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.mixin.Content;
import us.physion.ovation.exceptions.OvationException;

public class TabularDataWrapper extends AbstractDataVisualization {

    static Logger logger = LoggerFactory.getLogger(TabularDataWrapper.class);

    private final Content entity;
    private final TabularData data;
    
    public static TabularData load(File file) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(file));
        List<String[]> entries = reader.readAll();
        String[] columnNames = entries.remove(0);

        return new TabularData(entries, columnNames, file);
    }

    TabularDataWrapper(Content r)
    {
        entity = r;

        try {
            data = load(r.getData().get());
        } catch (InterruptedException | ExecutionException | IOException ex) {
            String rId = "";
            if (r != null)
            {
                rId = "'" + ((OvationEntity)r).getUuid() + "'";
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
    public boolean shouldAdd(Content r) {
        return false;
    }

    @Override
    public void add(Content r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterable<? extends OvationEntity> getEntities() {
        return Sets.newHashSet((OvationEntity)entity);
    }
}
