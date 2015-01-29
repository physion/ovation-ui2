package us.physion.ovation.ui.editor;

import com.google.common.collect.Sets;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.display.SingleImagePanel;
import com.pixelmed.display.SourceImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.JComponent;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Resource;

/**
 *
 * @author huecotanks
 */
public class DicomWrapper extends AbstractDataVisualization {
    private final static Logger log = LoggerFactory.getLogger(DicomWrapper.class);

    String name;
    SourceImage src;
    final Resource entity;

    DicomWrapper(Resource r) {
        entity = r;
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(new FileInputStream(r.getData().get()));
            src = new SourceImage(in);
            this.name = r.getName();
        } catch (InterruptedException ex) {
            log.info("", ex);
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } catch (ExecutionException ex) {
            log.info("", ex);
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } catch (DicomException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    log.warn("", ex);
                    throw new RuntimeException(ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    @Override
    public JComponent generatePanel() {
        return new ImagePanel(name, new SingleImagePanel(src));
    }

    @Override
    public boolean shouldAdd(Resource r) {
        return false;
    }

    @Override
    public void add(Resource r) {
        throw new UnsupportedOperationException("Dicoms are not displayed in groups");
    }

    @Override
    public Iterable<? extends OvationEntity> getEntities() {
        return Sets.newHashSet(entity);
    }

}
