/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.editor;

//import net.imglib2.img.ImagePlusAdapter;
//import net.imglib2.img.Img;
//import net.imglib2.img.ImgPlus;
//import net.imglib2.img.display.imagej.ImageJFunctions;
//import net.imglib2.io.ImgIOException;
//import net.imglib2.io.ImgOpener;
import java.net.MalformedURLException;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import us.physion.ovation.domain.Measurement;

@ServiceProvider(service = VisualizationFactory.class)
/**
 *
 * @author huecotanks
 */
    public class ImageJVisualizationFactory implements VisualizationFactory{

    @Override
	public Visualization createVisualization(Measurement r) {
        try {
            return new ImageJVisualization(r.getURI().toURL().toExternalForm());
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
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        }
    }

    @Override
	public int getPreferenceForDataContainer(Measurement r) {
        if (r.getMimeType().toLowerCase().contains("tif")) {
            return 110;
        }
        return -1;
    }
}