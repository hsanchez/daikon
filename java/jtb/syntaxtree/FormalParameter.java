//
// Generated by JTB 1.2.2
//

package jtb.syntaxtree;

/**
 * Grammar production:
 * f0 -> [ "final" ]
 * f1 -> Type()
 * f2 -> VariableDeclaratorId()
 */
public class FormalParameter implements Node {
   private Node parent;
   public NodeOptional f0;
   public Type f1;
   public VariableDeclaratorId f2;

   public FormalParameter(NodeOptional n0, Type n1, VariableDeclaratorId n2) {
      f0 = n0;
      if ( f0 != null ) f0.setParent(this);
      f1 = n1;
      if ( f1 != null ) f1.setParent(this);
      f2 = n2;
      if ( f2 != null ) f2.setParent(this);
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

