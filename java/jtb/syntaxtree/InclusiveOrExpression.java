//
// Generated by JTB 1.2.2
//

package jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> ExclusiveOrExpression()
 * f1 -> ( "|" ExclusiveOrExpression() )*
 */
public class InclusiveOrExpression implements Node {
   private Node parent;
   public ExclusiveOrExpression f0;
   public NodeListOptional f1;

   public InclusiveOrExpression(ExclusiveOrExpression n0, NodeListOptional n1) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
      f1 = n1;
      if ( f1 != null ) f1.setParent(this);
   }

   public void accept(jtb.visitor.Visitor v) {
      v.visit(this);
   }
   public Object accept(jtb.visitor.ObjectVisitor v, Object argu) {
      return v.visit(this,argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

