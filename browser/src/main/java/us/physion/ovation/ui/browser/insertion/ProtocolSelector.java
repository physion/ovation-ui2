package us.physion.ovation.ui.browser.insertion;

import com.google.common.collect.Lists;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.ui.interfaces.ConnectionProvider;

/**
 *
 * @author huecotanks
 */
@Messages({
    "ProtocolSelector_Name=Select an existing Protocol, or create your own",
    "ProtocolSelector_Protocol_None=<none>",
    "ProtocolSelector_New_Protocol=New Protocol:",
    "ProtocolSelector_Add_Protocol_Button=+"
})
public class ProtocolSelector extends JScrollPane{
    DefaultListModel listModel;
    
    ChangeSupport cs;
    private DataContext context;
    
    List<Protocol> protocols;
    
    Map<String, String> newProtocols;
    Map<UUID, String> editedProtocols;
    boolean noneSelectable;
    //boolean allowEditExistingProtocols;
    
    @Override
    public String getName() {
        return Bundle.ProtocolSelector_Name();
    }
    
    public ProtocolSelector(ChangeSupport cs, boolean noneSelectable)
    {
        this(cs, Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext(), noneSelectable);
    }

    /**
     * Creates new form ProtocolSelector
     */
    public ProtocolSelector(ChangeSupport cs, DataContext ctx, boolean noneSelectable) {
        this.noneSelectable = noneSelectable;
        this.cs = cs;
        initComponents();
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.jSplitPane1.setDividerLocation(170);

        resetProtocols(ctx);
        
        jList1.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                Protocol selected = getProtocol();
                if (selected == null)
                {
                    String name = (String)listModel.get(lse.getLastIndex());
                    if (newProtocols.containsKey(name))
                    {
                        jTextArea1.setText(newProtocols.get(name));
                        jTextArea1.setEditable(true);
                    }else{
                        //this happens when <none> is selected
                        jTextArea1.setText(""); //NOI18N
                        jTextArea1.setEditable(false);
                    }
                }else{
                    jTextArea1.setText(selected.getProtocolDocument());
                    jTextArea1.setEditable(false);
                            //allowEditExistingProtocols && 
                            //context.getAuthenticatedUser().equals(selected.getOwner()));
                }
            }
        });
        
        addProtocolButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String protocolName = jTextField1.getText();
                if (protocolName == null || protocolName.isEmpty())
                    return;
                addToNewProtocolList(protocolName);
                jTextField1.setText(""); //NOI18N
                //TODO: uncomment
                //ProtocolSelector.this.cs.fireChange();
            }
        });
        
        jTextArea1.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                if (jTextArea1.isEditable()) {
                    Protocol selected = getProtocol();
                    if (selected == null) {
                        String name = (String) listModel.get(jList1.getSelectedIndex());
                        if (newProtocols.containsKey(name)) {
                            newProtocols.put(name, jTextArea1.getText());
                        }
                    } else {
                        editedProtocols.put(selected.getUuid(), jTextArea1.getText());
                    }
                    ProtocolSelector.this.cs.fireChange();
                }
            }
        });
    }
    
    public void resetProtocols()
    {   
        resetProtocols(context);
    }
    
    public void resetProtocols(DataContext context)
    {
        this.context = context;
        newProtocols = new HashMap<String, String>();
        editedProtocols = new HashMap<UUID, String>();
        DefaultListModel newModel = new DefaultListModel();
        
        protocols = Lists.newArrayList(context.getProtocols());
        for (Protocol p : protocols)
        {
            newModel.addElement(p.getName());
        }
        if (noneSelectable)
            newModel.addElement(Bundle.ProtocolSelector_Protocol_None());

        jList1.setModel(newModel);
        listModel = newModel;

    }
    
    public Protocol getProtocol()
    {
        int selected = jList1.getSelectedIndex();
        if (selected >= 0 && selected < protocols.size())
            return protocols.get(selected);
        return null;
    }
    
    protected void addToNewProtocolList(String name)
    {
        listModel.add(listModel.size() -1, name);
        newProtocols.put(name, ""); //NOI18N
    }
    
    public Map<String, String> getNewProtocols()
    {
        return newProtocols;
    }
    
    public Map<UUID, String> getEditedProtocols()
    {
        return editedProtocols;
    }
    
    public String getProtocolName()
    {
        int selected = jList1.getSelectedIndex();
        if (selected >= 0 && selected < listModel.getSize()-1)
            return (String)listModel.get(selected);
        return ""; //NOI18N
    }
    
    public void initComponents()
    {
        jSplitPane1 = new javax.swing.JSplitPane();
        protocolNamePane = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        protocolDocPane = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        addProtocolButton = new javax.swing.JButton();

        protocolNamePane.setViewportView(jList1);

        jSplitPane1.setLeftComponent(protocolNamePane);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        protocolDocPane.setViewportView(jTextArea1);

        jSplitPane1.setRightComponent(protocolDocPane);

        jLabel1.setText(Bundle.ProtocolSelector_New_Protocol());

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToNewProtocolList(jTextField1.getText());
            }
        });

        addProtocolButton.setText(Bundle.ProtocolSelector_Add_Protocol_Button());
        addProtocolButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addToNewProtocolList(jTextField1.getText());
            }
        });
        
        JPanel newProtocolPanel = new JPanel();
        newProtocolPanel.setLayout(new BoxLayout(newProtocolPanel, BoxLayout.LINE_AXIS));
        newProtocolPanel.add(jLabel1);
        newProtocolPanel.add(jTextField1);
        newProtocolPanel.add(addProtocolButton);
        
        JPanel p = new JPanel();
        this.setViewportView(p);
        p.setLayout(new BorderLayout());
        p.add(jSplitPane1, BorderLayout.CENTER);
        p.add(newProtocolPanel, BorderLayout.SOUTH);
    }
    
    private javax.swing.JButton addProtocolButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane protocolNamePane;
    private javax.swing.JScrollPane protocolDocPane;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    

}
