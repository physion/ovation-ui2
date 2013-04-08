/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.physion.ovation.ui.query;

// import com.physion.ebuilder.expression.ExpressionTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import us.physion.ovation.ui.interfaces.ExpressionTreeProvider;
import us.physion.ovation.ui.interfaces.QueryListener;

@ActionID(category = "Query",
id = "us.physion.ovation.ui.query.CancelQuery")
@ActionRegistration(iconBase = "us/physion/ovation/ui/query/cancelQuery.png",
displayName = "#CTL_CancelQuery")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 11),
    //@ActionReference(path = "Toolbars/Find", position = 11),
    @ActionReference(path = "Shortcuts", name = "D-D"),})
@Messages("CTL_CancelQuery=Cancel Query")
public final class CancelQuery implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        final ExpressionTreeProvider etp = Lookup.getDefault().lookup(ExpressionTreeProvider.class);

        if (etp != null && etp instanceof QueryProvider) {
            final QueryProvider qp = (QueryProvider) etp;
            for (QueryListener listener : qp.getListeners()) {
                listener.cancel();
            }
        }
    }
}
