/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.importer;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.in.PrairieReader;
import loci.formats.in.ZeissLSMReader;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.services.OMEXMLService;
import org.junit.*;
import static org.junit.Assert.*;
import us.physion.ovation.ui.test.OvationTestCase;

/**
 *
 * @author huecotanks
 */
public class ImportImageTest extends OvationTestCase{
    @Test
    public void testSomeMethod() {
       /* ServiceFactory factory = null;
        OMEXMLService service = null;
        IMetadata meta = null;
        try {
            factory = new ServiceFactory();

            service = factory.getInstance(OMEXMLService.class);
            try {
                meta = service.createOMEXMLMetadata();
            } catch (ServiceException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (DependencyException ex) {
            System.out.println(ex.getMessage());
        }

        // create format reader
        IFormatReader reader = new ImageReader();//ZeissLSMReader();//PrairieReader();
        reader.setMetadataStore(meta);
        try {
            // initialize file
            //reader.setId("/Users/jackie/Documents/ZSeries-12062012-1229-002/ZSeries-12062012-1229-002_Cycle00004_CurrentSettings_Ch2_001000.tif");
            reader.setId("/Users/jackie/Documents/Stack&Tile_Stitched.lsm");

        } catch (FormatException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        MetadataRetrieve ret = service.asRetrieve(reader.getMetadataStore());
        * */
    }
}
