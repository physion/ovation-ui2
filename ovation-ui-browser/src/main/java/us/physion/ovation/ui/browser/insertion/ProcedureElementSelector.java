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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import us.physion.ovation.ui.browser.FilteredEntityChildren;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author jackie
 */
public class ProcedureElementSelector extends JPanel implements Lookup.Provider, ExplorerManager.Provider{

    private JButton refreshButton;
    private JButton queryButton;
    private BeanTreeView browserTree;
    private ExplorerManager em;
    private Lookup l;
    ChangeSupport changeSupport;
    
    @Override
    public String getName()
    {
        return "Select an existing Epoch or containing ProcedureElement";
    }
    
    public ProcedureElementSelector(ChangeSupport cs)
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
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        refreshButton = new JButton(new ImageIcon("us/physion/ovation/ui/browser/refresh24.png"));//TODO: image icons
        refreshButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                resetNodes();
            }
        });
        queryButton = new JButton(new ImageIcon("us/physion/ovation/ui/browser/reset-query24.png"));//TODO: image icons
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
        resetNodes();
        this.setSize(500, 500);
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
        em.setRootContext(new AbstractNode(new FilteredEntityChildren(FilteredEntityChildren.wrap(ctx.getProjects()), Sets.<Class>newHashSet(ProcedureElement.class))));
    }
    
    public ProcedureElement getProcedureElement()
    {
        EntityWrapper ew = l.lookup(EntityWrapper.class);
        if (ew == null)
            return null;
        
        if (ProcedureElement.class.isAssignableFrom(ew.getType()))
            return (ProcedureElement)ew.getEntity();
        return null;
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