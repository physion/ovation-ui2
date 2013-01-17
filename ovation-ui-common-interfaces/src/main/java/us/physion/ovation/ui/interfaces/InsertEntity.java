/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.interfaces;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Iterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import ovation.IAuthenticatedDataStoreCoordinator;

/**
 *
 * @author jackie
 */
public abstract class InsertEntity extends AbstractAction implements EntityInsertable{

    public WizardDescriptor.Iterator getPanelIterator() {
	IEntityWrapper parent = getEntity();
	return new InsertEntityIterator(getPanels(parent));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
	IEntityWrapper parent = getEntity();
        WizardDescriptor wiz = new WizardDescriptor(getPanelIterator());
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wiz.setTitleFormat(new MessageFormat("{0} ({1})"));
        wiz.setTitle("Insert Entity");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            wizardFinished(wiz, Lookup.getDefault().lookup(ConnectionProvider.class).getConnection(), getEntity());
            ResettableNode node = getEntityNode();
            if (node == null)
            {
                //we've just inserted a root Source or Project, so reset the entire view for now?
       
                Collection<? extends ResetBrowser> entities = Utilities.actionsGlobalContext().lookupResult(ResetBrowser.class).allInstances();
                if (entities.size() == 1) {
                    ResetBrowser b = entities.iterator().next();
                    b.actionPerformed(null);
                }
                /*if (this instanceof InsertSource)
                {
                    BrowserUtilities.switchToSourceView();
                }
                else if (this instanceof InsertProject)
                {
                    BrowserUtilities.switchToProjectView();
                }*/
                           
            }else{
                node.resetNode();
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

}
