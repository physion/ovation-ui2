/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.detailviews;

import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RMISecurityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.Source;
import us.physion.ovation.exceptions.OvationException;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.TestEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;
import static us.physion.ovation.ui.test.OvationTestCase.dsc;
import us.physion.ovation.domain.Resource;

/**
 *
 * @author huecotanks
 */
public class ResourceViewTopComponentTest extends OvationTestCase{
    private IResourceWrapper rw1;
    private IResourceWrapper rw2;

    private TestEntityWrapper project;
    private TestEntityWrapper source;

    @Before
    public void setUp() {
        try {
            super.setUp();

            String UNUSED_NAME = "name";
            String UNUSED_PURPOSE = "purpose";
            DateTime UNUSED_START = new DateTime(0);
            String contentType = "application/text";

            File temp;
            try {
                //make a temp file
                temp = File.createTempFile("temp-file-name", ".tmp");
                temp.deleteOnExit();
            } catch (IOException ex) {
                throw new OvationException(ex);
            }

            project = new TestEntityWrapper(ctx, ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START));
            source = new TestEntityWrapper(ctx, ctx.insertSource("source", "1002"));
            Resource r1 = ((Project) project.getEntity()).addResource("resource 1", temp.toURI().toURL(), contentType);
            rw1 = new DummyResourceWrapper(dsc, "resource 1", r1);
            Resource r2 = ((Source) source.getEntity()).addResource("resource 2", temp.toURI().toURL(), contentType);
            rw2 = new DummyResourceWrapper(dsc, "resource 2", r2);
        } catch (MalformedURLException ex) {
        }
    }

    @Test
    public void testUpdateResourcesUpdatesTheEntitiesList()
    {
        ResourceViewTopComponent t = new ResourceViewTopComponent();
        assertTrue( t.getResources().isEmpty());
        t.updateResources(Sets.<IEntityWrapper>newHashSet(project, source));

        Set<String> nameSet = new HashSet();
        for (IResourceWrapper rw :t.getResources()){
            nameSet.add(rw.getName());
        }
        assertTrue(nameSet.contains(rw1.getName()));
        assertTrue(nameSet.contains(rw2.getName()));

        t.updateResources(Sets.<IEntityWrapper>newHashSet(source));
        List<IResourceWrapper> resources = t.getResources();
        assertTrue(resources.get(0).getName().equals(rw2.getName()));
        assertEquals(resources.size(), 1);

    }

     @Test
    public void testUpdateResourcesUpdatesTheEditedSet()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        entitySet.add(project);
        entitySet.add(source);
        ResourceViewTopComponent t = new ResourceViewTopComponent();
        t.updateResources(entitySet);

        t.editResource(rw1);
        assertTrue(t.editedSet.contains(rw1));
    }

     @Test
     public void testAddNewResource() throws IOException
     {
         Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        entitySet.add(source);
        ResourceViewTopComponent t = new ResourceViewTopComponent();
        t.updateResources(entitySet);
        File tmp = null;
        try{
             tmp = File.createTempFile("file", ".ext");
             BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
             out.write("Stuff in the file!!");
             out.close();
             t.addResource(entitySet, tmp.getAbsolutePath());

             boolean foundResource = false;
             for (IResourceWrapper rw : t.getResources())
             {
                 if (rw.getName().equals(tmp.getName()))
                 {
                     foundResource = true;
                 }
             }

             assertNotNull(((Source)source.getEntity()).getResource(tmp.getName()));
             assertTrue(foundResource);
         }
        finally{
            tmp.delete();
        }
     }

    @Test
    public void testDeactivateSyncButtonWhenTheresNothingToSync()
    {
        Set<IEntityWrapper> entitySet = new HashSet<IEntityWrapper>();

        entitySet.add(project);
        entitySet.add(source);
        ResourceViewTopComponent t = new ResourceViewTopComponent();
        t.updateResources(entitySet);
        assertFalse(t.saveButtonIsEnabled());

        t.editResource(rw1);
        assertTrue(t.saveButtonIsEnabled());

        t.removeResources(new IResourceWrapper[]{rw1}, entitySet);
        assertFalse(t.saveButtonIsEnabled());
        assertEquals(t.getResources().size(), 1);
    }


}
