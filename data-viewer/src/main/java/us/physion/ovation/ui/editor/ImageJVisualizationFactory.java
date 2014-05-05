package us.physion.ovation.ui.editor;

//import net.imglib2.img.ImagePlusAdapter;
//import net.imglib2.img.Img;
//import net.imglib2.img.ImgPlus;
//import net.imglib2.img.display.imagej.ImageJFunctions;
//import net.imglib2.io.ImgIOException;
//import net.imglib2.io.ImgOpener;
import java.util.concurrent.ExecutionException;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.mixin.DataElement;
import us.physion.ovation.exceptions.OvationException;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author huecotanks
 */
    public class ImageJVisualizationFactory implements VisualizationFactory{

    @Override
    public DataVisualization createVisualization(DataElement r) {
        try {
            return new ImageJVisualization(r.getData().get());
            /*try {
             /*ImgPlus ip = ImgOpener.open(((URLResponse)r).getURLString());
             // display the dataset
             DisplayService displayService = new ImageJ().getService(DisplayService.class);
             displayService.getActiveDisplay().display(ip);
             return new ImageJVisualization(((URLResponse)r).getURLString());*/

            // define the file to open



            // display it via ImageJ
            //imp.show();//null pointer

            // wrap it into an ImgLib image (no copying)
            //final Img image = ImagePlusAdapter.wrap( imp );

            // display it via ImgLib using ImageJ
            //ImageJFunctions.show( image );
                    /*} catch (Exception ex) {
             System.out.println(ex.getMessage());
             /*try{
             ImgPlus ip = ImgOpener.open(((URLResponse)r).getURLString());
             // display the dataset
             DisplayService displayService = new ImageJ().getService(DisplayService.class);
             displayService.getActiveDisplay().display(ip);
             return new ImageJVisualization(((URLResponse)r).getURLString());
             /*System.out.println("First error " + e.getMessage());
             try{
             // load the dataset
             final IOService ioService = context.getService(IOService.class);
             final Dataset dataset = ioService.loadDataset(url);

             // display the dataset
             final DisplayService displayService =
             context.getService(DisplayService.class);
             displayService.createDisplay(file.getName(), dataset);*/
            /*}catch (Exception e)
             {
             System.out.println(e.getMessage());
             }*/
            /*
             * }
             */
        } catch (InterruptedException ex) {
            throw new OvationException(ex);
        } catch (ExecutionException ex) {
            throw new OvationException(ex);
        }
    }

    @Override
    public int getPreferenceForDataContainer(DataElement r) {
        if (r.getDataContentType().toLowerCase().contains("tif")) {
            return 110;
        }
        return -1;
    }
}