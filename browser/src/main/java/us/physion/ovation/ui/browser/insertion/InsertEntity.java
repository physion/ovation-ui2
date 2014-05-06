/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.browser.insertion;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import us.physion.ovation.DataContext;
import us.physion.ovation.domain.Protocol;
import us.physion.ovation.ui.interfaces.*;

/**
 *
 * @author jackie
 */
abstract public class InsertEntity extends AbstractAction implements EntityInsertable {

    @Override
    public void actionPerformed(ActionEvent ae) {
        IEntityWrapper parent =  getEntity();
        WizardDescriptor wiz = new WizardDescriptor(new InsertEntityIterator(getPanels(parent)));
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
        wiz.setTitle("Insert Entity");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            wizardFinished(wiz, Lookup.getDefault().lookup(ConnectionProvider.class).getDefaultContext(), parent);
            ResettableNode node = getEntityNode();
            if (node == null)
            {
                //we've just inserted a root Source or Project, so reset the entire view for now?
                Collection<? extends ResetBrowser> entities = Utilities.actionsGlobalContext().lookupResult(ResetBrowser.class).allInstances();
                if (entities.size() == 1) {
                    ResetBrowser b = entities.iterator().next();
                    b.actionPerformed(null);
                }
            }else{
                node.resetChildren();
            }
        }
    }

    @Override
    public int getPosition() {
        return 100;
    }

    @Override
    public int compareTo(Object t) {
        if (t instanceof EntityInsertable)
        {
            return getPosition() - ((EntityInsertable)t).getPosition();
        }
        return -1;
    }

    public IEntityWrapper getEntity()
    {
        Collection<? extends IEntityWrapper> entities = Utilities.actionsGlobalContext().lookupResult(IEntityWrapper.class).allInstances();
        if (entities.size() == 1)
        {
            return entities.iterator().next();
        }
        return null;
    }

    public ResettableNode getEntityNode()
    {
        Collection<? extends ResettableNode> entities = Utilities.actionsGlobalContext().lookupResult(ResettableNode.class).allInstances();
        if (entities.size() == 1)
        {
            return entities.iterator().next();
        }
        return null;
    }

    protected Protocol getProtocolFromProtocolSelector(DataContext context, Map<String, String> newProtocols, String selectedProtocolName, Protocol existingProtocol)
    {
        if (existingProtocol != null)
            return existingProtocol;

        if (newProtocols == null)
            return null;

        Protocol protocol = null;
        for(String name : newProtocols.keySet())
        {
            String doc = newProtocols.get(name);
            Protocol p = context.insertProtocol(
                    name,
                    newProtocols.get(name));
            if (name.equals(selectedProtocolName))
            {
                protocol = p;
            }
        }
        return protocol;
    }

    String combine(String prefix, String key)
    {
        if (prefix == null || prefix.isEmpty())
            return key;
        return prefix + "." + key;
    }
}
