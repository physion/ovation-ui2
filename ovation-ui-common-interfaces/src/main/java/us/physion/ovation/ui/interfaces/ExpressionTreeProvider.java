package us.physion.ovation.ui.interfaces;

//import com.physion.ebuilder.expression.ExpressionTree;


public interface ExpressionTreeProvider {

    //public ExpressionTree getExpressionTree();
    public Object getExpressionTree();
    public void addQueryListener(QueryListener cl);
    public void removeQueryListener(QueryListener cl);

}
