//
// Generated by JTB 1.2.2
//

package jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> PrimaryExpression()
 * f1 -> [ "++" | "--" ]
 */
public class PostfixExpression implements Node {
   private Node parent;
   public PrimaryExpression f0;
   public NodeOptional f1;

   public PostfixExpression(PrimaryExpression n0, NodeOptional n1) {
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

