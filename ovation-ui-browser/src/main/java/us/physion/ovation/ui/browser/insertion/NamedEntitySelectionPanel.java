/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import com.google.common.base.Function;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.OvationEntity;
import us.physion.ovation.domain.Source;
import us.physion.ovation.ui.interfaces.ConnectionProvider;
import us.physion.ovation.ui.interfaces.IEntityWrapper;
import us.physion.ovation.ui.interfaces.ParameterTableModel;

/**
 *
 * @author huecotanks
 */
public abstract class NamedEntitySelectionPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider{
    
    private JLabel descriptionLabel;
    final ChangeSupport cs;
    public DataContext context;
    private JScrollPane entityScrollPane;
    private JTable jTable1;
    private JSplitPane jSplitPane1;
    private JButton addButton;
    private JButton removeButton;
    private String labelText;
    
    ParameterTableModel tableModel;
    Lookup l;
    ExplorerManager em;
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
        jTable1 = new JTable();
        jTable1.setModel(tableModel);
        jTable1.setEnabled(true);
        jSplitPane1.setRightComponent(jTable1);
        jSplitPane1.setDividerLocation(300);
        
        em = new ExplorerManager();
        l = ExplorerUtils.createLookup(em, getActionMap());
        BeanTreeView sourcesTree = new BeanTreeView();
        sourcesTree.setRootVisible(false);
        entityScrollPane.setViewportView(sourcesTree);
        
        addButton = new JButton("+");
        removeButton = new JButton("-");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                addSelectedEntity();
            }
        });
        
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                removeSelectedEntity();
            }
        });
        jSplitPane1.setAlignmentX(jSplitPane1.LEFT_ALIGNMENT);
        buttonPanel.setAlignmentX(buttonPanel.LEFT_ALIGNMENT);
        this.add(jSplitPane1);
        this.add(buttonPanel);  
        
        resetEntities();
    }
    
    @Override
    public String getName()
    {
        return getTitle();
    }
    
    abstract String getTitle();
    
    public void addSelectedEntity()
    {
        IEntityWrapper e = getSelectedEntity();
        if (e != null)
        {
            addEntity(e.getDisplayName(), e.getEntity());
        }
    }
    public void removeSelectedEntity()
    {
        int num = jTable1.getSelectedRow();
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
         if (jTable1.getCellEditor() != null)
         {
            jTable1.getCellEditor().stopCellEditing();
         }
     }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public Lookup getLookup() {
        return l;
    }

    public IEntityWrapper getSelectedEntity() {
        return l.lookup(IEntityWrapper.class);
    }
    
}
