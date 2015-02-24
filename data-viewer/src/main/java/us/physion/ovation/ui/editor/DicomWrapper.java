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
import us.physion.ovation.domain.mixin.Content;
import us.physion.ovation.ui.actions.ContentUtils;

/**
 *
 * @author huecotanks
 */
public class DicomWrapper extends AbstractDataVisualization {
    private final static Logger log = LoggerFactory.getLogger(DicomWrapper.class);

    String name;
    SourceImage src;
    final Content entity;

    DicomWrapper(Content r) {
        entity = r;
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(new FileInputStream(r.getData().get()));
            src = new SourceImage(in);
            this.name = ContentUtils.contentLabel(r);
        } catch (InterruptedException | ExecutionException ex) {
            log.info("", ex);
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        } catch (DicomException | IOException ex) {
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
    public boolean shouldAdd(Content r) {
        return false;
    }

    @Override
    public void add(Content r) {
        throw new UnsupportedOperationException("Dicoms are not displayed in groups");
    }

    @Override
    public Iterable<? extends OvationEntity> getEntities() {
        return Sets.newHashSet((OvationEntity)entity);
    }

}
