package us.physion.ovation.ui.editor;

//import net.imglib2.img.ImagePlusAdapter;
//import net.imglib2.img.Img;
//import net.imglib2.img.ImgPlus;
//import net.imglib2.img.display.imagej.ImageJFunctions;
//import net.imglib2.io.ImgIOException;
//import net.imglib2.io.ImgOpener;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.Resource;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author huecotanks
 */
public class ImageJVisualizationFactory implements VisualizationFactory {

    @Override
    public DataVisualization createVisualization(Resource r) {

        return new ImageJVisualization(r);
    }

    @Override
    public int getPreferenceForDataContainer(Resource r) {
        if (r.getDataContentType().toLowerCase().contains("tif")) {
            return 110;
        }
        return -1;
    }
}
