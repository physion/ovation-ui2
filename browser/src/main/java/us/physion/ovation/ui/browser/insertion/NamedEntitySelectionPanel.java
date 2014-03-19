/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ParameterTableModel;

/**
 *
 * @author huecotanks
 */
public abstract class NamedEntitySelectionPanel extends JPanel implements 
        ExplorerManager.Provider, Lookup.Provider {
    
    private JLabel descriptionLabel;
    final ChangeSupport cs;
    public DataContext context;
    private JScrollPane entityScrollPane;
    private JTable namedEntitiesTable;
    private JSplitPane jSplitPane1;
    private JButton addButton;
    private JButton removeButton;
    private String labelText;
    private final Set<IEntityWrapper> selectedEntities;
    
    private ParameterTableModel tableModel;
    private Lookup entitiesLookup;
    private ExplorerManager explorerManager;
    
    NamedEntitySelectionPanel(ChangeSupport cs, String labelText)
    {
        super();
        this.cs = cs;
        this.labelText = labelText;
        this.context = Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext();
        tableModel = new ParameterTableModel(false);//doesnt have the extra row for ui editing
        tableModel.setColumnNames(new String[]{"Name", "Entity"});
        tableModel.setEditableFunction(new Function<Point, Boolean>() {

            @Override
            public Boolean apply(Point input) {
                return input.x == 0; //only the first column is editable
            }
        });
        
        selectedEntities = Sets.newHashSet();
        
        initComponents();
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        if (labelText != null)
        {
            descriptionLabel = new JLabel(labelText);
            this.add(descriptionLabel);
        }
        
        jSplitPane1 = new JSplitPane();
        entityScrollPane = new JScrollPane();
        jSplitPane1.setLeftComponent(entityScrollPane);
        namedEntitiesTable = new JTable();
        namedEntitiesTable.setModel(tableModel);
        namedEntitiesTable.setEnabled(true);
        jSplitPane1.setRightComponent(namedEntitiesTable);
        jSplitPane1.setDividerLocation(300);
        
        explorerManager = new ExplorerManager();
        entitiesLookup = ExplorerUtils.createLookup(explorerManager, getActionMap());
        BeanTreeView entitiesTree = new BeanTreeView();
        entitiesTree.setRootVisible(false);
        entityScrollPane.setViewportView(entitiesTree);
        

        addButton = new JButton("+");
        removeButton = new JButton("-");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                addSelection();
            }
        });
        
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                removeSelection();
            }
        });
        jSplitPane1.setAlignmentX(jSplitPane1.LEFT_ALIGNMENT);
        buttonPanel.setAlignmentX(buttonPanel.LEFT_ALIGNMENT);
        this.add(jSplitPane1);
        this.add(buttonPanel);
        
        addButton.setEnabled(false);

        final Lookup.Result<IEntityWrapper> selectionResult
                = entitiesLookup.lookupResult(IEntityWrapper.class);
        selectionResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent le) {
                selectedEntities.clear();
                Collection<? extends IEntityWrapper> selection = selectionResult.allInstances();
                if (!selection.isEmpty()) {
                    addButton.setEnabled(true);
                    selectedEntities.addAll(selection);
                }
                
                addButton.setEnabled(selectedEntities.size() > 0);
            }
        });

        resetEntities();
    }

    @Override
    public String getName()
    {
        return getTitle();
    }
    
    abstract String getTitle();
    
    public void addSelection()
    {
        for(IEntityWrapper e : getSelectedEntities()) {
            addEntity(e.getDisplayName(), e.getEntity());
        }
    }
    
    protected Set<IEntityWrapper> getSelectedEntities() {
        return selectedEntities;
    }
    
    public void removeSelection()
    {
        int num = namedEntitiesTable.getSelectedRow();
        tableModel.remove(num);
        NamedEntitySelectionPanel.this.cs.fireChange();
    }
    
    public void addEntity(String name, OvationEntity value)
    {   
        int keysCount = tableModel.countKeys(name);
        if (keysCount > 0)
        {
            tableModel.addParameter(name + "." + (keysCount+1), value);
        }
        else{
            tableModel.addParameter(name, value);
        }
        NamedEntitySelectionPanel.this.cs.fireChange();
    }
    
    public Map<String, Object> getNamedEntities()
    {
        return tableModel.getParams();
    }
    
    abstract public void resetEntities();
    
    public void addSelectedEntities(Map<String, OvationEntity> entities)
    {
        if (entities == null)
            return;
        for (String name : entities.keySet())
        {
            OvationEntity e = entities.get(name);
            if (e != null)
                addEntity(name, e);
        }
    }
   
    public void finishEditing()
     {
         if (namedEntitiesTable.getCellEditor() != null)
         {
            namedEntitiesTable.getCellEditor().stopCellEditing();
         }
     }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public Lookup getLookup() {
        return entitiesLookup;
    }

    public IEntityWrapper getSelectedEntity() {
        return entitiesLookup.lookup(IEntityWrapper.class);
    }
    
}
