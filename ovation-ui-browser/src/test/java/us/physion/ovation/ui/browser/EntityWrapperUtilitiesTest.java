/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser;

import org.junit.*;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.test.OvationTestCase;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import us.physion.ovation.domain.*;

/**
 *
 * @author jackie
 */
public class EntityWrapperUtilitiesTest extends OvationTestCase{

    ExplorerManager em;
    Map<String, Node> treeMap;
    public EntityWrapperUtilitiesTest() {
    }
    
    @Test
    public void testCreateNodeForNodeThatAlreadyExists() {
        //TODO
    }
/*
    @Test
    public void testQuerySetsProjectViewRootNodeAppropriately() {
        em = new ExplorerManager();
        em.setRootContext(new AbstractNode(new QueryChildren(true)));
        Iterator<OvationEntity> itr = dsc.getContext().query(Experiment.class, "true");

        Set s = new HashSet<ExplorerManager>();
        s.add(em);
        EntityWrapperUtilities.createNodesFromQuery(s, itr);

        Node[] projects = em.getRootContext().getChildren().getNodes(true);
        Set<String> projectSet = new HashSet<String>();
        for (Project p : dsc.getContext().getProjects()) {
            projectSet.add(p.getURI().toString());
        }

        for (Node n : projects) {
            IEntityWrapper ew = n.getLookup().lookup(IEntityWrapper.class);
            assertTrue(projectSet.contains(ew.getURI()));
            projectSet.remove(ew.getURI());
        }
        assertTrue(projectSet.isEmpty());
    }

    @Test
    public void testQuerySetsSourceViewRootNodeAppropriately() {
        ExplorerManager em = new ExplorerManager();
        em.setRootContext(new AbstractNode(new QueryChildren(false)));
        Iterator<OvationEntity> itr = dsc.getContext().query(Experiment.class, "true");

        Set mgrSet = new HashSet<ExplorerManager>();
        mgrSet.add(em);
        EntityWrapperUtilities.createNodesFromQuery(mgrSet, itr);

        Node[] sources = em.getRootContext().getChildren().getNodes(true);
        Set<String> sourcesSet = new HashSet<String>();

        for (Source s : dsc.getContext().getSources()) {
            if (s.getParents().isEmpty()) {
                sourcesSet.add(s.getURI().toString());
            }
        }
        for (Node n : sources) {
            IEntityWrapper ew = n.getLookup().lookup(IEntityWrapper.class);
            assertTrue(sourcesSet.contains(ew.getURI()));
            sourcesSet.remove(ew.getURI());
        }
        assertTrue(sourcesSet.isEmpty());
    }

    @Test
    public void testQuerySetsExperimentNodesAppropriatelyInSourceView() {
        ExplorerManager em = new ExplorerManager();
        em.setRootContext(new AbstractNode(new QueryChildren(false)));
        Iterator<OvationEntity> itr = dsc.getContext().query(Experiment.class, "true");

        Set mgrSet = new HashSet<ExplorerManager>();
        mgrSet.add(em);
        EntityWrapperUtilities.createNodesFromQuery(mgrSet, itr);
        while (itr.hasNext()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        ArrayList<Node> sourceChildren = new ArrayList();
        for (Node n : em.getRootContext().getChildren().getNodes(true)) {
            Node[] nodes = n.getChildren().getNodes(true);
            for (Node child : nodes) {
                sourceChildren.add(child);
            }
        }
        Set<String> entitySet = new HashSet<String>();

        for (Source s : dsc.getContext().getSources()) {
            if (s.getParent() == null) {
                for (Experiment e : s.getExperiments()) {
                    entitySet.add(e.getURI().toString());
                }
            }
        }
        for (Node n : sourceChildren) {
            IEntityWrapper ew = n.getLookup().lookup(IEntityWrapper.class);
            if (ew.getType().isAssignableFrom(Experiment.class)) {
                assertTrue(entitySet.contains(ew.getURI()));
                entitySet.remove(ew.getURI());
            }
        }
        assertTrue(entitySet.isEmpty());
    }
    @Test
    public void testQuerySetsExperimentNodesAppropriatelyInProjectView() {
        ExplorerManager em = new ExplorerManager();
        em.setRootContext(new AbstractNode(new QueryChildren(true)));
        Iterator<OvationEntity> itr = dsc.getContext().query(Experiment.class, "true");

        Set mgrSet = new HashSet<ExplorerManager>();
        mgrSet.add(em);
        EntityWrapperUtilities.createNodesFromQuery(mgrSet, itr);

        ArrayList<Node> projectChildren = new ArrayList();
        for (Node n : em.getRootContext().getChildren().getNodes(true)) {
            for (Node child : n.getChildren().getNodes(true)) {
                projectChildren.add(child);
            }
        }
        Set<String> entitySet = new HashSet<String>();
        for (Project p : dsc.getContext().getProjects()) {
            for (Experiment e : p.getExperiments()) {
                entitySet.add(e.getURI().toString());
            }
        }
        for (Node n : projectChildren) {
            IEntityWrapper ew = n.getLookup().lookup(IEntityWrapper.class);
            if (ew.getType().isAssignableFrom(Experiment.class)) {
                assertTrue(entitySet.contains(ew.getURI()));
                entitySet.remove(ew.getURI());
            } else {
                fail("Project node's child was something other than an Experment");
            }
        }
        assertTrue(entitySet.isEmpty());
    }

    @Test
    public void testQuerySetsAnalysisRecordNodesAppropriatelyInProjectView() {
        ExplorerManager em = new ExplorerManager();
        em.setRootContext(new AbstractNode(new QueryChildren(true)));
        Iterator<OvationEntity> itr = dsc.getContext().query"true");

        Set mgrSet = new HashSet<ExplorerManager>();
        mgrSet.add(em);
        EntityWrapperUtilities.createNodesFromQuery(mgrSet, itr);

        ArrayList<Node> analysisRecordNodes = new ArrayList();
        for (Node n : em.getRootContext().getChildren().getNodes(true)) {
            for (Node child : n.getChildren().getNodes(true)) {
                for (Node grandChild : child.getChildren().getNodes(true)) {
                    analysisRecordNodes.add(grandChild);
                }
            }
        }
        Set<String> entitySet = new HashSet<String>();
        for (User user : dsc.getContext().getUsers()) {
            for (Project p : dsc.getContext().getProjects()) {
                for (AnalysisRecord e : p.getAnalysisRecords(user)) {
                    entitySet.add(e.getURI().toString());
                }
            }
        }

        for (Node n : analysisRecordNodes) {
            IEntityWrapper ew = n.getLookup().lookup(IEntityWrapper.class);
            if (ew.getType().isAssignableFrom(Experiment.class)) {
                assertTrue(entitySet.contains(ew.getURI()));
                entitySet.remove(ew.getURI());
            } else {
                fail("Project node's child was something other than an Experment");
            }
        }
        assertTrue(entitySet.isEmpty());
    }*/

    //Manual test
    @Test
    public void testPerformanceOnManyChildrenNodes() {
    }

    @Test
    public void testQueryListenerCancelsIteration() {
    }
}
