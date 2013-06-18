/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Epoch;
import us.physion.ovation.domain.EpochContainer;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Project;
import us.physion.ovation.domain.mixin.ProcedureElement;
import us.physion.ovation.ui.browser.EntityChildren;
import us.physion.ovation.ui.browser.EntityWrapper;
import us.physion.ovation.ui.browser.EntityWrapperUtilities;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author jackie
 */
public class EpochSelector extends JPanel implements Lookup.Provider, ExplorerManager.Provider{

    private JButton refreshButton;
    private JButton queryButton;
    private BeanTreeView browserTree;
    private ExplorerManager em;
    private Lookup l;
    ChangeSupport changeSupport;
    
    public EpochSelector(ChangeSupport cs)
    {
        super();
        changeSupport = cs;
        initComponents();
    }
    void initComponents()
    {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        refreshButton = new JButton(new ImageIcon(""));//TODO: image icons
        queryButton = new JButton(new ImageIcon(""));//TODO: image icons
        browserTree = new BeanTreeView();
        em = new ExplorerManager();
        l = ExplorerUtils.createLookup(em, getActionMap());
        Lookup.Result <EntityWrapper> pe = l.lookupResult(EntityWrapper.class);
        pe.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent le) {
                changeSupport.fireChange();
            }
        });
        
        browserTree.setRootVisible(false);
        this.add(refreshButton, c);
        
        c.gridx = 1;
        this.add(queryButton, c);
        
        c.gridy = 1;
        c.gridx = 0;
        c.gridheight = 3;
        c.gridwidth = 5;
        this.add(browserTree, c);
    }
    
    public void resetNodes()
    {
        DataContext ctx = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        List<EntityWrapper> exps = Lists.newArrayList(Iterables.transform(ctx.getProjects(), new Function<Project, EntityWrapper>() {

                @Override
                public EntityWrapper apply(Project input) {
                    return new EntityWrapper(input);
                }
            }));
        em.setRootContext(new AbstractNode(new EntityChildren(exps)
        {
            ArrayList<Class<? extends ProcedureElement>> viewableClasses = Lists.newArrayList(Epoch.class, EpochContainer.class);
            @Override
            protected List<EntityWrapper> createKeysForEntity(DataContext c, EntityWrapper ew) {
                List<EntityWrapper> list = new ArrayList();
                for (Class clazz : viewableClasses)
                {
                    if (ew.getType().isAssignableFrom(clazz)) {
                        return (super.createKeysForEntity(c, ew));
                    }
                }
                return list;
            }
        }));
    }
    
    public ProcedureElement getProcedureElement()
    {
        return (ProcedureElement)l.lookup(EntityWrapper.class).getEntity();
    }

    @Override
    public Lookup getLookup() {
        return l;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}
